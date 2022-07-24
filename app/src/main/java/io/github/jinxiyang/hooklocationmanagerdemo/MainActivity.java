package io.github.jinxiyang.hooklocationmanagerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.jinxiyang.requestpermission.PermissionRequester;
import io.github.jinxiyang.requestpermission.utils.PermissionUtils;

public class MainActivity extends AppCompatActivity {

    private TextView tvLocationInfo;
    private Button btnLocate;

    private LocationManager mLocationManager = null;
    private LocationListener mLocationListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLocationInfo = findViewById(R.id.tvLocationInfo);
        btnLocate = findViewById(R.id.btnLocate);

        btnLocate.setOnClickListener(v -> startLocate());
    }

    private void startLocate() {
        if (mLocationManager != null) {
            Toast.makeText(this, "定位中……", Toast.LENGTH_SHORT).show();
            return;
        }

        new PermissionRequester(this)
                .addPermissions(PermissionUtils.LOCATION_PERMISSIONS)
                .request(permissionResult -> {
                    if (permissionResult.granted()) {
                        locate();
                    } else {
                        Toast.makeText(this, "未获取定位权限", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void locate() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "位置信息开关（GPS）未开启", Toast.LENGTH_SHORT).show();
            return;
        }
        mLocationListener = location -> {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.i("======", "onLocationChanged: " + latitude + "," + longitude);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            StringBuilder sb = new StringBuilder();
            sb.append("时间：");
            sb.append(format.format(new Date()));
            sb.append("位置：");
            sb.append(latitude + "," + longitude);
            tvLocationInfo.setText(sb);
        };
        mLocationManager = locationManager;
        registerLocationListener();
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerLocationListener();
    }

    @SuppressLint("MissingPermission")
    private void registerLocationListener() {
        if (mLocationManager != null && mLocationListener != null) {
            //单次定位
            //mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, Looper.myLooper());

            //连续定位
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, mLocationListener);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationManager != null && mLocationListener != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}