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

package org.exoplatform.wiki.jpa.dao;

import java.util.List;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import org.exoplatform.wiki.jpa.entity.WikiEntity;
import org.exoplatform.wiki.model.WikiType;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jun
 * 24, 2015
 */
public class WikiDAO extends WikiBaseDAO<WikiEntity, Long> {

  public List<Long> findAllIds(int offset, int limit) {
    return getEntityManager().createNamedQuery("wiki.getAllIds").setFirstResult(offset).setMaxResults(limit).getResultList();
  }

  public WikiEntity getWikiByTypeAndOwner(String wikiType, String wikiOwner) {

    //We need to add the first "/" on the wiki owner if it's  wiki group
    if (wikiType.toUpperCase().equals(WikiType.GROUP.name())) wikiOwner = validateGroupWikiOwner(wikiOwner);

    TypedQuery<WikiEntity> query = getEntityManager().createNamedQuery("wiki.getWikiByTypeAndOwner", WikiEntity.class)
                                               .setParameter("type", wikiType)
                                               .setParameter("owner", wikiOwner);

    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  public List<WikiEntity> getWikisByType(String wikiType) {
    TypedQuery<WikiEntity> query = getEntityManager().createNamedQuery("wiki.getWikisByType", WikiEntity.class)
                                               .setParameter("type", wikiType);

    return query.getResultList();
  }

}
