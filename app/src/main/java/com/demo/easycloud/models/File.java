package com.demo.easycloud.models;

public class File {
    String title;
    String fileType;
    String updatedOn;
    String uri;
    String path;

    boolean isFolder;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "File{" +
                "title='" + title + '\'' +
                ", fileType='" + fileType + '\'' +
                ", updatedOn='" + updatedOn + '\'' +
                ", uri='" + uri + '\'' +
                ", path='" + path + '\'' +
                ", isFolder=" + isFolder +
                '}';
    }
}
