package com.develogical;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneratePerfLogs {
    public static void main(String[] args) {
        //createLogFile();
        try {
            ArrayList<Integer> samples = getSamplesFromLog("logs.txt",
                    "lookupIngredientNutrition");
            System.out.println("0");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                System.out.println("BAAAAAAAAAAAd");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Integer> getSamplesFromLog(String logfile, String methodName) throws IOException {
        ArrayList<Integer> samples = new ArrayList<>();
        String logfileContent = new String(Files.readAllBytes(Paths.get(logfile)), StandardCharsets.UTF_8);
        Pattern pattern = Pattern.compile(methodName + "\\(\\):\\[(?<samplesAsStr>[0-9]+(,[0-9]+)+)\\]");
        Matcher matcher = pattern.matcher(logfileContent);
        if(matcher.find()) {
            String[] parts = matcher.group("samplesAsStr").split(",");
            for (String part : parts) {
                if(part.length() == 0) {
                    continue;
                }
                samples.add(Integer.parseInt(part));
            }
        }
        return samples;
    }
}
