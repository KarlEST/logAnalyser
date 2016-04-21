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

public class TimeSeries_AWT extends ApplicationFrame {
    public TimeSeries_AWT(final String title) {
        super(title);
        final XYDataset dataset = createDataset();
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
    }

    private XYDataset createDataset() {
        final TimeSeries series = new TimeSeries("Random Data");
        Day current = new Day(1, 1, 2016);
        double value = 100.0;
        try {
            value = 100;
            series.add(current, new Double(value));
            value = 20;

            current = new Day(10, 3, 2016);;
            series.add(current, new Double(value));

        } catch (SeriesException e) {
            System.err.println("Error adding to series");
        }


        return new TimeSeriesCollection(series);
    }

    private JFreeChart createChart(final XYDataset dataset) {
        return ChartFactory.createTimeSeriesChart(
                "Computing Test",
                "Seconds",
                "Value",
                dataset,
                false,
                false,
                false);
    }

    public static void main(final String[] args) {
       /* final String title = "Time Series Management";
        final TimeSeries_AWT demo = new TimeSeries_AWT(title);
        demo.pack();
        RefineryUtilities.positionFrameRandomly(demo);
        demo.setVisible(true);
        */
        int value = 60;
        System.out.println(value-(value%5));
    }
} 