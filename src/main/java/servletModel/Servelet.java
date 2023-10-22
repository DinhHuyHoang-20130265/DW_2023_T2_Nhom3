package servletModel;

import dao.DAO;
import object.Xoso;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "Home", value = "/")
public class Servelet extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello World! cái khỷ";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");
        DAO dao=new DAO();
        List<Xoso> l_KQXS_MN= null;
        try {
            l_KQXS_MN = dao.getKQXS("mn","2023-10-20");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ArrayList<String> tenTinhMN=getTenDai(l_KQXS_MN);

        System.out.println("Đã vào");
        for (String i :
                tenTinhMN) {
            System.out.println(i);
        }
        request.setAttribute("kqxsMN",l_KQXS_MN);
        request.setAttribute("tinhMN",tenTinhMN);
        request.getRequestDispatcher("index.jsp").forward(request,response);



    }
    public ArrayList<String> getTenDai(List<Xoso> list){
        ArrayList<String> listResult=new ArrayList<String>();
        for (Xoso xoso :
                list) {
            if(!listResult.contains(xoso.getTenDai())){
                listResult.add(xoso.getTenDai());
            }
        }
        return listResult;
    }


}
