import java.io.*;
import java.util.*;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;

public class logAnalyser {
    static private String fileName = "";
    static private String dateMonth = "";
    static private String dateDay = "";
    static private String dateTime = "";
    static private PrintWriter writer = null;
    static private String timePrint = "";
    static private String firstDate = "";
    @Parameter(names = {"--time", "-t"}, description = "Frequency time. \n\t5 for 5 minutes\n\t1 for 1 hour\n\t24 " +
            "for 24hour/1day")
    private static Integer time = 24;
    @Parameter(names = {"--path", "-p"}, required = true, description = "Path of the log file.")
    private static String filePath;
    @Parameter(names = {"--help", "-h"}, help = true, hidden = true)
    private boolean help;
    @Parameter(names = {"--image", "-i"}, description = "Number of top peak images. 0 means no images.")
    private static Integer imageCount = 10;
    @Parameter(names = "--corr", description = "Calculate positive peaks correlations.")
    private static boolean corr = false;
    @Parameter(names = {"--savePath", "-sp"}, description = "Path where the results will be saved")
    private static String fileSavePath = "";
    @Parameter(names = {"--topCount", "-tp"}, description = "Number of peaks to use for correlation analysis.")
    private static Integer countPosPeaks = 100;

    //Returns corresponding int to month name
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

    //Returns date from the syslog format logfile
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

    //Sets initial date from logs first line
    private static void setDateFromFirstLine(String filePath) throws IOException {
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

    //checks if line date is new compared to last date
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

    //checks if line date is new compared to last date
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

    //checks if line date is new compared to last date
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
        if (dateDayPart.equals(dateDay) && dateMonthPart.equals(dateMonth) && dateTimePart[0].equals(dateTimePart2[0])
                && timeMinute.equals(dateTimePart2[1])) {
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

    //Writes every value from map to file
    private static void writeResultToFile(Map<String, Integer> map)
            throws FileNotFoundException, UnsupportedEncodingException {
        String time = timePrint;
        for (String key : map.keySet()) {
            int value = map.get(key);
            writer.println(time + " \t" + value + " \t" + key);
        }
        writer.println();
    }

    //Calculate average for words
    private static HashMap<String, Double> globalAverage(
            HashMap<String, Integer> globalMap, int count) {

        HashMap<String, Double> globalAverageMap = new HashMap<>();
        for (String key : globalMap.keySet()) {
            int value = globalMap.get(key);
            globalAverageMap.put(key, value / (double) count);

        }
        return globalAverageMap;
    }

    //Calculate standard deviation for words
    private static HashMap<String, Double> standardDeviation(HashMap<String, Double> globalAverageMap,
                                                             ArrayList<ArrayList> wordCountList) {
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

    //Calculate and find peaks
    private static ArrayList calculatePeaks(HashMap<String, Double> globalAverageMap,
                                            HashMap<String, Double> standardDeviationMap, ArrayList<ArrayList> listList)
            throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writerPos = new PrintWriter(fileSavePath + fileName + "\\peakPos.txt", "UTF-8");
        PrintWriter writerNeg = new PrintWriter(fileSavePath + fileName + "\\peakNeg.txt", "UTF-8");
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
                    writerPos.printf("%s %-20s count  %-15s hälve  %-15.3f keskmine  %-15.3f\n", date, key, mapValue,
                            deviationValue, globalValue);
                } else if (globalValue - 2 * deviationValue > mapValue && mapValue != 0 && globalValue >= 2.0) {
                    writerNeg.printf("%s %-20s count  %-15s hälve  %-15.3f keskmine  %-15.3f\n", date, key, mapValue,
                            deviationValue, globalValue);
                }
            }
        }
        writerPos.close();
        writerNeg.close();
        return resultList;
    }

    //Makes a list of values for a given word. Increase is for calculating correlation(no diving by 0)
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

    //Find peaks that have correlation between them
    private static void calcCorrelation(List<ArrayList> posPeaks, ArrayList wordCountList) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writerCorr = new PrintWriter(fileSavePath + fileName + "\\correlations.txt", "UTF-8");
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
                if (deviation < 2) {
                    writerCorr.println(name + "  " + name2 + "  " + deviation);
                }
            }
        }
        writerCorr.close();
        System.out.println("Peak correlation calculation complete!");
    }

    //Day dataset for creating graphs
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
                value = (int) list.get(0);
                series.add(current, new Double(value));
            } catch (SeriesException e) {
                System.err.println(e);
            }
        }

        return new TimeSeriesCollection(series);
    }

    //Hour dataset for creating graphs
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
                Hour current = new Hour(Integer.parseInt(timeSplit[0]), Integer.parseInt(dateSplit[1]),
                        getMonthInt(dateSplit[0]), 2016);
                value = (int) list.get(0);
                series.add(current, new Double(value));
            } catch (SeriesException e) {
                System.err.println(e);
            }
        }
        return new TimeSeriesCollection(series);
    }

    //Minute dataset for creating graphs
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
                Minute current = new Minute(Integer.parseInt(timeSplit[1]), Integer.parseInt(timeSplit[0]),
                        Integer.parseInt(dateSplit[1]), getMonthInt(dateSplit[0]), 2016);

                value = (int) list.get(0);
                series.add(current, new Double(value));
            } catch (SeriesException e) {
                System.err.println(e);
            }
        }
        return new TimeSeriesCollection(series);
    }

    //Checks if title contains characters which can't be in title
    private static boolean checkTitle(String title) {
        List charList = new ArrayList();
        charList.addAll(Arrays.asList('"', '#', '%', '*', ':', '<', '>', '?', '/', '\\', '|'));
        for (int i = 0; i < charList.size(); i++) {
            if (title.indexOf((char) charList.get(i)) >= 0) {
                return false;
            }
        }
        return true;
    }

    //Create one graph depending on the time interval
    private static void makeImage(String title, ArrayList wordCountList, String name) throws IOException {
        if (checkTitle(title)) {
            final XYDataset dataset;
            if (time == 24) {
                dataset = createDatasetDay(title, wordCountList);
            } else if (time == 1) {
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
            File grapsDir = new File(fileSavePath + fileName + "\\graphs");
            grapsDir.mkdir();
            File timeChart = new File(fileSavePath + fileName + "\\graphs\\" + title + ".png");
            ChartUtilities.saveChartAsPNG(timeChart, timechart, width, height);
        }
    }


    private static void makeImages(List posPeaks, ArrayList wordCountList) throws IOException {
        String name = "day";
        if (time == 5) {
            name = "minute";
        } else if (time == 1) {
            name = "hour";
        }
        int n = 0;
        int i = 0;
        ArrayList check = new ArrayList();
        while (n < imageCount && n < posPeaks.size() && i < posPeaks.size()) {
            if (!check.contains((String) ((ArrayList) posPeaks.get(i)).get(0))) {
                makeImage((String) ((ArrayList) posPeaks.get(i)).get(0), wordCountList, name);
                n++;
                check.add((String) ((ArrayList) posPeaks.get(i)).get(0));
            }
            i++;
        }
        System.out.println("Image creation complete!");
    }

    //Counts words and creates word frequency based maps.
    private static ArrayList countWords(Command command)
            throws IOException {
        ArrayList returnList = new ArrayList(); //Contains wordCountList and totalCountMap
        Map<String, Integer> wordCountMap = new HashMap<String, Integer>();
        Map<String, Integer> totalCountMap = new HashMap<String, Integer>();
        ArrayList wordCountList = new ArrayList(); //Contains wordCountMap

        //Stream Log File
        FileInputStream inputStream = new FileInputStream(filePath);
        Scanner sc = new Scanner(inputStream, "UTF-8");
        if (sc.ioException() != null) {
            throw sc.ioException();
        }
        File f = new File(filePath);
        fileName = f.getName();
        File f2 = new File(fileSavePath + fileName);
        f2.mkdir();
        writer = new PrintWriter(fileSavePath + fileName + "\\wordFrequency.txt", "UTF-8");
        System.out.println("Started counting words!");
        //Word Frequency Counting
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String subLine = line.substring(16, line.length()); //Remove syslog Format Date
            subLine = subLine.substring(subLine.indexOf(" ") + 1, subLine.length());
            subLine = subLine.substring(subLine.indexOf(" ") + 1, subLine.length());
            String[] lineSplit = subLine.split(" |:|=|\"|\'");
            if (command.execute(line)) {
                writeResultToFile(wordCountMap);
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
        writeResultToFile(wordCountMap);
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

    //Control place for all the methods.
    private static void runLogAnalyser() throws IOException {
        setDateFromFirstLine(filePath);
        ArrayList resultList = new ArrayList();
        switch (time) {
            case 24:
                resultList = countWords(new NewDay());
                break;
            case 1:
                resultList = countWords(new NewHour());
                break;
            case 5:
                resultList = countWords(new NewMinute());
                break;
            default:
                throw new ParameterException("Parameter --time or -t should be 1, 5 or 24 (found " + time + ")");
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

        List checkList = new ArrayList<>();
        List posPeaksList = new ArrayList<>();
        for (int k = 0; k < posPeaks.size(); k++) {
            if (!checkList.contains((String) ((ArrayList) posPeaks.get(k)).get(0))) {
                checkList.add((String) ((ArrayList) posPeaks.get(k)).get(0));
                posPeaksList.add(posPeaks.get(k));
            }
        }
        posPeaks = posPeaksList.subList(0, countPosPeaks > posPeaksList.size() ? posPeaksList.size() : countPosPeaks);
        if (corr) {
            calcCorrelation(posPeaks, wordCountList);
        }
        if (imageCount > 0) {
            makeImages(posPeaks, wordCountList);
        }
    }

    //Check if save path has \ in the end
    private void checkSavePath() {
        if (fileSavePath.length() > 0 && (fileSavePath.charAt(fileSavePath.length() - 1)) != '\\') {
            fileSavePath += "\\";
        }
    }

    //Checks if --help is required or no. If not then run program.
    private void checkHelp(JCommander jcommander) throws IOException, ParameterException {
        if (help) {
            jcommander.usage();
        } else {
            if (imageCount < 0) {
                throw new ParameterException("Parameter --image or -i should be positive integer (found " +
                        imageCount + ")");
            }
            checkSavePath();
            runLogAnalyser();
        }
    }


    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        logAnalyser main = new logAnalyser();
        JCommander jcommander = new JCommander(main, args);
        main.checkHelp(jcommander);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("\nTotal runtime: " + totalTime + "ms");
    }
}
/*
 * https://ristov.github.io/slct/ http://www.nec-labs.com/~gfj/xia-sdm-14.pdf
 */
