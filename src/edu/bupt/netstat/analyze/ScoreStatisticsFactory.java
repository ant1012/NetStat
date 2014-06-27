package edu.bupt.netstat.analyze;

/**
 * ScoreStatisticsFactory
 * 
 * @author zzz
 * 
 */
public class ScoreStatisticsFactory {
    public static ScoreStatisticsSuper create(int type) {
        switch (type) {
        case ScoreStatisticsSuper.WEB:
            return new WebScoreStatistics();
        case ScoreStatisticsSuper.DOWNLOAD:
        case ScoreStatisticsSuper.VIDEO:
        default:
            return new DefaultScoreStatistics();
        }
    }
}
