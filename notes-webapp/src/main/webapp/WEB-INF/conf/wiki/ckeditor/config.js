﻿// force env when using the eXo Android app (the eXo Android app uses a custom user agent which
// is not known by CKEditor and which makes it not initialize the editor)
const oldEditorConfigFn = CKEDITOR.editorConfig;
CKEDITOR.editorConfig = function (config) {

  oldEditorConfigFn(config);

  // style inside the editor
  config.contentsCss = [];
  document.querySelectorAll('[skin-type=portal-skin]')
    .forEach(link => config.contentsCss.push(link.href));
  config.contentsCss.push(document.querySelector('#brandingSkin').href);
  config.contentsCss.push('/notes/ckeditorCustom/contents.css'); // load last

  const urlParams = new URLSearchParams(window.location.search);
  const webPageNote = urlParams.get('webPageNote') === 'true';

  if (!webPageNote) {
    CKEDITOR.plugins.addExternal('insertOptions','/notes/javascript/eXo/wiki/ckeditor/plugins/insertOptions/','plugin.js');
  }
  CKEDITOR.plugins.addExternal('toc','/notes/javascript/eXo/wiki/ckeditor/plugins/toc/','plugin.js');
  CKEDITOR.plugins.addExternal('linkBalloon', '/social/js/ckeditorPlugins/linkBalloon/', 'plugin.js');
  CKEDITOR.plugins.addExternal('insertImage','/notes/javascript/eXo/wiki/ckeditor/plugins/insertImage/','plugin.js');

  const blocksToolbarGroup = [
    'Blockquote',
    'tagSuggester',
    'emoji',
    'insertImage',
    'Table',
    'EmbedSemantic',
    'CodeSnippet',
    'attachFile',
    'InsertOptions'
  ];
  if (webPageNote) {
    blocksToolbarGroup.splice(blocksToolbarGroup.indexOf('tagSuggester'), 1);
    blocksToolbarGroup.splice(blocksToolbarGroup.indexOf('InsertOptions'), 1);
  }
  const toolbar = [
    {name: 'accessibility', items: ['A11ychecker']},
    {name: 'format', items: ['Format']},
    {name: 'fontsize', items: ['FontSize']},
    {
      name: 'basicstyles',
      groups: ['basicstyles', 'cleanup'],
      items: ['Bold', 'Italic', 'Underline', 'Strike', 'TextColor', 'RemoveFormat', 'CopyFormatting']
    },
    {
      name: 'paragraph',
      groups: ['align', 'list', 'indent'],
      items: ['JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock', 'NumberedList', 'BulletedList', 'Outdent', 'Indent'],
    },
    {name: 'links', items: ['Link', 'Anchor']},
    {
      name: 'blocks',
      items: blocksToolbarGroup
    },
  ];
  const mobileToolbar = [
    {
      name: 'basicstyles',
      groups: ['basicstyles', 'cleanup'],
      items: ['Bold', 'Italic']
    },
    {
      name: 'paragraph',
      groups: ['list'],
      items: ['BulletedList','NumberedList'],
    },
    {
      name: 'blocks',
      items: ['Blockquote', 'attachFile']
    },
  ];
  if (!webPageNote) {
    mobileToolbar[mobileToolbar.findIndex(item => item.name ==='blocks')].items.push('attachFile');
  }
  let extraPlugins = `a11ychecker,balloonpanel,indent,indentblock,indentlist,codesnippet,sharedspace,copyformatting,table,tabletools,embedsemantic,autolink,colordialog${!webPageNote && ',tagSuggester' || ''},emoji,link,font,justify,widget,${!webPageNote && ',insertOptions' || ''},contextmenu,tabletools,tableresize,toc,linkBalloon,suggester,image2,insertImage`;
  let removePlugins = `image,confirmBeforeReload,maximize,resize,autoembed${webPageNote && ',tagSuggester' || ''}`;

  require(['SHARED/extensionRegistry'], function(extensionRegistry) {
    const notesEditorExtensions = extensionRegistry.loadExtensions('NotesEditor', 'ckeditor-extensions');
    if (notesEditorExtensions?.length) {
      notesEditorExtensions.forEach(notesEditorExtension => {
        if (notesEditorExtension.extraPlugin) {
          extraPlugins = `${extraPlugins},${notesEditorExtension.extraPlugin}`;
        }
        if (notesEditorExtension.removePlugin) {
          removePlugins = `${extraPlugins},${notesEditorExtension.removePlugin}`;
        }
        if (notesEditorExtension.extraToolbarItem) {
          toolbar[toolbar.length - 1].items.push(notesEditorExtension.extraToolbarItem);
        }
      });
    }
  });

  if (window.innerWidth < 600) {
    config.toolbar = mobileToolbar;
  } else {
    config.toolbar = toolbar;
  }
  config.extraPlugins = extraPlugins;
  config.removePlugins = removePlugins;
  config.toolbarGroups = [
    { name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
    { name: 'paragraph', groups: ['align', 'list', 'indent', ] },
    { name: 'links'},
    { name: 'blocks'},
  ];

  config.autoGrow_minHeight = 500;
  config.height = 'auto';
  config.format_tags = 'p;h1;h2;h3';
};
