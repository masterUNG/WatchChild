package mind.chan.mean.watchchild;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends Activity {

    private TextView mTextView;
    private MyManage myManage;
    private LocationManager locationManager;
    private Criteria criteria;
    private double latADouble = 13.711166, lngADouble = 100.581848;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create SQLite Database
        mySetup();

        //Check Database
        checkDatabase();


    }   // Main Method



    @Override
    protected void onStop() {
        super.onStop();

        locationManager.removeUpdates(locationListener);

    }

    public Location myFindLocation(String strProvider) {

        Location location = null;

        if (locationManager.isProviderEnabled(strProvider)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            locationManager.requestLocationUpdates(strProvider, 1000, 10, locationListener);
        }

        return location;
    }


    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latADouble = location.getLatitude();
            lngADouble = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    private void mySetup() {
        myManage = new MyManage(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
    }

    private void showText(final String strShowText) {
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mTextView.setText(strShowText);
            }
        });
    }

    private void checkDatabase() {
        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(MyOpenHelper.database_name,
                MODE_PRIVATE, null);
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM childTABLE", null);
        cursor.moveToFirst();

        if (cursor.getCount() == 0) {
            //Data Blank ==> Start First
            startFirst();

        } else {
            haveData();
        }

    }

    private void haveData() {

        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(MyOpenHelper.database_name,
                MODE_PRIVATE, null);
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM childTABLE", null);
        cursor.moveToFirst();
        String strCode = cursor.getString(1);
        Log.d("12JulyV1", "Code ==> " + strCode);
        showText(strCode);

        myLoop(strCode);


    }

    private void myLoop(final String strCode) {

        //ToDo
        String tag = "12JulyV1";
        Log.d(tag, "Find ==> " + strCode);


        try {

            GetChildWhereCode getChildWhereCode = new GetChildWhereCode(this);
            getChildWhereCode.execute(strCode);
            Log.d(tag, "Result getChild ==> " + getChildWhereCode.get());

            if (getChildWhereCode.get().length() != 4) {

                //Have Parent
                findLatLng();
                Log.d(tag, "Lat ==> " + latADouble);
                Log.d(tag, "Lng ==> " + lngADouble);

                updateLocation(strCode);


            }   // if


        } catch (Exception e) {
            Log.d(tag, "e myLoop ==> " + e.toString());
        }







        //Delay
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myLoop(strCode);
            }
        }, 1000);


    }

    private void updateLocation(String strCode) {

        try {

            EditLocation editLocation = new EditLocation(this);
            editLocation.execute(strCode,
                    Double.toString(latADouble),
                    Double.toString(lngADouble));

            Log.d("12JulyV1", "Result from update Location ==> " + editLocation.get());

            showText("Success");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void findLatLng() {

        Location netWorkLocation = myFindLocation(LocationManager.NETWORK_PROVIDER);
        if (netWorkLocation != null) {
            latADouble = netWorkLocation.getLatitude();
            lngADouble = netWorkLocation.getLongitude();
        }

        Location gpsLocation = myFindLocation(LocationManager.GPS_PROVIDER);
        if (gpsLocation != null) {
            latADouble = gpsLocation.getLatitude();
            lngADouble = gpsLocation.getLongitude();
        }

    }

    private void startFirst() {

        Random random = new Random();
        int i = random.nextInt(100000);
        Log.d("12JulyV1", "i ==> " + i);

        myManage.addValue(Integer.toString(i));

        showText("Code = " + Integer.toString(i));

    }

}   // Main Class
