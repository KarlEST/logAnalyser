import java.io.*;
import java.util.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class logAnalyser extends ApplicationFrame {
    static private String dateMonth = "";
    static private String dateDay = "";
    static private String dateTime = "";
    static private PrintWriter writer = null;
    static private String timePrint = "";
    static private String firstDate = "";

    public logAnalyser(final String title, ArrayList<Integer> values) {
        super(title);
        final XYDataset dataset = createDataset(values);
        final JFreeChart chart = createChart(dataset, title);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
    }

    private XYDataset createDataset(ArrayList<Integer> values) {
        final TimeSeries series = new TimeSeries("Random Data");//, Second.class
        String[] dateSplit = firstDate.split(" ");

        Day current = new Day(Integer.parseInt(dateSplit[1]), Integer.parseInt(dateSplit[0]), Integer.parseInt(dateSplit[3]));
        double value = 0.0;
        for (int i = 0; i < values.size(); i++) {
            try {
                value = values.get(i);
                series.add(current, new Double(value));
                current = (Day) current.next();
            } catch (SeriesException e) {
                System.err.println(e);
            }
        }

        return new TimeSeriesCollection(series);
    }

    private JFreeChart createChart(final XYDataset dataset, String title) {
        return ChartFactory.createTimeSeriesChart(title, "Day",
                "Value", dataset, false, false, false);
    }

    public static boolean newDay(String line) {
        ArrayList<String> date = getDate(line);
        String dateMonthPart = date.get(0);
        String dateDayPart = date.get(1);
        if (dateDayPart.equals(dateDay) && dateMonthPart.equals(dateMonth)) {
            return false;
        } else {
            timePrint = dateMonth + " " + dateDay;
            if (!dateDayPart.equals(dateDay)) {
                dateDay = dateDayPart;
            }
            if (!dateMonthPart.equals(dateMonth)) {
                dateMonth = dateMonthPart;
            }
        }
        return true;
    }

    public static void firstLine(String filePath) throws IOException {
        BufferedReader firstLine = new BufferedReader(new FileReader(filePath));
        String firstLineText = firstLine.readLine();
        ArrayList<String> firstLineParts = getDate(firstLineText);
        dateMonth = firstLineParts.get(0);
        dateDay = firstLineParts.get(1);
        dateTime = firstLineParts.get(2);

        switch (dateMonth) {
            case "Jan":
                firstDate += "1 ";
                break;
            case "Feb":
                firstDate += "2 ";
                break;
            case "Mar":
                firstDate += "3 ";
                break;
            case "Apr":
                firstDate += "4 ";
                break;
            case "May":
                firstDate += "5 ";
                break;
            case "Jun":
                firstDate += "6 ";
                break;
            case "Jul":
                firstDate += "7 ";
                break;
            case "Aug":
                firstDate += "8 ";
                break;
            case "Sep":
                firstDate += "9 ";
                break;
            case "Oct":
                firstDate += "10 ";
                break;
            case "Nov":
                firstDate += "11 ";
                break;
            case "Dec":
                firstDate += "12 ";
                break;
        }
        firstDate += dateDay;
        firstDate += " ";
        firstDate += dateTime;
        firstDate += " 2016";
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


    public static void run(int time, String filePath) throws IOException {
        firstLine(filePath);
        ArrayList resultList = new ArrayList();
        switch (time) {
            case 24:
                resultList = workDay(filePath);
                break;
            case 1:
                break;
            case 5:
                break;
        }
        System.out.println("Word counting complete!");
        ArrayList wordCountList = (ArrayList) resultList.get(0);
        HashMap<String, Double> globalAverageMap = globalAverage((HashMap) resultList.get(1), wordCountList.size());
        System.out.println("Average calculation complete!");
        HashMap<String, Double> standardDeviationMap = standardDeviation(globalAverageMap, (ArrayList) wordCountList);
        System.out.println("Standard deviation calculation complete!");
        ArrayList posPeaks = calculatePeaks(globalAverageMap, standardDeviationMap, (ArrayList) wordCountList);
        System.out.println("Peak calculation complete!");

        Collections.sort(posPeaks, new Comparator<List<Integer>>() {
            @Override
            public int compare(List<Integer> a, List<Integer> b) {
                return b.get(1).compareTo(a.get(1));
            }
        });

        /* //for graphs
        for(int i = 0;i<10;i++){
            makeGraph((String) ((ArrayList) posPeaks.get(i)).get(0), wordCountList);
        }
*/
        //make jpeg graph
        for(int i = 0;i<10;i++){
            makeImage((String) ((ArrayList) posPeaks.get(i)).get(0), wordCountList);
        }


    }

    private static void makeImage(String title, ArrayList wordCountList) throws IOException {
        final TimeSeries series = new TimeSeries( title );
        String[] dateSplit = firstDate.split(" ");

        Day current = new Day(Integer.parseInt(dateSplit[1]), Integer.parseInt(dateSplit[0]), Integer.parseInt(dateSplit[3]));

        double value = 0.0;
        ArrayList<Integer> values = makeValueList(title, wordCountList);
        for ( int i = 0 ; i < values.size() ; i++ )
        {
            try
            {
                value = values.get(i);
                series.add( current , new Double( value ) );
                current = ( Day ) current.next( );
            }
            catch ( SeriesException e )
            {
                System.err.println( "Error adding to series" );
            }
        }
        final XYDataset dataset=( XYDataset )new TimeSeriesCollection(series);
        JFreeChart timechart = ChartFactory.createTimeSeriesChart(
                title,
                "Day",
                "Value",
                dataset,
                false,
                false,
                false);

        int width = 560; /* Width of the image */
        int height = 370; /* Height of the image */
        File timeChart = new File( ".\\graphs\\"+title+".png" );
        ChartUtilities.saveChartAsPNG( timeChart, timechart, width, height );

    }

    private static void makeGraph(String name, ArrayList wordCountList) {
        ArrayList<Integer> values = makeValueList(name, wordCountList);
        final logAnalyser demo = new logAnalyser(name, values);
        demo.pack();
        RefineryUtilities.positionFrameRandomly(demo);
        demo.setVisible(true);

    }

    private static ArrayList makeValueList(String name, ArrayList<ArrayList> wordCountList) {
        ArrayList resultList = new ArrayList();
        for (ArrayList list : wordCountList) {
            Integer mapValue = 0;
            HashMap<String, Integer> wordCountMap = (HashMap<String, Integer>) list.get(0);
            if (wordCountMap.containsKey(name)) {
                mapValue = wordCountMap.get(name);
            }
            resultList.add(mapValue);
        }

        return resultList;
    }


    public static ArrayList workDay(String filePath)
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
            if (newDay(line)) {
                prindiTulemus(wordCountMap);
                ArrayList mapTime = new ArrayList();//mapTime is for having date in peak files
                mapTime.add(wordCountMap);
                String[] dateNowSplit = timePrint.split(" ");
                mapTime.add(dateNowSplit[0] + " " + dateNowSplit[1]);
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

        timePrint = dateMonth + " " + dateDay;
        prindiTulemus(wordCountMap);
        writer.close();
        ArrayList mapTime = new ArrayList();
        mapTime.add(wordCountMap);
        mapTime.add(dateMonth + " " + dateDay);
        wordCountList.add(mapTime);
        //Close Log File Stream
        inputStream.close();
        sc.close();
        returnList.add(wordCountList);
        returnList.add(totalCountMap);
        return returnList;
    }


    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();
        //Käsk jooksutamiseks: run(int aeg,String logi_fail);


        //run(24, "C:\\Users\\karll\\Documents\\logAnalyser\\katse.log");
        run(24, "C:\\Users\\karll\\Documents\\logAnalyser\\logs\\kosmos-auth-logs\\auth.log.4");

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("\nTotal runtime: " + totalTime + "ms");
        final String title = "logAnalyser";
        /*
        final logAnalyser demo = new logAnalyser(title);
        demo.pack();
        RefineryUtilities.positionFrameRandomly(demo);
        demo.setVisible(true);
        */


    }
}

/*
 * Count ja date sõnadega mis saab. Kui järsku on ka logifailides. äkki _count_?
 * https://ristov.github.io/slct/ http://www.nec-labs.com/~gfj/xia-sdm-14.pdf
 */