package com.example.ticknardif.hotspot;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends Activity implements ChatroomOverlay.OnFragmentInteractionListener{

    //Variables for registering with GCM
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String TAG = "GCM";

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String SENDER_ID = "77761935104";

    private GoogleCloudMessaging gcm;
    private AtomicInteger msgId = new AtomicInteger();
    private SharedPreferences prefs;
    private Context context;
    private String regid;
    private RestAdapter restAdapter;
    private  WebService webService;

    private GoogleMap map;
    private LatLng myLatLng;

    private String session;

    private SharedPreferences sharedPref;

    private Callback<List<ChatroomResponse>> chatroomResponseCallback =  new Callback<List<ChatroomResponse>>() {
        @Override
        public void success(List<ChatroomResponse> chatroomList, Response response) {
            Log.d("Debug", "Successful finding nearby chatrooms");

            // Display the chatrooms we received on the screen
            ChatroomListAdapter adapter = new ChatroomListAdapter(getBaseContext(), R.layout.chatroom_list_item);
            ListView listView = (ListView) getFragmentManager().findFragmentById(R.id.chatroom_overlay_fragment).getView();
            listView.setAdapter(adapter);

            for(ChatroomResponse item: chatroomList) {
                Log.d("Debug", "Item is: " + item.toString());

                Chatroom chatroom = responseToChatroom(item);
                adapter.add(chatroom);
                addChatroomToMap(chatroom);
            }
        }
        @Override
        public void failure(RetrofitError error) {
            Log.e("Debug", error.toString());
            Log.d("Debug", "Failure finding nearby chatrooms");
        }

        private Chatroom responseToChatroom(ChatroomResponse response) {
            Log.d("Debug" , "Response is: " + response.toString());
            return new Chatroom(response.chat_id, response.Room_Admin, response.Latitude, response.Longitude, response.Chat_title, response.Chat_Dscrpn);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        restAdapter = new RestAdapter.Builder()
                .setServer("http://54.172.35.180:8080")
                .build();
        webService = restAdapter.create(WebService.class);
        context = getApplicationContext();
        Bundle bundle = this.getIntent().getExtras();
        session = bundle.getString("session");
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            Log.i("reg_id Pref",regid);
            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        Bundle extras = getIntent().getExtras();
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.main_map)).getMap();

        Context context = getBaseContext();
        sharedPref = context.getSharedPreferences(getString(R.string.shared_pref_file), Context.MODE_PRIVATE);
        String email = sharedPref.getString(getString(R.string.shared_pref_email), "No Email Set");
        String password = sharedPref.getString(getString(R.string.shared_pref_password), "No Password Set");
        String sessionID = sharedPref.getString(getString(R.string.shared_pref_session_id), "No SessionID Set");

        // If there is a location stored in the preferences, center the map on it
        if(getLocationFromPreferences()) {
           setLocation();
        }

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                setLocation();

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong("Latitude", Double.doubleToRawLongBits(location.getLatitude()));
                editor.putLong("Longitude", Double.doubleToRawLongBits(location.getLongitude()));
                editor.apply();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);

        Fragment overlayFragment = getFragmentManager().findFragmentById(R.id.chatroom_overlay_fragment);

        Log.d("Debug", "SessionID is: " + sessionID);
        webService.getChatrooms(sessionID, chatroomResponseCallback);

    }

    public void setLocation() {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
    }

    private boolean getLocationFromPreferences() {
        boolean success = false;
        Long lat = sharedPref.getLong("Latitude", 0);
        Long lng = sharedPref.getLong("Longitude", 0);
        if(lat != 0 && lng !=0) {
            Log.d("Debug", "Retrieved GPS coordinates from SharedPreferences");
            myLatLng = new LatLng(Double.longBitsToDouble(lat), Double.longBitsToDouble(lng));
            success = true;
        }

        return success;
    }

    // You need to do the Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    public void setLocation(Location location) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
    }

    private void addChatroomToMap(Chatroom chatroom) {
        LatLng location = new LatLng(chatroom.getLat(), chatroom.getLng());
        String title = chatroom.getTitle();
        map.addMarker(new MarkerOptions().position(location).title(title));
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("Debug", "Calling onFragmentInteraction");
        Log.d("Debug", "URI is :" + uri.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
        Log.e("Session",session);
        Log.e("Registered: ",regid);
        webService.regGCM(session, regid, new Callback<GCMResponse>() {
            @Override
            public void success(GCMResponse gcmResponse, Response response) {
                Log.d("GCM Success: ", gcmResponse.toString());
            }

            @Override
            public void failure(RetrofitError error)  {

                Log.e("HTTP Error", error.toString());
            }
        });
    }
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i("GCM","Registered with GCM!");
            }
        }.execute(null, null, null);
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        // Remove this activity from the Activity stack so the user cannot go back to this
        finish();
    }
}
