/**
 *
 */
package com.jug.data;


/**
 * @author jug
 */
public class Ellipse2D {

	private Vector2D center;
	private double angle;
	private double a;
	private double b;

	/**
	 * Creates an non-rotated Ellipse2D at (0,0) with a and be being set to 0.
	 */
	public Ellipse2D() {
		this( new Vector2D(), 0.0, 0.0, 0.0 );
	}

	/**
	 * Creates an Ellipse2D.
	 *
	 * @param offset
	 *            a vector pointing from the origin to the center of the
	 *            ellipse.
	 * @param angle
	 *            counter-clockwise rotation fo the ellipse -- given in rad!
	 * @param a
	 *            length of major axis.
	 * @param b
	 *            length of minor axis.
	 */
	public Ellipse2D( final Vector2D offset, final double angle, final double a, final double b ) {
		this.setCenter( offset );
		this.setAngle( angle );
		this.setA( a );
		this.setB( b );
	}

	/**
	 * @return the center
	 */
	public Vector2D getCenter() {
		return center;
	}

	/**
	 * @param center
	 *            the center to set
	 */
	public void setCenter( final Vector2D center ) {
		this.center = center;
	}

	/**
	 * @return the angle
	 */
	public double getAngle() {
		return angle;
	}

	/**
	 * @param angle
	 *            the angle to set
	 */
	public void setAngle( final double angle ) {
		this.angle = angle;
	}

	/**
	 * @return the a
	 */
	public double getA() {
		return a;
	}

	/**
	 * @param a
	 *            the a to set
	 */
	public void setA( final double a ) {
		this.a = a;
	}

	/**
	 * @return the b
	 */
	public double getB() {
		return b;
	}

	/**
	 * @param b
	 *            the b to set
	 */
	public void setB( final double b ) {
		this.b = b;
	}
}
