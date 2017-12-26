package com.orshachar.knownissue.jira.entities;

public class JiraIssueEntity {
    private String expand;
    private String id;
    private String self;
    private String key;
    private JiraFieldsEntity fields;

    public String getExpand() {
        return expand;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public JiraFieldsEntity getFields() {
        return fields;
    }

    public void setFields(JiraFieldsEntity fields) {
        this.fields = fields;
    }
}
