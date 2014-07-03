package edu.bupt.netstat;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.bupt.netstat.analyze.OnReadComplete;
import edu.bupt.netstat.analyze.PacketReader;
import edu.bupt.netstat.analyze.ScoreStatisticsFactory;
import edu.bupt.netstat.analyze.ScoreStatisticsSuper;
import edu.bupt.netstat.pcap.DumpHelper;

/**
 * NetQualityIndicatorsActivity
 * 
 * @author zzz
 * 
 */
public class NetQualityIndicatorsActivity extends Activity {
    private static final String TAG = "NetQualityIndicatorsActivity";

    private static final int READ_COMPLETE = 1;
    public static final String LOCALIP = "localip";
    public static final String PKG_TYPE = "pkg_type";

    DecimalFormat df = new DecimalFormat("#.###");

    private ProgressDialog progressDialog;

    private PacketReader reader;
    private String localIP;
    private int pkgType;
    private TextView loss;
    private TextView dns;
    private TextView tcp;
    private TextView resp;
    private TextView load;
    private TextView speed;
    private TextView traffic;
    private TextView thread;
    private TextView ss;
    private int score;

    /**
     * @author zzz
     * 
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_quality_indicators);
        Intent from = getIntent();
        localIP = from.getStringExtra(LOCALIP);
        pkgType = from.getIntExtra(PKG_TYPE, 0);
        Log.v(TAG, "localIP - " + localIP);
        Log.v(TAG, "pkgType - " + pkgType);

        loss = (TextView) this.findViewById(R.id.pl);
        dns = (TextView) this.findViewById(R.id.dns);
        tcp = (TextView) this.findViewById(R.id.tcp);
        resp = (TextView) this.findViewById(R.id.resp);
        load = (TextView) this.findViewById(R.id.load);
        speed = (TextView) this.findViewById(R.id.speed);
        traffic = (TextView) this.findViewById(R.id.traffic);
        thread = (TextView) this.findViewById(R.id.thread);
        ss = (TextView) this.findViewById(R.id.ss);
        reader = PacketReader.getInstance();

        Button detButt = (Button) this.findViewById(R.id.detail);
        detButt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(NetQualityIndicatorsActivity.this,
                        PacketDetailsActivity.class);
                startActivity(intent);
            }
        });

        progressDialog = ProgressDialog.show(this, null,
                getString(R.string.processing_hint), true, false);

        new Thread(new Runnable() {

            @Override
            public void run() {
                reader.read(localIP, DumpHelper.fileOutPath
                        + DumpHelper.fileName, new OnReadComplete() {

                    @Override
                    public void onComplete() {
                        ScoreStatisticsSuper statistics = ScoreStatisticsFactory
                                .create(pkgType);
                        score = statistics.totalScore(reader);
                        Message msg = new Message();
                        msg.what = READ_COMPLETE;
                        handler.sendMessage(msg);
                    }

                });
            }
        }).start();

    }

    /**
     * @author zzz
     * 
     */
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case READ_COMPLETE:
                loss.setText("" + reader.pktLoss);
                dns.setText(df.format(0.001 * reader.avrDns) + " ms");
                tcp.setText(df.format(0.001 * reader.avrRtt) + " ms");
                resp.setText(df.format(0.001 * reader.avrRes) + " ms");
                load.setText(df.format(1e-6 * reader.avrTime) + " s");
                speed.setText(formatSpeed(8 * reader.avrSpeed));
                traffic.setText(formatTraffic(reader.traffic));
                thread.setText(reader.threadNum + "");
                ss.setText("" + score);
                progressDialog.cancel();
            default:
                Log.w(TAG, "unknow msg");
            }
        }
    };

    /**
     * @author xiang
     * 
     */
    private String formatTraffic(long data) {
        return (data / 1000000 > 1) ? (df.format(data / 1048576.0)) + " MB"
                : ((data / 1000 > 1) ? (df.format(data / 1024.0)) + " KB"
                        : data + " B");
    }

    /**
     * @author xiang
     * 
     */
    private String formatSpeed(long data) {
        return (data / 1000000 > 1) ? (data / 1048576) + " Mbps"
                : ((data / 1000 > 1) ? (data / 1024) + " Kbps" : data + " bps");
    }

    /**
     * @author xiang
     * 
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.setResult(RESULT_OK, null);
            this.finish();
            return true;
        }
        return false;
    }

}
