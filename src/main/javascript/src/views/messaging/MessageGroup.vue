<template>
  <v-card
    v-if="isLoaded"
    class="message-card mb-6"
  >
    <h1>Message Group</h1>
    <v-col>
      <v-row>
        <v-text-field
          v-model="title"
          label="Group title"
          outlined
          required
          dense
        />
      </v-row>
      <v-checkbox
        v-model="toConsentedOnly"
        :disabled="viewOnly"
        label="Send to consented participants only"
      />
    </v-col>
  </v-card>
</template>

<script>
import { mapGetters, mapActions } from "vuex";
import { editableMessageStatuses } from "@/helpers/messaging/status.js";

export default {
  name: "MessageGroup",
  components: {
  },
  data: () => ({
    messageGroup: null,
    isLoaded: false
  }),
  computed: {
    ...mapGetters({
      editMode: "navigation/editMode"
    }),
    experimentId() {
      return this.$route.params.experimentId;
    },
    exposureId() {
      return this.$route.params.exposureId;
    },
    configuration() {
      return this.group.configuration;
    },
    configurationId() {
      return this.configuration.id;
    },
    groupId() {
      return this.group.id;
    },
    type() {
      return this.$route.params.type || this.types.multiple;
    },
    mode() {
      return this.$route.params.mode || this.modes.new;
    },
    types() {
      return {
        multiple: "MULTIPLE",
        single: "SINGLE"
      }
    },
    modes() {
      return {
        new: "NEW",
        edit: "EDIT"
      }
    },
    isNew() {
      return this.mode === this.modes.new;
    },
    single() {
      return this.type === this.types.single;
    },
    group: {
      get() {
        return this.messageGroup || {};
      },
      set(newGroup) {
        this.messageGroup = newGroup;
      }
    },
    title: {
      get() {
        return this.configuration?.title || "";
      },
      set(newTitle) {
        this.messageGroup.configuration.title = newTitle;
      }
    },
    toConsentedOnly: {
      get() {
        return this.configuration !== null ? this.configuration.toConsentedOnly : false;
      },
      set(newToConsentedOnly) {
        this.configuration.toConsentedOnly = newToConsentedOnly;
      }
    },
    viewOnly() {
      return !editableMessageStatuses.includes(this.configuration.status);
    },
    getSaveExitPage() {
      return this.editMode?.callerPage?.name || "Home";
    }
  },
  methods: {
    ...mapActions({
      createMessageGroup: "messagingMessageGroup/create",
      updateMessageGroup: "messagingMessageGroupConfiguration/update"
    }),
    async saveExit() {
      await this.updateMessageGroup(
        [
          this.experimentId,
          this.exposureId,
          this.groupId,
          this.configurationId,
          this.configuration
        ]
      );
      this.$router.push({
        name: "ExperimentSummary",
        params: {
          experiment_id: this.experimentId,
          exposure_id: this.exposureId
        }
      });
    }
  },
  async mounted() {
    if (this.isNew) {
      // is new; create new message group
      this.group = await this.createMessageGroup(
        [
          this.experimentId,
          this.exposureId,
          this.single
        ]
      );
    } else {
      // is edit; use existing message group
      this.group = JSON.parse(this.$route.params.group);
    }
    this.isLoaded = true;
  }
}
</script>
