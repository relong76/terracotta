import { authHeader } from "@/helpers"
import store from "@/store/index.js"

/**
 * Register methods
 */
export const groupConfigurationService = {
  update,
  updateAll
}

/**
 * Update group configuration
 */
async function update(experiment_id, exposure_id, group_id, configuration_id, group_configuration_dto) {
  const requestOptions = {
    method: "PUT",
    headers: { ...authHeader(), "Content-Type": "application/json" },
    body: JSON.stringify(
        group_configuration_dto
    )
  }

  return fetch(`${store.getters["api/aud"]}/api/experiments/${experiment_id}/exposures/${exposure_id}/messaging/group/${group_id}/configuration/${configuration_id}`, requestOptions).then(handleResponse);
}

/**
 * Update group configurations
 */
async function updateAll(experiment_id, exposure_id, group_configuration_dto) {
  const requestOptions = {
    method: "PUT",
    headers: { ...authHeader(), "Content-Type": "application/json" },
    body: JSON.stringify([
        ...group_configuration_dto
    ])
  }

  return fetch(`${store.getters["api/aud"]}/api/experiments/${experiment_id}/exposures/${exposure_id}/messaging/group/all/configuration`, requestOptions).then(handleResponse);
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
