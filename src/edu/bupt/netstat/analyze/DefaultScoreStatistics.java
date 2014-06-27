package edu.bupt.netstat.analyze;

public class DefaultScoreStatistics extends ScoreStatisticsSuper {

    @Override
    public int totalScore(int dns, int tcp, int resp, int load, long speed,
            long traffic) {
        return -1;
    }

}
