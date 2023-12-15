package servletModel;

import dao.DAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet(name = "Servelet", value = "/")
public class Servelet extends HttpServlet {

    public void init() {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");
        DAO dao = new DAO();
        LocalDate date = LocalDate.now();
        String dateNow = date.getYear() + "-" + (date.getMonthValue() < 10 ? "0" + date.getMonthValue() : date.getMonthValue()) + "-" + (date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth() : date.getDayOfMonth());

        request.setAttribute("kqxs_mn", dao.getKQXS("mt", dateNow));
        request.setAttribute("kqxs_mt", dao.getKQXS("mn", dateNow));
        request.setAttribute("kqxs_mb", dao.getKQXS("mb", dateNow));
        request.setAttribute("ngay", dateNow);
        request.getRequestDispatcher("index.jsp").forward(request, response);

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] date = request.getParameterValues("date");
        response.setContentType("text/html");
        DAO dao = new DAO();
        request.setAttribute("kqxs_mn", dao.getKQXS("mt", date[0]));
        request.setAttribute("kqxs_mt", dao.getKQXS("mn", date[0]));
        request.setAttribute("kqxs_mb", dao.getKQXS("mb", date[0]));
        request.setAttribute("ngay", date[0]);
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

}
