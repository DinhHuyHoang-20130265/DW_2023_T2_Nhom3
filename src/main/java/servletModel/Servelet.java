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
import java.time.LocalDate;
import java.util.*;

@WebServlet(name = "Servelet", value = "/")
public class Servelet extends HttpServlet {

    public void init() {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");
        DAO dao = new DAO();
        List<String> miens = dao.getAllMien();
        LocalDate date = LocalDate.now();
        String dateNow = date.getYear() + "-" + (date.getMonthValue() < 10 ? "0" + date.getMonthValue() : date.getMonthValue()) + "-" + (date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth() : date.getDayOfMonth());
        HashMap<String, List<Xoso>> allXoso = new LinkedHashMap<>();
        try {
            for (String mien : miens) {
                List<Xoso> xosos = dao.getKQXS(mien, dateNow);
                if (!xosos.isEmpty()) allXoso.put(mien, xosos);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        request.setAttribute("kqxs", allXoso);
        request.setAttribute("ngay", dateNow);
        request.getRequestDispatcher("index.jsp").forward(request, response);

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] date = request.getParameterValues("date");
        response.setContentType("text/html");
        DAO dao = new DAO();
        List<String> miens = dao.getAllMien();
        HashMap<String, List<Xoso>> allXoso = new LinkedHashMap<>();
        try {
            for (String mien : miens) {
                List<Xoso> xosos = dao.getKQXS(mien, date[0]);
                if (!xosos.isEmpty()) allXoso.put(mien, xosos);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        request.setAttribute("kqxs", allXoso);
        request.setAttribute("ngay", date[0]);
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

}
