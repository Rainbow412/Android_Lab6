package com.example.rainbow.lab6;

import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class MusicService extends Service{

    private boolean flag = true; //图片旋转需要start还是resume
    public final IBinder binder = new MyBinder();
    public class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent){
        return binder;
    }
    @Override
    public void onCreate(){
        super.onCreate();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mp != null){
            mp.stop();
            mp.release();
        }
    }

    public static MediaPlayer mp = new MediaPlayer();
    public MusicService(){
        try{
            //mp.setDataSource("/data/K.Will-Melt.mp3");
            mp.setDataSource("/storage/emulated/0/K.Will-Melt.mp3");
            mp.prepare();
            mp.setLooping(true);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void play(Button b, TextView t, ObjectAnimator animator){
        if(mp.isPlaying()){
            mp.pause();
            b.setText("play");
            t.setText("Paused");
            animator.pause();
        } else{
            mp.start();
            b.setText("pause");
            t.setText("Playing");
            if(flag==true){
                animator.start();
                flag = false;
            }
            else
                animator.resume();
        }
    }
    public void stop(){
        if(mp != null){
            mp.stop();
            flag = true;
            try {
                mp.prepare();
                mp.seekTo(0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }



}
