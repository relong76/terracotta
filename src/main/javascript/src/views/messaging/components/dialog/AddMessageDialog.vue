<template>
  <div
    v-if="isSingleConditionExperiment"
  >
    <v-btn
      @click="handleVersionSelection('SINGLE')"
      color="primary"
      elevation="0"
    >
      Add message
    </v-btn>
  </div>
  <div
    v-else
  >
    <v-menu
      v-model="addMessageDialogOpen"
      :close-on-content-click="false"
      :open-on-click="true"
      :open-on-hover="false"
      content-class="add-message-dialog"
      transition="scale-transition"
      origin="right top"
      bottom
      left
      offset-y
    >
      <template
        v-slot:activator="{ on, attrs }"
      >
        <v-btn
          v-on="on"
          v-bind="attrs"
          color="primary"
          elevation="0"
          :disabled="disableAddMessageButton"
        >
          Add Message
        </v-btn>
      </template>
      <span class="add-message-dialog">
        <div class="add-message-version-option">
          <v-btn
            @click="handleVersionSelection('MULTIPLE')"
            color="primary"
            elevation="0"
          >
            With Different Versions
          </v-btn>
          <p>
            Create <u>multiple</u> versions of your message based on conditions.
          </p>
        </div>
        <div class="add-message-version-option">
          <v-btn
            @click="handleVersionSelection('SINGLE')"
            color="primary"
            elevation="0"
          >
            With Only One Version
          </v-btn>
          <p>
            Create <u>one</u> message for all students.
          </p>
        </div>
      </span>
    </v-menu>
  </div>

</template>

<script>
  export default {
    name: "AddMessageDialog",
    props: [
      "hasExistingMessage",
      "isSingleConditionExperiment"
    ],
    data: () => ({
      addMessageDialogOpen: false,
      disableAddMessageButton: false
    }),
    watch: {
      addMessageDialogOpen: {
        handler(newVal) {
          this.disableAddMessageButton = newVal;
        }
      }
    },
    methods: {
      handleVersionSelection(version) {
        this.$emit("add", version);
      }
    }
  }
</script>

<style lang="scss" scoped>
.btn-create-first-message {
  border-radius: 24px;
  width: fit-content;
  min-height: 48px;
  background-color: white !important;
  border: 1px solid;
}
div.v-menu__content.add-message-dialog {
  width: 350px;
  background-color: white;
  padding: 5px 5px 0 5px;
  > span {
    > div.add-message-version-option {
      border: thin solid lightgrey;
      border-radius: 5px;
      padding: 5px;
      margin-bottom: 5px;
      text-align: center;
      > p {
        margin-bottom: 0 !important;
        padding-bottom: 0 !important;
        text-align: left;
      }
    }
  }
}
</style>
