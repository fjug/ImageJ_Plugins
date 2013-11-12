/**
 *
 */
package com.jug.util;

import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.DoubleType;

import com.jug.data.Point2D;
import com.jug.data.PointCloud2D;

/**
 * @author jug
 */
public class DumpImgFactory {

	/**
	 * Creates an Img<double> from a List of Lists of Pointcloud2D objects.
	 *
	 * @param clouds
	 */
	public static Img< DoubleType > dumpPointClouds( final List< List< PointCloud2D< Double >>> clouds, final boolean interpolated ) {
		double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
		int sizeZ = 0;

		for ( final List< PointCloud2D< Double >> frame : clouds ) {
			sizeZ = Math.max( sizeZ, frame.size() );
			for ( final PointCloud2D< Double > slice : frame ) {
				for ( final Point2D<Double> point : slice.getPoints() ) {
					minX = Math.min( minX, point.getX() );
					maxX = Math.max( maxX, point.getX() );
					minY = Math.min( minY, point.getY() );
					maxY = Math.max( maxY, point.getY() );
				}
			}
		}

		final int sizeX = ( int ) ( maxX - minX + 1 );
		final int sizeY = ( int ) ( maxY - minY + 1 );

		final Img< DoubleType > ret = new ArrayImgFactory< DoubleType >().create( new long[] { sizeX, sizeY, 1, sizeZ, clouds.size() }, new DoubleType() );

		final RandomAccess< DoubleType > ra = ret.randomAccess();
		int t = 0;
		for ( final List< PointCloud2D< Double >> frame : clouds ) {
			int z = 0;
			for ( final PointCloud2D< Double > slice : frame ) {
				for ( final Point2D< Double > point : slice.getPoints() ) {
					final int x = ( int ) ( point.getX() - minX );
					final int y = ( int ) ( point.getY() - minY );

					final double fracX = 1 - ( point.getX() - Math.floor( point.getX() ) );
					final double fracY = 1 - ( point.getY() - Math.floor( point.getY() ) );

					if ( !interpolated ) {
						ra.setPosition( new long[] { x, y, 1, z, t } );
						ra.get().add( new DoubleType( point.getData().doubleValue() ) );
					} else {
						ra.setPosition( new long[] { x, y, 1, z, t } );
						ra.get().add( new DoubleType( point.getData().doubleValue() * fracX * fracY ) );

						ra.setPosition( new long[] { x + 1, y, 1, z, t } );
						ra.get().add( new DoubleType( point.getData().doubleValue() * ( 1 - fracX ) * fracY ) );

						ra.setPosition( new long[] { x, y + 1, 1, z, t } );
						ra.get().add( new DoubleType( point.getData().doubleValue() * fracX * ( 1 - fracY ) ) );

						ra.setPosition( new long[] { x + 1, y + 1, 1, z, t } );
						ra.get().add( new DoubleType( point.getData().doubleValue() * ( 1 - fracX ) * ( 1 - fracY ) ) );
					}
				}
				z++;
			}
			t++;
		}

		return ret;
	}

}
