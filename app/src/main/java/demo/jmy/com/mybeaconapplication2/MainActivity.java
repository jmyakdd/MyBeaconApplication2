package demo.jmy.com.mybeaconapplication2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import demo.jmy.com.mybeaconapplication2.service.MyBeaconSearchService;

public class MainActivity extends AppCompatActivity {
    private String[] permissions = new String[]{Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int PERMISSION_REQUEST_RESULT = 121;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String p : permissions) {
                if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                    this.requestPermissions(permissions, PERMISSION_REQUEST_RESULT);
                    return;
                }
            }
        }
        startScanService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_RESULT) {
            for (int rs : grantResults) {
                if (rs != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            startScanService();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    public void startScanService() {
        startService(new Intent(this, MyBeaconSearchService.class));
    }
}
