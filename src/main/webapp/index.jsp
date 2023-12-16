<%@ page import="java.util.List" %>
<%@ page import="object.Xoso" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%
    String runServlet = (String) request.getAttribute("runServlet");
    if (runServlet == null || !runServlet.equals("true")) {
        request.setAttribute("runServlet", "true");
        request.getRequestDispatcher("Servelet").include(request, response);
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Kết Quả Xổ Số </title>
    <style>
        <%@include file="/css/style.css" %>
    </style>
</head>
<body>
<div class="container">
    <form id="calendar" method="post" action="Servelet">
        <label for="date-select">Ngày: </label><input type="date" name="date" value="<%=request.getAttribute("ngay")%>"
                                                      id="date-select"
                                                      style="font-size: 17px">
        <button type="submit">Chọn ngày</button>
    </form>
    <div class="result">
        <% List<Xoso> mt = (List<Xoso>) request.getAttribute("kqxs_mt");
            List<Xoso> mn = (List<Xoso>) request.getAttribute("kqxs_mn");
            List<Xoso> mb = (List<Xoso>) request.getAttribute("kqxs_mb");
            List<List<Xoso>> all = new ArrayList<>();
            all.add(mb);
            all.add(mt);
            all.add(mn);
        %>
        <%
            if (mt.isEmpty() && mn.isEmpty() && mb.isEmpty()) {
        %>
        <h3 style="justify-content: center; align-items: center;font-size: 30px; color: red">Ngày hiện tại chưa có kết
            quả xổ số !</h3>
        <%
        } else {
            for (List<Xoso> key : all) {

                Map<String, List<Xoso>> groupedMap = new HashMap<>();

                for (Xoso doiTuong : key) {
                    String dai = doiTuong.getTen_dai();

                    if (!groupedMap.containsKey(dai))
                        groupedMap.put(dai, new ArrayList<>());

                    List<Xoso> doiTuongs = groupedMap.get(dai);
                    doiTuongs.add(doiTuong);
                }
        %>
        <section class="section">
            <header class="section-header"><h2><a
            >Kết quả xổ
                số <%=(key.get(0).getTenMien().equals("mn") ? "Miền Nam - KQXS MN" : (key.get(0).getTenMien().equals("mt") ? "Miền Trung - KQXS MT" : " Miền Bắc - KQXS MB"))%>
            </a>
            </h2>
                <h3 class="site-link">Ngày <%=request.getAttribute("ngay")%>
                </h3></header>
            <div id="mn_kqngay_22102023_kq" class="section-content">
                <table class="table-result table-xsmn">
                    <thead>
                    <tr>
                        <th class="name-prize">G</th>
                        <%
                            for (String dai : groupedMap.keySet()) {
                        %>
                        <th class="prize-col3"><a title="Xổ số <%=dai%>"><%=dai%>
                        </a></th>
                        <%
                            }
                        %>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (int i = key.get(0).getTenMien().equals("mb") ? 7 : 8; i > -1; i--) { %>
                    <tr>
                        <th><%=i == 0 ? "ĐB" : i%>
                        </th>
                        <%
                            String name = i == 0 ? "giaiĐB" : "giai" + i;
                            for (String dai : groupedMap.keySet()) {
                                List<Xoso> theoDaiAndGiai = groupedMap.get(dai).stream().filter(c -> c.getTenGiai().equals(name))
                                        .collect(Collectors.toList()); %>
                        <td>
                            <%
                                for (Xoso x : theoDaiAndGiai) {
                            %>
                            <span id="TG_prize<%=i==0 ? "Db" : i%>_item0"
                                  class="xs_prize1 <%=(i==0 || (i== 7 && key.get(0).getTenMien().equals("mb")) || i==8) ? "color_red": ""%> <%=i==0 ? "prize_db" : ""%>"><%=x.getSoTrungThuong()%></span>
                            <%
                                }%>
                        </td>
                        <%}%>
                    </tr>
                    <%
                        }
                    %>
                    <%--                    <tr>--%>
                    <%--                        <th>8</th>--%>
                    <%--                        <td><span id="TG_prize8_item0" class="xs_prize1 color_red">99</span></td>--%>
                    <%--                        <td><span id="KG_prize8_item0" class="xs_prize1 color_red">89</span></td>--%>
                    <%--                        <td><span id="DL_prize8_item0" class="xs_prize1 color_red">77</span></td>--%>
                    <%--                    </tr>--%>
                    <%--                    <tr>--%>
                    <%--                        <th>7</th>--%>
                    <%--                        <td><span id="TG_prize7_item0" class="xs_prize1">077</span></td>--%>
                    <%--                        <td><span id="KG_prize7_item0" class="xs_prize1">396</span></td>--%>
                    <%--                        <td><span id="DL_prize7_item0" class="xs_prize1">131</span></td>--%>
                    <%--                    </tr>--%>
                    <%--                    <tr>--%>
                    <%--                        <th>6</th>--%>
                    <%--                        <td><span id="TG_prize6_item0" class="xs_prize1">6098 </span> <span id="TG_prize6_item1"--%>
                    <%--                                                                                            class="xs_prize1"> 7533 </span>--%>
                    <%--                            <span id="TG_prize6_item2" class="xs_prize1"> 3134 </span></td>--%>
                    <%--                        <td><span id="KG_prize6_item0" class="xs_prize1">9076 </span> <span id="KG_prize6_item1"--%>
                    <%--                                                                                            class="xs_prize1"> 2756 </span>--%>
                    <%--                            <span id="KG_prize6_item2" class="xs_prize1"> 1896 </span></td>--%>
                    <%--                        <td><span id="DL_prize6_item0" class="xs_prize1">3413 </span> <span id="DL_prize6_item1"--%>
                    <%--                                                                                            class="xs_prize1"> 6447 </span>--%>
                    <%--                            <span id="DL_prize6_item2" class="xs_prize1"> 7953 </span></td>--%>
                    <%--                    </tr>--%>
                    <%--                    <tr>--%>
                    <%--                        <th>5</th>--%>
                    <%--                        <td><span id="TG_prize5_item0" class="xs_prize1">6521 </span></td>--%>
                    <%--                        <td><span id="KG_prize5_item0" class="xs_prize1">3388 </span></td>--%>
                    <%--                        <td><span id="DL_prize5_item0" class="xs_prize1">6284 </span></td>--%>
                    <%--                    </tr>--%>
                    <%--                    <tr>--%>
                    <%--                        <th>4</th>--%>
                    <%--                        <td><span id="TG_prize4_item0" class="xs_prize1">65053 </span> <span id="TG_prize4_item1"--%>
                    <%--                                                                                             class="xs_prize1"> 02249 </span>--%>
                    <%--                            <span id="TG_prize4_item2" class="xs_prize1"> 37407 </span> <span id="TG_prize4_item3"--%>
                    <%--                                                                                              class="xs_prize1"> 45285 </span>--%>
                    <%--                            <span id="TG_prize4_item4" class="xs_prize1"> 47339 </span> <span id="TG_prize4_item5"--%>
                    <%--                                                                                              class="xs_prize1"> 67162 </span>--%>
                    <%--                            <span id="TG_prize4_item6" class="xs_prize1"> 32212 </span></td>--%>
                    <%--                        <td><span id="KG_prize4_item0" class="xs_prize1">36450 </span> <span id="KG_prize4_item1"--%>
                    <%--                                                                                             class="xs_prize1"> 77649 </span>--%>
                    <%--                            <span id="KG_prize4_item2" class="xs_prize1"> 37923 </span> <span id="KG_prize4_item3"--%>
                    <%--                                                                                              class="xs_prize1"> 91807 </span>--%>
                    <%--                            <span id="KG_prize4_item4" class="xs_prize1"> 46011 </span> <span id="KG_prize4_item5"--%>
                    <%--                                                                                              class="xs_prize1"> 51438 </span>--%>
                    <%--                            <span id="KG_prize4_item6" class="xs_prize1"> 71808 </span></td>--%>
                    <%--                        <td><span id="DL_prize4_item0" class="xs_prize1">27398 </span> <span id="DL_prize4_item1"--%>
                    <%--                                                                                             class="xs_prize1"> 34791 </span>--%>
                    <%--                            <span id="DL_prize4_item2" class="xs_prize1"> 47933 </span> <span id="DL_prize4_item3"--%>
                    <%--                                                                                              class="xs_prize1"> 01332 </span>--%>
                    <%--                            <span id="DL_prize4_item4" class="xs_prize1"> 20932 </span> <span id="DL_prize4_item5"--%>
                    <%--                                                                                              class="xs_prize1"> 97959 </span>--%>
                    <%--                            <span id="DL_prize4_item6" class="xs_prize1"> 14380 </span></td>--%>
                    <%--                    </tr>--%>
                    <%--                    <tr>--%>
                    <%--                        <th>3</th>--%>
                    <%--                        <td><span id="TG_prize3_item0" class="xs_prize1">50578 </span> <span id="TG_prize3_item1"--%>
                    <%--                                                                                             class="xs_prize1"> 23361 </span>--%>
                    <%--                        </td>--%>
                    <%--                        <td><span id="KG_prize3_item0" class="xs_prize1">42455 </span> <span id="KG_prize3_item1"--%>
                    <%--                                                                                             class="xs_prize1"> 98985 </span>--%>
                    <%--                        </td>--%>
                    <%--                        <td><span id="DL_prize3_item0" class="xs_prize1">60039 </span> <span id="DL_prize3_item1"--%>
                    <%--                                                                                             class="xs_prize1"> 12643 </span>--%>
                    <%--                        </td>--%>
                    <%--                    </tr>--%>
                    <%--                    <tr>--%>
                    <%--                        <th>2</th>--%>
                    <%--                        <td><span id="TG_prize2_item0" class="xs_prize1">22344</span></td>--%>
                    <%--                        <td><span id="KG_prize2_item0" class="xs_prize1">74572</span></td>--%>
                    <%--                        <td><span id="DL_prize2_item0" class="xs_prize1">85726</span></td>--%>
                    <%--                    </tr>--%>
                    <%--                    <tr>--%>
                    <%--                        <th>1</th>--%>
                    <%--                        <td><span id="TG_prize1_item0" class="xs_prize1">76934</span></td>--%>
                    <%--                        <td><span id="KG_prize1_item0" class="xs_prize1">39659</span></td>--%>
                    <%--                        <td><span id="DL_prize1_item0" class="xs_prize1">02851</span></td>--%>
                    <%--                    </tr>--%>
                    <%--                    <tr>--%>
                    <%--                        <th>ĐB</th>--%>
                    <%--                        <td><span id="TG_prize_Db_item0" class="xs_prize1 prize_db">027100</span></td>--%>
                    <%--                        <td><span id="KG_prize_Db_item0" class="xs_prize1 prize_db">402281</span></td>--%>
                    <%--                        <td><span id="DL_prize_Db_item0" class="xs_prize1 prize_db">898010</span></td>--%>
                    <%--                    </tr>--%>
                    </tbody>
                </table>
            </div>
        </section>
        <%
                }
            }
        %>
    </div>
</div>
</body>
</html>
