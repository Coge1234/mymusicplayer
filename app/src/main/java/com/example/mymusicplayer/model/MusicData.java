package com.example.mymusicplayer.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/3/1.
 */

public class MusicData implements Serializable{
    //音乐的资源Id
    private int mMusicRes;
    //专辑图片Id
    private int mMusicPicRes;
    //音乐名称
    private String mMusicName;
    //歌手名字
    private String mMusicSinger;

    public MusicData(int musicRes, int musicPicRes, String musicName, String musicSinger) {
        mMusicRes = musicRes;
        mMusicPicRes = musicPicRes;
        mMusicName = musicName;
        mMusicSinger = musicSinger;
    }

    public int getMusicRes() {
        return mMusicRes;
    }

    public int getMusicPicRes() {
        return mMusicPicRes;
    }

    public String getMusicName() {
        return mMusicName;
    }

    public String getMusicSinger() {
        return mMusicSinger;
    }
}
