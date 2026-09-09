<template>
  <div
    ref="tableRoot"
    @sorted="$emit('save-order', $event.detail)"
  >
    <v-data-table
      v-model:expanded="expandedRows"
      :headers="assignmentHeaders"
      :items="rows"
      :sort-by="[{ key: 'assignmentOrder', order: 'asc' }]"
      :mobile-breakpoint="mobileBreakpoint"
      :items-per-page="-1"
      :row-props="() => ({ class: 'assignment-row' })"
      item-value="assignmentId"
      class="v-data-table-alt v-data-table--sorted data-table-assignments mx-3 mb-5 mt-3"
      density="compact"
      hide-default-footer
      show-expand
    >
      <template #item.data-table-expand="{ internalItem, isExpanded, toggleExpand }">
        <v-icon
          :aria-label="`Expand component row ${internalItem.raw.title}`"
          @click="toggleExpand(internalItem)"
        >
          {{ isExpanded(internalItem) ? "mdi-chevron-up" : "mdi-chevron-down" }}
        </v-icon>
      </template>

      <template #item.title="{ item: row }">
        <div class="icon-circle" :class="rowIconCircleClass(row)">
          <v-icon>{{ rowIcon(row) }}</v-icon>
        </div>
        {{ row.title }}

        <v-chip
          v-if="row.treatments.length === 1"
          color="#d3d3d3"
          class="v-chip--only-one"
          variant="flat"
          density="compact"
          label
        >
          Only One Version
        </v-chip>
      </template>

      <template #expanded-row="{ item: row, columns }">
        <tr :class="['v-data-table__tr--expanded', { 'expanded-row--mobile': isMobile }]">
          <td
            :colspan="columns.length"
            class="treatments-table-container"
          >
            <div
              v-if="!singleConditionExperiment"
              class="treatments-section-label"
            >
              TREATMENTS - {{ row.treatments.length }} of {{ treatmentsTotalForRow(row) }} added
            </div>

            <v-data-table
              :headers="treatmentHeaders"
              :items="treatmentTableItems(row)"
              :items-per-page="-1"
              item-value="treatmentId"
              :class="['treatment-row', 'bg-grey-lighten-5', { 'treatment-row--mobile': isMobile }]"
              hide-default-header
              hide-default-footer
            >
              <template #item.title="{ item }">
                <div
                  v-if="item.isPlaceholder"
                  class="treatment-row-content treatment-add-row d-flex align-center justify-space-between"
                >
                  <div class="treatment-info-group ml-8 d-flex align-center">
                    <div class="icon-circle" :class="placeholderIconCircleClass(row, item.treatment)">
                      <v-icon>{{ placeholderIcon(row, item.treatment) }}</v-icon>
                    </div>
                    <span class="treatment-add-condition-name mr-2">{{ item.condition.conditionName }}</span>
                    <button
                      type="button"
                      class="treatment-add-box"
                      :aria-label="`add treatment for ${item.condition.conditionName}`"
                      @click="handlePlaceholderEdit(row, item)"
                    >
                      <v-icon>mdi-plus</v-icon>
                    </button>
                    <a
                      href="#"
                      class="treatment-add-link ml-2"
                      @click.prevent="handlePlaceholderEdit(row, item)"
                    >
                      Click to add treatment
                    </a>
                  </div>

                  <div class="treatment-btn-group d-flex align-center">
                    <v-chip
                      variant="tonal"
                      color="error"
                      density="compact"
                      class="status-pill mr-4"
                    >
                      <v-icon start>mdi-alert-circle</v-icon>
                      Needs attention
                    </v-chip>

                    <v-menu location="start">
                      <template #activator="{ props: menuProps }">
                        <v-btn
                          v-bind="menuProps"
                          :aria-label="`treatment actions for ${item.condition.conditionName}`"
                          icon="mdi-dots-horizontal"
                          variant="text"
                        />
                      </template>

                      <v-list>
                        <v-list-item @click="handlePlaceholderEdit(row, item)">
                          <v-list-item-title class="d-flex justify-content-center">
                            <v-icon>mdi-pencil</v-icon>
                            <span>Edit</span>
                          </v-list-item-title>
                        </v-list-item>

                        <v-list-item disabled>
                          <v-list-item-title class="d-flex justify-content-center">
                            <v-icon>mdi-eye-outline</v-icon>
                            <span>Preview</span>
                          </v-list-item-title>
                        </v-list-item>
                      </v-list>
                    </v-menu>
                  </div>
                </div>

                <TreatmentRow
                  v-else
                  :row="row"
                  :treatment="item"
                  :exposure="exposure"
                  :conditions="conditions"
                  :condition-color-mapping="conditionColorMapping"
                  :single-condition-experiment="singleConditionExperiment"
                  @edit-treatment="$emit('edit-treatment', $event)"
                  @preview-treatment="$emit('preview-treatment', $event)"
                />
              </template>
            </v-data-table>
          </td>
        </tr>
      </template>

      <template #item.treatments="{ item: row }">
        <span :class="rowTreatmentsColumnClass(row)">
          {{ row.treatments.length }} of {{ treatmentsTotalForRow(row) }}

          <ToolTip
            v-if="hasIncompleteTreatments(row)"
            :content="showRowTreatmentsColumnTooltipText(row)"
            :ref="`tooltip-component-${row.assignmentId}`"
            aria-label="incomplete treatments explanation tooltip"
            icon="mdi-circle"
            alignment="top"
            activator-type="icon"
            activator-class="label-treatment-incomplete treatment-ratio-dot"
          />
        </span>
      </template>

      <template #item.drag>
        <span class="dragger">
          <v-icon>mdi-drag</v-icon>
        </span>
      </template>

      <template #item.published="{ item: row }">
        <v-chip
          variant="tonal"
          :color="statusPillColor(row)"
          density="compact"
          class="status-pill"
        >
          {{ rowPublishedColumnText(row) }}
        </v-chip>
      </template>

      <template #item.dueDate="{ item: row }">
        {{ dueDate(row) }}
      </template>

      <template #item.actions="{ item: row }">
        <ComponentActionsMenu
          v-model="actionsMenuOpen[row.assignmentId]"
          :row="row"
          :can-delete-assignment="canDeleteAssignment"
          :exposure-count="exposureCount"
          :has-incomplete-treatments="hasIncompleteTreatments"
          @move="$emit('move', $event)"
          @edit="$emit('edit', $event)"
          @duplicate="$emit('duplicate', $event)"
          @delete="$emit('delete', $event)"
          @publish="$emit('publish', $event)"
          @unpublish="$emit('unpublish', $event)"
        />
      </template>
    </v-data-table>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from "vue";
import { useDisplay } from "vuetify";
import Sortable from "sortablejs";
import dayjs from "@/plugins/dayjs";

import { message as messageStatus } from "@/helpers/messaging/status.js";
import { deleteAttributesFromElement } from "@/helpers/ui-utils.js";
import ToolTip from "@/components/ToolTip.vue";
import TreatmentRow from "./TreatmentRow.vue";
import ComponentActionsMenu from "./ComponentActionsMenu.vue";

const props = defineProps({
  rows: {
    type: Array,
    required: true
  },
  exposure: {
    type: Object,
    required: true
  },
  conditions: {
    type: Array,
    required: true
  },
  conditionColorMapping: {
    type: Object,
    required: true
  },
  singleConditionExperiment: {
    type: Boolean,
    default: false
  },
  canDeleteAssignment: {
    type: Boolean,
    default: false
  },
  exposureCount: {
    type: Number,
    default: 1
  }
});

const emit = defineEmits([
  "save-order",
  "move",
  "edit",
  "duplicate",
  "delete",
  "publish",
  "unpublish",
  "edit-treatment",
  "preview-treatment",
  "add-treatment"
]);

const tableRoot = ref(null);
const expandedRows = ref([]);
const actionsMenuOpen = ref({});

const mobileBreakpoint = 636;
const { width } = useDisplay();
// mirrors mobileBreakpoint (not Vuetify's own global mobile breakpoint) so this
// switches in step with the table's own mobile-row transform.
const isMobile = computed(() => width.value < mobileBreakpoint);

// the drag-to-reorder handle has no title, so Vuetify's mobile card layout
// rendered it as its own orphaned label-less row (just a lone dot-grid icon) -
// drop it in mobile view entirely, since a tiny drag handle is a poor touch
// target anyway and SortableJS's `handle: ".dragger"` simply won't find
// anything to bind to once it's gone, so dragging is just unavailable there.
const assignmentHeaders = computed(() => {
  const headers = [
    { title: "", align: "start", sortable: false, key: "drag" },
    { title: "NAME", align: "start", sortable: false, key: "title" },
    { title: "TREATMENTS", sortable: false, key: "treatments" },
    { title: "DUE", sortable: false, key: "dueDate" },
    { title: "STATUS", sortable: false, key: "published" },
    { title: "Actions", align: "center", sortable: false, key: "actions" },
    { title: "", sortable: false, key: "data-table-expand" }
  ];

  return isMobile.value ? headers.filter(header => header.key !== "drag") : headers;
});
const treatmentHeaders = [
  { title: "Treatment Name", align: "start", sortable: false, key: "title" }
];
const rowType = {
  assignment: "assignment",
  message: "message"
};
const treatmentIcon = {
  file: "mdi-file-outline",
  message: "mdi-message-text-outline",
  assignment: "mdi-wrench-outline",
  integration: "mdi-application-brackets-outline"
};

// there's no persisted signal for "this assignment was deliberately created as
// single-version" (backend AssignmentService.isSingleVersion() computes the exact same
// thing from treatments.size() <= 1, without ever storing intent) - reuses the same
// condition the "Only One Version" chip already uses (row.treatments.length === 1)
// above, so both stay consistent with each other and with the pre-remodel behavior,
// where the Treatments column always showed a self-referential N/N (never compared
// against total conditions), making a single-version row trivially "complete".
const isSingleVersionRow = row => row.treatments.length === 1;

watch(
  () => props.rows,
  rows => {
    expandedRows.value = rows.map(row => row.assignmentId);
  },
  { immediate: true }
);

watch(
  actionsMenuOpen,
  async () => {
    await nextTick();
    deleteAttributesFromElement(".list-item-move", ["tabindex"]);
  },
  { deep: true }
);

const initSortable = async () => {
  await nextTick();

  const tbody = tableRoot.value?.querySelector(".data-table-assignments tbody");

  if (!tbody) {
    return;
  }

  Sortable.create(tbody, {
    animation: 150,
    handle: ".dragger",
    draggable: ".assignment-row",
    onUpdate(event) {
      tableRoot.value.dispatchEvent(
        new CustomEvent("sorted", {
          detail: event,
          bubbles: true
        })
      );
    }
  });
};

const rowIcon = row => {
  if (row.type === rowType.assignment) {
    return treatmentIcon.file;
  }

  if (row.type === rowType.message) {
    return treatmentIcon.message;
  }

  return "";
};

const rowIconCircleClass = row => {
  if (row.type === rowType.assignment) {
    return "icon-circle-document";
  }

  if (row.type === rowType.message) {
    return "icon-circle-message";
  }

  return "";
};

// the icon/circle an add-treatment placeholder shows. Reflects the existing
// treatment's own type when one exists (just incomplete), falling back to this
// row's default type (matching what a newly-created treatment would get) when the
// condition has no treatment at all - see TreatmentRow.vue's own wrench/code/message
// icon convention, which this mirrors.
const placeholderIcon = (row, treatment) => {
  if (row.type === rowType.assignment) {
    return treatment?.assessmentDto?.integration ? treatmentIcon.integration : treatmentIcon.assignment;
  }

  if (row.type === rowType.message) {
    return treatmentIcon.message;
  }

  return "";
};

const placeholderIconCircleClass = (row, treatment) => {
  if (row.type === rowType.assignment) {
    return treatment?.assessmentDto?.integration ? "icon-circle-code" : "icon-circle-control";
  }

  if (row.type === rowType.message) {
    return "icon-circle-message";
  }

  return "";
};

// the add-treatment placeholder's own edit action: if a treatment already exists
// (just incomplete) this is really an edit, not a create - creating one would leave
// a duplicate behind
const handlePlaceholderEdit = (row, item) => {
  if (item.treatment) {
    emit("edit-treatment", { row, treatment: item.treatment });
    return;
  }

  emit("add-treatment", { row, condition: item.condition });
};

// a treatment that exists but lacks content yet (no questions, invalid integration
// URL, or - for messages - a status that isn't ready/disabled/sent) - the per-treatment
// half of hasIncompleteTreatments below, factored out since the add-treatment
// placeholder needs to ask this about one specific treatment, not a whole row
const isTreatmentIncomplete = (row, treatment) => {
  if (row.type === rowType.assignment) {
    if (treatment.assessmentDto.integration && !treatment.assessmentDto.integrationUrlValid) {
      return true;
    }

    return !(treatment.assessmentDto && treatment.assessmentDto.questions && treatment.assessmentDto.questions.length);
  }

  if (row.type === rowType.message) {
    return ![messageStatus.ready, messageStatus.disabled, messageStatus.sent].includes(treatment.configuration.status);
  }

  return false;
};

// the Treatments column's denominator, and the "TREATMENTS - X of Y added" label's Y -
// a single-version row is always shown against its own treatment count (so "1 of 1"),
// not the experiment's total conditions
const treatmentsTotalForRow = row => {
  return isSingleVersionRow(row) ? row.treatments.length : props.conditions.length;
};

// one entry per relevant condition: the real treatment when it exists and has
// content, or an add-treatment placeholder when the condition has no treatment at
// all OR its treatment exists but is incomplete - a single-version row only ever
// considers the one condition it already has a treatment for (see isSingleVersionRow
// above), never the experiment's other conditions, which it was never meant to cover
const treatmentTableItems = row => {
  const relevantConditions = isSingleVersionRow(row)
    ? props.conditions.filter(condition =>
      row.treatments.some(treatment => treatment.conditionId === condition.conditionId)
    )
    : props.conditions;

  return relevantConditions.map(condition => {
    const treatment = row.treatments.find(item => item.conditionId === condition.conditionId);

    if (!treatment || isTreatmentIncomplete(row, treatment)) {
      return {
        isPlaceholder: true,
        condition,
        treatment: treatment || null,
        treatmentId: treatment ? treatment.treatmentId : `missing-treatment-${row.assignmentId}-${condition.conditionId}`
      };
    }

    return treatment;
  });
};

const dueDate = row => {
  return row.dueDate
    ? dayjs(row.dueDate).format("MMM D, YYYY hh:mma")
    : "";
};

const hasIncompleteTreatments = row => {
  if (!isSingleVersionRow(row) && row.treatments.length < props.conditions.length) {
    return true;
  }

  return row.treatments.some(treatment => isTreatmentIncomplete(row, treatment));
};

const rowTreatmentsColumnClass = row => {
  return hasIncompleteTreatments(row)
    ? "label-treatment-incomplete"
    : "label-treatment-complete";
};

const showRowTreatmentsColumnTooltipText = row => {
  if (row.type === rowType.assignment) {
    return `Set up your assignment by creating ${row.treatments.length > 1 ? "treatments" : "a treatment"}.`;
  }

  if (row.type === rowType.message) {
    return `Set up your message container by creating ${row.treatments.length > 1 ? "messages" : "a message"}.`;
  }

  return "";
};

const statusPillColor = row => {
  if (row.type === rowType.assignment) {
    return row.published ? "success" : "warning";
  }

  if (row.type === rowType.message) {
    if (row.error) {
      return "error";
    }

    if (row.sent) {
      return "info";
    }

    return row.published ? "success" : "warning";
  }

  return "";
};

const rowPublishedColumnText = row => {
  if (row.published) {
    return "Published";
  }

  if (row.sent) {
    return "Sent";
  }

  if (row.error) {
    return "Error";
  }

  return "Unpublished";
};

onMounted(initSortable);
</script>

<style lang="scss" scoped>
.icon-circle {
  width: 28px;
  height: 28px;
  min-width: 28px;
  border-radius: 50%;
  text-align: center;
  align-content: center;
  display: inline-block;
  margin-right: 8px;

  > .v-icon {
    font-size: 16px;
  }

  &.icon-circle-document {
    border: 1px solid map.get($blue, "primary");
    background-color: rgba(0, 119, 210, 0.2);
    color: map.get($blue, "primary");
    > .v-icon { color: map.get($blue, "primary") !important; }
  }

  &.icon-circle-message {
    border: 1px solid map.get($orange, "base");
    background-color: rgba(245, 124, 0, 0.2);
    color: map.get($orange, "base");
    > .v-icon { color: map.get($orange, "base") !important; }
  }

  &.icon-circle-control {
    border: 1px solid map.get($yellow, "base");
    background-color: rgba(255, 179, 0, 0.2);
    color: map.get($yellow, "base");
    > .v-icon { color: map.get($yellow, "base") !important; }
  }
}

.treatment-add-condition-name {
  font-weight: 600;
}

// the treatments column's incomplete-indicator is now a small dot rather
// than a large circled-alert glyph - mdi-circle renders large by default
.treatment-ratio-dot {
  font-size: 10px !important;
}

.treatments-section-label {
  padding: 10px 16px 4px;
  font-size: 15px;
  font-weight: 400;
}

// Home.vue has an unscoped, app-wide `.v-data-table *:not(.v-icon) { color: black
// !important; }` rule (see _global.scss's comment on the equivalent
// .label-treatment-incomplete override for the full explanation). It happens to
// already produce close to the right color here (pure black vs. the mockup's
// pixel-sampled rgba(0,0,0,0.87)) - set explicitly anyway, matching this app's own
// existing high-emphasis-text convention (see .v-card-text in _global.scss), rather
// than relying on an unrelated global rule by coincidence. A single class here (even
// with Vue's scoped-style data-v attribute added) only ties that rule's specificity
// and loses on source order - qualifying with the ancestor .treatments-table-container
// class too is what reliably beats it.
.treatments-table-container .treatments-section-label {
  color: rgba(0, 0, 0, 0.87) !important;
}

// ExperimentAssignments.vue has its own unscoped rule (search that file for
// "tr.v-data-table__tr--expanded") painting the WHOLE treatments-table-container <td>
// grey, to match the nested treatments table's own background - this label sits in
// that same <td> as a sibling of the nested table, so it inherits that grey too. The
// mockup wants it on plain white instead, with its own divider separating it from the
// (still grey) treatments below. That competing rule's selector is unusually long
// (4 class-level segments), so beating it needs real qualification, not just one
// ancestor class - .v-data-table-alt and .data-table-assignments are this table's own
// two identifying classes (see the root <v-data-table> class list above).
.v-data-table-alt.data-table-assignments .treatments-table-container .treatments-section-label {
  background-color: white !important;
  border-bottom: 1px solid rgba(0, 0, 0, 0.2);
}

.status-pill {
  text-transform: none;

  // same Home.vue override as .treatments-section-label above, but per Vuetify's
  // "text-<color>" utility class so each status keeps its own theme color instead of
  // every pill flattening to black. Home.vue's rule (`*:not(.v-icon)`) matches the
  // chip's own inner .v-chip__content span (the visible text) AND its .v-chip__underlay
  // span (the tonal variant's tinted background, painted via `background: currentColor`)
  // directly, not just this outer element - a color set only here would be inherited,
  // and a direct match always beats an inherited value regardless of specificity, so
  // both need their own explicit override too. Neither span is rendered by this file's
  // template (they're VChip's own internals), so Vue's scoped-style data-v attribute
  // never reaches them and a plain nested selector silently never matches - :deep() is
  // required to actually target them (matching this file's existing :deep(...) use for
  // the same child-component-internals reason elsewhere).
  &.text-success,
  &.text-success :deep(.v-chip__content),
  &.text-success :deep(.v-chip__underlay) {
    color: rgb(var(--v-theme-success)) !important;
  }

  &.text-warning,
  &.text-warning :deep(.v-chip__content),
  &.text-warning :deep(.v-chip__underlay) {
    color: rgb(var(--v-theme-warning)) !important;
  }

  &.text-error,
  &.text-error :deep(.v-chip__content),
  &.text-error :deep(.v-chip__underlay) {
    color: rgb(var(--v-theme-error)) !important;
  }

  &.text-info,
  &.text-info :deep(.v-chip__content),
  &.text-info :deep(.v-chip__underlay) {
    color: rgb(var(--v-theme-info)) !important;
  }
}

.treatment-add-row {
  padding: 0 16px;
}

// just the "+" square - the "Click to add treatment" text is a separate link
// alongside it, not inside the dashed box
.treatment-add-box {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: none;
  border: 2px dashed map.get($grey, "lighter");
  border-radius: 8px;
  padding: 0;
  cursor: pointer;
  color: inherit;

  > .v-icon {
    font-size: 18px;
  }

  &:hover {
    border-color: map.get($grey, "darker");
  }
}

// Home.vue's global `.v-data-table *:not(.v-icon) { color: black !important; }` rule
// (see the .treatments-section-label comment above for the full explanation) ties
// with a single class here (even with Vue's scoped-style data-v attribute) and loses
// on source order - same fix as .treatments-section-label, qualify with the ancestor
// .treatments-table-container class too. Also needed here to reliably beat the
// browser's own default anchor styling (blue, underlined) in the first place.
.treatments-table-container .treatment-add-link {
  color: rgba(0, 0, 0, 0.87) !important;
  text-decoration: underline;

  &:hover {
    color: map.get($blue, "primary") !important;
  }
}

.treatment-row {
  :deep(.v-table__wrapper) {
    border: none !important;
    border-radius: 0 !important;
    background-color: map.get($grey, "lightest") !important;
    > table {
      padding-top: 0px !important;
    }
  }
}

// mobile only: round this wrapper's own bottom corners, rather than the
// outer <td>'s (see the mobile-only rule on .expanded-row--mobile > td
// below). Confirmed empirically (a standalone, Vuetify-free reproduction)
// that a <td> doesn't reliably render a curved BORDER in this browser even
// when border-radius/overflow:hidden compute correctly - background-color
// clips to the curve fine, but the border edge itself renders as a sharp
// rectangle regardless. A plain div, like this v-table__wrapper, doesn't
// have that limitation. !important: needed to beat the blanket
// .treatment-row rule above (border-radius: 0 !important), which still
// applies here too since this element keeps the plain .treatment-row class
// alongside .treatment-row--mobile.
.treatment-row--mobile {
  :deep(.v-table__wrapper) {
    border-bottom-left-radius: 10px !important;
    border-bottom-right-radius: 10px !important;
    overflow: hidden;
  }
}

:deep(.data-table-assignments > .v-table__wrapper) {
  border: none !important;
}

// Vuetify's mobile card layout right-aligns every cell's value by default
// (fine for short single-line values like a status or date), but that makes
// a wrapped multi-line value - the component title plus the "Only One
// Version" chip - read as ragged, hard-to-follow right-aligned lines. Left-
// align just this table's mobile values so wrapped text reads naturally.
:deep(.data-table-assignments .v-data-table__tr--mobile .v-data-table__td-value) {
  text-align: start;
}

// this table already draws its own border via the tbody outline below (rows expand to
// variable heights, unlike .v-data-table-alt's ::before box-shadow which assumes a fixed
// row height) - disable the alt class's border so the two don't double up
:deep(.data-table-assignments > .v-table__wrapper > table > tbody::before) {
  content: none;
}

// _tables.scss's shared `.v-data-table-alt` rule independently draws its OWN left/right
// borders and rounded corners on every `.v-data-table-alt` table (this one included, since
// it carries that class alongside data-table-assignments) - that's the rounded-card look
// other .v-data-table-alt tables want, but this table's own rules above/below already
// establish a different, plain full-bleed desktop list per the mockup, so the shared
// version needs disabling here specifically, not just left to coexist. Not scoped to
// desktop only: this table's own dedicated mobile rules (the .v-data-table__tr--mobile
// selectors above/below, plus .treatment-row--mobile's nested-wrapper radius) already
// fully cover mobile's card look independently, so disabling the shared version doesn't
// lose anything there - it's redundant with those, not required by them.
:deep(.data-table-assignments > .v-table__wrapper > table > tbody > tr) {
  &:not(.v-data-table__tr--mobile) {
    > td:first-child,
    > td:last-child {
      border-left: none !important;
      border-right: none !important;
    }

    &:first-child > td:first-child,
    &:first-child > td:last-child,
    &:last-child > td:first-child,
    &:last-child > td:last-child {
      border-radius: 0 !important;
    }
  }
}

:deep(.data-table-assignments > .v-table__wrapper > table) {
  > thead > tr > th {
    border-bottom: none !important;
  }

  // 1px-wide drag-handle column - only meaningful for the desktop row-of-
  // columns layout. In mobile mode the drag column is dropped entirely (see
  // assignmentHeaders above) and each row's first <td> is actually
  // "Component Name" instead - excluding mobile rows here keeps this rule
  // from mistakenly shrinking that cell down to the handle's 1px width.
  > thead > tr > th:first-child,
  > tbody > tr:not(.v-data-table__tr--mobile) > td:first-child {
    width: 1px;
    padding-left: 0;
    padding-right: 0;
  }

  // real per-cell borders (table-row-group boxes don't consistently honor
  // border-radius/outline across browsers). Desktop is a plain full-bleed list per
  // the mockup - no left/right border, no rounded corners - only horizontal dividers
  // (see the bold group-boundary rule further down). Mobile still uses a genuine
  // rounded-card look, since each component's stacked fields need their own visual
  // boundary there; that's unrelated to and unaffected by the desktop styling here.
  > tbody {
    > tr {
      &:hover {
        background: unset !important;
      }

      // in mobile view every field is its own full-width stacked block, not
      // a column sharing the row's left/right edge with its siblings, so (unlike
      // desktop) every mobile td needs its own explicit left/right border for the
      // card's sides to read as one continuous line down the stack.
      &.v-data-table__tr--mobile > td {
        border-left: 1px solid rgba(0, 0, 0, 0.2);
        border-right: 1px solid rgba(0, 0, 0, 0.2);
      }

      // desktop: bold top edge under the header, matching the same weight as the
      // group-to-group divider further down (mockup pixel-sampled, see that rule's
      // comment) - no corner radius, this is a plain full-bleed list, not a card.
      // Mobile's own rounded-card top corner comes from the existing
      // .v-data-table__tr--mobile.assignment-row rule further below, which already
      // covers every mobile component row including the first.
      &:first-child:not(.v-data-table__tr--mobile) > td {
        padding-top: 8px !important;
        border-top: 2px solid rgba(0, 0, 0, 0.4);
      }

      // in mobile view, one component's row-of-fields (Component Name,
      // Treatments, Due Date, ...) stacks as several full-width label:value
      // lines instead of a single compact row. Vuetify's own CSS zeroes
      // border-bottom on every non-last mobile td, so without this, only the
      // table's literal first <tr> got dividers between its own fields - as
      // an accidental side effect of the border-top rule below applying to
      // ALL of that one row's children, not because it was actually meant to
      // provide inter-field dividers. Every mobile td needs its own explicit
      // top divider so every component's fields separate consistently, not
      // just the first component's.
      &.v-data-table__tr--mobile > td {
        border-top: 1px solid rgba(0, 0, 0, 0.2);
      }

      // component-to-component boundary: the divider above accounts for
      // fields *within* one component, but nothing marked where a NEW
      // component's stack begins - the divider after the *previous*
      // component's expanded content just looked like another routine
      // field-to-field line. Give every mobile row's first field the same
      // rounded-card treatment the table's very first row already had, so
      // each component still reads as its own card the way it does on
      // desktop.
      &.v-data-table__tr--mobile.assignment-row > td:first-child {
        padding-top: 8px !important;
        border-top-left-radius: 10px;
        border-top-right-radius: 10px;
      }

      // divider under each component's row group. Vuetify's own CSS zeroes
      // border-bottom on .v-data-table__tr--expanded from its "overrides" layer, so
      // this unlayered rule is needed just to win it back. Desktop only - in
      // mobile view this would draw a stray 1px line floating in the middle
      // of the white gap between cards; the gap and rounded corners already
      // tell them apart there.
      //
      // 2px at 0.4 opacity (vs. the nested treatments table's own default ~1px/0.12-opacity
      // row borders) so the group-to-group boundary reads as solidly grey and clearly more
      // pronounced than the divider between treatment sub-rows within one group - pixel-
      // sampled directly off the mockup (a solid rgb(155,155,155) on white, matching ~0.4
      // black opacity), since 0.2 alone rendered too faint to read as a real divider.
      &.v-data-table__tr--expanded:not(.expanded-row--mobile) > td {
        border-bottom: 2px solid rgba(0, 0, 0, 0.4);
      }

      // desktop: bold bottom edge closing out the list, same weight as the top edge
      // and the group dividers - no corner radius, matching the plain full-bleed
      // list the mockup wants. Mobile's own card spacing/rounding (padding + the
      // nested wrapper's own border-radius, see .treatment-row--mobile above and
      // .expanded-row--mobile below) is a different mechanism entirely and doesn't
      // need this border at all.
      &:last-child:not(.v-data-table__tr--mobile) > td {
        padding-bottom: 8px !important;
        border-bottom: 2px solid rgba(0, 0, 0, 0.4);
      }

      // mobile only: space every component's card apart (not just relying
      // on the thin 1px divider above) so each one reads as a complete,
      // distinct card - the actual rounded-corner look now comes from the
      // nested v-table__wrapper itself (see .treatment-row--mobile above),
      // since a <td> doesn't reliably render a curved border in this
      // browser. padding (not a thick border) creates the gap here - the
      // outer <td>'s own background needs to be plain white (not the grey
      // ExperimentAssignments.vue forces via !important) for that padding
      // area to actually read as empty space rather than more grey. Written
      // after the :last-child rule above so it also wins (equal
      // specificity, so source order decides) for the table's actual last
      // component too.
      &.expanded-row--mobile > td {
        padding-bottom: 32px;
        background-color: white !important;
      }
    }
  }
}
</style>