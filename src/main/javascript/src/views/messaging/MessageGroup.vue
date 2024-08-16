<template>
<div>
  <v-card
    class="message-card mb-6"
  >
    <h1>Message Group</h1>
    <h2>Message Group ID: {{ group.id }}</h2>
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
        v-model="group.toConsentedOnly"
        label="Send to consented participants only"
      />
    </v-col>
  </v-card>
</div>
</template>

<script>
import { mapGetters, mapActions } from "vuex";

export default {
  name: "MessageGroup",
  components: {
  },
  data: () => ({
    messageGroup: null
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
        return this.group?.title || "";
      },
      set(newTitle) {
        this.messageGroup.title = newTitle;
      }
    },
    getSaveExitPage() {
      return this.editMode?.callerPage?.name || "Home";
    },
    messageGroupsExpanded() {
      return this.$route.params.messageGroupsExpanded;
    }
  },
  methods: {
    ...mapActions({
      createMessageGroup: "messagingMessageGroup/create",
      updateMessageGroup: "messagingMessageGroup/update"
    }),
    async saveExit() {
      this.messageGroup = await this.updateMessageGroup(
        [
          this.experimentId,
          this.exposureId,
          this.messageGroup.id,
          this.messageGroup
        ]
      );
      this.$router.push({
        name: "ExperimentSummary",
        params: {
          experiment_id: this.experimentId,
          exposure_id: this.exposureId,
          messageGroupsExpanded: this.messageGroupsExpanded
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
  }
}
</script>
