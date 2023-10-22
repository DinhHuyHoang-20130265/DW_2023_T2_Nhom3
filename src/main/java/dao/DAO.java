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
    public List<Xoso> getKQXS(String mien, String ngay) throws SQLException {
        List<Xoso> listResult=new ArrayList<Xoso>();
        String query="select full_date, day_of_week,tenGiai, ten_dai, soTrungThuong, tenMien\n" +
                "from xosomn.ketquaxs_facts join xosomn.mien_dim\n" +
                "on xosomn.ketquaxs_facts.mien_key=xosomn.mien_dim.mien_key\n" +
                "join xosomn.ngay_dim\n" +
                "on xosomn.ketquaxs_facts.ngay_key=xosomn.ngay_dim.ngay_key\n" +
                "join xosomn.giai_dim\n" +
                "on xosomn.ketquaxs_facts.giai_key=xosomn.giai_dim.giai_key\n" +
                "join xosomn.dai_dim\n" +
                "on xosomn.ketquaxs_facts.dai_key=xosomn.dai_dim.dai_key\n" +
                "where xosomn.mien_dim.tenMien='mn' and xosomn.ketquaxs_facts.dateExpire=9999 and xosomn.ngay_dim.full_date='2023-10-17'";

        Xoso kQXS=new Xoso();
        try {
            Connection connection= new ConnectDB().Connect();
            PreparedStatement ps=connection.prepareStatement(query);
            ResultSet rs= ps.executeQuery();
            while (rs.next()) {
                kQXS.setFullday(rs.getDate(1));
                kQXS.setDayOfWeek(rs.getString(2));
                kQXS.setTenGiai(rs.getString(3));
                kQXS.setTenDai(rs.getString(4));
                kQXS.setSoTrungThuong(rs.getString(5));
                kQXS.setTenMien(rs.getString(6));
                listResult.add(kQXS);
                kQXS=new Xoso();
            }
            rs.close();
            ps.close();
            connection.close();
        }catch (Exception e){

        }

        return  listResult;
    }


    public static void main(String[] args) throws SQLException {
        List<Xoso> kq=new DAO().getKQXS("","");
        for (Xoso xoso :
                kq) {
            System.out.println(xoso.toString()+"\n----------------------");
        }

    }
}
