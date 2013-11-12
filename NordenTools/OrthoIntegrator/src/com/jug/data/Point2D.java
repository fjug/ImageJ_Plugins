/**
 *
 */
package com.jug.data;


/**
 * @author jug
 */
public class Point2D< T > {

	private double x;
	private double y;
	private T data;


	public Point2D( final double x, final double y, final T data ) {
		this.x = x;
		this.y = y;
		this.data = data;
	}


	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX( final double x ) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY( final double y ) {
		this.y = y;
	}

	/**
	 * @return the data
	 */
	public T getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData( final T data ) {
		this.data = data;
	}
}
