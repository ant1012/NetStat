package edu.bupt.netstat.analyze;

/**
 * DownloadScoreStatistics
 * 
 * @author yyl
 * 
 */
public class DownloadScoreStatistics extends ScoreStatisticsSuper {
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

		return (int) (0.0484 * dnsScore(reader.avrDns) + 
				0.1082 * tcpScore(reader.avrRtt) + 
				0.3882 * downloadScore(reader.avrTime) + 
				0.0802 * multiThreadScore(reader.threadNum) +
				0.2813 * speedScore(reader.avrSpeed) + 
				0.0938 * pktlossScore(reader.pktLoss));
	}
}
