<template>
  <div
    v-if="loaded"
  >
    <v-card
      class="message-card mb-6"
    >
      <h1>Compose Message</h1>
      <h2>Message ID: {{ messageId }}</h2>
      <v-col>
        <v-row>
          <v-select
            v-model="configurationType"
            :items="messageTypes"
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
            v-model="send.immediately"
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
          v-if="!send.immediately"
        >
          <v-col>
            <date-picker
              :date="send.date"
              label="Send Date"
              @updated="sendDate"
            />
          </v-col>
          <v-col>
            <time-picker
              :time="send.time"
              label="Send Time"
              @updated="sendTime"
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
              label="Reply to"
              outlined
              required
              dense
            />
            <v-btn
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
            v-model="configuration.subject"
            label="Subject"
            outlined
            required
            dense
          />
        </v-row>
        <v-row>
          <tip-tap-editor
            :content="content.body"
            :placeholders="placeholders"
            :editorType="editor"
            @edited="handleEditedBody"
            required
          />
        </v-row>
        <v-row
          v-if="showAttachments"
        >
          <file-uploader
            :files="uploadedAttachments"
            :message="message"
            @created="attachmentCreate"
            @removed="attachmentRemove"
          />
        </v-row>
      </v-col>
    </v-card>
  </div>
</template>

<script>
import { mapGetters, mapActions } from "vuex";
import moment from "moment";
import DatePicker from "@/components/picker/DatePicker";
import TimePicker from "@/components/picker/TimePicker";
import TipTapEditor from "@/components/editor/TipTapEditor";
import FileUploader from "@/components/fileuploader/FileUploader";

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
    loaded: false,
    send: {
      immediately: true,
      date: moment().format("YYYY-MM-DD"),
      time: moment().format("HH:mm:ss")
    }
  }),
  computed: {
    ...mapGetters({
      editMode: "navigation/editMode"
    }),
    groupId() {
      return this.group.id;
    },
    messageId() {
      return this.message.id;
    },
    experimentId() {
      return this.$route.params.experimentId;
    },
    exposureId() {
      return this.group.exposureId;
    },
    configuration: {
      get() {
        return this.message.configuration;
      },
      set(configuration) {
        this.message.configuration = configuration;
      }
    },
    configurationType: {
      get() {
        return this.configuration.type;
      },
      set(type) {
        this.message.configuration.type = type;
      }
    },
    content: {
      get() {
        return this.message.content;
      },
      set(content) {
        this.message.content = content;
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
    messageStatus() {
      return this.configuration.status;
    },
    attachments: {
      get() {
        return this.content.attachments || [];
      },
      set(attachment) {
        if (!this.content.attachments) {
          this.content.attachments = [attachment];
          return;
        }

        this.content.attachments.push(attachment);
      }
    },
    uploadedAttachments() {
      // non-deleted attachments
      return this.attachments.filter((attachment) => attachment.status === this.status.attachment.uploaded);
    },
    status() {
      return {
        attachment: {
          created: "CREATED",
          deleted: "DELETED",
          error: "ERROR",
          uploaded: "UPLOADED"
        },
        message: {
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
      }
    },
    editableStatuses() {
      return Object.keys(this.status.message.editable)
        .map((editable) => this.status.message.editable[editable]);
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

      return this.messageTypes.find((messageType) => messageType.id === this.configurationType).editor;
    },
    replyTo: {
      get() {
        return this.configuration.replyTo || [];
      },
      set(newReplyTo) {
        if (!this.replyTo.length) {
          this.configuration.replyTo = [newReplyTo];
          return;
        }

        this.configuration.replyTo.push(newReplyTo);
      }
    },
    showReplyTo() {
      return this.configurationType === "EMAIL";
    },
    allowRemoveReplyTo() {
      return this.replyTo.length > 1;
    },
    showAttachments() {
      return this.configurationType !== null;
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
      createConfiguration: "messagingConfiguration/create",
      updateConfiguration: "messagingConfiguration/update",
      createContent: "messagingContent/create",
      updateContent: "messagingContent/update",
      createAttachment: "messagingContentAttachment/create",
      removeAttachment: "messagingContentAttachment/remove"
    }),
    addReplyTo() {
      this.replyTo = { email: "" };
    },
    removeReplyTo(index) {
      this.replyTo.splice(index, 1);
    },
    sendDate(date) {
      this.send.date = date;
    },
    sendTime(time) {
      this.send.time = time;
    },
    sendAt() {
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
      this.content.body = body;
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
      var deletedAttachment = await this.removeAttachment(
        [
          this.experimentId,
          this.exposureId,
          this.groupId,
          this.messageId,
          this.contentId,
          fileToRemove.id
        ]
      );

      // set attachment status to "deleted"
      this.attachments
        .find((attachment) => attachment.id === deletedAttachment.id)
        .status = this.status.attachment.deleted;
    },
    async doSave() {
      this.sendAt();
      await this.process();
    },
    async saveExit() {
      await this.doSave();
      this.$router.push({
        name: "ExperimentSummary",
        params: {
          experiment_id: this.experimentId,
          exposure_id: this.exposureId,
          messageGroupsExpanded: this.messageGroupsExpanded
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
    this.loaded = true;
  }
}
</script>
