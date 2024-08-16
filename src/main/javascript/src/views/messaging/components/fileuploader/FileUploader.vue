<template>
  <v-col
    class="uploader"
  >
    <v-row>
      <v-file-input
        :disabled="readOnly"
        @change="upload"
        multiple
        hide-input
      />
    </v-row>
    <v-row
      v-if="files.length"
    >
      <file-display
        :files="files"
        :readOnly="readOnly"
        @removed="remove"
      />
    </v-row>
  </v-col>
</template>

<script>
import FileDisplay from "./FileDisplay";

export default {
  components: {
    FileDisplay
  },
  props: {
    files: {
      type: Array,
      required: false
    },
    readOnly: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    upload(files) {
      this.$emit("created", files);
    },
    remove(index) {
      this.$emit("removed", index);
    }
  }
}
</script>
