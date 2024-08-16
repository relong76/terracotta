<template>
  <v-dialog
    ref="sendTimeDialog"
    v-model="displayPicker"
    :return-value.sync="time"
    :width="getWidth"
    persistent
  >
    <template v-slot:activator="{ on, attrs }">
      <v-text-field
        v-model="displayedTime"
        :label="getLabel"
        v-bind="attrs"
        v-on="on"
        prepend-icon="mdi-clock-time-four-outline"
        readonly
      ></v-text-field>
    </template>
    <v-time-picker
      v-if="!readOnly && displayPicker"
      v-model="time"
      full-width
      scrollable
    >
      <v-spacer></v-spacer>
      <v-btn
        text
        color="primary"
        @click="handleCancel()"
      >
        Cancel
      </v-btn>
      <v-btn
        text
        color="primary"
        @click="handleOk()"
      >
        OK
      </v-btn>
    </v-time-picker>
  </v-dialog>
</template>

<script>
import moment from 'moment';

export default {
  name: "TimePicker",
  props: {
    time: {
      type: String,
      required: false
    },
    label: {
      type: String,
      required: true
    },
    width: {
      type: String,
      required: false
    },
    readOnly: {
      type: Boolean,
      default: false
    }
  },
  data: () => ({
    displayPicker: false
  }),
  computed: {
    getLabel() {
      return this.label || "Time";
    },
    getWidth() {
      return this.width || "290px";
    },
    displayedTime() {
      return moment(this.time, "HH:mm").format("h:mm A")
    }
  },
  methods: {
    handleOk() {
      this.$refs.sendTimeDialog.save(this.time);
      this.$emit("updated", this.time);
    },
    handleCancel() {
      this.displayPicker = false;
    }
  }
}
</script>
