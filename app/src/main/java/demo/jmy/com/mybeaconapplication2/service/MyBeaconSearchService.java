package demo.jmy.com.mybeaconapplication2.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.skybeacon.sdk.RangingBeaconsListener;
import com.skybeacon.sdk.ScanServiceStateCallback;
import com.skybeacon.sdk.locate.SKYBeacon;
import com.skybeacon.sdk.locate.SKYBeaconManager;
import com.skybeacon.sdk.locate.SKYRegion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import demo.jmy.com.mybeaconapplication2.bean.SendInfoBean;
import demo.jmy.com.mybeaconapplication2.util.DataSendUtil;
import demo.jmy.com.mybeaconapplication2.util.SystemInfoUtil;

public class MyBeaconSearchService extends Service {
    private static final SKYRegion ALL_SEEKCY_BEACONS_REGION = new SKYRegion("rid_all", null, null, null, null);
    private SKYBeaconManager skyBeaconManager;

    private String imei = "";
    private String id = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        imei = SystemInfoUtil.getIMEI(this);
        id = SystemInfoUtil.getDeviceId(imei);

        skyBeaconManager = SKYBeaconManager.getInstance();
        skyBeaconManager.init(this);
        skyBeaconManager.setCacheTimeMillisecond(2000);
        skyBeaconManager.setScanTimerIntervalMillisecond(1000);
        skyBeaconManager.setDecryptScan(true);

        skyBeaconManager.setRangingBeaconsListener(new RangingBeaconsListener() {

            @Override
            public void onRangedBeacons(SKYRegion skyRegion, List list) {
                Collections.sort(list, new Comparator() {
                    @Override
                    public int compare(Object o, Object t1) {
                        SKYBeacon skyBeacon = (SKYBeacon) o;
                        SKYBeacon skyBeacon1 = (SKYBeacon) t1;
                        return skyBeacon1.getRssi() - skyBeacon.getRssi();
                    }
                });

                List<SendInfoBean.BeaconInfo> sendData = new ArrayList<>();
                Log.e("test", "--------------------");
                Log.e("test", id);
                for (int i = 0; i < list.size(); i++) {
                    SKYBeacon skyBeacon = (SKYBeacon) list.get(i);
                    Log.e("test", skyBeacon.getMajor() + " " +
                            skyBeacon.getMinor() + " " + skyBeacon.getRssi() + " " +
                            skyBeacon.getDistance());
                }
                Log.e("test", "--------------------");

                if(list.size()<3){
                    return;
                }
                int count = 0;
                for (int i = 0; i < list.size(); i++) {
                    if(count>=3)
                        break;
                    SKYBeacon skyBeacon = (SKYBeacon) list.get(i);

                    if(skyBeacon.getMajor()!=20000&&skyBeacon.getMajor()!=10002){
                        continue;
                    }
                    SendInfoBean.BeaconInfo data = new SendInfoBean.BeaconInfo(skyBeacon.getMajor(), skyBeacon.getMinor(), skyBeacon.getRssi(), skyBeacon.getMeasuredPower());
                    sendData.add(data);
                    count++;
                }
                if (sendData.size() == 3) {
                    String msg = listToJson(sendData);
                    DataSendUtil.sendData(msg.getBytes());
                }
            }

            @Override
            public void onRangedBeaconsMultiIDs(SKYRegion skyRegion, List list) {
                Log.e("test", list.size() + "");
            }

            @Override
            public void onRangedNearbyBeacons(SKYRegion skyRegion, List list) {
                Log.e("test", list.size() + "");
            }
        });

        startRanging();
    }

    private String listToJson(List<SendInfoBean.BeaconInfo> sendData) {
        SendInfoBean data = new SendInfoBean();
        data.setId(id);
        data.setData(sendData);
        return new Gson().toJson(data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRanging();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startRanging() {
        skyBeaconManager.startScanService(new ScanServiceStateCallback() {
            @Override
            public void onServiceDisconnected() {
            }

            @Override
            public void onServiceConnected() {
                skyBeaconManager.startRangingBeacons(ALL_SEEKCY_BEACONS_REGION);
            }
        });
    }

    private void stopRanging() {
        if (skyBeaconManager != null) {
            skyBeaconManager.stopScanService();
            skyBeaconManager.stopRangingBeasons(ALL_SEEKCY_BEACONS_REGION);
        }
    }
}
