package com.develogical;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogUtils {

    public static void main(String[] args) {
        createLogFile();
    }

    public static void createLogFile() {
        try {
            Process process = Runtime.getRuntime().exec("heroku logs -n 1500");

            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                Map<String, ArrayList<Integer>> methodToExecTimes = new HashMap<>();
                Pattern pattern = Pattern.compile("EXECTIME\\(ms\\)\\sfor\\s(?<method>.*\\(\\))\\s=\\s(?<time>\\d+)");
                Matcher matcher = pattern.matcher(output);
                while (matcher.find()) {
                    String method = matcher.group("method");
                    Integer execTime = Integer.parseInt(matcher.group("time"));
                    if (methodToExecTimes.containsKey(method)) {
                        methodToExecTimes.get(method).add(execTime);
                    } else {
                        methodToExecTimes.put(method, new ArrayList() {{
                            add(execTime);
                        }});
                    }
                }
                File logs = new File("logs.txt");
                if (logs.createNewFile()) {
                    System.out.println("Log file created: " + logs.getName());
                } else {
                    System.out.println("Log file already exists. Will proceed to overwrite it.");
                }
                FileWriter myWriter = new FileWriter(logs.getName());
                for (Map.Entry<String, ArrayList<Integer>> entry : methodToExecTimes.entrySet()) {
                    myWriter.write(entry.getKey() + ":" + "[");
                    ArrayList<Integer> execTimes = entry.getValue();
                    int i;
                    for (i = 0; i < execTimes.size() - 1; i++) {
                        myWriter.write(execTimes.get(i) + ",");
                    }
                    myWriter.write(execTimes.get(i) + "");
                    myWriter.write("]\n");
                }
                //System.out.println(output);
                myWriter.close();
                System.out.println("SUCCESFULLY GENNERATED LOG FILES.");
                System.exit(0);
            } else {
                System.out.println("SOMETHING BAD HAPPENED");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
