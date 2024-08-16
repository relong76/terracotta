<template>
  <v-data-table
    v-if="showTable"
    :headers="headers.group"
    :items="displayedGroups"
    :items-per-page="displayedGroups.length"
    :expanded.sync="messageGroupsExpanded[exposureSetIndex]"
    :mobile-breakpoint="getMobileBreakpoint"
    item-key="id"
    hide-default-header
    hide-default-footer
    show-expand
  >
    <template v-slot:item.title="{ item: group }">
      {{ getGroupTitle(group) }}
      <v-chip
        v-if="group.messages.length === 1"
        label
        color="lightgrey"
        class="v-chip--only-one"
      >
        Only One Version
      </v-chip>
    </template>
    <template v-slot:item.actions="{ item: group }">
      <v-menu offset-y>
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            icon
            text
            tile
            v-bind="attrs"
            v-on="on"
            aria-label="message actions"
          >
            <v-icon>mdi-dots-horizontal</v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-list-item
            @click="handleGroupEdit(group)"
            aria-label="edit message group"
          >
            <v-list-item-title>
              <v-icon>mdi-pencil</v-icon> Edit
            </v-list-item-title>
          </v-list-item>
          <v-list-item
            @click="handleGroupDuplicate(group)"
            aria-label="duplicate message group"
          >
            <v-list-item-title>
              <v-icon>mdi-content-duplicate</v-icon> Duplicate
            </v-list-item-title>
          </v-list-item>
          <v-menu
            v-if="showMoveAction"
            offset-x
            :key="group.exposureId"
            open-on-hover
            transition="slide-x-transition"
          >
            <template v-slot:activator="{ on, attrs }">
              <v-list-item
                v-bind="attrs"
                v-on="on"
                aria-label="move message group to exposure set"
              >
                <v-list-item-title>
                  <v-icon>mdi-arrow-right-top</v-icon> Move
                </v-list-item-title>
                <v-list-item-action
                  class="justify-end"
                >
                  <v-icon>mdi-menu-right</v-icon>
                </v-list-item-action>
              </v-list-item>
            </template>
            <v-list>
              <template
                v-for="(exposure, idx) in exposures"
              >
                <v-list-item
                  v-if="exposure.exposureId !== group.exposureId"
                  :key="exposure.exposureId"
                  :aria-label="`Exposure set ${idx + 1}`"
                  @click="handleGroupMove(exposure.exposureId, group)"
                >
                  <v-list-item-title>
                    Exposure set {{ idx + 1 }}
                  </v-list-item-title>
                </v-list-item>
              </template>
            </v-list>
          </v-menu>
          <v-list-item
            v-if="showDeleteAction"
            aria-label="delete message group"
            @click="showDeleteAction(group)"
          >
            <v-list-item-title>
              <v-icon>mdi-delete</v-icon> Delete
            </v-list-item-title>
          </v-list-item>
          <v-list-item
            v-if="showSendAction(group)"
            @click="handleSend(group)"
            aria-label="send message group"
          >
            <v-list-item-title>
              <v-icon>mdi-send</v-icon> Send
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </template>
    <template v-slot:expanded-item="{ item: group }">
      <td
        :colspan="headers.message.length"
      >
        <v-data-table
          :headers="headers.message"
          :items="group.messages"
          :items-per-page="group.messages.length"
          hide-default-header
          hide-default-footer
          item-key="id"
          class="grey lighten-5"
        >
        <template
            v-slot:item.subject="{ item: message }"
          >
            {{ getMessageSubject(message) }}
          </template>
          <template
            v-slot:item.condition="{ item: message }"
          >
            <v-chip
              v-if="!singleConditionExperiment && group.messages.length === conditions.length"
              label
              :color="
                conditionColorMapping[
                  getConditionName(message.conditionId)
                ]
              "
            >
              {{
                getConditionName(message.conditionId)
              }}
            </v-chip>
          </template>
          <template
            v-slot:item.status="{ item: message }"
          >
            {{ getMessageStatus(message) }}
          </template>
          <template
            v-slot:item.actions="{ item: message }"
          >
            <v-btn
                text
                tile
                class="btn-treatment-edit"
                @click="handleMessageAction(group, message)"
              >
                <v-icon>{{ messageButtonIcon(message) }}</v-icon>
                <span
                  class="btn-edit"
                >
                  {{ messageButtonText(message) }}
                </span>
              </v-btn>
          </template>
        </v-data-table>
      </td>
    </template>
  </v-data-table>
</template>

<script>
import { mapGetters, mapActions } from "vuex";

export default {
  name: "MessageGroupTable",
  props: {
    experiment: {
      type: Object,
      required: true
    },
    exposure: {
      type: Object,
      required: true
    },
    exposureSetIndex: {
      type: Number,
      required: true
    },
    messageGroupsExpanded: {
      type: Array,
      required: false
    },
    mobileBreakpoint: {
      type: Number
    }
  },
  data: () => ({
    messageGroups: [],
    headers: {
      group: [
        {
          text: "Group Title",
          align: "start",
          value: "title"
        },
        {
          text: "Group Status",
          align: "center",
          value: "status"
        },
        {
          text: "Actions",
          align: "center",
          value: "actions"
        },
        {
          text: "",
          value: "data-table-expand"
        }
      ],
      message: [
        {
          text: "Subject",
          align: "start",
          value: "subject",
        },
        {
          text: "Condition",
          align: "center",
          value: "condition",
        },
        {
          text: "Owner Email",
          align: "center",
          value: "ownerEmail",
        },
        {
          text: "Message Status",
          align: "center",
          value: "status"
        },
        {
          text: "Actions",
          align: "center",
          value: "actions"
        }
      ]
    }
  }),
  computed: {
    ...mapGetters({
      conditionColorMapping: "condition/conditionColorMapping",
      exposures: "exposures/exposures",
    }),
    experimentId() {
      return this.experiment.experimentId;
    },
    exposureId() {
      return this.exposure.exposureId;
    },
    conditions() {
      return this.experiment.conditions || [];
    },
    getMobileBreakpoint() {
      return this.mobileBreakpoint || 636;
    },
    status() {
      return {
        editable: {
          copied: "COPIED",
          created: "CREATED",
          edited: "EDITED",
          ready: "READY"
        },
        uneditable: {
          canceled: "CANCELED",
          deleted: "DELETED",
          error: "ERROR",
          processing: "PROCESSING",
          queued: "QUEUED",
          sent: "SENT"
        }
      }
    },
    editableStatuses() {
      return Object.keys(this.status.editable)
        .map((editable) => this.status.editable[editable]);
    },
    displayedGroups() {
      // display only non-deleted groups
      return this.messageGroups[exposureSetIndex].filter((group) => group.status !== this.status.uneditable.deleted);
    },
    groups: {
      get() {
        return this.messageGroups[exposureSetIndex] || [];
      },
      set(newGroup) {
        newGroup = Array.isArray(newGroup) ? newGroup : [newGroup];

        newGroup = newGroup.map(
          newGroup => {
            // check if all messages are ready to be sent
            newGroup.isReady = newGroup.messages.filter((message) => message.status !== this.status.editable.ready).length === 0;
            return newGroup;
          }
        );

        if (!this.messageGroups[exposureSetIndex]) {
          this.messageGroups[exposureSetIndex] = newGroup;
        } else {
          this.messageGroups[exposureSetIndex].push(...newGroup);
        }
      }
    },
    groupsCount() {
      return this.groups.length;
    },
    showTable() {
      return this.groupsCount > 0;
    },
    singleConditionExperiment() {
      return this.conditions.length === 1;
    },
    showMoveAction() {
      return this.exposures.length > 1;
    }
  },
  methods: {
    ...mapActions({
      saveEditMode: "navigation/saveEditMode",
      getAll: "messagingMessageGroup/getAll",
      update: "messagingMessageGroup/update",
      send: "messagingMessageGroup/send",
      delete: "messagingMessageGroup/deleteGroup",
      move: "messagingMessageGroup/move",
      duplicate: "messagingMessageGroup/duplicate"
    }),
    messageButtonText(message) {
      return !message.configuration || this.editableStatuses.includes(message.configuration.status) ? "EDIT" : "VIEW";
    },
    messageButtonIcon(message) {
      return !message.configuration || this.editableStatuses.includes(message.configuration.status) ? "mdi-pencil" : "mdi-eye";
    },
    getGroupTitle(group) {
      return group?.title || "No title";
    },
    getCondition(conditionId) {
      return this.conditions.find((condition) => condition.conditionId === conditionId);
    },
    getConditionName(conditionId) {
      return this.getCondition(conditionId).name || "No condition";
    },
    getMessageSubject(message) {
      return message.configuration?.subject || "No subject"
    },
    getMessageStatus(message) {
      return message.configuration?.status || this.status.editable.created;
    },
    showSendAction(group) {
      return group.status === this.status.editable.ready;
    },
    showDeleteAction(group) {
      return group.status !== this.status.uneditable.sent;
    },
    updateGroups(updatedGroup) {
      const index = this.groups.findIndex((g) => g.id === updatedGroup.id);

      if (index >= 0) {
        this.messageGroups[index] = updatedGroup;
      }

      // reset array for computed properties
      this.messageGroups[exposureSetIndex] = [...this.messageGroups[exposureSetIndex]];
    },
    removeExpandedGroup(expandedGroup) {
      const index = this.messageGroupsExpanded.findIndex((g) => g.id === expandedGroup.id);

      if (index >= 0) {
        this.messageGroupsExpanded.splice(index, 1);
      }
    },
    async handleGroupEdit(group) {
      await this.saveEditMode({
        initialPage: "MessageGroup",
        callerPage: {
          name: "ExperimentSummary",
          tab: "assignment",
          exposureSet: this.exposureSetIndex
        }
      });
      this.$router.push({
        name: "MessageGroup",
        params: {
          experimentId: this.experimentId,
          exposureId: this.exposureId,
          type: null,
          mode: "EDIT",
          group: JSON.stringify(group),
          messageGroupsExpanded: JSON.stringify(this.messageGroupsExpanded)
        }
      });
    },
    async handleSend(group) {
      const updatedGroup = await this.send(
        [
          this.experimentId,
          this.exposureId,
          group.id
        ]
      );

      this.updateGroups(updatedGroup);
    },
    async handleDelete(group) {
      const deletedGroup = await this.delete(
        [
          this.experimentId,
          this.exposureId,
          group.id
        ]
      )

      this.updateGroups(deletedGroup);
      this.removeExpandedGroup(deletedGroup);
    },
    async handleGroupMove(targetExposureId, group) {
      try {
        const response = await this.move([
          this.experimentId,
          group.exposureId,
          group.id,
          {
            ...group,
            exposureId: targetExposureId
          }
        ]);

        if (response.status === 201) {
          this.refreshMessageGroups();
        }
      } catch (error) {
        console.error("handleMoveGroup | catch", { error });
      }
    },
    async handleGroupDuplicate(group) {
      try {
        const response = await this.duplicate([
          this.experimentId,
          group.exposureId,
          group.id
        ]);

        if (response.status === 201) {
          this.refreshMessageGroups();
        }
        // add the duplicated group to exapnded
        this.messageGroupsExpanded[this.exposureSetIndex].push(this.groups[this.groups.length - 1]);
      } catch (error) {
        console.error("handleMoveGroup | catch", { error });
      }
    },
    async handleMessageAction(group, message) {
      await this.saveEditMode({
        initialPage: "Message",
        callerPage: {
          name: "ExperimentSummary",
          tab: "assignment",
          exposureSet: this.exposureSetIndex
        }
      });
      this.$router.push(
        {
          name: "Message",
          params: {
            message: JSON.stringify(message),
            group: JSON.stringify(group),
            experimentId: this.experimentId,
            messageGroupsExpanded: JSON.stringify(this.messageGroupsExpanded)
          }
        }
      )
    },
    refreshMessageGroups() {
      for (let [index, exposure] of this.exposures.entries()) {
        this.messageGroups[index] = await this.getAll(
          [
            this.experimentId,
            exposure.exposureId
          ]
        );
      }
    }
  },
  async mounted() {
    this.refreshMessageGroups();
    if (!this.messageGroupsExpanded[this.exposureSetIndex]) {
      this.messageGroupsExpanded[this.exposureSetIndex] = [];
    }
  }
}
</script>
