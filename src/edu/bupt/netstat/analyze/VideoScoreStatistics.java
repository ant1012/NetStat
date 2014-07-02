package edu.bupt.netstat.analyze;

/**
 * VideoScoreStatistics
 * 
 * @author zzz
 * 
 */
public class VideoScoreStatistics extends ScoreStatisticsSuper {

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
        return (int) (0.0355 * dnsScore(reader.avrDns) + 0.1015
                * tcpScore(reader.avrRtt) + 0.1621 * respScore(reader.avrRes)
                + 0.3676 * delayJitterScore(reader.delayJitter) + 0.2778
                * pktLossScore(reader.pktLoss) + 0.0556 * speedScore(reader.avrSpeed));
    }

}
