package com.orshachar.knownissue.jira.utils;

import com.orshachar.knownissue.http.auth.AuthBuilder;
import com.orshachar.knownissue.http.exceptions.MethodHttpException;
import com.orshachar.knownissue.http.method.MethodBuilder;
import com.orshachar.knownissue.jira.entities.JiraIssueEntity;
import com.orshachar.knownissue.jira.entities.JiraIssueWithStatusEntity;
import com.orshachar.knownissue.jira.entities.JiraStatusEnum;
import org.apache.http.HttpStatus;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by Or on 17/12/17.
 */
public class JiraUtils {

    private String jiraUrl;
    private String username;
    private String password;
    private final String V2_URL_SUFFIX = "/rest/api/2";

    protected static final Logger LOGGER = LoggerFactory.getLogger(JiraUtils.class);

    public JiraUtils(String jiraUrl, String username, String password) {

        assertThat("jiraUrl must be set", jiraUrl, is(notNullValue()));
        assertThat("jira username must be set", username, is(notNullValue()));
        assertThat("jira password must be set", password, is(notNullValue()));

        this.jiraUrl = jiraUrl + V2_URL_SUFFIX;
        this.username = username;
        this.password = password;
    }
    
    public JiraStatusEnum getIssueStatus(String statusId) {
        LayeredConnectionSocketFactory ssl = MethodBuilder.getSslSocketFactory();
        try {
            JiraIssueWithStatusEntity issue =  MethodBuilder.newGet(jiraUrl + "/issue/" + statusId + "?fields=status").
                    expectedCode(HttpStatus.SC_OK).withContentType(ContentType.APPLICATION_JSON)
                    .withAuth(AuthBuilder.basic(username, password)).build().execute(JiraIssueWithStatusEntity.class);
            return issue.getFields().getStatus().getName();
        } catch (MethodHttpException e) {
            LOGGER.error("Failed to get Jira issue - " + statusId, e);
        }
        return null;
    }

    public JiraIssueEntity getIssue(String statusId) {
        LayeredConnectionSocketFactory ssl = MethodBuilder.getSslSocketFactory();
        try {
            return MethodBuilder.newGet(jiraUrl + "/issue/" + statusId).
                    expectedCode(HttpStatus.SC_OK).withContentType(ContentType.APPLICATION_JSON)
                    .withAuth(AuthBuilder.basic(username, password)).build().execute(JiraIssueEntity.class);
        } catch (MethodHttpException e) {
            LOGGER.error("Failed to get Jira issue - " + statusId, e);
        }
        return null;
    }
}
