/**
 *
 */
package com.jug.fkt;

import java.util.List;

/**
 * @author jug
 */
public class SampledFunction1D implements Function1D< Double > {

	private static final long serialVersionUID = 3831229532343640677L;

	private final List< Double > x;
	private final List< Double > y;

	public SampledFunction1D( final List< Double > x, final List< Double > y ) {
		assert x.size() == y.size();
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns a linear interpolation between the two function samples enclosing
	 * the given value x.
	 * 
	 * @return If this function contains zero x/y-samples we return zero,
	 *         otherwise a linear interpolation between the two closest
	 *         (enclosing) data-points. If the given parameter x is at the left
	 *         of all x/y-samples this function does also return 0.
	 * @see com.jug.fkt.Function1D#evaluate(double)
	 */
	@Override
	public Double evaluate( final double x ) {
		if ( this.x.size() == 0 ) return new Double( 0 );

		int i;
		for ( i = 0; i < this.x.size(); i++ ) {
			if ( this.x.get( i ) >= x ) break;
		}

		if ( i == 0 || i == this.x.size() ) {
			return new Double( 0 );
		} else {
			final double left = this.x.get( i - 1 );
			final double right = this.x.get( i );
			final double ratio = ( x - left ) / ( right - left );

			return new Double( ratio * this.y.get( i - 1 ) + ( 1 - ratio ) * this.y.get( i ) );
		}
	}

	/**
	 * @return a list of x-axis coordinates
	 */
	public List< Double > getXVals() {
		return x;
	}

	/**
	 * @return a list of y-axis coordinates
	 */
	public List< Double > getYVals() {
		return y;
	}

	/**
	 * Normalizes this function to that the sum of all give y-values sums to 1.
	 */
	public void normalizeToDist() {
		double sum = 0.0;
		for ( final double d : y ) {
			sum += d;
		}
		for ( int i = 0; i < y.size(); i++ ) {
			y.set( i, y.get( i ) / sum );
		}
	}

	/**
	 * Normalizes this function so that the maximal value is going to be 1.
	 */
	public void normalizeMax() {
		double max = Double.MIN_VALUE;
		for ( final double d : y ) {
			if ( d > max ) max = d;
		}
		for ( int i = 0; i < y.size(); i++ ) {
			y.set( i, y.get( i ) / max );
		}
	}

}
