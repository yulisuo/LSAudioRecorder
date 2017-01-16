package com.yls.recorder.lsaudiorecorder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;

/**
 * 基于Android developer guide的简单录音机（带播放功能）
 * TODO 后面可以写成至少不比Android原生简单的，带更多的界面和功能，可以参考国内主流的UI
 */
public class MainActivity extends AppCompatActivity {

    private MediaRecorder recorder;
    private RecordButton recordButton;

    private String recordFileName;

    private MediaPlayer player;
    private PlayButton playButton;

    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private boolean permissionAccepted = false;
    private static final int REQUEST_AUDIO_RECORD_CODE = 200;

    private static final String TAG = "LSAudioRecorder";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordFileName = getExternalCacheDir().getAbsolutePath();
        recordFileName += "/my_record_file.3gp";

        requestPermissions(permissions,REQUEST_AUDIO_RECORD_CODE);

        LinearLayout ll = new LinearLayout(this);
        recordButton = new RecordButton(this);
        ll.addView(recordButton,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,0));
        playButton = new PlayButton(this);
        ll.addView(playButton,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,0));
        setContentView(ll);
    }

    /**
     * TODO 需要了解一下Android M后权限管理和申请的feature
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_AUDIO_RECORD_CODE:
                permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            default:
                break;
        }
        if(! permissionAccepted){
            Log.i(TAG,"permission not accepted!");
        }
    }

    /**
     * 这种在demo上写view的方法挺简单实用的
     * TODO 不利于扩展和后期自己看代码和界面结构，需要写成layout xml形式的
     */
    class PlayButton extends Button{

        boolean startPlay = true;
        public PlayButton(Context context) {
            super(context);
            setText("start play");
            setOnClickListener(listener);
        }

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(startPlay);
                if(startPlay){
                    setText("stop play");
                }else {
                    setText("start play");
                }
                startPlay = !startPlay;
            }
        };
    }

    void onPlay(boolean play){
        if(play){
            startPlay();
        }else{
            stopPlay();
        }
    }

    private void stopPlay() {
        player.release();
        player = null;
    }

    private void startPlay() {
        player = new MediaPlayer();
        try {
            player.setDataSource(recordFileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class RecordButton extends Button{
        boolean startRecord = true;
        public RecordButton(Context context) {
            super(context);
            setText("start record");
            setOnClickListener(listener);
        }

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(startRecord);
                if(startRecord){
                    setText("stop record");
                }else {
                    setText("start record");
                }
                startRecord = !startRecord;
            }
        };
    }

    void onRecord(boolean start){
        if(start){
            startRecord();
        }else{
            stopRecord();
        }
    }

    void startRecord(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordFileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder.start();
    }

    void stopRecord(){
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(recorder != null){
            recorder.release();
            recorder = null;
        }
        if(player != null){
            player.release();
            player = null;
        }
    }
}
