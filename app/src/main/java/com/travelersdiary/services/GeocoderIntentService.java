package com.travelersdiary.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;

import com.travelersdiary.R;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.models.AddressDetails;
import com.travelersdiary.models.LocationPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeocoderIntentService extends IntentService {
    private static final String TAG = "GeocoderIntentService";

    public class GeocoderResult {
        public int resultCode;
        public String message;
        public AddressDetails addressDetails;

        public GeocoderResult(int resultCode, String message, AddressDetails addressDetails) {
            this.resultCode = resultCode;
            this.message = message;
            this.addressDetails = addressDetails;
        }
    }

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String LOCATION_DATA_EXTRA = "LOCATION_DATA_EXTRA";

    public GeocoderIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        LocationPoint location = (LocationPoint) intent.getSerializableExtra(GeocoderIntentService.LOCATION_DATA_EXTRA);
        if (location == null) {
            errorMessage = getString(R.string.no_location_data_provided_text);
            Log.wtf(TAG, errorMessage);
            BusProvider.bus().post(new GeocoderResult(GeocoderIntentService.FAILURE_RESULT, errorMessage, null));
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.geocoder_service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.geocoder_invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " + location.getLongitude(), illegalArgumentException);
        }

        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.geocoder_no_address_found);
                Log.e(TAG, errorMessage);
            }
            BusProvider.bus().post(new GeocoderResult(GeocoderIntentService.FAILURE_RESULT, errorMessage, null));
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using {@code getAddressLine},
            // join them, and send them to the thread. The {@link android.location.address}
            // class provides other options for fetching address details that you may prefer
            // to use. Here are some examples:
            // getLocality() ("Mountain View", for example)
            // getAdminArea() ("CA", for example)
            // getPostalCode() ("94043", for example)
            // getCountryCode() ("US", for example)
            // getCountryName() ("United States", for example)

            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            String foundAddress = TextUtils.join(System.getProperty("line.separator"), addressFragments);
            Log.i(TAG, getString(R.string.geocoder_address_found) + ": " + foundAddress);

            BusProvider.bus().post(new GeocoderResult(GeocoderIntentService.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"), addressFragments), setupAddressDetails(address)));
        }
    }

    private AddressDetails setupAddressDetails(Address address) {
        AddressDetails addressDetails = new AddressDetails();

        addressDetails.setAdminArea(address.getAdminArea());
        addressDetails.setSubAdminArea(address.getSubAdminArea());

        addressDetails.setLocality(address.getLocality());
        addressDetails.setSubLocality(address.getSubLocality());

        addressDetails.setThoroughfare(address.getThoroughfare());
        addressDetails.setSubThoroughfare(address.getSubThoroughfare());

        addressDetails.setCountryCode(address.getCountryCode());
        addressDetails.setCountryName(address.getCountryName());

        addressDetails.setFeatureName(address.getFeatureName());

        addressDetails.setPhone(address.getPhone());
        addressDetails.setPostalCode(address.getPostalCode());
        addressDetails.setPremises(address.getPremises());

        return addressDetails;
    }

}
