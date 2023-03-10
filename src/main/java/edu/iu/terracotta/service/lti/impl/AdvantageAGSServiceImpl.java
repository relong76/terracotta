/**
 * Copyright 2021 Unicon (R)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.iu.terracotta.service.lti.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.iu.terracotta.exceptions.ConnectionException;
import edu.iu.terracotta.exceptions.helper.ExceptionMessageGenerator;
import edu.iu.terracotta.model.LtiContextEntity;
import edu.iu.terracotta.model.PlatformDeployment;
import edu.iu.terracotta.model.ags.LineItem;
import edu.iu.terracotta.model.ags.LineItems;
import edu.iu.terracotta.model.ags.Result;
import edu.iu.terracotta.model.ags.Results;
import edu.iu.terracotta.model.ags.Score;
import edu.iu.terracotta.model.oauth2.LTIToken;
import edu.iu.terracotta.service.lti.AdvantageAGSService;
import edu.iu.terracotta.service.lti.AdvantageConnectorHelper;
import edu.iu.terracotta.utils.TextConstants;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This manages all the Membership call for the LTIRequest (and for LTI in general)
 * Necessary to get appropriate TX handling and service management
 */
@Slf4j
@Service
@SuppressWarnings({"rawtypes", "PMD.GuardLogStatement"})
public class AdvantageAGSServiceImpl implements AdvantageAGSService {

    @Autowired
    private AdvantageConnectorHelper advantageConnectorHelper;

    @Autowired
    private ExceptionMessageGenerator exceptionMessageGenerator;

    //Asking for a token with the right scope.
    @Override
    public LTIToken getToken(String type, PlatformDeployment platformDeployment) throws ConnectionException {
        String scope = "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem";

        if ("results".equals(type)) {
            scope = "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly";
        }

        if ("scores".equals(type)) {
            scope = "https://purl.imsglobal.org/spec/lti-ags/scope/score";
        }

        return advantageConnectorHelper.getToken(platformDeployment, scope);
    }

    //Calling the AGS service and getting a paginated result of lineitems.
    @Override
    public LineItems getLineItems(LTIToken ltiToken, LtiContextEntity context) throws ConnectionException {
        LineItems lineItems = new LineItems();
        log.debug(TextConstants.TOKEN + ltiToken.getAccess_token());

        try {
            RestTemplate restTemplate = advantageConnectorHelper.createRestTemplate();
            //We add the token in the request with this.
            HttpEntity request = advantageConnectorHelper.createTokenizedRequestEntity(ltiToken);
            //The URL to get the course contents is stored in the context (in our database) because it came
            // from the platform when we created the link to the context, and we saved it then.
            final String getLineItems = context.getLineitems();
            log.debug("GET_LINEITEMS -  " + getLineItems);
            ResponseEntity<LineItem[]> lineItemsGetResponse = restTemplate.exchange(getLineItems, HttpMethod.GET, request, LineItem[].class);
            HttpStatus status = lineItemsGetResponse.getStatusCode();

            if (status.is2xxSuccessful()) {
                List<LineItem> lineItemsList = new ArrayList<>(Arrays.asList(Objects.requireNonNull(lineItemsGetResponse.getBody())));
                //We deal here with pagination
                log.debug("We have {} lineItems", lineItems.getLineItemList().size());
                String nextPage = advantageConnectorHelper.nextPage(lineItemsGetResponse.getHeaders());
                log.debug("We have next page: " + nextPage);

                while (nextPage != null) {
                    ResponseEntity<LineItem[]> responseForNextPage = restTemplate.exchange(nextPage, HttpMethod.GET, request, LineItem[].class);
                    LineItem[] nextLineItemsList = responseForNextPage.getBody();
                    //List<LineItem> nextLineItems = nextLineItemsList.getLineItemList();
                    log.debug("We have {} lineitems in the next page", nextLineItemsList.length);
                    lineItemsList.addAll(Arrays.asList(nextLineItemsList));
                    nextPage = advantageConnectorHelper.nextPage(responseForNextPage.getHeaders());
                }

                lineItems.getLineItemList().addAll(lineItemsList);
            } else {
                String exceptionMsg = "Can't get the AGS";
                log.error(exceptionMsg);
                throw new ConnectionException(exceptionMsg);
            }
        } catch (Exception e) {
            StringBuilder exceptionMsg = new StringBuilder();
            exceptionMsg.append("Can't get the AGS");
            log.error(exceptionMsg.toString(), e);
            throw new ConnectionException(exceptionMessageGenerator.exceptionMessage(exceptionMsg.toString(), e));
        }

        return lineItems;
    }

    @Override
    public boolean deleteLineItem(LTIToken ltiToken, LtiContextEntity context, String id) throws ConnectionException {
        log.debug(TextConstants.TOKEN + ltiToken.getAccess_token());

        try {
            RestTemplate restTemplate = advantageConnectorHelper.createRestTemplate();
            //We add the token in the request with this.
            HttpEntity request = advantageConnectorHelper.createTokenizedRequestEntity(ltiToken);
            //The URL to get the course contents is stored in the context (in our database) because it came
            // from the platform when we created the link to the context, and we saved it then.
            final String deleteLineItem = context.getLineitems() + "/" + id;
            log.debug("DELETE_LINEITEM -  " + deleteLineItem);
            ResponseEntity<String> lineItemsGetResponse = restTemplate.
                    exchange(deleteLineItem, HttpMethod.DELETE, request, String.class);
            HttpStatus status = lineItemsGetResponse.getStatusCode();

            if (status.is2xxSuccessful()) {
                return true;
            }

            String exceptionMsg = "Can't delete the lineitem with id: " + id;
            log.error(exceptionMsg);
            throw new ConnectionException(exceptionMsg);
        } catch (Exception e) {
            StringBuilder exceptionMsg = new StringBuilder();
            exceptionMsg.append("Can't delete the lineitem with id").append(id);
            log.error(exceptionMsg.toString(), e);
            throw new ConnectionException(exceptionMessageGenerator.exceptionMessage(exceptionMsg.toString(), e));
        }
    }

    @Override
    public LineItem putLineItem(LTIToken ltiToken, LtiContextEntity context, LineItem lineItem) throws ConnectionException {
        log.debug(TextConstants.TOKEN + ltiToken.getAccess_token());
        LineItem resultlineItem;

        try {
            RestTemplate restTemplate = advantageConnectorHelper.createRestTemplate();
            //We add the token in the request with this.
            HttpEntity<LineItem> request = advantageConnectorHelper.createTokenizedRequestEntity(ltiToken, lineItem);
            //The URL to get the course contents is stored in the context (in our database) because it came
            // from the platform when we created the link to the context, and we saved it then.
            final String putLineItem = context.getLineitems() + "/" + lineItem.getId();
            log.debug("PUT_LINEITEM -  " + putLineItem);
            ResponseEntity<LineItem> lineItemsGetResponse = restTemplate.
                    exchange(putLineItem, HttpMethod.PUT, request, LineItem.class);
            HttpStatus status = lineItemsGetResponse.getStatusCode();

            if (status.is2xxSuccessful()) {
                resultlineItem = lineItemsGetResponse.getBody();
                //We deal here with pagination
            } else {
                String exceptionMsg = "Can't put the lineitem " + lineItem.getId();
                log.error(exceptionMsg);
                throw new ConnectionException(exceptionMsg);
            }
        } catch (Exception e) {
            StringBuilder exceptionMsg = new StringBuilder();
            exceptionMsg.append("Can't get put lineitem ").append(lineItem.getId());
            log.error(exceptionMsg.toString(), e);
            throw new ConnectionException(exceptionMessageGenerator.exceptionMessage(exceptionMsg.toString(), e));
        }

        return resultlineItem;
    }

    @Override
    public LineItem getLineItem(LTIToken ltiToken, LtiContextEntity context, String id) throws ConnectionException {
        LineItem lineItem;
        log.debug(TextConstants.TOKEN + ltiToken.getAccess_token());

        try {
            RestTemplate restTemplate = advantageConnectorHelper.createRestTemplate();
            //We add the token in the request with this.
            HttpEntity request = advantageConnectorHelper.createTokenizedRequestEntity(ltiToken);
            //The URL to get the course contents is stored in the context (in our database) because it came
            // from the platform when we created the link to the context, and we saved it then.
            final String getLineItem = context.getLineitems() + "/" + id;
            log.debug("GET_LINEITEMS -  " + getLineItem);
            ResponseEntity<LineItem> lineItemsGetResponse = restTemplate.
                    exchange(getLineItem, HttpMethod.GET, request, LineItem.class);
            HttpStatus status = lineItemsGetResponse.getStatusCode();

            if (status.is2xxSuccessful()) {
                lineItem = lineItemsGetResponse.getBody();
                //We deal here with pagination
            } else {
                String exceptionMsg = "Can't get the lineitem " + id;
                log.error(exceptionMsg);
                throw new ConnectionException(exceptionMsg);
            }
        } catch (Exception e) {
            StringBuilder exceptionMsg = new StringBuilder();
            exceptionMsg.append("Can't get the lineitem ").append(id);
            log.error(exceptionMsg.toString(), e);
            throw new ConnectionException(exceptionMessageGenerator.exceptionMessage(exceptionMsg.toString(), e));
        }

        return lineItem;
    }

    @Override
    public LineItems postLineItems(LTIToken ltiToken, LtiContextEntity context, LineItems lineItems) throws ConnectionException {
        LineItems resultLineItems = new LineItems();
        log.debug(TextConstants.TOKEN + ltiToken.getAccess_token());

        try {
            RestTemplate restTemplate = advantageConnectorHelper.createRestTemplate();
            //We add the token in the request with this.
            HttpEntity<LineItems> request = advantageConnectorHelper.createTokenizedRequestEntity(ltiToken, lineItems);
            //The URL to get the course contents is stored in the context (in our database) because it came
            // from the platform when we created the link to the context, and we saved it then.
            final String postLineItems = context.getLineitems();
            log.debug("POST_LINEITEMS -  " + postLineItems);
            ResponseEntity<LineItem[]> lineItemsGetResponse = restTemplate.
                    exchange(postLineItems, HttpMethod.POST, request, LineItem[].class);
            HttpStatus status = lineItemsGetResponse.getStatusCode();

            if (status.is2xxSuccessful()) {
                List<LineItem> lineItemsList = new ArrayList<>(Arrays.asList(Objects.requireNonNull(lineItemsGetResponse.getBody())));
                //We deal here with pagination
                log.debug("We have {} lineItems", lineItems.getLineItemList().size());
                String nextPage = advantageConnectorHelper.nextPage(lineItemsGetResponse.getHeaders());
                log.debug("We have next page: " + nextPage);

                while (nextPage != null) {
                    ResponseEntity<LineItems> responseForNextPage = restTemplate.exchange(nextPage, HttpMethod.GET, request, LineItems.class);
                    LineItems nextLineItemsList = responseForNextPage.getBody();
                    List<LineItem> nextLineItems = Objects.requireNonNull(nextLineItemsList).getLineItemList();
                    log.debug("We have {} lineitems in the next page", nextLineItemsList.getLineItemList().size());
                    lineItemsList.addAll(nextLineItems);
                    nextPage = advantageConnectorHelper.nextPage(responseForNextPage.getHeaders());
                }

                resultLineItems.getLineItemList().addAll(lineItemsList);
            } else {
                String exceptionMsg = "Can't post lineitems";
                log.error(exceptionMsg);
                throw new ConnectionException(exceptionMsg);
            }
        } catch (Exception e) {
            StringBuilder exceptionMsg = new StringBuilder();
            exceptionMsg.append("Can't post lineitems");
            log.error(exceptionMsg.toString(), e);
            throw new ConnectionException(exceptionMessageGenerator.exceptionMessage(exceptionMsg.toString(), e));
        }

        return resultLineItems;
    }

    @Override
    public Results getResults(LTIToken ltiTokenResults, LtiContextEntity context, String lineItemId) throws ConnectionException {
        Results results = new Results();
        log.debug(TextConstants.TOKEN + ltiTokenResults.getAccess_token());

        try {
            RestTemplate restTemplate = advantageConnectorHelper.createRestTemplate();
            //We add the token in the request with this.
            HttpEntity request = advantageConnectorHelper.createTokenizedRequestEntity(ltiTokenResults);
            //The URL to get the course contents is stored in the context (in our database) because it came
            // from the platform when we created the link to the context, and we saved it then.
            final String getResults = lineItemId + "/results";
            log.debug("GET_RESULTS -  " + getResults  + "/" + lineItemId + "/results");
            ResponseEntity<Result[]> resultsGetResponse = restTemplate.exchange(getResults, HttpMethod.GET, request, Result[].class);
            HttpStatus status = resultsGetResponse.getStatusCode();

            if (status.is2xxSuccessful()) {
                List<Result> resultList = new ArrayList<>(Arrays.asList(Objects.requireNonNull(resultsGetResponse.getBody())));
                //We deal here with pagination
                log.debug("We have {} results", results.getResultList().size());
                String nextPage = advantageConnectorHelper.nextPage(resultsGetResponse.getHeaders());
                log.debug("We have next page: " + nextPage);

                while (nextPage != null) {
                    ResponseEntity<Result[]> responseForNextPage = restTemplate.exchange(nextPage, HttpMethod.GET,
                            request, Result[].class);
                    List<Result> nextResults = new ArrayList<>(
                            Arrays.asList(Objects.requireNonNull(responseForNextPage.getBody())));
                    log.debug("We have {} results in the next page", nextResults.size());
                    resultList.addAll(nextResults);
                    nextPage = advantageConnectorHelper.nextPage(responseForNextPage.getHeaders());
                }
                results.getResultList().addAll(resultList);
            } else {
                String exceptionMsg = "Can't get the AGS";
                log.error(exceptionMsg);
                throw new ConnectionException(exceptionMsg);
            }
        } catch (Exception e) {
            StringBuilder exceptionMsg = new StringBuilder();
            exceptionMsg.append("Can't get the AGS");
            log.error(exceptionMsg.toString(), e);
            throw new ConnectionException(exceptionMessageGenerator.exceptionMessage(exceptionMsg.toString(), e));
        }

        return results;
    }

    @Override
    public void postScore(LTIToken lTITokenScores, LTIToken lTITokenResults, LtiContextEntity context, String lineItemId, Score score) throws ConnectionException {
        log.debug(TextConstants.TOKEN + lTITokenScores.getAccess_token());
        try {
            RestTemplate restTemplate = advantageConnectorHelper.createRestTemplate();
            // We add the token in the request with this.
            // HttpEntity<Score> request = advantageConnectorHelper.createTokenizedRequestEntity(lTITokenScores, score);
            // The URL to get the course contents is stored in the context (in our database) because it came
            // from the platform when we created the link to the context, and we saved it then.
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = objectMapper.writeValueAsString(score);
            HttpEntity<String> request = advantageConnectorHelper.createTokenizedRequestEntity(lTITokenScores, jsonStr);
            final String postScores = lineItemId + "/scores";
            log.debug("POST_SCORES -  " + postScores);
            ResponseEntity<Void> scoreGetResponse = restTemplate.exchange(postScores, HttpMethod.POST, request, Void.class);
            HttpStatus status = scoreGetResponse.getStatusCode();

            if (status.is2xxSuccessful()) {
                // return getResults(lTITokenResults, context, lineItemId);
                return;
            }

            String exceptionMsg = "Can't post scores";
            log.error(exceptionMsg);
            throw new ConnectionException(exceptionMsg);
        } catch (Exception e) {
            StringBuilder exceptionMsg = new StringBuilder();
            exceptionMsg.append("Can't post scores");
            log.error(exceptionMsg.toString(), e);
            throw new ConnectionException(exceptionMessageGenerator.exceptionMessage(exceptionMsg.toString(), e));
        }
    }

}
