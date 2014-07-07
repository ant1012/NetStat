package edu.bupt.netstat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;  
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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
	private TextView jitter;
	private TextView advertise;
	private TextView res_efficiency;
	private TextView ss;
	private int score;
	//private File file;
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

		View layoutRetransmission = (View) findViewById(R.id.layout_retransmission);
		View layoutDns = (View) findViewById(R.id.layout_dns);
		View layoutTcpConnectionTime = (View) findViewById(R.id.layout_tcpconnectiontime);
		View layoutResponse = (View) findViewById(R.id.layout_responsetime);
		View layoutLoadTime = (View) findViewById(R.id.layout_loadtime);
		View layoutSpeed = (View) findViewById(R.id.layout_speed);
		View layoutTraffic = (View) findViewById(R.id.layout_traffic);
		View layoutThread = (View) findViewById(R.id.layout_thread);
		View layoutJitter = (View) findViewById(R.id.layout_jitter);
		View layoutAdvertise = (View) findViewById(R.id.layout_advertise);
		View layoutResEfficiency = (View) findViewById(R.id.layout_reseffictive);
		
		switch (pkgType) {
		case ScoreStatisticsSuper.WEB:
			layoutDns.setVisibility(View.VISIBLE);
			layoutTcpConnectionTime.setVisibility(View.VISIBLE);
			layoutResponse.setVisibility(View.VISIBLE);
			layoutLoadTime.setVisibility(View.VISIBLE);
			layoutSpeed.setVisibility(View.VISIBLE);
			layoutTraffic.setVisibility(View.VISIBLE);
			layoutRetransmission.setVisibility(View.VISIBLE);
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
		case ScoreStatisticsSuper.GAME:
			layoutDns.setVisibility(View.VISIBLE);
			layoutTcpConnectionTime.setVisibility(View.VISIBLE);
			layoutResponse.setVisibility(View.VISIBLE);
			layoutSpeed.setVisibility(View.VISIBLE);
			layoutTraffic.setVisibility(View.VISIBLE);
			layoutRetransmission.setVisibility(View.VISIBLE);
			layoutAdvertise.setVisibility(View.VISIBLE);
			layoutResEfficiency.setVisibility(View.VISIBLE);
		default:
		}

		loss = (TextView) this.findViewById(R.id.textview_retransmission);
		dns = (TextView) this.findViewById(R.id.textview_dns);
		tcp = (TextView) this.findViewById(R.id.textview_tcpconnectiontime);
		resp = (TextView) this.findViewById(R.id.textview_responsetime);
		load = (TextView) this.findViewById(R.id.textview_loadtime);
		speed = (TextView) this.findViewById(R.id.textview_speed);
		traffic = (TextView) this.findViewById(R.id.textview_traffic);
		thread = (TextView) this.findViewById(R.id.textview_thread);
		jitter = (TextView) this.findViewById(R.id.textview_jitter);
		advertise = (TextView) this.findViewById(R.id.textview_advertise);
		res_efficiency = (TextView) this.findViewById(R.id.textview_resefficiency);
		
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
		
		Button saveBut = (Button) this.findViewById(R.id.save);
		//File file = new File(Environment.getExternalStorageDirectory(), "reslut.txt");
		saveBut.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				writeToLocal();
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

				},pkgType);
			}
		}).start();

	}
	/**
	 * @author yyl
	 */
	public void writeToLocal(){
		File file = new File(Environment.getExternalStorageDirectory(), "reslut.txt");
		BufferedWriter writeToLocal = null;
		String appType=null;
        try {
        	writeToLocal = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
        } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        
        StringBuilder sbTitle = new StringBuilder();
    	sbTitle.append("Retransnission"+"\t")
    	.append("DNS Time"+"\t")
    	.append("TCP Conn Time" + "\t")
    	.append("Response Time"+"\t")
    	.append("Load Time"+"\t")
    	.append("Speed"+"\t")
    	.append("Total Traffic" +"\t")
    	.append("Jitter"+"\t")
    	.append("Thread Numbers"+"\t")
    	.append("Advertisement"+"\t")
    	.append("Resource Efficiency"+"\t")
    	.append("Score"+"\t").append("App Type"+"\t").append("netWork_Type");
    	
    	StringBuilder sbDataCommon = new StringBuilder();
    	sbDataCommon.append("\n"+loss.getText()+"\t").append(dns.getText()+"\t")
    	.append(tcp.getText() + "\t");
    	
    	StringBuilder sbData = new StringBuilder();
    	
    	switch (pkgType){
    	case 0:
    		appType="web";
    		sbData = sbDataCommon.append(resp.getText()+"\t").append(load.getText()+"\t").append(speed.getText()+"\t")
    		.append(traffic.getText() +"\t").append("--"+"\t").append("--"+"\t").append("--"+"\t").append("--"+"\t")
        	.append(ss.getText()+"\t").append(appType+"\t").append("null");
    		break;
    	case 1:
    		appType="download";
    		sbData = sbDataCommon.append("--"+"\t").append(load.getText()+"\t").append(speed.getText()+"\t").append("--"+"\t")
    		.append("--"+"\t").append(thread.getText()+"\t").append("--"+"\t").append("--"+"\t")
        	.append(ss.getText()+"\t").append(appType+"\t").append("null");
    		break;
    	case 2:
    		appType="video";
    		sbData = sbDataCommon.append(resp.getText()+"\t").append("--"+"\t").append(speed.getText()+"\t").append("--"+"\t")
    		.append(jitter.getText()+"\t").append("--"+"\t").append("--"+"\t").append("--"+"\t")
        	.append(ss.getText()+"\t").append(appType+"\t").append("null");
    		break;
    	case 3:
    		appType="trading";
    		break;
    	case 4:
    		appType="game";
    		sbData = sbDataCommon.append(resp.getText()+"\t").append("--"+"\t").append(speed.getText()+"\t").append(traffic.getText()+"\t")
    	    		.append("--"+"\t").append("--"+"\t").append(advertise.getText()+"\t").append(res_efficiency.getText()+"\t")
    	        	.append(ss.getText()+"\t").append(appType+"\t").append("null");
    		break;
    	default:
    	}
        try {
        	if(file.length()>10){
        		writeToLocal.write(sbData.toString());
        	}else{
        		writeToLocal.write(sbTitle.toString());
        		writeToLocal.write(sbData.toString());
        	}
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }    
        try {
        	writeToLocal.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
        	writeToLocal.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }    
        Toast toast = Toast.makeText(NetQualityIndicatorsActivity.this, 
        		"写文件到SD卡成功   位置：根目录 /result.txt", Toast.LENGTH_SHORT); 
        toast.show();           
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
				thread.setText(String.valueOf(reader.threadNum));
				jitter.setText(String.valueOf(reader.delayJitter));
				
				advertise.setText(reader.advertise_traffic+" Bytes");
				res_efficiency.setText(reader.res_efficiency+"%");
				
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
