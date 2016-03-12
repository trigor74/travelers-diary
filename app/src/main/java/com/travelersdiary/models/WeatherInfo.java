package com.travelersdiary.models;

import java.io.Serializable;

public class WeatherInfo implements Serializable {
    private String unit; // Default, Metric, Imperial
    private String weatherMain; // Group of weather parameters (Rain, Snow, Extreme etc.)
    private String weatherDescription; // Weather condition within the group
    private String weatherIcon; // Weather icon id
    private float temp; // Temperature. Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    private float pressure; // Atmospheric pressure (on the sea level), hPa
    private int humidity; // Humidity, %
    private float windSpeed; // Wind speed. Unit Default: meter/sec, Metric: meter/sec, Imperial: miles/hour.
    private float windDeg; // Wind direction, degrees (meteorological)
    private int clouds; // Cloudiness, %
    private long sunrise; // time stamp
    private long sunset; // time stamp

    public WeatherInfo() {
    }

    public WeatherInfo(String unit, String weatherMain, String weatherDescription, String weatherIcon,
                       float temp, float pressure, int humidity, float windSpeed, float windDeg, int clouds,
                       long sunrise, long sunset) {
        this.unit = unit;
        this.weatherMain = weatherMain;
        this.weatherDescription = weatherDescription;
        this.weatherIcon = weatherIcon;
        this.temp = temp;
        this.pressure = pressure;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.windDeg = windDeg;
        this.clouds = clouds;
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getWeatherMain() {
        return weatherMain;
    }

    public void setWeatherMain(String weatherMain) {
        this.weatherMain = weatherMain;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public float getWindDeg() {
        return windDeg;
    }

    public void setWindDeg(float windDeg) {
        this.windDeg = windDeg;
    }

    public int getClouds() {
        return clouds;
    }

    public void setClouds(int clouds) {
        this.clouds = clouds;
    }

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }
}
