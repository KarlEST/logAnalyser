import java.io.*;
import java.util.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class logAnalyser {
    static private String dateMonth = "";
    static private String dateDay = "";
    static private String dateTime = "";
    static private PrintWriter writer = null;
    static private String timePrint = "";
    static private String firstDate = "";


    private static boolean newDay(String line) {
        ArrayList<String> date = getDate(line);
        String dateMonthPart = date.get(0);
        String dateDayPart = date.get(1);
        String dateTimePart = date.get(2);

        if (dateDayPart.equals(dateDay) && dateMonthPart.equals(dateMonth)) {
            return false;
        }

        timePrint = dateMonth + " " + dateDay + " " + dateTime;
        dateDay = dateDayPart;
        dateMonth = dateMonthPart;
        String[] dateTimeParts = dateTimePart.split(":");
        int time = Integer.parseInt(dateTimeParts[1]);
        time = time - (time % 5);
        String timePrint = String.valueOf(time);
        if (time < 10) {
            timePrint = "0" + String.valueOf(time);
        }
        dateTime = dateTimeParts[0] + ":" + timePrint + ":00";


        return true;
    }

    private static int getMonthInt(String month) {
        switch (dateMonth) {
            case "Jan":
                return 1;
            case "Feb":
                return 2;
            case "Mar":
                return 3;
            case "Apr":
                return 4;
            case "May":
                return 5;
            case "Jun":
                return 6;
            case "Jul":
                return 7;
            case "Aug":
                return 8;
            case "Sep":
                return 9;
            case "Oct":
                return 10;
            case "Nov":
                return 11;
            case "Dec":
                return 12;
        }
        return 1;
    }

    private static void firstLine(String filePath) throws IOException {
        BufferedReader firstLine = new BufferedReader(new FileReader(filePath));
        String firstLineText = firstLine.readLine();
        ArrayList<String> firstLineParts = getDate(firstLineText);
        dateMonth = firstLineParts.get(0);
        dateDay = firstLineParts.get(1);

        firstDate += String.valueOf(getMonthInt(dateMonth));
        firstDate += " ";
        firstDate += dateDay;
        firstDate += " ";
        String[] dateTimePart = firstLineParts.get(2).split(":");
        int time = Integer.parseInt(dateTimePart[1]);
        time = time - (time % 5);
        String timePrint = String.valueOf(time);
        if (time < 10) {
            timePrint = "0" + String.valueOf(time);
        }
        dateTime = dateTimePart[0] + ":" + timePrint + ":00";

        firstDate += dateTime;
        firstDate += " 2016";
        firstLine.close();


    }


    private static ArrayList<String> getDate(String line) {
        String[] splitList = line.split("");

        String month = splitList[0] + splitList[1] + splitList[2];
        String day;
        if (" ".equals(splitList[4])) {
            day = splitList[5];
        } else {
            day = splitList[4] + splitList[5];
        }
        String time = "";
        for (int i = 7; i <= 14; i++) {
            time += splitList[i];

        }
        ArrayList<String> result = new ArrayList();
        result.add(month);
        result.add(day);
        result.add(time);
        return result;
    }


    private static void prindiTulemus(Map<String, Integer> map)
            throws FileNotFoundException, UnsupportedEncodingException {

        String time = timePrint;
        for (String key : map.keySet()) {
            int value = map.get(key);
            writer.println(time + " \t" + value + " \t" + key);
        }
        writer.println();
    }


    private static ArrayList calculatePeaks(HashMap<String, Double> globalAverageMap, HashMap<String, Double> standardDeviationMap, ArrayList<ArrayList> listList)
            throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writerPos = new PrintWriter("peakPos.txt", "UTF-8");
        PrintWriter writerNeg = new PrintWriter("peakNeg.txt", "UTF-8");
        ArrayList resultList = new ArrayList();
        for (String key : globalAverageMap.keySet()) {
            Double globalValue = globalAverageMap.get(key);
            Double deviationValue = standardDeviationMap.get(key);
            for (ArrayList list : listList) {
                Integer mapValue = 0;
                HashMap<String, Integer> map = (HashMap<String, Integer>) list.get(0);
                String date = (String) list.get(1);
                if (map.containsKey(key)) {
                    mapValue = map.get(key);
                }
                if (globalValue + 2 * deviationValue < mapValue && globalValue >= 2) {
                    ArrayList peak = new ArrayList();
                    peak.add(key);
                    peak.add(mapValue);
                    peak.add(date);
                    resultList.add(peak);
                    writerPos.printf("%s %-20s count  %-15s hälve  %-15.3f keskmine  %-15.3f\n", date, key, mapValue, deviationValue, globalValue);
                } else if (globalValue - 2 * deviationValue > mapValue && mapValue != 0 && globalValue >= 2.0) {
                    writerNeg.printf("%s %-20s count  %-15s hälve  %-15.3f keskmine  %-15.3f\n", date, key, mapValue, deviationValue, globalValue);

                }
            }
        }
        writerPos.close();
        writerNeg.close();
        return resultList;
    }

    private static HashMap<String, Double> standardDeviation(HashMap<String, Double> globalAverageMap, ArrayList<ArrayList> wordCountList) {
        HashMap<String, Double> sdMap = new HashMap<>();
        int dayCount = wordCountList.size();
        for (String key : globalAverageMap.keySet()) {
            Double globalValue = globalAverageMap.get(key);
            Double deviationSum = 0.0;
            for (ArrayList list : wordCountList) {
                HashMap<String, Integer> wordCountMap = (HashMap<String, Integer>) list.get(0);
                int mapValue = 0;
                if (wordCountMap.containsKey(key)) {
                    mapValue = wordCountMap.get(key);
                }
                Double deviation = Math.pow((mapValue - globalValue), 2);
                deviationSum += deviation;
            }
            Double standardDeviation = Math.sqrt(deviationSum / dayCount);
            sdMap.put(key, standardDeviation);
        }
        return sdMap;
    }

    private static HashMap<String, Double> globalAverage(
            HashMap<String, Integer> globalMap, int count) {

        HashMap<String, Double> globalAverageMap = new HashMap<>();
        for (String key : globalMap.keySet()) {
            int value = globalMap.get(key);
            globalAverageMap.put(key, value / (double) count);

        }
        return globalAverageMap;

    }


    private static void run(int time, String filePath) throws IOException {
        firstLine(filePath);
        ArrayList resultList = new ArrayList();
        switch (time) {
            case 24:
                resultList = workDay(filePath, new NewDay());
                break;
            case 1:
                resultList = workDay(filePath, new NewHour());
                break;
            case 5:
                resultList = workDay(filePath, new NewMinute());
                break;
        }
        System.out.println("Word counting complete!");
        ArrayList wordCountList = (ArrayList) resultList.get(0);
        HashMap<String, Double> globalAverageMap = globalAverage((HashMap) resultList.get(1), wordCountList.size());
        System.out.println("Average calculation complete!");
        HashMap<String, Double> standardDeviationMap = standardDeviation(globalAverageMap, (ArrayList) wordCountList);
        System.out.println("Standard deviation calculation complete!");
        List posPeaks = calculatePeaks(globalAverageMap, standardDeviationMap, (ArrayList) wordCountList);
        System.out.println("Peak calculation complete!");

        Collections.sort(posPeaks, new Comparator<List<Integer>>() {
            @Override
            public int compare(List<Integer> a, List<Integer> b) {
                return b.get(1).compareTo(a.get(1));
            }
        });
        //make jpeg graph
        String name = "day";
        if (time == 5) {
            name = "minute";
        } else if (time == 1) {
            name = "hour";
        }
        int count = 100;
        List checkList = new ArrayList<>();
        List posPeaksList = new ArrayList<>();
        for(int k = 0;k<posPeaks.size();k++){
            if(!checkList.contains((String) ((ArrayList) posPeaks.get(k)).get(0))){
                checkList.add((String) ((ArrayList) posPeaks.get(k)).get(0));
                posPeaksList.add(posPeaks.get(k));
            }
        }
        posPeaks = posPeaksList.subList(0, count > posPeaksList.size() ? posPeaksList.size() : count);
        samePeaksDay(posPeaks, wordCountList);
       int n=0;
        int i = 0;
        ArrayList check = new ArrayList();
        while(n<50 && n < posPeaks.size()&&i<posPeaks.size()){
            //System.out.println((String) ((ArrayList) posPeaks.get(i)).get(0));
            if(!check.contains((String) ((ArrayList) posPeaks.get(i)).get(0))){
                makeImage((String) ((ArrayList) posPeaks.get(i)).get(0), wordCountList, time, name);
                n++;
                check.add((String) ((ArrayList) posPeaks.get(i)).get(0));
            }

            i++;

        }
        System.out.println("Image creation complete!");


    }

    private static void samePeaksDay(List<ArrayList> posPeaks, ArrayList wordCountList) {
        for (int i = 0; i < posPeaks.size(); i++) {
            ArrayList list = posPeaks.get(i);
            String name = (String) list.get(0);

            ArrayList<ArrayList> values = makeValueList(name, wordCountList, true);
            for (int j = i + 1; j < posPeaks.size(); j++) {
                ArrayList list2 = posPeaks.get(j);
                String name2 = (String) list2.get(0);
                if (name.equals(name2)) {
                    continue;
                }
                ArrayList<ArrayList> values2 = makeValueList(name2, wordCountList, true);
                ArrayList<Double> dividedPeaks = new ArrayList();
                double averageSum = 0;
                for (int k = 0; k < values.size(); k++) {

                    ArrayList<Integer> value = values.get(k);
                    ArrayList<Integer> value2 = values2.get(k);
                    double number = value.get(0) / (double) value2.get(0);
                    number = Math.log(number);
                    dividedPeaks.add(number);
                    averageSum += number;

                }
                double average = averageSum / values.size();

                double deviationSum = 0;
                for (Double averageS : dividedPeaks) {
                    deviationSum += (averageS - average) * (averageS - average);
                }
                double deviation = Math.sqrt(deviationSum / values.size());

               /* if(name.equals("166.111.7.72")&&name2.equals("166.111.26.5")){
                    System.out.println("average "+average);
                    System.out.println("count "+ values.size());
                    System.out.println("deviation "+deviation);

                }*/
                if (deviation < 2) {
                    System.out.println(name + "  " + name2 + "  " + deviation);
                }
            }

        }
    }

    private static XYDataset createDatasetDay(String title, ArrayList<ArrayList> wordCountList) {
        final TimeSeries series = new TimeSeries(title);
        double value = 0.0;
        ArrayList<ArrayList> values = makeValueList(title, wordCountList, false);
        for (int i = 0; i < values.size(); i++) {
            try {
                ArrayList list = values.get(i);
                String time = (String) list.get(1);
                String[] dateSplit = time.split(" ");
                Day current = new Day(Integer.parseInt(dateSplit[1]), getMonthInt(dateSplit[0]), 2016);
                //String[] timeSplit = dateSplit[2].split(":");
                //Hour current = new Hour(Integer.parseInt(timeSplit[0]), Integer.parseInt(dateSplit[1]), getMonthInt(dateSplit[0]), 2016);

                value = (int) list.get(0);
                series.add(current, new Double(value));
            } catch (SeriesException e) {
                System.err.println(e);
            }
        }

        return new TimeSeriesCollection(series);
    }

    private static XYDataset createDatasetHour(String title, ArrayList<ArrayList> wordCountList) {
        final TimeSeries series = new TimeSeries(title, Hour.class);
        double value = 0.0;
        ArrayList<ArrayList> values = makeValueList(title, wordCountList, false);
        for (int i = 0; i < values.size(); i++) {
            try {
                ArrayList list = values.get(i);
                String time = (String) list.get(1);
                String[] dateSplit = time.split(" ");

                String[] timeSplit = dateSplit[2].split(":");
                Hour current = new Hour(Integer.parseInt(timeSplit[0]), Integer.parseInt(dateSplit[1]), getMonthInt(dateSplit[0]), 2016);

                value = (int) list.get(0);
                series.add(current, new Double(value));
            } catch (SeriesException e) {
                System.err.println(e);
            }
        }
        return new TimeSeriesCollection(series);
    }

    private static XYDataset createDatasetMinute(String title, ArrayList<ArrayList> wordCountList) {
        final TimeSeries series = new TimeSeries(title, Minute.class);
        double value = 0.0;
        ArrayList<ArrayList> values = makeValueList(title, wordCountList, false);
        for (int i = 0; i < values.size(); i++) {
            try {
                ArrayList list = values.get(i);
                String time = (String) list.get(1);
                String[] dateSplit = time.split(" ");

                String[] timeSplit = dateSplit[2].split(":");
                Minute current = new Minute(Integer.parseInt(timeSplit[1]), Integer.parseInt(timeSplit[0]), Integer.parseInt(dateSplit[1]), getMonthInt(dateSplit[0]), 2016);

                value = (int) list.get(0);
                series.add(current, new Double(value));
            } catch (SeriesException e) {
                System.err.println(e);
            }
        }
        return new TimeSeriesCollection(series);
    }


    private static void makeImage(String title, ArrayList wordCountList, int type, String name) throws IOException {
        final XYDataset dataset;
        if (type == 24) {
            dataset = createDatasetDay(title, wordCountList);
        } else if (type == 1) {
            dataset = createDatasetHour(title, wordCountList);
        } else {
            dataset = createDatasetMinute(title, wordCountList);
        }
        JFreeChart timechart = ChartFactory.createTimeSeriesChart(
                title,
                name,
                "Value",
                dataset,
                false,
                false,
                false);

        int width = 560; /* Width of the image */
        int height = 370; /* Height of the image */
        File grapsDir = new File("graphs");
        grapsDir.mkdir();
        File timeChart = new File(".\\graphs\\" + title + ".png");
        ChartUtilities.saveChartAsPNG(timeChart, timechart, width, height);

    }


    private static ArrayList makeValueList(String name, ArrayList<ArrayList> wordCountList, boolean increase) {
        ArrayList resultList = new ArrayList();
        for (ArrayList list : wordCountList) {
            Integer mapValue = 0;
            HashMap<String, Integer> wordCountMap = (HashMap<String, Integer>) list.get(0);
            if (wordCountMap.containsKey(name)) {
                mapValue = wordCountMap.get(name);
            }
            if (increase) {
                mapValue++;
            }
            ArrayList helpList = new ArrayList();
            helpList.add(mapValue);
            helpList.add(list.get(1));
            resultList.add(helpList);
        }
        return resultList;
    }


    private static ArrayList workDay(String filePath, Command command)
            throws IOException {
        ArrayList returnList = new ArrayList(); //Contains wordCountList and totalCountMap
        Map<String, Integer> wordCountMap = new HashMap<String, Integer>();
        Map<String, Integer> totalCountMap = new HashMap<String, Integer>();
        ArrayList wordCountList = new ArrayList(); //Contains wordCountMap
        writer = new PrintWriter("dayWordCounts.txt", "UTF-8");

        //Stream Log File
        FileInputStream inputStream = new FileInputStream(filePath);
        Scanner sc = new Scanner(inputStream, "UTF-8");
        if (sc.ioException() != null) {
            throw sc.ioException();
        }

        //Word Frequency Counting
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String subLine = line.substring(16, line.length()); //Remove syslog Format Date
            subLine = subLine.substring(subLine.indexOf(" ") + 1, subLine.length());
            subLine = subLine.substring(subLine.indexOf(" ") + 1, subLine.length());
            String[] lineSplit = subLine.split(" |:|=|\"|\'");
            if (command.execute(line)) {
                prindiTulemus(wordCountMap);
                ArrayList mapTime = new ArrayList();//mapTime is for having date in peak files
                mapTime.add(wordCountMap);
                mapTime.add(timePrint);
                wordCountList.add(mapTime);
                wordCountMap = new HashMap<String, Integer>();

            }
            for (String word : lineSplit) {
                if ("".equals(word)) {
                    continue;
                }
                if (wordCountMap.containsKey(word)) {
                    totalCountMap.put(word, totalCountMap.get(word) + 1);
                    wordCountMap.put(word, wordCountMap.get(word) + 1);
                } else {
                    if (totalCountMap.containsKey(word)) {
                        totalCountMap.put(word, totalCountMap.get(word) + 1);
                    } else {
                        totalCountMap.put(word, 1);
                    }
                    wordCountMap.put(word, 1);
                }

            }
        }

        timePrint = dateMonth + " " + dateDay + " " + dateTime;
        prindiTulemus(wordCountMap);
        writer.close();
        ArrayList mapTime = new ArrayList();
        mapTime.add(wordCountMap);
        mapTime.add(timePrint);
        wordCountList.add(mapTime);
        //Close Log File Stream
        inputStream.close();
        sc.close();
        returnList.add(wordCountList);
        returnList.add(totalCountMap);
        return returnList;
    }


    interface Command {
        public Boolean execute(String line);
    }

    private static class NewDay implements Command {
        public Boolean execute(String line) {
            return newDay(line);
        }
    }

    private static class NewHour implements Command {
        public Boolean execute(String line) {
            return newHour(line);
        }
    }

    private static class NewMinute implements Command {
        public Boolean execute(String line) {
            return newMinute(line);
        }


    }

    public static void callCommand(Command command, String line) {
        command.execute(line);
    }

    private static boolean newHour(String line) {
        ArrayList<String> date = getDate(line);
        String dateMonthPart = date.get(0);
        String dateDayPart = date.get(1);
        String[] dateTimePart = date.get(2).split(":");
        String[] dateTimePart2 = dateTime.split(":");

        if (dateDayPart.equals(dateDay) && dateMonthPart.equals(dateMonth) && dateTimePart[0].equals(dateTimePart2[0])) {
            return false;
        }

        timePrint = dateMonth + " " + dateDay + " " + dateTime;
        dateDay = dateDayPart;
        dateMonth = dateMonthPart;
        int time = Integer.parseInt(dateTimePart[1]);
        time = time - (time % 5);
        String timePrint = String.valueOf(time);
        if (time < 10) {
            timePrint = "0" + String.valueOf(time);
        }
        dateTime = dateTimePart[0] + ":" + timePrint + ":00";


        return true;
    }

    private static boolean newMinute(String line) {
        ArrayList<String> date = getDate(line);
        String dateMonthPart = date.get(0);
        String dateDayPart = date.get(1);
        String[] dateTimePart = date.get(2).split(":");
        String[] dateTimePart2 = dateTime.split(":");
        int minute = Integer.parseInt(dateTimePart[1]);
        minute = minute - (minute % 5);
        String timeMinute = String.valueOf(minute);
        if (minute < 10) {
            timeMinute = "0" + String.valueOf(minute);
        }

        if (dateDayPart.equals(dateDay) && dateMonthPart.equals(dateMonth) && dateTimePart[0].equals(dateTimePart2[0]) && timeMinute.equals(dateTimePart2[1])) {
            return false;
        }

        timePrint = dateMonth + " " + dateDay + " " + dateTime;
        dateDay = dateDayPart;
        dateMonth = dateMonthPart;
        int time = Integer.parseInt(dateTimePart[1]);
        time = time - (time % 5);
        String timePrint = String.valueOf(time);
        if (time < 10) {
            timePrint = "0" + String.valueOf(time);
        }
        dateTime = dateTimePart[0] + ":" + timePrint + ":00";


        return true;
    }

    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();
        //Käsk jooksutamiseks: run(int aeg,String logi_fail);


        //run(5, "C:\\Users\\karll\\Documents\\logAnalyser\\katse.log");
        run(5, "C:\\Users\\karll\\Documents\\logAnalyser\\logs\\kosmos-auth-logs\\auth.log.4");

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("\nTotal runtime: " + totalTime + "ms");


    }
}

/*
 * Count ja date sõnadega mis saab. Kui järsku on ka logifailides. äkki _count_?
 * https://ristov.github.io/slct/ http://www.nec-labs.com/~gfj/xia-sdm-14.pdf
 */

//https://stackoverflow.com/questions/1200054/java-library-for-parsing-command-line-parameters