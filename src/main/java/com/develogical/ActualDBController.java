package com.develogical;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class ActualDBController implements DBController {

    //TODO: Measure time in ADBC, log times to heroku, pull from heroku ^ build model
    public static Connection getConnection() throws URISyntaxException, SQLException {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        Connection connection = DriverManager.getConnection(dbUrl);

        return connection;
    }

    @Override
    public String lookup(String input) {
        return null;
    }

    public static int getEmployeesCount() {
        String SQL = "SELECT count(*) FROM employees";
        int count = 0;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            rs.next();
            count = rs.getInt(1);
        } catch (SQLException | URISyntaxException e) {
            System.out.println(e.getMessage());
        }

        return count;
    }
}
