package dao;

import connection.ConnectDB;
import object.Xoso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DAO {

    public List<Xoso> getKQXS(String mien, String ngay) {
        List<Xoso> listResult = new ArrayList<>();
        String query = "select Mien, Dai, Ngay, tenGiai, soTrungThuong \n" +
                "                 from ketquaxs_results_" + mien + " " +
                "                 where Ngay=?";

        try {
            Connection connection = ConnectDB.Connect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, ngay);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Xoso kQXS = new Xoso();
                kQXS.setTenMien(rs.getString(1));
                kQXS.setTen_dai(rs.getString(2));
                kQXS.setFull_date(rs.getDate(3));
                kQXS.setTenGiai(rs.getString(4));
                kQXS.setSoTrungThuong(rs.getString(5));
                listResult.add(kQXS);
            }
            rs.close();
            ps.close();
            connection.close();
        } catch (Exception ignored) {

        }

        return listResult;
    }


    public static void main(String[] args) {
        List<Xoso> kq = new DAO().getKQXS("mn", "2023-10-22");
        System.out.println(kq);
    }
}
