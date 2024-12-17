 /**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.exoplatform.wiki.service;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.wiki.service.plugin.WikiPageAttachmentPlugin;
import org.exoplatform.wiki.utils.Utils;
import org.springframework.util.MimeTypeUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.common.service.HTMLUploadImageProcessor;
import org.exoplatform.wiki.WikiException;
import org.exoplatform.wiki.model.Attachment;
import org.exoplatform.wiki.model.ExportList;
import org.exoplatform.wiki.model.NoteToExport;
import org.exoplatform.wiki.model.Page;
import org.exoplatform.wiki.model.WikiType;

import io.meeds.notes.model.NoteFeaturedImage;
import io.meeds.notes.model.NotePageProperties;

 public class ExportThread implements Runnable {

  private static final Log               log                          = ExoLogger.getLogger(ExportThread.class);

  private static final String            IMAGE_URL_REPLACEMENT_PREFIX = "//-";

  private static final String            IMAGE_URL_REPLACEMENT_SUFFIX = "-//";

  private static final String            EXPORT_ZIP_EXTENSION         = ".zip";

  private static final String            EXPORT_ZIP_PREFIX            = "exportzip";

  private static final String            TEMP_DIRECTORY_PATH          = "java.io.tmpdir";
  
  private static final String            FEATURED_IMAGES_DIRECTORY    = "featuredImages";

  private final NoteService              noteService;

  private final WikiService              wikiService;

  private final NotesExportService       notesExportService;

  private final HTMLUploadImageProcessor htmlUploadImageProcessor;

  private final ExportData               exportData;

  private final FileService              fileService;

  public ExportThread(NoteService noteService,
                      WikiService wikiService,
                      NotesExportService notesExportService,
                      HTMLUploadImageProcessor htmlUploadImageProcessor,
                      ExportData exportData,
                      FileService fileService) {
    this.noteService = noteService;
    this.wikiService = wikiService;
    this.notesExportService = notesExportService;
    this.htmlUploadImageProcessor = htmlUploadImageProcessor;
    this.exportData = exportData;
    this.fileService = fileService;
  }

  public static void cleanUp(List<File> files) throws IOException {
    for (File file : files) {
      if (Files.exists(file.toPath())) {
        Files.delete(file.toPath());
      }
    }
  }

  public static File zipFiles(String zipFileName,
                              List<List<File>> addToZip,
                              NotesExportService notesExportService,
                              int exportId) throws IOException {

    String zipPath = System.getProperty(TEMP_DIRECTORY_PATH) + File.separator + zipFileName;
    FileOutputStream fos = new FileOutputStream(zipPath);
    List<File> files = addToZip.getFirst();
    List<File> featuredImages = addToZip.getLast();
    try (ZipOutputStream zipOut = new ZipOutputStream(fos)) {
      addFilesToZip(zipOut, files, exportId, null, notesExportService);
      addFilesToZip(zipOut, featuredImages, exportId, FEATURED_IMAGES_DIRECTORY, notesExportService);
      zipOut.close();
      fos.close();
    } catch (InterruptedIOException e) {
      return null;
    } catch (IOException e) {
      log.warn("cannot zip files");
    }
    File zip = new File(zipPath);
    if (!zip.exists()) {
      throw new FileNotFoundException("The created zip file could not be found");
    }
    return zip;
  }

  @Override
  public void run() {
    try {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      processExport(exportData.getExportId(),
                    exportData.getNotesToExportIds(),
                    exportData.isExportAll(),
                    exportData.getIdentity());
    } catch (Exception e) {
      log.error("cannot Export Notes", e);
    } finally {
      RequestLifeCycle.end();
    }
  }

  public void processExport(int exportId, String[] notesToExportIds, boolean exportAll, Identity identity) throws Exception {

    File zipFile = null;
    List<List<File>> listFiles = new ArrayList<>();
    List<File> featuredImages = new ArrayList<>();
    ExportResource exportResource = notesExportService.getExportRessourceById(exportId);
    if (exportResource != null) {
      exportResource.setStatus(ExportStatus.IN_PROGRESS.name());
      exportResource.getAction().setStarted(true);
      exportResource.getAction().setAction(ExportAction.GETTING_NOTES);
      Page note_ = null;
      List<NoteToExport> noteToExportList = new ArrayList<>();
      if (exportAll) {
        for (String noteId : notesToExportIds) {
          try {
            Page note = noteService.getNoteById(noteId, identity);
            if (note == null) {
              log.warn("Failed to export note {}: note not find ", noteId);
              continue;
            }
            if (note_ == null)
              note_ = note;
            NoteToExport noteToExport = getNoteToExport(new NoteToExport(note.getId(),
                                                                         note.getName(),
                                                                         note.getOwner(),
                                                                         note.getAuthor(),
                                                                         note.getContent(),
                                                                         note.getSyntax(),
                                                                         note.getTitle(),
                                                                         note.getComment(),
                                                                         note.getWikiId(),
                                                                         note.getWikiType(),
                                                                         note.getWikiOwner()),
                                                        exportId);
            if (noteToExport == null) {
              exportResource = notesExportService.getExportRessourceById(exportId);
              if (exportResource.getStatus().equals(ExportStatus.CANCELLED.name())) {
                return;
              }
            }
            noteToExportList.add(noteToExport);
            exportResource.getAction().setNotesGetted(true);
          } catch (IllegalAccessException e) {
            log.error("User does not have  permissions on the note {}", noteId, e);
          } catch (Exception ex) {
            log.warn("Failed to export note {} ", noteId, ex);
            Thread.currentThread().interrupt();
          }
        }
      } else {
        List<NoteToExport> allNotesToExport = new ArrayList<>();
        int maxAncestors = 0;
        for (String noteId : notesToExportIds) {
          Page note;
          try {
            note = noteService.getNoteById(noteId, identity);
            if (note == null) {
              log.warn("Failed to export note {}: note not find ", noteId);
              continue;
            }
            NoteToExport noteToExport = new NoteToExport(note.getId(),
                                                         note.getName(),
                                                         note.getOwner(),
                                                         note.getAuthor(),
                                                         note.getContent(),
                                                         note.getSyntax(),
                                                         note.getTitle(),
                                                         note.getComment(),
                                                         note.getWikiId(),
                                                         note.getWikiType(),
                                                         note.getWikiOwner());
            noteToExport.setProperties(note.getProperties());
            noteToExport.setContent(processImagesForExport(note));
            noteToExport.setContent(processNotesLinkForExport(noteToExport));
            LinkedList<String> ancestors = getNoteAncestorsIds(noteToExport.getId());
            noteToExport.setAncestors(ancestors);
            if (ancestors.size() > maxAncestors) {
              maxAncestors = ancestors.size();
            }
            allNotesToExport.add(noteToExport);
            exportResource = notesExportService.getExportRessourceById(exportId);
            if (exportResource.getStatus().equals(ExportStatus.CANCELLED.name())) {
              notesExportService.removeExportResource(exportId);
              return;
            }
            exportResource.setExportedNotesCount(exportResource.getExportedNotesCount() + 1);
          } catch (IllegalAccessException e) {
            log.error("User does not have  permissions on the note {}", noteId, e);
          } catch (Exception ex) {
            log.warn("Failed to export note {} ", noteId, ex);
          }
        }
        exportResource.getAction().setNotesGetted(true);
        exportResource.getAction().setAction(ExportAction.UPDATING_NOTES_PARENTS);
        for (NoteToExport noteToExport : allNotesToExport) {
          noteToExport.setParent(getParentOfNoteFromExistingNotes(noteToExport.getAncestors(),
                                                                  allNotesToExport,
                                                                  notesToExportIds));
        }
        if (exportResource.getStatus().equals(ExportStatus.CANCELLED.name())) {
          notesExportService.removeExportResource(exportId);
          return;
        }
        for (int level = maxAncestors; level >= 0; level--) {
          List<NoteToExport> bottomNotes = getBottomNotesToExport(allNotesToExport, level);
          for (NoteToExport bottomNote : bottomNotes) {
            NoteToExport parent = bottomNote.getParent();
            if (parent != null) {
              List<NoteToExport> children = parent.getChildren();
              if (children != null) {
                children.add(bottomNote);
              } else {
                children = new ArrayList<>(Collections.singletonList(bottomNote));
              }
              for (NoteToExport child : children) {
                NoteToExport currentParent = new NoteToExport(parent);
                currentParent.setChildren(null);
                currentParent.setParent(null);
                child.setParent(currentParent);
              }
              parent.setChildren(children);
              allNotesToExport.remove(bottomNote);
              allNotesToExport.set(allNotesToExport.indexOf(parent), parent);
            }
          }
        }
        noteToExportList.addAll(allNotesToExport);
      }
      exportResource = notesExportService.getExportRessourceById(exportId);
      if (exportResource.getStatus().equals(ExportStatus.CANCELLED.name())) {
        notesExportService.removeExportResource(exportId);
        return;
      }
      exportResource.getAction().setNotesPrepared(true);
      exportResource.getAction().setAction(ExportAction.CREATING_CONTENT_DATA);
      if (exportResource.getStatus().equals(ExportStatus.CANCELLED.name())) {
        notesExportService.removeExportResource(exportId);
        return;
      }
      ExportList notesExport = new ExportList(new Date().getTime(), noteToExportList);
      exportResource.setNotesExport(notesExport);
      List<File> files = new ArrayList<>();
      File temp;
      temp = File.createTempFile("notesExport_" + new Date().getTime(), ".json");
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(notesExport);
      String contentUpdated = json;
      String fileName = "";
      String filePath = "";
      exportResource.getAction().setJsonCreated(true);
      processNoteFeaturedImages(exportResource, notesExport.getNotes(), featuredImages);
      exportResource.getAction().setAction(ExportAction.UPDATING_IMAGES_URLS);
      while (contentUpdated.contains(IMAGE_URL_REPLACEMENT_PREFIX)) {
        fileName = contentUpdated.split(IMAGE_URL_REPLACEMENT_PREFIX)[1].split(IMAGE_URL_REPLACEMENT_SUFFIX)[0];
        filePath = System.getProperty(TEMP_DIRECTORY_PATH) + File.separator + fileName;
        files.add(new File(filePath));
        contentUpdated = contentUpdated.replace(IMAGE_URL_REPLACEMENT_PREFIX + fileName + IMAGE_URL_REPLACEMENT_SUFFIX, "");
      }
      listFiles.add(files);
      listFiles.add(featuredImages);
      exportResource = notesExportService.getExportRessourceById(exportId);
      if (exportResource.getStatus().equals(ExportStatus.CANCELLED.name())) {
        cleanUp(files);
        cleanUp(featuredImages);
        notesExportService.removeExportResource(exportId);
        return;
      }
      exportResource.getAction().setImageUrlsUpdated(true);
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
        bw.write(json);
      }
      files.add(temp);
      exportResource = notesExportService.getExportRessourceById(exportId);
      if (exportResource.getStatus().equals(ExportStatus.CANCELLED.name())) {
        cleanUp(files);
        cleanUp(featuredImages);
        notesExportService.removeExportResource(exportId);
        return;
      }
      exportResource.getAction().setAction(ExportAction.CREATING_ZIP_FILE);
      String zipName = EXPORT_ZIP_PREFIX + exportId + EXPORT_ZIP_EXTENSION;
      exportResource.setZipFile(zipFile);
      zipFile = zipFiles(zipName, listFiles, notesExportService, exportId);
      exportResource.setZipFile(zipFile);
      exportResource = notesExportService.getExportRessourceById(exportId);
      if (exportResource.getStatus().equals(ExportStatus.CANCELLED.name())) {
        cleanUp(files);
        cleanUp(featuredImages);
        if (zipFile != null) {
          cleanUp(List.of(zipFile));
        }
        notesExportService.removeExportResource(exportId);
        return;
      }
      String date = new SimpleDateFormat("dd_MM_yyyy").format(new Date());
      if (zipFile != null && note_ != null && note_.getWikiType().toUpperCase().equals(WikiType.GROUP.name())) {
        htmlUploadImageProcessor.uploadSpaceFile(zipFile.getPath(),
                                                 note_.getWikiOwner(),
                                                 "notesExport_" + date + ".zip",
                                                 "Documents/Notes/exports");
      }
      if (zipFile != null && note_ != null && note_.getWikiType().toUpperCase().equals(WikiType.USER.name())) {
        htmlUploadImageProcessor.uploadUserFile(zipFile.getPath(),
                                                note_.getWikiOwner(),
                                                "notesExport_" + date + ".zip",
                                                "Documents/Notes/exports");
      }
      exportResource.setStatus(ExportStatus.ZIP_CREATED.name());
      exportResource.getAction().setZipCreated(true);
      exportResource.getAction().setAction(ExportAction.CLEANING_TEMP_FILE);
      cleanUp(files);
      cleanUp(featuredImages);
      exportResource.getAction().setAction(ExportAction.EXPORT_DATA_CREATED);
    }
  }

  private List<NoteToExport> getBottomNotesToExport(List<NoteToExport> allNotesToExport, int level) {
    return allNotesToExport.stream().filter(export -> export.getAncestors().size() == level).collect(Collectors.toList());
  }

  private NoteToExport getParentOfNoteFromExistingNotes(LinkedList<String> ancestors,
                                                        List<NoteToExport> exports,
                                                        String[] noteIds) {
    NoteToExport parent = null;
    Iterator<String> descendingIterator = ancestors.descendingIterator();
    String parentId = null;
    boolean parentFound = false;
    while (descendingIterator.hasNext() && !parentFound) {
      String current = descendingIterator.next();
      if (Arrays.asList(noteIds).contains(current)) {
        parentId = current;
        parentFound = true;
      }
    }
    if (parentId != null) {
      String finalParentId = parentId;
      Optional<NoteToExport> parentToExport = exports.stream().filter(export -> export.getId().equals(finalParentId)).findFirst();
      if (parentToExport.isPresent()) {
        parent = parentToExport.get();
      }
    }
    return parent;
  }

  /**
   * Recursive method to build the children and parent of a note
   *
   * @param note get the note details to be exported
   * @return
   * @throws WikiException
   */
  public NoteToExport getNoteToExport(NoteToExport note, int exportId) throws WikiException,
                                                                                          IOException,
                                                                                          InterruptedException {

    try {
      Page page = noteService.getNoteById(note.getId());
      if (page != null) {
        note.setProperties(page.getProperties());
        note.setContent(processImagesForExport(page));
      }
    } catch (Exception e) {
      log.warn("Cannot process images for note {}", note.getId());
    }
    try {
      note.setContent(processNotesLinkForExport(note));
    } catch (Exception e) {
      log.warn("Cannot process notes link for note {}", note.getId());
    }
    ExportResource exportResource = notesExportService.getExportRessourceById(exportId);
    if (exportResource != null) {
      exportResource.setExportedNotesCount(exportResource.getExportedNotesCount() + 1);
      if (exportResource.getStatus().equals(ExportStatus.CANCELLED.name())) {
        return null;
      }
    }
    List<NoteToExport> children = noteService.getChildrenNoteOf(note);
    for (NoteToExport child : children) {
      child.setParent(note);
    }
    note.setChildren(children);
    note.setParent(noteService.getParentNoteOf(note));
    for (NoteToExport child : children) {
      getNoteToExport(child, exportId);
    }
    return note;
  }

  public String processNotesLinkForExport(NoteToExport note) throws WikiException {
    String content = note.getContent();
    String noteLinkprefix = "class=\"noteLink\" href=\"(?:.*?/|)(\\d+)";
    String contentUpdated = content;
    Map<String, String> urlToReplaces = new HashMap<>();
    Pattern pattern = Pattern.compile(noteLinkprefix);
    Matcher matcher = pattern.matcher(contentUpdated);
    while (matcher.find()) {
      String matchedLink = matcher.group(0);
      String noteId;
      if (matcher.group(1) != null) {
        noteId = matcher.group(1);
      } else {
        noteId = matcher.group(2);
      }
      
      Page linkedNote = null;
      try {
        linkedNote = noteService.getNoteById(noteId);
      } catch (NumberFormatException e) {
        Page note_ = noteService.getNoteById(note.getId());
        linkedNote = noteService.getNoteOfNoteBookByName(note_.getWikiType(), note_.getWikiOwner(), noteId);
      }
      if (linkedNote != null) {
        String noteParams = IMAGE_URL_REPLACEMENT_PREFIX + linkedNote.getWikiType() + IMAGE_URL_REPLACEMENT_SUFFIX
            + IMAGE_URL_REPLACEMENT_PREFIX + linkedNote.getWikiOwner() + IMAGE_URL_REPLACEMENT_SUFFIX
            + IMAGE_URL_REPLACEMENT_PREFIX + linkedNote.getName() + IMAGE_URL_REPLACEMENT_SUFFIX;
        urlToReplaces.put(matchedLink + "\"", "class=\"noteLink\" href=\"" + noteParams + "\"");
      }
    }
    if (!urlToReplaces.isEmpty()) {
      content = replaceUrl(content, urlToReplaces);
    }
    return content;
  }

  public List<File> getFilesfromContent(NoteToExport note, List<File> files) throws WikiException {
    String contentUpdated = note.getContent();
    String fileName = "";
    String filePath = "";
    while (contentUpdated.contains("//-")) {
      fileName = contentUpdated.split("//-")[1].split("-//")[0];
      filePath = System.getProperty(TEMP_DIRECTORY_PATH) + File.separator + fileName;
      files.add(new File(filePath));
      contentUpdated = contentUpdated.replace("//-" + fileName + "-//", "");
    }
    List<NoteToExport> children = noteService.getChildrenNoteOf(note);
    for (NoteToExport child : children) {
      getFilesfromContent(child, files);
    }
    return files;
  }

  /**
   * Process images by creating images found in the content
   *
   * @param note
   * @return content
   * @throws WikiException
   */
  public String processImagesForExport(Page note) throws WikiException, IOException {
    String content = note.getContent();
    String restUploadUrl = "/portal/rest/wiki/attachments/";
    Map<String, String> urlToReplaces = new HashMap<>();
    while (content.contains(restUploadUrl)) {
      String checkContent = content;
      String urlToReplace = content.split(restUploadUrl)[1].split("\"")[0];
      urlToReplace = restUploadUrl + urlToReplace;
      String attachmentId = StringUtils.substringAfterLast(urlToReplace, "/");
      Attachment attachment = wikiService.getAttachmentOfPageByName(attachmentId, note, true);
      if (attachment != null && attachment.getContent() != null) {
        InputStream bis = new ByteArrayInputStream(attachment.getContent());
        File tempFile = new File(System.getProperty(TEMP_DIRECTORY_PATH) + File.separator + attachmentId);
        Files.copy(bis, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        urlToReplaces.put(urlToReplace, IMAGE_URL_REPLACEMENT_PREFIX + tempFile.getName() + IMAGE_URL_REPLACEMENT_SUFFIX);
      }
      content = content.replace(urlToReplace, "");
      if (content.equals(checkContent)) {
        break;
      }
    }

    List<String> contentImageIds = Utils.getContentImagesIds(content, note.getAttachmentObjectType(), note.getId());
    if (!contentImageIds.isEmpty()) {
      String noteAttachmentRestUrl = new StringBuilder("/portal/rest/v1/social/attachments/")
              .append(note.getAttachmentObjectType())
              .append("/")
              .append(note.getId())
              .append("/")
              .toString();
     contentImageIds.forEach(fileId -> {
      try {
        FileItem imageFile = fileService.getFile(Long.parseLong(fileId));
        if (imageFile != null && imageFile.getFileInfo() != null) {
          FileInfo fileInfo = imageFile.getFileInfo();
          String extension = "." + MimeTypeUtils.parseMimeType(fileInfo.getMimetype()).getSubtype();
          String filePath = System.getProperty(TEMP_DIRECTORY_PATH) + File.separator + fileId + extension;
          File file = new File(filePath);
          FileUtils.copyInputStreamToFile(imageFile.getAsStream(), file);
          urlToReplaces.put(noteAttachmentRestUrl.concat(fileId), IMAGE_URL_REPLACEMENT_PREFIX.concat(file.getName()).concat(IMAGE_URL_REPLACEMENT_SUFFIX));
        }
      } catch (Exception exception) {
        log.error("Error when processing note content image", exception);
      }
     });
    }

    if (!urlToReplaces.isEmpty()) {
      content = replaceUrl(note.getContent(), urlToReplaces);
    }
    return htmlUploadImageProcessor.processImagesForExport(content);
  }

  private String replaceUrl(String body, Map<String, String> urlToReplaces) {
    for (String url : urlToReplaces.keySet()) {
      while (body.contains(url)) {
        body = body.replace(url, urlToReplaces.get(url));
      }
    }
    return body;
  }

  private LinkedList<String> getNoteAncestorsIds(String noteId) throws WikiException {
    return getNoteAncestorsIds(null, noteId);
  }

  private LinkedList<String> getNoteAncestorsIds(LinkedList<String> ancestorsIds, String noteId) throws WikiException {
    if (ancestorsIds == null) {
      ancestorsIds = new LinkedList<>();
    }
    if (noteId == null) {
      return ancestorsIds;
    }
    Page note = noteService.getNoteById(noteId);
    String parentId = note.getParentPageId();

    if (parentId != null) {
      ancestorsIds.push(parentId);
      getNoteAncestorsIds(ancestorsIds, parentId);
    }

    return ancestorsIds;
  }

  private void processNoteFeaturedImages(ExportResource exportResource,
                                         List<NoteToExport> notesToExport,
                                         List<File> files) throws Exception {
    exportResource.getAction().setAction(ExportAction.PROCESS_FEATURED_IMAGES);
    if (notesToExport != null) {
      for (NoteToExport noteToExport : notesToExport) {
        if (noteToExport != null) {
          if (noteToExport.getProperties() != null) {
            NotePageProperties properties = noteToExport.getProperties();
            NoteFeaturedImage featuredImage = properties.getFeaturedImage();
            if (featuredImage != null) {
              FileItem imageFile = fileService.getFile(featuredImage.getId());
              if (imageFile != null && imageFile.getFileInfo() != null) {
                FileInfo fileInfo = imageFile.getFileInfo();
                String extension = "." + MimeTypeUtils.parseMimeType(fileInfo.getMimetype()).getSubtype();
                String filePath = System.getProperty(TEMP_DIRECTORY_PATH) + File.separator + featuredImage.getId() + extension;
                File file = new File(filePath);
                FileUtils.copyInputStreamToFile(imageFile.getAsStream(), file);
                files.add(file);
              }
            }
          }
          processNoteFeaturedImages(exportResource, noteToExport.getChildren(), files);
        }
      }
    }
    exportResource.getAction().setFeaturedImagesProcessed(true);
  }

  private static void addFilesToZip(ZipOutputStream zipOut,
                                    List<File> files,
                                    int exportId,
                                    String parentDirectory,
                                    NotesExportService notesExportService) throws IOException {
    if (parentDirectory != null) {
      ZipEntry directoryEntry = new ZipEntry(parentDirectory + File.separator);
      zipOut.putNextEntry(directoryEntry);
    }
    for (File fileToZip : files) {
      ExportResource exportResource = notesExportService.getExportRessourceById(exportId);
      if (exportResource.getStatus().equals(ExportStatus.CANCELLED.name())) {
        throw new InterruptedIOException();
      }
      try (FileInputStream fis = new FileInputStream(fileToZip)) {
        String name = parentDirectory != null ? parentDirectory + File.separator + fileToZip.getName() : fileToZip.getName();
        ZipEntry zipEntry = new ZipEntry(name);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
          zipOut.write(bytes, 0, length);
        }
      } catch (IOException e) {
        log.warn("cannot add the file: {} to the zip", fileToZip.getName());
      }
    }
  }
}
