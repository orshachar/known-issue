package com.orshachar.knownissue.jira.entities;

public class JiraFieldsEntity {
    private JiraStatusEntity status;
    private JiraCreatorEntity creator;
    private JiraAssigneeEntity assignee;
    private JiraReporterEntity reporter;
    private JiraProjectEntity project;
    private String summary;
    private String description;

    public JiraStatusEntity getStatus() {
        return status;
    }

    public void setStatus(JiraStatusEntity status) {
        this.status = status;
    }

    public JiraCreatorEntity getCreator() {
        return creator;
    }

    public void setCreator(JiraCreatorEntity creator) {
        this.creator = creator;
    }

    public JiraAssigneeEntity getAssignee() {
        return assignee;
    }

    public void setAssignee(JiraAssigneeEntity assignee) {
        this.assignee = assignee;
    }

    public JiraReporterEntity getReporter() {
        return reporter;
    }

    public void setReporter(JiraReporterEntity reporter) {
        this.reporter = reporter;
    }

    public JiraProjectEntity getProject() {
        return project;
    }

    public void setProject(JiraProjectEntity project) {
        this.project = project;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}