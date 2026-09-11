import { describe, expect, it } from "vitest";

import { mountComponent } from "@/test-utils/mount";
import CopyCandidatesDialog from "./CopyCandidatesDialog.vue";

const candidates = [
  {
    id: "c1",
    experimentTitle: "Reading Study",
    sourceCourseTitle: "Course A",
    conditionCount: 2,
    assignmentCount: 3
  },
  {
    id: "c2",
    experimentTitle: "Writing Study",
    sourceCourseTitle: "Course A",
    conditionCount: 1,
    assignmentCount: 1
  }
];

describe("CopyCandidatesDialog", () => {
  it("renders one option per candidate, with its source course and counts", () => {
    const wrapper = mountComponent(CopyCandidatesDialog, { props: { candidates } });

    expect(wrapper.text()).toContain("Reading Study");
    expect(wrapper.text()).toContain("Writing Study");
    expect(wrapper.text()).toContain("Course A");
    expect(wrapper.text()).toContain("2 conditions");
    expect(wrapper.text()).toContain("1 assignment");
  });

  it("starts with every candidate selected", () => {
    const wrapper = mountComponent(CopyCandidatesDialog, { props: { candidates } });

    const hiddenInput = wrapper.find("#copy-candidates-selected");
    expect(JSON.parse(hiddenInput.element.value)).toEqual(["c1", "c2"]);
  });

  it("updates the hidden input when a candidate is unchecked", async () => {
    const wrapper = mountComponent(CopyCandidatesDialog, { props: { candidates } });

    const checkboxes = wrapper.findAllComponents({ name: "VCheckbox" });
    await checkboxes[0].find("input").setValue(false);
    await wrapper.vm.$nextTick();

    const hiddenInput = wrapper.find("#copy-candidates-selected");
    expect(JSON.parse(hiddenInput.element.value)).toEqual(["c2"]);
  });
});
