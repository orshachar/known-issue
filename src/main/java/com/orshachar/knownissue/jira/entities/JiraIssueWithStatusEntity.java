package com.orshachar.knownissue.jira.entities;

/**
 * Created by Or on 17/12/17.
 */
public class JiraIssueWithStatusEntity {
    private String expand;
    private String id;
    private String self;
    private String key;
    private JiraStatusFieldEntity fields;

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

    public JiraStatusFieldEntity getFields() {
        return fields;
    }

    public void setFields(JiraStatusFieldEntity fields) {
        this.fields = fields;
    }
}
