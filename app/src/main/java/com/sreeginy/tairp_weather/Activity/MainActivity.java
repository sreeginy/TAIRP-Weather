package com.sreeginy.tairp_weather.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sreeginy.tairp_weather.R;
import com.sreeginy.tairp_weather.Model.WeatherData;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {


    TextView date;
    String apiKey = "0834e2dbfe812be60ce5bb46a74aa17c";
    String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=Colombo&appid=" + apiKey;

    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;
    final int REQUEST_CODE = 101;

    String locationProvider = LocationManager.GPS_PROVIDER;

    TextView mNameOfCity, mWeatherType, mTemperature, rain, windSpeed, humidity;
    ImageView mWeatherIcon;
    Button searchBtn;
    LocationManager locationManager;
    LocationListener locationListener;
    ImageButton viewBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mWeatherType = findViewById(R.id.weatherType);
        mTemperature = findViewById(R.id.temperature);
        mWeatherIcon = findViewById(R.id.weatherIcon);
        searchBtn = findViewById(R.id.searchBtn);
        mNameOfCity = findViewById(R.id.cityName);
        date = findViewById(R.id.date);

        rain = findViewById(R.id.rain);
        windSpeed = findViewById(R.id.wind);
        humidity = findViewById(R.id.humidity);

        viewBtn = findViewById(R.id.viewBtn);

        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
        String currentDate = format.format(new Date());
        date.setText(currentDate);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WeatherSortingActivity.class);
                startActivity(intent);
            }
        });

        mWeatherIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a WeatherData object with the weather details
                WeatherData weatherData = new WeatherData();
                weatherData.setmNameOfCity(mNameOfCity.getText().toString());
                weatherData.setmTemperature(mTemperature.getText().toString());
                weatherData.setmWeatherType(mWeatherType.getText().toString());
                weatherData.setmWeatherIcon("weather_icon"); // Set the appropriate weather icon value
                weatherData.setRain(rain.getText().toString());
                weatherData.setWindSpeed(windSpeed.getText().toString());
                weatherData.setHumidity(humidity.getText().toString());
                weatherData.setPressure(pressure.getText().toString()); // Replace 'pressure' with the appropriate TextView for pressure


                Intent intent = new Intent(MainActivity.this, ViewWeatherDetails.class);
                intent.putExtra("weatherData", weatherData);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String city = intent.getStringExtra("City");
        if (city != null) {
            apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;
            getWeatherForNewCity(city);
        } else {
            getWeatherForCurrentLocation();
        }
    }

    private void getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", apiKey);
        letsdoSomeNetworking(params);
    }

    private void getWeatherForCurrentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());

                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", apiKey);
                letsdoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }
        };


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        locationManager.requestLocationUpdates(locationProvider, MIN_TIME, MIN_DISTANCE, locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Location obtained successfully", Toast.LENGTH_SHORT).show();
                getWeatherForCurrentLocation();
            } else {
                Toast.makeText(MainActivity.this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void letsdoSomeNetworking(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(apiUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(MainActivity.this, "Data retrieved successfully", Toast.LENGTH_SHORT).show();
                WeatherData weatherData = null;
                try {
                    weatherData = WeatherData.fromJson(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                updateUI(weatherData);
            }
        });
    }
    public void updateUI(WeatherData weather) {
        mTemperature.setText(weather.getmTemperature() + "°");
        mNameOfCity.setText(weather.getmNameOfCity());
        mWeatherType.setText("Mostly " + weather.getmWeatherType());
        int resourceId = getResources().getIdentifier(weather.getmWeatherIcon(), "drawable", getPackageName());
        mWeatherIcon.setImageResource(resourceId);
        rain.setText(weather.getRain() + " mm");
        windSpeed.setText(weather.getWindSpeed() + " km/h");
        humidity.setText(weather.getHumidity() + "%");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
    }
