package object;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Xoso {
    private Date fullday;
    private String dayOfWeek;
    private String tenGiai;
    private String tenDai;
    private String soTrungThuong;
    private String tenMien;

    public Xoso(Date fullday, String dayOfWeek, String tenGiai, String tenDai, String soTrungThuong, String tenMien) {
        this.fullday = fullday;
        this.dayOfWeek = dayOfWeek;
        this.tenGiai = tenGiai;
        this.tenDai = tenDai;
        this.soTrungThuong = soTrungThuong;
        this.tenMien = tenMien;
    }

    public Xoso() {
    }

    public Date getFullday() {
        return fullday;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getTenGiai() {
        return tenGiai;
    }

    public String getTenDai() {
        return tenDai;
    }

    public String getSoTrungThuong() {
        return soTrungThuong;
    }

    public String getTenMien() {
        return tenMien;
    }

    public void setFullday(Date fullday) {
        this.fullday = fullday;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setTenGiai(String tenGiai) {
        this.tenGiai = tenGiai;
    }

    public void setTenDai(String tenDai) {
        this.tenDai = tenDai;
    }

    public void setSoTrungThuong(String soTrungThuong) {
        this.soTrungThuong = soTrungThuong;
    }

    public void setTenMien(String tenMien) {
        this.tenMien = tenMien;
    }


    @Override
    public String toString() {
        return this.fullday+" "+this.dayOfWeek+" "+this.tenGiai+" "+this.tenDai+" "+this.soTrungThuong+" "+this.tenMien;
    }
}
