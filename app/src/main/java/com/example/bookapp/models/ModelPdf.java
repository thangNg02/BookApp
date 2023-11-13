package com.example.bookapp.models;

public class ModelPdf {
    String uid, id, title, description, categoryId, url;
    long timestamp, downloadsCount, viewsCount;

    public ModelPdf() {

    }
    //constructor
    public ModelPdf(String uid, String id, String title, String description, String categoryId, String url, long timestamp, long downloadsCount, long viewsCount) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.url = url;
        this.timestamp = timestamp;
        this.downloadsCount = downloadsCount;
        this.viewsCount = viewsCount;
    }

    //getter và setter
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getDownloadsCount() {
        return downloadsCount;
    }

    public void setDownloadsCount(long downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }
}
