package edu.bupt.netstat;

import edu.bupt.netstat.analyze.OnReadComplete;
import edu.bupt.netstat.analyze.PacketReader;
import edu.bupt.netstat.pcap.DumpHelper;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.widget.ListView;
import android.widget.TextView;

/**
 * PacketReader
 * 
 * @author zzz
 * 
 */
public class PacketDetailsActivity extends Activity {

    private TextView tv;
    private ListView lv;
    private String localIP;
    private String[] ipList;
    private PacketReader reader;

    /**
     * @author zzz
     * 
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        localIP = i.getStringExtra("localIp");
        ipList = i.getStringArrayExtra("IPLIST");
        setContentView(R.layout.packets_list);
        tv = (TextView) this.findViewById(R.id.numPkt);
        lv = (ListView) this.findViewById(R.id.pktList);
        
		reader = new PacketReader(ipList,localIP, DumpHelper.fileOutPath + DumpHelper.fileName);
		//必须加这个线程，否则报空指针异常，2014-08-06，yyl
		new Thread(new Runnable() {

			@Override
			public void run() {
				reader.read(localIP, DumpHelper.fileOutPath
						+ DumpHelper.fileName, new OnReadComplete() {

					@Override
					public void onComplete() {
					
					}

				},0);
			}
		}).start();
		
        PacketDetailsAdapter adapter = new PacketDetailsAdapter(this,
                R.layout.list_item, reader.packets,reader);
        lv.setAdapter(adapter);
        tv.setText("" + reader.packets.size());
    }

}
