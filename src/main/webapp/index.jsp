<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" type="text/css" href="src/main/webapp/css/style.css">
    <title>Kết Quả Xổ Số Miền Nam</title>
    <style>
    table {
        display: inline-block;
        text-align: center;
        margin-right: 20px;
    }

    th, td {
        border: 1px solid #000;
        padding: 5px;
    }


    td {
        text-align: center;
    }
    th {
        text-align: center;
    }
    .container {
        position: absolute;
        top: 95%;
        left: 50%;
        transform: translate(-50%, -50%);
    }

    </style>

</head>
<body>
<div class="container">
    <h1>Kết Quả Xổ Số Miền Bắc </br>${kqxsMN.get(0).getDayOfWeek()} ngày 22/22/2022</h1>
    <table>
        <tr>

            <th>Giải 1</th>
            <td>12345</td>
        </tr>
        <tr>
            <th>Giải 2</th>
            <td>23456</td>
        </tr>
        <tr>
            <th>Giải 3</th>
            <td>34567</td>
        </tr>
        <tr>
            <th>Giải 4</th>
            <td>45678 &emsp; 45678 &emsp;45678 </br>45678 &emsp;45678 &emsp;45678 </td>

        </tr>
        <tr>
            <th>Giải 5</th>
            <td>56789</td>

        </tr>
        <tr>
            <th>Giải 6</th>
            <td>67890</td>

        </tr>
        <tr>
            <th>Giải 7</th>
            <td>78901</td>

        </tr>
        <tr>
            <th>Giải 8</th>
            <td>89012</td>

        </tr>
        <tr>
            <th>Giải Đặc Biệt</th>
            <td>99999</td>

        </tr>
    </table>

    <h1>Kết Quả Xổ Số Miền Nam </br>${kqxsMN.get(0).getDayOfWeek()} ngày 22/22/2022</h1>
    <table>
        <tr>
            <th>Tên Đài</th>
            <td>Đài 1</td>
            <td>Đài 2</td>
            <td>Đài 3</td>
        </tr>
        <tr>

            <th>Giải 1</th>
            <td>12345</td>
            <td>98765</td>
            <td>11111</td>
        </tr>
        <tr>
            <th>Giải 2</th>
            <td>23456</td>
            <td>87654</td>
            <td>22222</td>
        </tr>
        <tr>
            <th>Giải 3</th>
            <td>34567</td>
            <td>76543</td>
            <td>33333</td>
        </tr>
        <tr>
            <th>Giải 4</th>
            <td>45678 </br> 45678 </br>45678 </br>45678 </br>45678 </br>45678 </br>45678 </td>
            <td>65432</td>
            <td>44444</td>
        </tr>
        <tr>
            <th>Giải 5</th>
            <td>56789</td>
            <td>54321</td>
            <td>55555</td>
        </tr>
        <tr>
            <th>Giải 6</th>
            <td>67890</td>
            <td>43210</td>
            <td>66666</td>
        </tr>
        <tr>
            <th>Giải 7</th>
            <td>78901</td>
            <td>32109</td>
            <td>77777</td>
        </tr>
        <tr>
            <th>Giải 8</th>
            <td>89012</td>
            <td>21098</td>
            <td>88888</td>
        </tr>
        <tr>
            <th>Giải Đặc Biệt</th>
            <td>99999</td>
            <td>11111</td>
            <td>99999</td>
        </tr>
    </table>
    <h1>Kết Quả Xổ Số Miền Trung </br>${kqxsMN.get(0).getDayOfWeek()} ngày 22/22/2022</h1>
        <table>
            <tr>
                <th>Tên Đài</th>
                <td>Đài 1</td>
                <td>Đài 2</td>
                <td>Đài 3</td>
            </tr>
            <tr>

                <th>Giải 1</th>
                <td>12345</td>
                <td>98765</td>
                <td>11111</td>
            </tr>
            <tr>
                <th>Giải 2</th>
                <td>23456</td>
                <td>87654</td>
                <td>22222</td>
            </tr>
            <tr>
                <th>Giải 3</th>
                <td>34567</td>
                <td>76543</td>
                <td>33333</td>
            </tr>
            <tr>
                <th>Giải 4</th>
                <td>45678 </br> 45678 </br>45678 </br>45678 </br>45678 </br>45678 </br>45678 </td>
                <td>65432</td>
                <td>44444</td>
            </tr>
            <tr>
                <th>Giải 5</th>
                <td>56789</td>
                <td>54321</td>
                <td>55555</td>
            </tr>
            <tr>
                <th>Giải 6</th>
                <td>67890</td>
                <td>43210</td>
                <td>66666</td>
            </tr>
            <tr>
                <th>Giải 7</th>
                <td>78901</td>
                <td>32109</td>
                <td>77777</td>
            </tr>
            <tr>
                <th>Giải 8</th>
                <td>89012</td>
                <td>21098</td>
                <td>88888</td>
            </tr>
            <tr>
                <th>Giải Đặc Biệt</th>
                <td>99999</td>
                <td>11111</td>
                <td>99999</td>
            </tr>
        </table>
</div>
</body>
</html>
