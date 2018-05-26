package com.example.christ.musicplayer;

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.christ.musicplayer.RecorderUtil.AudioFunction;
import com.example.christ.musicplayer.RecorderUtil.ComposeAudioInterface;
import com.example.christ.musicplayer.RecorderUtil.Constant;
import com.example.christ.musicplayer.RecorderUtil.DecodeOperateInterface;
import com.example.christ.musicplayer.RecorderUtil.FileFunction;
import com.example.christ.musicplayer.RecorderUtil.Variable;
import com.example.christ.musicplayer.RecorderUtil.VoiceFunction;
import com.example.christ.musicplayer.RecorderUtil.VoiceRecorderOperateInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class PlayAndRecord extends AppCompatActivity
        implements VoiceRecorderOperateInterface, DecodeOperateInterface, ComposeAudioInterface {
    private ImageView image;
    private SeekBar seekBar;
    private Button play;
    private Button record;
    private TextView song;
    private ServiceConnection conn;
    private IBinder mBinder;
    private static Handler mHandler;
    private TextView current;
    private TextView length;
    private TextView status;
//    private boolean isPlay;
    private java.text.SimpleDateFormat time = new java.text.SimpleDateFormat("mm:ss");


    private static PlayAndRecord instance;
    private boolean recordVoiceBegin;
    private int recordTime;
    private int actualRecordTime;

    private MusicInfo musicInfo;
    private String recordFileUrl;
    private String decodeFileUrl;
    private String composeVoiceFileUrl;
    private String musicFileUrl;

    private String artist = null;
    private int artist_id;

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMessageEvent(User user){
        // 获取用户
        artist = user.getUsername();
        artist_id = user.getId();
        Log.e("accept artist", artist);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_and_record);

        // 注册订阅者
        EventBus.getDefault().register(this);

        image = (ImageView) findViewById(R.id.pic);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        play = (Button) findViewById(R.id.playing);
        record = (Button) findViewById(R.id.recording);
        song = (TextView) findViewById(R.id.song);
        current = (TextView) findViewById(R.id.current);
        length = (TextView) findViewById(R.id.length);
        status = (TextView) findViewById(R.id.status);

        // 创建文件夹
        FileFunction.InitStorage(PlayAndRecord.this);
        recordVoiceBegin = false;
        instance = this;

        Intent intent = getIntent();
        musicInfo = (MusicInfo) intent.getSerializableExtra("music");
        song.setText(musicInfo.getTitle() +" @ "+ musicInfo.getArtist());
        image.setImageBitmap(MediaUtil.getAlbumArt(this, musicInfo.getAlbum_id()));
        mBinder = (IBinder)intent.getSerializableExtra("binder");//绑定播放器服务
    //    isPlay = intent.getBooleanExtra("isPlay", true);
        final Parcel data = Parcel.obtain();
        try{
            mBinder.transact(100,Parcel.obtain(),data,0);
        }catch (RemoteException e){
            e.printStackTrace();
        }
        if(data.readInt()==1){
            status.setText("播放中");
            play.setText("暂停");
            record.setText("录音");
        } else {
            status.setText("暂停中");
            play.setText("播放");
            record.setText("录音");
        }

        recordFileUrl = Variable.StorageDirectoryPath + musicInfo.getTitle() + "-record.pcm";
        musicFileUrl = musicInfo.getUrl();
        decodeFileUrl = Variable.StorageDirectoryPath + musicInfo.getTitle() + "-decode.pcm";

        Log.e("play", "create");

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!recordVoiceBegin){  // 未进行录音
                    Parcel reply = Parcel.obtain();
                    Parcel data = Parcel.obtain();
                    try{
                        // 播放或暂停
                        mBinder.transact(101,Parcel.obtain(),reply,0);
                        mBinder.transact(100,Parcel.obtain(),data,0);
                        current.setText(time.format(reply.readInt()));
                    }catch(RemoteException e){
                        e.printStackTrace();
                    }

                    if(data.readInt()==0){
                        status.setText("暂停中");
                        play.setText("播放");
                    } else {
                        status.setText("播放中");
                        play.setText("暂停");
                    }
                //   isPlay = !isPlay;
                }
            }
        });
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("artist", artist == null ? "null" : artist);
                if(artist == null){
                    Toast.makeText(PlayAndRecord.this, "请先登录", Toast.LENGTH_SHORT).show();
                } else {
                    composeVoiceFileUrl = Variable.StorageDirectoryPath + musicInfo.getTitle()
                            + "-record" + artist_id + ".mp3";
                    if(recordVoiceBegin){  // 非录音状态
                        try{
                            // 播放或暂停
                            Parcel reply = Parcel.obtain();
                            mBinder.transact(101,Parcel.obtain(),reply,0);
                            current.setText(time.format(reply.readInt()));
                        }catch(RemoteException e){
                            e.printStackTrace();
                        }
                        status.setText("暂停中");
                        play.setText("播放");
                        record.setText("录音");
                        play.setClickable(true);
                    //    isPlay = false;
                        seekBar.setEnabled(true);

                        // 停止录音
                        stopRecoding();
                        // 解码
                        AudioFunction.DecodeMusicFile(musicFileUrl, decodeFileUrl, 0,
                                actualRecordTime + Constant.MusicCutEndOffset, instance);
                    } else {  // 录音状态
                        // 从头播放
                        seekBar.setProgress(0);
                        try{
                            Parcel data = Parcel.obtain();
                            data.writeInt(0);
                            mBinder.transact(105,data,Parcel.obtain(),0);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        status.setText("录音中");
                        record.setText("结束");
                        play.setText("禁用");
                        play.setClickable(false);
                    //    isPlay = true;
                        seekBar.setEnabled(false);

                        // 开始录音
                        record();
                    }
                }
            }
        });
        //更新UI
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 123) {
                    if(mBinder != null) {
                        try {
                            Parcel reply = Parcel.obtain();
                            mBinder.transact(104, Parcel.obtain(), reply, 0);
                            int location = reply.readInt();
                            seekBar.setProgress(location);
                            current.setText(time.format(location));
                            int max = reply.readInt();
                            seekBar.setMax(max);
                            length.setText(time.format(max));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        //创建子线程，在子线程中处理耗时工作
        Thread mThread = new Thread() {
            @Override
            public void run() {
                while (true){
                    try{
                        Thread.sleep(100);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    mHandler.obtainMessage(123).sendToTarget();
                }
            }
        };
        mThread.start();

        //进度条
        seekBar.setEnabled(true);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    try{
                        Parcel data = Parcel.obtain();
                        data.writeInt(progress);
                        mBinder.transact(105,data,Parcel.obtain(),0);
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }



    // 录音功能
    private void record(){
        VoiceFunction.StartRecordVoice(PlayAndRecord.this,
                recordFileUrl, instance);

    }
    // 停止录音
    private void stopRecoding(){
//        mAudioRecorder.stopRecord();
        VoiceFunction.StopRecordVoice(PlayAndRecord.this);

    }

    // --------------------------- VoiceRecorderOperateInterface---------------------------
    @Override
    public void recordVoiceBegin() {
//        VoiceFunction.StopVoice();

        if (!recordVoiceBegin) {
            recordVoiceBegin = true;

            recordTime = 0;
            Toast.makeText(PlayAndRecord.this, "录音开始", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(PlayAndRecord.this, "录音失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void recordVoiceFinish() {
        if (recordVoiceBegin) {
            actualRecordTime = recordTime;

            goRecordSuccessState();
            Toast.makeText(PlayAndRecord.this, "录音结束", Toast.LENGTH_SHORT).show();

            status.setText("暂停中");
            play.setText("播放");
            record.setText("录音");
            play.setClickable(true);
        //    isPlay = false;
            seekBar.setEnabled(true);

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

       // Toast.makeText(PlayAndRecord.this, "解码成功", Toast.LENGTH_SHORT).show();
        // 开始合成
        AudioFunction.BeginComposeAudio(PlayAndRecord.this,
                recordFileUrl, decodeFileUrl, composeVoiceFileUrl,
                false, Constant.VoiceWeight, Constant.VoiceBackgroundWeight,
                -1 * Constant.MusicCutEndOffset / 2 * Constant.RecordDataNumberInOneSecond,
                instance);

    }

    @Override
    public void decodeFail() {
        Toast.makeText(PlayAndRecord.this, "解码失败,请您检查网络后，再次尝试",
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
        Toast.makeText(PlayAndRecord.this, "录音已保存并上传，请刷新列表",
                Toast.LENGTH_SHORT).show();

        // 删除pcm
        FileFunction.DeleteFile(PlayAndRecord.this, recordFileUrl);
        FileFunction.DeleteFile(PlayAndRecord.this, decodeFileUrl);

        // 添加到系统数据库
        MediaUtil.updateMedia(PlayAndRecord.this, new String[]{composeVoiceFileUrl});
        getContentResolver().notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
    }

    @Override
    public void composeFail() {
        Toast.makeText(PlayAndRecord.this, "合成失败",
                Toast.LENGTH_SHORT).show();
    }
}
