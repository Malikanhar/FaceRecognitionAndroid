package id.inditech.facerecognitionapp;

public class Absent  {
    private String name, time, tanggal;

    public Absent(String name, String time, String tanggal) {
        this.name = name;
        this.time = time;
        this.tanggal = tanggal;
    }

    public Absent(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }
}
