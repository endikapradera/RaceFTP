package org.raceftp.models;

public class ProcessedTrackPoint {
    private String time;
    private int heartRate;
    private int ftp;

    public ProcessedTrackPoint() {
    }
    public ProcessedTrackPoint(String time, int heartRate, int ftp) {
        this.time = time;
        this.heartRate = heartRate;
        this.ftp = ftp;
    }

    // GETTER & SETTER
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public int getHeartRate() {
        return heartRate;
    }
    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
    public int getFtp() {
        return ftp;
    }
    public void setFtp(int ftp) {
        this.ftp = ftp;
    }


}
