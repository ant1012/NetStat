package edu.bupt.netstat.analyze;

import android.util.Log;

/**
 * DownloadScoreStatistics
 * 
 * @author yyl
 * 
 */
public class DownloadScoreStatistics extends ScoreStatisticsSuper {
    private final static String TAG = "DownloadScoreStatistics";

    public DownloadScoreStatistics() {
        this.scoreWeight = new ScoreWeight(0.0484, 0.1082, 0, 0, 0.2813, 0, 0,
                0.0938, 0.3882, 0.0802, 0, 0);
    }

    // dns connect delay score
    protected int dnsScore(int dns) { // unit: us
        return (int) (dns > 0 ? 20910.0 / (209.1 + 0.001 * dns) : 0);
    }

    // tcp connect delay, same as dns connect delay
    protected int tcpScore(int tcp) { // unit: us
        return dnsScore(tcp);
    }

    // download delay
    protected int downloadScore(int load) { // unit: us
        return (int) (load > 0 ? 99.1 * Math.exp(-0.04665 * load * 1e-6) : 0);
    }

    // download speed score
    protected int speedScore(long speed) { // unit: B/s
        int s = (int) (speed > 0 ? 16.15 * Math.log(81.4 * 8 * speed / 1024.0
                / 1024.0) : 0);
        return s > 100 ? 100 : s;
    }

    // multiThread score
    protected int multiThreadScore(int threadNum) {
        return threadNum >= 10 ? 100 : 10 * threadNum;
    }

    // packat loss score
    protected int pktlossScore(float plr) { // unit: B
        return (int) (1 - plr) * 100;
    }

    @Override
    public int totalScore(PacketReader reader) {
        Log.v(TAG, "" + this.scoreWeight.weightDnsScore + " "
                + this.scoreWeight.weightTcpScore + " "
                + this.scoreWeight.weightDownloadScore + " "
                + this.scoreWeight.weightMultithreadScore + " "
                + this.scoreWeight.weightSpeedScore + " "
                + this.scoreWeight.weightPacketlossScore);

        return (int) (this.scoreWeight.weightDnsScore * dnsScore(reader.avrDns)
                + this.scoreWeight.weightTcpScore * tcpScore(reader.avrRtt)
                + this.scoreWeight.weightDownloadScore
                * downloadScore(reader.avrTime)
                + this.scoreWeight.weightMultithreadScore
                * multiThreadScore(reader.threadNum)
                + this.scoreWeight.weightSpeedScore
                * speedScore(reader.avrSpeed) + this.scoreWeight.weightPacketlossScore
                * pktlossScore(reader.pktLoss));
    }
}
