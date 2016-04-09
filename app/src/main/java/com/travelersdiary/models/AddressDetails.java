package com.travelersdiary.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressDetails implements Serializable {
    private String adminArea;
    private String subAdminArea;
    private String thoroughfare;
    private String subThoroughfare;
    private String subLocality;
    private String countryCode;
    private String countryName;
    private String featureName;
    private String premises;
    private String locality;
    private String phone;
    private String postalCode;

    public AddressDetails() {
    }

    public AddressDetails(String adminArea, String countryCode, String featureName,
                          String countryName, String premises, String subAdminArea,
                          String subLocality, String subThoroughfare, String thoroughfare,
                          String locality, String phone, String postalCode) {
        this.adminArea = adminArea;
        this.countryCode = countryCode;
        this.featureName = featureName;
        this.countryName = countryName;
        this.premises = premises;
        this.subAdminArea = subAdminArea;
        this.subLocality = subLocality;
        this.subThoroughfare = subThoroughfare;
        this.thoroughfare = thoroughfare;
        this.locality = locality;
        this.phone = phone;
        this.postalCode = postalCode;
    }

    public String getAdminArea() {
        return adminArea;
    }

    public void setAdminArea(String adminArea) {
        this.adminArea = adminArea;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getPremises() {
        return premises;
    }

    public void setPremises(String premises) {
        this.premises = premises;
    }

    public String getSubAdminArea() {
        return subAdminArea;
    }

    public void setSubAdminArea(String subAdminArea) {
        this.subAdminArea = subAdminArea;
    }

    public String getSubLocality() {
        return subLocality;
    }

    public void setSubLocality(String subLocality) {
        this.subLocality = subLocality;
    }

    public String getSubThoroughfare() {
        return subThoroughfare;
    }

    public void setSubThoroughfare(String subThoroughfare) {
        this.subThoroughfare = subThoroughfare;
    }

    public String getThoroughfare() {
        return thoroughfare;
    }

    public void setThoroughfare(String thoroughfare) {
        this.thoroughfare = thoroughfare;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
