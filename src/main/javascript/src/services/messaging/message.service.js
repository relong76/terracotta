import { authHeader } from "@/helpers"
import store from "@/store/index.js"

/**
 * Register methods
 */
export const messageService = {
  get,
  update
}

/**
 * Get a message
 */
async function get(experiment_id, exposure_id, group_id, message_id) {
  const requestOptions = {
    method: "GET",
    headers: { ...authHeader(), "Content-Type": "application/json" }
  }

  return fetch(`${store.getters["api/aud"]}/api/experiments/${experiment_id}/exposures/${exposure_id}/messaging/group/${group_id}/message/${message_id}`, requestOptions).then(handleResponse);
}

/**
 * Update message
 */
async function update(experiment_id, exposure_id, group_id, message_id, payload) {
  const requestOptions = {
    method: "PUT",
    headers: { ...authHeader(), "Content-Type": "application/json" },
    body: JSON.stringify([
      ...payload
    ])
  }

  return fetch(`${store.getters["api/aud"]}/api/experiments/${experiment_id}/exposures/${exposure_id}/messaging/group/${group_id}/message/${message_id}`, requestOptions).then(handleResponse);
}

/**
 * Handle API response
 */
function handleResponse(response) {
  return response.text()
    .then(text => {
      const data = text && JSON.parse(text);

      if (!response || !response.ok) {
        if (response.status === 401 || response.status === 402 || response.status === 500) {
          console.log("handleResponse | 401/402/500",{response});
        } else if (response.status === 404) {
          console.log("handleResponse | 404",{response});
        }

        return response;
      } else if (response.status === 204) {
        console.log("handleResponse | 204",{text,data,response});
        return [];
      }

      return data || response;
    }).catch(text => {
      console.error("handleResponse | catch",{text});
    })
}
