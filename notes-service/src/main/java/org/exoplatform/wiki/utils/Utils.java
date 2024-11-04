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

package org.exoplatform.wiki.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.suigeneris.jrcs.diff.DifferentiationFailedException;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.diff.DiffResult;
import org.exoplatform.commons.diff.DiffService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.utils.MentionUtils;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.URIWriter;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.wiki.WikiException;
import org.exoplatform.wiki.model.Attachment;
import org.exoplatform.wiki.model.Page;
import org.exoplatform.wiki.model.PageHistory;
import org.exoplatform.wiki.model.Wiki;
import org.exoplatform.wiki.model.WikiType;
import org.exoplatform.wiki.service.IDType;
import org.exoplatform.wiki.service.NoteService;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.impl.WikiPageHistory;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.service.search.WikiSearchData;

import io.meeds.notes.notifications.plugin.MentionInNoteNotificationPlugin;
import io.meeds.notes.service.NotePageViewService;
import io.meeds.social.cms.service.CMSService;

public class Utils {

  private static final Log                                       LOG                              =
                                                                     ExoLogger.getLogger(Utils.class);

  public static final String                                     SLASH                            = "SLASH";

  public static final String                                     DOT                              = "DOT";

  public static final String                                     SPACE                            = "space";

  public static final String                                     PAGE                             = "page";

  public static final String                                     NOTE_LINK                        =
                                                                           "class=\"noteLink\" href=\"//-";

  public static final String                                     ANONYM_IDENTITY                  = "__anonim";

  private static final Log                                       log_                             =
                                                                      ExoLogger.getLogger(Utils.class);

  public static final String                                     COMPARE_REVISION                 = "CompareRevision";

  public static final String                                     VER_NAME                         = "verName";

  final private static String                                    MIMETYPE_TEXTHTML                = "text/html";

  private static final Map<String, Map<String, WikiPageHistory>> editPageLogs                     = new HashMap<>();

  public static final String                                     WIKI_RESOUCE_BUNDLE_NAME         =
                                                                                          "locale.wiki.service.WikiService";

  private static final String                                    ILLEGAL_SEARCH_CHARACTERS        = "\\!^()+{}[]:-\"";

  private static final String                                    ILLEGAL_NAME_CHARACTERS          = "*|\":[]/',^<>";

  public static final String                                     SPLIT_TEXT_OF_DRAFT_FOR_NEW_PAGE = "_A_A_";

  public static final String                                     NOTES_METADATA_OBJECT_TYPE       = "notes";

  public static final String                                     WIKI_APP_ID                      = "ks-wiki:spaces";

  public static final String                                     PAGE_ID_KEY                      = "page_id";

  public static final String                                     PAGE_TYPE_KEY                    = "page_type";

  public static final String                                     PAGE_OWNER_KEY                   = "page_owner";

  public static final Pattern                                    MENTION_PATTERN                  =
                                                                                 Pattern.compile("@([^\\s]+)|@([^\\s]+)$");

  public static String normalizeUploadedFilename(String name) {
    name = name.replace("%22", "\"");  // Fix the bug in Chrome which a double quotes is encoded to %22
    name = name.replace("\\\"", "\"");  // Fix the bug in Firefox which a double quotes is escaped to \\"

    name = Utils.escapeIllegalCharacterInName(name);
    return name;
  }

  public static String escapeIllegalCharacterInQuery(String query) {
    String ret = query;
    if (ret != null) {
      for (char c : ILLEGAL_SEARCH_CHARACTERS.toCharArray()) {
        ret = ret.replace(c + "", "\\" + c);
      }
      ret = ret.replace("'", "''");
    }
    return ret;
  }
  
  public static String escapeIllegalCharacterInName(String name) {
    if (name == null) return null;
    else if (".".equals(name)) return "_";
    else {
      int first = name.indexOf('.');
      int last = name.lastIndexOf('.');
      //if only 1 dot character
      if (first != -1 && first == last && ( first == 0 || last == name.length() - 1)) {
        name = name.replace('.', '_');
      } 
      for (char c : ILLEGAL_NAME_CHARACTERS.toCharArray())
        name = name.replace(c, '_');
      return name;
    }
  }
  
  public static String getPortalName() {
    return PortalContainer.getCurrentPortalContainerName();
  }
  
  /**
   * Get resource bundle from given resource file
   *
   * @param key key
   * @param cl ClassLoader to load resource file
   * @return The value of key in resource bundle
   */
  public static String getWikiResourceBundle(String key, ClassLoader cl) {
    Locale locale = WebuiRequestContext.getCurrentInstance().getLocale();
    ResourceBundle resourceBundle = ResourceBundle.getBundle(WIKI_RESOUCE_BUNDLE_NAME, locale,cl);
    return resourceBundle.getString(key);
  }
  
  /**
   * Log the edit page action of user
   * 
   * @param pageParams The page that has been editing
   * @param username The name of user that editing wiki page
   * @param updateTime The time that this page is edited
   * @param draftName The name of draft for this edit
   * @param isNewPage Is the wiki page a draft or not
   */
  public static void logEditPageTime(WikiPageParams pageParams, String username, long updateTime, String draftName, boolean isNewPage) {
    String pageId = pageParams.getPageName();
    Map<String, WikiPageHistory> logByPage = editPageLogs.get(pageId);
    if (logByPage == null) {
      logByPage = new HashMap<String, WikiPageHistory>();
      editPageLogs.put(pageId, logByPage);
    }
    WikiPageHistory logByUsername = logByPage.get(username);
    if (logByUsername == null) {
      logByUsername = new WikiPageHistory(pageParams, username, draftName, isNewPage);
      logByPage.put(username, logByUsername);
    }
    logByUsername.setEditTime(updateTime);
  }
  
  /**
   * removes the log of user editing page.
   * @param pageParams wiki page params
   * @param user current userName
   */
  public static void removeLogEditPage(WikiPageParams pageParams, String user) {
    String pageId = pageParams.getPageName();
    Map<String, WikiPageHistory> logByPage = editPageLogs.get(pageId);
    if (logByPage != null) {
      logByPage.remove(user);
    }
  }

  /**
   * get user identity.
   * @param userId current userName
   *
   * @return the full name of the user
   */
  public static String getIdentityUser( String userId) {
    IdentityManager identityManager = ExoContainerContext.getService(IdentityManager.class);
    Identity userIdentity =  identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
   return userIdentity.getProfile().getFullName();
  }

  /**
   * Get the permalink of current wiki page <br>
   *
   * With the current page param:
   * <ul>
   *   <li>type = "group"</li>
   *   <li>owner = "spaces/test_space"</li>
   *   <li>pageId = "test_page"</li>
   * </ul>
   * <br>
   *
   *  The permalink will be:
   * <ul>
   *   <li>http://int.exoplatform.org/portal/intranet/wiki/group/spaces/test_space/test_page</li>
   * </ul>
   * <br>
   *
   * @param params the wiki oage parms
   * @param hasDowmainUrl if page has domain url
   * @return The permalink of current wiki page
   * @throws Exception if error occured
   */
  public static String getPermanlink(WikiPageParams params, boolean hasDowmainUrl) throws Exception {
    WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
    
    // get wiki webapp name
    String wikiWebappUri = wikiService.getWikiWebappUri();
    
    // Create permalink
    StringBuilder sb = new StringBuilder(wikiWebappUri);
    sb.append("/");
    if (!params.getType().equalsIgnoreCase(WikiType.PORTAL.toString())) {
      sb.append(params.getType().toLowerCase());
      sb.append("/");
      sb.append(org.exoplatform.wiki.utils.Utils.validateWikiOwner(params.getType(), params.getOwner()));
      sb.append("/");
    }
    
    if (params.getPageName() != null) {
      sb.append(params.getPageName());
    }
    
    if (hasDowmainUrl) {
      return getDomainUrl() + fillPortalName(sb.toString());
    }
    return fillPortalName(sb.toString());
  }

  private static String getDomainUrl() {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    StringBuilder domainUrl = new StringBuilder();
    domainUrl.append(portalRequestContext.getRequest().getScheme());
    domainUrl.append("://");
    domainUrl.append(portalRequestContext.getRequest().getServerName());
    int port = portalRequestContext.getRequest().getServerPort();
    if (port != 80) {
      domainUrl.append(":");
      domainUrl.append(port);
    }
    return domainUrl.toString();
  }
  
  private static String fillPortalName(String url) {
    RequestContext ctx = RequestContext.getCurrentInstance();
    NodeURL nodeURL =  ctx.createURL(NodeURL.TYPE);
    NavigationResource resource = new NavigationResource(SiteType.PORTAL, Util.getPortalRequestContext().getPortalOwner(), url);
    return nodeURL.setResource(resource).toString(); 
  }

  /**
   * Get the editting log of wiki page
   * 
   * @param pageId The id of wiki page to get log
   * @return The editting log of wiki pgae
   */
  public static Map<String, WikiPageHistory> getLogOfPage(String pageId) {
    Map<String, WikiPageHistory> logByPage = editPageLogs.get(pageId);
    if (logByPage == null) {
      logByPage = new HashMap<String, WikiPageHistory>();
    }
    return logByPage;
  }
  
  /**
   * Validate {@code wikiOwner} depending on {@code wikiType}. <br>
   * If wikiType is {@link PortalConfig#GROUP_TYPE}, {@code wikiOwner} is checked to removed slashes at the begin and the end point of it.
   * @param wikiType the wiki type
   * @param wikiOwner the wiki owner
   * @return wikiOwner after validated.
   */ 
  public static String validateWikiOwner(String wikiType, String wikiOwner){
    if(wikiType != null && wikiType.equals(PortalConfig.GROUP_TYPE) && StringUtils.isNotEmpty(wikiOwner)) {
      if(wikiOwner.startsWith("/")){
        wikiOwner = wikiOwner.substring(1,wikiOwner.length());
      }
      if(wikiOwner.endsWith("/")){
        wikiOwner = wikiOwner.substring(0,wikiOwner.length()-1);
      }
    }
    return wikiOwner;
  }
  
  public static String getDefaultRestBaseURI() {
    StringBuilder sb = new StringBuilder();
    sb.append("/");
    sb.append(PortalContainer.getCurrentPortalContainerName());
    sb.append("/");
    sb.append(PortalContainer.getCurrentRestContextName());
    return sb.toString();
  }
  
  public static String getDocumentURL(WikiContext wikiContext) {
    if (wikiContext.getPortalURL() == null && wikiContext.getPortletURI() == null) {
      return wikiContext.getPageName();
    }
    StringBuilder sb = new StringBuilder();
    sb.append(wikiContext.getPortalURL());
    sb.append(wikiContext.getPortletURI());
    sb.append("/");
    if (!PortalConfig.PORTAL_TYPE.equalsIgnoreCase(wikiContext.getType())) {
      sb.append(wikiContext.getType().toLowerCase());
      sb.append("/");
      sb.append(Utils.validateWikiOwner(wikiContext.getType(), wikiContext.getOwner()));
      sb.append("/");
    }
    sb.append(wikiContext.getPageName());
    return sb.toString();
  }
  
  public static String getCurrentUser() {
    ConversationState conversationState = ConversationState.getCurrent();
    if (conversationState != null) {
      return ConversationState.getCurrent().getIdentity().getUserId();
    }
    return null; 
  }
  
  public static boolean isDescendantPage(Page page, Page parentPage) throws WikiException {
    NoteService noteService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NoteService.class);
    // if page and parentPage are the same page, it is considered as a descendant
    if(page.getWikiType().equals(parentPage.getWikiType()) && page.getWikiOwner().equals(parentPage.getWikiOwner())
            && page.getName().equals(parentPage.getName())) {
      return true;
    }
    Page parentOfPage = noteService.getParentNoteOf(page);
    // we reach the Wiki root
    if(parentOfPage == null) {
      return false;
    }
    // if the parent of the given page is the same than the parentPage, page is a descendant of parentPage
    if(parentOfPage.getWikiType().equals(parentPage.getWikiType()) && parentOfPage.getWikiOwner().equals(parentPage.getWikiOwner())
            && parentOfPage.getName().equals(parentPage.getName())) {
      return true;
    } else {
      // otherwise we continue to go up in the page tree
      return isDescendantPage(parentOfPage, parentPage);
    }
  }
  
  public static Object getObjectFromParams(WikiPageParams param) throws WikiException {
    NoteService noteService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NoteService.class);
    WikiService wikiService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
    String wikiType = param.getType();
    String wikiOwner = param.getOwner();
    String wikiPageId = param.getPageName();

    if (wikiOwner != null && wikiPageId != null) {
      if (!wikiPageId.equals(NoteConstants.NOTE_HOME_NAME)) {
        // Object is a page
        return noteService.getNoteByRootPermission(wikiType, wikiOwner, wikiPageId);
      } else {
        // Object is a Home page
        Wiki wiki = wikiService.getWikiByTypeAndOwner(wikiType, wikiOwner);
        if(wiki != null) {
          Page wikiHome = wiki.getWikiHome();
          return wikiHome;
        } else {
          return null;
        }
      }
    } else if (wikiOwner != null) {
      // Object is a wiki
      Wiki wiki =  wikiService.getWikiByTypeAndOwner(wikiType.toUpperCase(), wikiOwner);
      return wiki;
    } else if (wikiType != null) {
      // Object is a space
      return wikiType;
    } else {
      return null;
    }
  }
  
  public static Deque<WikiPageParams> getStackParams(Page page) throws WikiException {
    WikiService wikiService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
    NoteService noteService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NoteService.class);
    Deque<WikiPageParams> stack = new ArrayDeque<>();
    Wiki wiki = wikiService.getWikiByTypeAndOwner(page.getWikiType(), page.getWikiOwner());
    if (wiki != null) {
      while (page != null) {
        stack.push(new WikiPageParams(wiki.getType(), wiki.getOwner(), page.getName()));
        page = noteService.getParentNoteOf(page);
      }      
    }
    return stack;
  }
  
  
  public static WikiPageParams getWikiPageParams(Page page) {
    WikiService wikiService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
    try {
      Wiki wiki = wikiService.getWikiByTypeAndOwner(page.getWikiType(), page.getWikiOwner());
      String wikiType = wiki.getType();
      WikiPageParams params = new WikiPageParams(wikiType, wiki.getOwner(), page.getName());
      return params;
    } catch(Exception e) {
      log_.error("Cannot build wiki page params from wiki page " + page.getWikiType() + ":" + page.getWikiOwner()
              + ":" + page.getName() + " - Cause : " + e.getMessage(), e);
      return null;
    }
  }
  
  public static String getWikiOnChangeContent(Page page)
          throws WikiException, DifferentiationFailedException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    NoteService wikiService = container.getComponentInstanceOfType(NoteService.class);
    DiffService diffService = container.getComponentInstanceOfType(DiffService.class);
    
    // Get differences
    String currentVersionContent = page.getContent() != null ? new String(page.getContent()) : StringUtils.EMPTY;
    List<PageHistory> versions = wikiService.getVersionsHistoryOfNote(page,"");
    String previousVersionContent = StringUtils.EMPTY;
    if(versions != null && !versions.isEmpty()) {
      PageHistory previousVersion = versions.get(0);
      previousVersionContent = previousVersion.getContent();
    }
    DiffResult diffResult = diffService.getDifferencesAsHTML(previousVersionContent,
                                                             currentVersionContent,
                                                             false);
    
    if (diffResult.getChanges() == 0) {
      diffResult.setDiffHTML("No changes, new revision is created.");
    } 

    StringBuilder sbt = new StringBuilder();
    sbt.append("<html>")
        .append("  <body>")
            .append(insertStyle(diffResult.getDiffHTML()))
            .append("  </body>")
            .append("</html>");
    return sbt.toString();
  }
  
  private static boolean isEnabledUser(String userName) throws WikiException {
    OrganizationService orgService = ExoContainerContext.getService(OrganizationService.class);
    try {
      return orgService.getUserHandler().findUserByName(userName) != null;
    } catch (Exception e) {
      throw new WikiException("Cannot check if user " + userName + " is enabled", e);
    }
  }
  
  public static String getEmailUser(String userName) throws WikiException {
    OrganizationService organizationService = ExoContainerContext.getCurrentContainer()
            .getComponentInstanceOfType(OrganizationService.class);
    User user;
    try {
      user = organizationService.getUserHandler().findUserByName(userName);
      String email = user.getEmail();
      return email;
    } catch (Exception e) {
      throw new WikiException("Cannot get email of user " + userName, e);
    }
  }
  
  public static HashMap<String, IDType> getACLForAdmins() {
    HashMap<String, IDType> permissionMap = new HashMap<String, IDType>();
    UserACL userACL = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);
    permissionMap.put(userACL.getSuperUser(), IDType.USER);
    for (String group : userACL.getPortalCreatorGroups()) {
      if (!StringUtils.isEmpty(group)) {
        permissionMap.put(group, IDType.MEMBERSHIP);
      }
    }
    return permissionMap;
  }

  private static String insertStyle(String rawHTML) {
    String result = rawHTML;
    result = result.replaceAll("class=\"diffaddword\"", "style=\"background: #b5ffbf;\"");
    result = result.replaceAll("<span class=\"diffremoveword\">",
                               "<span style=\" background: #ffd8da;text-decoration: line-through;\">");
    result = result.replaceAll("<pre class=\"diffremoveword\">",
                               "<pre style=\" background: #ffd8da;\">");
    return result;
  }
  
  /*
   * get URL to public on social activity
   */
  public static String getURL(String url, String verName){
    StringBuffer strBuffer = new StringBuffer();
    strBuffer.append(url).append("?").append(WikiContext.ACTION).append("=").append(COMPARE_REVISION).append("&").append(VER_NAME).append("=").append(verName);
    return strBuffer.toString();
  }
  
  public static long countSearchResult(WikiSearchData data) throws Exception {
    data.setOffset(0);
    data.setLimit(Integer.MAX_VALUE);
    NoteService noteService = (NoteService) PortalContainer.getComponent(NoteService.class);
    PageList<SearchResult> results = noteService.search(data);
    return results.getAll().size();

  }
  
  public static String getAttachmentCssClass(Attachment attachment, String append) throws Exception {
    Class<?> dmsMimeTypeResolverClass = Class.forName("org.exoplatform.services.cms.mimetype.DMSMimeTypeResolver");
    Object dmsMimeTypeResolverObject =
        dmsMimeTypeResolverClass.getDeclaredMethod("getInstance", null).invoke(null, null);
    Object mimeType = dmsMimeTypeResolverClass
      .getMethod("getMimeType", new Class[] { String.class})
      .invoke(dmsMimeTypeResolverObject, new Object[]{new String(attachment.getFullTitle().toLowerCase())});

    StringBuilder cssClass = new StringBuilder();
    cssClass.append(append);
    cssClass.append("FileDefault");
    cssClass.append(" ");
    cssClass.append(append);
    cssClass.append("nt_file");
    cssClass.append(" ");
    cssClass.append(append);
    cssClass.append(((String)mimeType).replaceAll("/|\\.", ""));
    return cssClass.toString();
  }

  /**
   * gets rest context name
   * @return rest context name
   */
  public static String getRestContextName() {
    return PortalContainer.getCurrentRestContextName();
  }

  public static String getPageUrl(Page page){
    String spaceUri = getSpacesURI(page);
    StringBuilder spaceUrl = new StringBuilder("/portal");
    spaceUrl.append(spaceUri);
    spaceUrl.append("/notes/");
    if (!StringUtils.isEmpty(page.getId())) {
      spaceUrl.append(page.getId());
    }
    return spaceUrl.toString();
  }

  public static String getSpacesURI(Page page) {
    try {
    QualifiedName REQUEST_HANDLER = QualifiedName.create("gtn", "handler");
    QualifiedName REQUEST_SITE_TYPE = QualifiedName.create("gtn", "sitetype");
    QualifiedName REQUEST_SITE_NAME = QualifiedName.create("gtn", "sitename");
    QualifiedName PATH = QualifiedName.create("gtn", "path");
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    WebAppController webAppController = CommonsUtils.getService(WebAppController.class);
    Router router = webAppController.getRouter();
      Space space = spaceService.getSpaceByGroupId(page.getWikiOwner());
      if(space==null){
        return "";
      }
      Map<QualifiedName, String> qualifiedName = new HashedMap();
      qualifiedName.put(REQUEST_HANDLER, "portal");
      qualifiedName.put(REQUEST_SITE_TYPE, "group");

        StringBuilder urlBuilder = new StringBuilder();
        qualifiedName.put(REQUEST_SITE_NAME, space.getGroupId());
        qualifiedName.put(PATH, space.getPrettyName());
        router.render(qualifiedName, new URIWriter(urlBuilder));
        return(urlBuilder.toString());

    } catch (Exception e) {
      return "";
    }
  }

  public static List<String> unzip(String zipFilePath, String folderPath) throws IOException {
    Path destDirPath = Paths.get(folderPath).toAbsolutePath().normalize();
    if (!Files.exists(destDirPath)) {
      Files.createDirectories(destDirPath);
    }

    List<String> extractedFiles = new ArrayList<>();

    try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
      ZipEntry entry;

      while ((entry = zipIn.getNextEntry()) != null) {
        Path entryPath = Paths.get(entry.getName()).normalize();
        Path targetPath = destDirPath.resolve(entryPath);

        if (!targetPath.toAbsolutePath().startsWith(destDirPath)) {
          throw new IOException("Potential Zip Slip detected: " + entry.getName());
        }

        if (entry.isDirectory()) {
          Files.createDirectories(targetPath);
        } else {
          Path parentDir = targetPath.getParent();
          if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
          }

          extractFile(zipIn, targetPath.toFile());
          extractedFiles.add(targetPath.toString());
        }

        zipIn.closeEntry();
      }
    }
    return extractedFiles;
  }

  private static void extractFile(ZipInputStream zipIn, File targetFile) throws IOException {
    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile))) {
      byte[] buffer = new byte[1024];
      int len;
      while ((len = zipIn.read(buffer)) > 0) {
        bos.write(buffer, 0, len);
      }
    }
  }

  public static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));) {
      byte[] bytesIn = new byte[4096];
      int read = 0;
      while ((read = zipIn.read(bytesIn)) != -1) {
        bos.write(bytesIn, 0, read);
      }
    }
  }

  public static String html2text(String html) {
    if (StringUtils.isBlank(html)) {
      return html;
    }
    Document doc = Jsoup.parse(html);
    return doc.text();
  }

  public static <S, D> void broadcast(ListenerService listenerService, String eventName, S source, D data) {
    try {
      listenerService.broadcast(eventName, source, data);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event: {}. Wheres, the operation will continue to proceed", eventName, e);
    }
  }
  
  public static boolean canManageNotes(String authenticatedUser, Space space, Page page) throws WikiException {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    CMSService cmsService = CommonsUtils.getService(CMSService.class);
    if (space != null) {
      return spaceService.canRedactOnSpace(space, authenticatedUser);
    } else if (StringUtils.equals(page.getOwner(), IdentityConstants.SYSTEM)) {
      return cmsService.hasEditPermission(getIdentity(authenticatedUser), NotePageViewService.CMS_CONTENT_TYPE, page.getName());
    } else {
      return StringUtils.equals(page.getOwner(), authenticatedUser);
    }
  }
  
  public static org.exoplatform.services.security.Identity getIdentity(String username) {
    if (StringUtils.isBlank(username)) {
      return null;
    }
    IdentityRegistry identityRegistry = CommonsUtils.getService(IdentityRegistry.class);
    org.exoplatform.services.security.Identity aclIdentity = identityRegistry.getIdentity(username);
    if (aclIdentity == null) {
      try {
        OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
        List<MembershipEntry> entries = organizationService.getMembershipHandler()
                                                           .findMembershipsByUser(username)
                                                           .stream()
                                                           .map(membership -> new MembershipEntry(membership.getGroupId(),
                                                                                                  membership.getMembershipType()))
                                                           .toList();
        aclIdentity = new org.exoplatform.services.security.Identity(username, entries);
        identityRegistry.register(aclIdentity);
      } catch (Exception e) {
        throw new IllegalStateException("Unable to retrieve user " + username + " memberships", e);
      }
    }
    return aclIdentity;
  }

  public static Set<String> processMentions(String content, Space space) {
    Set<String> mentions = new HashSet<>();
    mentions.addAll(MentionUtils.getMentionedUsernames(content));

    if (space != null) {
      IdentityStorage identityStorage = CommonsUtils.getService(IdentityStorage.class);
      String spaceIdentityId = identityStorage.findIdentityId(SpaceIdentityProvider.NAME, space.getPrettyName());
      Set<String> mentionedRoles = MentionUtils.getMentionedRoles(content, spaceIdentityId);
      mentionedRoles.forEach(role -> {
        if (StringUtils.equals("member", role) && space.getMembers() != null) {
          mentions.addAll(Arrays.asList(space.getMembers()));
        } else if (StringUtils.equals("manager", role) && space.getManagers() != null) {
          mentions.addAll(Arrays.asList(space.getManagers()));
        } else if (StringUtils.equals("redactor", role) && space.getRedactors() != null) {
          mentions.addAll(Arrays.asList(space.getRedactors()));
        } else if (StringUtils.equals("publisher", role) && space.getPublishers() != null) {
          mentions.addAll(Arrays.asList(space.getPublishers()));
        }
      });
    }

    return mentions.stream().map(remoteId -> {
      IdentityStorage identityStorage = CommonsUtils.getService(IdentityStorage.class);

      Identity identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, remoteId);
      return identity == null ? null : identity.getId();
    }).filter(Objects::nonNull).collect(Collectors.toSet());
  }

  public static void sendMentionInNoteNotification(Page note, Page originalNote, String currentUser) {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Space space = spaceService.getSpaceByGroupId(note.getWikiOwner());
    org.exoplatform.social.core.identity.model.Identity identity = identityManager.getOrCreateUserIdentity(note.getAuthor());
    String authorAvatarUrl = LinkProviderUtils.getUserAvatarUrl(identity.getProfile());
    String authorProfileUrl = identity.getProfile().getUrl();
    Set<String> mentionedIds = Utils.processMentions(note.getContent(), space);
    if (originalNote != null) {
      Set<String> previousMentionedIds = Utils.processMentions(originalNote.getContent(), space);
      mentionedIds = mentionedIds.stream().filter(id -> !previousMentionedIds.contains(id)).collect(Collectors.toSet());
    }
    NotificationContext mentionNotificationCtx =
            NotificationContextImpl.cloneInstance()
                    .append(MentionInNoteNotificationPlugin.CURRENT_USER,
                            currentUser)
                    .append(MentionInNoteNotificationPlugin.NOTE_AUTHOR,
                            note.getAuthor())
                    .append(MentionInNoteNotificationPlugin.SPACE_ID,
                            space.getId())
                    .append(MentionInNoteNotificationPlugin.NOTE_URL,
                            note.getUrl())
                    .append(MentionInNoteNotificationPlugin.NOTE_TITLE,
                            note.getTitle())
                    .append(MentionInNoteNotificationPlugin.AUTHOR_AVATAR_URL,
                            authorAvatarUrl)
                    .append(MentionInNoteNotificationPlugin.AUTHOR_PROFILE_URL,
                            authorProfileUrl)
                    .append(MentionInNoteNotificationPlugin.ACTIVITY_LINK,
                            note.getUrl())
                    .append(MentionInNoteNotificationPlugin.MENTIONED_IDS,
                            mentionedIds);
    mentionNotificationCtx.getNotificationExecutor()
            .with(mentionNotificationCtx.makeCommand(PluginKey.key(MentionInNoteNotificationPlugin.ID)))
            .execute(mentionNotificationCtx);
  }

}
