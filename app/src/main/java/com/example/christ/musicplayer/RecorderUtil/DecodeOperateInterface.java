package com.example.christ.musicplayer.RecorderUtil;

/**
 * Created by christ on 2018/5/23.
 */

public interface DecodeOperateInterface {
    public void updateDecodeProgress(int decodeProgress);

    public void decodeSuccess();

    public void decodeFail();
}