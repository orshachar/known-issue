package com.orshachar.knownissue.testng.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Or on 17/11/2017.
 */
@Retention(RUNTIME)
public @interface KnownIssue {
    String jiraTicket();
}
