package object;

import java.io.Serializable;
import java.sql.Date;

public class Xoso implements Serializable {
    private Date full_date;
    private String tenGiai;
    private String ten_dai;
    private String soTrungThuong;
    private String tenMien;

    public Xoso(Date full_date, String tenGiai, String ten_dai, String soTrungThuong, String tenMien) {
        this.full_date = full_date;
        this.tenGiai = tenGiai;
        this.ten_dai = ten_dai;
        this.soTrungThuong = soTrungThuong;
        this.tenMien = tenMien;
    }

    public Xoso() {
    }

    public Date getFull_date() {
        return full_date;
    }

    public void setFull_date(Date full_date) {
        this.full_date = full_date;
    }

    public String getTenGiai() {
        return tenGiai;
    }

    public void setTenGiai(String tenGiai) {
        this.tenGiai = tenGiai;
    }

    public String getTen_dai() {
        return ten_dai;
    }

    public void setTen_dai(String ten_dai) {
        this.ten_dai = ten_dai;
    }

    public String getSoTrungThuong() {
        return soTrungThuong;
    }

    public void setSoTrungThuong(String soTrungThuong) {
        this.soTrungThuong = soTrungThuong;
    }

    public String getTenMien() {
        return tenMien;
    }

    public void setTenMien(String tenMien) {
        this.tenMien = tenMien;
    }

    @Override
    public String toString() {
        return "Xoso{" +
                "full_date=" + full_date +
                ", tenGiai='" + tenGiai + '\'' +
                ", ten_dai='" + ten_dai + '\'' +
                ", soTrungThuong='" + soTrungThuong + '\'' +
                ", tenMien='" + tenMien + '\'' +
                '}';
    }
}
