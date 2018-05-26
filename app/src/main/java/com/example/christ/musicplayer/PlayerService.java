package com.example.christ.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerService extends Service {
    private MediaPlayer mp;
    public IBinder iBinder = new mBinder();
    //  private AudioRecordFunc recorder = AudioRecordFunc.getInstance();


    public class mBinder extends Binder  implements Serializable {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException{
            switch (code){
                case 100:
                    if(mp.isPlaying())
                        reply.writeInt(1);
                    else
                        reply.writeInt(0);
                    break;
                case 101: //播放
                    if(mp.isPlaying()){
                        mp.pause();
                        //    recorder.stopRecordAndFile();
                    }
                    else{
                        mp.start();
                        reply.writeInt(mp.getCurrentPosition());
                    }
                    break;
                case 102:
                    try{
                        String url = data.readString();
                        if(url!=null){
                            if(mp!=null){
                                mp.stop();
                                mp.release();
                            }
                            mp = new MediaPlayer();
                            mp.setDataSource(url);
                            mp.prepare();
                            mp.start();
                            //    recorder.startRecordAndFile();
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    break;
                case 103: //退出
                    mp.stop();
                    break;
                case 104: //获取当前播放位置
                    if(mp!=null){
                        reply.writeInt(mp.getCurrentPosition());
                        reply.writeInt(mp.getDuration());
                    }
                    break;
                case 105: //进度条拖动
                    mp.seekTo(data.readInt());
                    break;
            }
            return super.onTransact(code,data,reply,flags);
        }
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        try{
            if(mp==null)
                mp = new MediaPlayer();
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        try{
            mp = new MediaPlayer();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){
        mp.stop();
        mp.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


}
