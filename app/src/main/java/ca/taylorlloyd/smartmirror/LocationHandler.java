package ca.taylorlloyd.smartmirror;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import ca.taylorlloyd.smartmirror.event.LocationEvent;
import ca.taylorlloyd.smartmirror.event.UpdateRequest;

/**
 * Created by taylor on 2016-03-17.
 */
public class LocationHandler implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static String TAG = LocationHandler.class.getSimpleName();
    private Bus bus;
    private Context context;
    private GoogleApiClient client;

    public LocationHandler(Bus bus, Context context) {
        this.bus = bus;
        this.context = context;
        this.client = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        this.bus.register(this);
    }

    @Subscribe
    public void onUpdateRequest(UpdateRequest req) {
        Log.i(TAG, "Location Requested");
        this.client.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected. Retrieving location");
        try {
            this.bus.post(new LocationEvent(LocationServices.FusedLocationApi.getLastLocation(this.client)));
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        this.client.disconnect();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult((Activity)context, 9000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }
}
