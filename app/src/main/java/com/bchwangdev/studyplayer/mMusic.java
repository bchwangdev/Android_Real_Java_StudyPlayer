package com.bchwangdev.studyplayer;

public class mMusic {
    private String musicNo;
    private String musicName;

    public mMusic(String musicNo, String songName) {
        this.musicNo = musicNo;
        this.musicName = songName;
    }

    public String getMusicNo() {
        return musicNo;
    }

    public void setMusicNo(String musicNo) {
        this.musicNo = musicNo;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }


}
