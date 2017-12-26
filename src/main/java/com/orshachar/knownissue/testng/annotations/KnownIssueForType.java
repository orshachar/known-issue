package com.orshachar.knownissue.testng.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Or on 17/11/17.
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface KnownIssueForType {

    /**
     * The jira ticket to check
     *
     * @return
     */
    String jiraTicket();

}