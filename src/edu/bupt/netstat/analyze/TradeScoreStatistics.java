package edu.bupt.netstat.analyze;
/**
 * TradeScoreStatistics
 * @author yyl
 *
 */
public class TradeScoreStatistics extends ScoreStatisticsSuper {
	private final static String TAG = "TradeScoreStatistics";
	
	public TradeScoreStatistics() {
        this.scoreWeight = new ScoreWeight(0.0245, 0.0738, 0, 0, 0, 0.1015, 0.1830,
                0.1188, 0, 0, 0, 0, 0.2661, 0.2324);
    }
   
    protected int dnsScore(int dns) { // unit: us
        return (int) (dns > 0 ? 20910.0 / (209.1 + 0.001 * dns) : 0);
    }

    protected int tcpScore(int tcp) { // unit: us
        return dnsScore(tcp);
    }
    protected int delayJitterScore(float jitter) { // unit: us
        return (int) (jitter > 0 ? 32820 / (331.4 + 0.001 * jitter) : 0);
    }

    protected int pktLossScore(float loss) {
        return (int) (100 * (1 - loss));
    }
    protected int secureScore(float ssl){
    	return (int) (19.47 * Math.log(180 * ssl));
    }
    protected int tradeTimeScore(float time){ 
         return (int) (time > 0 ? 99.1 * Math.exp(-0.04665 * time * 1e-6) : 0);  
    }
    protected int trafficScore(long traffic){// unit: B
    	float a = (float) (traffic / 1024.0);
    	int s = (int) (100 * Math.exp(-0.001107 * a));
    	return s < 0 ? 0 : s;
    }
	@Override
	public int totalScore(PacketReader reader) {
		// TODO Auto-generated method stub
		return (int) (this.scoreWeight.weightDnsScore * dnsScore(reader.avrDns)
                + this.scoreWeight.weightTcpScore * tcpScore(reader.avrRtt)
                + this.scoreWeight.weightTrafficScore * trafficScore(reader.traffic)
                + this.scoreWeight.weightDelayjitterScore * delayJitterScore(reader.delayJitter)
                + this.scoreWeight.weightPacketlossScore
                * pktLossScore(reader.pktLoss) + this.scoreWeight.weightSecureScore
                * secureScore(reader.ssl) + this.scoreWeight.weightTimeScore *
                tradeTimeScore(reader.tradeTime));
	}

}
