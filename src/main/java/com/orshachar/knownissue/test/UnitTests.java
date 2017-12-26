package com.orshachar.knownissue.test;

import com.orshachar.knownissue.jira.listner.JiraListener;
import com.orshachar.knownissue.testng.annotations.KnownIssue;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Listeners(JiraListener.class)
public class UnitTests {




    @KnownIssue(jiraTicket = "KNOW-1")
    @Test
    public void testFailedWithOpenIssue() {
        assertThat(1, equalTo(2));

    }

    @KnownIssue(jiraTicket = "123")
    @Test
    public void testWrongIssue() {

        assertThat(1, equalTo(2));

    }
}
