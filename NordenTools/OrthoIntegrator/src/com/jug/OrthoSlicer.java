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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import net.imglib2.Cursor;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;

import com.jug.data.Ellipse2D;
import com.jug.data.PlotData;
import com.jug.data.PointCloud2D;
import com.jug.data.Vector2D;
import com.jug.util.DumpImgFactory;

/**
 * @author jug
 */
public class OrthoSlicer {

	private static OrthoSlicer main;

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
		ImageJ temp = IJ.getInstance();

		if ( temp == null ) {
			temp = new ImageJ();
			IJ.open( "/Users/jug/MPI/ProjectNorden/FirstSegmentation3D_wholeStack_NordenCrop.tif" );
		}

		main = new OrthoSlicer();

		if ( !main.loadCurrentImage() ) { return; }

		final Line lineRoi = new Line( 0, main.imgPlus.getHeight(), main.imgPlus.getWidth(), 0 );
		main.imgPlus.setRoi( lineRoi, true );
		main.shapeFittingEllipses( true );
	}

	public static OrthoSlicer getInstance() {
		return main;
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
	 * Does the orthogonal slicing and summing.
	 */
	public void projectToLine(final boolean showPlots) {
		final Roi roi = this.imgPlus.getRoi();
		if ( roi == null ) {
			IJ.error( "Use the imagej line-tool to draw a line anling which you want to sum ortho-slices." );
			return;
		}

		final JFileChooser chooser = new JFileChooser();

		chooser.setCurrentDirectory( new File( imgPlus.getOriginalFileInfo().directory ) );
		chooser.setDialogTitle( "Choose folder to export plot data to..." );
		chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		chooser.setAcceptAllFileFilterUsed( false );
		chooser.setMultiSelectionEnabled( false );

		File path = null; // output folder for CSV-data...
		if ( chooser.showSaveDialog( IJ.getInstance() ) == JFileChooser.APPROVE_OPTION ) {
			path = chooser.getSelectedFile();
		}

		try {
			final Line lineRoi = ( Line ) roi;
//			IJ.log( "Line: (" + lineRoi.x1 + "," + lineRoi.y1 + ")->(" + lineRoi.x2 + "," + lineRoi.y2 + ")" );
			final PlotData[] data = concentrationAlongLine( lineRoi );

			// Export plot data and show plots
			for ( int i = 0; i < data.length; i++ ) {
				if ( path != null ) {
					data[ i ].saveToFile( path, String.format( "concentration_plot_data_t%03d.csv", i + 1 ) );
				}

				if ( showPlots ) {
					plotStripped( "Frame: " + ( i + 1 ), data[ i ].getXData(), data[ i ].getYData() );
				}
			}

		}
		catch ( final ClassCastException e ) {
			e.printStackTrace();
			IJ.error( "Found a non-line ROI... please use the line-tool to indicate the slicing direction!" );
		}
	}

	/**
	 * @param xData
	 * @param volume_normalized
	 */
	private Plot plotStripped( final String title, final double[] xData, final double[] yData ) {
		final ArrayList< Double > alX = new ArrayList< Double >();
		final ArrayList< Double > alY = new ArrayList< Double >();

		for ( int i = 0; i < xData.length; i++ ) {
			if ( yData[ i ] > 0.0001 ) {
				alX.add( new Double( xData[ i ] ) );
				alY.add( new Double( yData[ i ] ) );
			}
		}

		final double[] x = new double[ alX.size() ];
		final double[] y = new double[ alY.size() ];
		for ( int i = 0; i < alX.size(); i++ ) {
			x[ i ] = alX.get( i ).doubleValue();
			y[ i ] = alY.get( i ).doubleValue();
		}

		final Plot plot = new Plot( title, "x", "myosin/volume", x, y );
		plot.show();

		return plot;
	}

	/**
	 * @param imgPlus2
	 * @return
	 */
	private PlotData[] concentrationAlongLine( final Line line ) {
		final Img< UnsignedShortType > img = ImagePlusAdapter.wrap( imgPlus );
		final Cursor< UnsignedShortType > cursor = img.cursor();

		final double vec_x = line.x2 - line.x1;
		final double vec_y = line.y2 - line.y1;
		final double vec_len = Math.sqrt( vec_x*vec_x + vec_y*vec_y );
		final double[] vec = new double[] { vec_x / vec_len, vec_y / vec_len }; //normalized

		final int frames = imgPlus.getDimensions()[ 4 ]; // 4 happens to be the time-dimension...
		final PlotData[] summed_intensities = new PlotData[ frames ];
		final PlotData[] volume = new PlotData[ frames ];
		final PlotData[] concentrations = new PlotData[ frames ];

		for ( int i = 0; i < frames; i++ ) {
			summed_intensities[ i ] = new PlotData( ( int ) Math.floor( line.getLength() ) );
			volume[ i ] = new PlotData( ( int ) Math.floor( line.getLength() ) );
			concentrations[ i ] = new PlotData( ( int ) Math.floor( line.getLength() ) );
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
				if ( pixel_value > 0.0 ) {
					summed_intensities[ tpos ].addValueToXValue( projected_x, pixel_value );
					volume[ tpos ].addValueToXValue( projected_x, 1.0 );
				}
			}
		}

		// compute concentrations (iterate over summed intensities and divide by volume)
		for ( int t = 0; t < summed_intensities.length; t++ ) {
			for ( int i = 0; i < summed_intensities[ t ].getXData().length; i++ ) {
				concentrations[ t ].addValueToXValue( summed_intensities[ t ].getXData()[ i ], summed_intensities[ t ].getYData()[ i ] / volume[ t ].getYData()[ i ] );
			}
		}

		return concentrations;
	}

	/**
	 *
	 */
	public void shapeFittingEllipses( final boolean showPlots ) {
		final Roi roi = this.imgPlus.getRoi();
		if ( roi == null ) {
			IJ.error( "Use the imagej line-tool to draw a line anling which you want to sum ortho-slices." );
			return;
		}

		final JFileChooser chooser = new JFileChooser();

		chooser.setCurrentDirectory( new File( imgPlus.getOriginalFileInfo().directory ) );
		chooser.setDialogTitle( "Choose folder to export gaussian shape fits to..." );
		chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		chooser.setAcceptAllFileFilterUsed( false );
		chooser.setMultiSelectionEnabled( false );

		File path = null; // output folder for CSV-data...
		if ( chooser.showSaveDialog( IJ.getInstance() ) == JFileChooser.APPROVE_OPTION ) {
			path = chooser.getSelectedFile();
		}

		Line lineRoi = null;
		try {
			lineRoi = ( Line ) roi;
		} catch ( final ClassCastException e ) {
			e.printStackTrace();
			IJ.error( "Found a non-line ROI... please use the line-tool to indicate the slicing direction!" );
			return;
		}

		// extract point (voxel) coulds along line-ROI)
		final List< List< PointCloud2D< Double >>> allPointClouds = getOrthogonalPointClouds( lineRoi, 4.0 );

		// create new Img<Double> showing those points
		final Img< DoubleType > dump = DumpImgFactory.dumpPointClouds( allPointClouds, true );
		ImageJFunctions.show( dump, "Dumped PointClouds" );

		// iterate over all time-point and there over all point-clouds -- approximate by ellipse and collect those!
		int t = 1;
		final List< List< Ellipse2D >> ellipticalShapes = new ArrayList< List< Ellipse2D >>();
		for ( final List< PointCloud2D< Double > > shapeClouds : allPointClouds ) {
//			IJ.log( "--- NEXT SHAPE -------------------------------------------------" );
			final ArrayList< Ellipse2D > newShape = new ArrayList< Ellipse2D >();
			int z = 1;
			for ( final PointCloud2D< Double > cloud : shapeClouds ) {
				if ( cloud.isEmpty() ) {
					newShape.add( new Ellipse2D() );
				} else {
					final Ellipse2D ellipse = cloud.getEllipticalApproximation();
					newShape.add( ellipse );
					if ( t == 6 && z >= 236 && z <= 237 ) {
						IJ.log( String.format( "Ellipse: (x,y,z);angle;(a,b) = (%.2f, %.2f, %d); %.2f; (%.2f, %.2f)", ellipse.getCenter().getX(), ellipse.getCenter().getY(), newShape.size(), ellipse.getAngle(), ellipse.getA(), ellipse.getB() ) );
					}
				}
				z++;
			}

			if ( path != null ) {
				saveEllipticalShapeToFile( newShape, path, String.format( "elliptical_shape_t%03d.csv", ellipticalShapes.size() + 1 ) );
			}

			ellipticalShapes.add( newShape );
			t++;
		}

		// create new Img<Double> showing the approximated ellipses
		final Img< DoubleType > dump2 = DumpImgFactory.dumpEllipticalShapes( ellipticalShapes );
		ImageJFunctions.show( dump2, "Dumped Ellipses" );

		// Plot
		if ( showPlots ) {

		}
	}

	/**
	 * @param newShape
	 * @param path
	 * @param format
	 */
	private void saveEllipticalShapeToFile( final ArrayList< Ellipse2D > shape, final File path, final String filename ) {
		final File file = new File( path, filename );
		try {
			final FileOutputStream fos = new FileOutputStream( file );
			final OutputStreamWriter out = new OutputStreamWriter( fos );

			out.write( "# Each row one ellipse, parametrized as: x, y, angle, a, b" );
			for ( final Ellipse2D e : shape ) {
				out.write( String.format( "%f, %f, %f, %f, %f\n", e.getCenter().getX(), e.getCenter().getY(), e.getAngle(), e.getA(), e.getB() ) );
			}
			out.close();
			fos.close();
		} catch ( final FileNotFoundException e ) {
			IJ.error( "File '" + file.getAbsolutePath() + "' could not be opened!" );
		} catch ( final IOException e ) {
			IJ.error( "Could not write to file '" + file.getAbsolutePath() + "'!" );
		}
	}

	/**
	 * Iterates over imgPlus, projects each voxel onto given line and adds two
	 * copies of that voxel into PointClouds2D-objects.
	 * Two copies since projection will lie in between to ortho-slizes along
	 * line. Each copy will also split the voxel intensity based on the relative
	 * distance of the voxel center to the two ortho-slizes.
	 *
	 * @param line
	 *            Line ROI onto which to project. (We imagine this line to be at
	 *            z=0!)
	 * @return
	 */
	private List< List< PointCloud2D< Double >>> getOrthogonalPointClouds( final Line line, final double factorVoxelDepth ) {
		final int num_frames = imgPlus.getDimensions()[ 4 ]; // happens to be the time dimension
		final int num_slices = ( int ) line.getLength();
		final List< List< PointCloud2D< Double >>> ret = new ArrayList< List< PointCloud2D< Double >>>();

		// Add new PointCloud2D for each ortho-slice
		for ( int i = 0; i < num_frames; i++ ) {
			ret.add( new ArrayList< PointCloud2D< Double >>() );
			for ( int j = 0; j <= num_slices; j++ ) { // '=' because of fractional_slice_num below
				ret.get( i ).add( new PointCloud2D< Double >() );
			}
		}

		// Wrap imgPlus into Img
		final Img< UnsignedShortType > img = ImagePlusAdapter.wrap( imgPlus );
		final Cursor< UnsignedShortType > cursor = img.cursor();

		// get 'line' as vector based at 0
		final double vec_x = line.x2 - line.x1;
		final double vec_y = line.y2 - line.y1;
		// direction of 'line' as normalized vecter at 0
		final Vector2D vec_line_direction = new Vector2D( vec_x, vec_y );
		final Vector2D vec_orthogonal_direction = new Vector2D( vec_y, -vec_x );

		// Iterate over each voxel in Img
		double pixel_value;
		while ( cursor.hasNext() ) {
			pixel_value = cursor.next().get();
			final double xpos = cursor.getIntPosition( 0 ) - line.x1;
			final double ypos = cursor.getIntPosition( 1 ) - line.y1;
			final int zpos = cursor.getIntPosition( 2 );
			final double zdepth = factorVoxelDepth * zpos;
			final int tpos = cursor.getIntPosition( 3 );

			// compute (x/y)-projection onto line
			final Vector2D vec_pos = new Vector2D( xpos, ypos );
			final double fractional_slice_num = vec_line_direction.project( vec_pos ).getLength();
			final double dist_to_line = vec_orthogonal_direction.project( vec_pos ).getLength();
			// add the shit
			if ( fractional_slice_num >= 0 && fractional_slice_num <= Math.floor( line.getLength() ) ) {
				if ( pixel_value > 0.0 ) {
					final double p = ( fractional_slice_num - Math.floor( fractional_slice_num ) );
					ret.get( tpos ).get( ( int ) fractional_slice_num ).addPoint( dist_to_line, zdepth, p * pixel_value );
					ret.get( tpos ).get( ( int ) fractional_slice_num + 1 ).addPoint( dist_to_line, zdepth, ( 1 - p ) * pixel_value );
				}
			}
		}

		return ret;
	}
}