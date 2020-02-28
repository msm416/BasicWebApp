package com.develogical;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class QueryProcessor {

    public String process(String query) {
        if (query.toLowerCase().contains("how to")) {
            try {
                return "db lookup result: " + ActualDBController.getConnection().toString();
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        return "We do not support this recipe";
    }
}
