package edu.bupt.netstat;

import edu.bupt.netstat.pcap.DumpHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    private EditText etPkgName;
    private TextView tvIpList;

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
        etPkgName = (EditText) findViewById(R.id.edittext_pkg_name);
        tvIpList = (TextView) findViewById(R.id.textview_ip_list);

        btDumpStart = (Button) findViewById(R.id.button_dump_start);
        btDumpStop = (Button) findViewById(R.id.button_dump_stop);
        btAnalyze = (Button) findViewById(R.id.button_analyze);

        btDumpStart.setOnClickListener(this);
        btDumpStop.setOnClickListener(this);
        btAnalyze.setOnClickListener(this);

        btAnalyze.setClickable(false);
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
            helper = new DumpHelper(MainActivity.this, etPkgName.getText()
                    .toString());
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
}
