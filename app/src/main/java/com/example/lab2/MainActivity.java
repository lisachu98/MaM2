package com.example.lab2;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Button play, pause, stop, record, file;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private TextView fileName;
    private Uri song;
    private boolean isRecording;

    public static final int RequestPermissionCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        song = null;

        mediaPlayer = new MediaPlayer();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioSamplingRate(8000);
        if (checkPermission()) {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "Recording.mp3");
        }else requestPermission();
        isRecording = false;

        play = (Button) findViewById(R.id.play);
        pause = (Button) findViewById(R.id.pause);
        stop = (Button) findViewById(R.id.stop);
        record = (Button) findViewById(R.id.record);
        file = (Button) findViewById(R.id.file);

        fileName = (TextView) findViewById(R.id.fileName);

        play.setEnabled(false);
        pause.setEnabled(false);
        stop.setEnabled(false);

        fileName.setText("Choose file");

        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/mpeg");
                //intent.putExtra(Intent.EXTRA_TITLE, fileName);
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.DIRECTORY_DOCUMENTS);
                startActivityForResult(intent, 111);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                play.setEnabled(false);
                pause.setEnabled(true);
                stop.setEnabled(true);
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
                pause.setEnabled(false);
                play.setEnabled(true);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                pause.setEnabled(false);
                play.setEnabled(true);
                stop.setEnabled(false);
            }
        });

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    if (!isRecording) {
                        try {
                            mediaRecorder.prepare();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        mediaRecorder.start();
                        isRecording = true;
                        Toast.makeText(MainActivity.this, "Started recording", Toast.LENGTH_LONG).show();
                    } else {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        isRecording = false;
                        Toast.makeText(MainActivity.this, "Recording saved", Toast.LENGTH_LONG).show();
                    }
                }else requestPermission();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == Activity.RESULT_OK){
            if (data != null){
                song = data.getData();
                fileName.setText(song.getPath());
                play.setEnabled(true);
                if (checkPermission()) {
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(MainActivity.this, song);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else requestPermission();
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{MANAGE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
            }
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    public boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return Environment.isExternalStorageManager() && result1 == PackageManager.PERMISSION_GRANTED;
    }
}