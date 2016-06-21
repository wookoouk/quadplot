//package com.wookoouk.quadplot;
//
//import android.content.Context;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.util.Log;
//import android.view.View;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.ArrayAdapter;
//import android.widget.ListAdapter;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.o3dr.android.client.ControlTower;
//import com.o3dr.android.client.Drone;
//import com.o3dr.android.client.interfaces.DroneListener;
//import com.o3dr.android.client.interfaces.TowerListener;
//import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
//import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
//import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
//import com.o3dr.services.android.lib.drone.connection.ConnectionType;
//
//import java.util.ArrayList;
//
//public class MainActivityOLD extends AppCompatActivity implements DroneListener, TowerListener {
//
//    private static final String TAG = MainActivityOLD.class.getSimpleName();
//    private static final int MinimumGPSAccuracy = 50; //lower is better
//    private ArrayAdapter<String> adapter;
//    private ListView mListView;
//    private TextView gpsText;
//    private Location currentLocation;
//    private FloatingActionButton fab;
//    private ArrayList<String> listItems = new ArrayList<String>();
//    private ArrayList<Location> locations = new ArrayList<Location>();
//    private Drone drone;
//    private ControlTower controlTower;
//    private final Handler handler = new Handler();
//    private static final int DEFAULT_USB_BAUD_RATE = 57600;
//
//
//    private boolean viewingSetup = true;
//    private boolean viewingPlan = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        viewSetup();
//    }
//
//
//    private void viewSetup() {
//        viewingSetup = true;
//        viewingPlan = false;
//        setContentView(R.layout.setup);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        fab = (FloatingActionButton) findViewById(R.id.btnConnect);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onBtnConnectTap();
//            }
//        });
//
//        final Context context = getApplicationContext();
//        this.controlTower = new ControlTower(context);
//        this.drone = new Drone();
//
//        if (this.drone.isConnected()) {
//            viewPlan(); //TODO this.drone.isConnected() is not ALWAYS correct
//        }
//    }
//
//    private void viewPlan() {
//        viewingPlan = true;
//        viewingSetup = false;
//        setContentView(R.layout.activity_main);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                addToList(view);
//            }
//        });
//
//        if (mListView == null) {
//            mListView = (ListView) findViewById(R.id.plot_list);
//        }
//
//        listItems.add("Take Off");
//        listItems.add("Return to land");
//
//        adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1,
//                listItems);
//        setListAdapter(adapter);
//
//        initGPS();
//
//    }
//
//    private void initGPS() {
//
//        gpsText = (TextView) findViewById(R.id.gps_text);
//        gpsText.setText(getText(R.string.gps_connecting));
//
//
//        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        LocationListener locationListener = new LocationListener() {
//
//            public void onLocationChanged(Location location) {
//                gpsText.setText(getText(R.string.gps_label) + " " + location.getAccuracy());
//                currentLocation = location;
//            }
//
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//            }
//
//            public void onProviderEnabled(String provider) {
//            }
//
//            public void onProviderDisabled(String provider) {
//            }
//        };
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//    }
//
//
//    private void addToList(View v) {
//
//        if (currentLocation != null) {
//            if (currentLocation.getAccuracy() < MinimumGPSAccuracy) {
//                listItems.add(listItems.size() - 1, "Plot " + (locations.size() + 1));
//                locations.add(currentLocation);
//                adapter.notifyDataSetChanged();
//            } else {
//                Snackbar.make(v, currentLocation.getAccuracy() + "/" + MinimumGPSAccuracy, Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        } else {
//            Snackbar.make(v, getText(R.string.gps_connecting), Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
//        }
//    }
//
//    private ListView getListView() {
//        if (mListView == null) {
//            mListView = (ListView) findViewById(R.id.plot_list);
//        }
//        return mListView;
//    }
//
//    private void setListAdapter(ListAdapter adapter) {
//        getListView().setAdapter(adapter);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) { //TODO reset plots
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) { //TODO reset plots
//
//        viewSetup();
//
//        int id = item.getItemId();
//        return id == R.id.action_settings || super.onOptionsItemSelected(item);
//    }
//
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        this.controlTower.connect(this);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        if (this.drone.isConnected()) {
//            this.drone.disconnect();
//            updateConnectedButton(false);
//        }
//
//        this.controlTower.unregisterDrone(this.drone);
//        this.controlTower.disconnect();
//    }
//
//    @Override
//    public void onDroneConnectionFailed(ConnectionResult result) {
//        alertUser("Connection Failed:" + result.getErrorMessage());
//        updateConnectedButton(this.drone.isConnected());
//    }
//
//
//    @Override
//    public void onDroneEvent(String event, Bundle extras) {
//
//        switch (event) {
//            case AttributeEvent.STATE_CONNECTED:
//                alertUser("Drone Connected");
//                updateConnectedButton(this.drone.isConnected());
//                break;
//
//            case AttributeEvent.STATE_DISCONNECTED:
//                alertUser("Drone Disconnected");
//                updateConnectedButton(this.drone.isConnected());
//                break;
//
//            default:
//                Log.i("DRONE_EVENT", event); //Uncomment to see events from the drone
//                break;
//        }
//    }
//
//
//    private void alertUser(String message) {
//        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//        Log.d(TAG, message);
//    }
//
//    @Override
//    public void onDroneServiceInterrupted(String errorMsg) {
//        updateConnectedButton(this.drone.isConnected());
//    }
//
//    @Override
//    public void onTowerConnected() {
//        alertUser("3DR Services Connected");
//        this.controlTower.registerDrone(this.drone, this.handler);
//        this.drone.registerDroneListener(this);
//        updateConnectedButton(this.drone.isConnected());
//    }
//
//    @Override
//    public void onTowerDisconnected() {
//        updateConnectedButton(this.drone.isConnected());
//        alertUser("3DR Service Interrupted");
//    }
//
//    private void onBtnConnectTap() {
//        if (this.drone.isConnected()) {
//            this.drone.disconnect();
//        } else {
//            Bundle extraParams = new Bundle();
//            extraParams.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, DEFAULT_USB_BAUD_RATE); // Set default baud rate to 57600
//
//            ConnectionParameter connectionParams = new ConnectionParameter(0, extraParams, null);
//            this.drone.connect(connectionParams);
//        }
//    }
//
//
//    private void updateConnectedButton(Boolean isConnected) {
//
//        if (viewingSetup) {
//            FloatingActionButton connectButton = (FloatingActionButton) findViewById(R.id.btnConnect);
//            if (isConnected) {
//                connectButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_network_connected_icon_36dp));
//                if (!viewingPlan) {
//                    viewPlan();
//                }
//            } else {
//                connectButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_network_disconnected_icon_36dp));
//                if (!viewingSetup) {
//                    viewSetup();
//                }
//            }
//        }
//    }
//}