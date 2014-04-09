package view.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import model.figure.DrawFigureFactory;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.RealARGBConverter;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.QuadTreeDrawing;
import org.jhotdraw.draw.action.ButtonFactory;
import org.jhotdraw.draw.io.DOMStorableInputOutputFormat;
import org.jhotdraw.draw.io.InputFormat;
import org.jhotdraw.draw.io.OutputFormat;
import org.jhotdraw.draw.tool.BezierTool;
import org.jhotdraw.draw.tool.Tool;
import org.jhotdraw.util.ResourceBundleUtil;

import view.display.InteractiveDrawingView;
import view.viewer.InteractiveRealViewer2D;

import com.jug.util.ImglibUtil;

import controller.tool.NullTool;

/**
 * A swing panel that can show imglib2 image data and annotate them using
 * JHotDraw figures.
 * 
 * @author HongKee Moon, Florian Jug, Tobias Pietzsch
 * @since 9/4/13
 */

//TODO This component should be totally generic, being able to host all image types (not only LongType and DoubleType)

public class IddeaComponent extends JPanel {

	private static final long serialVersionUID = -3808140519052170304L;

	// The everything containing scroll-bar
	private final JScrollPane scrollPane;

	// InteractiveViewer2D for the imglib2 data to be shown.
	private InteractiveRealViewer2D< DoubleType > interactiveViewer2D;

	// The imglib2 image data container
	private IntervalView< DoubleType > ivSourceImage = null;

	// JHotDraw related stuff
	private DrawingEditor editor;
	private final InteractiveDrawingView view;

	// Toolbar setup and the toolbar itself
	private String toolbarLocation;
	private boolean isToolbarVisible = false;
	private JToolBar tb;

	//////////////////////////// CONSTUCTION /////////////////////////////////

	/**
	 * Creates an <code>IddeaComponent</code> that does not (yet) display any
	 * image.
	 */
	public IddeaComponent() {
		this( null );
	}

	/**
	 * Creates an <code>IddeaComponent</code> and adds the given
	 * <code>soureImage</code> to it.
	 */
	public IddeaComponent( final IntervalView< DoubleType > sourceImage ) {

		this.ivSourceImage = sourceImage;

		editor = new DefaultDrawingEditor();
		createEmptyToolbar();

		scrollPane = new javax.swing.JScrollPane();
		view = buildInteractiveDrawingView( ivSourceImage );

		setLayout( new java.awt.BorderLayout() );

		scrollPane.setHorizontalScrollBarPolicy( javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setVerticalScrollBarPolicy( javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER );
		scrollPane.setViewportView( view );

		add( scrollPane, java.awt.BorderLayout.CENTER );

		if ( isToolbarVisible ) {
			add( tb, toolbarLocation );
		}

		setEditor( editor );
		view.setDrawing( createDrawing() );
	}

	//////////////////////////// GETTERS AND SETTERS /////////////////////////////////

	/**
	 * @return The <code>AffineTransform2D</code> describing the transformation
	 *         of the <code>ivSourceImage</code> on screen.
	 */
	public AffineTransform2D getViewerTransform() {
		return interactiveViewer2D.getViewerTransform();
	}

	/**
	 * @return All JHotDraw annotations made in this component.
	 */
	public Set< Figure > getAllAnnotationFigures() {
		return interactiveViewer2D.getJHotDrawDisplay().getAllFigures();
	}

	//////////////////////////// OVERRIDDEN /////////////////////////////////

	@Override
	public void setPreferredSize( final Dimension dim ) {
		interactiveViewer2D.getJHotDrawDisplay().setPreferredSize( dim );
	}

	//////////////////////////// FUNCTIONS /////////////////////////////////

	/**
	 * Returns the current screen image.
	 * 
	 * @return
	 */
	public IntervalView< DoubleType > getSourceImage() {
		return this.ivSourceImage;
	}

	/**
	 * Installs a toolbar that contains no annotation functionality at all.
	 */
	public void installDefaultToolBar() {
		this.tb.removeAll();

		ButtonFactory.addSelectionToolTo( tb, editor, ButtonFactory.createDrawingActions( editor ), ButtonFactory.createSelectionActions( editor ) );

		final ResourceBundleUtil labels = ResourceBundleUtil.getBundle( "model.Labels" );
		ButtonFactory.addToolTo( tb, editor, new NullTool(), "edit.handleImageData", labels );

		final HashMap< AttributeKey, Object > polygon = new HashMap< AttributeKey, Object >();
		org.jhotdraw.draw.AttributeKeys.FILL_COLOR.put( polygon, new Color( 0.0f, 0.0f, 1.0f, 0.1f ) );
		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( polygon, new Color( 0.0f, 0.0f, 1.0f, 0.33f ) );
		ButtonFactory.addToolTo( tb, editor, new BezierTool( new BezierFigure( true ), polygon ), "edit.createPolygon", ResourceBundleUtil.getBundle( "org.jhotdraw.draw.Labels" ) );
	}

	/**
	 * @return Returns the currently installed toolbar.
	 */
	public JToolBar getInstalledToolbar() {
		return this.tb;
	}

	/**
	 * Sets the location of the toolbar.
	 * 
	 * @param location
	 *            Either <code>BorderLayout.NORTH</code>,
	 *            <code>BorderLayout.EAST</code>,
	 *            <code>BorderLayout.SOUTH</code>, or
	 *            <code>BorderLayout.WEST</code>
	 */
	public void setToolBarLocation( final String location ) {
		if ( location.equals( BorderLayout.WEST ) || location.equals( BorderLayout.EAST ) ) {
			tb.setOrientation( JToolBar.VERTICAL );
		} else {
			tb.setOrientation( JToolBar.HORIZONTAL );
		}

		toolbarLocation = location;

		if ( isToolbarVisible ) {
			setToolBarVisible( false );
			setToolBarVisible( true );
		}
	}

	/**
	 * Shows or hides the currently installed toolbar.
	 * 
	 * @param visible
	 */
	public void setToolBarVisible( final boolean visible ) {
		isToolbarVisible = visible;
		if ( isToolbarVisible ) {
			add( tb, toolbarLocation );
		} else {
			remove( tb );
		}
	}

	/**
	 * Loads annotations from file.
	 * 
	 * @param filename
	 */
	public void loadAnnotations( final String filename ) {
		try {
			final Drawing drawing = createDrawing();

			boolean success = false;
			for ( final InputFormat sfi : drawing.getInputFormats() ) {
				try {
					sfi.read( new FileInputStream( filename ), drawing, true );
					success = true;
					break;
				} catch ( final Exception e ) {
					// try with the next input format
				}
			}
			if ( !success ) {
				final ResourceBundleUtil labels = ResourceBundleUtil.getBundle( "org.jhotdraw.app.Labels" );
				throw new IOException( labels.getFormatted( "file.open.unsupportedFileFormat.message", filename ) );
			}

			SwingUtilities.invokeAndWait( new Runnable() {

				@Override
				public void run() {
					view.setDrawing( drawing );
				}
			} );
		} catch ( final InterruptedException e ) {
			final InternalError error = new InternalError();
			error.initCause( e );
			throw error;
		} catch ( final java.lang.reflect.InvocationTargetException e ) {
			final InternalError error = new InternalError();
			error.initCause( e );
			throw error;
		} catch ( final IOException e ) {
			final InternalError error = new InternalError();
			error.initCause( e );
			throw error;
		}
	}

	/**
	 * Saves all annotations to a given file.
	 * 
	 * @param filename
	 */
	public void saveAnnotations( final String filename ) {
		final Drawing drawing = view.getDrawing();
		final OutputFormat outputFormat = drawing.getOutputFormats().get( 0 );
		try {
			outputFormat.write( new FileOutputStream( filename ), drawing );
		} catch ( final IOException e ) {
			final InternalError error = new InternalError();
			error.initCause( e );
			throw error;

		}
	}

	/**
	 * Replaces the current
	 * 
	 * @param viewImg
	 */
	public < T extends RealType< T > & NativeType< T >> void setSourceImage( final IntervalView< T > viewImg ) {
		final T min = Views.iterable( viewImg ).firstElement().copy();
		final T max = min.copy();
		ImglibUtil.computeMinMax( viewImg, min, max );

		RealRandomAccessible< T > interpolated = null;
		if ( viewImg.numDimensions() > 2 ) {
			interpolated = Views.interpolate( Views.extendZero( Views.hyperSlice( viewImg, 2, 0 ) ), new NearestNeighborInterpolatorFactory< T >() );
		} else {
			interpolated = Views.interpolate( Views.extendZero( viewImg ), new NearestNeighborInterpolatorFactory< T >() );
		}

		final RealARGBConverter< T > converter = new RealARGBConverter< T >( min.getMinValue(), max.getMaxValue() );

		updateDoubleTypeSourceAndConverter( interpolated, converter );
	}

	/**
	 * Sets the image data to be displayed.
	 * 
	 * @param sourceImage
	 *            an IntervalView<DoubleType> containing the desired view
	 *            onto the raw image data
	 */
	public void setDoubleTypeSourceImage( final IntervalView< DoubleType > sourceImage ) {
		final DoubleType min = new DoubleType();
		final DoubleType max = new DoubleType();
		ImglibUtil.computeMinMax( sourceImage, min, max );

		RealRandomAccessible< DoubleType > interpolated = null;
		if ( sourceImage.numDimensions() > 2 ) {
			interpolated = Views.interpolate( Views.extendZero( Views.hyperSlice( sourceImage, 2, 0 ) ), new NearestNeighborInterpolatorFactory< DoubleType >() );
		} else {
			interpolated = Views.interpolate( Views.extendZero( sourceImage ), new NearestNeighborInterpolatorFactory< DoubleType >() );
		}

		final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( min.get(), max.get() );

		updateDoubleTypeSourceAndConverter( interpolated, converter );
	}

	/**
	 * Sets the image data to be displayed.
	 * 
	 * @param raiSource
	 *            an RandomAccessibleInterval<DoubleType> containing the desired
	 *            view
	 *            onto the raw image data
	 */
	public void setDoubleTypeSourceImage( final RandomAccessibleInterval< DoubleType > raiSource ) {
		this.setDoubleTypeSourceImage( Views.interval( raiSource, raiSource ) );
	}

	/**
	 * Sets the image data to be displayed.
	 * 
	 * @param glf
	 *            the GrowthLineFrameto be displayed
	 * @param sourceImage
	 *            an IntervalView<LongType> containing the desired view
	 *            onto the raw image data
	 */
	public void setLongTypeSourceImage( final IntervalView< LongType > sourceImage ) {

		final LongType min = new LongType();
		final LongType max = new LongType();
		ImglibUtil.computeMinMax( sourceImage, min, max );

		final RealRandomAccessible< LongType > interpolated = Views.interpolate( Views.extendZero( sourceImage ), new NearestNeighborInterpolatorFactory< LongType >() );

		final RealARGBConverter< LongType > converter = new RealARGBConverter< LongType >( min.get(), max.get() );

		updateDoubleTypeSourceAndConverter( interpolated, converter );
	}

	/**
	 * Sets the image data to be displayed.
	 * 
	 * @param raiSource
	 *            an RandomAccessibleInterval<LongType> containing the desired
	 *            view
	 *            onto the raw image data
	 */
	public void setLongTypeSourceImage( final RandomAccessibleInterval< LongType > raiSource ) {
		this.setLongTypeSourceImage( Views.interval( raiSource, raiSource ) );
	}

	/**
	 * Update the realRandomSource with new source.
	 * 
	 * @param source
	 */
	public void updateDoubleTypeSourceAndConverter( final RealRandomAccessible source, final RealARGBConverter converter ) {
		interactiveViewer2D.updateConverter( converter );
		interactiveViewer2D.updateSource( source );
	}

	//////////////////////////// PRIVATE STUFF /////////////////////////////////

	/**
	 * Builds an Interactive Drawing view from a given <code>sourceImage</code>
	 * Caution: this function also creates a new
	 * <code>interactiveViewer2D</code>.
	 * 
	 * @param sourceImage
	 * @return
	 */
	private InteractiveDrawingView buildInteractiveDrawingView( final IntervalView< DoubleType > sourceImage ) {
		if ( sourceImage != null ) {
			final AffineTransform2D transform = new AffineTransform2D();

			final DoubleType min = new DoubleType();
			final DoubleType max = new DoubleType();
			ImglibUtil.computeMinMax( sourceImage, min, max );

			final RealRandomAccessible< DoubleType > interpolated = Views.interpolate( Views.extendZero( sourceImage ), new NearestNeighborInterpolatorFactory< DoubleType >() );
			final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( min.get(), max.get() );

			interactiveViewer2D = new InteractiveRealViewer2D< DoubleType >( ( int ) sourceImage.max( 0 ), ( int ) sourceImage.max( 1 ), interpolated, transform, converter );
		} else {
			final AffineTransform2D transform = new AffineTransform2D();
			final RealRandomAccessible< DoubleType > dummy = new DummyRealRandomAccessible();
			final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( 0, 0 );

			interactiveViewer2D = new InteractiveRealViewer2D< DoubleType >( 300, 200, dummy, transform, converter );
		}

		return interactiveViewer2D.getJHotDrawDisplay();
	}

	/**
	 * Creates a new Drawing for this view.
	 */
	private Drawing createDrawing() {
		final Drawing drawing = new QuadTreeDrawing();
		final DOMStorableInputOutputFormat ioFormat = new DOMStorableInputOutputFormat( new DrawFigureFactory() );

		drawing.addInputFormat( ioFormat );
		drawing.addOutputFormat( ioFormat );

		return drawing;
	}

	/**
	 * Sets a JHotDraw drawing editor for this component.
	 */
	private void setEditor( final DrawingEditor newValue ) {
		if ( editor != null ) {
			editor.remove( view );
		}
		editor = newValue;
		if ( editor != null ) {
			editor.add( view );
		}
	}

	/**
	 * Creates an empty toolbar.
	 */
	private void createEmptyToolbar() {
		this.tb = new JToolBar();
		this.tb.setOrientation( JToolBar.VERTICAL );

		final ResourceBundleUtil labels = ResourceBundleUtil.getBundle( "org.jhotdraw.draw.Labels" );
		tb.setName( labels.getString( "window.drawToolBar.title" ) );
	}

	/**
	 * Adds a <code>Tool</code> to the toolbar.
	 * 
	 * @param tool
	 *            The tool to be added to the currently installed toolbar.
	 * @param lableKey
	 *            The key that points to the label resource.
	 * @param labels
	 *            All the labels.
	 */
	public void addTool( final Tool tool, final String labelKey, final ResourceBundleUtil labels ) {
		ButtonFactory.addToolTo( tb, editor, tool, labelKey, labels );
	}

	/**
	 * @param strokes
	 *            An array containing all available stroke thicknesses.
	 */
	public void addToolStrokeWidthButton( final double[] strokes ) {
		tb.add( ButtonFactory.createStrokeWidthButton( editor, strokes, ResourceBundleUtil.getBundle( "org.jhotdraw.draw.Labels" ) ) );
	}

	/**
	 * Adds an separator to the currently installed toolbar.
	 */
	public void addToolBarSeparator() {
		tb.addSeparator();
	}

}
