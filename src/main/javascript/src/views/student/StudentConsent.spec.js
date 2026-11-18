import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { flushPromises } from "@vue/test-utils";

vi.mock("vue-pdf-embed", () => ({
  default: {
    name: "VuePdfEmbed",
    template: "<div class=\"vue-pdf-embed-stub\" />"
  }
}));

vi.mock("sweetalert2", () => ({
  default: {
    fire: vi.fn().mockResolvedValue({})
  }
}));

vi.mock("@/services", () => ({
  consentService: {
    getConsentFile: vi.fn()
  },
  participantService: {
    updateParticipant: vi.fn()
  },
  apiService: {
    reportStep: vi.fn()
  }
}));

import Swal from "sweetalert2";
import { consentService, participantService, apiService } from "@/services";
import { mountComponent } from "@/test-utils/mount";
import StudentConsent from "./StudentConsent.vue";

const baseParticipant = {
  participantId: 1,
  source: null,
  started: false,
  consent: null,
  dateGiven: null,
  dateRevoked: null
};

async function mountAndLoad(participantOverrides = {}, extraProps = {}) {
  consentService.getConsentFile.mockResolvedValue({
    status: 200,
    base: "PDFDATA"
  });

  apiService.reportStep.mockResolvedValue({
    status: 200,
    data: { ...baseParticipant, ...participantOverrides }
  });

  const wrapper = mountComponent(StudentConsent, {
    props: {
      experimentId: "1",
      userId: "u1",
      ...extraProps
    }
  });

  await flushPromises();
  await flushPromises();

  return wrapper;
}

describe("StudentConsent", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it("loads the consent PDF and participant step on mount, then emits loaded", async () => {
    const wrapper = await mountAndLoad();

    expect(consentService.getConsentFile).toHaveBeenCalledWith("1");
    expect(apiService.reportStep).toHaveBeenCalledWith(
      "1",
      "launch_consent_assignment",
      null,
      false
    );

    expect(wrapper.emitted("loaded")).toBeTruthy();
    expect(wrapper.find(".consent-steps").attributes("style")).not.toContain("display: none");
  });

  it("shows the responded alert when the participant has already consented", async () => {
    const wrapper = await mountAndLoad({
      source: "CONSENT",
      consent: true,
      dateGiven: "2024-01-01T00:00:00.000Z"
    });

    expect(wrapper.text()).toContain("You responded \"agree to participate\"");
  });

  it("shows the responded alert with 'do not' wording when consent was declined/revoked", async () => {
    const wrapper = await mountAndLoad({
      source: "REVOKED",
      consent: false,
      dateRevoked: "2024-01-01T00:00:00.000Z"
    });

    expect(wrapper.text()).toContain("You responded \"do not agree to participate\"");
  });

  it("shows the already-accessed alert when the participant started without consenting", async () => {
    const wrapper = await mountAndLoad({
      started: true,
      consent: false
    });

    expect(wrapper.text()).toContain("no matter your response to the following question");
  });

  // switching an experiment to informed consent used to reset consent to false but stamp a date
  // given - which showed "do not agree to participate" on an "Invalid Date" on first launch
  it("does not say the student declined when they're unconsented but only have a date given", async () => {
    const wrapper = await mountAndLoad({
      source: "CONSENT",
      consent: false,
      dateGiven: "2024-01-01T00:00:00.000Z",
      dateRevoked: null
    });

    expect(wrapper.text()).not.toContain("You responded");
    expect(wrapper.text()).not.toContain("Invalid Date");
  });

  // the server leaves out a student who submits before answering by setting a date revoked, but
  // keeps the CONSENT source; that isn't an answer
  it("does not say a student declined when they were left out for submitting before answering", async () => {
    const wrapper = await mountAndLoad({
      started: true,
      source: "CONSENT",
      consent: false,
      dateRevoked: "2024-01-01T00:00:00.000Z"
    });

    expect(wrapper.text()).not.toContain("You responded");
    expect(wrapper.text()).toContain("no matter your response to the following question");
  });

  it("records a decline from a student left out for submitting before answering", async () => {
    const wrapper = await mountAndLoad({
      started: true,
      source: "CONSENT",
      consent: false,
      dateRevoked: "2024-01-01T00:00:00.000Z"
    });

    participantService.updateParticipant.mockResolvedValue({
      ...baseParticipant,
      started: true,
      source: "REVOKED",
      consent: false,
      dateRevoked: "2024-02-01T00:00:00.000Z"
    });

    const radioInputs = wrapper.findAll("input[type=\"radio\"]");
    await radioInputs[1].setValue(true);

    await wrapper.find("form").trigger("submit");
    await flushPromises();

    expect(participantService.updateParticipant).toHaveBeenCalledWith(
      "1",
      expect.objectContaining({ consent: false })
    );
    expect(wrapper.text()).toContain("You responded \"do not agree to participate\"");
  });

  it("does not show either alert for a fresh, unstarted, unconsented participant", async () => {
    const wrapper = await mountAndLoad();

    expect(wrapper.text()).not.toContain("You responded");
    expect(wrapper.text()).not.toContain("no matter your response to the following question");
  });

  it("submits the selected answer and shows a success message", async () => {
    const wrapper = await mountAndLoad();

    participantService.updateParticipant.mockResolvedValue({
      ...baseParticipant,
      consent: true
    });

    const radioInputs = wrapper.findAll("input[type=\"radio\"]");
    await radioInputs[0].setValue(true);

    await wrapper.find("form").trigger("submit");
    await flushPromises();

    expect(participantService.updateParticipant).toHaveBeenCalledWith(
      "1",
      expect.objectContaining({ consent: true, participantId: 1 })
    );
    expect(Swal.fire).toHaveBeenCalledWith(
      expect.objectContaining({ text: "Successfully submitted consent", icon: "success" })
    );
  });

  it("tells the student the assignment opens next, and lets App open it once the answer is saved", async () => {
    const wrapper = await mountAndLoad({}, { continuesToAssignment: true });

    expect(Swal.fire).toHaveBeenCalledWith({
      text: "You have not yet completed the consent form. Please approve or revoke consent. You will be redirected to your assignment upon completion.",
      icon: "info",
      position: "top",
      showDenyButton: true,
      denyButtonText: "Just take me to my assignment",
      denyButtonColor: "#515961"
    });
    // closing the popup (OK, Escape, or outside it) keeps the student on the form
    expect(wrapper.emitted("skipped")).toBeUndefined();

    participantService.updateParticipant.mockResolvedValue({ ...baseParticipant, consent: false });
    await wrapper.findAll("input[type=\"radio\"]")[1].setValue(true);
    await wrapper.find("form").trigger("submit");
    await flushPromises();

    expect(wrapper.emitted("answered")).toHaveLength(1);
  });

  it("lets the student skip the form and go to their assignment without answering", async () => {
    Swal.fire.mockResolvedValueOnce({ isDenied: true });

    const wrapper = await mountAndLoad({}, { continuesToAssignment: true });
    await flushPromises();

    expect(wrapper.emitted("skipped")).toHaveLength(1);
    expect(wrapper.emitted("answered")).toBeUndefined();
    expect(participantService.updateParticipant).not.toHaveBeenCalled();
  });

  it("keeps the student on the form when they close the popup any other way", async () => {
    for (const result of [{ isConfirmed: true }, { isDismissed: true, dismiss: "esc" }, { isDismissed: true, dismiss: "backdrop" }]) {
      Swal.fire.mockResolvedValueOnce(result);

      const wrapper = await mountAndLoad({}, { continuesToAssignment: true });
      await flushPromises();

      expect(wrapper.emitted("skipped")).toBeUndefined();
    }
  });

  it("scrolls to the top and shows the confirmation there, so it's in view after a long consent PDF", async () => {
    const wrapper = await mountAndLoad({}, { continuesToAssignment: true });
    const scrollTo = vi.spyOn(window, "scrollTo").mockImplementation(() => {});

    participantService.updateParticipant.mockResolvedValue({ ...baseParticipant, consent: true });
    await wrapper.findAll("input[type=\"radio\"]")[0].setValue(true);
    await wrapper.find("form").trigger("submit");
    await flushPromises();

    const confirmation = Swal.fire.mock.calls.find(([options]) => options.icon === "success")[0];
    expect(confirmation).toEqual({
      text: "Successfully submitted consent. You will now be taken to your assignment.",
      icon: "success",
      position: "top"
    });
    expect(scrollTo).toHaveBeenCalledWith({ top: 0 });

    scrollTo.mockRestore();
  });

  it("shows every popup at the top", async () => {
    const wrapper = await mountAndLoad({ started: true, consent: false }, { continuesToAssignment: true });

    // agreeing after starting an assignment is refused, in an error popup
    await wrapper.findAll("input[type=\"radio\"]")[0].setValue(true);
    await wrapper.find("form").trigger("submit");
    await flushPromises();

    Swal.fire.mock.calls.forEach(([options]) => expect(options.position).toBe("top"));
    expect(Swal.fire).toHaveBeenCalledWith(expect.objectContaining({ icon: "error", position: "top" }));
  });

  it("doesn't mention an assignment when the consent form was launched itself", async () => {
    await mountAndLoad();

    expect(Swal.fire).not.toHaveBeenCalled();
  });

  it("doesn't report an answer the server didn't accept", async () => {
    const wrapper = await mountAndLoad({}, { continuesToAssignment: true });

    participantService.updateParticipant.mockResolvedValue({ message: "Participant has already started experiment, consent cannot be changed to given" });
    await wrapper.findAll("input[type=\"radio\"]")[0].setValue(true);
    await wrapper.find("form").trigger("submit");
    await flushPromises();

    expect(wrapper.emitted("answered")).toBeUndefined();
  });

  it("blocks agreeing and warns when the participant has already accessed an assignment", async () => {
    const wrapper = await mountAndLoad({ started: true });

    const radioInputs = wrapper.findAll("input[type=\"radio\"]");
    await radioInputs[0].setValue(true);

    await wrapper.find("form").trigger("submit");
    await flushPromises();

    expect(participantService.updateParticipant).not.toHaveBeenCalled();
    expect(Swal.fire).toHaveBeenCalledWith(
      expect.objectContaining({ icon: "error" })
    );
  });

  it("shows a success message without re-submitting when the same answer is chosen again", async () => {
    const wrapper = await mountAndLoad({
      source: "CONSENT",
      consent: true,
      dateGiven: "2024-01-01T00:00:00.000Z"
    });

    const radioInputs = wrapper.findAll("input[type=\"radio\"]");
    await radioInputs[0].setValue(true);

    await wrapper.find("form").trigger("submit");
    await flushPromises();

    expect(participantService.updateParticipant).not.toHaveBeenCalled();
    expect(Swal.fire).toHaveBeenCalledWith(
      expect.objectContaining({ text: "Successfully submitted Consent", icon: "success" })
    );
  });

  // reported from Canvas: a student who agreed and then started an assignment was told they
  // couldn't be included, and their consent was never withdrawn
  it("lets a student who agreed and has started an assignment withdraw", async () => {
    const wrapper = await mountAndLoad({
      started: true,
      source: "CONSENT",
      consent: true,
      dateGiven: "2024-01-01T00:00:00.000Z"
    });

    participantService.updateParticipant.mockResolvedValue({
      ...baseParticipant,
      started: true,
      source: "REVOKED",
      consent: false,
      dateRevoked: "2024-02-01T00:00:00.000Z"
    });

    const radioInputs = wrapper.findAll("input[type=\"radio\"]");
    await radioInputs[1].setValue(true);

    await wrapper.find("form").trigger("submit");
    await flushPromises();

    expect(participantService.updateParticipant).toHaveBeenCalledWith(
      "1",
      expect.objectContaining({ consent: false, participantId: 1 })
    );
    expect(Swal.fire).not.toHaveBeenCalledWith(expect.objectContaining({ icon: "error" }));
    expect(Swal.fire).toHaveBeenCalledWith(
      expect.objectContaining({ text: "Successfully submitted consent", icon: "success" })
    );
  });

  it("lets a student who started without answering decline", async () => {
    const wrapper = await mountAndLoad({ started: true });

    participantService.updateParticipant.mockResolvedValue({
      ...baseParticipant,
      started: true,
      consent: false
    });

    const radioInputs = wrapper.findAll("input[type=\"radio\"]");
    await radioInputs[1].setValue(true);

    await wrapper.find("form").trigger("submit");
    await flushPromises();

    expect(participantService.updateParticipant).toHaveBeenCalledWith(
      "1",
      expect.objectContaining({ consent: false })
    );
    expect(Swal.fire).not.toHaveBeenCalledWith(expect.objectContaining({ icon: "error" }));
  });

  it("lets a student who agreed and has started an assignment confirm they still agree", async () => {
    const wrapper = await mountAndLoad({
      started: true,
      source: "CONSENT",
      consent: true,
      dateGiven: "2024-01-01T00:00:00.000Z"
    });

    const radioInputs = wrapper.findAll("input[type=\"radio\"]");
    await radioInputs[0].setValue(true);

    await wrapper.find("form").trigger("submit");
    await flushPromises();

    expect(participantService.updateParticipant).not.toHaveBeenCalled();
    expect(Swal.fire).toHaveBeenCalledWith(
      expect.objectContaining({ text: "Successfully submitted Consent", icon: "success" })
    );
  });
});
