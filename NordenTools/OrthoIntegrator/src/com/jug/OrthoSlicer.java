package com.jug;

/**
 * Main class for the NordenTools-OrthoSlicer project.
 */

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

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
		IJ.open( "/Users/jug/MPI/ProjectNorden/FirstSegmentation_SumProjection_crop.tif" );

		final OrthoSlicer main = new OrthoSlicer();
		guiFrame = new JFrame( "NordenTools -- OrthoSlicer" );
		main.init( guiFrame );
		main.loadCurrentImage();

		final OrthoSlicerGui gui = new OrthoSlicerGui();
		gui.setVisible( true );

		main.ij = temp;
		guiFrame.add( gui );
//			guiFrame.setSize( GUI_WIDTH, GUI_HEIGHT );
//			guiFrame.setLocation( GUI_POS_X, GUI_POS_Y );
		guiFrame.setVisible( true );
	}

	/**
	 * Loads current IJ image into the OrthoSlicer
	 */
	private void loadCurrentImage() {
		imgPlus = WindowManager.getCurrentImage();
		if ( imgPlus == null ) {
			IJ.error( "There must be an active, open window!" );
			IJ.showMessage( "There must be an active, open window!" );
			return;
		}
//		final int[] dims = imgPlus.getDimensions(); // width, height, nChannels, nSlizes, nFrames
//		if ( dims[ 3 ] > 1 || dims[ 4 ] < 1 ) {
//			IJ.error( "The active, open window must contain an image with multiple frames, but no slizes!" );
//			return;
//		}
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
}