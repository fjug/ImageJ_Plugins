/**
 *
 */
package com.jug.data;


/**
 * @author jug
 */
public class Vector2D {

	private double[] vector;

	/**
	 * Creates a 2D null vector.
	 */
	public Vector2D() {
		this( 0, 0 );
	}

	/**
	 * Creates a 2D vector.
	 *
	 * @param x
	 *            extend in x-dimension.
	 * @param y
	 *            extend in y-dimension.
	 */
	public Vector2D( final double x, final double y ) {
		this.vector = new double[] { x, y };
	}

	/**
	 * @return the vector
	 */
	public double[] getVector() {
		return vector;
	}

	/**
	 * @param vector
	 *            the vector to set
	 */
	public void setVector( final double x, final double y ) {
		this.vector = new double[] { x, y };
	}

	/**
	 * @return
	 */
	public double getAngle() {
		return Math.atan2( vector[ 1 ], vector[ 0 ] );
	}

	/**
	 * @return
	 */
	public double getX() {
		return vector[ 0 ];
	}

	/**
	 * @return
	 */
	public double getY() {
		return vector[ 1 ];
	}
}
