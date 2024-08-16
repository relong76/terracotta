import Vue from 'vue';
import Vuex from 'vuex';
import createPersistedState from 'vuex-persistedstate';

import { api } from './api.module';
import { alert } from './alert.module';
import { assignment } from './assignment.module';
import { assessment } from './assessment.module';
import { experiment } from './experiment.module';
import { condition } from './condition.module';
import { consent } from './consent.module';
import { exposures } from './exposures.module';
import { participants } from './participants.module';
import { outcome } from './outcome.module';
import { treatment } from './treatment.module';
import { exportdata } from './exportdata.module';
import { submissions } from './submission.module';
import { mediaevents } from "./mediaevents.module";
import { navigation } from "./navigation.module";
import { groups } from "./groups.module";
import { resultsDashboard } from "./dashboard/results.module";
import { group as messagingMessageGroup } from "./messaging/group.module";
import { groupConfiguration as messagingMessageGroupConfiguration } from "./messaging/group.configuration.module";
import { message as messagingMessage } from "./messaging/message.module";
import { messageConfiguration as messagingMessageConfiguration } from "./messaging/message.configuration.module";
import { content as messagingContent } from "./messaging/content.module";
import { customfield as messagingContentCustomField } from "./messaging/customfield.module";
import { standardfield as messagingContentStandardField } from "./messaging/standardfield.module";
import { attachment as messagingContentAttachment } from "./messaging/attachment.module";

Vue.use(Vuex);

const store = new Vuex.Store({
  plugins: [
    createPersistedState({
      key: 'terracotta'
    })
  ],
  modules: {
    api,
    alert,
    assignment,
    assessment,
    experiment,
    condition,
    consent,
    exposures,
    participants,
    outcome,
    treatment,
    exportdata,
    submissions,
    mediaevents,
    navigation,
    groups,
    resultsDashboard,
    messagingMessageGroup,
    messagingMessageGroupConfiguration,
    messagingMessage,
    messagingMessageConfiguration,
    messagingContent,
    messagingContentAttachment,
    messagingContentCustomField,
    messagingContentStandardField
  },
  strict: process.env.NODE_ENV !== 'production'
})

export default store;
