package com.develogical;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import umontreal.ssj.gof.GofStat;
import umontreal.ssj.probdist.*;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.util.Comparator.comparingDouble;

public class GeneratePerfLogs {
    public static void main(String[] args) {
        //createLogFile();

//        ArrayList<Double> samples = getSamplesFromLog("logs.txt",
//                "lookupIngredientNutrition");
//
//        getBestDistributionFromEmpiricalData(getSamplesFromLog("logs.txt", "lookupIngredientNutrition"));

        try {
            runForSomeTimeAndGenerateLogsOnHeroku();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runForSomeTimeAndGenerateLogsOnHeroku() throws InterruptedException,
            UnsupportedEncodingException, UnirestException {

        HttpResponse<String> response = Unirest.get("https://mmorpg-perf.herokuapp.com/?q=" +
                URLEncoder.encode("canned spam", "UTF-8"))
                .asString();

        System.out.println(response.getStatus());
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

    public static ArrayList<Double> getSamplesFromLog(String logfile, String methodName) {
        ArrayList<Double> samples = new ArrayList<>();
        String logfileContent = null;
        try {
            logfileContent = new String(Files.readAllBytes(Paths.get(logfile)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Pattern pattern = Pattern.compile(methodName + "\\(\\):\\[(?<samplesAsStr>[0-9]+(,[0-9]+)+)\\]");
        Matcher matcher = pattern.matcher(logfileContent);
        if (matcher.find()) {
            String[] parts = matcher.group("samplesAsStr").split(",");
            for (String part : parts) {
                if (part.length() == 0) {
                    continue;
                }
                samples.add(Double.parseDouble(part));
            }
        }
        return samples;
    }

    // This method is based on Kolmogorov Smirnov test, but any other could work.
    public static Distribution getBestDistributionFromEmpiricalData(ArrayList<Double> data) {
        double[] dataArray = data.stream().mapToDouble(Double::doubleValue).toArray();

        List<Distribution> distributionList = new ArrayList() {{
            add(NormalDist.getInstanceFromMLE(dataArray, dataArray.length));
            add(UniformDist.getInstanceFromMLE(dataArray, dataArray.length));
            add(LaplaceDist.getInstanceFromMLE(dataArray, dataArray.length));
            //add(CauchyDist.getInstanceFromMLE(dataArray, dataArray.length));
        }};

        int distributionListLen = distributionList.size();
        double[][] sval = new double[distributionList.size()][3];
        double[][] pval = new double[distributionList.size()][3];

        int maxPvalIndex = -1;
        double maxPval = -1d;

        for (int i = 0; i < distributionListLen; i++) {
            GofStat.kolmogorovSmirnov(dataArray, (ContinuousDistribution) distributionList.get(i), sval[i], pval[i]);
            if (maxPval < pval[i][2]) {
                maxPval = pval[i][2];
                maxPvalIndex = i;
            }
        }

        return distributionList.get(maxPvalIndex);
    }
}
