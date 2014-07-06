package edu.bupt.netstat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.bupt.netstat.analyze.ScoreStatisticsSuper;
import edu.bupt.netstat.analyze.ScoreWeight;

/**
 * SetWeightActivity
 * 
 * @author zzz
 */
public class SetWeightActivity extends Activity implements OnClickListener {
    public static final String TAG = "SetWeightActivity";

    private int pkgType;
    private ScoreWeight scoreWeight;
    private EditText etRetransmission;
    private EditText etDnsTime;
    private EditText etTcpConnectTime;
    private EditText etResponseTime;
    private EditText etLoadTime;
    private EditText etSpeed;
    private EditText etTraffic;
    private EditText etThread;
    private EditText etJitter;

    private Button btSubmit;
    private Button btCancel;

    /**
     * @author zzz
     * 
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_weight);
        Intent intent = getIntent();
        pkgType = intent.getIntExtra(NetQualityIndicatorsActivity.PKG_TYPE, -1);
        scoreWeight = intent
                .getParcelableExtra(NetQualityIndicatorsActivity.SET_WEIGHT);

        View layoutRetransmission = (View) findViewById(R.id.layout_set_retransmission);
        View layoutDns = (View) findViewById(R.id.layout_set_dns);
        View layoutTcpConnectionTime = (View) findViewById(R.id.layout_set_tcpconnectiontime);
        View layoutResponse = (View) findViewById(R.id.layout_set_responsetime);
        View layoutLoadTime = (View) findViewById(R.id.layout_set_loadtime);
        View layoutSpeed = (View) findViewById(R.id.layout_set_speed);
        View layoutTraffic = (View) findViewById(R.id.layout_set_traffic);
        View layoutThread = (View) findViewById(R.id.layout_set_thread);
        View layoutJitter = (View) findViewById(R.id.layout_set_jitter);

        switch (pkgType) {
        case ScoreStatisticsSuper.WEB:
            layoutDns.setVisibility(View.VISIBLE);
            layoutTcpConnectionTime.setVisibility(View.VISIBLE);
            layoutResponse.setVisibility(View.VISIBLE);
            layoutLoadTime.setVisibility(View.VISIBLE);
            layoutSpeed.setVisibility(View.VISIBLE);
            layoutTraffic.setVisibility(View.VISIBLE);
            break;
        case ScoreStatisticsSuper.DOWNLOADING:
            layoutDns.setVisibility(View.VISIBLE);
            layoutTcpConnectionTime.setVisibility(View.VISIBLE);
            layoutLoadTime.setVisibility(View.VISIBLE);
            layoutThread.setVisibility(View.VISIBLE);
            layoutSpeed.setVisibility(View.VISIBLE);
            layoutRetransmission.setVisibility(View.VISIBLE);
            break;
        case ScoreStatisticsSuper.VIDEO:
            layoutDns.setVisibility(View.VISIBLE);
            layoutTcpConnectionTime.setVisibility(View.VISIBLE);
            layoutResponse.setVisibility(View.VISIBLE);
            layoutJitter.setVisibility(View.VISIBLE);
            layoutSpeed.setVisibility(View.VISIBLE);
            layoutRetransmission.setVisibility(View.VISIBLE);
            break;
        default:
        }

        etRetransmission = (EditText) findViewById(R.id.edittext_retransmission);
        etDnsTime = (EditText) findViewById(R.id.edittext_dns);
        etTcpConnectTime = (EditText) findViewById(R.id.edittext_tcpconnectiontime);
        etResponseTime = (EditText) findViewById(R.id.edittext_responsetime);
        etLoadTime = (EditText) findViewById(R.id.edittext_loadtime);
        etSpeed = (EditText) findViewById(R.id.edittext_speed);
        etTraffic = (EditText) findViewById(R.id.edittext_traffic);
        etThread = (EditText) findViewById(R.id.edittext_thread);
        etJitter = (EditText) findViewById(R.id.edittext_jitter);

        etRetransmission.setText(String
                .valueOf(scoreWeight.weightPacketlossScore));
        etDnsTime.setText(String.valueOf(scoreWeight.weightDnsScore));
        etTcpConnectTime.setText(String.valueOf(scoreWeight.weightTcpScore));
        etResponseTime.setText(String.valueOf(scoreWeight.weightRespScore));
        etLoadTime.setText(String.valueOf(scoreWeight.weightLoadScore));
        etSpeed.setText(String.valueOf(scoreWeight.weightSpeedScore));
        etTraffic.setText(String.valueOf(scoreWeight.weightTrafficScore));
        etThread.setText(String.valueOf(scoreWeight.weightMultithreadScore));
        etJitter.setText(String.valueOf(scoreWeight.weightDelayjitterScore));

        btSubmit = (Button) findViewById(R.id.button_set);
        btCancel = (Button) findViewById(R.id.button_cancel);
        btSubmit.setOnClickListener(this);
        btCancel.setOnClickListener(this);
    }

    /**
     * @author zzz
     * 
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button_cancel:
            setResult(Activity.RESULT_CANCELED, new Intent());
            finish();
        case R.id.button_set:
            Intent intent = new Intent();
            scoreWeight.weightPacketlossScore = Double
                    .parseDouble(etRetransmission.getText().toString());
            scoreWeight.weightDnsScore = Double.parseDouble(etDnsTime.getText()
                    .toString());
            scoreWeight.weightTcpScore = Double.parseDouble(etTcpConnectTime
                    .getText().toString());
            scoreWeight.weightRespScore = Double.parseDouble(etResponseTime
                    .getText().toString());
            scoreWeight.weightLoadScore = Double.parseDouble(etLoadTime
                    .getText().toString());
            scoreWeight.weightSpeedScore = Double.parseDouble(etSpeed.getText()
                    .toString());
            scoreWeight.weightTrafficScore = Double.parseDouble(etTraffic
                    .getText().toString());
            scoreWeight.weightMultithreadScore = Double.parseDouble(etThread
                    .getText().toString());
            scoreWeight.weightDelayjitterScore = Double.parseDouble(etJitter
                    .getText().toString());

            intent.putExtra(NetQualityIndicatorsActivity.SET_WEIGHT,
                    scoreWeight);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}
