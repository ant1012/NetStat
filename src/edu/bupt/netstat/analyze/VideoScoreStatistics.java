package edu.bupt.netstat.analyze;

import android.util.Log;

/**
 * VideoScoreStatistics
 * 
 * @author zzz
 * 
 */
public class VideoScoreStatistics extends ScoreStatisticsSuper {
    private final static String TAG = "VideoScoreStatistics";

    public VideoScoreStatistics() {
        this.scoreWeight = new ScoreWeight(0.0355, 0.1015, 0.1621, 0, 0.0556,
                0, 0.3676, 0.2778, 0, 0, 0, 0);
    }

    protected int dnsScore(int dns) { // unit: us
        return (int) (dns > 0 ? 20910.0 / (208.3 + 0.001 * dns) : 0);
    }

    protected int tcpScore(int tcp) { // unit: us
        return dnsScore(tcp);
    }

    protected int respScore(int resp) { // unit: us
        return (int) (resp > 0 ? 102.8 * Math.exp(-0.00112 * resp * 0.001) : 0);
    }

    protected int delayJitterScore(float jitter) { // unit: us
        return (int) (jitter > 0 ? 32820 / (331.4 + 0.001 * jitter) : 0);
    }

    protected int pktLossScore(float loss) {
        return (int) (100 * (1 - loss));
    }

    protected int speedScore(long speed) { // unit: B/s
        int s = (int) (speed > 0 ? 16.25 * Math
                .log(8 * speed / 1024.0 / 1024.0) + 68.21 : 0);
        return s > 100 ? 100 : s;
    }

    @Override
    public int totalScore(PacketReader reader) {
        Log.v(TAG, "" + this.scoreWeight.weightDnsScore + " "
                + this.scoreWeight.weightTcpScore + " "
                + this.scoreWeight.weightRespScore + " "
                + this.scoreWeight.weightDelayjitterScore + " "
                + this.scoreWeight.weightPacketlossScore + " "
                + this.scoreWeight.weightSpeedScore);

        return (int) (this.scoreWeight.weightDnsScore * dnsScore(reader.avrDns)
                + this.scoreWeight.weightTcpScore * tcpScore(reader.avrRtt)
                + this.scoreWeight.weightRespScore * respScore(reader.avrRes)
                + this.scoreWeight.weightDelayjitterScore
                * delayJitterScore(reader.delayJitter)
                + this.scoreWeight.weightPacketlossScore
                * pktLossScore(reader.pktLoss) + this.scoreWeight.weightSpeedScore
                * speedScore(reader.avrSpeed));
    }

}
