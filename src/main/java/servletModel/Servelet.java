package servletModel;

import dao.DAO;
import object.Xoso;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "Home", value = "/")
public class Servelet extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello World! cái khỷ";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");
        DAO dao = new DAO();
        List<List<Xoso>> list = new ArrayList<>();
        List<Xoso> l_KQXS_MN;
        try {
            l_KQXS_MN = dao.getKQXS("mn", "2023-10-20");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        request.setAttribute("kqxsMN", l_KQXS_MN);
        request.getRequestDispatcher("index.jsp").forward(request, response);

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

    }
}
