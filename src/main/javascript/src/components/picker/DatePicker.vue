<template>
  <v-dialog
    ref="sendDateDialog"
    v-model="displayPicker"
    :return-value.sync="date"
    :width="getWidth"
    persistent
  >
    <template v-slot:activator="{ on, attrs }">
      <v-text-field
        v-model="date"
        :label="getLabel"
        v-bind="attrs"
        v-on="on"
        prepend-icon="mdi-calendar"
        readonly
      ></v-text-field>
    </template>
    <v-date-picker
      v-if="!readOnly && displayPicker"
      v-model="date"
      :min="new Date().toISOString()"
      scrollable
    >
      <v-spacer></v-spacer>
      <v-btn
        text
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
    </v-date-picker>
  </v-dialog>
</template>

<script>
export default {
  name: "DatePicker",
  props: {
    date: {
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
      return this.label || "Date";
    },
    getWidth() {
      return this.width || "290px";
    }
  },
  methods: {
    handleOk() {
      this.$refs.sendDateDialog.save(this.date);
      this.$emit("updated", this.date);
    },
    handleCancel() {
      this.displayPicker = false;
    }
  }
}
</script>
