/**
 *
 */
package com.jug.data;



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
}
