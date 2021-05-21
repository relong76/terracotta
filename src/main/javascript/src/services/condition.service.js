import { authHeader } from '@/helpers'
// import store from '@/store/index.js'

/**
 * Register methods
 */
export const conditionService = {
    createCondition,
    update,
    delete: _delete
}

/**
 * Create Condition
 */
function createCondition(condition) {
    const requestOptions = {
        method: 'POST',
        headers: { ...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify(condition)
    }

    return fetch(`http://localhost:8081/api/experiments/${condition.experiment_experiment_id}/conditions`, requestOptions).then(handleResponse)
}

/**
 * Update Condition
 */
function update(condition) {
    const requestOptions = {
        method: 'PUT',
        headers: { ...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify(condition)
    }

    return fetch(`http://localhost:8081/api/experiments/${condition.experiment_experiment_id}/conditions/${condition.condition_id}`, requestOptions).then(handleResponse)
}

/**
 * Delete Condition
 *
 * (Prefixed function name with underscore because delete is a reserved word in javascript)
 */
function _delete(condition) {
    const requestOptions = {
        method: 'DELETE',
        headers: authHeader()
    }

    return fetch(`http://localhost:8081/api/experiment/${condition.experiment_experiment_id}/condition/${condition.condition_id}`, requestOptions).then(handleResponse)
}

/**
 * Handle API response
 */
function handleResponse(response) {
    return response.text()
    .then(text => {
        const data = text && JSON.parse(text)

        if (!response || !response.ok) {
            if (response.status === 401 || response.status === 402 || response.status === 500) {
                console.log("handleResponse | 401/402/500",{response})
            } else if (response.status===404) {
                console.log("handleResponse | 404",{response})
            }

            return response
        }

        console.log("handleResponse | then",{text,data,response})
        return data || response
    }).catch(text => {
        console.log("handleResponse | catch",{text})
    })
}