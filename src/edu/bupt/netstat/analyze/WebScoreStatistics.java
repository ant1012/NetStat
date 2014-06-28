package edu.bupt.netstat.analyze;

/**
 * WebScoreStatistics
 * 
 * @author xiang
 * 
 */
public class WebScoreStatistics extends ScoreStatisticsSuper {

    protected int dnsScore(int dns) { // unit: us
        return (int) (dns > 0 ? 20910.0 / (209.1 + 0.001 * dns) : 0);
    }

    protected int tcpScore(int tcp) { // unit: us
        return dnsScore(tcp);
    }

    protected int respScore(int resp) { // unit: us
        return (int) (resp > 0 ? 102.8 * Math.exp(-0.00112 * resp * 0.001) : 0);
    }

    protected int loadScore(int load) { // unit: us
        return (int) (load > 0 ? 99.1 * Math.exp(-0.04665 * load * 1e-6) : 0);
    }

    protected int speedScore(long speed) { // unit: B/s
        int s = (int) (speed > 0 ? 16.15 * Math.log(81.4 * 8 * speed / 1024.0
                / 1024.0) : 0);
        return s > 100 ? 100 : s;
    }

    protected int trafficScore(long traffic) { // unit: B
        return (int) (traffic > 0 ? 100 * Math.exp(-0.00028 * traffic) : 0);
    }

    @Override
    public int totalScore(PacketReader reader) {

        return (int) (0.0389 * dnsScore(reader.avrDns) + 0.0859
                * tcpScore(reader.avrRtt) + 0.138 * respScore(reader.avrRes)
                + 0.4039 * loadScore(reader.avrTime) + 0.2857
                * speedScore(reader.avrSpeed) + 0.0476 * trafficScore(reader.traffic));
    }
}
