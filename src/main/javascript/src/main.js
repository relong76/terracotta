import App from './App.vue';
import router from './router';
import store from './store/index';
import Vue from 'vue';
import VueRouterBackButton from 'vue-router-back-button';
import VueSweetalert2 from 'vue-sweetalert2';
import vuetify from './plugins/vuetify';

Vue.config.productionTip = false

Vue.use(VueRouterBackButton, { router })
Vue.use(VueSweetalert2);

const url = new URL(window.location.href);
const params = new URLSearchParams(url.search);
const tokenParam = params.get('token');
const lmsApiOAuthURL = params.get("lms_api_oauth_url");
const integrations = {
  integration: params.get("integrations"),
  status: params.get("status")
};
var appProps = {};

const operations = [];

if (tokenParam) {
  operations.push(store.dispatch('api/setLtiToken', tokenParam));
}

operations.push(store.dispatch('api/setLmsApiOAuthURL', decodeURIComponent(lmsApiOAuthURL)));
Promise.all(operations).then(startVue);

function startVue() {
  // always start with clean experiment/s list
  store.dispatch('experiment/resetExperiment')
  store.dispatch('experiment/resetExperiments')
  store.dispatch('consent/resetConsent')

  cleanURL()

  if (lmsApiOAuthURL) {
    router.replace({name: "oauth2-redirect"});
  }

  if (integrations.integration && integrations.status) {
    appProps["integrationsData"] = integrations;
  }

  new Vue({
    store,
    router,
    vuetify,
    props: ["integrationsData"],
    render: h => h(App, {props: appProps}),
  }).$mount('#app')
}

function cleanURL() {
  // delete the token from the url
  params.delete("token");
  params.delete("lms_api_oauth_url");
  params.delete("integration");
  params.delete("status");
  // update the url without the token param
  window.history.replaceState(
      {},
      '',
      `${window.location.pathname}?${params}${window.location.hash}`,
  )
}
