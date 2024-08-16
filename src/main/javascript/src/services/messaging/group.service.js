import { authHeader } from "@/helpers"
import store from "@/store/index.js"

/**
 * Register methods
 */
export const messageGroupService = {
  get,
  getAll,
  create,
  update,
  send,
  deleteGroup,
  move,
  duplicate
}

/**
 * Get a message group
 */
async function get(experiment_id, exposure_id, group_id) {
  const requestOptions = {
    method: "GET",
    headers: {...authHeader()}
  }

  return fetch(`${store.getters["api/aud"]}/api/experiments/${experiment_id}/exposures/${exposure_id}/messaging/group/${group_id}`, requestOptions).then(handleResponse);
}

/**
 * Get all message groups for an experiment and owner
 */
async function getAll(experiment_id, exposure_id) {
    const requestOptions = {
      method: "GET",
      headers: {...authHeader()}
    }

    return fetch(`${store.getters["api/aud"]}/api/experiments/${experiment_id}/exposures/${exposure_id}/messaging/group`, requestOptions).then(handleResponse);
  }

/**
 * Create message group and all child messages
 */
async function create(experiment_id, exposure_id, single) {
  const requestOptions = {
    method: "POST",
    headers: { ...authHeader(), "Content-Type": "application/json" }
  }

  return fetch(`${store.getters["api/aud"]}/api/experiments/${experiment_id}/exposures/${exposure_id}/messaging/group?single=${single}`, requestOptions).then(handleResponse);
}

/**
 * Update message group
 */
async function update(experiment_id, exposure_id, group_id, message_group_dto) {
  const requestOptions = {
    method: "PUT",
    headers: { ...authHeader(), "Content-Type": "application/json" },
    body: JSON.stringify(
      message_group_dto
    )
  }

  return fetch(`${store.getters["api/aud"]}/api/experiments/${experiment_id}/exposures/${exposure_id}/messaging/group/${group_id}`, requestOptions).then(handleResponse);
}

/**
 * Mark message group and all child messages as ready to send
 */
async function send(experiment_id, exposure_id, group_id) {
  const requestOptions = {
    method: "POST",
    headers: { ...authHeader(), "Content-Type": "application/json" }
  }

  return fetch(`${store.getters["api/aud"]}/api/experiments/${experiment_id}/exposures/${exposure_id}/messaging/group/${group_id}/send`, requestOptions).then(handleResponse);
}

/**
 * Delete message group
 */
async function deleteGroup(experiment_id, exposure_id, group_id) {
    const requestOptions = {
      method: "DELETE",
      headers: { ...authHeader(), "Content-Type": "application/json" }
    }

    return fetch(`${store.getters["api/aud"]}/api/experiments/${experiment_id}/exposures/${exposure_id}/messaging/group/${group_id}`, requestOptions).then(handleResponse);
  }

/**
 * Move message group
 */
async function move(experiment_id, exposure_id, group_id, group_dto) {
  const requestOptions = {
    method: "POST",
    headers: {...authHeader()},
    body: JSON.stringify(
      group_dto
    )
  }
  return fetch(`${store.getters["api/aud"]}/api/experiments/${experiment_id}/exposures/${exposure_id}/messaging/group/${group_id}/move`, requestOptions).then(handleResponse);
}

/**
 * Duplicate message group
 */
async function duplicate(experiment_id, exposure_id, group_id) {
  const requestOptions = {
    method: "POST",
    headers: {...authHeader()}
  }
  return fetch(`${store.getters["api/aud"]}/api/experiments/${experiment_id}/exposures/${exposure_id}/messaging/group/${group_id}/duplicate`, requestOptions).then(handleResponse);
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
