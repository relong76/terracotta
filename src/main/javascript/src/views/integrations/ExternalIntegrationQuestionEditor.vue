<template>
  <v-col>
    <v-row>External Integrations</v-row>
    <v-row>Question ID: {{ question.questionId }}</v-row>
    <v-row>Integration ID: {{ integration.id }}</v-row>
    <v-row>Integration Configuration ID: {{ configuration.id }}</v-row>
    <v-row>
      <v-select
        v-model="currentClient"
        :items="clientsData"
        item-text="name"
        item-value="id"
        label="Client Type"
        return-object
        single-line
        dense
        hide-details
        hide-selected
        outlined
      />
    </v-row>
    <v-row
      v-if="showUrls"
    >
      <v-col>
        <v-row>
          <v-text-field
            v-model="currentLaunchUrl"
            label="Launch URL"
            outlined
            required
          />
        </v-row>
        <v-row
          v-if="currentClient.customTokenVariableAllowed"
        >
          <v-text-field
            v-model="currentTokenVariable"
            label="Launch token variable"
            outlined
            required
          />
        </v-row>
        <v-row>
          <v-text-field
            v-model="currentScoreVariable"
            label="Score variable"
            outlined
            required
          />
        </v-row>
        <v-row>
          <v-textarea
            v-model="returnUrl"
            label="Return URL"
            rows="2"
            auto-grow
            outlined
            readonly
            dense
          />
        </v-row>
      </v-col>
    </v-row>
  </v-col>
</template>

<script>
export default {
  props: {
    question: {
      type: Object,
      required: true
    }
  },
  data: () => ({
    clientsData: [],
    client: null
  }),
  watch: {
    integration: {
      handler(integration) {
        this.$emit("integrationUpdated", integration);
      },
      deep: true,
      immediate: false
    },
    currentClient: {
      handler(newCurrentClient) {
        this.client = newCurrentClient;
        this.configurationClient = newCurrentClient;
      },
      deep: true,
      immediate: false
    }
  },
  computed: {
    loaded() {
      return this.question.integration !== null;
    },
    integration() {
      return this.question.integration;
    },
    integrationClients() {
      return this.integration.clients;
    },
    configuration() {
      return this.integration.configuration;
    },
    configurationClient: {
      get() {
        return this.configuration.client;
      },
      set(newClient) {
        this.configuration.client = newClient;
        this.configuration.launchUrl = this.currentLaunchUrl;
        this.configuration.scoreVariable = this.currentScoreVariable;
        this.configuration.tokenVariable = this.currentTokenVariable;
      }
    },
    clients: {
      get() {
        return this.integrationClients || [];
      },
      set(clients) {
        this.clientsData = clients.map(
          (client) => {
            return {
              ...client,
              launchUrl: null,
              scoreVariable: null
            }
          }
        )
      }
    },
    currentClient: {
      get() {
        return this.client;
      },
      set(newClient) {
        this.client = newClient;
      }
    },
    currentLaunchUrl: {
      get() {
        return this.currentClient.launchUrl;
      },
      set(newLaunchUrl) {
        this.currentClient.launchUrl = newLaunchUrl;
        this.configuration.launchUrl = newLaunchUrl;
      }
    },
    currentReturnUrl() {
      return this.currentClient.returnUrl || "";
    },
    currentScoreVariable: {
      get() {
        return this.currentClient.scoreVariable !== null ? this.currentClient.scoreVariable : "{{SCORE_VARIABLE}}";
      },
      set(newScoreVariable) {
        this.currentClient.scoreVariable = newScoreVariable;
        this.configuration.scoreVariable = newScoreVariable;
      }
    },
    currentTokenVariable: {
      get() {
        return this.currentClient.tokenVariable !== null ? this.currentClient.tokenVariable : "{{TOKEN_VARIABLE}}";
      },
      set(newTokenVariable) {
        if (!this.currentClient.customTokenVariableAllowed) {
          // user cannot update token variable
          this.configuration.tokenVariable = this.currentClient.tokenVariable;
        }

        this.currentClient.tokenVariable = newTokenVariable;
        this.configuration.tokenVariable = newTokenVariable;
      }
    },
    returnUrl() {
      if (!this.currentReturnUrl) {
        return "N/A";
      }

      // https://app.terracotta.education/integrations?launch_token={{TOKEN_VARIABLE}}&score={{SCORE_VARIABLE}}
      const urlSplit = this.currentReturnUrl.split("?");
      const querySplit = urlSplit[1].split("&");
      const launchTokenKey = querySplit[0].split("=")[0];
      const scoreKey = querySplit[1].split("=")[0];

      return urlSplit[0] + "?" + launchTokenKey + "=" + this.currentTokenVariable + "&" + scoreKey + "=" + this.currentScoreVariable;
    },
    showUrls() {
      // show urls if client is selected
      return this.currentClient != null;
    }
  },
  mounted() {
    this.clients = this.integrationClients;

    if (this.configurationClient) {
      this.currentClient = this.clientsData.find((clientData) => clientData.id == this.configurationClient.id);
      this.currentLaunchUrl = this.configuration.launchUrl;
      this.currentScoreVariable = this.configuration.scoreVariable;
      this.currentTokenVariable = this.configuration.tokenVariable;
    }
  }
}
</script>
