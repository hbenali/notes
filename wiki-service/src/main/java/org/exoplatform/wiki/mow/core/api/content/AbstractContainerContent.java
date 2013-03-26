/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.mow.core.api.content;

import org.exoplatform.wiki.mow.api.content.ContentItem;

/**
 * Abstract class for container content items such as paragraphs
 *
 * @version $Revision$
 */
public abstract class AbstractContainerContent extends AbstractContentItem {

  /**
   * Get text by delegating to the composite children
   */
  public String getText() {
    StringBuffer buf = new StringBuffer();
    for (ContentItem contentItem : getChildren()) {
      buf.append(contentItem.getText());
    }
    return buf.toString();
  }

}
