package org.mapdb.benchmarks;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.Rotation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Charts {

    public static void main(String[] args) throws IOException {

        String target = args[0];

        Properties p = new Properties();
        p.load(new FileReader(new File(BU.RESULT_PROPERTIES)));

        Set<String> cats = new TreeSet();
        Set<String> tasks = new LinkedHashSet<String>();
        Set<Integer> threads = new TreeSet();


        for(String key:p.stringPropertyNames()){
            String[] s = key.split("_");
            cats.add(s[0]);
            tasks.add(s[1]);
            threads.add(new Integer(s[2]));
        }


        //single threaded perf
        {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            for (String cat : cats) {
                for (String task : tasks) {
                    String val = (String) p.get(cat + "_" + task + "_1");
                    dataset.addValue(Long.parseLong(val), cat, task);
                }
            }


            // based on the dataset we create the chart
            JFreeChart chart = ChartFactory.createBarChart(
                    "Single threaded",
                    "",
                    "ops/second",
                    dataset
            );

            target = target.replace(".","_"); //Latex has really stupid extension detection

            ChartUtilities.saveChartAsPNG(new File(target + "-single-thread.png"), chart, 700, 500);
        }

        for(String cat:cats){
            XYSeriesCollection dataset = new XYSeriesCollection();

            for (String task : tasks) {
                XYSeries xyseries1 = new XYSeries(task);
                for (Integer thread : threads) {
                    String val = (String) p.get(cat + "_" + task + "_"+thread);
                    xyseries1.add(thread,new Long(val));
                }
                dataset.addSeries(xyseries1);

            }



            // based on the dataset we create the chart
            JFreeChart chart = ChartFactory.createXYLineChart(
                    cat + " - concurrent performance",
                    "number of threads",
                    "ops/second",
                    dataset
            );




            ChartUtilities.saveChartAsPNG(new File(target + "-"+cat+"-scalability.png"), chart, 700, 500);

        }


    }

} 