package com.example.rainbow.lab6;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private Button play_pause_button, stop_button,quit_button;
    private TextView state, time1, time2;
    private ImageView imageAlbum;
    //private MusicService ms;
    private SeekBar seekBar;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private boolean isChanging=false;//互斥变量，防止handler与SeekBar拖动时进度冲突
    private boolean play_pause=false; //F:play T:pause
    private boolean isPlaying = false;
    private ObjectAnimator animator;
    private boolean flag = true; //图片旋转需要start还是resume
    private int code,duration,currentPosition;
    private Parcel data, reply;
    private Message msg;
    private IBinder mBinder;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //ms = ((MusicService.MyBinder)service).getService();
            mBinder = service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //ms = null;
            mBinder = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView(); //找到所需组件
        bindButton(); //button组件绑定监听器
        animatorInit(); //设置图片旋转的参数
        connection(); //连接MusicService
        seekbarChange(); //设置seekbar被拖动的操作
    }

    final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                //更新currentPosition
                case 200:
                    if(mBinder!=null){
                        code = 104;
                        data = Parcel.obtain();
                        reply = Parcel.obtain();
                        try{
                            mBinder.transact(code, data, reply, 0);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        currentPosition = reply.readInt();
                    }
                    break;
                //获取duration
                case 201:
                    if(mBinder!=null){
                        code = 103;
                        data = Parcel.obtain();
                        reply = Parcel.obtain();
                        try{
                            mBinder.transact(code, data, reply, 0);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        duration = reply.readInt();
                    }
                    break;
                //play
                case 202:
                    if(mBinder!=null){
                        code = 100;
                        data = Parcel.obtain();
                        reply = Parcel.obtain();
                        try{
                            mBinder.transact(code, data, reply, 0);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                    }
                    break;
                //pause
                case 203:
                    if(mBinder!=null){
                        code = 101;
                        data = Parcel.obtain();
                        reply = Parcel.obtain();
                        try{
                            mBinder.transact(code, data, reply, 0);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                    }
                    break;
                //stop
                case 204:
                    if(mBinder!=null){
                        code = 102;
                        data = Parcel.obtain();
                        reply = Parcel.obtain();
                        try{
                            mBinder.transact(code, data, reply, 0);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                    }
                    break;
                //seekto
                case 205:
                    if(mBinder!=null){
                        code = 105;
                        data = Parcel.obtain();
                        reply = Parcel.obtain();
                        data.writeInt(seekBar.getProgress());
                        try{
                            mBinder.transact(code, data, reply, 0);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                    }
                    break;
                //playing or not
                case 206:
                    if(mBinder!=null){
                        code = 106;
                        data = Parcel.obtain();
                        reply = Parcel.obtain();
                        try{
                            mBinder.transact(code, data, reply, 0);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        isPlaying = reply.readInt()==1;
                        Log.d("isplay1", isPlaying==true?"t":"f");
                    }
                    break;
            }
        }
    };
    int t = 0;
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(t<3||t==4) t++;
            //绑定需要一定时间，经过调试发现第3次runnable才成功绑定
            if(t==3){
                Log.d("mBinder==null", mBinder==null?"y":"n");
                msg = new Message();
                msg.what = 206; //isPlaying?
                mHandler.sendMessage(msg);
                t++;
            }
            //接收是否播放的结果需要一定时间，经过调试发现第5次runnable才接收成功
            if(t==5){
                //打开Activity要判断是否有音乐正在播放
                Log.d("isplay2", isPlaying==true?"t":"f");
                if(isPlaying==true){
                    animator.start(); //图片旋转
                    state.setText("Playing");
                    play_pause_button.setText("pause");
                    flag=!flag;
                    play_pause=!play_pause;
                }
                //打开Activity判断是否是音乐被暂停了
                else if(currentPosition>0)
                    state.setText("Paused");
                t++;
            }
            //定时更新currentPosition
            if(isChanging==false){
                if(mBinder!=null){
                        msg = new Message();
                        msg.what = 200; //更新currentPosition
                        mHandler.sendMessage(msg);
                        seekBar.setProgress(currentPosition); //currentPosition

                        msg = new Message();
                        msg.what = 201; //更新duration
                        mHandler.sendMessage(msg);
                        seekBar.setMax(duration);

                        time1.setText(time.format(currentPosition));
                        time2.setText(time.format(duration-currentPosition));
                }
            }
            mHandler.postDelayed(mRunnable, 100); //暂停100毫秒
        }
    };

    private void findView(){
        play_pause_button = (Button)findViewById(R.id.play_pause);
        stop_button = (Button)findViewById(R.id.stop);
        quit_button = (Button)findViewById(R.id.quit);
        state = (TextView)findViewById(R.id.state);
        time1 = (TextView)findViewById(R.id.time1);
        time2 = (TextView)findViewById(R.id.time2);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        imageAlbum = (ImageView)findViewById(R.id.image);
    }
    private void bindButton(){
        play_pause_button.setOnClickListener(this);
        stop_button.setOnClickListener(this);
        quit_button.setOnClickListener(this);
    }
    private void animatorInit(){
        animator = ObjectAnimator.ofFloat(imageAlbum, "rotation", 0, 360); //0-360度旋转
        animator.setDuration(10000); //转速
        animator.setRepeatCount(ObjectAnimator.INFINITE); //无限次重复
        animator.setRepeatMode(ObjectAnimator.RESTART); //当单词动画播放完成后，重新播放
        animator.setInterpolator(new LinearInterpolator()); //匀速
    }
    private void connection(){
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, sc, BIND_AUTO_CREATE); //绑定MusicService
        mHandler.post(mRunnable); //runnable开始运作

    }
    private void seekbarChange(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public  void onProgressChanged(SeekBar arg0, int arg1, boolean arg2){
                //进度条拖动的过程中
                //开始时间和结束时间会随着进度条拖动而改变
                time1.setText(time.format(seekBar.getProgress()));
                time2.setText(time.format(duration-seekBar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar arg0){
                //开始拖动进度条
                isChanging=true;
            }
            @Override
            public void onStopTrackingTouch(SeekBar arg0){
                //停止拖动进度条
                isChanging=false;

                msg = new Message();
                msg.what = 205; //seekto
                mHandler.sendMessage(msg);

                msg = new Message();
                msg.what = 200; //更新currentPosition
                mHandler.sendMessage(msg);

                time1.setText(time.format(currentPosition));
                time2.setText(time.format(duration-currentPosition));
            }
        });
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.play_pause:
                //此时音乐没有在播放
                if(play_pause==false){
                    msg = new Message();
                    msg.what = 202; //play
                    mHandler.sendMessage(msg);

                    if(flag==true){
                        animator.start(); //首次播放时，图片开始旋转
                        flag=false;
                    }
                    else
                        animator.resume(); //暂停后播放，图片继续旋转

                    state.setText("Playing");
                    play_pause_button.setText("pause");
                    play_pause=true;
                }
                //此时音乐正在播放
                else{
                    msg = new Message();
                    msg.what = 203; //pause
                    mHandler.sendMessage(msg);

                    animator.pause(); //图片暂停旋转
                    play_pause_button.setText("play");
                    state.setText("Paused");
                    play_pause=false;
                }
                break;
            case R.id.stop:
                msg = new Message();
                msg.what = 204; //stop
                mHandler.sendMessage(msg);

                seekBar.setProgress(0);
                currentPosition=0;
                animator.cancel(); //图片取消旋转
                play_pause_button.setText("Play");
                state.setText("Stopped");
                play_pause=false;
                flag=true;
                break;
            case R.id.quit:
                mHandler.removeCallbacks(mRunnable); //移除runnable
                unbindService(sc); //解除绑定MusicService
                try{
                    MainActivity.this.finish(); //结束MainActivity
                    System.exit(0);
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }
}
