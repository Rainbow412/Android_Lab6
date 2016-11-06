package com.example.rainbow.lab6;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;


public class MusicService extends Service{


    public final IBinder binder = new MyBinder();
    public class MyBinder extends Binder{
        @Override
        protected  boolean onTransact(int code, Parcel data,
                                      Parcel reply, int flags)
                throws RemoteException{
            switch (code){
                case 100:
                    play();
                    break;
                case 101:
                    pause();
                    break;
                case 102:
                    stop();
                    break;
                case 103:
                    if(mp!=null){
                        int duration = mp.getDuration();
                        reply.writeInt(duration);
                    }
                    break;
                case 104:
                    if(mp!=null) {
                        int currentPosition = mp.getCurrentPosition();
                        reply.writeInt(currentPosition);
                    }
                    break;
                case 105:
                    int i = data.readInt();
                    mp.seekTo(i);
                    break;
                case 106:
                    int playing = mp.isPlaying()?1:0;
                    Log.d("playing", Integer.toString(playing));
                    reply.writeInt(playing);
                    break;
            }
            return super.onTransact(code, data, reply, flags);
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
    public void onDestroy(){ super.onDestroy(); }

    private static MediaPlayer mp = new MediaPlayer();
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

    private void play(){
         if(!mp.isPlaying()){
            mp.start();
        }
    }

    private void pause(){
        if(mp.isPlaying()){
            mp.pause();
        }
    }
    private void stop(){
        if(mp != null){
            mp.stop();
//            flag = true;
            try {
                mp.prepare();
                mp.seekTo(0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }



}
