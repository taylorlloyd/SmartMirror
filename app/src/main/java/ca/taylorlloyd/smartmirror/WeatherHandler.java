package ca.taylorlloyd.smartmirror;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.*;
import ca.taylorlloyd.smartmirror.event.*;

import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.otto.*;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by taylor on 2016-03-16.
 */
public class WeatherHandler {
    private static String TAG = "WeatherHandler";
    private static String API_KEY = "CHANGEME";
    private static final long TIMEOUT = 60*60*1000;
    private Bus bus;
    private Context context;
    private RequestQueue queue;
    private Location location;

    private WeatherEvent lastEvent;
    private long lastEventTime;

    public WeatherHandler(Bus bus, Context context, RequestQueue queue) {
        this.bus = bus;
        this.queue = queue;
        this.context = context;
        bus.register(this);
    }

    @Subscribe public void onLocationEvent(LocationEvent evt) {
        if(evt.location == null) {
            Log.w(TAG, "Null Location Returned");
            return;
        }
        Log.i(TAG, "Got location - lat:" + evt.location.getLatitude() + ", long:" + evt.location.getLongitude());
        if(this.location == null) {
            this.location = evt.location;
            onUpdateRequest(new UpdateRequest());
        } else {
            this.location = evt.location;
        }
    }

    @Subscribe public void onUpdateRequest(UpdateRequest req) {
        if(this.lastEvent != null && System.currentTimeMillis()-this.lastEventTime < TIMEOUT) {
            bus.post(this.lastEvent);
            Log.i(TAG, "Resending last weather response");
        }
        if(this.location != null) {
            Log.i(TAG, "Retrieving weather at lat:" + this.location.getLatitude() + ", long:" + this.location.getLongitude());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                                                              getForecastUrl(this.location.getLatitude(), this.location.getLongitude()),
                                                              null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject current = response.getJSONObject("currently");
                                JSONObject today = response.getJSONObject("daily").getJSONArray("data").getJSONObject(0);

                                double curTemp = current.getDouble("temperature");
                                double lowTemp = today.getDouble("temperatureMin");
                                double highTemp = today.getDouble("temperatureMax");
                                String icon = today.getString("icon");

                                lastEvent = new WeatherEvent(curTemp, highTemp, lowTemp, icon);
                                lastEventTime = System.currentTimeMillis();
                                bus.post(lastEvent);

                                Log.i(TAG, "Weather info posted to bus");
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing forecast.io response");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error retrieving weather", error);
                        }
                    });
            queue.add(request);
        } else {
            Log.i(TAG, "Delaying weather lookup for location");
        }
    }

    private String getForecastUrl(double latitude, double longitude) {
        return "https://api.forecast.io/forecast/"+API_KEY+"/"+latitude+","+longitude+"?units=si";
    }





}
