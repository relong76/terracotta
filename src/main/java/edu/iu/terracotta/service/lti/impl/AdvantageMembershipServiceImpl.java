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

import edu.iu.terracotta.exceptions.ConnectionException;
import edu.iu.terracotta.exceptions.helper.ExceptionMessageGenerator;
import edu.iu.terracotta.model.LtiContextEntity;
import edu.iu.terracotta.model.PlatformDeployment;
import edu.iu.terracotta.model.membership.CourseUser;
import edu.iu.terracotta.model.membership.CourseUsers;
import edu.iu.terracotta.model.oauth2.LTIToken;
import edu.iu.terracotta.service.lti.AdvantageConnectorHelper;
import edu.iu.terracotta.service.lti.AdvantageMembershipService;
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
import java.util.List;
import java.util.Objects;

/**
 * This manages all the Membership call for the LTIRequest (and for LTI in general)
 * Necessary to get appropriate TX handling and service management
 */
@Slf4j
@Service
@SuppressWarnings({"rawtypes", "PMD.GuardLogStatement"})
public class AdvantageMembershipServiceImpl implements AdvantageMembershipService {

    @Autowired
    private AdvantageConnectorHelper advantageConnectorHelper;

    @Autowired
    private ExceptionMessageGenerator exceptionMessageGenerator;

    //Asking for a token with the right scope.
    @Override
    public LTIToken getToken(PlatformDeployment platformDeployment) throws ConnectionException {
        return advantageConnectorHelper.getToken(platformDeployment, "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly");
    }

    //Calling the membership service and getting a paginated result of users.
    @Override
    public CourseUsers callMembershipService(LTIToken ltiToken, LtiContextEntity context) throws ConnectionException {
        CourseUsers courseUsers;
        log.debug(TextConstants.TOKEN + ltiToken.getAccess_token());

        try {
            RestTemplate restTemplate = advantageConnectorHelper.createRestTemplate();
            //We add the token in the request with this.
            HttpEntity request = advantageConnectorHelper.createTokenizedRequestEntity(ltiToken);
            //The URL to get the course contents is stored in the context (in our database) because it came
            // from the platform when we created the link to the context, and we saved it then.
            final String getMembership = context.getContext_memberships_url();
            log.debug("GET_MEMBERSHIP -  " + getMembership);
            ResponseEntity<CourseUsers> membershipGetResponse = restTemplate.exchange(getMembership, HttpMethod.GET, request, CourseUsers.class);
            HttpStatus status = membershipGetResponse.getStatusCode();

            if (status.is2xxSuccessful()) {
                courseUsers = membershipGetResponse.getBody();
                List<CourseUser> courseUserList = new ArrayList<>(Objects.requireNonNull(courseUsers).getCourseUserList());
                //We deal here with pagination
                log.debug("We have {} users", courseUsers.getCourseUserList().size());
                String nextPage = advantageConnectorHelper.nextPage(membershipGetResponse.getHeaders());
                log.debug("We have next page: " + nextPage);

                while (nextPage != null) {
                    ResponseEntity<CourseUsers> responseForNextPage = restTemplate.exchange(nextPage, HttpMethod.GET, request, CourseUsers.class);
                    CourseUsers nextCourseList = responseForNextPage.getBody();
                    List<CourseUser> nextCourseUsersList = Objects.requireNonNull(nextCourseList).getCourseUserList();
                    log.debug("We have {} users in the next page", nextCourseList.getCourseUserList().size());
                    courseUserList.addAll(nextCourseUsersList);
                    nextPage = advantageConnectorHelper.nextPage(responseForNextPage.getHeaders());
                }

                courseUsers = new CourseUsers();
                courseUsers.getCourseUserList().addAll(courseUserList);
            } else {
                String exceptionMsg = "Can't get the membership";
                log.error(exceptionMsg);
                throw new ConnectionException(exceptionMsg);
            }
        } catch (Exception e) {
            StringBuilder exceptionMsg = new StringBuilder();
            exceptionMsg.append("Can't get the membership");
            log.error(exceptionMsg.toString(), e);
            throw new ConnectionException(exceptionMessageGenerator.exceptionMessage(exceptionMsg.toString(), e));
        }

        return courseUsers;
    }

}
