package com.example.christ.musicplayer;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.christ.musicplayer.RecorderUtil.AudioFunction;
import com.example.christ.musicplayer.RecorderUtil.CommonFunction;
import com.example.christ.musicplayer.RecorderUtil.ComposeAudioInterface;
import com.example.christ.musicplayer.RecorderUtil.Constant;
import com.example.christ.musicplayer.RecorderUtil.DecodeOperateInterface;
import com.example.christ.musicplayer.RecorderUtil.FileFunction;
import com.example.christ.musicplayer.RecorderUtil.Variable;
import com.example.christ.musicplayer.RecorderUtil.VoiceFunction;
import com.example.christ.musicplayer.RecorderUtil.VoiceRecorderOperateInterface;


public class TestActivity extends AppCompatActivity
        implements VoiceRecorderOperateInterface, DecodeOperateInterface, ComposeAudioInterface {

    private ImageView mic_on;  // 麦克风

    private static final String TAG = "RecordActivity";

    private static TestActivity instance;
    private boolean recordVoiceBegin;
    private int recordTime;
    private int actualRecordTime;

    private MusicInfo musicInfo;
    private String recordFileUrl;
    private String decodeFileUrl;
    private String composeVoiceFileUrl;
    private String musicFileUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 创建文件夹
        FileFunction.InitStorage(TestActivity.this);
        recordVoiceBegin = false;
        instance = this;

        // 获取音频信息
        Intent intent = getIntent();
        musicInfo = (MusicInfo) intent.getSerializableExtra("musicInfo");
        ImageView album_art = (ImageView) findViewById(R.id.album_art);
        album_art.setImageBitmap(MediaUtil.getAlbumArt(TestActivity.this, musicInfo.getAlbum_id()));

        recordFileUrl = Variable.StorageDirectoryPath + musicInfo.getTitle() + "-record.pcm";
        musicFileUrl = musicInfo.getUrl();
        decodeFileUrl = Variable.StorageDirectoryPath + musicInfo.getTitle() + "-decode.pcm";
        // !!!!!!!!!!!!!!!!
        composeVoiceFileUrl = Variable.StorageDirectoryPath + musicInfo.getTitle() + "-record.mp3";


        // 录音
        mic_on = (ImageView) findViewById(R.id.mic_on);
        mic_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("recordVoiceBegin", String.valueOf(recordVoiceBegin));
                if(recordVoiceBegin){
                    // 停止录音
                    stopRecoding();
                    mic_on.setImageResource(R.drawable.baseline_mic_white_18dp);
                    // 解码
                    AudioFunction.DecodeMusicFile(musicFileUrl, decodeFileUrl, 0,
                            actualRecordTime + Constant.MusicCutEndOffset, instance);
                } else {
                    // 开始录音
                    record();
                    mic_on.setImageResource(R.drawable.baseline_mic_off_white_18dp);
                }
            }
        });

    }

    // 录音功能
    private void record(){
        VoiceFunction.StartRecordVoice(TestActivity.this,
                recordFileUrl, instance);

    }
    // 停止录音
    private void stopRecoding(){
//        mAudioRecorder.stopRecord();
        VoiceFunction.StopRecordVoice(TestActivity.this);

    }

    // --------------------------- VoiceRecorderOperateInterface---------------------------
    @Override
    public void recordVoiceBegin() {
//        VoiceFunction.StopVoice();

        if (!recordVoiceBegin) {
            recordVoiceBegin = true;

            recordTime = 0;
            Toast.makeText(TestActivity.this, "录音开始", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void recordVoiceStateChanged(int volume, long recordDuration) {
        if (recordDuration > 0) {
            recordTime = (int) (recordDuration / Constant.OneSecond);
        }
    }

    @Override
    public void prepareGiveUpRecordVoice() {
    }

    @Override
    public void recoverRecordVoice() {
    }

    @Override
    public void giveUpRecordVoice() {
    }

    @Override
    public void recordVoiceFail() {
        if (recordVoiceBegin) {
            if (actualRecordTime != 0) {
                goRecordSuccessState();
            } else {
                goRecordFailState();
            }
            Toast.makeText(TestActivity.this, "录音失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void recordVoiceFinish() {
        if (recordVoiceBegin) {
            actualRecordTime = recordTime;

            goRecordSuccessState();
            Toast.makeText(TestActivity.this, "录音结束", Toast.LENGTH_SHORT).show();
        }
    }

    private void goRecordSuccessState() {
        recordVoiceBegin = false;
    }

    private void goRecordFailState() {
        recordVoiceBegin = false;
    }

    // --------------------------- DecodeOperateInterface---------------------------
    @Override
    public void updateDecodeProgress(int decodeProgress) {
//        composeProgressBar.setProgress(
//                decodeProgress * Constant.MaxDecodeProgress / Constant.NormalMaxProgress);
    }

    @Override
    public void decodeSuccess() {
//        composeProgressBar.setProgress(Constant.MaxDecodeProgress);

        Toast.makeText(TestActivity.this, "解码成功",
                Toast.LENGTH_SHORT).show();
        // 开始合成
        AudioFunction.BeginComposeAudio(TestActivity.this,
                recordFileUrl, decodeFileUrl, composeVoiceFileUrl,
                false, Constant.VoiceWeight, Constant.VoiceBackgroundWeight,
                -1 * Constant.MusicCutEndOffset / 2 * Constant.RecordDataNumberInOneSecond,
                instance);

    }

    @Override
    public void decodeFail() {
        Toast.makeText(TestActivity.this, "解码失败,请您检查网络后，再次尝试",
                Toast.LENGTH_SHORT).show();

    }

    // --------------------------- ComposeAudioInterface---------------------------
    @Override
    public void updateComposeProgress(int composeProgress) {
//        composeProgressBar.setProgress(
//                composeProgress * (Constant.NormalMaxProgress - Constant.MaxDecodeProgress) /
//                        Constant.NormalMaxProgress + Constant.MaxDecodeProgress);
    }

    @Override
    public void composeSuccess() {
        Toast.makeText(TestActivity.this, "合成成功，可播放合成语音",
                Toast.LENGTH_SHORT).show();

        // 删除pcm
        FileFunction.DeleteFile(TestActivity.this, recordFileUrl);
        FileFunction.DeleteFile(TestActivity.this, decodeFileUrl);
        // 添加到系统数据库
//        MediaUtil.insertMusic(TestActivity.this.getContentResolver(),
//                new MusicInfo());
    }

    @Override
    public void composeFail() {
        Toast.makeText(TestActivity.this, "合成失败",
                Toast.LENGTH_SHORT).show();
    }
}
