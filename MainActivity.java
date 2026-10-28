
package com.example.secretcalculatoralina;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView display;
    private MediaPlayer mediaPlayer;

    private String current = "";
    private double firstNumber = 0;
    private String operator = "";
    private boolean newNumber = true;

    private static final int REQUEST_GAMBAR = 100;
    private static final int REQUEST_GPS = 200;

    private final int[] nimDigits = {0, 4, 0, 4, 2};

    private final String[] features = {
            "Kamera",
            "Play Audio",
            "Tampil Gambar",
            "GPS",
            "Gyroscope dan Accelerometer"
    };

    private final int ORANGE = Color.rgb(255, 152, 0);
    private final int DARK = Color.rgb(23, 23, 23);

    private LocationManager locationManager;
    private LocationListener gpsListener;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private TextView sensorText;
    private AlertDialog sensorDialog;
    private boolean sensorAktif = false;

    private float ax, ay, az;
    private float gx, gy, gz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildCalculator();

        locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);

        sensorManager =
                (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER);

            gyroscope = sensorManager.getDefaultSensor(
                    Sensor.TYPE_GYROSCOPE);
        }
    }

    private void buildCalculator() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 24, 16, 16);
        root.setBackgroundColor(DARK);

        TextView title = new TextView(this);
        title.setText("SECRET CALCULATOR ALINA");
        title.setTextColor(ORANGE);
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        root.addView(title, new LinearLayout.LayoutParams(-1, 65));

        display = new TextView(this);
        display.setText("0");
        display.setTextColor(Color.WHITE);
        display.setTextSize(38);
        display.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        display.setPadding(12, 12, 12, 12);

        root.addView(display, new LinearLayout.LayoutParams(-1, 0, 1));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setRowCount(4);

        String[] keys = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "C", "0", "=", "+"
        };

        for (String key : keys) {
            Button button = new Button(this);
            button.setText(key);
            button.setTextSize(22);
            button.setAllCaps(false);
            button.setTextColor(Color.WHITE);

            button.setBackgroundTintList(
                    ColorStateList.valueOf(
                            isOperator(key)
                                    ? ORANGE
                                    : Color.rgb(55, 55, 55)
                    )
            );

            GridLayout.LayoutParams params =
                    new GridLayout.LayoutParams();

            params.width = 0;
            params.height = 0;
            params.columnSpec =
                    GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec =
                    GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(4, 4, 4, 4);

            grid.addView(button, params);
            button.setOnClickListener(v -> press(key));
        }

        root.addView(grid,
                new LinearLayout.LayoutParams(-1, 0, 3));

        setContentView(root);
    }

    private boolean isOperator(String key) {
        return key.equals("+") || key.equals("-")
                || key.equals("*") || key.equals("/")
                || key.equals("=");
    }

    private void press(String key) {
        if (key.equals("C")) {
            current = "";
            operator = "";
            firstNumber = 0;
            newNumber = true;
            display.setText("0");
            return;
        }

        if (key.matches("[0-9]")) {
            if (newNumber) {
                current = key;
                newNumber = false;
            } else {
                current += key;
            }

            display.setText(current);
            return;
        }

        if (key.equals("=")) {
            calculate();
            return;
        }

        if (!current.isEmpty()) {
            firstNumber = Double.parseDouble(current);
            operator = key;
            newNumber = true;
        }
    }

    private void calculate() {
        if (current.isEmpty() || operator.isEmpty()) {
            return;
        }

        double secondNumber = Double.parseDouble(current);
        double result;

        switch (operator) {
            case "+":
                result = firstNumber + secondNumber;
                break;

            case "-":
                result = firstNumber - secondNumber;
                break;

            case "*":
                result = firstNumber * secondNumber;
                break;

            case "/":
                if (secondNumber == 0) {
                    display.setText("Tidak bisa dibagi 0");
                    current = "";
                    operator = "";
                    newNumber = true;
                    return;
                }
                result = firstNumber / secondNumber;
                break;

            default:
                return;
        }

        if (result == Math.floor(result)
                && Math.abs(result) < 1e15) {
            current = String.valueOf((long) result);
        } else {
            current = String.valueOf(result);
        }

        display.setText(current);
        operator = "";
        newNumber = true;

        checkSecretFeature(result);
    }

    private void checkSecretFeature(double result) {
        if (result != Math.floor(result)
                || result < 0 || result > 9) {
            return;
        }

        int digit = (int) result;
        ArrayList<String> matched = new ArrayList<>();

        for (int i = 0; i < nimDigits.length; i++) {
            if (nimDigits[i] == digit) {
                matched.add(features[i]);
            }
        }

        if (matched.isEmpty()) {
            return;
        }

        String[] options = matched.toArray(new String[0]);

        if (options.length == 1) {
            openFeature(options[0]);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Secret Feature")
                    .setItems(options, (dialog, which) ->
                            openFeature(options[which]))
                    .setNegativeButton("Batal", null)
                    .show();
        }
    }

    private void openFeature(String feature) {
        switch (feature) {
            case "Kamera":
                openCamera();
                break;

            case "Play Audio":
                playAudio();
                break;

            case "Tampil Gambar":
                chooseImage();
                break;

            case "GPS":
                getGpsLocation();
                break;

            case "Gyroscope dan Accelerometer":
                openSensors();
                break;
        }
    }

    private void openCamera() {
        Intent intent =
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this,
                    "Aplikasi kamera tidak ditemukan",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void playAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = MediaPlayer.create(
                this, R.raw.audio_rahasia);

        if (mediaPlayer == null) {
            Toast.makeText(this,
                    "Audio gagal dimuat. Periksa file audio_rahasia.mp3",
                    Toast.LENGTH_LONG).show();
            return;
        }

        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
            if (mediaPlayer == mp) {
                mediaPlayer = null;
            }
        });

        mediaPlayer.start();

        Toast.makeText(this,
                "Audio sedang diputar",
                Toast.LENGTH_SHORT).show();
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(
                Intent.createChooser(intent, "Pilih Gambar"),
                REQUEST_GAMBAR
        );
    }

    private void getGpsLocation() {
        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_GPS
            );
            return;
        }

        try {
            if (locationManager == null) {
                Toast.makeText(this,
                        "Layanan lokasi tidak tersedia",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!locationManager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER)) {

                new AlertDialog.Builder(this)
                        .setTitle("GPS Belum Aktif")
                        .setMessage(
                                "Aktifkan lokasi/GPS terlebih dahulu.")
                        .setPositiveButton("Buka Pengaturan",
                                (dialog, which) -> startActivity(
                                        new Intent(
                                                Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                        .setNegativeButton("Batal", null)
                        .show();
                return;
            }

            if (gpsListener != null) {
                locationManager.removeUpdates(gpsListener);
            }

            gpsListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (locationManager != null && gpsListener != null) {
                        locationManager.removeUpdates(gpsListener);
                        gpsListener = null;
                    }

                    showGpsResult(location);
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Toast.makeText(MainActivity.this,
                            "GPS dinonaktifkan",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onStatusChanged(
                        String provider, int status, Bundle extras) {
                }
            };

            Toast.makeText(this,
                    "Sedang mengambil lokasi GPS...",
                    Toast.LENGTH_LONG).show();

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    0f,
                    gpsListener,
                    Looper.getMainLooper()
            );

        } catch (SecurityException e) {
            Toast.makeText(this,
                    "Izin lokasi belum tersedia",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this,
                    "GPS gagal: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showGpsResult(Location location) {
        String hasil = String.format(
                Locale.getDefault(),
                "Latitude: %f\nLongitude: %f",
                location.getLatitude(),
                location.getLongitude()
        );

        new AlertDialog.Builder(this)
                .setTitle("Lokasi GPS")
                .setMessage(hasil)
                .setPositiveButton("OK", null)
                .show();
    }

    private void openSensors() {
        if (sensorManager == null) {
            Toast.makeText(this,
                    "Sensor tidak tersedia",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        boolean adaAccelerometer = accelerometer != null;
        boolean adaGyroscope = gyroscope != null;

        if (!adaAccelerometer && !adaGyroscope) {
            Toast.makeText(this,
                    "Perangkat tidak memiliki sensor yang diperlukan",
                    Toast.LENGTH_LONG).show();
            return;
        }

        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 20, 24, 20);
        scrollView.addView(layout);

        sensorText = new TextView(this);
        sensorText.setTextSize(18);
        sensorText.setTextColor(Color.BLACK);
        sensorText.setText(
                "Mengaktifkan sensor...\n\n"
                        + "Accelerometer: membaca gerakan perangkat\n\n"
                        + "Gyroscope: membaca rotasi perangkat"
        );

        layout.addView(sensorText);

        sensorDialog = new AlertDialog.Builder(this)
                .setTitle("Gyroscope dan Accelerometer")
                .setView(scrollView)
                .setPositiveButton("Tutup", (dialog, which) -> {
                    stopSensors();
                })
                .create();

        sensorDialog.setOnDismissListener(dialog -> stopSensors());
        sensorDialog.show();

        sensorAktif = true;

        if (accelerometer != null) {
            sensorManager.registerListener(
                    this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (gyroscope != null) {
            sensorManager.registerListener(
                    this, gyroscope,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gx = event.values[0];
            gy = event.values[1];
            gz = event.values[2];
        }

        if (sensorText != null && sensorAktif) {
            String hasil = String.format(
                    Locale.getDefault(),
                    "ACCELEROMETER (m/s²)\nX: %.2f\nY: %.2f\nZ: %.2f\n\n"
                            + "GYROSCOPE (rad/s)\nX: %.2f\nY: %.2f\nZ: %.2f",
                    ax, ay, az, gx, gy, gz
            );

            sensorText.setText(hasil);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void stopSensors() {
        sensorAktif = false;

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        sensorText = null;
        sensorDialog = null;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == REQUEST_GPS) {
            boolean granted = checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;

            if (granted) {
                getGpsLocation();
            } else {
                Toast.makeText(this,
                        "Izin lokasi ditolak",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GAMBAR
                && resultCode == RESULT_OK
                && data != null) {

            Uri imageUri = data.getData();

            if (imageUri != null) {
                ImageView image = new ImageView(this);
                image.setImageURI(imageUri);
                image.setAdjustViewBounds(true);
                image.setMaxHeight(800);

                new AlertDialog.Builder(this)
                        .setTitle("Gambar Pilihan")
                        .setView(image)
                        .setPositiveButton("Tutup", null)
                        .show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (locationManager != null && gpsListener != null) {
            locationManager.removeUpdates(gpsListener);
            gpsListener = null;
        }

        stopSensors();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onDestroy();
    }
}
