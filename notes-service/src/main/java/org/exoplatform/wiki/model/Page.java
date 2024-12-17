/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.wiki.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.meeds.notes.model.NotePageProperties;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.wiki.service.BreadcrumbData;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Page {

  private String                          id;

  private String                          name;

  private String                          owner;

  private String                          author;

  private String                          authorFullName;

  private Date                            createdDate;

  private Date                            updatedDate;

  private String                          content;

  private String                          syntax;

  private String                          title;

  private String                          comment;

  private List<PermissionEntry>           permissions;

  private String                          url;

  private String                          activityId;

  private String                          wikiId;

  private String                          wikiType;

  private String                          wikiOwner;

  private String                          parentPageName;

  private String                          parentPageId;

  private String                          appName;

  private boolean                         isMinorEdit;

  private boolean                         isDraftPage = isDraftPage();

  private boolean                         toBePublished;

  private List<BreadcrumbData>            breadcrumb;

  private boolean                         canManage;

  private boolean                         canView;

  private boolean                         canImport;

  private List<Page>                      children;

  private boolean                         hasChild;

  private boolean                         isDeleted;

  private Page                            parent;

  private Map<String, List<MetadataItem>> metadatas;

  private String                          lang;

  private NotePageProperties              properties;

  private String                          attachmentObjectType;

  public Page(String name) {
    this.name = name;
  }

  public Page(String name, String title) {
    this();
    this.name = name;
    this.title = title;
  }

  public boolean isDraftPage() {
    return false;
  }

  public String getAttachmentObjectType() {
    return attachmentObjectType != null ? attachmentObjectType : "wikiPage";
  }

}
