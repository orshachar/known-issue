package com.orshachar.knownissue.jira.entities;

/**
 * Created by Or on 17/12/17.
 */
public class JiraStatusEntity {
    private String self;
    private String description;
    private String iconUrl;
    private JiraStatusEnum name;
    private int id;
    private JiraStatusCategoryEntity statusCategory;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public JiraStatusEnum getName() {
        return name;
    }

    public void setName(JiraStatusEnum name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JiraStatusCategoryEntity getStatusCategory() {
        return statusCategory;
    }

    public void setStatusCategory(JiraStatusCategoryEntity statusCategory) {
        this.statusCategory = statusCategory;
    }
}
