<template>
  <div
    v-if="show"
    class="editor mb-6 outlined"
  >
    <v-card
      v-if="editorType === 'html'"
      flat
    >
      <editor-content
        :editor="htmlEditor"
        class="content"
      />
      <tool-bar
        :editor="htmlEditor"
        :activeItems="activeItems"
      />
    </v-card>
    <v-card
      v-if="editorType === 'basic'"
      flat
    >
      <editor-content
        :editor="basicEditor"
        class="content"
      />
    </v-card>
  </div>
</template>

<script>
import { mergeAttributes } from "@tiptap/core";
import { Editor, EditorContent } from "@tiptap/vue-2";
import { Mention as Placeholder } from "@tiptap/extension-mention";
import { Placeholder as Hint } from "@tiptap/extension-placeholder";
import StarterKit from "@tiptap/starter-kit";
import Document from "@tiptap/extension-document";
import Link from "@tiptap/extension-link";
import Paragraph from "@tiptap/extension-paragraph";
import Text from "@tiptap/extension-text";
import Underline from "@tiptap/extension-underline";
import YouTube from "@tiptap/extension-youtube";
import ToolBar from "./ToolBar";
import placeholder from "./components/placeholder/placeholder.js";

export default {
  components: {
    EditorContent,
    ToolBar
  },
  props: {
    content: {
      type: String
    },
    placeholders: {
      type: Array
    },
    editorType: {
      type: String
    },
    hint: {
      type: String
    },
    readOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      html: null,
      activeItems: null,
      editors: {
        basic: null,
        html: null
      }
    }
  },
  watch: {
    editorType: {
      handler() {
        this.destroyEditors();
        this.createEditors();
      },
      immediate: false
    },
    content: {
      handler(newContent) {
        this.html = newContent;
        this.destroyEditors();
        this.createEditors();
      },
      deep: true
    }
  },
  computed: {
    htmlEditor: {
      get() {
        return this.editors["html"];
      },
      set(editor) {
        this.editors["html"] = editor;
      }
    },
    basicEditor: {
      get() {
        return this.editors["basic"];
      },
      set(editor) {
        this.editors["basic"] = editor;
      }
    },
    show() {
      return this.editorType !== null && this.htmlEditor && this.basicEditor;
    },
    placeholderExtension() {
      return {
        HTMLAttributes: {
          class: "placeholder",
        },
        renderHTML({ options, node }) {
          return [
            "span",
            mergeAttributes(
              options.HTMLAttributes
            ),
            `{{ ${node.attrs.label ?? node.attrs.id} }}`,
          ]
        },
        deleteTriggerWithBackspace: true,
        suggestion: placeholder
      };
    },
    hintExtension() {
      return {
        placeholder: this.hint || ""
      }
    },
    extensions() {
      return {
        basic: [
          Document,
          Hint.configure(this.hintExtension),
          Paragraph,
          Placeholder.configure(this.placeholderExtension),
          Text
        ],
        html: [
          StarterKit.configure(
            {
              heading: {
                levels: [1, 2, 3]
              }
            }
          ),
          Hint.configure(this.hintExtension),
          Link.configure(
            {
              openOnClick: true,
              defaultProtocol: "https",
              protocols: ["ftp", "mailto", "git", "cal"],
              HTMLAttributes: {
                target: "_blank",
              },
            }
          ),
          Placeholder.configure(this.placeholderExtension),
          Underline,
          YouTube.configure(
            {
              modestBranding: true,
              inline: true,
              nocookie: true
            }
          )
        ]
      };
    },
    onUpdate() {
      return {
        basic: ({ editor }) => {
          /*
           *replace all removed placeholder <span> tags that were removed in the plaintext content
           */
          var content = editor.view.dom.innerText;

          // match all "{{ placeholder }}" blocks
          const toReplace = [];
          const regex = new RegExp(`{{(.*?)}}`, "g");
          let match;

          while ((match = regex.exec(content)) !== null) {
            toReplace.push("{{" + match[1] + "}}");
          }

          // find all html elements in the editor content
          const placeholders = document.querySelectorAll(".message-card .editor .content span.placeholder");

          // replace all found placeholders with the <span> tag
          toReplace.forEach(
            (replace) => {
              Array.from(placeholders)
                .filter((placeholder) => placeholder.textContent === replace)
                .forEach((placeholder) => content = content.replaceAll(replace, placeholder.outerHTML));
            }
          );

          this.html = editor.getText() ? editor.getHTML() : "";
          this.$emit("edited", content);
        },
        html: ({ editor }) => {
          this.html = editor.getText() ? editor.getHTML() : "";
          this.$emit("edited", this.html);
        }
      };
    },
    onSelectionUpdate() {
      return ({ editor }) => {
        const { view } = editor;
        const { selection } = view.state;
        this.activeItems = {};

        if (selection.$head.nodeBefore?.marks.length) {
          this.activeItems.marks = selection.$head.nodeBefore.marks.map(m => m.type.name);
        }

        if (selection.$head.node(1)) {
          this.activeItems.nodes = [
            {
              name: selection.$head.node(1).type.name,
              attributes: selection.$head.node(1).attrs
            }
          ];
        }
      };
    },
    placeholderItems() {
      return ({ query }) => {
        return this.placeholders
          .filter(item => item.label.toLowerCase().startsWith(query.toLowerCase()))
          .slice(0, 10);
      };
    }
  },
  methods: {
    createBasicEditor() {
      this.basicEditor = new Editor({
        content: this.html,
        editable: !this.readOnly,
        extensions: this.extensions.basic,
        onUpdate: this.onUpdate.basic,
        onSelectionUpdate: this.onSelectionUpdate
      });
    },
    createHtmlEditor() {
      this.htmlEditor = new Editor({
        content: this.html,
        editable: !this.readOnly,
        extensions: this.extensions.html,
        onUpdate: this.onUpdate.html,
        onSelectionUpdate: this.onSelectionUpdate
      })
    },
    createEditors() {
      this.createBasicEditor();
      this.createHtmlEditor();
    },
    destroyEditors() {
      if (this.htmlEditor) {
        this.htmlEditor.destroy();
        this.htmlEditor = null;
      }
      if (this.basicEditor) {
        this.basicEditor.destroy();
        this.basicEditor = null;
      }
    }
  },
  mounted() {
    this.html = this.content;
    placeholder.items = this.placeholderItems;
    this.createEditors();
  },
  beforeDestroy() {
    this.destroyEditors();
  }
}
</script>

<style lang="scss" scoped>
.editor::v-deep {
  min-width: 100%;
  box-shadow: none;
  border-radius: 4px;
  border: 1px solid map-get($grey, "base");
  overflow: hidden;
  .ProseMirror {
    margin: 20px 5px !important;
    .is-editor-empty::before {
      color: map-get($grey, "darken-1");
      font-style: normal;
    }
    &.ProseMirror-focused:focus-visible {
      outline: none;
    }
  }
  .content {
    > div {
      transition: all 2s;
      overflow: auto !important;
      padding: 5px;
    }
    & blockquote {
      border-left: .25em solid #dfe2e5;
      color: #6a737d;
      padding-left: 1em;
      margin: 20px 0 !important;
    }
    & h1 {
      font-size: 2em !important;
    }
    & span.placeholder {
      background-color: lightsteelblue;
      border-radius: 0.4rem;
      box-decoration-break: clone;
      color: var(--purple);
      padding: 0.1rem 0.3rem;
    }
  }
}
</style>
