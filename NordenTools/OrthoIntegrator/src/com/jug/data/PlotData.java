/**
 *
 */
package com.jug.data;

import ij.IJ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;



/**
 * @author jug
 */
public class PlotData {

	private final double[] data;

	public PlotData( final int maxX ) {
		data = new double[ maxX + 1 ];
	}

	public double[] getXData() {
		final double[] ret = new double[ data.length ];
		for ( int i = 0; i < data.length; i++ ) {
			ret[ i ] = i;
		}
		return ret;
	}

	public double[] getYData() {
		return data;
	}

	/**
	 * @param projected_x
	 * @param d
	 */
	public void addValueToXValue( final int x, final double y ) {
		data[ x ] += y;
	}

	/**
	 * @param projected_x
	 * @param d
	 */
	public void addValueToXValue( final double x, final double y ) {
		final int x1 = ( int ) Math.round( Math.floor( x ) );
		final int x2 = ( int ) Math.round( Math.ceil( x ) );
		final double fraq = x - x1;
		data[ x1 ] += ( 1 - fraq ) * y;
		data[ x2 ] += fraq * y;
	}

	/**
	 * @param path
	 * @param format
	 */
	public void saveToFile( final File path, final String filename ) {
		final File file = new File( path, filename );
		try {
			final FileOutputStream fos = new FileOutputStream( file );
			final OutputStreamWriter out = new OutputStreamWriter( fos );

			for ( int i = 0; i < data.length; i++ ) {
				if ( !Double.isNaN( data[ i ] ) ) {
					out.write( String.format( "%4d, %12.5f\n", i, data[ i ] ) );
				}
			}
			out.close();
			fos.close();
		}
		catch ( final FileNotFoundException e ) {
			IJ.error( "File '" + file.getAbsolutePath() + "' could not be opened!" );
		}
		catch ( final IOException e ) {
			IJ.error( "Could not write to file '" + file.getAbsolutePath() + "'!" );
		}

	}
}
