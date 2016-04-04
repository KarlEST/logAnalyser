import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.RefineryUtilities;

public class logAnalyser extends ApplicationFrame {
    static String dateMonth = "";
    static String dateDay = "";
    static String dateTime = "";
    static PrintWriter writer = null;
    static String timePrint = "";

    public logAnalyser(final String title) {
        super(title);
        final XYDataset dataset = createDataset();
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
    }

    private XYDataset createDataset() {
        final TimeSeries series = new TimeSeries("Random Data");//, Second.class
        Day current = new Day();
        double value = 100.0;
        for (int i = 0; i < 4000; i++) {
            try {
                value = value + Math.random() - 0.5;
                series.add(current, new Double(value));
                current = (Day) current.next();
            } catch (SeriesException e) {
                System.err.println(e);
            }
        }

        return new TimeSeriesCollection(series);
    }

    private JFreeChart createChart(final XYDataset dataset) {
        return ChartFactory.createTimeSeriesChart("1 day", "Days",
                "Value", dataset, false, false, false);
    }

    public static boolean newDay(String line) {
        ArrayList<String> date = getDate(line);
        String dateMonthPart = date.get(0);
        String dateDayPart = date.get(1);
        String dateTimePart = date.get(2);
        if (dateDayPart.equals(dateDay) && dateMonthPart.equals(dateMonth)) {
            return false;
        } else {
            timePrint = dateMonth + " " + dateDay + " " + dateTime;
            if (!dateDayPart.equals(dateDay)) {
                dateDay = dateDayPart;
            }
            if (!dateMonthPart.equals(dateMonth)) {
                dateMonth = dateMonthPart;
            }
            if (!dateTimePart.equals(dateTime)) {
                dateTime = dateTimePart;
            }
        }
        return true;
    }

    public static ArrayList readFile(String filePath) throws IOException {
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(filePath);
            sc = new Scanner(inputStream, "UTF-8");
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
            ArrayList arrayHelp = new ArrayList();
            arrayHelp.add(inputStream);
            arrayHelp.add(sc);
            return arrayHelp;
        } finally {

        }
    }

    public static void closeFile(Scanner sc, FileInputStream inputStream)
            throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (sc != null) {
            sc.close();
        }
    }

    public static void firstLine(String filePath) throws IOException {
        BufferedReader firstLine = new BufferedReader(new FileReader(filePath));
        String firstLineText = firstLine.readLine();
        ArrayList<String> firstLineParts = getDate(firstLineText);
        dateMonth = firstLineParts.get(0);
        dateDay = firstLineParts.get(1);
        dateTime = firstLineParts.get(2);
        firstLine.close();
    }

    public static ArrayList<String> getDate(String line) {
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

    public static ArrayList workFileDay(Scanner sc)
            throws FileNotFoundException, UnsupportedEncodingException {
        writer = new PrintWriter("tulemus.txt", "UTF-8");
        Map<String, Integer> map = new HashMap<String, Integer>();
        Map<String, Integer> globalMap = new HashMap<String, Integer>();
        map.put("count", 0);
        globalMap.put("count", 0);
        globalMap.put("dayCount", 1);
        ArrayList list = new ArrayList();

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] lineSplit = line.split(" |:|=|\"|\'");
            if (newDay(line)) {
                ArrayList mapTime = new ArrayList();
                mapTime.add(map);
                String[] dateNowSplit = timePrint.split(" ");
                mapTime.add(dateNowSplit[0] + " " + dateNowSplit[1]);
                list.add(mapTime);
                prindiTulemus(map);
                map = new HashMap<String, Integer>();
                for (int i = 5; i < lineSplit.length; i++) {
                    String word = lineSplit[i];
                    if ("".equals(word)) {
                        continue;
                    }
                    if (map.containsKey(word)) {
                        globalMap.put(word, globalMap.get(word) + 1);
                        map.put(word, map.get(word) + 1);
                    } else {
                        if (globalMap.containsKey(word)) {
                            globalMap.put(word, globalMap.get(word) + 1);
                        } else {
                            globalMap.put(word, 1);
                        }

                        map.put(word, 1);
                    }
                }
                map.put("count", 1);
                globalMap.put("count", globalMap.get("count") + 1);
                globalMap.put("dayCount", globalMap.get("dayCount") + 1);
            } else {

                for (int i = 5; i < lineSplit.length; i++) {
                    String word = lineSplit[i];
                    if ("".equals(word)) {
                        continue;
                    }
                    if (map.containsKey(word)) {
                        globalMap.put(word, globalMap.get(word) + 1);
                        map.put(word, map.get(word) + 1);
                    } else {
                        if (globalMap.containsKey(word)) {
                            globalMap.put(word, globalMap.get(word) + 1);
                        } else {
                            globalMap.put(word, 1);
                        }
                        map.put(word, 1);
                    }
                }
                map.put("count", map.get("count") + 1);
                globalMap.put("count", globalMap.get("count") + 1);
            }
        }

        ArrayList mapTime = new ArrayList();
        mapTime.add(map);
        mapTime.add(dateMonth + " " + dateDay);
        list.add(mapTime);
        // kuva(list,3);
        timePrint = dateMonth + " " + dateDay + " " + dateTime;
        prindiTulemus(map);
        ArrayList listAbi = new ArrayList();
        listAbi.add(globalMap);
        // kuva(listAbi, 0);
        ArrayList returnList = new ArrayList();
        returnList.add(list);
        returnList.add(globalMap);
        writer.close();
        return returnList;
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

    public static void kuva(ArrayList kuvaList, int i) {

        Map<String, Integer> map = (HashMap) kuvaList.get(i);
        for (String key : map.keySet()) {
            int value = map.get(key);
            System.out.println(key + " : " + value);
        }
    }

    public static void run(int time, String filePath) throws IOException {
        ArrayList fileReadArray = readFile(filePath);
        FileInputStream inputStream = (FileInputStream) fileReadArray.get(0);
        Scanner sc = (Scanner) fileReadArray.get(1);
        firstLine(filePath);
        ArrayList resultList = new ArrayList();
        switch (time) {
            case 24:
                resultList = workFileDay(sc);
                break;
            case 1:
                break;
            case 5:
                break;
        }
        ArrayList listList = (ArrayList) resultList.get(0);
        HashMap<String, Double> globalAverageMap = globalAverage((HashMap) resultList
                .get(1));
        HashMap<String, Double> standardDeviationMap = standardDeviation(
                globalAverageMap, (ArrayList) listList);
        calculatePeaks(globalAverageMap, standardDeviationMap,
                (ArrayList) listList);
        closeFile(sc, inputStream);

    }

    private static void calculatePeaks(
            HashMap<String, Double> globalAverageMap,
            HashMap<String, Double> standardDeviationMap,
            ArrayList<ArrayList> listList) throws FileNotFoundException,
            UnsupportedEncodingException {
        PrintWriter writerPos = new PrintWriter("peakPos.txt", "UTF-8");
        PrintWriter writerNeg = new PrintWriter("peakNeg.txt", "UTF-8");
        for (String key : globalAverageMap.keySet()) {
            Double globalValue = globalAverageMap.get(key);
            Double deviationValue = standardDeviationMap.get(key);
            for (ArrayList list : listList) {
                Integer mapValue = 0;
                HashMap<String, Integer> map = (HashMap<String, Integer>) list
                        .get(0);
                String date = (String) list.get(1);
                if (map.containsKey(key)) {
                    mapValue = map.get(key);
                }
                if (globalValue + 2 * deviationValue < mapValue
                        && globalValue >= 2) {
                    // System.out.println("Pos: " + date + " : "+ key + "\t\t
                    // count "+mapValue+"\t\t hälve "+deviationValue+"\t\t
                    // keskmine "+globalValue);
                    // System.out.printf("Pos: %s %-20s count %-15s hälve
                    // %-15.3f keskmine %-15.3f\n",
                    // date,key,mapValue,deviationValue,globalValue);
                    writerPos
                            .printf("Pos: %s %-20s count  %-15s hälve  %-15.3f keskmine  %-15.3f\n",
                                    date, key, mapValue, deviationValue,
                                    globalValue);
                } else if (globalValue - 2 * deviationValue > mapValue
                        && mapValue != 0 && globalValue >= 2.0) {
                    // System.out.println("Neg: " + date +" : " + key + "\t\t
                    // count "+mapValue+"\t\t hälve "+deviationValue+"\t\t
                    // keskmine "+globalValue);
                    // System.out.printf("Neg: %s %-20s count %-15s hälve
                    // %-15.3f keskmine %-15.3f\n",
                    // date,key,mapValue,deviationValue,globalValue);
                    writerNeg
                            .printf("Neg: %s %-20s count  %-15s hälve  %-15.3f keskmine  %-15.3f\n",
                                    date, key, mapValue, deviationValue,
                                    globalValue);

                }
            }
        }
        writerPos.close();
        writerNeg.close();

    }

    private static HashMap<String, Double> standardDeviation(
            HashMap<String, Double> globalAverageMap,
            ArrayList<ArrayList> listList) {
        HashMap<String, Double> sdMap = new HashMap<>();
        int dayCount = listList.size();
        for (String key : globalAverageMap.keySet()) {
            Double globalValue = globalAverageMap.get(key);
            Double deviationSum = 0.0;
            for (ArrayList list : listList) {
                HashMap<String, Integer> map = (HashMap<String, Integer>) list
                        .get(0);
                int mapValue = 0;
                if (map.containsKey(key)) {
                    mapValue = map.get(key);
                }
                Double deviation = Math.pow((mapValue - globalValue), 2);
                deviationSum += deviation;
                // System.out.println(key+" : "+globalValue+" - "+mapValue+" :
                // "+deviation);
            }
            // System.out.println("Deviation sum for: "+key+" is
            // "+deviationSum);
            Double standardDeviation = Math.sqrt(deviationSum / dayCount);
            sdMap.put(key, standardDeviation);
            // System.out.println(key+" : "+standardDeviation);
        }
        return sdMap;
    }

    private static HashMap<String, Double> globalAverage(
            HashMap<String, Integer> globalMap) {
        double dayCount = (int) globalMap.get("dayCount");
        HashMap<String, Double> globalAverageMap = new HashMap<>();
        for (String key : globalMap.keySet()) {
            int value = globalMap.get(key);
            if (key != "dayCount" && key != "count") {
                globalAverageMap.put(key, value / dayCount);
                // System.out.println(key + " : " + value + " : " +
                // value/dayCount);
            }
        }
        return globalAverageMap;

    }

    public static void main(String[] args) throws IOException {

        System.out.println("le beginning\n");
        long startTime = System.currentTimeMillis(); // läpakas
        //run(24,"C:\\Users\\karll\\Documents\\Baka\\Baka\\logid\\var\\log\\daemon.log.4");

        //run(24,"C:\\Users\\karll\\Documents\\Baka\\Baka\\logid\\var\\kosmos-auth-logs\\auth.log.1");
        // run(24,"C:\\Users\\karll\\Documents\\Baka\\Baka\\obfuscated-ssg-6days.log\\obfuscated-ssg-6days.log");
        // töö arvuti

        // run(24,"C:\\Users\\Karl\\Documents\\Baka\\Baka\\logid\\var\\log\\daemon.log.4");
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("\nTotal runtime: " + totalTime + "ms");
        System.out.println("the end");

        final String title = "Peaks";
        final logAnalyser demo = new logAnalyser(title);
        demo.pack();
        RefineryUtilities.positionFrameRandomly(demo);
        demo.setVisible(true);

    }
}

/*
 * Count ja date sõnadega mis saab. Kui järsku on ka logifailides. äkki _count_?
 * https://ristov.github.io/slct/ http://www.nec-labs.com/~gfj/xia-sdm-14.pdf
 */