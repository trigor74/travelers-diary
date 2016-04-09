package com.travelersdiary.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.models.LocationPoint;
import com.travelersdiary.models.WeatherInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class WeatherIntentService extends IntentService {
    private static final String TAG = "WeatherIntentService";

    public static final String LOCATION_DATA_EXTRA = "LOCATION_DATA_EXTRA";

    public WeatherIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            LocationPoint location = (LocationPoint) intent.getSerializableExtra(WeatherIntentService.LOCATION_DATA_EXTRA);
            if (location == null) {
                Log.wtf(TAG, getString(R.string.no_location_data_provided_text));
                WeatherInfo weather = new WeatherInfo();
                weather.setWeatherDescription(getString(R.string.weather_api_na_text));
                deliverResultToReceiver(weather);
                return;
            }

            try {
                // TODO: 05.04.16 units format from settings
                URL url = new URL(getString(R.string.weather_api_data_url,
                        String.format(Locale.ENGLISH, "%.3f", location.getLatitude()),
                        String.format(Locale.ENGLISH, "%.3f", location.getLongitude()),
                        "metric",
                        Constants.OPENWEATHERMAP_AIPID,
                        Locale.getDefault().getLanguage()));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    StringBuilder buffer = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    JSONObject jsonObject = new JSONObject(buffer.toString());

                    WeatherInfo weather = new WeatherInfo();

                    weather.setWeatherIcon(jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon"));
                    weather.setWeatherMain(jsonObject.getJSONArray("weather").getJSONObject(0).getString("main"));
                    weather.setWeatherDescription(jsonObject.getJSONArray("weather").getJSONObject(0).getString("description"));
                    weather.setTemp(Float.valueOf(jsonObject.getJSONObject("main").getString("temp").toString()));
                    weather.setPressure(Float.valueOf(jsonObject.getJSONObject("main").getString("pressure").toString()));
                    weather.setHumidity(Integer.valueOf(jsonObject.getJSONObject("main").getString("humidity").toString()));
                    weather.setWindSpeed(Float.valueOf(jsonObject.getJSONObject("wind").getString("speed").toString()));
                    weather.setWindDeg(Float.valueOf(jsonObject.getJSONObject("wind").getString("deg").toString()));
                    weather.setClouds(Integer.valueOf(jsonObject.getJSONObject("clouds").getString("all").toString()));
                    weather.setSunrise(Long.valueOf(jsonObject.getJSONObject("sys").getString("sunrise").toString()));
                    weather.setSunset(Long.valueOf(jsonObject.getJSONObject("sys").getString("sunset").toString()));

                    deliverResultToReceiver(weather);
                } catch (JSONException e) {
                    Log.wtf(TAG, e.getMessage());
                    WeatherInfo weather = new WeatherInfo();
                    weather.setWeatherDescription(getString(R.string.weather_api_na_text));
                    deliverResultToReceiver(weather);
                } finally {
                    urlConnection.disconnect();
                }

            } catch (IOException e) {
                Log.wtf(TAG, e.getMessage());
                WeatherInfo weather = new WeatherInfo();
                weather.setWeatherDescription(getString(R.string.weather_api_na_text));
                deliverResultToReceiver(weather);
            }
        }
    }

    private void deliverResultToReceiver(WeatherInfo weatherInfo) {
        BusProvider.bus().post(weatherInfo);
    }
}
