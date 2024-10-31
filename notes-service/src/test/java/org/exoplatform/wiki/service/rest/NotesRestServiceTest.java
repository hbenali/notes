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

package org.exoplatform.wiki.service.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.IdentityEntity;
import org.exoplatform.upload.UploadService;
import org.exoplatform.wiki.WikiException;
import org.exoplatform.wiki.model.DraftPage;
import org.exoplatform.wiki.model.Page;
import org.exoplatform.wiki.model.Wiki;
import org.exoplatform.wiki.service.BreadcrumbData;
import org.exoplatform.wiki.service.NoteService;
import org.exoplatform.wiki.service.NotesExportService;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.impl.BeanToJsons;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.tree.JsonNodeData;
import org.exoplatform.wiki.utils.NoteConstants;
import org.exoplatform.wiki.utils.Utils;

import io.meeds.notes.model.NoteFeaturedImage;

import jakarta.servlet.http.HttpServletRequest;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NotesRestServiceTest extends AbstractKernelTest {

  @Mock
  private NoteService                       noteService;

  @Mock
  private WikiService                       noteBookService;

  @Mock
  private UploadService                     uploadService;
  @Mock
  private IdentityManager identityManager;
  @Mock
  private NotesExportService                notesExportService;

  private NotesRestService                  notesRestService;

  private ResourceBundleService             resourceBundleService;

  @Mock
  private Identity                          identity;

  @Mock
  private SpaceService                      spaceService;

  @Mock
  private OrganizationService               organizationService;

  private MockedStatic<ConversationState>   conversationStateStatic;

  private MockedStatic<EnvironmentContext>  environmentContextStatic;

  private MockedStatic<Utils>               utilsStatic;

  private MockedStatic<RestUtils>           REST_UTILS;

  private MockedStatic<EntityBuilder>       ENTITY_BUILDER;

  private MockedStatic<ExoContainerContext> containerContextStatic;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    conversationStateStatic = mockStatic(ConversationState.class);
    environmentContextStatic = mockStatic(EnvironmentContext.class);
    utilsStatic = mockStatic(Utils.class);
    REST_UTILS = mockStatic(RestUtils.class);
    ENTITY_BUILDER = mockStatic(EntityBuilder.class);
    containerContextStatic = mockStatic(ExoContainerContext.class);

    this.resourceBundleService = mock(ResourceBundleService.class);
    ResourceBundle resourceBundle = mock(ResourceBundle.class);
    when(resourceBundle.getString(anyString())).thenReturn("Notes" + System.currentTimeMillis());
    when(resourceBundleService.getResourceBundle(anyString(), any())).thenReturn(resourceBundle);

    this.notesRestService = new NotesRestService(noteService,
                                                 noteBookService,
                                                 uploadService,
            identityManager,
                                                 resourceBundleService,
                                                 notesExportService);

    ConversationState currentConversationState = mock(ConversationState.class);
    conversationStateStatic.when(() -> ConversationState.getCurrent()).thenReturn(currentConversationState);
    conversationStateStatic.when(() -> ConversationState.getCurrent().getIdentity()).thenReturn(identity);

    EnvironmentContext environmentContext = mock(EnvironmentContext.class);
    environmentContextStatic.when(() -> EnvironmentContext.getCurrent()).thenReturn(environmentContext);

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getLocale()).thenReturn(new Locale("en"));
    when(environmentContext.get(HttpServletRequest.class)).thenReturn(request);

    ExoContainer exoContainer = mock(ExoContainer.class);
    containerContextStatic.when(() -> ExoContainerContext.getCurrentContainer()).thenReturn(exoContainer);
    when(exoContainer.getComponentInstanceOfType(WikiService.class)).thenReturn(noteBookService);
    when(exoContainer.getComponentInstanceOfType(NoteService.class)).thenReturn(noteService);
    when(exoContainer.getComponentInstanceOfType(SpaceService.class)).thenReturn(spaceService);
    when(exoContainer.getComponentInstanceOfType(OrganizationService.class)).thenReturn(organizationService);
    when(exoContainer.getComponentInstanceOfType(ResourceBundleService.class)).thenReturn(resourceBundleService);
  }

  @Override
  @After
  public void tearDown() throws Exception {
    conversationStateStatic.close();
    environmentContextStatic.close();
    utilsStatic.close();
    REST_UTILS.close();
    ENTITY_BUILDER.close();
    containerContextStatic.close();
    super.tearDown();
  }
  
  @Test
  public void testGetNote() throws WikiException, IllegalAccessException {
    Page homePage = new Page("home");
    homePage.setWikiOwner("user");
    homePage.setWikiType("WIKIHOME");
    homePage.setOwner("user");
    homePage.setId("1");
    homePage.setParentPageId("0");
    homePage.setHasChild(true);
    Wiki noteBook = new Wiki();
    noteBook.setOwner("user");
    noteBook.setType("WIKI");
    noteBook.setId("0");
    noteBook.setWikiHome(homePage);
    when(noteBookService.getWikiByTypeAndOwner("group", "/spaces/test")).thenReturn(noteBook);
    Page page = new Page();
    page.setId("1");
    page.setName("note1");
    page.setActivityId("1");
    page.setLang("en");
    page.setUrl("/space/test/notes/1");
    page.setContent("<h3>test</h3>");
    when(noteService.getNoteOfNoteBookByName("group", "/spaces/test", "1", identity, "source")).thenReturn(page);
    List<BreadcrumbData> breadcrumb = new ArrayList<>();
    breadcrumb.add(new BreadcrumbData("1", "test", "note", "user"));
    when(noteService.getNoteByIdAndLang(1L, identity, "source", "en")).thenReturn(page);

    when(noteService.getBreadCrumb("group", "/spaces/test", "1", "en", identity, false)).thenReturn(breadcrumb);
    Response response = notesRestService.getNote("group", "/spaces/test", "1", "source", "en");
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Page fetchedNote = (Page) response.getEntity();
    assertEquals(page.getName(), fetchedNote.getName());

    doThrow(new IllegalAccessException("Fake Exception")).when(noteService).getNoteByIdAndLang(1L, identity, "source", "en");
    Response response1 = notesRestService.getNote("group", "/spaces/test", "1", "source", "en");
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response1.getStatus());

    doThrow(new IllegalStateException("Fake Exception")).when(noteService).getNoteByIdAndLang(1L, identity, "source", "en");
    Response response2 = notesRestService.getNote("group", "/spaces/test", "1", "source", "en");
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
  }

  @Test
  public void testGetNoteById() throws WikiException, IllegalAccessException {
    Page page = new Page();
    List<Page> children = new ArrayList<>();
    children.add(new Page("child1"));
    List<BreadcrumbData> breadcrumb = new ArrayList<>();
    breadcrumb.add(new BreadcrumbData("1", "test", "note", "user"));
    page.setDeleted(true);
    when(noteService.getNoteByIdAndLang(1L, identity, "source", null)).thenReturn(null);
    Response response = notesRestService.getNoteById("1", "note", "user", true, "source", "en");
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    when(noteService.getNoteByIdAndLang(1L, identity, "source", "en")).thenReturn(page);
    Response response1 = notesRestService.getNoteById("1", "note", "user", true, "source", "en");
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response1.getStatus());
    page.setDeleted(false);
    page.setWikiType("type");
    Response response2 = notesRestService.getNoteById("1", "note", "user", true, "source", "en");
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response2.getStatus());
    page.setWikiType("note");
    page.setWikiOwner("owner");
    Response response3 = notesRestService.getNoteById("1", "note", "user", true, "source", "en");
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response3.getStatus());

    page.setWikiOwner("user");
    page.setContent("any wiki-children-pages ck-widget any");
    when(identity.getUserId()).thenReturn("userId");
    when(noteService.getChildrenNoteOf(page, false, true)).thenReturn(children);

    when(noteService.getBreadCrumb("note", "user", "1", false)).thenReturn(breadcrumb);
    when(noteService.updateNote(page)).thenReturn(page);
    Response response4 = notesRestService.getNoteById("1", "note", "user", true, "source", "en");
    assertEquals(Response.Status.OK.getStatusCode(), response4.getStatus());

    doThrow(new IllegalAccessException("Fake Exception")).when(noteService).getNoteByIdAndLang(1L, identity, "source", "en");
    Response response5 = notesRestService.getNoteById("1", "note", "user", true, "source", "en");
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response5.getStatus());

    doThrow(new IllegalStateException("Fake Exception")).when(noteService).getNoteByIdAndLang(1L, identity, "source", "en");
    Response response6 = notesRestService.getNoteById("1", "note", "user", true, "source", "en");
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response6.getStatus());
  }
  
  @Test
  public void testGetFullTreeData() throws Exception {
    Page homePage = new Page("home");
    homePage.setWikiOwner("user");
    homePage.setWikiType("WIKIHOME");
    homePage.setOwner("user");
    homePage.setId("1");
    homePage.setParentPageId("0");
    homePage.setHasChild(true);
    Wiki noteBook = new Wiki();
    noteBook.setOwner("user");
    noteBook.setType("WIKI");
    noteBook.setId("0");
    noteBook.setWikiHome(homePage);
    Page page = new Page("testPage");
    page.setName("testPage");
    page.setTitle("testPage");
    page.setId("10");
    page.setParentPageId("1");
    page.setWikiType("PAGE");
    Page page1 = new Page("testPage 1");
    page1.setName("testPage 1");
    page1.setTitle("testPage 1");
    page1.setId("11");
    page1.setParentPageId("1");
    page1.setWikiType("PAGE");
    Page page2 = new Page("testPage 2");
    page2.setName("testPage 22");
    page2.setTitle("testPage 22");
    page2.setId("12");
    page2.setParentPageId("1");
    page2.setWikiType("PAGE");
    Page page10 = new Page("testPage 10");
    page10.setName("testPage 10");
    page10.setTitle("testPage 10");
    page10.setId("13");
    page10.setParentPageId("1");
    page10.setWikiType("PAGE");
    Page page12 = new Page("testPage 12");
    page12.setName("testPage 2");
    page12.setTitle("testPage 2");
    page12.setId("14");
    page12.setParentPageId("1");
    page12.setWikiType("PAGE");
    Page draftPage = new DraftPage();
    draftPage.setId("3");
    draftPage.setName("testPageDraft");
    page.setWikiType("PAGE");
    draftPage.setParentPageId("1");
    draftPage.setDraftPage(true);
    draftPage.setWikiType("PAGE");
    WikiPageParams pageParams = new WikiPageParams();
    pageParams.setPageName("home");
    pageParams.setOwner("/spaces/test");
    pageParams.setType("group");
    List<Page> childrenWithDraft = new ArrayList<>(List.of(page, draftPage, page10, page12, page2, page1));
    List<Page> childrenWithoutDrafts = new ArrayList<>(List.of(page12, page10, page1, page, page2)); // return
                                                                                                     // an
                                                                                                     // unordered
                                                                                                     // list
    @SuppressWarnings("unchecked")
    Deque<Object> paramsDeque = mock(Deque.class);
    when(identity.getUserId()).thenReturn("1");
    GroupHandler groupHandler = mock(GroupHandler.class);
    Group group = mock(Group.class);
    when(groupHandler.findGroupById(anyString())).thenReturn(null, group, null, group, null, group, null, group);
    when (organizationService.getGroupHandler()).thenReturn(groupHandler);
    utilsStatic.when(() -> Utils.getStackParams(homePage)).thenReturn(paramsDeque);
    when(paramsDeque.pop()).thenReturn(pageParams);
    when(noteService.getNoteOfNoteBookByName(pageParams.getType(),
                                             pageParams.getOwner(),
                                             pageParams.getPageName(),
                                             identity)).thenReturn(null);
    when(noteService.getNoteOfNoteBookByName(pageParams.getType(),
                                             pageParams.getOwner(),
                                             NoteConstants.NOTE_HOME_NAME)).thenReturn(homePage);

    when(noteBookService.getWikiByTypeAndOwner(pageParams.getType(), pageParams.getOwner())).thenReturn(noteBook);
    when(noteBookService.getWikiByTypeAndOwner(homePage.getWikiType(), homePage.getWikiOwner())).thenReturn(noteBook);
    when(noteService.hasChildren(Long.parseLong(homePage.getId()))).thenReturn(true);
    when(noteService.getChildrenNoteOf(homePage, true, false)).thenReturn(childrenWithDraft);
    when(noteService.getChildrenNoteOf(homePage, false, false)).thenReturn(childrenWithoutDrafts);

    utilsStatic.when(() -> Utils.validateWikiOwner(homePage.getWikiType(), homePage.getWikiOwner())).thenCallRealMethod();
    utilsStatic.when(() -> Utils.getObjectFromParams(pageParams)).thenReturn(homePage);
    utilsStatic.when(() -> Utils.isDescendantPage(homePage, page)).thenReturn(true);
    utilsStatic.when(() -> Utils.isDescendantPage(homePage, page1)).thenReturn(true);
    utilsStatic.when(() -> Utils.isDescendantPage(homePage, page2)).thenReturn(true);
    utilsStatic.when(() -> Utils.isDescendantPage(homePage, page10)).thenReturn(true);
    utilsStatic.when(() -> Utils.isDescendantPage(homePage, page12)).thenReturn(true);
    utilsStatic.when(() -> Utils.isDescendantPage(homePage, draftPage)).thenReturn(true);
    utilsStatic.when(() -> Utils.canManageNotes(anyString(), any(Space.class), any(Page.class))).thenReturn(true);
    when(spaceService.getSpaceByGroupId(anyString())).thenReturn(mock(Space.class));
    Response response = notesRestService.getNoteTreeData("all","drafts", "group/spaces/test/home");
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    Response response3 = notesRestService.getNoteTreeData("all", "notes", "group/spaces/test/home");
    assertEquals(Response.Status.OK.getStatusCode(), response3.getStatus());
    assertEquals(5, ((JsonNodeData)((BeanToJsons) response3.getEntity()).getJsonList().getFirst()).getChildren().size());
    JsonNodeData jsonNodeData = ((JsonNodeData)((BeanToJsons) response3.getEntity()).getJsonList().getFirst());
    assertEquals(5, jsonNodeData.getChildren().size());
    assertEquals("testPage", jsonNodeData.getChildren().get(0).getName());
    assertEquals("testPage 1", jsonNodeData.getChildren().get(1).getName());
    assertEquals("testPage 2", jsonNodeData.getChildren().get(2).getName());
    assertEquals("testPage 10", jsonNodeData.getChildren().get(3).getName());
    assertEquals("testPage 22", jsonNodeData.getChildren().get(4).getName());

    doThrow(new IllegalAccessException()).when(noteService)
                                         .getNoteOfNoteBookByName(pageParams.getType(),
                                                                  pageParams.getOwner(),
                                                                  pageParams.getPageName(),
                                                                  identity);
    Response response1 = notesRestService.getNoteTreeData("all","drafts", "group/spaces/test/home");
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response1.getStatus());

    doThrow(new RuntimeException()).when(noteService)
                                   .getNoteOfNoteBookByName(pageParams.getType(),
                                                            pageParams.getOwner(),
                                                            pageParams.getPageName(),
                                                            identity);
    Response response2 = notesRestService.getNoteTreeData("all","drafts", "group/spaces/test/home");
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
  }

  @Test
  public void testGetPageAvailableTranslationLanguages() throws WikiException {
    List<String> langs = new ArrayList<>();
    langs.add("ar");
    langs.add("en");
    Response response = notesRestService.getPageAvailableTranslationLanguages(null, false);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    when(identity.getUserId()).thenReturn("1");
    when(noteService.getPageAvailableTranslationLanguages(1L, false)).thenReturn(langs);
    response = notesRestService.getPageAvailableTranslationLanguages(1L, false);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    doThrow(new WikiException()).when(noteService).getPageAvailableTranslationLanguages(2L, false);
    response = notesRestService.getPageAvailableTranslationLanguages(2L, false);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

  }

  @Test
  public void testSearchData() throws Exception {
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getPath()).thenReturn("uriPath");
    REST_UTILS.when(() -> RestUtils.getLimit(uriInfo)).thenReturn(10);
    when(identity.getUserId()).thenReturn("1");
    SearchResult searchResult = new SearchResult();
    List<SearchResult> results = new ArrayList<>();
    searchResult.setTitle("title");
    searchResult.setCreatedDate(Calendar.getInstance());
    searchResult.setUpdatedDate(Calendar.getInstance());
    searchResult.setWikiType("wikiType");
    searchResult.setWikiOwner("wikiOwner");
    searchResult.setPageName("test");
    searchResult.setLang("en");
    org.exoplatform.social.core.identity.model.Identity socialIdentity =
                                                                       mock(org.exoplatform.social.core.identity.model.Identity.class);
    searchResult.setPoster(socialIdentity);
    results.add(searchResult);
    PageList<SearchResult> pageList = mock(PageList.class);
    when(pageList.getAll()).thenReturn(results);
    when(noteService.search(any())).thenReturn(pageList);
    Page page = new Page();
    page.setId("100");
    page.setActivityId("12");
    page.setLang("en");
    page.setUrl("/space/note/100");
    when(noteService.getNoteOfNoteBookByName(searchResult.getWikiType(),
                                             searchResult.getWikiOwner(),
                                             searchResult.getPageName(),
                                             "en",
                                             identity)).thenReturn(page);
    IdentityEntity identityEntity = mock(IdentityEntity.class);
    ENTITY_BUILDER.when(() -> EntityBuilder.buildEntityIdentity(any(org.exoplatform.social.core.identity.model.Identity.class),
                                                                anyString(),
                                                                anyString()))
                  .thenReturn(identityEntity);

    Response response = notesRestService.searchData(uriInfo, "test", 10, "wikiType", "wikiOwner", true, new ArrayList<>());
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void getFeaturedImageIllustration() throws Exception {
    Request request = mock(Request.class);
    Response response = notesRestService.getFeaturedImageIllustration(request, null, false, null, null,0L);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    REST_UTILS.when(RestUtils::getCurrentUserIdentityId).thenReturn(1L);

    NoteFeaturedImage noteFeaturedImage = new NoteFeaturedImage();
    noteFeaturedImage.setId(123L);
    noteFeaturedImage.setLastUpdated(new Date().getTime());
    noteFeaturedImage.setMimeType("image/png");
    when(noteService.getNoteFeaturedImageInfo(1L, null, false, "150x150", 1L)).thenReturn(noteFeaturedImage);
    response = notesRestService.getFeaturedImageIllustration(request, 1L, false, null, "150x150", 12359547L);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    when(noteService.getNoteFeaturedImageInfo(1L, null, false, "150x150", 1L)).thenThrow(new ObjectNotFoundException("note not found"));
    response = notesRestService.getFeaturedImageIllustration(request, 1L, false, null, "150x150", 12359547L);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    reset(noteService);
    when(noteService.getNoteFeaturedImageInfo(1L, null, false, "150x150", 1L)).thenThrow(new RuntimeException());
    response = notesRestService.getFeaturedImageIllustration(request, 1L, false, null, "150x150", 12359547L);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
  }
}
