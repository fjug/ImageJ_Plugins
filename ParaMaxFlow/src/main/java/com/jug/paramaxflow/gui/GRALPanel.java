package com.jug.paramaxflow.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Random;

import javax.swing.JPanel;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.data.filters.Convolution;
import de.erichseifert.gral.data.filters.Filter;
import de.erichseifert.gral.data.filters.Kernel;
import de.erichseifert.gral.data.filters.KernelUtils;
import de.erichseifert.gral.data.filters.Median;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.GraphicsUtils;
import de.erichseifert.gral.util.Insets2D;
import de.erichseifert.gral.util.Orientation;
import de.erichseifert.gral.util.Location;
import edu.mines.jtk.sgl.Axis;

public class GRALPanel extends JPanel 
{
	/** First corporate color used for normal coloring.*/
	protected static final Color COLOR1 = new Color( 55, 170, 200);
	/** Second corporate color used as signal color */
	protected static final Color COLOR2 = new Color(200,  80,  75);
	
	private static final int SAMPLE_COUNT = 200;
	
	private XYPlot plot = null;
	private InteractivePanel interactivePanel = null;
	
	@SuppressWarnings("unchecked")
	public GRALPanel() {
		
		super(new BorderLayout());
		setPreferredSize(new Dimension(800, 600));
		setBackground(Color.WHITE);
			
		plot = new XYPlot();
		plot.getAxis(plot.AXIS_X).setAutoscaled(false);
		plot.getAxis(plot.AXIS_Y).setAutoscaled(false);
		
		plot.setInsets(new Insets2D.Double(20.0, 180.0, 40.0, 40.0));
		plot.setLegendVisible(true);
		//
		plot.setLegendLocation(Location.WEST);
		//plot.setLegendDistance(20);

		// Format legend
		plot.getLegend().setOrientation(Orientation.VERTICAL);
		//plot.getLegend().setAlignmentY(0.0);

		// Add plot to Swing component
		interactivePanel = new InteractivePanel(plot);
		add(interactivePanel, BorderLayout.CENTER);
	}

	private static void formatLine(XYPlot plot, DataSeries series, Color color) 
	{
		plot.setPointRenderer(series, null);
		DefaultLineRenderer2D line = new DefaultLineRenderer2D();
		line.setColor(color);
		plot.setLineRenderer(series, line);
	}
	
	public void removeAllPlots()
	{
		plot.clear();
	}

	public void addLinePlot(String string, Color color, double[] xArray, double[] yArray) 
	{
		DataTable data = new DataTable(Double.class, Double.class);

		for (int i = 0; i < SAMPLE_COUNT; i++) 
		{
			data.add(xArray[i], yArray[i]);
		}
		
		DataSeries ds = new DataSeries(string, data, 0, 1);
		plot.add(ds);
		formatLine(plot, ds, color);
	}

	public void setFixedBounds(int i, double d, double e) 
	{		
		plot.getAxis(plot.AXIS_X).setRange(new Double(d - 0.7), new Double(e + 0.3));
		plot.getAxis(plot.AXIS_Y).setRange(new Double(-1.2), new Double(0.8));
	}

	public void refresh() 
	{
		interactivePanel.repaint();
	}
}
