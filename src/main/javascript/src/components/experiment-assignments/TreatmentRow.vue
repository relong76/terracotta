<template>
  <div
    class="treatment-row-content d-flex align-center"
  >
    <div
      class="treatment-info-group d-flex align-center"
    >
      <div class="icon-circle" :class="rowTreatmentsIconCircleClass">
        <v-icon class="component-icon">
          {{ rowTreatmentsIcon }}
        </v-icon>
      </div>
      <ToolTip
        v-if="showTreatmentRowTooltip"
        :content="treatmentRowTooltipText"
        :ref="`tooltip-${row.assignmentId}-${treatment.treatmentId}`"
        aria-label="treatment explanation tooltip"
        icon="mdi-information-outline"
        alignment="top"
        activator-type="icon"
        activator-class="icon-treatment-incomplete"
      />
      <span class="treatment-condition-name" :class="treatmentRowClass">
        {{ conditionName }}
      </span>
    </div>
    <div class="treatment-btn-group">
      <v-menu location="top start">
        <template #activator="{ props: menuProps }">
          <v-btn
            v-bind="menuProps"
            :aria-label="`treatment actions for ${row.title}`"
            :style="actionsOffset == null ? { position: 'static' } : { left: `${actionsOffset}px` }"
            class="treatment-actions-btn"
            icon="mdi-dots-vertical"
            variant="text"
            density="compact"
          />
        </template>

        <v-list>
          <v-list-item
            class="btn-treatment-edit"
            @click="$emit('edit-treatment', { row, treatment })"
          >
            <v-list-item-title class="d-flex justify-content-center">
              <v-icon>{{ editTreatmentIcon }}</v-icon>
              <span class="btn-edit">{{ editTreatmentText }}</span>
            </v-list-item-title>
          </v-list-item>

          <v-list-item
            v-if="!isMessage"
            :disabled="previewDisabled"
            @click="!isIntegrationAssignment && $emit('preview-treatment', treatment)"
          >
            <v-list-item-title class="d-flex justify-content-center">
              <v-icon>mdi-eye-outline</v-icon>
              <span class="treatment-btn">
                <a
                  v-if="isIntegrationAssignment"
                  :href="integrationsPreviewLaunchUrl(treatment.assessmentDto.integrationPreviewUrl)"
                  target="_blank"
                  class="integration-preview-link"
                >
                  Preview
                </a>
                <template v-else>
                  Preview
                </template>
              </span>
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { message as messageStatus } from "@/helpers/messaging/status.js";
import ToolTip from "@/components/ToolTip.vue";

const props = defineProps({
  row: {
    type: Object,
    required: true
  },
  treatment: {
    type: Object,
    required: true
  },
  exposure: {
    type: Object,
    required: true
  },
  // measured live by ComponentTable.vue's measureColumnOffsets() - see that file's
  // comment for why: this row's actions button needs to line up with the outer
  // table's real Actions column, which a plain right-aligned flex layout can't track
  // (that column isn't flush with this nested table's own right edge, and its exact
  // position shifts with viewport/content). null before the first measurement runs.
  actionsOffset: {
    type: Number,
    default: null
  }
});

defineEmits(["edit-treatment", "preview-treatment"]);

const rowType = {
  assignment: "assignment",
  message: "message"
};

const treatmentIcon = {
  integration: "mdi-application-brackets-outline",
  assignment: "mdi-wrench-outline",
  file: "mdi-file-outline",
  message: "mdi-message-text-outline"
};

const isMessage = computed(() => props.row.type === rowType.message);
const isIntegrationAssignment = computed(() => {
  return props.row.type === rowType.assignment && props.treatment.assessmentDto.integration;
});

const rowTreatmentsIcon = computed(() => {
  if (props.row.type === rowType.assignment) {
    return props.treatment.assessmentDto.integration
      ? treatmentIcon.integration
      : treatmentIcon.assignment;
  }

  if (props.row.type === rowType.message) {
    return treatmentIcon.message;
  }

  return "";
});

const rowTreatmentsIconCircleClass = computed(() => {
  if (isIntegrationAssignment.value) {
    return "icon-circle-code";
  }

  if (props.row.type === rowType.assignment) {
    return "icon-circle-control";
  }

  if (props.row.type === rowType.message) {
    return "icon-circle-message";
  }

  return "";
});

const previewDisabled = computed(() => {
  if (!props.treatment.assessmentDto.questions.length) {
    return true;
  }

  return isIntegrationAssignment.value && !props.treatment.assessmentDto.integrationUrlValid;
});

const conditionForTreatment = computed(() => {
  return props.exposure.groupConditionList.find(
    condition => condition.conditionId === props.treatment.conditionId
  );
});

// every condition should have a name - this fallback is for the case where one
// somehow doesn't, not an expected/normal state
const conditionName = computed(() => conditionForTreatment.value?.conditionName || "No condition name");

const showTreatmentRowTooltip = computed(() => {
  if (props.row.type === rowType.assignment) {
    if (props.treatment.assessmentDto.integration && !props.treatment.assessmentDto.integrationUrlValid) {
      return true;
    }

    return !(props.treatment.assessmentDto && props.treatment.assessmentDto.questions.length);
  }

  if (props.row.type === rowType.message) {
    return ![
      messageStatus.ready,
      messageStatus.disabled,
      messageStatus.sent
    ].includes(props.treatment.configuration.status);
  }

  return false;
});

const treatmentRowTooltipText = computed(() => {
  if (props.row.type === rowType.assignment) {
    if (props.treatment.assessmentDto.integration && !props.treatment.assessmentDto.integrationUrlValid) {
      return "Error rendering content. Please check the URL.";
    }

    return "Please add content to this treatment.";
  }

  if (props.row.type === rowType.message) {
    return "Please create a message for this treatment.";
  }

  return "";
});

const treatmentRowClass = computed(() => {
  return showTreatmentRowTooltip.value
    ? "label-treatment-incomplete"
    : "label-treatment-complete";
});

const editTreatmentIcon = computed(() => {
  if (props.row.type === rowType.assignment) {
    return "mdi-pencil";
  }

  if (props.row.type === rowType.message) {
    return ![
      messageStatus.queued,
      messageStatus.processing,
      messageStatus.sent,
      messageStatus.deleted
    ].includes(props.treatment.configuration.status)
      ? "mdi-pencil"
      : "mdi-eye";
  }

  return "";
});

const editTreatmentText = computed(() => {
  if (props.row.type === rowType.assignment) {
    return "Edit";
  }

  if (props.row.type === rowType.message) {
    return ![
      messageStatus.queued,
      messageStatus.processing,
      messageStatus.sent,
      messageStatus.deleted
    ].includes(props.treatment.configuration.status)
      ? "Edit"
      : "View";
  }

  return "";
});

const integrationsPreviewLaunchUrl = (url = "http://localhost") => {
  return `/integrations/preview?url=${btoa(url)}`;
};
</script>

<style lang="scss" scoped>
.icon-circle {
  width: 24px;
  height: 24px;
  min-width: 24px;
  border-radius: 50%;
  text-align: center;
  align-content: center;
  display: inline-block;
  margin-right: 8px;

  > .v-icon {
    font-size: 14px;
  }

  &.icon-circle-control {
    border: 1px solid map.get($yellow, "base");
    background-color: rgba(255, 179, 0, 0.2);
    > .v-icon { color: map.get($yellow, "base") !important; }
  }

  &.icon-circle-code {
    border: 1px solid map.get($light-blue, "base");
    background-color: rgba(3, 169, 244, 0.2);
    > .v-icon { color: map.get($light-blue, "base") !important; }
  }

  &.icon-circle-message {
    border: 1px solid map.get($orange, "base");
    background-color: rgba(245, 124, 0, 0.2);
    > .v-icon { color: map.get($orange, "base") !important; }
  }
}

.treatment-condition-name {
  font-weight: 600;
}

// --treatment-indent is set on the shared ancestor by ComponentTable.vue's
// measureColumnOffsets() - see that file's comment for why this needs to be measured
// in JS rather than a fixed margin: the goal is to line this row's icon-circle up
// directly under the top-level assignment/message row's own icon-circle, and how far
// that sits from this nested table's own left edge depends on the outer table's real
// column widths, which shift with viewport/content in ways a fixed margin can't track.
// CSS custom properties inherit straight through Vue's scoped-style component
// boundaries (this component and ComponentTable.vue's placeholder version - see that
// file - both key off the exact same variable), so no prop-drilling is needed here.
// The fallback (32px, this file's original hardcoded ml-8 value) only matters for a
// brief instant before the first measurement runs.
.treatment-info-group {
  margin-left: var(--treatment-indent, 32px);
}

// positioning context for .treatment-actions-btn's absolute "left" below - mirrors
// ComponentTable.vue's .treatment-add-row, which needs the same thing for the same
// reason (see that file's comment on measureColumnOffsets).
.treatment-row-content {
  position: relative;
}

// lines this row's actions button up with the outer table's real Actions column via
// the actionsOffset prop (see that prop's comment) instead of the plain right-aligned
// flex position it used to have.
.treatment-actions-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
}
</style>
