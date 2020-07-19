package com.example.note_gabru_android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;import android.view.MenuItem;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DescriptionActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener  {

    ImageButton popupButton;
    ImageView imageView;
    Uri imageUri;
    ImageButton startRec;
    ImageButton stopRec;
    ImageButton playRec, replayRec;
    String mCurrentPhotoPath;
    Bitmap mImageBitmap;
    double latitude, longitude;

    private static final int REQUEST_CODE = 1;
    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_Reset_IMAGE = 3;
    public static final int CAMERA_REQUEST = 1000;
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

//on start
    @Override
    protected void onStart() {
        super.onStart();

        if (!checkPermission())
            requestPermission();
        else
            getLastLocation();
    }


//onCreate
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);


        final EditText editTextTitle = findViewById(R.id.title_edit_text);
        final EditText editTextDesc = findViewById(R.id.description_edit_text);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Detail");
        //image capture
        popupButton = findViewById(R.id.popup);
        imageView = findViewById(R.id.image_view);
        startRec = findViewById(R.id.btn_start_record);
        stopRec = findViewById(R.id.btn_stop_record);
        playRec = findViewById(R.id.btn_play_record);
        replayRec = findViewById(R.id.btn_replay);

        //popup

        popupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenuExample();

            }
        });

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


        startRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermissionDevice()) {
                    requestAudioPermission();
                    return;
                }

                if (checkPermissionDevice()) {

                    RECORDED_FILE = "/audio" + titleName + ".3gp";
                    audiofilepath = getExternalCacheDir().getAbsolutePath()
                            + RECORDED_FILE;
                    setUpMediaRecorder();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException ise) {
                        // make something ...
                        ise.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    startRec.setVisibility(View.GONE);
                    stopRec.setVisibility(View.VISIBLE);

                } else {
                    requestAudioPermission();
                }
            }
        });

        stopRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaRecorder.stop();
                stopRec.setVisibility(View.GONE);
                playRec.setVisibility(View.VISIBLE);
            }
        });

        playRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audiofilepath);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        playRec.setVisibility(View.GONE);
                        replayRec.setVisibility(View.VISIBLE);

                    }
                });

                mediaPlayer.start();
            }
        });

        replayRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audiofilepath);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();

            }
        });


    }


//popup_show
    private void popupMenuExample() {
        PopupMenu p = new PopupMenu(DescriptionActivity.this, popupButton);
        p.getMenuInflater().inflate(R.menu.popup_menu, p .getMenu());
        p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.camera:
                            openCamera();
                            return true;
                        case R.id.Gallery:
                            opengallery();
                            return true;
                        case R.id.Reset:
                         imgReset();
                            return true;
                    }
                Toast.makeText(DescriptionActivity.this,item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        p.show();
    }




    //popup_actions


    public void showMenu(View v)
    {
        PopupMenu popup = new PopupMenu(this,v);
        popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) this);// to implement on click event on items of menu
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.popup_menu, popup.getMenu());
        popup.show();
    }




//opengallery fn
    public void opengallery(){
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.category_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.camera:
                openCamera();
                return true;
            case R.id.Gallery:
                opengallery();
                return true;
            case R.id.Reset:
             imgReset();
                return true;


            case R.id.btn_location:
                Intent intent = new Intent(DescriptionActivity.this, MapActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
//camera-fn
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = titleName + timeStamp + "_";

        File storageDir = Environment.getExternalStorageDirectory();

        File dir = new File(getExternalCacheDir().getAbsolutePath() + "/notes/");
        if (!dir.exists()) {
            dir.mkdir();
        }

        File image = File.createTempFile(imageFileName, ".jpg", dir);


        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_CODE == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openCamera();
                getLastLocation();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            } else {
                Toast.makeText(this, "denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

        if (CAMERA_REQUEST == requestCode) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            } else {
                Toast.makeText(this, " Camera denied", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = titleName + timeStamp + ".jpg";

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();

                File dir = new File(getExternalCacheDir().getAbsolutePath() + "/notes/");
                if (!dir.exists()) {
                    dir.mkdir();
                }

                File file = new File(dir.getAbsolutePath() + "/" + imageFileName);

                FileOutputStream fileOuputStream = new FileOutputStream(file);
                fileOuputStream.write(b);
                Log.d("TAG", "image path: " + file.getAbsolutePath());

                mCurrentPhotoPath = "file:" + file.getAbsolutePath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(DescriptionActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {

        }

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            try {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = titleName + timeStamp + ".jpg";

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();

                File dir = new File(getExternalCacheDir().getAbsolutePath() + "/notes/");
                if (!dir.exists()) {
                    dir.mkdir();
                }

                File file = new File(dir.getAbsolutePath() + "/" + imageFileName);

                FileOutputStream fileOuputStream = new FileOutputStream(file);
                fileOuputStream.write(b);
                Log.d("TAG", "image path: " + file.getAbsolutePath());

                mCurrentPhotoPath = "file:" + file.getAbsolutePath();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (requestCode == RESULT_Reset_IMAGE && resultCode == RESULT_OK && null != data) {

          imgReset();

        }
        else {

        }

    }
//Reset image
    public void imgReset(){  try {

        final Bitmap selectedImage = BitmapFactory.decodeResource(getResources(),
                R.drawable.placeholder);
        imageView.setImageBitmap(selectedImage);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = titleName + timeStamp + ".jpg";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();

        File dir = new File(getExternalCacheDir().getAbsolutePath() + "/notes/");
        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(dir.getAbsolutePath() + "/" + imageFileName);

        FileOutputStream fileOuputStream = new FileOutputStream(file);
        fileOuputStream.write(b);
        Log.d("TAG", "image path: " + file.getAbsolutePath());

        mCurrentPhotoPath = "file:" + file.getAbsolutePath();
    } catch (FileNotFoundException e) {
        e.printStackTrace();
        Toast.makeText(DescriptionActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
    } catch (IOException e) {
        e.printStackTrace();
    }

    }
    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);

    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {

                    noteLocation = location;


                }
            }
        };
    }

    private boolean checkPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_PERMISSION_CODE);
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    noteLocation = task.getResult();
                    System.out.println(noteLocation.getLongitude());
                    System.out.println(noteLocation.getLatitude());

                }
            }
        });
    }

    private boolean checkPermissionDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }

    private void setUpMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(audiofilepath);


    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }
}
