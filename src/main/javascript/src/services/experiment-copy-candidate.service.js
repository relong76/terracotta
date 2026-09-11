import {
  authHeader,
  isJson
} from "@/helpers";

import { api } from "@/store/api.module";

export const experimentCopyCandidateService = {
  getAll,
  importCandidate,
  dismiss
};

async function getAll() {
  return request(
    "/api/experiments/copy-candidates"
  );
}

async function importCandidate(candidateId) {
  return request(
    `/api/experiments/copy-candidates/${candidateId}/import`,
    {
      method: "POST"
    }
  );
}

async function dismiss(candidateId) {
  return request(
    `/api/experiments/copy-candidates/${candidateId}/dismiss`,
    {
      method: "PUT"
    }
  );
}

async function request(path, options = {}) {
  const {
    method = "GET"
  } = options;

  const response = await fetch(
    `${api().aud}${path}`,
    {
      method,
      headers: {
        ...authHeader()
      }
    }
  );

  return handleResponse(response);
}

async function handleResponse(response) {
  try {
    const text = await response.text();

    const data =
      text && isJson(text)
        ? JSON.parse(text)
        : text;

    if (response.status === 204) {
      return [];
    }

    if (!response?.ok) {
      console.error(
        "handleResponse | error",
        {
          response,
          data
        }
      );

      return {
        data,
        status: response.status,
        error: data
      };
    }

    return data
      ? {
          data,
          status: response.status
        }
      : response;
  } catch (error) {
    console.error(
      "handleResponse | catch",
      {
        error
      }
    );

    return {
      error,
      status: response?.status
    };
  }
}
