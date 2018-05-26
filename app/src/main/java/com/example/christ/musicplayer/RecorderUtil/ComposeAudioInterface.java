package com.example.christ.musicplayer.RecorderUtil;

/**
 * Created by christ on 2018/5/23.
 */

public interface ComposeAudioInterface {
    public void updateComposeProgress(int composeProgress);

    public void composeSuccess();

    public void composeFail();
}