package com.jug;

/**
 * Main class for the NordenTools-OrthoSlicer project.
 */

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Plot;
import ij.gui.Roi;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import net.imglib2.Cursor;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import com.jug.data.PlotData;
import com.jug.gui.OrthoSlicerGui;

/**
 * @author jug
 */
public class OrthoSlicer {

	private static JFrame guiFrame;
	public ImageJ ij;
	private ImagePlus imgPlus = null;

	/**
	 * PROJECT MAIN
	 * ============
	 *
	 * @param args
	 *            muh!
	 */
	public static void main( final String[] args ) {
		final ImageJ temp = new ImageJ();
//		IJ.open( "/Users/jug/MPI/ProjectNorden/FirstSegmentation_SumProjection_crop.tif" );
//		IJ.open( "/Users/jug/MPI/ProjectNorden/FirstSegmentation3D_wholeStack_crop.tif" );

		final OrthoSlicer main = new OrthoSlicer();
		guiFrame = new JFrame( "NordenTools -- OrthoSlicer" );
		main.init( guiFrame );

		if ( !main.loadCurrentImage() ) { return; }

		IJ.setTool( "Line Tool" );
		final OrthoSlicerGui gui = new OrthoSlicerGui( main );

		main.ij = temp;
		guiFrame.add( gui );
		guiFrame.setSize( 150, 100 );
		guiFrame.setLocation( 0, 100 );
		guiFrame.setVisible( true );
	}

	/**
	 * Loads current IJ image into the OrthoSlicer
	 */
	private boolean loadCurrentImage() {
		imgPlus = WindowManager.getCurrentImage();
		if ( imgPlus == null ) {
			IJ.error( "There must be an active, open window!" );
			return false;
		}
//		final int[] dims = imgPlus.getDimensions(); // width, height, nChannels, nSlizes, nFrames
//		if ( dims[ 3 ] > 1 || dims[ 4 ] < 1 ) {
//			IJ.error( "The active, open window must contain an image with multiple frames, but no slizes!" );
//			return;
//		}
		return true;
	}

	/**
	 * Initializes the main app. This method contains platform
	 * specific code like setting icons, etc.
	 *
	 * @param guiFrame
	 *            the JFrame containing the MotherMachine.
	 */
	private void init( final JFrame guiFrame ) {
		guiFrame.addWindowListener( new WindowAdapter() {

			@Override
			public void windowClosing( final WindowEvent we ) {
				System.exit( 0 );
			}
		} );
	}

	/**
	 * Does the orthogonal slicing and summing.
	 */
	public void slice() {
		final Roi roi = this.imgPlus.getRoi();
		if ( roi == null ) {
			IJ.error( "Use the imagej line-tool to draw a line anling which you want to sum ortho-slices." );
			return;
		}
		try {
			final Line lineRoi = ( Line ) roi;
			IJ.log( "Line: (" + lineRoi.x1 + "," + lineRoi.y1 + ")->(" + lineRoi.x2 + "," + lineRoi.y2 + ")" );
			final PlotData[] data = sumAlongLine( imgPlus, lineRoi );
		}
		catch ( final ClassCastException e ) {
			e.printStackTrace();
			IJ.error( "Found a non-line ROI... please use the line-tool to indicate the slicing direction!" );
		}
	}

	/**
	 * @param imgPlus2
	 * @return
	 */
	private PlotData[] sumAlongLine( final ImagePlus imgPlus2, final Line line ) {
		final Img< UnsignedShortType > img = ImagePlusAdapter.wrap( imgPlus );
		final Cursor< UnsignedShortType > cursor = img.cursor();

		final double vec_x = line.x2 - line.x1;
		final double vec_y = line.y2 - line.y1;
		final double vec_len = Math.sqrt( vec_x*vec_x + vec_y*vec_y );
		final double[] vec = new double[] { vec_x / vec_len, vec_y / vec_len }; //normalized

		final int frames = imgPlus.getDimensions()[ 4 ]; // 4 happens to be the time-dimension...
		final PlotData[] data = new PlotData[ frames ];
		final PlotData[] volume = new PlotData[ frames ];
		for ( int i = 0; i < frames; i++ ) {
			data[ i ] = new PlotData( ( int ) Math.floor( line.getLength() ) );
			volume[ i ] = new PlotData( ( int ) Math.floor( line.getLength() ) );
		}

		double pixel_value;
		while ( cursor.hasNext() ) {
			pixel_value = cursor.next().get();
			final double xpos = cursor.getIntPosition( 0 ) - line.x1;
			final double ypos = cursor.getIntPosition( 1 ) - line.y1;
//			final int zpos = cursor.getIntPosition( 2 );
			final int tpos = cursor.getIntPosition( 3 );

			// compute (x/y)-projection onto line
			final double projected_x = vec[ 0 ] * xpos + vec[ 1 ] * ypos;
			// add the shit
			if ( projected_x >= 0 && projected_x <= Math.floor( line.getLength() ) ) {
				data[ tpos ].addValueToXValue( projected_x, pixel_value );
				if ( pixel_value > 0.0 ) {
					volume[ tpos ].addValueToXValue( projected_x, 1.0 );
				}
			}
		}

		// Plot all the plots... ;)
		for ( int i = frames - 1; i >= 0; i-- ) {
			final double[] volume_normalized = data[ i ].getYData();
			for ( int j = 0; j < volume_normalized.length; j++ ) {
				volume_normalized[ j ] /= volume[ i ].getYData()[ j ];
			}
			final Plot plot = new Plot( "Frame: " + ( i + 1 ), "x", "myosin/volume", data[ i ].getXData(), volume_normalized );
			plot.show();
		}
		return data;
	}
}