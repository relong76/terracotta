<template>
  <div
    v-show="pageFullyLoaded"
    class="consent-steps my-5 mx-auto"
  >
    <v-row class="mb-6 mx-0">
      <div>
        You are being asked to participate in a research study. Please read the
        statement below, then scroll to the bottom to select your response. Your
        teacher will be able to see whether you submitted a response, but will
        not be able to see your selection.
      </div>
    </v-row>

    <v-alert
      v-if="respondedAlert.show"
      type="info"
      variant="outlined"
    >
      <v-row align="center">
        <v-col class="grow">
          You responded "{{ respondedAlert.consent }}agree to participate" on
          {{ respondedAlert.date }}
        </v-col>
      </v-row>
    </v-alert>

    <v-alert
      v-if="alreadyAccessedAlert.show"
      type="error"
      variant="outlined"
    >
      <v-row align="center">
        <v-col class="grow">
          You have already accessed an assignment that is part of this study. At
          this time, no matter your response to the following question, you
          cannot be included in this study.
        </v-col>
      </v-row>
    </v-alert>

    <VuePdfEmbed
      v-if="pageFullyLoaded"
      :source="`data:application/pdf;base64,${pdfFile}`"
    />

    <form @submit.prevent="updateConsent(answer || false)">
      <v-card
        ref="questionCard"
        class="mt-5"
      >
        <v-card-title>
          In consideration of the above, will you participate in this research
          study?
        </v-card-title>

        <v-radio-group
          v-model="answer"
          :disabled="disableOptions"
          class="ml-4"
        >
          <v-radio
            v-for="option in options"
            :key="option.label"
            :label="option.label"
            :value="option.value"
          />
        </v-radio-group>
      </v-card>

      <v-row class="submit-row">
        <v-btn
          :disabled="disableSubmit"
          elevation="0"
          color="primary"
          class="mr-4"
          type="submit"
        >
          Submit
        </v-btn>

        <div
          v-if="disableOptions"
          class="please-wait"
        >
          Submitting your consent. Please wait...
        </div>
      </v-row>
    </form>
  </div>
</template>

<script setup>
import {
  ref,
  computed,
  watch,
  onMounted
} from "vue";

import dayjs from "@/plugins/dayjs";
import Swal from "sweetalert2";
import VuePdfEmbed from "vue-pdf-embed";

import { consent as consentModule } from "@/store/consent.module";
import { participants as participantsModule } from "@/store/participants.module";
import { api as apiModule } from "@/store/api.module";

defineOptions({
  name: "StudentConsent"
});

const props = defineProps({
  experimentId: {
    type: String,
    required: true
  },
  userId: {
    type: String,
    required: true
  },
  // shown before an assignment the student launched without having answered: their answer opens it
  continuesToAssignment: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits([
  "loaded",
  "answered",
  "skipped"
]);

const consentStore = consentModule();
const participantsStore = participantsModule();
const apiStore = apiModule();

const answer = ref("");
const participant = ref(null);
const pdfFile = ref(null);
const pdfReady = ref(false);
const participantReady = ref(false);
const pageFullyLoaded = ref(false);
const disableSubmit = ref(true);
const disableOptions = ref(false);

const options = [
  {
    label: "I agree to participate",
    value: true
  },
  {
    label: "I do not agree to participate",
    value: false
  }
];

const hasConsentedAlready = computed(() => {
  if (!participant.value) {
    return false;
  }

  // an answer is the date that matches it: given for "agree", revoked for "do not agree". Either
  // date alone used to count, so a participant reset to unanswered but still carrying a date
  // given was told they'd responded "do not agree" on an "Invalid Date". A "do not agree" also
  // moves the source to REVOKED; a student left out for submitting before answering gets a date
  // revoked too, but keeps the CONSENT source, and hasn't answered
  const answeredDate = participant.value.consent
    ? participant.value.dateGiven
    : participant.value.dateRevoked;
  const answeredSource = participant.value.consent ? "CONSENT" : "REVOKED";

  return (
    participant.value.source === answeredSource &&
    answeredDate != null &&
    dayjs(answeredDate).isValid()
  );
});

const alreadyAccessedAlert = computed(() => {
  return {
    show:
      participant.value &&
      participant.value.started &&
      !participant.value.consent
  };
});

const respondedAlert = computed(() => {
  if (!participant.value) {
    return {
      show: false,
      consent: "",
      date: ""
    };
  }

  return {
    show: hasConsentedAlready.value,
    consent: participant.value.consent ? "" : "do not ",
    date: dayjs(
      participant.value.consent
        ? participant.value.dateGiven
        : participant.value.dateRevoked
    ).format("MMMM D, YYYY [ at ] h:mma")
  };
});

watch(pdfFile, () => {
  pdfReady.value = true;

  if (participantReady.value) {
    pageFullyLoaded.value = true;
  }
});

watch(
  participant,
  () => {
    participantReady.value = true;

    if (pdfReady.value) {
      pageFullyLoaded.value = true;
    }
  },
  { deep: true }
);

watch(pageFullyLoaded, isLoaded => {
  if (!isLoaded) {
    return;
  }

  emit("loaded");

  if (props.continuesToAssignment) {
    // at the top: in the LMS, Terracotta's frame grows to the whole page, so a centered popup
    // would sit halfway down a long consent PDF, out of sight. The student can skip the form: the
    // assignment then opens as it would have without it. A deny button, not cancel, so pressing
    // Escape or clicking outside closes the popup without skipping
    void Swal.fire({
      text: "You have not yet completed the consent form. Please approve or revoke consent. You will be redirected to your assignment upon completion.",
      icon: "info",
      position: "top",
      showDenyButton: true,
      denyButtonText: "Just take me to my assignment",
      denyButtonColor: "#515961"
    }).then(result => {
      if (result?.isDenied) {
        emit("skipped");
      }
    });
  }
});

watch(answer, newAnswer => {
  disableSubmit.value = newAnswer === "";
});

const updateConsent = answerValue => {
  // a student who has started an assignment without consenting can't join the study now, but
  // anyone can decline, and a student who agreed can always withdraw
  if (participant.value.started && !participant.value.consent && answerValue === true) {
    Swal.fire({
      text: "You have already accessed an assignment that is part of this study. At this time, no matter your response, you cannot be included in this study.",
      icon: "error"
    });

    return;
  }

  if (answerValue === "") {
    return;
  }

  if (
    hasConsentedAlready.value &&
    participant.value.consent === answerValue
  ) {
    Swal.fire({
      text: "Successfully submitted Consent",
      icon: "success"
    });

    return;
  }

  disableSubmit.value = true;
  disableOptions.value = true;

  const updatedParticipant = {
    ...participant.value,
    consent: answerValue
  };

  void submitParticipant(updatedParticipant);
};

const questionCard = ref(null);
const POPUP_GAP = 16;

// moves a top-positioned popup down to just above the consent question. The popup is positioned
// in the frame's viewport, which in the LMS is the whole (grown) page; it never goes above the top
const placeAboveQuestion = popup => {
  const card = questionCard.value?.$el ?? questionCard.value;

  if (!card?.getBoundingClientRect) {
    return;
  }

  const top = card.getBoundingClientRect().top - popup.offsetHeight - POPUP_GAP;
  popup.style.marginTop = `${Math.max(top, POPUP_GAP)}px`;
};

const submitParticipant = async participantData => {
  try {
    const response =
      await participantsStore.updateParticipant({
        experimentId: props.experimentId,
        participantData
      });

    disableSubmit.value = false;
    disableOptions.value = false;

    participant.value = {
      ...participant.value,
      ...response
    };

    // just above the question the student answered: in the LMS the frame grows to the whole page,
    // so a centered popup could be out of sight after a long consent PDF - and before an
    // assignment, closing this popup is what opens it
    await Swal.fire({
      text: props.continuesToAssignment
        ? "Successfully submitted consent. You will now be taken to your assignment."
        : "Successfully submitted consent",
      icon: "success",
      position: "top",
      didOpen: placeAboveQuestion
    });

    if (response?.message) {
      await Swal.fire({
        text: response.message,
        icon: "error"
      });

      return;
    }

    emit("answered");
  } catch (error) {
    console.log(
      "submitParticipant | catch",
      { error }
    );

    disableSubmit.value = false;
    disableOptions.value = false;
  }
};

const handleConsentFileDownload = async () => {
  const file =
    await consentStore.getConsentFile(
      props.experimentId
    );

  pdfFile.value = encodeURI(file);
};

onMounted(async () => {
  void handleConsentFileDownload();

  const stepResponse =
    await apiStore.reportStep({
      experimentId: props.experimentId,
      step: "launch_consent_assignment"
    });

  participant.value = stepResponse.data;
});
</script>

<style lang="scss" scoped>
.consent-steps {
  min-height: 100%;
  padding: 30px 45px;
}

div.vue-pdf-embed {
  margin: 0 auto;
  min-height: fit-content;
  max-height: fit-content;
  overflow-y: scroll;
  box-shadow:
    0 3px 1px -2px rgba(0, 0, 0, 0.2),
    0 2px 2px 0 rgba(0, 0, 0, 0.14),
    0 1px 5px 0 rgba(0, 0, 0, 0.12);
}

.submit-row {
  // space between the question card and the submit button (a v-row's own margins are reset here)
  margin: 24px 0 0;

  > .please-wait {
    max-height: fit-content;
    margin: auto 0;
    color: map.get($grey, "darker");
  }
}
</style>
