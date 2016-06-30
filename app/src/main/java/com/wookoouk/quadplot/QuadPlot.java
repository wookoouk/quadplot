package com.wookoouk.quadplot;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

interface DroneConnectionChangedListener {
    void onDroneConnectionChanged();
}

interface LocationChangedListener {
    void onLocationChanged();
}

public class QuadPlot extends Application implements DroneListener, TowerListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    static ArrayList<Plot> plots = new ArrayList<>();
    private static Boolean DroneConnected = false;

    private static List<DroneConnectionChangedListener> droneListeners = new ArrayList<>();
    private static List<LocationChangedListener> LocationListeners = new ArrayList<>();

    static final int DEFAULT_USB_BAUD_RATE = 57600;
    static int baseHeight = 20;
    private static Location currentLocation;

    private final Handler handler = new Handler();
    public static Drone drone;
    private static ControlTower controlTower;

    static boolean getIsDroneConnected() {
        return DroneConnected;
    }

    private void initGPS() {

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                SetCurrentLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("ERROR", "COULD NOT USE GPS");
            return;
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    private static void SetDroneConnected(boolean value) {
        DroneConnected = value;

        for (DroneConnectionChangedListener l : droneListeners) {
            l.onDroneConnectionChanged();
        }
    }

    private static void SetCurrentLocation(Location loc) {
        currentLocation = loc;

        for (LocationChangedListener l : LocationListeners) {
            l.onLocationChanged();
        }
    }


    static void addDroneConnectionListener(DroneConnectionChangedListener l) {
        droneListeners.add(l);
    }

    static void addLoadionListener(LocationChangedListener l) {
        LocationListeners.add(l);
    }


    @Override
    public void onCreate() {
        super.onCreate();

        final Context context = getApplicationContext();

        controlTower = new ControlTower(context);
        drone = new Drone(this); // Later version will use context
        controlTower.connect(this);

        initGPS();

//        try {
//            ArrayList<Plot> testPlots = Util.loadPlots(PreferenceManager.getDefaultSharedPreferences(this));
//            if (testPlots != null) {
//                plots = testPlots;
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "ON TERMINATE");
        super.onTerminate();

        if (drone.isConnected()) {
            drone.disconnect();
//            updateConnectedButton(false);
        }

        controlTower.unregisterDrone(drone);
        controlTower.disconnect();


//        try {
//            Util.storePlots(PreferenceManager.getDefaultSharedPreferences(this), plots);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onDroneConnectionFailed(ConnectionResult result) {
        String errorMsg = result.getErrorMessage();
        Toast.makeText(getApplicationContext(), "Connection failed: " + errorMsg,
                Toast.LENGTH_LONG).show();

//        lbm.sendBroadcast(new Intent(ACTION_DRONE_CONNECTION_FAILED)
//                .putExtra(EXTRA_CONNECTION_FAILED_ERROR_CODE, result.getErrorCode())
//                .putExtra(EXTRA_CONNECTION_FAILED_ERROR_MESSAGE, result.getErrorMessage()));
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_CONNECTED: {
                SetDroneConnected(true);

                Toast.makeText(getApplicationContext(), "Drone Connected",
                        Toast.LENGTH_LONG).show();

                break;
            }

            case AttributeEvent.STATE_DISCONNECTED: {
                SetDroneConnected(false);

                Toast.makeText(getApplicationContext(), "Drone Disconnected",
                        Toast.LENGTH_LONG).show();

                break;
            }

            default: {
                break;
            }
        }
    }


    @Override
    public void onDroneServiceInterrupted(String errorMsg) {
        controlTower.unregisterDrone(drone);

        if (!TextUtils.isEmpty(errorMsg))
            Log.e(TAG, errorMsg);
    }

    @Override
    public void onTowerConnected() {

        drone.unregisterDroneListener(this);

        controlTower.registerDrone(drone, handler);
        drone.registerDroneListener(this);


        Toast.makeText(getApplicationContext(), "ControlTower Connected",
                Toast.LENGTH_LONG).show();

//        SetUSBConnected(true);
    }

    @Override
    public void onTowerDisconnected() {
//        SetUSBConnected(false);
        Toast.makeText(getApplicationContext(), "ControlTower Disconnected",
                Toast.LENGTH_LONG).show();
    }

    static Location getCurrentLocation() {
        return currentLocation;
    }
}