package com.example.christ.musicplayer;

import java.io.Serializable;

/**
 * Created by christ on 2018/5/17.
 */

public class MusicInfo implements Serializable {
    private long id;
    private String title;  // 标题
    private String album;  // 专辑
    private int duration;  // 时长
    private long size;  // 大小
    private String artist;  // 歌手
    private String url;  // 地址
    private int album_id; // 专辑id

    public MusicInfo(){
        title = album = artist = url = "";
        size = id = duration = album_id = 0;
    }

    public MusicInfo(long id, String title, String album, int duration, String artist, String url){
        this.id = id;
        this.title = title;
        this.album = album;
        this.duration = duration;
        this.artist = artist;
        this.url = url;
    }
    public MusicInfo(long id, String title, String album, int album_id, int duration, String artist, String url){
        this.id = id;
        this.title = title;
        this.album = album;
        this.duration = duration;
        this.artist = artist;
        this.url = url;
        this.album_id = album_id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getAlbum_id(){
        return album_id;
    }
    public void setAlbum_id(int album_id){
        this.album_id = album_id;
    }

}
