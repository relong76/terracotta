import { authHeader } from "@/helpers";
import store from "@/store/index.js";

// /**
//  * Register methods
//  */
export const mediaEventsService = {
  createVideoEvent,
};

/**
 * Send Video Started Event
 */
async function createVideoEvent({
  experiment_id,
  condition_id,
  treatment_id,
  assessment_id,
  submission_id,
  question_id,
  event,
}) {
  const requestOptions = {
    method: "POST",
    headers: authHeader(),
    body: JSON.stringify(event),
  };

  const url = `${store.getters["api/aud"]}/api/experiments/${experiment_id}/conditions/${condition_id}/treatments/${treatment_id}/assessments/${assessment_id}/submissions/${submission_id}/questions/${question_id}/media_event`;
  // console.log(requestOptions.method, url, event);
  return fetch(url, requestOptions).then(handleResponse);
}

/**
 * Handle API response
 */
function handleResponse(response) {
  return response
    .text()
    .then((text) => {
      const data = text && JSON.parse(text);

      if (!response || !response.ok) {
        if (
          response.status === 401 ||
          response.status === 402 ||
          response.status === 500
        ) {
          console.log("handleResponse | 401/402/500", { response });
        } else if (response.status === 404) {
          console.log("handleResponse | 404", { response });
        }

        return response;
      } else if (response.status === 204) {
        return [];
      }

      return data || response;
    })
    .catch((text) => {
      console.log("handleResponse | catch", { text });
    });
}
