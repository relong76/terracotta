<template>
  <v-col
    class="uploader"
  >
    <v-row>
      <v-file-input
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
