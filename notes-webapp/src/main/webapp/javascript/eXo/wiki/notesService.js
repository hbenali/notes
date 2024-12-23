import { notesConstants } from './notesConstants.js';

export function getNote(noteBookType, noteBookOwner, noteId,source,lang) {
let url = `${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/${noteBookType}/${noteBookOwner}/${noteId}`;
  if (source){
    url=`${url}?source=${source}`;
  }
  if (lang){
    url=`${url}${getSeparator(url)}lang=${lang}`;
  }
  return fetch(url, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
} 

export function getDraftNoteById(noteId, lang) {
  let url = `${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/draftNote/${noteId}`;
  if (lang){
    url=`${url}${getSeparator(url)}lang=${lang}`;
  }
  return fetch(url , {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getLatestDraftOfPage(noteId, lang) {
  let url = `${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/latestDraftNote/${noteId}`;
  if (lang){
    url=`${url}${getSeparator(url)}lang=${lang}`;
  }
  return fetch(url, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getNoteById(noteId,lang,source,type,owner,withChildren) {
  let url = `${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/${noteId}`;
  if (source){
    url=`${url}${getSeparator(url)}source=${source}`;
  } if (type){
    url=`${url}${getSeparator(url)}noteBookType=${type}`;
  } if (owner){
    url=`${url}${getSeparator(url)}noteBookOwner=${owner}`;
  } if (withChildren){
    url=`${url}${getSeparator(url)}withChildren=${withChildren}`;
  } if (lang){
    url=`${url}${getSeparator(url)}lang=${lang}`;
  }
  return fetch(url, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
} 

export function getSeparator(url) {
  return url.indexOf('?') !== -1 ? '&' : '?';
}

export function getNoteTree(noteBookType, noteBookOwner, noteId, treeType, noteType) {
  if (noteBookOwner.indexOf('/') !== 0) {
    noteBookOwner = `/${noteBookOwner}`;
  }
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/tree/${treeType}/${noteType}?path=${noteBookType}${noteBookOwner}/${noteId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getNoteTreeLevel(path) {
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/tree/children/notes?path=${path}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Error while getting note tree level');
    } else {
      return resp.json();
    }
  });
}

export function createNote(page) {
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note`, {
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json'
    },
    method: 'POST',
    credentials: 'include',
    body: JSON.stringify(page)
  }).then((resp) => {
    if (!resp || !resp.ok) {
      if (resp.status===409) {
        throw new Error('error.duplicate.title', resp);
      } else {
        throw new Error('error', resp);
      }
    } else {
      return resp.json();    }
  });
}

export function saveDraftNote(draftNote, parentPageId) {
  if (parentPageId) {
    draftNote.parentPageId = parentPageId;
  }
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/saveDraft`, {
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json'
    },
    method: 'POST',
    credentials: 'include',
    body: JSON.stringify(draftNote)
  }).then((resp) => {
    if (!resp || !resp.ok) {
      if (resp.status === 409) {
        throw new Error('error.duplicate.title', resp);
      } else {
        throw new Error('error', resp);
      }
    } else {
      return resp.json();
    }
  });
}

export function updateNote(note) {
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/${note.wikiType}/${note.wikiOwner}/${note.name}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify(note)
  }).then(resp => {
    if (!resp || !resp.ok) {
      if (resp.status===409){
        throw new Error('error.duplicate.title', resp);
      } else {
        throw new Error('error', resp);
      }
    } else {
      return resp.json();
    }
  });
}

export function updateNoteById(note) {
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/${note.id}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(note),
  }).then(resp => {
    if (!resp || !resp.ok) {
      if (resp.status===409){
        throw new Error('error.duplicate.title', resp);
      } else {
        throw new Error('error', resp);
      }
    } else {
      return resp.json();
    }
  });
}

export function restoreNoteVersion(note,version) {
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/restore/${version}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(note),
  }).then(resp => {
    if (!resp || !resp.ok) {
      if (resp.status===409){
        throw new Error('error.duplicate.title', resp);
      } else {
        throw new Error('error', resp);
      }
    } else {
      return resp.json();
    }
  });
}

export function deleteNotes(note) {
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/${note.id}`, {
    credentials: 'include',
    method: 'DELETE',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp;
    } else {
      throw new Error('Error when deleting notes');
    }
  });
}

export function deleteDraftNote(note) {
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/draftNote/${note.id}`, {
    credentials: 'include',
    method: 'DELETE',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp;
    } else {
      throw new Error('Error when deleting draft');
    }
  });
}

export function moveNotes(note,destination) {
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/move/${note.id}/${destination.id}`, {
    credentials: 'include',
    method: 'PATCH',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp;
    } else {
      throw new Error('Error when moving notes');
    }
  });
}

export function importZipNotes(noteId,uploadId,overrideMode) {
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/import/${noteId}/${uploadId}?conflict=${overrideMode}`, {
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json'
    },
    method: 'POST',
    credentials: 'include',
  }).then((resp) => {
    if (!resp || !resp.ok) {
      throw new Error('error', resp);
    } else {
      return resp;    }
  });
}


export function exportNotes(notes,exportAll,exportId) {

  fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/export/${exportId}/${notes}?exportAll=${exportAll}`, {
    credentials: 'include',
    method: 'GET',
  }).then((resp) => {
    if (!resp || !resp.ok) {
      throw new Error('error', resp);
    }
  });
}

export function getExportedZip(exportId) {

  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/export/zip/${exportId}`, {
    credentials: 'include',
    method: 'GET',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp;
    } else { 
      throw new Error('Error when getting exported notes');
    }
  });
}

export function getExportStatus(exportId) {

  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/export/status/${exportId}`, {
    credentials: 'include',
    method: 'GET',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else { 
      throw new Error('Error when getting export status');
    }
  });
}
export function cancelExportNotes(exportId) {
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/export/cancel/${exportId}`, {
    credentials: 'include',
    method: 'GET',
  }).then((resp) => {
    if (!resp || !resp.ok) {
      throw new Error('error', resp);
    }
  });
}

export function getNoteVersionsByNoteId(noteId,lang) {
  let url = `${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/versions/${noteId}`;
  if (lang){
    url=`${url}${getSeparator(url)}lang=${lang}`;
  }
  return fetch(url, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getAvailableLanguages() {
  const lang = eXo?.env.portal.language || 'en';
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/languages?lang=${lang}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getNoteLanguages(noteId, withDrafts) {
  let url = `${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/translation/${noteId}`;
  if (withDrafts){
    url=`${url}${getSeparator(url)}withDrafts=${withDrafts}`;
  }
  return fetch(url, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function deleteNoteTranslation(noteId, translation) {
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/note/translation/${noteId}/${translation}`, {
    credentials: 'include',
    method: 'DELETE',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp;
    } else {
      throw new Error('Error when deleting translation');
    }
  });
}

export function saveNoteMetadata(properties, lang) {
  const formData = new FormData();
  if (lang) {
    formData.append('lang', lang);
  }
  const params = new URLSearchParams(formData).toString();
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/metadata?${params}`, {
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json'
    },
    method: 'POST',
    body: JSON.stringify(properties),
    credentials: 'include',
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error('Error while saving note metadata');
    } else {
      return resp.json();
    }
  });
}

export function removeNoteFeaturedImage(noteId, isDraft, lang) {
  const formData = new FormData();
  if (lang) {
    formData.append('lang', lang);
  }
  formData.append('isDraft', isDraft);
  const params = new URLSearchParams(formData).toString();
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/metadata/featured-image/${noteId}?${params}`, {
    credentials: 'include',
    method: 'DELETE',
  }).then((resp) => {
    if (resp?.ok) {
      return resp;
    } else {
      throw new Error('Error when deleting note featured image');
    }
  });
}

export function searchNotes(keyword, limit) {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
  return fetch(`${notesConstants.PORTAL}/${notesConstants.PORTAL_REST}/notes/contextsearch?keyword=${keyword}&isNotesTreeFilter=true&limit=${limit}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  }).finally(() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')));
}