package graphDisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.flow.FlowPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.flow.DefaultFlowDataset;
import org.jfree.data.flow.FlowDatasetUtils;
import org.jfree.data.flow.NodeKey;

import filter.FilteredTable;
import mapOptions.MapOptionsUtil;

/**
 * The class to display a Sankey Diagram by constructing a flow dataset from 
 * user selected input from a query result table
 * 
 * Author Action Date Flag
 * ======================================================================= 
 * Yadong
 * created September/03/2024
 */

public class SankeyDiagramFromTable extends JFrame implements ComponentListener {
	private static final long serialVersionUID = 1L;
	private String chartName;
	private JTable jtable;
	private JFrame frame;
	private JToolBar toolBar;
	private JPanel scenarioMenuPanel;
	private JPanel regionMenuPanel;
	private JPanel yearMenuPanel;
	private JPanel sankeyPanel;
	private JPanel sankeyLabelPanel;
	private JPanel barChartPanel;
	private JPanel summaryPanel;
	private JButton nextYearButton;
	private JButton prevYearButton;
	
	private JLabel scenarioListLabel;
	private JLabel regionListLabel;
	private	JLabel listLabel;
	private JComboBox<String> scenarioListMenu;
	private	JComboBox<String> regionListMenu;
	private	JComboBox<String> yearListMenu;
	private double defaultNodeWidth = 200.0;
	private double defaultNodeMargin = 0.02;
	private boolean replaceWithBarChart = false;
	
	
	public SankeyDiagramFromTable(String chartName,JTable jtable) throws ClassNotFoundException {
		this.chartName = chartName;
		this.jtable=jtable;
		initialize();
		
	}
	
	private void initialize() {
		frame = new JFrame("Sankey for "+chartName);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		int regionIdx = FilteredTable.getColumnByName(jtable, "region");
		ArrayList<String> yearList = FilteredTable.getYearListFromTableData(jtable);
		int firstYearIdx = FilteredTable.getColumnByName(jtable, yearList.get(0));
		//check if we need to replace Sankey with StackedBarChart if only one column available
		// between "region" and the columns of query results
		if (firstYearIdx-regionIdx==2) {
		   replaceWithBarChart = true;	
		}
		frame.getContentPane().add(createToolBar(),BorderLayout.WEST);
		if (replaceWithBarChart){
		   frame.getContentPane().add(createStackedBarPlot(),BorderLayout.CENTER);	
		}else {
		   frame.getContentPane().add(createSankeyPlot(),BorderLayout.CENTER);
		}
		frame.getContentPane().add(createSummary(),BorderLayout.EAST);
		frame.validate();
		frame.pack();
		Dimension preferredD = new Dimension(1200,800);
		frame.setSize(preferredD);
		frame.setMinimumSize(new Dimension(500,300));
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		//frame.setAlwaysOnTop(true);
	}
	
	protected JComponent createToolBar() {
		//create a toolBar on the left
		toolBar = new JToolBar();
		toolBar.setBackground(Color.LIGHT_GRAY);
		toolBar.setBorder(new EmptyBorder(5,5,5,5));
		toolBar.setLayout(new GridLayout(10,1));
		toolBar.setFloatable(false);
		//add scenario dropdown menu inside the JToolBar 
		scenarioMenuPanel = new JPanel();
		scenarioMenuPanel.setBorder(new EmptyBorder(5,5,5,5));
		scenarioMenuPanel.setLayout(new BoxLayout(scenarioMenuPanel,BoxLayout.Y_AXIS));
		scenarioMenuPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		scenarioListLabel = new JLabel("Scenario:",SwingConstants.LEFT);
		scenarioListLabel.setFont(new Font("Arial",Font.BOLD,16));
		scenarioListLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		List<String> scenarioListFromTable = MapOptionsUtil.getScenarioListFromTableData(jtable);
		//System.out.println("SankeyDiagramFromTable::this is the first scenario:"+scenarioListFromTable.get(0));
		DefaultComboBoxModel<String> dmlScenario = new DefaultComboBoxModel<String>();
			for (int i=0;i< scenarioListFromTable.size();i++) {
				dmlScenario.addElement(scenarioListFromTable.get(i));	
			}
		scenarioListMenu = new JComboBox<String>();
		scenarioListMenu.setModel(dmlScenario);
		scenarioListMenu.setVisible(true);
		scenarioListMenu.setFont(new Font("Arial",Font.BOLD,14));
		scenarioListMenu.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		scenarioListMenu.setMaximumSize(new Dimension(300,25));
		scenarioListMenu.addActionListener(new UpdateSankeyOrBarChart());
		scenarioMenuPanel.add(scenarioListLabel);
		scenarioMenuPanel.add(scenarioListMenu);
		toolBar.add(scenarioMenuPanel);
		//add region dropdown menu inside the JToolBar 
		regionMenuPanel = new JPanel();
		regionMenuPanel.setBorder(new EmptyBorder(5,5,5,5));
		regionMenuPanel.setLayout(new BoxLayout(regionMenuPanel,BoxLayout.X_AXIS));
		regionMenuPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		regionListLabel = new JLabel("Region:",SwingConstants.LEFT);
		regionListLabel.setFont(new Font("Arial",Font.BOLD,16));
		regionListLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		List<String> regionListFromTable = MapOptionsUtil.getUniqueRegionsInTable(jtable);
		//System.out.println("SankeyDiagramFromTable::this is the first region:"+regionListFromTable.get(0));
		DefaultComboBoxModel<String> dmlRegion = new DefaultComboBoxModel<String>();
			for (int i=0;i< regionListFromTable.size();i++) {
				dmlRegion.addElement(regionListFromTable.get(i));	
			}
		regionListMenu = new JComboBox<String>();
		regionListMenu.setModel(dmlRegion);
		regionListMenu.setVisible(true);
		regionListMenu.setFont(new Font("Arial",Font.BOLD,14));
		regionListMenu.setMaximumSize(new Dimension(100,25));
		regionListMenu.addActionListener(new UpdateSankeyOrBarChart());
		regionMenuPanel.add(regionListLabel);
		regionMenuPanel.add(regionListMenu);
		toolBar.add(regionMenuPanel);		
		//add year dropdown menu inside the JToolBar 
		yearMenuPanel = new JPanel();
		yearMenuPanel.setBorder(new EmptyBorder(10,10,10,10));
		yearMenuPanel.setLayout(new BoxLayout(yearMenuPanel,BoxLayout.X_AXIS));
		yearMenuPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		listLabel = new JLabel("Year:",SwingConstants.LEFT);
		listLabel.setFont(new Font("Arial",Font.BOLD,16));
		listLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		ArrayList<String> yearListFromTable = MapOptionsUtil.getYearListFromTableData(jtable);
		DefaultComboBoxModel<String> dml = new DefaultComboBoxModel<String>();
		for (int i=0;i< yearListFromTable.size();i++) {
			dml.addElement(yearListFromTable.get(i));	
		}
		yearListMenu = new JComboBox<String>();
		yearListMenu.setModel(dml);
		yearListMenu.setVisible(true);
		//yearListMenu.setMaximumSize(yearListMenu.getPreferredSize());
		yearListMenu.setFont(new Font("Arial",Font.BOLD,14));
		yearListMenu.setMaximumSize(new Dimension(150,25));
		yearListMenu.addActionListener(new UpdateSankeyOrBarChart());
		
		nextYearButton = new JButton(">");
		nextYearButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int y=yearListMenu.getSelectedIndex();
				if (y<yearListMenu.getModel().getSize()-1) {
					yearListMenu.setSelectedIndex(y+1);
				}
		}
		});
		nextYearButton.setVisible(true);
		prevYearButton = new JButton("<");
		prevYearButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
					int y=yearListMenu.getSelectedIndex();
					if (y>0) {
						yearListMenu.setSelectedIndex(y-1);
					}
			}
		});
		prevYearButton.setVisible(true);
		
		yearMenuPanel.add(listLabel);
		yearMenuPanel.add(prevYearButton);
		yearMenuPanel.add(yearListMenu);
		yearMenuPanel.add(nextYearButton);		
		
		toolBar.add(yearMenuPanel);		

		toolBar.add(yearMenuPanel);
		return toolBar;
	};
	
	protected JComponent createSankeyPlot() {
		
		String selectedScenario = (String)scenarioListMenu.getSelectedItem();
		String selectedRegion = (String)regionListMenu.getSelectedItem();
		String selectedYear =(String)yearListMenu.getSelectedItem();
		DefaultFlowDataset myDataset = createFlowDatasetFromTable(jtable,selectedScenario,selectedRegion,selectedYear);
		Set<NodeKey> mySet = myDataset.getAllNodes();
		
		sankeyPanel = new JPanel();
		sankeyPanel.setLayout(new BoxLayout(sankeyPanel,BoxLayout.Y_AXIS));
		sankeyPanel.setBorder(new EmptyBorder(10,10,10,10));
		sankeyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		Iterator<NodeKey> nodeIterator = mySet.iterator();
		
		FlowPlot myPlot = new FlowPlot(myDataset);
		HashMap<String,Color> myColors=new HashMap();
		while (nodeIterator.hasNext()) {
			NodeKey myKey = nodeIterator.next();
			//next few lines create internally consistent colors
			//Color myColor=null;
			//if(!myColors.containsKey(myKey.getNode().toString())) {
			//	Random r=new Random();
			//	myColor=new Color(r.nextInt(200)+50,r.nextInt(200)+50,r.nextInt(200)+50);
			//	myColors.put(myKey.getNode().toString(), myColor);
			//}
			
			//myColor=myColors.get(myKey.getNode().toString());
			//myPlot.setNodeFillColor(myKey, myColor);
			
		   // System.out.println("Check each of the NodeKey: "+myKey.toString());
		   // System.out.println("Check getNode of the NodeKey: "+myKey.getNode());
		   /* if (myKey.getNode().toString().equalsIgnoreCase("Electricity")) {
		    	myPlot.setNodeFillColor(myKey, Color.ORANGE);
		    	double inFlowE = FlowDatasetUtils.calculateInflow(myDataset, myKey.getNode(),1);
		    	System.out.println("Check inflow for electricity: "+inFlowE);
		    }else if (myKey.getNode().toString().equalsIgnoreCase("Gas")) {
		    	myPlot.setNodeFillColor(myKey, Color.BLUE);
		    }else if (myKey.getNode().toString().equalsIgnoreCase("Biomass")) {
		    	myPlot.setNodeFillColor(myKey, Color.GREEN);
		    }*/
		}
		myPlot.setNodeLabelOffsetX(-170.0);
		myPlot.setNodeLabelOffsetY(-170.0);
		myPlot.setNodeWidth(defaultNodeWidth);
		myPlot.setNodeMargin(defaultNodeMargin);
		myPlot.setDefaultNodeLabelFont(new Font("Arial",Font.BOLD,16));
		myPlot.setOutlineVisible(true);

		String chartTitle = chartName + " for " + selectedRegion + " in " + selectedYear;
		JFreeChart chart = new JFreeChart(chartTitle,myPlot);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.validate();
		//chartPanel.setBorder(BorderFactory.createLineBorder(Color.blue,5));
		chartPanel.setPreferredSize(new Dimension(800,600));
		sankeyPanel.add(chartPanel,BorderLayout.CENTER);
		
		sankeyLabelPanel = new JPanel();
		sankeyLabelPanel.setLayout(new BoxLayout(sankeyLabelPanel,BoxLayout.X_AXIS));
		sankeyLabelPanel.setBorder(new EmptyBorder(10,10,10,10));
		sankeyLabelPanel.setMaximumSize(new Dimension(10000,100));
		sankeyLabelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		//Node is displayed from left to right
		//get the first column name right after "region"
		int regionIdx = FilteredTable.getColumnByName(jtable, "region");
		ArrayList<String> yearList = FilteredTable.getYearListFromTableData(jtable);
		int firstYearIdx = FilteredTable.getColumnByName(jtable, yearList.get(0));
		int totalNodes = firstYearIdx-regionIdx-1;
		//String firstNode = jtable.getColumnName(firstYearIdx-1);
		//JTextField nodeFromColumn = new JTextField(firstNode);
		//nodeFromColumn.setFont(new Font("Arial",Font.BOLD,16));
		int gapWidth = (int) Math.round(4*myPlot.getNodeWidth()/totalNodes);
		if(totalNodes==3) {
			gapWidth=(int) Math.round(2*myPlot.getNodeWidth()/totalNodes);
		}else if(totalNodes>=4) {
			gapWidth=(int) Math.round(myPlot.getNodeWidth()/totalNodes);
		}
		//nodeFromColumn.setSize(new Dimension((int)myPlot.getNodeWidth()/2,100));
		//nodeFromColumn.setHorizontalAlignment(JTextField.CENTER);
		//nodeFromColumn.setBackground(Color.GRAY);
		//sankeyLabelPanel.add(nodeFromColumn);
		for (int i=0;i<totalNodes;i++) {
			String nextNode = jtable.getColumnName(firstYearIdx-i-1);
			JTextField nextNodeFromColumn = new JTextField(nextNode);
			nextNodeFromColumn.setFont(new Font("Arial",Font.BOLD,16));
			nextNodeFromColumn.setSize(new Dimension((int)myPlot.getNodeWidth()/2,100));
			nextNodeFromColumn.setHorizontalAlignment(JTextField.CENTER);
			nextNodeFromColumn.setBackground(Color.GRAY);
			sankeyLabelPanel.add(nextNodeFromColumn);
			//sankeyLabelPanel.add(Box.createRigidArea(new Dimension(gapWidth,0)));
			
		}
		
		sankeyPanel.add(sankeyLabelPanel);
		return sankeyPanel;
	}
	
protected JComponent createStackedBarPlot() {
	    
	    barChartPanel = new JPanel();
	    barChartPanel.setLayout(new BoxLayout(barChartPanel,BoxLayout.Y_AXIS));
	    barChartPanel.setBorder(new EmptyBorder(10,10,10,10));
	    barChartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		String selectedScenario = (String)scenarioListMenu.getSelectedItem();
		String selectedRegion = (String)regionListMenu.getSelectedItem();
		String selectedYear =(String)yearListMenu.getSelectedItem();
		int unitColIdx = FilteredTable.getColumnByName(jtable, "Units");
		String unitForYAxis = (String) jtable.getValueAt(0, unitColIdx);
		DefaultCategoryDataset myDataset = createCategoryDatasetFromTable(jtable,selectedScenario,selectedRegion,selectedYear);
		JFreeChart barChart = ChartFactory.createStackedBarChart("","",unitForYAxis, myDataset,PlotOrientation.VERTICAL,true,true,false);
		LegendTitle legend = barChart.getLegend();
		legend.setItemFont(new Font("Arial",Font.BOLD,14));
	
		CategoryPlot barPlot = (CategoryPlot) barChart.getPlot();
		barPlot.setDataset(0,myDataset);
		StackedBarRenderer renderer = (StackedBarRenderer)barPlot.getRenderer();
		renderer.setMaximumBarWidth(0.2);
		renderer.setDefaultItemLabelFont(new Font("Arial",Font.BOLD,16));
		renderer.setSeriesItemLabelFont(0,new Font("Arial",Font.BOLD,16));
		//remove the shining white stripe from the stackedBarChart
		renderer.setBarPainter(new StandardBarPainter());
		//if we want to set the color for each individual bar
		//renderer.setSeriesPaint(0,Color.blue);
		barPlot.setRenderer(0,renderer);
		
		
		ChartPanel chartPanel = new ChartPanel(barChart);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.validate();
		chartPanel.setPreferredSize(new Dimension(800,600));
		barChartPanel.add(chartPanel,BorderLayout.CENTER);
		
		return barChartPanel;
}
	
	protected JComponent createSummary() {
		//add information display field at the bottom
		summaryPanel = new JPanel();
		summaryPanel.setBorder(new EmptyBorder(10,10,10,10));
		summaryPanel.setLayout(new BoxLayout(summaryPanel,BoxLayout.X_AXIS));
		summaryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		summaryPanel.add(Box.createHorizontalGlue());
		summaryPanel.add(Box.createHorizontalGlue());
		return summaryPanel;
	}
	
	public class UpdateSankeyOrBarChart extends JPanel implements ActionListener {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (replaceWithBarChart) {
			  RedrawBarChart();	
			}else {
			  RedrawSankeyPlot();
			}
			
		}
	}
	
	public void RedrawSankeyPlot() {
		//clear up the previous displayed panels 
		frame.remove(sankeyPanel);
		frame.getContentPane().add(createSankeyPlot(),BorderLayout.CENTER);
		frame.revalidate();
		frame.repaint();
	}
	
	public void RedrawBarChart() {
		//clear up the previous displayed panels 
		frame.remove(barChartPanel);
		frame.getContentPane().add(createStackedBarPlot(),BorderLayout.CENTER);
		frame.revalidate();
		frame.repaint();
	}
	
	private DefaultFlowDataset createFlowDatasetFromTable(JTable jtable, String scenarioStr, String regionStr, String yearStr) {
	
		//remove those rows not matching scenarioStr and regionStr from jtable first
		int regionIdx = FilteredTable.getColumnByName(jtable, "region");
		int scenarioIdx = FilteredTable.getColumnByName(jtable, "scenario");
		ArrayList<String> yearList = FilteredTable.getYearListFromTableData(jtable);
		int firstYearIdx = FilteredTable.getColumnByName(jtable, yearList.get(0));
		int yearIdx = FilteredTable.getColumnByName(jtable, yearStr);
		//int nodeN = firstYearIdx-regionIdx-1;
		DefaultFlowDataset dataset = new DefaultFlowDataset(); 
		int curStage=0;
		for(int row=0;row<jtable.getRowCount();row++) {
			curStage=0;
		//check if the scenario and region match the selected
			boolean scenario2Keep = ((String)jtable.getValueAt(row, scenarioIdx)).equals(scenarioStr);
			boolean region2Keep = ((String)jtable.getValueAt(row, regionIdx)).equals(regionStr);
			if (scenario2Keep && region2Keep) {
			  //for (int j = regionIdx+1; j < firstYearIdx-1; j++) {
				for (int j = firstYearIdx-1; j > regionIdx+1; j--) {
					String fromSource = (String)jtable.getValueAt(row, j);
					String toDes = (String)jtable.getValueAt(row, j-1);
					double flowRate = Double.parseDouble((String)jtable.getValueAt(row, yearIdx));
					//System.out.println("SankeyDiagramFromTable::at this node number: "+(j-regionIdx));
					//System.out.println("SankeyDiagramFromTable::from this source: "+fromSource);
					//System.out.println("SankeyDiagramFromTable::to this destination: "+toDes);
					if (flowRate !=0) {
						dataset.setFlow(curStage,fromSource,toDes,flowRate);
					}
					curStage=curStage+1;
				  }
			}
		}
		return dataset;
	}
	
	
	private DefaultCategoryDataset createCategoryDatasetFromTable(JTable jtable, String scenarioStr, String regionStr, String yearStr) {
		int yearIdx = FilteredTable.getColumnByName(jtable, yearStr);
		int scenarioIdx = FilteredTable.getColumnByName(jtable, "scenario");
		int regionIdx = FilteredTable.getColumnByName(jtable, "region");
		String colName = jtable.getColumnName(regionIdx+1);
		DefaultCategoryDataset myDataset = new DefaultCategoryDataset();
		//look into "CategoryStackedBarChart.java" ;
		for(int row=0;row<jtable.getRowCount();row++) {
			//check if the scenario and region match the selected
				boolean scenario2Keep = ((String)jtable.getValueAt(row, scenarioIdx)).equals(scenarioStr);
				boolean region2Keep = ((String)jtable.getValueAt(row, regionIdx)).equals(regionStr);
				if (scenario2Keep && region2Keep) {		  
					String myStr = (String)jtable.getValueAt(row, regionIdx+1);
					double myNum = Double.parseDouble((String)jtable.getValueAt(row, yearIdx));
					myDataset.addValue(myNum, myStr,colName);
				}
		}
		return myDataset;
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
	
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
	
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
	
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
	
	}
}
