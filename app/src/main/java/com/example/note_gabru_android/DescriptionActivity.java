package com.example.note_gabru_android;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DescriptionActivity extends AppCompatActivity {
    ImageButton imageButton;
    ImageView imageView;
    Uri imageUri;
    ImageButton startRec;
    ImageButton stopRec;
    ImageButton playRec, replayRec;
    String mCurrentPhotoPath;
    Bitmap mImageBitmap;
    double latitude, longitude;

    private static final int REQUEST_CODE = 1;


    public static final int CAMERA_REQUEST = 1000;
    public static final int MY_CAMERA_PERMISSION_CODE = 1001;
    DataBaseHelper dataBaseHelper;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    Location noteLocation;
    String titleName;
    int nid;

    String audiofilepath;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    boolean selected;

    EditText editTextTitle;
    EditText editTextDesc;

    CategoryModel selectednote;

    final int REQUEST_PERMISSION_CODE = 1000;

    String RECORDED_FILE;


    @Override
    protected void onStart() {
        super.onStart();

        if (!checkPermission())
            requestPermission();
        else
            getLastLocation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        final EditText editTextTitle = findViewById(R.id.title_edit_text);
        final EditText editTextDesc = findViewById(R.id.description_edit_text);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Detail");
        //image capture

        imageButton = findViewById(R.id.chooseimagebtn);
        imageView = findViewById(R.id.image_view);
        startRec = findViewById(R.id.btn_start_record);
        stopRec = findViewById(R.id.btn_stop_record);
        playRec = findViewById(R.id.btn_play_record);
        replayRec = findViewById(R.id.btn_replay);


        Button buttonSave = findViewById(R.id.btn_save_note);


        dataBaseHelper = new DataBaseHelper(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        buildLocationRequest();
        buildLocationCallBack();

        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        // set the volume of played media to maximum.
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        Intent intent = getIntent();
        selected = intent.getBooleanExtra("selected", false);

        if (selected) {

            selectednote = (CategoryModel) intent.getSerializableExtra("note");
            editTextTitle.setText(selectednote.getTitle());
            editTextDesc.setText(selectednote.getDescription());
            audiofilepath = selectednote.getAudio();
            mCurrentPhotoPath = selectednote.getImage();
            latitude = selectednote.getNoteLat();
            longitude = selectednote.getNoteLong();
            nid = selectednote.getId();
            startRec.setVisibility(View.GONE);
            playRec.setVisibility(View.VISIBLE);

            if (audiofilepath != null) {
                playRec.setVisibility(View.VISIBLE);
                startRec.setVisibility(View.GONE);
                stopRec.setVisibility(View.GONE);
                replayRec.setVisibility(View.GONE);
            } else {
                startRec.setVisibility(View.VISIBLE);
                stopRec.setVisibility(View.GONE);
                replayRec.setVisibility(View.GONE);
                playRec.setVisibility(View.GONE);

            }


            if (mCurrentPhotoPath != null) {
                try {
                    mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                    imageView.setImageBitmap(mImageBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
        
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                titleName = editTextTitle.getText().toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, CAMERA_REQUEST);

                    } else {
                        openCamera();
                    }
                } else {
                    openCamera();
                }
            }
        });


        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat();
                String joiningDate = sdf.format(calendar.getTime());

                String cname = MainActivity.categoryName.get(MainActivity.catPosition);
                String ntitle = editTextTitle.getText().toString().trim();
                String ndesc = editTextDesc.getText().toString().trim();

                titleName = ntitle;

                if (ntitle.isEmpty() && ndesc.isEmpty()) {
                    Toast.makeText(DescriptionActivity.this, "Fill the required feilds", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (selected) {
                    if (dataBaseHelper.updateNote(nid, ntitle, ndesc, audiofilepath, mCurrentPhotoPath)) {
                        Toast.makeText(DescriptionActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DescriptionActivity.this, "Not Updated", Toast.LENGTH_SHORT).show();
                    }
                }

                if (!selected) {
                    if (dataBaseHelper.addNote(cname, ntitle, ndesc, joiningDate, noteLocation.getLatitude(), noteLocation.getLongitude(), audiofilepath, mCurrentPhotoPath)) {
                        Toast.makeText(DescriptionActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DescriptionActivity.this, "Not saved", Toast.LENGTH_SHORT).show();
                    }
                }


                Intent intent = new Intent(DescriptionActivity.this, NotesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);


            }
        });

