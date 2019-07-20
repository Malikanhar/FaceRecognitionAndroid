package id.inditech.facerecognitionapp;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String nama;
    private String id;
    private List<Mat> faces = new ArrayList<>();
    private String tanggal;
    private String waktu;

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public User(){

    }
    public User(String nama){
        this.nama = nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getNama() {
        return nama;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<Mat> getFaces() {
        return faces;
    }

    public void addFace(Mat face){
        faces.add(face);
    }
}
