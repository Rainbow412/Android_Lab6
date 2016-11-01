package com.example.rainbow.lab6;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
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
    private MusicService ms;
    private SeekBar seekBar;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private boolean isChanging=false;//互斥变量，防止定时器与SeekBar拖动时进度冲突
    private ObjectAnimator animator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();
        bindButton();
        connection();

        animator = ObjectAnimator.ofFloat(imageAlbum, "rotation", 0, 360);
        animator.setDuration(10000);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setRepeatMode(ObjectAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public  void onProgressChanged(SeekBar arg0, int arg1, boolean arg2){
                time1.setText(time.format(seekBar.getProgress()));
                time2.setText(time.format(ms.mp.getDuration()-seekBar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar arg0){
                //开始拖动进度条
                isChanging=true;
            }
            @Override
            public void onStopTrackingTouch(SeekBar arg0){
                //停止拖动进度条
                ms.mp.seekTo(seekBar.getProgress());
                time1.setText(time.format(ms.mp.getCurrentPosition()));
                time2.setText(time.format(ms.mp.getDuration()-ms.mp.getCurrentPosition()));
                isChanging=false;
            }

        });




    }

    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(ms.mp != null){
                if(isChanging==false){
                    seekBar.setProgress(ms.mp.getCurrentPosition());
                    //Log.d("Progress",""+ms.mp.getCurrentPosition());
                    time1.setText(time.format(ms.mp.getCurrentPosition()));
                    time2.setText(time.format(ms.mp.getDuration()-ms.mp.getCurrentPosition()));
                }
               mHandler.postDelayed(mRunnable, 100);
            }
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

    private void connection(){
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, sc, BIND_AUTO_CREATE);
        mHandler.post(mRunnable);

    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.play_pause:
                ms.play(play_pause_button, state, animator);
                seekBar.setMax(ms.mp.getDuration());
                //Log.d("Max",""+ms.mp.getDuration());
                break;
            case R.id.stop:
                if(ms!=null)
                    ms.stop();
                    //seekBar.setProgress(0);
                    state.setText("Stopped");
                    play_pause_button.setText("Play");
                    animator.cancel();
                break;
            case R.id.quit:
                mHandler.removeCallbacks(mRunnable);
                unbindService(sc);
                try{
                    MainActivity.this.finish();
                    System.exit(0);
                } catch (Exception e){
                    e.printStackTrace();
                }

                break;
        }

    }

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ms = ((MusicService.MyBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ms = null;
        }
    };



}
