package com.develogical;

public class QueryProcessor {

    public String process(String query) {
        if (query.toLowerCase().contains("how to")) {
            try {
                long startTime = System.currentTimeMillis();
                int result = ActualDBController.getEmployeesCount();
                long endTime = System.currentTimeMillis();
                System.out.println("EXECTIME(ms) for getEmployeesCount() = " + (endTime - startTime));
                return "db lookup result: " + result;
            } catch (Exception e) {
                return "db lookup result:" + e.getMessage();
            }
        }
        return "We do not support this recipe";
    }
}
