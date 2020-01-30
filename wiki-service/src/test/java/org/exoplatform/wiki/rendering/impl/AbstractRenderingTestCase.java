/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.impl;

import org.exoplatform.wiki.jpa.BaseTest;
import org.exoplatform.wiki.rendering.RenderingService;

public abstract class AbstractRenderingTestCase extends BaseTest {

  protected RenderingService renderingService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    renderingService = getContainer().getComponentInstanceOfType(RenderingService.class);
  }
}
