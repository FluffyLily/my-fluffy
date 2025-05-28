import '@ckeditor/ckeditor5-theme-lark/theme/index.css';
import SimpleUploadAdapter from '@ckeditor/ckeditor5-upload/src/adapters/simpleuploadadapter'; 
import ClassicEditorBase from '@ckeditor/ckeditor5-editor-classic/src/classiceditor.js';

import Essentials from '@ckeditor/ckeditor5-essentials/src/essentials.js';
import Paragraph from '@ckeditor/ckeditor5-paragraph/src/paragraph.js';
import Heading from '@ckeditor/ckeditor5-heading/src/heading.js';

import Bold from '@ckeditor/ckeditor5-basic-styles/src/bold.js';
import Italic from '@ckeditor/ckeditor5-basic-styles/src/italic.js';
import Underline from '@ckeditor/ckeditor5-basic-styles/src/underline.js';
import Strikethrough from '@ckeditor/ckeditor5-basic-styles/src/strikethrough.js';
import Subscript from '@ckeditor/ckeditor5-basic-styles/src/subscript.js';
import Superscript from '@ckeditor/ckeditor5-basic-styles/src/superscript.js';

import Font from '@ckeditor/ckeditor5-font/src/font.js';
import HorizontalLine from '@ckeditor/ckeditor5-horizontal-line/src/horizontalline.js';
import Highlight from '@ckeditor/ckeditor5-highlight/src/highlight.js';

import Link from '@ckeditor/ckeditor5-link/src/link.js';

import BlockQuote from '@ckeditor/ckeditor5-block-quote/src/blockquote.js';
import CodeBlock from '@ckeditor/ckeditor5-code-block/src/codeblock.js';

import List from '@ckeditor/ckeditor5-list/src/list.js';
import Indent from '@ckeditor/ckeditor5-indent/src/indent.js';

import Image from '@ckeditor/ckeditor5-image/src/image.js';
import ImageCaption from '@ckeditor/ckeditor5-image/src/imagecaption.js';
import ImageStyle from '@ckeditor/ckeditor5-image/src/imagestyle.js';
import ImageToolbar from '@ckeditor/ckeditor5-image/src/imagetoolbar.js';
import ImageUpload from '@ckeditor/ckeditor5-image/src/imageupload.js';
import ImageResize from '@ckeditor/ckeditor5-image/src/imageresize.js';

import Table from '@ckeditor/ckeditor5-table/src/table.js';
import TableToolbar from '@ckeditor/ckeditor5-table/src/tabletoolbar.js';
import TableProperties from '@ckeditor/ckeditor5-table/src/tableproperties.js';
import TableCellProperties from '@ckeditor/ckeditor5-table/src/tablecellproperties.js';


import FindAndReplace from '@ckeditor/ckeditor5-find-and-replace/src/findandreplace.js';
import Autoformat from '@ckeditor/ckeditor5-autoformat/src/autoformat.js';
import PasteFromOffice from '@ckeditor/ckeditor5-paste-from-office/src/pastefromoffice.js';
import WordCount from '@ckeditor/ckeditor5-word-count/src/wordcount.js';

import Alignment from '@ckeditor/ckeditor5-alignment/src/alignment.js';
import SpecialCharacters from '@ckeditor/ckeditor5-special-characters/src/specialcharacters.js';
import SpecialCharactersEssentials from '@ckeditor/ckeditor5-special-characters/src/specialcharactersessentials.js';
import MediaEmbed from '@ckeditor/ckeditor5-media-embed/src/mediaembed.js';

export default class ClassicEditor extends ClassicEditorBase {}

ClassicEditor.builtinPlugins = [
  Essentials, Paragraph, Heading,
  Bold, Italic, Underline, Strikethrough, Subscript, Superscript,
  Font, Highlight, HorizontalLine,
  Link, MediaEmbed,
  BlockQuote, CodeBlock,
  List, Indent,
  Image, ImageCaption, ImageStyle, ImageToolbar, ImageUpload, ImageResize,
  Table, TableToolbar,
  TableProperties,
  TableCellProperties,
  FindAndReplace, Autoformat, PasteFromOffice, WordCount,
  Alignment,
  SpecialCharacters,
  SpecialCharactersEssentials,
  SimpleUploadAdapter
];

ClassicEditor.defaultConfig = {
  toolbar: {
    items: [
      'heading',
      '|',
      'bold', 'italic', 'underline', 'strikethrough', 'subscript', 'superscript',
      'fontColor', 'fontBackgroundColor', 'highlight', 'horizontalLine',
      '|',
      'alignment',
      'link',
      'bulletedList', 'numberedList', 'outdent', 'indent',
      '|',
      'imageUpload', 'insertTable', 'blockQuote', 'codeBlock',
      '|',
      'mediaEmbed',
      '|',
      'specialCharacters',
      '|',
      'undo', 'redo',
      'findAndReplace'
    ]
  },
  image: {
    resizeUnit: '%',
    toolbar: [
      'imageStyle:full', 'imageStyle:side',
      '|',
      'imageTextAlternative'
    ],
    styles: ['full', 'side']
  },
  simpleUpload: {
    uploadUrl: '',
    headers: {}
  },
  table: {
    contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells']
  },
  language: 'ko'
};