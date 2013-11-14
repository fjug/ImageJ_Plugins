/*
 */

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.frame.PlugInFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.DoubleType;

import com.jug.ij.panels.InterpolatedCropPanel;
import com.jug.ij.panels.ShapeTrackPanel;

/**
 * Retinal Cell Shape Tracker
 *
 * @author jug
 */
public class RetinalCellShapeTracker_ extends PlugInFrame implements ActionListener {

	/**
	 * Serialization ID
	 */
	private static final long serialVersionUID = 7509276069657478715L;

	private ImagePlus imgPlus;
	private InterpolatedCropPanel cropPanel;
	private ShapeTrackPanel trackPanel;

	/**
	 * Construction
	 */
	public RetinalCellShapeTracker_() {
		super( "RetinaCellShapeTracker" );
	}

	@Override
	public void run( final String arg ) {
		imgPlus = WindowManager.getCurrentImage();
		if ( imgPlus == null ) {
			IJ.error( "There must be an active, open window!" );
			return;
		}
		final int[] dims = imgPlus.getDimensions(); // width, height, nChannels, nSlizes, nFrames
		if ( dims[ 3 ] > 1 || dims[ 4 ] < 1 ) {
			IJ.error( "The active, open window must contain an image with multiple frames, but no slizes!" );
			return;
		}

		cropPanel = new InterpolatedCropPanel( this, imgPlus );
		this.add( cropPanel );

		this.setVisible( true );
		System.out.println( "RCST started" );
	}

	public static void main( final String[] args ) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
//		final Class< ? > clazz = RetinalCellShapeTracker_.class;
//		final String url = clazz.getResource( "/" + clazz.getName().replace( '.', '/' ) + ".class" ).toString();
//		final String pluginsDir = url.substring( 5, url.length() - clazz.getName().length() - 6 );
//		System.setProperty( "plugins.dir", pluginsDir );

		new ImageJ();
//		final ImagePlus image = IJ.openImage( "http://imagej.net/images/clown.jpg" );
		final ImagePlus image = IJ.openImage( "/Users/jug/MPI/ProjectNorden/ShapeTrackingDummy.tif" );
		image.show();
		WindowManager.addWindow( image.getWindow() );

//		IJ.runPlugIn( clazz.getName(), "parameter=Hello" );
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed( final ActionEvent e ) {
		if ( e.getSource().equals( cropPanel.getButtonCrop() ) ) {
//			this.setVisible( false );
			this.remove( cropPanel );

			// Here we switch to ImgLib2...
			final Img< DoubleType > croppedImg = cropPanel.getCroppedImgNormalized();

			trackPanel = new ShapeTrackPanel( this, croppedImg );
			this.add( trackPanel );
//			this.setVisible( true );
		}
	}

}
