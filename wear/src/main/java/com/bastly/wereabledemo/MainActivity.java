package com.bastly.wereabledemo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getName();
    private TextView mTextView;
    private SensorManager sensorManager;
    private Sensor mSensor;
    private long timeStampSec;
    private float xLast;
    private float yLast;
    private float zLast;
    private float DIFF = (float) 2.5;
    private long lastTimeStamp;
    private GoogleApiClient mGoogleApiClient;
    private String Z_WAVE = "com.bastly.Z.count";
    private String Y_WAVE = "com.bastly.Y.count";
    private String X_WAVE = "com.bastly.X.count";
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];


        timeStampSec = System.currentTimeMillis();
        if (x - xLast > DIFF && (timeStampSec - lastTimeStamp) > 200) {
            Log.d(TAG, " HARDER " + (x - xLast));
            lastTimeStamp = timeStampSec;
            increaseCounter(X_WAVE);
        }

        if (y - yLast > DIFF && (timeStampSec - lastTimeStamp) > 200) {
            Log.d(TAG, " BETTER" + (y - yLast));
            lastTimeStamp = timeStampSec;
            increaseCounter(Y_WAVE);
        }
        if (z - zLast > DIFF && (timeStampSec - lastTimeStamp) > 200) {
            Log.d(TAG, " FASTER " + (z - zLast));
            lastTimeStamp = timeStampSec;
            increaseCounter(Z_WAVE);
        }

        xLast = x;
        yLast = y;
        zLast = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    // Create a data map and put data in it
    private void increaseCounter(String wave) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count");
        putDataMapReq.getDataMap().putInt(wave, count++);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d(TAG, "sending " + wave + count);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "connected to phone");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
