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
        case ScoreStatisticsSuper.DOWNLOADING:
        	return new DownloadScoreStatistics();
        case ScoreStatisticsSuper.VIDEO:
        case ScoreStatisticsSuper.TRADING:
        case ScoreStatisticsSuper.GAME:
        default:
            return new DefaultScoreStatistics();
        }
    }
}
