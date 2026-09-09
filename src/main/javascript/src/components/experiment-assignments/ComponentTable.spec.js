import { afterEach, describe, expect, it } from "vitest";

import { mountComponent } from "@/test-utils/mount";
import ComponentTable from "./ComponentTable.vue";
import { message as messageStatus } from "@/helpers/messaging/status.js";

let wrapper;

afterEach(() => {
  wrapper?.unmount();
  wrapper = undefined;
});

const exposure = {
  exposureId: 1,
  groupConditionList: [
    { conditionId: 1, conditionName: "Condition A" },
    { conditionId: 2, conditionName: "Condition B" },
    { conditionId: 3, conditionName: "Condition C" }
  ]
};

const conditions = exposure.groupConditionList;

const conditionColorMapping = {
  "Condition A": "blue",
  "Condition B": "red"
};

const completeTreatment = (id, conditionId = 1) => ({
  treatmentId: id,
  conditionId,
  assessmentDto: {
    integration: false,
    integrationUrlValid: true,
    questions: [{ id: 1 }]
  }
});

const incompleteTreatment = (id, conditionId = 1) => ({
  treatmentId: id,
  conditionId,
  assessmentDto: {
    integration: false,
    integrationUrlValid: true,
    questions: []
  }
});

const assignmentRow = overrides => ({
  type: "assignment",
  assignmentId: 1,
  title: "Assignment 1",
  assignmentOrder: 1,
  published: true,
  dueDate: "2024-05-01T12:00:00Z",
  treatments: [completeTreatment(10, 1), completeTreatment(11, 2), completeTreatment(12, 3)],
  ...overrides
});

const messageRow = overrides => ({
  type: "message",
  assignmentId: 2,
  title: "Message 1",
  assignmentOrder: 2,
  published: false,
  sent: false,
  error: false,
  dueDate: null,
  configuration: { status: messageStatus.ready },
  treatments: [
    { treatmentId: 20, conditionId: 1, configuration: { status: messageStatus.ready } },
    { treatmentId: 21, conditionId: 2, configuration: { status: messageStatus.ready } },
    { treatmentId: 22, conditionId: 3, configuration: { status: messageStatus.ready } }
  ],
  ...overrides
});

const mountTable = (rows, props = {}) => {
  wrapper = mountComponent(ComponentTable, {
    props: {
      rows,
      exposure,
      conditions,
      conditionColorMapping,
      singleConditionExperiment: false,
      canDeleteAssignment: true,
      exposureCount: 2,
      ...props
    }
  });

  return wrapper;
};

describe("ComponentTable", () => {
  it("renders one row per assignment with its title", () => {
    mountTable([assignmentRow(), messageRow()]);

    expect(wrapper.text()).toContain("Assignment 1");
    expect(wrapper.text()).toContain("Message 1");
  });

  it("shows the 'Only One Version' chip when a row has exactly one treatment", () => {
    mountTable([
      assignmentRow({ treatments: [completeTreatment(10)] })
    ]);

    expect(wrapper.find(".v-chip--only-one").exists()).toBe(true);
    expect(wrapper.text()).toContain("Only One Version");
  });

  it("does not show the 'Only One Version' chip when there is more than one treatment", () => {
    mountTable([assignmentRow()]);

    expect(wrapper.find(".v-chip--only-one").exists()).toBe(false);
  });

  it("shows the treatments count as complete/complete when all treatments are filled in", () => {
    mountTable([assignmentRow()]);

    expect(wrapper.find(".label-treatment-complete").exists()).toBe(true);
    expect(wrapper.text()).toContain("3 of 3");
  });

  it("shows the real treatments ratio (not a no-op) when a genuinely multi-version row has fewer treatments than conditions", () => {
    mountTable([
      assignmentRow({ treatments: [completeTreatment(10, 1), completeTreatment(11, 2)] })
    ]);

    expect(wrapper.text()).toContain("2 of 3");
    expect(wrapper.find(".label-treatment-incomplete").exists()).toBe(true);
  });

  it("marks the treatments column incomplete and shows a small-dot tooltip when a treatment is missing content", () => {
    mountTable([
      assignmentRow({ treatments: [completeTreatment(10, 1), completeTreatment(11, 2), incompleteTreatment(12, 3)] })
    ]);

    expect(wrapper.find(".label-treatment-incomplete").exists()).toBe(true);

    const tooltip = wrapper.findComponent({ name: "ToolTip" });
    expect(tooltip.exists()).toBe(true);
    expect(tooltip.props("icon")).toBe("mdi-circle");
  });

  it("renders the 'TREATMENTS - X of Y added' section label for a multi-condition row", () => {
    mountTable([assignmentRow()]);
    expect(wrapper.text()).toContain("TREATMENTS - 3 of 3 added");
  });

  it("suppresses the 'TREATMENTS - X of Y added' section label for a single-condition experiment", () => {
    mountTable([assignmentRow()], { singleConditionExperiment: true });
    expect(wrapper.find(".treatments-section-label").exists()).toBe(false);
  });

  it("renders an add-treatment placeholder for each condition missing a treatment, and clicking it emits add-treatment", async () => {
    mountTable([
      assignmentRow({ treatments: [completeTreatment(10, 1), completeTreatment(11, 2)] })
    ]);

    const addBox = wrapper.find(".treatment-add-box");
    expect(addBox.exists()).toBe(true);
    expect(wrapper.text()).toContain("Click to add treatment");
    expect(wrapper.text()).toContain("Needs attention");

    await addBox.trigger("click");

    expect(wrapper.emitted("add-treatment")).toBeTruthy();
    expect(wrapper.emitted("add-treatment")[0][0]).toMatchObject({
      condition: { conditionId: 3, conditionName: "Condition C" }
    });
  });

  it("renders no add-treatment placeholder when every condition already has a treatment", () => {
    mountTable([assignmentRow()]);

    expect(wrapper.find(".treatment-add-box").exists()).toBe(false);
  });

  // there's no persisted signal distinguishing "deliberately single-version" from
  // "multi-version but still incomplete" (both are just treatments.length === 1) -
  // matching the pre-remodel app's behavior, a row showing "Only One Version" is
  // always treated as complete against its own treatment count, not total conditions
  it("treats an 'Only One Version' row as complete against its own count, not total conditions - no placeholder, no incomplete dot", () => {
    mountTable([
      assignmentRow({ treatments: [completeTreatment(10, 1)] })
    ]);

    expect(wrapper.text()).toContain("Only One Version");
    expect(wrapper.text()).toContain("1 of 1");
    expect(wrapper.find(".treatment-add-box").exists()).toBe(false);
    expect(wrapper.find(".label-treatment-incomplete").exists()).toBe(false);
  });

  it("still marks an 'Only One Version' row incomplete when its own single treatment has no content", () => {
    mountTable([
      assignmentRow({ treatments: [incompleteTreatment(10, 1)] })
    ]);

    expect(wrapper.text()).toContain("Only One Version");
    expect(wrapper.text()).toContain("1 of 1");
    expect(wrapper.find(".label-treatment-incomplete").exists()).toBe(true);
    expect(wrapper.find(".treatment-add-box").exists()).toBe(false);
  });

  it("shows the status pill with the theme color matching each row's status", () => {
    mountTable([
      assignmentRow({ published: true }),
      assignmentRow({ assignmentId: 2, published: false }),
      messageRow({ assignmentId: 3, sent: true, published: false }),
      messageRow({ assignmentId: 4, error: true })
    ]);

    const pills = wrapper.findAllComponents({ name: "VChip" })
      .filter(chip => ["Published", "Unpublished", "Sent", "Error"].includes(chip.text()));

    expect(pills.find(chip => chip.text() === "Published").props("color")).toBe("success");
    expect(pills.find(chip => chip.text() === "Unpublished").props("color")).toBe("warning");
    expect(pills.find(chip => chip.text() === "Sent").props("color")).toBe("info");
    expect(pills.find(chip => chip.text() === "Error").props("color")).toBe("error");
  });

  it("renders the shortened, all-caps column headers", () => {
    mountTable([assignmentRow()]);

    expect(wrapper.text()).toContain("NAME");
    expect(wrapper.text()).toContain("DUE");
    expect(wrapper.text()).toContain("TREATMENTS");
    expect(wrapper.text()).toContain("STATUS");
  });

  it("formats the due date for assignment rows and leaves it blank when absent", () => {
    mountTable([
      assignmentRow({ dueDate: "2024-05-01T12:00:00Z" }),
      messageRow({ assignmentId: 3, dueDate: null })
    ]);

    expect(wrapper.text()).toMatch(/May 1, 2024/);
  });

  it("shows Published status for a published assignment and Unpublished otherwise", () => {
    mountTable([
      assignmentRow({ published: true }),
      messageRow({ assignmentId: 3 })
    ]);

    expect(wrapper.text()).toContain("Published");
    expect(wrapper.text()).toContain("Unpublished");
  });

  it("shows Sent status for a sent message row", () => {
    mountTable([
      messageRow({ sent: true, published: false })
    ]);

    expect(wrapper.text()).toContain("Sent");
  });

  it("shows Error status for a message row with an error", () => {
    mountTable([
      messageRow({ error: true })
    ]);

    expect(wrapper.text()).toContain("Error");
  });

  it("renders an actions menu button per row", () => {
    mountTable([assignmentRow(), messageRow()]);

    const actionButtons = wrapper.findAll('[aria-label="actions"]');

    expect(actionButtons.length).toBe(2);
  });

  it("expands all rows by default (expandedRows initialized from rows)", () => {
    mountTable([assignmentRow(), messageRow()]);

    // When expanded, treatment rows render inside expanded-row slot content.
    expect(wrapper.findAllComponents({ name: "TreatmentRow" }).length).toBeGreaterThan(0);
  });

  it("wraps the expanded-row slot content in its own <tr> (required by Vuetify 3's expanded-row slot contract, unlike Vuetify 2's auto-wrapping expanded-item slot)", () => {
    mountTable([assignmentRow()]);

    const expandedTr = wrapper.find("tr.v-data-table__tr--expanded");

    expect(expandedTr.exists()).toBe(true);
    expect(expandedTr.find("td.treatments-table-container").exists()).toBe(true);
  });

  it("re-initializes expandedRows when the rows prop changes", async () => {
    mountTable([assignmentRow()]);

    expect(wrapper.findAllComponents({ name: "TreatmentRow" }).length).toBe(3);

    await wrapper.setProps({ rows: [assignmentRow(), messageRow()] });

    expect(wrapper.text()).toContain("Message 1");
  });

  it("emits edit-treatment when a TreatmentRow requests an edit", async () => {
    mountTable([assignmentRow()]);

    const treatmentRow = wrapper.findComponent({ name: "TreatmentRow" });

    await treatmentRow.vm.$emit("edit-treatment", { row: assignmentRow(), treatment: completeTreatment(10) });

    expect(wrapper.emitted("edit-treatment")).toBeTruthy();
  });

  it("emits preview-treatment when a TreatmentRow requests a preview", async () => {
    mountTable([assignmentRow()]);

    const treatmentRow = wrapper.findComponent({ name: "TreatmentRow" });

    await treatmentRow.vm.$emit("preview-treatment", completeTreatment(10));

    expect(wrapper.emitted("preview-treatment")).toBeTruthy();
  });

  it("emits save-order when the table root receives a sorted custom event", async () => {
    mountTable([assignmentRow(), messageRow()]);

    const detail = { some: "sortable-event-detail" };

    wrapper.element.dispatchEvent(new CustomEvent("sorted", { detail, bubbles: true }));
    await wrapper.vm.$nextTick();

    expect(wrapper.emitted("save-order")).toBeTruthy();
    expect(wrapper.emitted("save-order")[0][0]).toBe(detail);
  });

  it("renders no rows when given an empty rows array", () => {
    mountTable([]);

    expect(wrapper.findAllComponents({ name: "TreatmentRow" })).toHaveLength(0);
  });
});
