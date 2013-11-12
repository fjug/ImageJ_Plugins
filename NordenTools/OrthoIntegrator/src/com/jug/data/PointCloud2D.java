/**
 *
 */
package com.jug.data;

import java.util.ArrayList;
import java.util.List;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;


/**
 * @author jug
 */
public class PointCloud2D< T > {

	private final List< Point2D< T > > points;

	public PointCloud2D() {
		points = new ArrayList< Point2D< T > >();
	}

	public void addPoint( final double x, final double y, final T weight ) {
		final Point2D< T > p = new Point2D< T >( x, y, weight );
		addPoint( p );
	}

	/**
	 * @param p
	 * @param weight
	 */
	private void addPoint( final Point2D< T > p ) {
		getPoints().add( p );
	}

	/**
	 * @return
	 */
	public Ellipse2D getEllipticalApproximation() {
		// determine center
		final Vector2D center = new Vector2D( this.getXVals().getExpectedValue(), this.getYVals().getExpectedValue() );

		// determine first two principle components
		final Matrix covM = this.getCovarianceMatrix();
		final EigenvalueDecomposition eigen = covM.eig();
		final Matrix V = eigen.getV();
		final Matrix D = eigen.getD();

		final Vector2D firstEV = new Vector2D( V.get( 0, 0 ), V.get( 1, 0 ) );

		return new Ellipse2D( center, firstEV.getAngle(), D.get( 0, 0 ), D.get( 1, 1 ) );
	}

	/**
	 * @return
	 */
	private Matrix getCovarianceMatrix() {
		final PointCloud1D xPoints = getXVals();
		final PointCloud1D yPoints = getYVals();
		return xPoints.getCovarianceMatrix( yPoints );
	}

	/**
	 * @return
	 */
	private PointCloud1D getYVals() {
		final PointCloud1D yVals = new PointCloud1D();
		for ( final Point2D< T > point : this.getPoints() ) {
			yVals.add( point.getY() );
		}
		return yVals;
	}

	/**
	 * @return
	 */
	private PointCloud1D getXVals() {
		final PointCloud1D xVals = new PointCloud1D();
		for ( final Point2D< T > point : this.getPoints() ) {
			xVals.add( point.getX() );
		}
		return xVals;
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return getPoints().isEmpty();
	}

	/**
	 * @return the points
	 */
	public List< Point2D< T > > getPoints() {
		return points;
	}
}
