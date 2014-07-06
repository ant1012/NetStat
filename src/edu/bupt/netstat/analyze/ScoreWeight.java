package edu.bupt.netstat.analyze;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * ScoreWeight
 * 
 * @author zzz
 * 
 */
public class ScoreWeight implements Parcelable {
    public double weightDnsScore;
    public double weightTcpScore;
    public double weightRespScore;
    public double weightLoadScore;
    public double weightSpeedScore;
    public double weightTrafficScore;
    public double weightDelayjitterScore;
    public double weightPacketlossScore;
    public double weightDownloadScore;
    public double weightMultithreadScore;

    public ScoreWeight() {
        this.weightDnsScore = 0;
        this.weightTcpScore = 0;
        this.weightRespScore = 0;
        this.weightLoadScore = 0;
        this.weightSpeedScore = 0;
        this.weightTrafficScore = 0;
        this.weightDelayjitterScore = 0;
        this.weightPacketlossScore = 0;
        this.weightDownloadScore = 0;
        this.weightMultithreadScore = 0;
    }

    public ScoreWeight(double weightDnsScore, double weightTcpScore,
            double weightRespScore, double weightLoadScore,
            double weightSpeedScore, double weightTrafficScore,
            double weightDelayjitterScore, double weightPacketlossScore,
            double weightDownloadScore, double weightMultithreadScore) {
        this.weightDnsScore = weightDnsScore;
        this.weightTcpScore = weightTcpScore;
        this.weightRespScore = weightRespScore;
        this.weightLoadScore = weightLoadScore;
        this.weightSpeedScore = weightSpeedScore;
        this.weightTrafficScore = weightTrafficScore;
        this.weightDelayjitterScore = weightDelayjitterScore;
        this.weightPacketlossScore = weightPacketlossScore;
        this.weightDownloadScore = weightDownloadScore;
        this.weightMultithreadScore = weightMultithreadScore;
    }

    public static final Parcelable.Creator<ScoreWeight> CREATOR = new Creator<ScoreWeight>() {

        @Override
        public ScoreWeight createFromParcel(Parcel source) {
            ScoreWeight w = new ScoreWeight();
            w.weightDnsScore = source.readDouble();
            w.weightTcpScore = source.readDouble();
            w.weightRespScore = source.readDouble();
            w.weightLoadScore = source.readDouble();
            w.weightSpeedScore = source.readDouble();
            w.weightTrafficScore = source.readDouble();
            w.weightDelayjitterScore = source.readDouble();
            w.weightPacketlossScore = source.readDouble();
            w.weightDownloadScore = source.readDouble();
            w.weightMultithreadScore = source.readDouble();
            return w;
        }

        @Override
        public ScoreWeight[] newArray(int size) {
            return new ScoreWeight[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeDouble(weightDnsScore);
        arg0.writeDouble(weightTcpScore);
        arg0.writeDouble(weightRespScore);
        arg0.writeDouble(weightLoadScore);
        arg0.writeDouble(weightSpeedScore);
        arg0.writeDouble(weightTrafficScore);
        arg0.writeDouble(weightDelayjitterScore);
        arg0.writeDouble(weightPacketlossScore);
        arg0.writeDouble(weightDownloadScore);
        arg0.writeDouble(weightMultithreadScore);
    }
}
