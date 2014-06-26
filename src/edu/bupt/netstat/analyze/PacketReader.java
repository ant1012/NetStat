package edu.bupt.netstat.analyze;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.network.Ip6;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;
import org.jnetpcap.protocol.tcpip.Http.Request;

import android.util.Log;
import edu.bupt.netstat.pcap.DumpHelper;

/**
 * PacketReader
 * 
 * @author zzz
 * 
 */
public class PacketReader {
    private final static String TAG = "PacketReader";

    private static PacketReader instance;

    public ArrayList<JPacket> packets;

    public String localIP;
    public String pcapFileName;

    public boolean[] retTable;
    public HashMap<Integer, Integer> rttMap;
    public long pktTime;
    public float pktLoss;
    public int avrRtt;
    public int avrDns;
    public int avrRes;
    public int avrTime;
    public long avrSpeed;
    public long traffic;
    public HashMap<String, Integer> responseTime = new HashMap<String, Integer>();
    public HashMap<String, Integer> dnsTime = new HashMap<String, Integer>();
    public HashMap<Integer, Integer> rrMap = new HashMap<Integer, Integer>();

    static {
        System.loadLibrary("jnetpcap");
    }

    /**
     * @author zzz
     * 
     */
    private PacketReader() {
    }

    /**
     * @author zzz
     * 
     */
    public static PacketReader getInstance() {
        if (instance == null) {
            synchronized (PacketReader.class) {
                if (instance == null) {
                    instance = new PacketReader();
                }
            }
        }
        return instance;
    }

    /**
     * @author zzz
     * 
     */
    public void read(String localIP, String pcapFileName,
            OnReadComplete onReadComplete) {
        this.localIP = localIP;
        this.pcapFileName = pcapFileName;

        packets = new ArrayList<JPacket>();
        listPackets();
        retTable = new boolean[packets.size()];
        rttMap = new HashMap<Integer, Integer>();
        pktLoss = getRetTimes();
        avrRtt = getRtt();
        avrDns = getDns();
        avrRes = getHttpResponse();

        pktTime = getPktTime();

        avrTime = getAvrTime();

        traffic = new File(pcapFileName).length();
        avrSpeed = getAvrSpeed();

        testLog();
        onReadComplete.onComplete();
    }

    /**
     * @author xiang
     * 
     */
    private void listPackets() {
        StringBuilder errbuf = new StringBuilder();
        Pcap pcap = Pcap.openOffline(pcapFileName, errbuf);
        JPacketHandler<String> handler = new JPacketHandler<String>() {

            @Override
            public void nextPacket(JPacket packet, String user) {
                // if (filterByIp(packet, ipList)){
                packets.add(packet);
            }
        };
        try {
            pcap.loop(-1, handler, null);
        } finally {
            pcap.close();
        }
    }

    /**
     * @author xiang
     * 
     */
    // dpt > 1024 && spt > 1024 : compare with local ip
    private String getServerIp(JPacket pkt) {
        String ip = "";
        byte[] addr;
        if (pkt.hasHeader(Tcp.ID)) {
            Tcp tcp = new Tcp();
            pkt.getHeader(tcp);
            if (pkt.hasHeader(Ip4.ID)) {
                Ip4 ip4 = new Ip4();
                pkt.getHeader(ip4);
                // addr = (tcp.destination() < 1024) ? ip4.destination() :
                // ip4.source();
                // ip = FormatUtils.ip(addr);
                ip = FormatUtils.ip(ip4.source());
                if (ip.equals(this.localIP)) {
                    ip = FormatUtils.ip(ip4.destination());
                }
            } else if (pkt.hasHeader(Ip6.ID)) {
                Ip6 ip6 = new Ip6();
                pkt.getHeader(ip6);
                addr = (tcp.destination() < 1024) ? ip6.destination() : ip6
                        .source();
                ip = FormatUtils.asStringIp6(addr, true);
            }
        } else if (pkt.hasHeader(Udp.ID)) {
            Udp udp = new Udp();
            pkt.getHeader(udp);
            if (pkt.hasHeader(Ip4.ID)) {
                Ip4 ip4 = new Ip4();
                pkt.getHeader(ip4);
                // int dpt = udp.destination();
                // // SSDP address: 239.255.255.250:1900
                // addr = (dpt > 1024 || dpt == 1900) ? ip4.destination() :
                // ip4.source();
                // ip = FormatUtils.ip(addr);
                ip = FormatUtils.ip(ip4.source());
                if (ip.equals(this.localIP)) {
                    ip = FormatUtils.ip(ip4.destination());
                }
            } else if (pkt.hasHeader(Ip6.ID)) {
                Ip6 ip6 = new Ip6();
                pkt.getHeader(ip6);
                addr = (udp.destination() > 1024) ? ip6.destination() : ip6
                        .source();
                ip = FormatUtils.asStringIp6(addr, true);
            }
        }
        return ip;
    }

    /**
     * @author xiang
     * 
     */
    private boolean filterByIp(JPacket pkt, String[] ipList) {
        if (this.localIP == null) {
            Log.e("LocalIp", "Local ip is null!");
            return true;
        }
        String ip = getServerIp(pkt);
        if (ipList == null || ipList.length == 0 || ip.equals("")) {
            Log.e("Netstat", "Ip list is null or empty! or Remote ip is empty!");
            return false;
        }
        for (String s : ipList) {
            if (ip.equals(s)) {
                Log.i("RemoteIp", ip);
                return true;
            }
        }
        Log.i("OtherIp ", ip);
        return false;
    }

    /**
     * @author xiang
     * 
     */
    private int getRtt() {
        int rtt, totRtt = 0, cnt = 0;
        long ack = 0, seq = 0, t2 = 0;
        JPacket p = null;
        Tcp h = new Tcp();
        int psize = packets.size();
        // The android tool SparseArrays can be used.
        HashMap<Integer, Long[]> map = new HashMap<Integer, Long[]>();
        for (int i = 0; i < psize; i++) {
            p = packets.get(i);
            if (p.hasHeader(Tcp.ID)) {
                p.getHeader(h);
                if (h.flags() != 0x10) { // not only ACK
                    ack = h.ack();
                    seq = h.seq();
                    t2 = p.getCaptureHeader().timestampInMicros();
                    map.put(i, new Long[] { ack, seq, t2 });
                }
                if (h.flags() != 0x02) { // not only SYN
                    ack = h.ack();
                    seq = h.seq();
                    Set<Integer> s = map.keySet();
                    for (Integer m : s) {
                        if (m.intValue() == i) {
                            continue;
                        }
                        Long[] val = map.get(m);
                        if (ack == (val[1] + 1) || seq == val[0]) {
                            map.remove(m);
                            rtt = (int) (p.getCaptureHeader()
                                    .timestampInMicros() - val[2]);
                            if (rtt < 500000) {
                                rttMap.put(i, rtt);
                                rrMap.put(m, i);
                                totRtt += rtt;
                                cnt++;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return cnt > 0 ? totRtt / cnt : 0;
    }

    /**
     * @author xiang
     * 
     */
    private float getRetTimes() {
        int cnt = 0;
        int psize = packets.size();
        long seq, pseq;
        String key = "";
        Tcp h = new Tcp();
        JPacket p = null;
        HashMap<String, Long> map = new HashMap<String, Long>();
        for (int i = 0; i < psize; i++) {
            p = packets.get(i);
            if (p.hasHeader(Tcp.ID)) {
                p.getHeader(h);
                seq = h.seq();
                key = (h.source() > 1023) ? ("C" + h.source()) : ("S" + h
                        .destination());
                if (map.containsKey(key)) {
                    pseq = map.get(key);
                    if (pseq < seq) {
                        map.put(key, seq);
                    } else if (pseq > seq) {
                        cnt++;
                        Log.i(key, "" + i);
                        retTable[i] = true;
                    }
                } else {
                    map.put(key, seq);
                }
            }
        }
        return psize > 0 ? ((float) cnt) / psize : 0;
    }

    /**
     * @author xiang
     * 
     */
    private int getDns() {
        long t1 = 0;
        int transId = 0, plOff = 0;
        int offset = 0, size = 0;
        int cnt = 0, totDns = 0;
        int psize = packets.size();
        JPacket p;
        byte[] url = null;
        for (int i = 0; i < psize; i++) {
            p = packets.get(i);
            if (p.hasHeader(Udp.ID)) {
                Udp udp = new Udp();
                p.getHeader(udp);
                if (udp.destination() == 53) {
                    plOff = udp.getPayloadOffset();
                    offset = plOff + 12 + 1;
                    size = udp.getPayloadLength() - (12 + 4 + 2);
                    transId = (p.getByte(plOff) << 8) & 0xFF00
                            | p.getByte(plOff + 1) & 0xFF;
                    url = p.getByteArray(offset, size);
                    t1 = p.getCaptureHeader().timestampInMicros();
                } else if (udp.source() == 53 && transId != 0) {
                    int tmpId = (p.getByte(plOff) << 8) & 0xFF00
                            | p.getByte(plOff + 1) & 0xFF;
                    // if (compByteArray(url,p.getByteArray(offset, size))) {
                    if (transId == tmpId) {
                        int dns = (int) (p.getCaptureHeader()
                                .timestampInMicros() - t1);
                        dnsTime.put(new String(url) + "~" + i, dns);
                        totDns += dns;
                        cnt++;
                        transId = 0;
                    }
                }
            }
        }
        return cnt > 0 ? totDns / cnt : 0;
    }

    /**
     * @author xiang
     * 
     */
    private boolean compByteArray(byte[] b1, byte[] b2) {
        int i = 0;
        if (b1.length == b2.length) {
            while (b1[i] == b2[i] && i < b1.length - 1) {
                i++;
            }
            return (b1[i] == b2[i]);
        }
        return false;
    }

    /**
     * @author xiang
     * 
     */
    private int getHttpResponse() {
        int totRes = 0, cnt = 0;
        int psize = packets.size();
        JPacket p;
        String s = null;
        for (int i = 0; i < psize; i++) {
            p = packets.get(i);
            if (p.hasHeader(Http.ID)) {
                Http http = new Http();
                p.getHeader(http);
                s = http.fieldValue(Request.Host);
                Integer res = rrMap.get(i);
                if (s != null && res != null) {
                    String get = s + http.fieldValue(Request.RequestUrl);
                    int r = rttMap.get(res); // TODO error here
                    responseTime.put(get, r);
                    totRes += r;
                    cnt++;
                }
            }
        }
        return cnt > 0 ? totRes / cnt : 0;
    }

    /**
     * @author xiang
     * 
     */
    private long getPktTime() {
        long pktTime = 0;
        long t1, t2;
        int psize = packets.size();
        if (psize > 0) {
            JPacket p;
            p = packets.get(0);
            t1 = p.getCaptureHeader().timestampInMicros();
            p = packets.get(psize - 1);
            t2 = p.getCaptureHeader().timestampInMicros();
            pktTime = t2 - t1;
        }
        return pktTime;
    }

    /**
     * @author xiang
     * 
     */
    private int getAvrTime() {
        int n = dnsTime.size(); // to represent the num of web accessed.
        n = n > 0 ? n : 1;
        return (int) (pktTime / n);
    }

    /**
     * @author xiang
     * 
     */
    private long getAvrSpeed() {
        return pktTime > 0 ? (1000000 * traffic) / pktTime : 0;
    }

    /**
     * @author xiang
     * 
     */
    private void testLog() {
        for (String s : dnsTime.keySet()) {
            Log.i("DNS", s + " " + dnsTime.get(s));
        }
        for (String s : responseTime.keySet()) {
            Log.i("HttpResponse", s + " " + responseTime.get(s));
        }
        for (int s : rttMap.keySet()) {
            Log.i("RTT " + s, "" + rttMap.get(s));
        }
        Log.i("PktLoss", "" + pktLoss);
        Log.i("AvrDns", "" + avrDns + "us");
        Log.i("AvrRtt", "" + avrRtt + "us");
        Log.i("AvrRes", "" + avrRes + "us");
        Log.i("AvrTime", "" + avrTime + "ms");
        Log.i("AvrSpeed", "" + avrSpeed + "B/s");
        Log.i("Traffic", "" + traffic + "B");
    }

    // public void getTcpHandshake() {
    // ArrayList<Integer> al = new ArrayList<Integer>();
    // for (String s:dnsTime.keySet()) {
    // al.add(Integer.valueOf(s.split("~")[1]));
    // }
    // }
}
