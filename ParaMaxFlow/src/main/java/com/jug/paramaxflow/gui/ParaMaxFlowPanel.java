/**
 *
 */
package com.jug.paramaxflow.gui;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import loci.formats.gui.ExtensionFileFilter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;
import view.component.IddeaComponent;

import com.jug.fkt.Function1D;
import com.jug.fkt.FunctionComposerDialog;
import com.jug.segmentation.SegmentationMagic;
import com.jug.util.converter.RealDoubleNormalizeConverter;

import controller.action.HistogramToolAction;

/**
 * @author jug
 */
public class ParaMaxFlowPanel extends JPanel implements ActionListener, ChangeListener {

//	private static final String DEFAULT_PATH = "/Users/moon/Projects/git-projects/fjug/ImageJ_PlugIns/ParaMaxFlow/src/main/resources/";
	private static final String DEFAULT_PATH = "/Users/jug/Dropbox/WorkingData/Repositories/GIT/ImageJ_PlugIns/ParaMaxFlow/src/main/resources";

	private static ParaMaxFlowPanel main;

	private static JFrame guiFrame;

	private final Frame frame;

	private final ImagePlus imgPlus;

	private JTabbedPane tabsViews;

	private final IddeaComponent icOrig;
	private final IddeaComponent icClass;
	private final IddeaComponent icSumImg;
	private final IddeaComponent icSeg;
	private CostFunctionPanel costPlots;

	private JSlider sliderSegmentation;

	private JButton bLoadCostFunctions;
	private JButton bSaveCostFunctions;

	private JButton bSetUnaries;
	private JButton bSetPairwiseIsing;
	private JButton bSetPairwiseEdge;

	private JButton bHistogram;

	private JButton bLoadClassifier;
	private JToggleButton bUseClassifier;
	private JButton bCompute;

	private final RandomAccessibleInterval< DoubleType > imgOrig;     // original pixel values -- used for classification!
	private final RandomAccessibleInterval< DoubleType > imgOrigNorm; // normalized pixel values -- used for everything else!
	private RandomAccessibleInterval< DoubleType > imgClassified;
	private RandomAccessibleInterval< LongType > imgSumLong;
	private RandomAccessibleInterval< LongType > imgSegmentation;

	private long currSeg = -1;
	private long numSols = -1;

	private FunctionComposerDialog funcComposerUnaries;
	private FunctionComposerDialog funcComposerPairwiseEdge;

	/**
	 * @param imgPlus
	 */
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public ParaMaxFlowPanel( final Frame frame, final ImagePlus imgPlus ) {
		super( new BorderLayout( 5, 5 ) );
		setBorder( BorderFactory.createEmptyBorder( 10, 15, 5, 15 ) );
		this.frame = frame;
		this.imgPlus = imgPlus;

		final Img< ? > temp = ImagePlusAdapter.wrapNumeric( imgPlus );
		this.imgOrig = Converters.convert( Views.interval( temp, temp ), new RealDoubleNormalizeConverter( 1.0 ), new DoubleType() );
		this.imgOrigNorm = Converters.convert( Views.interval( temp, temp ), new RealDoubleNormalizeConverter( imgPlus.getStatistics().max ), new DoubleType() );

		this.imgClassified = null;
		this.imgSumLong = null;
		this.imgSegmentation = null;

		this.icOrig = new IddeaComponent( Views.interval( imgOrigNorm, imgOrigNorm ) );
		// THIS WORKS
		this.icClass = new IddeaComponent( Views.interval( imgOrigNorm, imgOrigNorm ) );
		this.icSumImg = new IddeaComponent( Views.interval( imgOrigNorm, imgOrigNorm ) );
		this.icSeg = new IddeaComponent( Views.interval( imgOrigNorm, imgOrigNorm ) );
		// THIS SHOULD WORK
//		this.icClass = new IddeaComponent();
//		this.icSumImg = new IddeaComponent();
//		this.icSeg = new IddeaComponent();

		buildGui();

		frame.setSize( 800, 600 );
	}

	private void buildGui() {
		this.tabsViews = new JTabbedPane();

		// ****************************************************************************************
		// *** IMAGE VIEWER
		// ****************************************************************************************
		this.icOrig.setToolBarLocation( BorderLayout.WEST );
		this.icOrig.setToolBarVisible( true );
		this.icOrig.setPreferredSize( new Dimension( imgPlus.getWidth(), imgPlus.getHeight() ) );

		this.icClass.setToolBarLocation( BorderLayout.WEST );
		this.icClass.setToolBarVisible( false );
		this.icClass.setPreferredSize( new Dimension( imgPlus.getWidth(), imgPlus.getHeight() ) );

		this.icSumImg.setToolBarLocation( BorderLayout.WEST );
		this.icSumImg.setToolBarVisible( false );
		this.icSumImg.setPreferredSize( new Dimension( imgPlus.getWidth(), imgPlus.getHeight() ) );

		this.icSeg.setToolBarLocation( BorderLayout.WEST );
		this.icSeg.setToolBarVisible( false );
		this.icSeg.setPreferredSize( new Dimension( imgPlus.getWidth(), imgPlus.getHeight() ) );
		sliderSegmentation = new JSlider( 0, 0 );
		sliderSegmentation.addChangeListener( this );

		// ****************************************************************************************
		// *** TEXTs AND CONTROLS
		// ****************************************************************************************
		final JPanel pLowerControls = new JPanel();
		pLowerControls.setLayout( new BoxLayout( pLowerControls, BoxLayout.LINE_AXIS ) );
		final JPanel pUpperControls = new JPanel();
		pUpperControls.setLayout( new BoxLayout( pUpperControls, BoxLayout.LINE_AXIS ) );

		final JTextArea textIntro = new JTextArea( "" + "Thanks to Vladimir Kolmogorov for native parametric max-flow code.\n" + "Classification models can be generated using the 'Trainable WEKA Segmentation'-plugin.\n" + "Bugs, comments, feedback? jug@mpi-cbg.de" );
		textIntro.setBackground( new JButton().getBackground() );
		textIntro.setEditable( false );
		textIntro.setBorder( BorderFactory.createEmptyBorder( 0, 2, 5, 2 ) );

		bLoadCostFunctions = new JButton( "load costs" );
		bLoadCostFunctions.addActionListener( this );
		bSaveCostFunctions = new JButton( "save costs" );
		bSaveCostFunctions.addActionListener( this );

		bSetUnaries = new JButton( "unary costs" );
		bSetUnaries.addActionListener( this );
		bSetPairwiseIsing = new JButton( "ising prior" );
		bSetPairwiseIsing.addActionListener( this );
		bSetPairwiseEdge = new JButton( "edge prior" );
		bSetPairwiseEdge.addActionListener( this );
		bCompute = new JButton( "GO FOR IT" );
		bCompute.addActionListener( this );

		bHistogram = new JButton( "Histogram" );
		bHistogram.addActionListener( new HistogramToolAction( icOrig.getCurrentInteractiveViewer2D() ) );

		bLoadClassifier = new JButton( "load class." );
		bLoadClassifier.addActionListener( this );
		bUseClassifier = new JToggleButton( "use class." );
		bUseClassifier.addActionListener( this );
		bUseClassifier.setEnabled( false );

		// ****************************************************************************************
		// *** COST PANEL
		// ****************************************************************************************
		final JPanel tabCosts = new JPanel( new BorderLayout() );
		// costPlots = new Plot2DPanel();
		costPlots = new CostFunctionPanel();
		costPlots.setFixedBoundsOnX( 0.0, 1.0 );
		updateCostPlots();
		costPlots.setPreferredSize( new Dimension( 500, 500 ) );
		tabCosts.add( costPlots, BorderLayout.CENTER );

		tabsViews.addTab( "raw data", icOrig );
		tabsViews.addTab( "classif.", icClass );
		tabsViews.addTab( "sum img", icSumImg );
		final JPanel help = new JPanel( new BorderLayout() );
		help.add( icSeg, BorderLayout.CENTER );
		help.add( sliderSegmentation, BorderLayout.SOUTH );
		tabsViews.addTab( "segm. hyp.", help );
		tabsViews.addTab( "cost fkts", tabCosts );

		add( textIntro, BorderLayout.NORTH );
		add( tabsViews, BorderLayout.CENTER );

		pUpperControls.add( bLoadCostFunctions );
		pUpperControls.add( bSaveCostFunctions );
		pUpperControls.add( Box.createHorizontalGlue() );
		pUpperControls.add( bLoadClassifier );
		pUpperControls.add( bUseClassifier );

		pLowerControls.add( bSetUnaries );
		pLowerControls.add( bHistogram );
		pLowerControls.add( bSetPairwiseIsing );
		pLowerControls.add( bSetPairwiseEdge );
		pLowerControls.add( Box.createHorizontalGlue() );
		pLowerControls.add( bCompute );

		final JPanel controls = new JPanel( new GridLayout( 2, 1 ) );
		controls.add( pUpperControls );
		controls.add( pLowerControls );
		add( controls, BorderLayout.SOUTH );

		// - - - - - - - - - - - - - - - - - - - - - - - -
		// KEYSTROKE SETUP (usingInput- and ActionMaps)
		// - - - - - - - - - - - - - - - - - - - - - - - -
		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( 'g' ), "MMGUI_bindings" );
		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( 'l' ), "MMGUI_bindings" );
		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( 'u' ), "MMGUI_bindings" );
		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( '1' ), "MMGUI_bindings" );
		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( '2' ), "MMGUI_bindings" );
		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( '3' ), "MMGUI_bindings" );
		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( '4' ), "MMGUI_bindings" );
		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( '5' ), "MMGUI_bindings" );
		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( '.' ), "MMGUI_bindings" );
		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( ',' ), "MMGUI_bindings" );

		this.getActionMap().put( "MMGUI_bindings", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent e ) {
				if ( e.getActionCommand().equals( "g" ) ) {
					bCompute.doClick();
				}
				if ( e.getActionCommand().equals( "l" ) ) {
					bLoadClassifier.doClick();
				}
				if ( e.getActionCommand().equals( "u" ) ) {
					bUseClassifier.doClick();
				}
				if ( e.getActionCommand().equals( "1" ) ) {
					tabsViews.setSelectedComponent( icOrig );
				}
				if ( e.getActionCommand().equals( "2" ) ) {
					tabsViews.setSelectedComponent( icClass );
				}
				if ( e.getActionCommand().equals( "3" ) ) {
					tabsViews.setSelectedComponent( icSumImg );
				}
				if ( e.getActionCommand().equals( "4" ) ) {
					tabsViews.setSelectedComponent( icSeg.getParent() );
				}
				if ( e.getActionCommand().equals( "5" ) ) {
					tabsViews.setSelectedIndex( tabsViews.getTabCount() - 1 );
				}
				if ( e.getActionCommand().equals( "," ) ) {
					decrementSolutionToShow();
				}
				if ( e.getActionCommand().equals( "." ) ) {
					incrementSolutionToShow();
				}
			}
		} );
	}

	/**
	 *
	 */
	private void updateCostPlots() {
		final int STEPS = 200;

		costPlots.removeAllPlots();

		final double[] xArray = new double[ STEPS ];
		final double[] costUnary = new double[ STEPS ];
		final double[] costIsing = new double[ STEPS ];
		final double[] costPairwiseX = new double[ STEPS ];
		final double[] costPairwiseY = new double[ STEPS ];
		final double[] costPairwiseZ = new double[ STEPS ];
		for ( int i = 0; i < STEPS; i++ ) {
			final double value = ( ( double ) i + 1 ) / STEPS;
			xArray[ i ] = value;
			costUnary[ i ] = SegmentationMagic.getFktUnary().evaluate( value );
			costIsing[ i ] = SegmentationMagic.getCostIsing();
			costPairwiseX[ i ] = SegmentationMagic.getFktPairwiseX().evaluate( value );
			costPairwiseY[ i ] = SegmentationMagic.getFktPairwiseY().evaluate( value );
			costPairwiseZ[ i ] = SegmentationMagic.getFktPairwiseZ().evaluate( value );
		}

		costPlots.addLinePlot( "Unary costs", new Color( 80, 255, 80 ), xArray, costUnary );
		costPlots.addLinePlot( "Ising costs", new Color( 127, 127, 255 ), xArray, costIsing );
		costPlots.addLinePlot( "Pairwise costs (X)", new Color( 200, 64, 64 ), xArray, costPairwiseX );
		costPlots.addLinePlot( "Pairwise costs (Y)", new Color( 200, 64, 64 ), xArray, costPairwiseY );
		costPlots.addLinePlot( "Pairwise costs (Z)", new Color( 200, 64, 64 ), xArray, costPairwiseZ );

	}

	/**
	 *
	 */
	protected void decrementSolutionToShow() {
		if ( currSeg > 0 ) {
			this.currSeg--;
		}
		this.sliderSegmentation.setValue( ( int ) currSeg );
	}

	/**
	 *
	 */
	protected void incrementSolutionToShow() {
		if ( currSeg < this.numSols ) {
			this.currSeg++;
		}
		this.sliderSegmentation.setValue( ( int ) currSeg );
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed( final ActionEvent e ) {

		if ( e.getSource().equals( bLoadClassifier ) ) {
			this.imgClassified = null;
			loadClassifierButtonPushed();

		} else if ( e.getSource().equals( bUseClassifier ) ) {
			getImgClassified();
//			SegmentationMagic.showLastClassified();
			this.icClass.setDoubleTypeScreenImage( this.imgClassified );

		} else if ( e.getSource().equals( bLoadCostFunctions ) ) {
			final JFileChooser fc = new JFileChooser( DEFAULT_PATH );
			fc.addChoosableFileFilter( new ExtensionFileFilter( new String[] { "jug", "JUG" }, "JUG-cost-function-file" ) );

			if ( fc.showOpenDialog( this.getTopLevelAncestor() ) == JFileChooser.APPROVE_OPTION ) {
				final File file = fc.getSelectedFile();
				try {
					final FileInputStream fileIn = new FileInputStream( file );
					final ObjectInputStream in = new ObjectInputStream( fileIn );
					SegmentationMagic.setFktUnary( ( Function1D< Double > ) in.readObject() );
					SegmentationMagic.setCostIsing( ( ( Double ) in.readObject() ).doubleValue() );
					SegmentationMagic.setFktPairwiseX( ( Function1D< Double > ) in.readObject() );
					SegmentationMagic.setFktPairwiseY( ( Function1D< Double > ) in.readObject() );
					SegmentationMagic.setFktPairwiseZ( ( Function1D< Double > ) in.readObject() );
				} catch ( final IOException ex ) {
					ex.printStackTrace();
				} catch ( final ClassNotFoundException ex2 ) {
					ex2.printStackTrace();
				}
				updateCostPlots();
			}

		} else if ( e.getSource().equals( bSaveCostFunctions ) ) {
			final JFileChooser fc = new JFileChooser( DEFAULT_PATH );
			fc.addChoosableFileFilter( new ExtensionFileFilter( new String[] { "jug", "JUG" }, "JUG-cost-function-file" ) );

			if ( fc.showSaveDialog( this.getTopLevelAncestor() ) == JFileChooser.APPROVE_OPTION ) {
				File file = fc.getSelectedFile();
				if ( !file.getAbsolutePath().endsWith( ".jug" ) && !file.getAbsolutePath().endsWith( ".JUG" ) ) {
					file = new File( file.getAbsolutePath() + ".jug" );
				}
				try {
					final FileOutputStream fileOut = new FileOutputStream( file );
					final ObjectOutputStream out = new ObjectOutputStream( fileOut );
					out.writeObject( SegmentationMagic.getFktUnary() );
					out.writeObject( new Double( SegmentationMagic.getCostIsing() ) );
					out.writeObject( SegmentationMagic.getFktPairwiseX() );
					out.writeObject( SegmentationMagic.getFktPairwiseY() );
					out.writeObject( SegmentationMagic.getFktPairwiseZ() );
				} catch ( final IOException ex ) {
					ex.printStackTrace();
				}
			}

		} else if ( e.getSource().equals( bSetUnaries ) ) {
			if ( funcComposerUnaries == null ) {
				funcComposerUnaries = new FunctionComposerDialog( SegmentationMagic.getFktUnary() );
			}
			final Function1D< Double > newFkt = funcComposerUnaries.open();
			if ( newFkt != null ) {
				SegmentationMagic.setFktUnary( newFkt );
			}
			updateCostPlots();

		} else if ( e.getSource().equals( bSetPairwiseIsing ) ) {
			try {
				SegmentationMagic.setCostIsing( Double.parseDouble( JOptionPane.showInputDialog( "Please add an Ising cost:" ) ) );
			} catch ( final NumberFormatException ex ) {
				JOptionPane.showMessageDialog( this, "Parse error: please input a number that can be parsed as a double." );
			} catch ( final NullPointerException ex2 ) {
				// cancel was hit in JOptionPane
			}
			updateCostPlots();

		} else if ( e.getSource().equals( bSetPairwiseEdge ) ) {
			if ( funcComposerPairwiseEdge == null ) {
				funcComposerPairwiseEdge = new FunctionComposerDialog( SegmentationMagic.getFktPairwiseX() );
			}
			final Function1D< Double > newFkt = funcComposerPairwiseEdge.open();
			if ( newFkt != null ) {
				SegmentationMagic.setFktPairwiseX( newFkt );
				SegmentationMagic.setFktPairwiseY( newFkt );
				SegmentationMagic.setFktPairwiseZ( newFkt );
			}
			updateCostPlots();

		} else if ( e.getSource().equals( bCompute ) ) {
			if ( this.bUseClassifier.isSelected() ) {
				this.imgSumLong = SegmentationMagic.returnClassificationBoostedParamaxflowRegionSums( imgOrigNorm, imgClassified );
			} else {
				this.imgSumLong = SegmentationMagic.returnParamaxflowRegionSums( imgOrigNorm );
			}
			this.icSumImg.setLongTypeScreenImage( this.imgSumLong );
			this.numSols = SegmentationMagic.getNumSolutions();
			this.sliderSegmentation.setMaximum( ( int ) this.numSols );
			this.sliderSegmentation.setValue( 0 );
		}
	}

	/**
	 * @return
	 */
	private RandomAccessibleInterval< DoubleType > getImgClassified() {
		if ( this.imgClassified == null ) {
			this.imgClassified = SegmentationMagic.returnClassification( this.imgOrig );
		}
		return this.imgClassified;
	}

	/**
	 *
	 */
	private void loadClassifierButtonPushed() {
		final JFileChooser fc = new JFileChooser( DEFAULT_PATH );
		fc.addChoosableFileFilter( new ExtensionFileFilter( new String[] { "model", "MODEL" }, "weka-model-file" ) );

		if ( fc.showOpenDialog( guiFrame ) == JFileChooser.APPROVE_OPTION ) {
			final File file = fc.getSelectedFile();
			// this.wekaSegmenter = new SilentWekaSegmenter< DoubleType >(
			// file.getParent() + "/", file.getName() );
			SegmentationMagic.setClassifier( file.getParent() + "/", file.getName() );
			this.bUseClassifier.setEnabled( true );
		}
	}

	public static void main( final String[] args ) {
		ImageJ temp = IJ.getInstance();

		if ( temp == null ) {
			temp = new ImageJ();
			// IJ.open( "/Users/moon/Documents/clown.tif" );
			// IJ.open( "/Users/moon/Pictures/spim/spim-0.tif" );
			IJ.open( "/Users/jug/Desktop/clown.tif" );
		}

		final ImagePlus imgPlus = WindowManager.getCurrentImage();
		if ( imgPlus == null ) {
			IJ.error( "There must be an active, open window!" );
			// System.exit( 1 );
			return;
		}

		guiFrame = new JFrame( "ParaMaxFlow Segmentation" );
		main = new ParaMaxFlowPanel( guiFrame, imgPlus );

		// main.imgCanvas = imgPlus.getCanvas();

		guiFrame.add( main );
		guiFrame.setVisible( true );
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged( final ChangeEvent e ) {
		if ( e.getSource().equals( sliderSegmentation ) ) {
			currSeg = sliderSegmentation.getValue();
			this.imgSegmentation = SegmentationMagic.returnSegmentation( imgSumLong, currSeg );
			this.icSeg.setLongTypeScreenImage( imgSegmentation );
		}
	}
}
