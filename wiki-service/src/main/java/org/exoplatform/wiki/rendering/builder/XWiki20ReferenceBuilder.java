/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.rendering.builder;

import org.exoplatform.wiki.service.WikiPageParams;
import org.xwiki.component.annotation.Component;

@Component("xwiki/2.0")
public class XWiki20ReferenceBuilder implements ReferenceBuilder {

  @Override
  public String build(WikiPageParams params) {
    StringBuilder sb = new StringBuilder();
    sb.append(params.getType())
      .append(wikiSpaceSeparator)
      .append(params.getOwner())
      .append(spacePageSeparator)
      .append(params.getPageId().replace(".", "\\."));
    return (sb.toString());
  }

}
