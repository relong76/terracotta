<template>
  <v-data-table
    v-if="showTable"
    :headers="headers.group"
    :items="groups"
    :items-per-page="groups.length"
    :expanded.sync="groupMessagesExpanded"
    :mobile-breakpoint="getMobileBreakpoint"
    :sort-by="['order']"
    @item-expanded="handleGroupExpansionChange"
    @sorted="
      (event) =>
        saveOrder(
          event,
          groups
        )
    "
    class="v-data-table-alt v-data-table--sorted data-table-assignments mx-3 mb-5 mt-3"
    item-key="id"
    v-sortable-data-table
    hide-default-footer
    show-expand
  >
    <template
      v-slot:item.drag="{}"
    >
      <span
        class="dragger"
      >
        <v-icon>mdi-drag</v-icon>
    </span>
    </template>
    <template
      v-slot:item.title="{ item: group }"
    >
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
    <template
      v-slot:item.status="{ item: group }"
    >
      {{ getGroupStatus(group) }}
    </template>
    <template
      v-slot:item.actions="{ item: group }"
    >
      <v-menu
        offset-y
      >
        <template
          v-slot:activator="{ on, attrs }"
        >
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
            v-if="showMoveAction(group)"
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
            v-if="showDeleteAction(group)"
            aria-label="delete message group"
            @click="handleDelete(group)"
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
        :colspan="headers.group.length"
        class="treatments-table-container"
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
          <template v-slot:item.subject="{ item: message }">
            <span>
              {{ getMessageSubject(message) }}
            </span>
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
            <span>
              {{ getMessageStatus(message) }}
            </span>
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
import { mapGetters, mapMutations, mapActions } from "vuex";
import { editableMessageStatuses, message as messageStatus } from "@/helpers/messaging/status.js";
import Sortable from "sortablejs";

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
    mobileBreakpoint: {
      type: Number
    }
  },
  data: () => ({
    headers: {
      group: [
        {
          text: "",
          align: "start",
          sortable: false,
          value: "drag"
        },
        {
          text: "Subject",
          align: "start",
          sortable: false,
          value: "title"
        },
        {
          text: "Status",
          align: "center",
          sortable: false,
          value: "status"
        },
        {
          text: "Actions",
          align: "center",
          sortable: false,
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
          sortable: false,
          value: "subject",
        },
        /*{
          text: "Condition",
          align: "center",
          sortable: false,
          value: "condition",
        },
        {
          text: "Owner Email",
          align: "center",
          sortable: false,
          value: "ownerEmail",
        },
        {
          text: "Status",
          align: "center",
          sortable: false,
          value: "status"
        },
        {
          text: "Actions",
          align: "center",
          value: "actions"
        }*/
      ]
    },
    isLoaded: false
  }),
  computed: {
    ...mapGetters({
      conditionColorMapping: "condition/conditionColorMapping",
      exposures: "exposures/exposures",
      groupMessages: "messagingMessageGroup/groupMessages",
      groupMessagesExpanded: "messagingMessageGroup/groupMessagesExpanded"
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
    groups: {
      get() {
        return this.groupMessages[this.exposureSetIndex] || [];
      },
      set(newGroup) {
        newGroup = Array.isArray(newGroup) ? newGroup : [newGroup];
        this.setGroupMessages(newGroup);
      }
    },
    showTable() {
      return this.isLoaded && this.groups.length > 0;
    },
    singleConditionExperiment() {
      return this.conditions.length === 1;
    }
  },
  methods: {
    ...mapActions({
      saveEditMode: "navigation/saveEditMode",
      getAll: "messagingMessageGroup/getAll",
      updateAll: "messagingMessageGroupConfiguration/updateAll",
      send: "messagingMessageGroup/send",
      delete: "messagingMessageGroup/deleteGroup",
      move: "messagingMessageGroup/move",
      duplicate: "messagingMessageGroup/duplicate"
    }),
    ...mapMutations({
      setGroupMessages: "messagingMessageGroup/setGroupMessages",
      setGroupMessagesExpanded: "messagingMessageGroup/setGroupMessagesExpanded"
    }),
    messageButtonText(message) {
      return !message.configuration || editableMessageStatuses.includes(message.configuration.status) ? "EDIT" : "VIEW";
    },
    messageButtonIcon(message) {
      return !message.configuration || editableMessageStatuses.includes(message.configuration.status) ? "mdi-pencil" : "mdi-eye";
    },
    getGroupTitle(group) {
      return group?.configuration?.title || "No title";
    },
    getGroupStatus(group) {
      const everyMessageQueuedAndPastSendAt = group.messages.every((message) => message.configuration.status === messageStatus.queued && message.configuration.sendAt <= Date.now());

      if (everyMessageQueuedAndPastSendAt) {
        return messageStatus.sent;
      }

      return group.configuration?.status || messageStatus.created;
    },
    getCondition(conditionId) {
      return this.conditions.find((condition) => condition.conditionId === conditionId);
    },
    getConditionName(conditionId) {
      return this.getCondition(conditionId).name || "No condition";
    },
    getMessageSubject(message) {
      return message.configuration?.subject || "No subject";
    },
    getMessageStatus(message) {
      if (message.configuration.status === messageStatus.queued && message.configuration.sendAt < Date.now()) {
        return messageStatus.sent;
      }

      return message.configuration?.status || messageStatus.created;
    },
    showMoveAction(group) {
      return group.configuration.status !== messageStatus.sent && this.exposures.length > 1;
    },
    showSendAction(group) {
      return group.configuration.status === messageStatus.ready;
    },
    showDeleteAction(group) {
      return group.configuration.status !== messageStatus.sent;
    },
    handleGroupExpansionChange({ item: group, value: isExpanded }) {
      if (isExpanded) {
        this.setGroupMessagesExpanded(this.groupMessagesExpanded.toSpliced(this.groupMessagesExpanded.length, 0, group));
      } else {
        const index = this.groupMessagesExpanded.findIndex((g) => g.id === group.id);

        if (index !== -1) {
          this.setGroupMessagesExpanded(this.groupMessagesExpanded.toSpliced(index, 1));
        }
      }
    },
    async saveOrder(event, groups) {
      const movedGroup = groups[event.oldIndex];
      groups = groups.toSpliced(event.oldIndex, 1);
      groups = groups.toSpliced(event.newIndex, 0, movedGroup);
      const updated = groups.map((group, idx) => ({
        ...group.configuration,
        order: idx + 1,
      }));
      await this.updateAll(
        [
          this.experimentId,
          this.exposureId,
          updated
        ]
      );
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
          group: JSON.stringify(group)
        }
      });
    },
    async handleSend(group) {
      try {
        await this.send(
          [
            this.experimentId,
            this.exposureId,
            group.id
          ]
        );

        await this.refreshMessageGroups();
      } catch (error) {
        console.error("handleSend | catch", { error });
      }
    },
    async handleDelete(group) {
      try {
        await this.delete(
          [
            this.experimentId,
            this.exposureId,
            group.id
          ]
        );

        await this.refreshMessageGroups();
      } catch (error) {
        console.error("handleMoveGroup | catch", { error });
      }
    },
    async handleGroupMove(targetExposureId, group) {
      try {
        await this.move([
          this.experimentId,
          group.exposureId,
          group.id,
          {
            ...group,
            exposureId: targetExposureId
          }
        ]);

        await this.refreshMessageGroups();
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

        await this.refreshMessageGroups();
        // add the duplicated group to expanded
        this.setGroupMessagesExpanded(this.groupMessagesExpanded.toSpliced(this.groupMessagesExpanded.length, 0, response.data));
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
            groups: JSON.stringify(this.groups),
            experiment: JSON.stringify(this.experiment)
          }
        }
      )
    },
    async refreshMessageGroups() {
      const refreshedGroupMessages = [];
      for (let [index, exposure] of this.exposures.entries()) {
        refreshedGroupMessages[index] = await this.getAll(
          [
            this.experimentId,
            exposure.exposureId
          ]
        );
      }
      this.groups = refreshedGroupMessages;
    }
  },
  async mounted() {
    await this.refreshMessageGroups();

    if (!this.groupMessagesExpanded) {
      this.setGroupMessagesExpanded([]);
    }

    this.isLoaded = true;
  },
  directives: {
    sortableDataTable: {
      bind(el, binding, vnode) {
        const options = {
          animation: 150,
          onUpdate: function(event) {
            vnode.child.$emit("sorted", event);
          },
          handle: ".dragger",
        };
        Sortable.create(el.getElementsByTagName("tbody")[0], options);
      },
    },
  },
}
</script>
