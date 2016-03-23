package ca.taylorlloyd.smartmirror;

import android.content.Context;
import android.hardware.Camera;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.google.android.gms.vision.face.*;

import java.io.IOException;

import ca.taylorlloyd.smartmirror.event.FaceFoundEvent;
import ca.taylorlloyd.smartmirror.event.FaceLostEvent;

/**
 * Created by taylor on 2016-03-18.
 */
public class FaceDetector {
    private static final String TAG = "FaceDetector";
    private Bus bus;
    private Context context;
    private com.google.android.gms.vision.face.FaceDetector detector;

    public FaceDetector(Bus bus, Context context) {
        this.bus = bus;
        this.context = context;
        this.bus.register(this);
        this.bus.post(new FaceLostEvent());

        detector = new com.google.android.gms.vision.face.FaceDetector.Builder(context).build();
        detector.setProcessor(new MultiProcessor.Builder<Face>(new FaceTrackerFactory())
                .build());

        CameraSource source = new CameraSource.Builder(context.getApplicationContext(), detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(5fyi.0f)
                .build();
        try {
            source.start();
        } catch (IOException e) {
            Log.e(TAG, "Error starting the CameraSource", e);
        }
        Log.i(TAG, "Face Detection Started");
    }

    private void postEvent(final Object event) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                bus.post(event);
            }
        });
    }

    private class FaceTrackerFactory
            implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new FaceTracker();
        }
    }

    private class FaceTracker extends Tracker<Face> {
        // other stuff

        @Override
        public void onNewItem(int faceId, Face face) {
            Log.i(TAG, "Found Face");
            postEvent(new FaceFoundEvent());
        }

        @Override
        public void onUpdate(com.google.android.gms.vision.face.FaceDetector.Detections<Face> detectionResults,
                             Face face) {
        }

        @Override
        public void onMissing(com.google.android.gms.vision.face.FaceDetector.Detections<Face> detectionResults) {
            Log.i(TAG, "Lost Face");
            postEvent(new FaceLostEvent());
        }

        @Override
        public void onDone() {
        }
    }
}
