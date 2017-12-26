package com.orshachar.knownissue.jira.listner;

import com.orshachar.knownissue.jira.entities.JiraIssueEntity;
import com.orshachar.knownissue.jira.entities.JiraStatusEnum;
import com.orshachar.knownissue.jira.utils.JiraUtils;
import com.orshachar.knownissue.testng.annotations.KnownIssue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.slf4j.LoggerFactory;
import org.testng.*;
import org.testng.internal.ConstructorOrMethod;


/**
 * Created by Or on 17/12/17.
 */
public class JiraListener implements ITestListener {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JiraListener.class);
    Properties properties = new Properties();


    private void getJiraProperties()  {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("jira.properties").getFile());
        FileInputStream fileInput = null;
        try {
            fileInput = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error("Could not find properties file in resources folder");
            e.printStackTrace();
        }

        try {
            properties.load(fileInput);
        } catch (IOException e) {
            logger.error("Properties file is not valid");
            e.printStackTrace();
        }
        try {
            fileInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onTestStart(ITestResult result) {
        // Do nothing
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ITestNGMethod testNgMethod = result.getMethod();
        ConstructorOrMethod constructorOrMethod = testNgMethod.getConstructorOrMethod();
        Method method = constructorOrMethod.getMethod();

        if (method == null || (!method.isAnnotationPresent(KnownIssue.class))) {
            return;
        }
        //Known-issue annotation present

        // Get jira properties
        getJiraProperties();
        JiraUtils jiraUtils = new JiraUtils(properties.getProperty("jiraUrl"), properties.getProperty("jiraUser"), properties.getProperty("jiraPassword"));
        String jiraTicket = method.getAnnotation(KnownIssue.class).jiraTicket();

        JiraIssueEntity issue = jiraUtils.getIssue(jiraTicket);
        JiraStatusEnum issueStatus = issue.getFields().getStatus().getName();

        if (issueStatus == null) {
            logger.error("Test passed but there is a Jira ticket - " + jiraTicket + " which could not be found on Jira." +
                    " Please remove the annotation from the test.");
            Exception exception = new Exception("Test passed but there is a Jira ticket - " + jiraTicket + " which could not be found on Jira." +
                    " Please remove the annotation from the test.", result.getThrowable());
            result.setThrowable(exception);
        } else {
            logger.error("Test passed but there is a Jira ticket - " + jiraTicket + " that is marked as \"" +
                    issueStatus.getStatus() + "\". Please close the bug and remove the annotation from the test.");
            Exception exception = new Exception("Test passed but there is a Jira ticket - " + jiraTicket + " that is marked as \"" +
                    issueStatus.getStatus() + "\". Please close the bug and remove the annotation from the test.", result.getThrowable());
            result.setThrowable(exception);
        }
        result.setStatus(ITestResult.FAILURE);

    }

    @Override
    public void onTestFailure(ITestResult result) {
        ITestNGMethod testNgMethod = result.getMethod();
        ConstructorOrMethod constructorOrMethod = testNgMethod.getConstructorOrMethod();
        Method method = constructorOrMethod.getMethod();

        if (method == null || (!method.isAnnotationPresent(KnownIssue.class))) {
            return;
        }

        getJiraProperties();
        JiraUtils jiraUtils = new JiraUtils(properties.getProperty("jira.url"), properties.getProperty("jira.user"), properties.getProperty("jira.password"));
        String jiraTicket = method.getAnnotation(KnownIssue.class).jiraTicket();

        //Known-issue annotation present
        if (method.isAnnotationPresent(KnownIssue.class)) {

            JiraIssueEntity issue = jiraUtils.getIssue(jiraTicket);

            if (issue == null) {
                logger.error("Issue Number: " + jiraTicket + " was not found,check the @KnownIssue annotation");
                return;
            }
//            JiraStatusEnum issueStatus = jiraUtils.getIssueStatus(jiraTicket);
            JiraStatusEnum issueStatus = issue.getFields().getStatus().getName();
            if (issueStatus == null) {
                return;
            }
            //ticket is closed or resolved
            if ((issueStatus.equals(JiraStatusEnum.CLOSED) || issueStatus.equals(JiraStatusEnum.RESOLVED))) {
                logger.error("Test failed but Jira ticket - " + jiraTicket + " is marked as \"" + issueStatus.getStatus() + "\". Please re-open the bug.");
                Exception exception = new Exception("Test failed but Jira ticket - " + jiraTicket + " is marked as \""
                        + issueStatus.getStatus() + "\". Please re-open the bug.", result.getThrowable());
                result.setThrowable(exception);
            } else {
                logger.info("There is a nonAssignee().getDisplayName()); critical Jira issue - " + jiraTicket + " in  \"" + issueStatus.getStatus() + "\" status on this test.");
                //logger.info("Assign to: " + issue.getFields().getAssignee().getDisplayName());
                result.setStatus(ITestResult.SKIP);

                SkipException exception = new SkipException("There is a non critical Jira issue - " + jiraTicket +  " in  \""
                        + issueStatus.getStatus() + "\" status on this test.");
                result.setThrowable(exception);
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // Do nothing

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Do nothing
    }

    @Override
    public void onStart(ITestContext context) {
        // Do nothing
    }

    @Override
    public void onFinish(ITestContext context) {
    }

}
