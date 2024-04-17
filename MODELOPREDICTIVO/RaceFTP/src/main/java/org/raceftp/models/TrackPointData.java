package org.raceftp.models;

public class TrackPointData {
    private String time;
    private double speed;
    private int heartRate;
    private int distance;
    private int age;
    private int ftp;

    public TrackPointData() {
    }

    public TrackPointData(String time, double speed, int heartRate, int distance, int age, int ftp) {
        this.time = time;
        this.speed = speed;
        this.heartRate = heartRate;
        this.distance = distance;
        this.age = age;
        this.ftp = ftp;
    }

    // Métodos getter y setter
    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getFtp() {
        return ftp;
    }

    public void setFtp(int ftp) {
        this.ftp = ftp;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

}