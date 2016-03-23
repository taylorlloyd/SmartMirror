package ca.taylorlloyd.smartmirror;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.toolbox.Volley;
import ca.taylorlloyd.smartmirror.event.*;
import com.squareup.otto.*;

import java.util.HashMap;
import java.util.Map;

public class FullscreenActivity extends AppCompatActivity {

    private RequestQueue queue;
    private Bus bus;

    private TextView timeTxt;
    private TextView dateTxt;
    private TextView curTempTxt;
    private TextView highTempTxt;
    private TextView lowTempTxt;
    private ImageView tempIcon;

    private boolean isLive = false;
    private long UPDATE_INTERVAL = 5000;

    private Runnable liveUpdater = new Runnable() {
        @Override
        public void run() {
            if(isLive) {
                bus.post(new UpdateRequest());
                new Handler(Looper.getMainLooper()).postDelayed(liveUpdater, UPDATE_INTERVAL);
            }
        }
    };

    private static final Map<String, Integer> icons = new HashMap<>();
    static {
        icons.put("clear-day", R.drawable.clear_day);
        icons.put("clear-night", R.drawable.clear_night);
        icons.put("cloudy", R.drawable.cloudy);
        icons.put("fog", R.drawable.fog);
        icons.put("partly-cloudy-day", R.drawable.partly_cloudy_day);
        icons.put("partly-cloudy-night", R.drawable.partly_cloudy_night);
        icons.put("rain", R.drawable.rain);
        icons.put("sleet", R.drawable.sleet);
        icons.put("snow", R.drawable.snow);
        icons.put("wind", R.drawable.wind);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        // Hide UI
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        timeTxt = (TextView) findViewById(R.id.timeTxt);
        dateTxt = (TextView) findViewById(R.id.dateTxt);
        curTempTxt = (TextView) findViewById(R.id.tempTxt);
        highTempTxt = (TextView) findViewById(R.id.highTempTxt);
        lowTempTxt = (TextView) findViewById(R.id.lowTempTxt);
        tempIcon = (ImageView) findViewById(R.id.weatherImg);

        queue = Volley.newRequestQueue(this);
        bus = new Bus();
        // Attach handlers to the bus
        bus.register(this);
        new WeatherHandler(bus, this, queue);
        new TimeHandler(bus);
        new LocationHandler(bus, this);
        new FaceDetector(bus, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLive = true;
        bus.post(new ResumeEvent());
        liveUpdater.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isLive = false;
        bus.post(new PauseEvent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
        bus = null;
        queue = null;
    }

    @Subscribe
    public void onTimeUpdate(TimeEvent evt) {
        dateTxt.setText(evt.dateStr);
        timeTxt.setText(evt.timeStr);
    }

    @Subscribe
    public void onWeatherUpdate(WeatherEvent evt) {
        Resources r = getResources();
        String units = r.getString(R.string.celsius);
        curTempTxt.setText(String.format(r.getString(R.string.curTempTxt), Math.round(evt.currentTemperature), units));
        highTempTxt.setText(String.format(r.getString(R.string.highTempTxt), Math.round(evt.highTemperature), units));
        lowTempTxt.setText(String.format(r.getString(R.string.lowTempTxt), Math.round(evt.lowTemperature), units));
        tempIcon.setImageResource(icons.get(evt.icon));
    }

    @Subscribe
    public void onFaceFound(FaceFoundEvent evt) {
        setBrightness(1.0f);
    }

    @Subscribe
    public void onFaceLost(FaceLostEvent evt) {
        setBrightness(0.1f);
    }

    private void setBrightness(float brightness) {
        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS,
                (int) (brightness * 255));
    }
}
