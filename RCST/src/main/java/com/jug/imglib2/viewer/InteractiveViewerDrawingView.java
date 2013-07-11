package com.jug.imglib2.viewer;
/**
 *
 */


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.RealARGBConverter;
import net.imglib2.display.XYProjector;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;

import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingView;


/**
 * @author jug
 */
//public class Viewer2DCanvas extends JComponent implements MouseInputListener {
public class InteractiveViewerDrawingView extends DefaultDrawingView {

	private static final long serialVersionUID = 8284204775277266994L;

	private int contentWidth;
	private int contentHeight;
	private XYProjector projector;
	private ARGBScreenImage screenImage;
	private IntervalView< DoubleType > view;

	// tracking the mouse (when over)
	private boolean isMouseOver;
	private int mousePosX;
	private int mousePosY;

	// tracking the mouse (when dragging)
	private boolean isDragging;
	private int dragX;
	private int dragY;

	public InteractiveViewerDrawingView( final DefaultDrawing drawing, final int w, final int h ) {
		super();

		super.setOpaque( false );
		drawing.set( org.jhotdraw.draw.AttributeKeys.CANVAS_FILL_OPACITY, 0.0 );
		setDrawing( drawing );

		this.setContentDimensions( w, h );
		this.projector = null;
		this.view = null;
	}

	/**
	 * @param w
	 *            width
	 * @param h
	 *            height
	 */
	private void setContentDimensions( final int w, final int h ) {
		this.setContentWidth( w );
		this.setContentHeight( h );
		setPreferredSize( new Dimension( w, h ) );
		this.screenImage = new ARGBScreenImage( w, h );
	}

	/**
	 * Sets the image data to be displayed when paintComponent is called.
	 *
	 * @param glf
	 *            the GrowthLineFrameto be displayed
	 * @param viewImg
	 *            an IntervalView<DoubleType> containing the desired view
	 *            onto the raw image data
	 */
	public void setScreenImage( final IntervalView< DoubleType > viewImg ) {
		setEmptyScreenImage();
		this.setContentDimensions( ( int ) viewImg.dimension( 0 ), ( int ) viewImg.dimension( 1 ) );
		this.projector = new XYProjector< DoubleType, ARGBType >( viewImg, screenImage, new RealARGBConverter< DoubleType >( 0, 1 ) );
		this.view = viewImg;
		this.repaint();
	}

	/**
	 * Prepares to display an empty image.
	 */
	public void setEmptyScreenImage() {
		screenImage = new ARGBScreenImage( getContentWidth(), getContentHeight() );
		this.projector = null;
		this.view = null;
	}


	@Override
	public void paintComponent( final Graphics gr ) {
		if ( projector != null ) {
			projector.map();
		}

		// DRAW HERE !!!
		gr.drawImage( screenImage.image(), 0, 0, getContentWidth(), getContentHeight(), null );

//		super.paintComponent( gr );
		final Graphics2D g = ( Graphics2D ) gr;
		setViewRenderingHints( g );
//		drawBackground( g );
		drawCanvas( g );
		drawConstrainer( g );
		if ( isDrawingDoubleBuffered() ) {
			if ( System.getProperty( "os.name" ).toLowerCase().startsWith( "win" ) ) {
				drawDrawingNonvolatileBuffered( g );
			} else {
				drawDrawingVolatileBuffered( g );
			}
		} else {
			drawDrawing( g );
		}
		drawHandles( g );
		drawTool( g );
	}

	/**
	 * @return the w
	 */
	public int getContentWidth() {
		return contentWidth;
	}

	/**
	 * @param w
	 *            the w to set
	 */
	public void setContentWidth( final int w ) {
		this.contentWidth = w;
	}

	/**
	 * @return the h
	 */
	public int getContentHeight() {
		return contentHeight;
	}

	/**
	 * @param h
	 *            the h to set
	 */
	public void setContentHeight( final int h ) {
		this.contentHeight = h;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension( contentWidth, contentHeight );
	}
}