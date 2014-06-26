package edu.bupt.netstat;

import java.util.ArrayList;

import org.jnetpcap.packet.JPacket;

import edu.bupt.netstat.analyze.PacketReader;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
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

    /**
     * @author zzz
     * 
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.packets_list);
        tv = (TextView) this.findViewById(R.id.numPkt);
        lv = (ListView) this.findViewById(R.id.pktList);

        ArrayList<JPacket> packets = PacketReader.getInstance().packets;
        PacketDetailsAdapter adapter = new PacketDetailsAdapter(this,
                R.layout.list_item, packets);
        lv.setAdapter(adapter);
        tv.setText("" + packets.size());
    }

}
