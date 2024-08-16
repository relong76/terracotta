<template>
  <v-col
    class="uploaded-files"
  >
    <v-row
      v-if="files.length"
    >
      <v-chip
        v-for="(file, index) in files"
        :key="index"
        :close="!readOnly"
        @click:close="remove(index)"
        close-icon="mdi-delete"
        small
      >
        {{ fileInfo(file) }}
      </v-chip>
    </v-row>
    <v-row
      v-if="files.length"
    >
      {{ totalFileInfo }}
    </v-row>
  </v-col>
</template>

<script>
export default {
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
  computed: {
    totalFileInfo() {
      const size = this.files
        .map((file) => file.size)
        .reduce((partialSum, a) => partialSum + a, 0);
      return "count: " + this.fileCount + ", total size: " + this.fileSize(size);
    },
    fileCount() {
      return this.files.length || 0;
    }
  },
  methods: {
    remove(index) {
      this.$emit("removed", index);
    },
    fileInfo(file) {
      return file.fileName + " (" + this.fileSize(file.size) + ")";
    },
    fileSize(size) {
      return size > 1000 ? (Math.round(((size / 1000) + Number.EPSILON) * 10) / 10) + "kb" : size + "b";
    }
  }
}
</script>
