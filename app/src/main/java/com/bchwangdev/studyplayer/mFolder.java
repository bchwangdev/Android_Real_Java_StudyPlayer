package com.bchwangdev.studyplayer;

public class mFolder {
    private int folderInSongCount;
    private String folderName;
    private String folderPath;
    private boolean folderChecked;

    public mFolder(int folderInSongCount, String folderName, String folderPath) {
        this.folderInSongCount = folderInSongCount;
        this.folderName = folderName;
        this.folderPath = folderPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public boolean isFolderChecked() {
        return folderChecked;
    }

    public void setFolderChecked(boolean folderChecked) {
        this.folderChecked = folderChecked;
    }

    public int getFolderInSongCount() {
        return folderInSongCount;
    }

    public void setFolderInSongCount(int folderInSongCount) {
        this.folderInSongCount = folderInSongCount;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}
