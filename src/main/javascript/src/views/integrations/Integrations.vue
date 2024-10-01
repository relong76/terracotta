<template>
  <div>
    <v-row>
      status: {{ getStatus }}
    </v-row>
  </div>
</template>

<script>
export default {
  props: {
    status:{
      type: String,
      required: true
    }
  },
  computed: {
    getStatus() {
      return this.status || "no status";
    }
  },
  mounted() {
    var event = new CustomEvent(
      "integrations_score",
      {
        detail: {
          status: this.status
        }
      }
    );
    window.parent.document.dispatchEvent(event);
  }
}
</script>
