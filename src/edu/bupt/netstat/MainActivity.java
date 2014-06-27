package edu.bupt.netstat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import edu.bupt.netstat.pcap.DumpHelper;

/**
 * MainActivity
 * 
 * @author zzz
 * 
 */
public class MainActivity extends Activity implements OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int MSG_UPDATE_IPLIST = 1;

    private TextView tvFileLength;
    private Button btDumpStart;
    private Button btDumpStop;
    private Button btAnalyze;
    private Spinner spPkgType;
    private Spinner spPkgName;
    private TextView tvIpList;
    private ArrayAdapter adapterPkgType;
    private SimpleAdapter adapterPkgName;
    private List<HashMap<String, Object>> appList;
    private int appSelected = 0;

    private DumpHelper helper;

    /**
     * @author zzz
     * 
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvFileLength = (TextView) findViewById(R.id.textview_file_length);
        tvIpList = (TextView) findViewById(R.id.textview_ip_list);

        btDumpStart = (Button) findViewById(R.id.button_dump_start);
        btDumpStop = (Button) findViewById(R.id.button_dump_stop);
        btAnalyze = (Button) findViewById(R.id.button_analyze);

        btDumpStart.setOnClickListener(this);
        btDumpStop.setOnClickListener(this);
        btAnalyze.setOnClickListener(this);
        btAnalyze.setClickable(false);

        spPkgType = (Spinner) findViewById(R.id.spinner_pkg_type);
        adapterPkgType = ArrayAdapter.createFromResource(this,
                R.array.pkg_types, android.R.layout.simple_spinner_item);
        adapterPkgType
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPkgType.setAdapter(adapterPkgType);

        appList = getInstalledApps(false);
        spPkgName = (Spinner) findViewById(R.id.spinner_pkg_name);
        adapterPkgName = new SimpleAdapter(this, appList,
                android.R.layout.simple_list_item_1,
                new String[] { "app_title" }, new int[] { android.R.id.text1 });
        spPkgName.setAdapter(adapterPkgName);
        spPkgName.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                appSelected = arg2;
                Log.v(TAG,
                        "selected package "
                                + appList.get(arg2).get("app_package")
                                        .toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    /**
     * @author zzz
     * 
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * @author zzz
     * 
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @author zzz
     * 
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button_dump_start:
            handler.post(runnable);
            helper = new DumpHelper(MainActivity.this, appList.get(appSelected)
                    .get("app_package").toString());
            helper.startCapture();

            btAnalyze.setClickable(true);
            break;
        case R.id.button_dump_stop:
            handler.removeCallbacks(runnable);
            helper.stopCapture();
            break;
        case R.id.button_analyze:
            Intent i = new Intent(MainActivity.this,
                    NetQualityIndicatorsActivity.class);
            i.putExtra(NetQualityIndicatorsActivity.PKG_TYPE,
                    spPkgType.getSelectedItemPosition());
            i.putExtra(NetQualityIndicatorsActivity.LOCALIP,
                    helper.getLocalIp());
            startActivity(i);
            break;
        default:
            Log.w(TAG, "unknow button");
        }
    }

    /**
     * @author zzz
     * 
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_IPLIST:
                if (helper.getIpSet().isEmpty()) {
                    tvIpList.setText("Null");
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (String s : helper.getIpSet()) {
                        sb.append(s).append("\n");
                    }
                    tvIpList.setText(sb.toString());
                }
                tvFileLength.setText(String.valueOf(helper
                        .getCaptureFileLength()) + " B");
                break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * @author zzz
     * 
     */
    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            Log.v(TAG, "runnable.run");
            helper.updateIpSet();
            Message m = new Message();
            m.what = MSG_UPDATE_IPLIST;
            handler.sendMessage(m);
            handler.postDelayed(runnable, 1000);
        }

    };

    /**
     * @author zzz
     * 
     */
    private List<HashMap<String, Object>> getInstalledApps(
            boolean getSysPackages) {
        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

        List<PackageInfo> pkgs = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < pkgs.size(); i++) {
            PackageInfo pkg = pkgs.get(i);
            if (!getSysPackages
                    && (pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                continue;
            }
            String label = pkg.applicationInfo.loadLabel(getPackageManager())
                    .toString();
            String version = pkg.versionName;
            String packageName = pkg.packageName;
            // Drawable icon =
            // pkg.applicationInfo.loadIcon(getPackageManager());

            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("app_title", label + " " + version);
            map.put("app_package", packageName);
            // map.put("app_icon", icon);
            Log.v(TAG, "find package " + packageName);
            list.add(map);
        }
        return list;
    }
}
