<template>
  <div>
    <p class="copy-candidates-intro">
      This course was copied from a previous course that had one or more Terracotta experiments.
      Choose which one(s) to recreate here - each is rebuilt as a brand new experiment, exactly
      like importing an exported experiment file.
    </p>

    <div
      v-for="candidate in candidates"
      :key="candidate.id"
      class="copy-candidate-option"
    >
      <v-checkbox
        v-model="selectedIds"
        :value="candidate.id"
        :label="candidate.experimentTitle || '(untitled experiment)'"
        color="primary"
        density="compact"
        hide-details
      />

      <div class="copy-candidate-meta">
        From course: <b>{{ candidate.sourceCourseTitle || "(unknown course)" }}</b>
        &middot;
        {{ candidate.conditionCount }} condition{{ candidate.conditionCount === 1 ? "" : "s" }}
        &middot;
        {{ candidate.assignmentCount }} assignment{{ candidate.assignmentCount === 1 ? "" : "s" }}
      </div>
    </div>

    <input
      id="copy-candidates-selected"
      :value="JSON.stringify(selectedIds)"
      type="hidden"
    />
  </div>
</template>

<script setup>
import { ref } from "vue";

defineOptions({
  name: "CopyCandidatesDialog"
});

const props = defineProps({
  candidates: {
    type: Array,
    required: true
  }
});

const selectedIds = ref(props.candidates.map(candidate => candidate.id));
</script>

<style lang="scss" scoped>
.copy-candidates-intro {
  text-align: left;
  margin-bottom: 16px;
}

.copy-candidate-option {
  text-align: left;
  border: thin solid rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 8px;
}

.copy-candidate-meta {
  font-size: 0.85em;
  color: rgba(0, 0, 0, 0.6);
  margin-left: 32px;
}
</style>
