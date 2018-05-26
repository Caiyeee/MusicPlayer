package com.example.christ.musicplayer.RecorderUtil;

/**
 * Created by christ on 2018/5/22.
 */

public interface VoiceRecorderOperateInterface {
    public void recordVoiceBegin();

    public void recordVoiceStateChanged(int volume, long recordDuration);

    public void prepareGiveUpRecordVoice();

    public void recoverRecordVoice();

    public void giveUpRecordVoice();

    public void recordVoiceFail();

    public void recordVoiceFinish();
}
