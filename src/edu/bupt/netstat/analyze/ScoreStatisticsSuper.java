package edu.bupt.netstat.analyze;

/**
 * ScoreStatisticsSuper
 * 
 * @author zzz
 * 
 */
public abstract class ScoreStatisticsSuper {
    public static final int WEB = 0;
    public static final int DOWNLOAD = 1;
    public static final int VIDEO = 2;

    public abstract int totalScore(int dns, int tcp, int resp, int load,
            long speed, long traffic);
}
