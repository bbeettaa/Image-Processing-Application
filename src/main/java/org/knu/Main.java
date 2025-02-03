package org.knu;

import org.knu.bll.FileService;
import org.knu.bll.algorithms.*;
import org.knu.bll.algorithms.blur.*;
import org.knu.bll.algorithms.clustering.Cluster;
import org.knu.bll.algorithms.clustering.KMeansCluster;
import org.knu.bll.algorithms.clustering.OtsuThresholding;
import org.knu.bll.algorithms.edges.EdgeDetectionOperator;
import org.knu.bll.algorithms.edges.PrewittOperator;
import org.knu.bll.algorithms.edges.RobertsCrossOperator;
import org.knu.bll.algorithms.edges.SobelOperator;
import org.knu.bll.algorithms.histograms.HistogramEqualization;
import org.knu.ui.SwingUI;
import org.knu.ui.swing.WorkingPanel;
import org.knu.ui.tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        FileService service = new FileService();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        BlurFilter[] blurFilters = new BlurFilter[]{new GaussBlur(), new MedianBlur(executorService), new BoxBlur(), new NoneBlur()};
        Cluster[] clusters = new Cluster[]{new KMeansCluster(), new OtsuThresholding()};
        EdgeDetectionOperator[] edgeDetection = new EdgeDetectionOperator[]{
                new SobelOperator(),
                new RobertsCrossOperator(),
                new PrewittOperator()};

        WorkingPanel workingPanel = new WorkingPanel();

        StatisticTool statisticTool = new StatisticTool(new ImageStatisticsCalculator(), workingPanel);

        workingPanel.setNotificationTool(statisticTool);

        List<Tool> tools = new ArrayList<>();

        tools.add(new ZoomTool(workingPanel));
        tools.add(new FiltersTool(workingPanel));

        tools.add(new SeparatorTool());
        tools.add(new BlurTool(blurFilters, workingPanel));

        tools.add(new SeparatorTool());
        tools.add(new EdgeDetectorOperatorTool(edgeDetection, workingPanel));
        tools.add(new CannyFilterTool(new CannyFilter(), blurFilters, edgeDetection, workingPanel));

        tools.add(new SeparatorTool());
        tools.add(new ClusteringTool(clusters, workingPanel));

        tools.add(new SeparatorTool());
        tools.add(new HistogramEqualizerTool(new HistogramEqualization(), workingPanel));

//        tools.add(new SeparatorTool());
//        tools.add(statisticTool);

        new SwingUI(workingPanel, service, tools, List.of(createStatistic(statisticTool)));
    }


    private static JMenu createStatistic(StatisticTool statisticTool) {
        JMenu fileMenu = new JMenu("Info");

        JMenuItem saveFile = new JMenuItem("Statistic");
        saveFile.addActionListener(e -> {
            JFrame frame = new JFrame(statisticTool.getName());
            frame.add(statisticTool.createUI());
            frame.setVisible(true);
            frame.setMinimumSize(new Dimension(300, 300));
            frame.setSize(new Dimension(400, 600));
            frame.setLocationRelativeTo(null);

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    super.windowOpened(e);
                    statisticTool.setFrame(true);
                    if(statisticTool.getImageWorkingPanel().getCurrentImagePanel() != null){
                        statisticTool.updateInfo();
                    }
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    statisticTool.setFrame(false);
                }
            });
        });
        fileMenu.add(saveFile);


        return fileMenu;
    }


}
