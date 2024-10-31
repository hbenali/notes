package org.exoplatform.wiki.jpa.search;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceListAccess;
import org.exoplatform.social.core.space.SpaceListAccessType;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.utils.Utils;

import io.meeds.social.core.search.SpaceSearchConnector;

@RunWith(MockitoJUnitRunner.Silent.class)
public class WikiElasticSearchServiceConnectorTest extends AbstractKernelTest {

  private WikiElasticSearchServiceConnector searchServiceConnector;

  @Mock
  private ElasticSearchingClient elasticSearchingClient;

  @Mock
  private IdentityManager                   identityManager;

  @Mock
  private SpaceService                      spaceService;
  
  @Mock
  private SpaceStorage                      spaceStorage;

  @Mock
  private SpaceSearchConnector              spaceSearchConnector;

  @Mock
  private ConfigurationManager              configurationManager;

  private MockedStatic<CommonsUtils>        commonsUtils;

  private MockedStatic<Utils>               utils;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    commonsUtils = mockStatic(CommonsUtils.class);
    utils = mockStatic(Utils.class);
  }

  @Override
  @After
  public void tearDown() throws Exception {
    commonsUtils.close();
    utils.close();
    super.tearDown();
  }

  @Test
  public void shouldReturnResultsWithoutExcerptWhenNoHighlight() {

    Identity systemIdentity = new Identity(IdentityConstants.SYSTEM);
    ConversationState.setCurrent(new ConversationState(systemIdentity));
    commonsUtils.when(() -> CommonsUtils.getService(SpaceService.class)).thenReturn(spaceService);
    commonsUtils.when(() -> CommonsUtils.getService(IdentityManager.class)).thenReturn(identityManager);
    utils.when(() -> Utils.html2text(anyString())).thenReturn("");
    // Given
    when(elasticSearchingClient.sendRequest(anyString(),anyString()))
           .thenReturn("{\n" + "  \"took\": 939,\n" + "  \"timed_out\": false,\n" + "  \"_shards\": {\n" + "    \"total\": 5,\n"
               + "    \"successful\": 5,\n" + "    \"failed\": 0\n" + "  },\n" + "  \"hits\": {\n" + "    \"total\": 4,\n"
               + "    \"max_score\": 1.0,\n" + "    \"hits\": [{\n" + "      \"_index\": \"wiki\",\n"
               + "      \"_type\": \"wiki-page\",\n" + "      \"_id\": \"2\",\n" + "      \"_score\": 1.0,\n"
               + "      \"_source\": {\n" + "        \"wikiOwner\": \"intranet\",\n"
               + "        \"createdDate\": \"1494833363955\",\n" + "        \"name\": \"Page_1\",\n"
               + "        \"wikiType\": \"portal\",\n" + "        \"updatedDate\": \"1494833363955\",\n"
               + "        \"title\": \"Page 1\",\n" + "        \"url\": \"/portal/intranet/wiki/Page_1\"\n" + "      }\n"
               + "    }, {\n" + "      \"_index\": \"wiki\",\n" + "      \"_type\": \"wiki-page\",\n" + "      \"_id\": \"3\",\n"
               + "      \"_score\": 1.0,\n" + "      \"_source\": {\n" + "        \"wikiOwner\": \"intranet\",\n"
               + "        \"createdDate\": \"1494833380251\",\n" + "        \"name\": \"Page_2\",\n"
               + "        \"wikiType\": \"portal\",\n" + "        \"updatedDate\": \"1494833380251\",\n"
               + "        \"title\": \"Page 2\",\n" + "        \"url\": \"/portal/intranet/wiki/Page_2\"\n" + "      }\n"
               + "    }]\n" + "  }\n" + "}");

    InitParams initParams = new InitParams();
    PropertiesParam properties = new PropertiesParam();
    properties.setProperty("searchType", "wiki-es");
    properties.setProperty("displayName", "wiki-es");
    properties.setProperty("index", "wiki");
    properties.setProperty("type", "wiki,wiki-page,wiki-attachment");
    properties.setProperty("titleField", "title");
    properties.setProperty("searchFields", "name,title,content,comment,file");
    initParams.put("constructor.params", properties);
    this.searchServiceConnector = new WikiElasticSearchServiceConnector(configurationManager,
                                                                        initParams,
                                                                        elasticSearchingClient,
                                                                        identityManager) {
      @Override
      protected String getPermissionFilter() {
        return "";
      }
    };
    this.searchServiceConnector.setSearchQuery("{\n" + "  \"from\": \"@offset@\",\n" + "  \"size\": \"@limit@\",\n"
        + "  \"query\":{\n" + "    \"bool\":{\n" + "      \"filter\":{\n" + "        \"terms\":{\n"
        + "          \"permissions\": [@permissions@]\n" + "        }\n" + "      },\n" + "      \"should\": {\n"
        + "        \"match_phrase\": {\n" + "          \"summary\": {\n" + "            \"query\": \"@term@\",\n"
        + "            \"boost\": 3\n" + "          }\n" + "        }\n" + "      }\n" + " @term_query@,\n" + "      \"must_not\": {\n"
        + "        \"exists\" : { \"field\" : \"sites\" }\n" + "      }\n" + "    }\n" + " @tags_query@  },\n " + "  \"highlight\" : {\n"
        + "    \"number_of_fragments\" : 2,\n" + "    \"fragment_size\" : 150,\n" + "    \"no_match_size\" : 0,\n"
        + "    \"order\": \"score\",\n" + "    \"fields\" : {\n" + "      \"description\" : {\n"
        + "        \"pre_tags\" : [\"<span class='searchMatchExcerpt'>\"],\n" + "        \"post_tags\" : [\"</span>\"]\n"
        + "      },\n" + "      \"summary\" : {\n" + "        \"pre_tags\" : [\"<span class='searchMatchExcerpt'>\"],\n"
        + "        \"post_tags\" : [\"</span>\"]\n" + "      },\n" + "      \"location\" : {\n"
        + "        \"pre_tags\" : [\"<span class='searchMatchExcerpt'>\"],\n" + "        \"post_tags\" : [\"</span>\"]\n"
        + "      }\n" + "    }\n" + "  }\n" + "}");
    when(spaceService.getMemberSpaces("__system")).thenReturn(new SpaceListAccess(spaceStorage,
                                                                                  spaceSearchConnector,
                                                                                  "__system",
                                                                                  SpaceListAccessType.MEMBER));
    when(spaceStorage.getMemberSpacesCount("__system")).thenReturn(0);
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,"__system")).thenReturn(new org.exoplatform.social.core.identity.model.Identity("1"));

    // when
    List<String> tagNames = new ArrayList<>();
    tagNames.add("testNoteTag");
    List<SearchResult> searchResults = searchServiceConnector.searchWiki("*","__system", tagNames, false, 0, 20);

    // Then
    assertNotNull(searchResults);
    assertEquals(2, searchResults.size());
    assertEquals("", searchResults.get(0).getExcerpt());
    assertEquals("", searchResults.get(1).getExcerpt());
  }

}
