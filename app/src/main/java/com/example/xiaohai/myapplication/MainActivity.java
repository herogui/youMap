package com.example.xiaohai.myapplication;


        import android.Manifest;
        import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;

        import android.content.pm.PackageManager;
        import android.graphics.Canvas;
        import android.location.LocationManager;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;

        import android.preference.PreferenceManager;
        import android.provider.Settings;

        import android.os.Bundle;

        import android.support.annotation.NonNull;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
        import android.util.Log;
        import android.view.MotionEvent;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;


        import com.example.geoConverter;
        import com.example.myLatLng;
        import com.example.xiaohai.myapplication.MyEvent.CusEvent;
        import com.example.xiaohai.myapplication.MyEvent.CusEventListener;
        import com.example.xiaohai.myapplication.MyEvent.EventSourceObject;


        import org.osmdroid.api.IGeoPoint;
        import org.osmdroid.api.IMapController;
        import org.osmdroid.config.Configuration;
        import org.osmdroid.events.MapEventsReceiver;
        import org.osmdroid.events.MapListener;
        import org.osmdroid.events.ScrollEvent;
        import org.osmdroid.events.ZoomEvent;
        import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
        import org.osmdroid.util.GeoPoint;
        import org.osmdroid.views.MapView;
        import org.osmdroid.views.Projection;
        import org.osmdroid.views.overlay.*;

        import java.util.ArrayList;

public class MainActivity extends Activity {

   public   MapView map;
    private IMapController mapController;
    GPSManager gps;
    GpsStatusManager gpsStatus;
    NetworkLbsManager NetLbs;
    ItemizedOverlay<OverlayItem> currentItemItemizedOverlay;
    TextView txtGPSState;
    MyRoute route;
    Distance dis;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();

        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);

        map = (MapView) findViewById(R.id.map);
       // map.getController().
        route = new MyRoute(this.map);

        //测距离
        dis = new Distance(MainActivity.this);


        initUI();

        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        else
        {
            init();
        }
    }

    void  init()
    {
        if(isNetworkAvailable(MainActivity.this))
        {
            NetLbs = NetworkLbsManager.instance(MainActivity.this);
            NetLbs.GetResEventInit.addCusListener(cusEventLbs);
            NetLbs.startGpsLocate();
        }
        else
        {
            // Toast.makeText(MainActivity.this,"请打�?网络，用于初始化地图位置!", Toast.LENGTH_LONG).show();
            GeoPoint gp5 = new GeoPoint(23.149679,113.145819);
            final ArrayList<OverlayItem> items = new ArrayList<>();
            items.add(new OverlayItem("Hannover", "SampleDescription",gp5));
            AddOverLay(items);
            initMap(gp5);
        }

        if(openGPSSettings()) {
            gps = GPSManager.instance(MainActivity.this);
            //定位
            gpsStatus = GpsStatusManager.instance(MainActivity.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission Denied
            }
        }
    }

    void initUI()
    {
        txtGPSState = (TextView)this.findViewById(R.id.txtGPSState);
        txtGPSState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dis.clear();
            }
        });

        Button btn = (Button)this.findViewById(R.id.btnDis);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button)v;
                if(btn.getText()=="开始测距") {
                    dis.start();
                    btn.setText("结束测距");
                }
                else
                {
                    double distance =  dis.stop();
                    Toast.makeText(MainActivity.this.getApplication(),String.valueOf(distance),Toast.LENGTH_LONG).show();
                    btn.setText("开始测距");
                }
            }
        });
    }

    void initMap(GeoPoint center)
    {
        try {
            map.setUseDataConnection(false);
            map.setTileSource(TileSourceFactory.getTileSource("Mapnik"));

            mapController = map.getController();

            map.setMultiTouchControls(true);
            map.setBuiltInZoomControls(true);
            map.setClickable(true);
            map.setUseDataConnection(false);

            mapController.setZoom(17);
            mapController.setCenter(center);


        }
        catch (Exception exp)
        {
            Log.d("log",exp.getMessage());
            Toast.makeText(MainActivity.this,exp.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    CusEventListener cusEventLbs =   new CusEventListener() {
        @Override
        public void fireCusEvent(CusEvent e) {
            EventSourceObject eObject = (EventSourceObject) e.getSource();
            String res =eObject.getString();

            //Toast.makeText(MainActivity.this,"lbs"+  res, Toast.LENGTH_LONG).show();

            double lon = Double.parseDouble(res.split(",")[0]);
            double lat = Double.parseDouble(res.split(",")[1]);

            myLatLng latLng =  geoConverter.toGooglePoint(lat, lon);
            GeoPoint  center = new GeoPoint(latLng.getLatitude(),latLng.getLongitude());
            initMap(center);

            //marker
            final ArrayList<OverlayItem> overlayItems = new ArrayList<>();
            OverlayItem myLocationOverlayItem = new OverlayItem("提示", "当前位置", center);
            myLocationOverlayItem.setMarker(MainActivity.this.getResources().getDrawable(R.mipmap.people));
            //overlayItems.add(new OverlayItem("Hannover", "SampleDescription",center));
            overlayItems.add(myLocationOverlayItem);
            AddOverLay(overlayItems);

            //获取初始化的位置后要停止监听
            NetLbs.GetResEvent.removeListener(cusEventLbs);
            NetLbs.closeGpsLocate();

            gps.GetResEvent.addCusListener(cusEventGPS);
            gps.startGpsLocate();

            gpsStatus.GetResEvent.addCusListener(cusEventGPSStatus);
            gpsStatus.start();
        }
    };

    CusEventListener cusEventGPSStatus =   new CusEventListener() {
        @Override
        public void fireCusEvent(CusEvent e) {
            EventSourceObject eObject = (EventSourceObject) e.getSource();
            String res = eObject.getString();
            txtGPSState.setText("接收到"+res+"个GPS");
            //Toast.makeText(MainActivity.this,res, Toast.LENGTH_SHORT).show();
        }
    };

    CusEventListener cusEventGPS =   new CusEventListener() {
        @Override
        public void fireCusEvent(CusEvent e) {
            EventSourceObject eObject = (EventSourceObject) e.getSource();
            String res = eObject.getString();
            Toast.makeText(MainActivity.this,"gps  " +res, Toast.LENGTH_SHORT).show();
            double lon = Double.parseDouble(res.split(",")[0]);
            double lat = Double.parseDouble(res.split(",")[1]);

            myLatLng  latLng =  geoConverter.toGooglePoint(lat, lon);
            GeoPoint  center = new GeoPoint(latLng.getLatitude(),latLng.getLongitude());

            route.addPoint(center);

            final ArrayList<OverlayItem> overlayItems = new ArrayList<>();
            OverlayItem myLocationOverlayItem = new OverlayItem("提示", "当前位置", center);
            myLocationOverlayItem.setMarker(MainActivity.this.getResources().getDrawable(R.mipmap.people));
            //overlayItems.add(new OverlayItem("Hannover", "SampleDescription",center));
            overlayItems.add(myLocationOverlayItem);
            AddOverLay(overlayItems);
        }
    };

    void AddOverLay(ArrayList<OverlayItem> items)
    {

			/* OnTapListener for the Markers, shows a simple Toast. */
        ItemizedOverlay<OverlayItem> itemItemizedOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        Toast.makeText(
                                MainActivity.this,
                                "Item '" + item.getTitle() + "' (index=" + index
                                        + ") got single tapped up", Toast.LENGTH_LONG).show();
                        return true; // We 'handled' this event.
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        Toast.makeText(
                                MainActivity.this,
                                "Item '" + item.getTitle() + "' (index=" + index
                                        + ") got long pressed", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }, MainActivity.this.getApplicationContext());

        //清楚当前�?
        if(currentItemItemizedOverlay!=null)
        map.getOverlayManager().remove(currentItemItemizedOverlay);

        this.map.getOverlays().add(itemItemizedOverlay);
        currentItemItemizedOverlay = itemItemizedOverlay;
    }

    private boolean openGPSSettings() {
        LocationManager alm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Toast.makeText(this, "GPS模块正常" ,Toast.LENGTH_SHORT) .show();
            return true;
        }
        Toast.makeText(this, "请开启GPS�?", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        this.startActivityForResult(intent, 0); //此为设置完成后返回到获取界面
        return  false;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前�?连接的网络可�?
                    return true;
                }
        }
        }
        return false;
    }



    public void onResume() {
        super.onResume();

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }

    public  void onStop()
    {
        super.onStop();
        this.gpsStatus.stop();
        this.gps.closeGpsLocate();
    }
}
