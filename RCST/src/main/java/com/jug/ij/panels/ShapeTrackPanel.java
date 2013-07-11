/**
 *
 */
package com.jug.ij.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.LineFigure;
import org.jhotdraw.draw.action.ButtonFactory;
import org.jhotdraw.draw.tool.BezierTool;
import org.jhotdraw.draw.tool.CreationTool;
import org.jhotdraw.util.ResourceBundleUtil;

import com.jug.imglib2.viewer.InteractiveViewerPanel;

/**
 * @author jug
 */
public class ShapeTrackPanel extends JPanel implements ActionListener, ChangeListener, ItemListener {

	/**
	 * Serialization ID
	 */
	private static final long serialVersionUID = -8040344519142406774L;

	private final Frame frame;
	private final Img< DoubleType > img;

	//private Viewer2DCanvas imgViewer;
	private InteractiveViewerPanel iviewer;

	private JSlider sliderTime;
	private JSlider sliderChannel;

	private JToolBar toolbar = new JToolBar();
	private JButton bToolCenterLine;

	/**
	 * @param imgPlus
	 */
	public ShapeTrackPanel( final Frame frame, final Img< DoubleType > img ) {
		super( new BorderLayout( 5, 5 ) );
		setBorder( BorderFactory.createEmptyBorder( 10, 15, 5, 15 ) );
		this.frame = frame;
		this.img = img;
		buildGui();
		final Dimension dim = this.getPreferredSize();
		dim.setSize( dim.width + 20, dim.height + 25 );
		frame.setSize( dim );
	}

	private void buildGui() {
		final JPanel pContent = new JPanel( new BorderLayout() );
		final JPanel pControls = new JPanel();
		pControls.setLayout( new BoxLayout( pControls, BoxLayout.LINE_AXIS ) );

		JPanel panelHorizontalHelper;
		JPanel panelVerticalHelper;

		// Text on top
		final JTextArea textIntro = new JTextArea( "" +
				"Step 2: Shape Tracking\n" +
				"----------------------\n" +
				"a) do it!" );
		textIntro.setBackground( new JButton().getBackground() );
		textIntro.setEditable( false );
		textIntro.setBorder( BorderFactory.createEmptyBorder( 0, 2, 5, 2 ) );

		// Slider-stuff
		sliderTime = new JSlider( JSlider.HORIZONTAL, 0, ( int ) img.dimension( 3 ) - 1, 0 );
		sliderTime.addChangeListener( this );
		sliderTime.setMajorTickSpacing( 10 );
		sliderTime.setMinorTickSpacing( 2 );
		sliderTime.setPaintTicks( true );
		sliderTime.setPaintLabels( true );
		sliderTime.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 3 ) );
		panelHorizontalHelper = new JPanel( new BorderLayout() );
		panelHorizontalHelper.setBorder( BorderFactory.createEmptyBorder( 5, 10, 0, 5 ) );
		panelHorizontalHelper.add( new JLabel( " t = " ), BorderLayout.WEST );
		panelHorizontalHelper.add( sliderTime, BorderLayout.CENTER );
		pContent.add( panelHorizontalHelper, BorderLayout.SOUTH );

		sliderChannel = new JSlider( JSlider.VERTICAL, 0, ( int ) img.dimension( 2 ) - 1, 0 );
		sliderChannel.addChangeListener( this );
		sliderChannel.setMajorTickSpacing( 5 );
		sliderChannel.setMinorTickSpacing( 1 );
		sliderChannel.setPaintTicks( true );
		sliderChannel.setPaintLabels( true );
		sliderChannel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 3 ) );
		panelVerticalHelper = new JPanel( new BorderLayout() );
		panelVerticalHelper.setBorder( BorderFactory.createEmptyBorder( 10, 10, 0, 5 ) );
		panelVerticalHelper.add( new JLabel( "  C" ), BorderLayout.NORTH );
		panelVerticalHelper.add( sliderChannel, BorderLayout.CENTER );
		pContent.add( panelVerticalHelper, BorderLayout.WEST );

		// Viewer
		iviewer = new InteractiveViewerPanel( ( int ) img.dimension( 0 ), ( int ) img.dimension( 1 ) );
		setScreenImage( 0, 0 );
		pContent.add( iviewer, BorderLayout.CENTER );

		// Controls & Toolbar
		toolbar = createToolBar( iviewer.getEditor() );
		pContent.add( toolbar, BorderLayout.EAST );
		bToolCenterLine = new JButton( "set center-line" );
		bToolCenterLine.addActionListener( this );
		pControls.add( bToolCenterLine );

		// Adding all together
		add( textIntro, BorderLayout.NORTH );
		add( pContent, BorderLayout.CENTER );
		add( pControls, BorderLayout.SOUTH );

		// Key-Bindings
		activateKeyBindings();
	}

	private JToolBar createToolBar( final DrawingEditor editor ) {
		final JToolBar tb = new JToolBar();

		final ResourceBundleUtil orig_labels = ResourceBundleUtil.getBundle( "org.jhotdraw.draw.Labels" );
		final ResourceBundleUtil my_labels = ResourceBundleUtil.getBundle( "com.jug.jhotdraw.draw.Labels" );

		// Add a selection tool to the toolbar.
		ButtonFactory.addSelectionToolTo( tb, editor );

		// Add a creation tool for green rectangles to the toolbar.
		HashMap< AttributeKey, Object > a = new HashMap< AttributeKey, Object >();
		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( a, Color.ORANGE );
		org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH.put( a, 2.0 );
		ButtonFactory.addToolTo( tb, editor, new CreationTool( new LineFigure(), a ), "edit.createLine", orig_labels );

		a = new HashMap< AttributeKey, Object >();
		org.jhotdraw.draw.AttributeKeys.UNCLOSED_PATH_FILLED.put( a, false );
		org.jhotdraw.draw.AttributeKeys.FILL_COLOR.put( a, new Color( 0.0f, 1.0f, 0.0f, 0.1f ) );
		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( a, new Color( 1.0f, 0.0f, 0.0f, 0.33f ) );
		org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH.put( a, 2.5 );
		ButtonFactory.addToolTo( tb, editor, new BezierTool( new BezierFigure( true ), a ), "edit.createScribble", orig_labels );

//		a = new HashMap< AttributeKey, Object >();
//		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( a, Color.RED );
//		org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH.put( a, 2.5 );
//		org.jhotdraw.draw.AttributeKeys.FILL_COLOR.put( a, Color.PINK );
//		ButtonFactory.addToolTo( tb, editor, new CreationTool( new ElongatedBezierFigure(), a ), "edit.createElongatedBezier", my_labels );

		tb.addSeparator(); // =============================================================================================

		ButtonFactory.addZoomButtonsTo( tb, editor );

		tb.addSeparator(); // =============================================================================================

		final JButton buttonMakeKeyFrame = new JButton( "+" );
		final JButton buttonRemoveKeyFrame = new JButton( "-" );
//		buttonMakeKeyFrame.setPreferredSize( new Dimension( 132, 132 ) );
//		buttonRemoveKeyFrame.setPreferredSize( new Dimension( 132, 132 ) );
		buttonMakeKeyFrame.addItemListener( this );
		buttonRemoveKeyFrame.addItemListener( this );
		tb.add( buttonMakeKeyFrame );
		tb.add( buttonRemoveKeyFrame );

		tb.setOrientation( JToolBar.VERTICAL );

		// Place the toolbar on the left
		this.add( tb, BorderLayout.EAST );

		return tb;
	}

	/**
	 *
	 */
	private void activateKeyBindings() {
//		this.getInputMap( WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( 't' ), "STP_bindings" );
//		this.getInputMap( WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( 'c' ), "STP_bindings" );
		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( 't' ), "STP_bindings" );
		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( 'c' ), "STP_bindings" );

		this.getActionMap().put( "STP_bindings", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent e ) {
				if ( e.getActionCommand().equals( "t" ) ) {
					sliderTime.requestFocus();
				}
				if ( e.getActionCommand().equals( "c" ) ) {
					sliderChannel.requestFocus();
				}
			}
		} );
	}

	/**
	 * @param frame
	 * @param channel
	 */
	private void setScreenImage( final int frame, final int channel ) {
		final IntervalView< DoubleType > viewImg = Views.hyperSlice( Views.hyperSlice( img, 3, frame ), 2, channel );
		iviewer.getImglib2viewer().setScreenImage( viewImg );
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed( final ActionEvent e ) {
		if ( e.getSource().equals( bToolCenterLine ) ) {
			System.out.println( "Tool..." );
		}
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged( final ChangeEvent arg0 ) {
		setScreenImage( sliderTime.getValue(), sliderChannel.getValue() );
	}

	/**
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged( final ItemEvent ie ) {
		System.out.println( ie.getSource().toString() );
	}
}
