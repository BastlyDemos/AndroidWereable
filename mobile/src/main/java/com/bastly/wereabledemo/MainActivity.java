package com.bastly.wereabledemo;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bastly.bastlysdk.Bastly;
import com.bastly.bastlysdk.interfaces.MessageListener;
import com.bastly.wereabledemo.models.Sound;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, MessageListener<Sound> {

    private GoogleApiClient mGoogleApiClient;
    private Bastly<Sound> bastly;
    private String Z_WAVE = "com.bastly.Z.count";
    private String Y_WAVE = "com.bastly.Y.count";
    private String X_WAVE = "com.bastly.X.count";
    private String TAG = MainActivity.class.getName();

    private static final String APIKEY = "6bdfe310-0db3-11e5-8f7e-095df2532ac5";
    private String CHANNEL = "DEMOMHD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bastly = new Bastly("wearable", APIKEY, this, Sound.class);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    protected void onResume() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Log.d(TAG, "connected to wearrrr");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "discconn to wear");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "discconn to wear");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "EVENT RECEIVED");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                int max = 0;
                String maxString = "";
                if (dataMap.containsKey(Y_WAVE)) maxString = "1";
                if (dataMap.containsKey(X_WAVE)) maxString = "2";
                if (dataMap.containsKey(Z_WAVE)) maxString = "3";

                Log.d(TAG, "RECEIVED UPDATED " + maxString);
                bastly.send(CHANNEL, new Sound("weargtable", maxString));
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    @Override
    public void onMessageReceived(String s, Sound sound) {

    }
}
