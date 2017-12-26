package com.orshachar.knownissue.jira.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Created by Or on 17/12/17.
 */
public enum JiraStatusEnum {
    IN_TRIAGE("In Triage"),
    OPEN("Open"),
    TO_DO("To Do"),
    CLOSED("Closed"),
    IN_PROGRESS("In Progress"),
    IN_DEVELOPMENT("In Development"),
    READY_FOR_REVIEW("Ready For Review"),
    RESOLVED("Resolved"),
    REOPENED("Reopened");



    private String status;
    JiraStatusEnum(String inStatus) { status = inStatus; }
    public String getStatus() { return status; }

    @JsonCreator
    public static JiraStatusEnum forValue(String value) {
        for (JiraStatusEnum status :  JiraStatusEnum.values()) {
            if (status.getStatus().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException(value);
    }
}
