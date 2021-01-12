package com.example.cobakamera;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cobakamera.ApiService;
import com.example.cobakamera.ApiUtils;
import com.example.cobakamera.R;
import com.example.cobakamera.Post;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.util.Base64;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int KODE_KAMERA = 555;
    private static final int MY_PERMISSION_WRITE_FILE = 556;
    Button btnAmbilKamera, btnUploadFoto;
    ImageView imgFoto;
    String namaFile, base64Encode;
//    Retrofit
    private ApiService mAPIService;
//    SensorManager
    private SensorManager manager;
    private Sensor accelerometer;
    private TextView textView;
    private float xAcceleration,yAcceleration,zAcceleration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        askWritePermission();
        setContentView(R.layout.activity_main);

        btnAmbilKamera = (Button) findViewById(R.id.btnAmbil);
        btnUploadFoto = (Button) findViewById(R.id.btnUpload);
        imgFoto = (ImageView) findViewById(R.id.imageView2);

//        Retrofit
        mAPIService = ApiUtils.getAPIService();

//        SensorManager
        textView = (TextView)findViewById(R.id.sensorData);
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

//       CAPTURE FOTO DAN MENYIMPAN MENGGUNAKAN METHOD putExtra
        btnAmbilKamera.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Intent intentKamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intentKamera, 0);
//                File imagesFolder = new File(Environment.getExternalStorageDirectory(), "HasilFoto");
//                imagesFolder.mkdirs();
//                Date tanggalSekarang = new Date();
//                String formatNama = new SimpleDateFormat("yyyyMMdd_HHmmss").format(tanggalSekarang);
//                namaFile = imagesFolder + File.separator + formatNama + ".jpg";
//
//                File image = new File(namaFile);
//                Uri uriSavedImage = FileProvider.getUriForFile(MainActivity.this, "com.example.nanta.cobakamera.provider", image);
//
////              Menyimpan dengan menggunakan putExtra
//                intentKamera.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
//                VOID Function
                startActivityForResult(intentKamera, KODE_KAMERA);
//                Log.d("TESTES", "TESTES");
            }
        });

        btnUploadFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFoto(base64Encode);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("VALUE RESULT CODE", String.valueOf(resultCode));
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (KODE_KAMERA):
                    try {
//
                        Log.d("TESPROSES", "TESPROSES");
                        prosesKamera(data);
                        Log.d("TESTES2", "TESTES2");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void prosesKamera(Intent kamera) throws IOException {
        Bitmap bm;
        bm = (Bitmap) kamera.getExtras().get("data");
        imgFoto.setImageBitmap(bm);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        Date tanggalSekarang = new Date();
        String formatNama = new SimpleDateFormat("yyyyMMdd_HHmmss").format(tanggalSekarang);
        namaFile = formatNama + ".png";
        File output = new File(dir, namaFile);
        FileOutputStream fo = new FileOutputStream(output);
        fo.write(byteArray);
        fo.flush();
        fo.close();
//        ENCODE BYTE ARRAY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            base64Encode = Base64.getEncoder().encodeToString(byteArray);
            Log.d("ISI BASE64", base64Encode);
        }
        Toast.makeText(this, "Data Telah Terload ke ImageView", Toast.LENGTH_LONG).show();
    }

    private void askWritePermission() {
        if(Build.VERSION.SDK_INT >= 23) {
            int cameraPermission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(cameraPermission != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_WRITE_FILE);
            }
        }

    }

    private void uploadFoto(String base64) {
        if(base64 == null) {
//            Toast.makeText(getBaseContext(), "ADA ISINYA", Toast.LENGTH_LONG).show();
            Toast.makeText(getBaseContext(), "Belum Ada Foto", Toast.LENGTH_LONG).show();
        }
        else {
            Log.d("CEKISI", base64);
//            String tes = "nyobak";
            //            Do Retrofit HERE
            mAPIService.savePost(base64).enqueue(new Callback<Post>() {
                @Override
                public void onResponse(Call<Post> call, Response<Post> response) {
                    if(response.isSuccessful()) {
                        Toast.makeText(getBaseContext(), "Foto Berhasil Dikirim ke API " + response.body().toString(), Toast.LENGTH_LONG).show();
                        Log.i("POST BERHASIL", "Foto Berhasil Terupload melalui API " + response.body().toString());
                    }
                }

                @Override
                public void onFailure(Call<Post> call, Throwable t) {
                    Log.d("Call", String.valueOf(call));
                    Log.d("Throwable", String.valueOf(t));
                    if (t instanceof SocketTimeoutException)
                    {
                        // "Connection Timeout";
                        Log.d("SocketTimeoutException", "SocketTimeoutException Error");
                    }
                    else if (t instanceof IOException)
                    {
                        // "Timeout";
                        Log.d("TIMEOUT", "IOException Error");
                    }
                    else
                    {
                        //Call was cancelled by user
                        if(call.isCanceled())
                        {
                            Log.d("CANCELED", "Call was cancelled forcefully");
                        }
                        else
                        {
                            //Generic error handling
                            Log.d("GENERIC" ,"Network Error :: " + t.getLocalizedMessage());
                        }
                    }
                    Toast.makeText(getBaseContext(), "Gagal Mengirim Foto", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            xAcceleration = event.values[0];
            yAcceleration = event.values[1];
            zAcceleration = event.values[2];
            textView.setText("x:"+xAcceleration+"\nY:"+yAcceleration+"\nZ:"+zAcceleration);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }
}