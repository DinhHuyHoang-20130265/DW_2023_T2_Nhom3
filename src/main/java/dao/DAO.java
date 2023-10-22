package dao;

import connection.ConnectDB;
import object.Xoso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DAO {

    public List<String> getAllMien() {
        List<String> listResult = new ArrayList<>();
        String query = "select tenMien\n" +
                "from mien_dim\n";
        try {
            Connection connection = ConnectDB.Connect();
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listResult.add(rs.getString(1));
            }
            rs.close();
            ps.close();
            connection.close();
        } catch (Exception ignored) {

        }

        return listResult;
    }

    public List<Xoso> getKQXS(String mien, String ngay) throws SQLException {
        List<Xoso> listResult = new ArrayList<>();
        String query = "select ngay_dim.full_date, giai_dim.tenGiai, dai_dim.ten_dai, ketquaxs_facts.soTrungThuong, mien_dim.tenMien \n" +
                "                 from ketquaxs_facts join mien_dim  \n" +
                "                 on ketquaxs_facts.mien_key=mien_dim.mien_key  \n" +
                "                 join ngay_dim  \n" +
                "                 on ketquaxs_facts.ngay_key=ngay_dim.ngay_key  \n" +
                "                 join giai_dim\n" +
                "                 on ketquaxs_facts.giai_key=giai_dim.giai_key  \n" +
                "                 join dai_dim  \n" +
                "                 on ketquaxs_facts.dai_key=dai_dim.dai_key  \n" +
                "                 where mien_dim.tenMien=? and ngay_dim.full_date=?";

        try {
            Connection connection = ConnectDB.Connect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, mien);
            ps.setString(2, ngay);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Xoso kQXS = new Xoso();
                kQXS.setFull_date(rs.getDate(1));
                kQXS.setTenGiai(rs.getString(2));
                kQXS.setTen_dai(rs.getString(3));
                kQXS.setSoTrungThuong(rs.getString(4));
                kQXS.setTenMien(rs.getString(5));
                listResult.add(kQXS);
            }
            rs.close();
            ps.close();
            connection.close();
        } catch (Exception ignored) {

        }

        return listResult;
    }


    public static void main(String[] args) throws SQLException {
        List<String> mien = new DAO().getAllMien();
        System.out.println(mien);
        List<Xoso> kq = new DAO().getKQXS("mt", "2023-10-22");
        System.out.println(kq.size());
    }
}
