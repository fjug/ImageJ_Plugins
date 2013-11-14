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

	private final List< Pixel2D< T > > points;

	public PointCloud2D() {
		points = new ArrayList< Pixel2D< T > >();
	}

	public void addPoint( final double x, final double y, final T weight ) {
		final Pixel2D< T > p = new Pixel2D< T >( x, y, weight );
		addPoint( p );
	}

	/**
	 * @param p
	 * @param weight
	 */
	private void addPoint( final Pixel2D< T > p ) {
		getPoints().add( p );
	}

	/**
	 * @return
	 */
	public Ellipse2D getEllipticalApproximation() {
		final Ellipse2D ret;

		if (this.size() == 1) {
			ret = new Ellipse2D( this.getPoints().get( 0 ), 0.0, 0.0, 0.0 );
		} else
		if (this.size() == 2) {
			final Point2D p = new Point2D( this.getPoints().get( 0 ) );
			p.add( this.getPoints().get( 1 ) );
			p.multiply( 0.5 );
			final double len = Point2D.distance( p, this.getPoints().get( 1 ) );
			ret = new Ellipse2D( p, Math.atan2( this.getPoints().get( 1 ).getY() - p.getY(), this.getPoints().get( 1 ).getX() - p.getX() ), len, 0.0 );
		} else {
			// determine center
			final Point2D center = new Point2D( this.getXVals().getExpectedValue(), this.getYVals().getExpectedValue() );

			// determine first two principle components
			final Matrix covM = this.getCovarianceMatrix();
			final EigenvalueDecomposition eigen = covM.eig();
			final Matrix V = eigen.getV();
//			final Matrix D = eigen.getD();
			final double[] evs = eigen.getRealEigenvalues();

			final Vector2D firstEV = new Vector2D( V.get( 0, 0 ), V.get( 1, 0 ) );

			final double a = evs[ 0 ] / 2;
			final double b = evs[ 1 ] / 2;
//			if ( a > b ) {
//				final double c = a;
//				a = b;
//				b = c;
//			}
			ret = new Ellipse2D( center, firstEV.getAngle(), a, b ); //D.get( 0, 0 ), D.get( 1, 1 ) );
//			ret.scaleToHostFractionOfPoints( this, .1 );
		}
		return ret;
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
		for ( final Pixel2D< T > point : this.getPoints() ) {
			yVals.add( point.getY() );
		}
		return yVals;
	}

	/**
	 * @return
	 */
	private PointCloud1D getXVals() {
		final PointCloud1D xVals = new PointCloud1D();
		for ( final Pixel2D< T > point : this.getPoints() ) {
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
	public List< Pixel2D< T > > getPoints() {
		return points;
	}

	/**
	 * @return number of points contained in this cloud.
	 */
	public int size() {
		return points.size();
	}
}
