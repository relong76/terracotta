<template>
  <div
    v-if="loaded"
  >
    <v-card
      class="message-card mb-6"
    >
      <h1>Compose Message</h1>
      <h2>Condition:
        <v-chip
          v-if="group.messages.length > 1"
          label
          :color="conditionColorMapping[getConditionName(message.conditionId)]"
        >
          {{ getConditionName(message.conditionId) }}
        </v-chip>
      </h2>
      <v-col>
        <v-row>
          <v-select
            v-model="type"
            :items="messageTypes"
            :disabled="viewOnly"
            item-text="label"
            item-value="id"
            label="Type"
            single-line
            dense
            hide-details
            hide-selected
            outlined
          />
        </v-row>
        <v-row>
          <v-radio-group
            v-model="sendImmediately"
            :disabled="viewOnly"
            row
          >
            <v-radio
              :value="true"
              label="Send immediately"
              color="blue"
              class="mb-5"
            />
            <v-radio
              :value="false"
              label="Send at a later date"
              color="blue"
              class="mb-5"
            />
          </v-radio-group>
        </v-row>
        <v-row
          v-if="!sendImmediately"
        >
          <v-col>
            <date-picker
              :date="sendDate"
              :readOnly="viewOnly"
              label="Send Date"
              @updated="processSendDate"
            />
          </v-col>
          <v-col>
            <time-picker
              :time="sendTime"
              :readOnly="viewOnly"
              label="Send Time"
              @updated="processSendTime"
            />
          </v-col>
        </v-row>
        <v-col
          v-if="showReplyTo"
        >
          <v-row
            v-for="(replyTo, index) in replyTo"
            :key="index"
          >
            <v-text-field
              v-model="replyTo.email"
              :disabled="viewOnly"
              label="Reply to"
              outlined
              required
              dense
            />
            <v-btn
              v-if="allowAddReplyTo"
              @click="addReplyTo"
            >
              <v-icon>mdi-account-plus</v-icon>
            </v-btn>
            <v-btn
              v-if="allowRemoveReplyTo"
              @click="removeReplyTo(index)"
            >
              <v-icon>mdi-account-remove</v-icon>
            </v-btn>
          </v-row>
        </v-col>
        <v-row>
          <v-text-field
            v-model="subject"
            :disabled="viewOnly"
            label="Subject"
            outlined
            required
            dense
          />
        </v-row>
        <v-row>
          <tip-tap-editor
            :content="initialBody"
            :placeholders="placeholders"
            :editorType="editor"
            :readOnly="viewOnly"
            @edited="handleEditedBody"
            required
          />
        </v-row>
        <v-row
          v-if="showAttachments"
        >
          <file-uploader
            :files="attachments"
            :message="message"
            :readOnly="viewOnly"
            @created="attachmentCreate"
            @removed="attachmentRemove"
          />
        </v-row>
        <v-row
          v-if="!viewOnly"
        >
          <v-menu
            v-if="messagesAvailableToCopy.length > 0"
            v-model="copyMenuOpen"
            offset-y
            close-on-click
            close-on-content-click
          >
            <template
              v-slot:activator="{ on, attrs }"
            >
              <v-btn
                v-bind="attrs"
                v-on="on"
                color="primary"
                elevation="0"
                class="mb-3 mt-3"
                plain
              >
                Copy message from <v-icon>mdi-chevron-down</v-icon>
              </v-btn>
            </template>
            <v-list>
                <template
                  v-for="(group, index) in messagesAvailableToCopy"
                >
                  <v-menu
                    v-if="group.messages.length > 0 && hasMessagesNotCurrent(group.messages)"
                    :key="group.id"
                    transition="slide-x-transition"
                    offset-x
                    open-on-hover
                    close-on-click
                    close-on-content-click
                  >
                    <template
                      v-slot:activator="{ on, attrs }"
                    >
                      <v-list-item
                        :key="index"
                        v-bind="attrs"
                        v-on="on"
                      >
                        <v-list-item-title>
                          {{ group.configuration.title }}
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
                        v-for="message in group.messages"
                      >
                        <v-list-item
                          v-if="message.id != messageId"
                          :key="message.id"
                          @click="copyFrom(message)"
                        >
                          <v-list-item-title>
                            Message
                            <v-chip
                              v-if="group.messages.length > 1"
                              label
                              :color="conditionColorMapping[getConditionName(message.conditionId)]"
                            >
                              {{ getConditionName(message.conditionId) }}
                            </v-chip>
                          </v-list-item-title>
                        </v-list-item>
                      </template>
                    </v-list>
                  </v-menu>
                </template>
            </v-list>
          </v-menu>
        </v-row>
      </v-col>
    </v-card>
  </div>
</template>

<script>
import { mapGetters, mapActions } from "vuex";
import { editableMessageStatuses, message as messageStatus } from "@/helpers/messaging/status.js";
import moment from "moment";
import DatePicker from "@/components/picker/DatePicker";
import TimePicker from "@/components/picker/TimePicker";
import TipTapEditor from "@/components/editor/TipTapEditor";
import FileUploader from "@/views/messaging/components/fileuploader/FileUploader";

export default {
  name: "Message",
  components: {
    DatePicker,
    FileUploader,
    TimePicker,
    TipTapEditor
  },
  data: () => ({
    message: null,
    group: null,
    initialBody: null,
    messagesAvailableToCopy: [],
    copyMenuOpen: false,
    isCopied: false,
    previousMessage: null,
    loaded: false,
    send: {
      immediately: true,
      date: moment().format("YYYY-MM-DD"),
      time: moment().format("HH:mm:ss")
    }
  }),
  computed: {
    ...mapGetters({
      editMode: "navigation/editMode",
      conditionColorMapping: "condition/conditionColorMapping"
    }),
    groups() {
      return JSON.parse(this.$route.params.groups);
    },
    groupId() {
      return this.group.id;
    },
    messageId() {
      return this.message.id;
    },
    experiment() {
      return JSON.parse(this.$route.params.experiment);
    },
    experimentId() {
      return this.experiment.experimentId;
    },
    exposureId() {
      return this.group.exposureId;
    },
    conditions() {
      return this.experiment.conditions || [];
    },
    configuration: {
      get() {
        return this.message.configuration;
      },
      set(newConfiguration) {
        this.message.configuration = newConfiguration;
      }
    },
    type: {
      get() {
        return this.configuration.type;
      },
      set(newType) {
        this.message.configuration.type = newType;
      }
    },
    sendAt: {
      get() {
        return this.configuration.sendAt;
      },
      set(newSendAt) {
        this.configuration.sendAt = newSendAt;
      }
    },
    sendImmediately: {
      get() {
        return this.send.immediately;
      },
      set(newSendImmediately) {
        this.send.immediately = newSendImmediately;
      }
    },
    sendDate: {
      get() {
        return this.send.date;
      },
      set(newSendDate) {
        this.send.date = newSendDate;
      }
    },
    sendTime: {
      get() {
        return this.send.time;
      },
      set(newSendTime) {
        this.send.time = newSendTime;
      }
    },
    subject: {
      get() {
        return this.configuration.subject;
      },
      set(newSubject) {
        this.configuration.subject = newSubject;
      }
    },
    replyTo: {
      get() {
        return this.configuration.replyTo || [];
      },
      set(newReplyTo) {
        newReplyTo = Array.isArray(newReplyTo) ? newReplyTo : [newReplyTo];

        if (!this.configuration.replyTo) {
          this.configuration.replyTo = newReplyTo;
          return;
        }

        this.configuration.replyTo.push(...newReplyTo);
      }
    },
    content: {
      get() {
        return this.message.content;
      },
      set(newContent) {
        this.message.content = newContent;
      }
    },
    body: {
      get() {
        return this.content.body;
      },
      set(newBody) {
        this.content.body = newBody;
      }
    },
    attachments: {
      get() {
        return this.content.attachments || [];
      },
      set(newAttachment) {
        newAttachment = Array.isArray(newAttachment) ? newAttachment : [newAttachment];

        if (!this.content.attachments) {
          this.content.attachments = newAttachment;
          return;
        }

        this.content.attachments.push(...newAttachment);
      }
    },
    contentId() {
      return this.content.id;
    },
    configurationId() {
      return this.configuration.id;
    },
    standardPlaceholders() {
      return this.content.standardPlaceholders || [];
    },
    customPlaceholders() {
      return this.content.customPlaceholders || [];
    },
    messageTypes() {
      return [
        {id: "CONVERSATION", label: "Conversation", editor: "basic"},
        {id: "EMAIL", label: "Email", editor: "html"}
      ]
    },
    placeholders() {
      return [
        ...this.standardPlaceholders
          .filter((standardPlaceholder) => standardPlaceholder.enabled)
          .map((standardPlaceholder) => ({
              id: standardPlaceholder.id,
              label: standardPlaceholder.label,
              type: "standard"
            })
          ),
        ...this.customPlaceholders
          .filter((customPlaceholder) => customPlaceholder.enabled)
          .map((customPlaceholder) => ({
              id: customPlaceholder.id,
              label: customPlaceholder.label,
              type: "custom"
            })
          )
      ];
    },
    editor() {
      if (!this.configuration || !this.configuration.type) {
        return null;
      }

      return this.messageTypes.find((messageType) => messageType.id === this.type).editor;
    },
    showReplyTo() {
      return this.type === "EMAIL";
    },
    allowAddReplyTo() {
      return this.replyTo.every((replyTo) => this.validateEmail(replyTo.email));
    },
    allowRemoveReplyTo() {
      return this.replyTo.length > 1;
    },
    showAttachments() {
      return this.type !== null;
    },
    getSaveExitPage() {
      return this.editMode?.callerPage?.name || "Home";
    },
    viewOnly() {
      return !editableMessageStatuses.includes(this.configuration.status);
    }
  },
  methods: {
    ...mapActions({
      updateConfiguration: "messagingMessageConfiguration/update",
      updateContent: "messagingContent/update",
      createAttachment: "messagingContentAttachment/create",
      removeAttachment: "messagingContentAttachment/remove",
      get: "messagingMessage/get",
    }),
    addReplyTo() {
      this.replyTo = { email: "" };
    },
    removeReplyTo(index) {
      this.replyTo.splice(index, 1);
    },
    processSendDate(date) {
      this.sendDate = date;
    },
    processSendTime(time) {
      this.sendTime = time;
    },
    processSendAt() {
      if (this.send.immediately) {
        // send immediately, set date and time to current
        this.send = {
          immediately: true,
          date: moment().format("YYYY-MM-DD"),
          time: moment().format("HH:mm:ss")
        }
      }

      this.configuration.sendAt = moment(this.send.date + "T" + this.send.time).valueOf();
    },
    handleEditedBody(body) {
      this.body = body;
    },
    getCondition(conditionId) {
      return this.conditions.find((condition) => condition.conditionId === conditionId);
    },
    getConditionName(conditionId) {
      return this.getCondition(conditionId).name || "No condition";
    },
    hasMessagesNotCurrent(messages) {
      return messages.some((message) => message.id !== this.messageId);
    },
    validateEmail(email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(email);
    },
    findMessagessAvailableToCopy() {
      this.groups
        .filter((group) => group.configuration.status !== messageStatus.deleted)
        .forEach((group) => {
          var hasAvailableMessage = this.hasMessagesNotCurrent(group.messages);

          if (hasAvailableMessage) {
            this.messagesAvailableToCopy.push(group);
          }
        });
    },
    async copyFrom(fromMessage) {
      this.copyMenuOpen = false;
      this.isCopied = true;
      this.previousMessage = this.message;
      this.sendAt = fromMessage.configuration.sendAt;
      this.sendImmediately = fromMessage.configuration.sendAt <= Date.now();
      this.sendDate = moment(fromMessage.configuration.sendAt).format("YYYY-MM-DD");
      this.sendTime = moment(fromMessage.configuration.sendAt).format("HH:mm:ss");
      this.subject = fromMessage.configuration.subject;
      this.type = fromMessage.configuration.type;
      this.configuration.replyTo = [];
      this.replyTo = fromMessage.configuration.replyTo;
      this.content.attachments = [];
      this.attachments = fromMessage.content.attachments;
      this.body = fromMessage.content.body;
      this.initialBody = fromMessage.content.body;
    },
    async attachmentCreate(files) {
      // send file to server async
      for (let i = 0; i < files.length; i++) {
        var attachment = await this.createAttachment(
          [
            this.experimentId,
            this.exposureId,
            this.groupId,
            this.messageId,
            this.contentId,
            files[i]
          ]
        );

        this.attachments = attachment;
      }
    },
    async attachmentRemove(index) {
      // get file from attachments array
      var fileToRemove = this.attachments[index];

      // send async delete command to server if already uploaded
      await this.removeAttachment(
        [
          this.experimentId,
          this.exposureId,
          this.groupId,
          this.messageId,
          this.contentId,
          fileToRemove.id
        ]
      );

      this.attachments.splice(index, 1);
    },
    async doSave() {
      this.processSendAt();
      await this.process();
    },
    async saveExit() {
      if (!this.viewOnly) {
        await this.doSave();
      }

      this.$router.push({
        name: "ExperimentSummary",
        params: {
          experiment_id: this.experimentId,
          exposure_id: this.exposureId
        }
      });
    },
    async process() {
      this.content = await this.updateContent(
        [
          this.experimentId,
          this.exposureId,
          this.groupId,
          this.messageId,
          this.contentId,
          this.content
        ]
      );
      this.configuration = await this.updateConfiguration(
        [
          this.experimentId,
          this.exposureId,
          this.groupId,
          this.messageId,
          this.configurationId,
          this.configuration
        ]
      );
    },
    async initialize() {
      this.message = JSON.parse(this.$route.params.message);
      this.group = JSON.parse(this.$route.params.group)
      this.initialBody = this.body;
      this.initializeReplyTo();
    },
    initializeReplyTo() {
      if (this.replyTo.length) {
        return;
      }

      // initialize reply-to with message owner's email
      this.replyTo = { email: this.message.ownerEmail };
    }
  },
  async mounted() {
    await this.initialize();
    this.findMessagessAvailableToCopy();
    this.loaded = true;
  }
}
</script>
