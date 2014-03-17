/**
 *
 */
package com.jug.segmentation;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.paramaxflow.Parametric;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import com.jug.fkt.Function1D;

/**
 * @author jug
 */
public class ParaMaxFlow< T extends RealType< T > > {

	Parametric parametric;

	private final RandomAccessibleInterval< T > rai;
	private final RandomAccessibleInterval< ? extends RealType > probMap;
	private Img< LongType > regionsImg;

	private final boolean is3d;

	private final Function1D< Double > fktUnary;
	private final double costIsing;
	private final Function1D< Double > fktPairwiseX;
	private final Function1D< Double > fktPairwiseY;
	private final Function1D< Double > fktPairwiseZ;

	/**
	 * Instatiates an instance of <code>Parametric</code>, and builds a graph
	 * structure corresponding to the given image.
	 *
	 * @param rai
	 *            the image for which the graph in <code>Parametric</code> will
	 *            be built. This <code>Img</code> must be 1d, 2d, or 3d.
	 * @param probMap
	 *            if given this probability map must have the same
	 *            dimensionality as <code>img</code>. All elements must be in
	 *            [0,1] and will be used to modify the cost for unary and binary
	 *            potentials.
	 * @param is3d
	 *            if true, the given image will be treated as being 3d,
	 *            otherwise as a sequence of 2d images. In case <code>img</code>
	 *            is 1d or 2d, this parameter can be ignored.
	 * @param fktUnary
	 * @param costIsing
	 * @param fktPairwiseX
	 * @param fktPairwiseY
	 * @param fktPairwiseZ
	 */
	public ParaMaxFlow( final RandomAccessibleInterval< T > rai, final RandomAccessibleInterval< ? extends RealType > probMap, final boolean is3d,
						final Function1D< Double > fktUnary, final double costIsing,
						final Function1D< Double > fktPairwiseX, final Function1D< Double > fktPairwiseY, final Function1D< Double > fktPairwiseZ ) {
		if ( rai.numDimensions() > 3 ) { throw new UnsupportedOperationException( "ParaMaxFlow does not yet support Img's with >3 dimensions! Sorry..." ); }
		if ( Views.iterable( rai ).size() * rai.numDimensions() > Integer.MAX_VALUE ) { throw new UnsupportedOperationException( "ParaMaxFlow can only operate on Img's with <Integer.MAX_VALUE many binary potentials!" ); }

		this.rai = Views.zeroMin( rai );
		if ( probMap == null ) {
			this.probMap = null;
		} else {
			this.probMap = Views.zeroMin( probMap );
		}
		this.regionsImg = null;

		this.is3d = is3d;

		this.fktUnary = fktUnary;
		this.costIsing = costIsing;
		this.fktPairwiseX = fktPairwiseX;
		this.fktPairwiseY = fktPairwiseY;
		this.fktPairwiseZ = fktPairwiseZ;

		parametric = new Parametric( ( int ) Views.iterable( rai ).size(), ( int ) ( Views.iterable( rai ).size() * rai.numDimensions() ) );
		buildGraph();
	}

	/**
	 * @param rai
	 */
	private void buildGraph() {

		parametric.AddNode( ( int ) Views.iterable( rai ).size() ); // add as many nodes as the input image has pixels

		//her now a trick to make <3d images also comply to the code below
		IntervalView< T > ivImg = Views.interval( rai, rai );
		IntervalView< ? extends RealType > ivProbMap = ( probMap == null ) ? null : Views.interval( probMap, probMap );

		//make everything appear with 3 dimensions even if it is initially not
		for ( int i = 0; i < 3 - rai.numDimensions(); i++ ) {
			ivImg = Views.addDimension( ivImg, 0, 0 );
		}
		for ( int i = 0; probMap != null && i < 3 - probMap.numDimensions(); i++ ) {
			ivProbMap = Views.addDimension( ivProbMap, 0, 0 );
		}

		final long[] dims = new long[ 3 ];
		ivImg.dimensions( dims );

		final RandomAccess< T > raImg = ivImg.randomAccess();
		final RandomAccess< ? extends RealType > raProbMap = ( ivProbMap == null ) ? null : ivProbMap.randomAccess();

		int pixelId = 0;
		final float eps = 0.0000001f;

		// for each pixel in input image --> create unary term
		for ( long z = 0; z < dims[ 2 ]; z++ ) {
			for ( long y = 0; y < dims[ 1 ]; y++ ) {
				for ( long x = 0; x < dims[ 0 ]; x++ ) {
					raImg.setPosition( new long[] { x, y, z } );
					if ( raProbMap != null ) {
						raProbMap.setPosition( new long[] { x, y, z } );
					}

					final double intensity = raImg.get().getRealDouble();
					double likelihood = fktUnary.evaluate( intensity ).doubleValue();
					if ( raProbMap != null ) {
						likelihood *= ( 1.0 - raProbMap.get().getRealDouble() );
					}

					pixelId = ( int ) ( z * dims[ 1 ] * dims[ 0 ] + y * dims[ 0 ] + x );
					parametric.AddUnaryTerm( pixelId, 1.0, likelihood );
				}
			}
	    }

		// for each pixel in input image --> create pairwise terms towards right (x), down (y) and back (z)
		raImg.setPosition( new long[] { 0, 0, 0 } );	// do I need this?
		if ( raProbMap != null ) {
			raProbMap.setPosition( new long[] { 0, 0, 0 } );// do I need this?
		}
		for ( long z = 0; z < dims[ 2 ]; z++ ) {
			for ( long y = 0; y < dims[ 1 ]; y++ ) {
				for ( long x = 0; x < dims[ 0 ]; x++ ) {
					raImg.setPosition( new long[] { x, y, z } );
					if ( raProbMap != null ) {
						raProbMap.setPosition( new long[] { x, y, z } );
					}

					final double intensity = raImg.get().getRealDouble();
					pixelId = ( int ) ( z * dims[ 0 ] * dims[ 1 ] + y * dims[ 0 ] + x );

					if ( x+1 < dims[0] ) {
						raImg.move( 1,0 );
						final double intensity_next = raImg.get().getRealDouble();
						raImg.move( -1,0 );

						double diff = Math.abs( intensity - intensity_next );
						if ( diff < eps ) diff = eps;

						double cost = costIsing + fktPairwiseX.evaluate( diff ).doubleValue(); // k2*Ising + k3*Edge
						if ( raProbMap != null ) {
							cost *= ( 1.0 - raProbMap.get().getRealDouble() );
						}

						final long xNeighborId = pixelId + 1;
						parametric.AddPairwiseTerm( pixelId, xNeighborId, 0.0, cost, cost, 0.0); // add term with costs E00, E01, E10, and E11,
					}
					if ( y+1 < dims[1] ) {
						raImg.move( 1,1 );
						final double intensity_next = raImg.get().getRealDouble();
						raImg.move( -1,1 );

						double diff = Math.abs( intensity - intensity_next );
						if ( diff < eps ) diff = eps;

						double cost = costIsing + fktPairwiseY.evaluate( diff ).doubleValue(); // k2*Ising + k3*Edge
						if ( raProbMap != null ) {
							cost *= ( 1.0 - raProbMap.get().getRealDouble() );
						}

						final long yNeighborId = pixelId + dims[ 0 ];
						parametric.AddPairwiseTerm( pixelId, yNeighborId, 0.0, cost, cost, 0.0); // add term with costs E00, E01, E10, and E11,
					}
					// connect in z-direction ONLY if is3d==TRUE!!!
					if ( is3d && z+1 < dims[2] ) {
						raImg.move( 1, 2 );
						final double intensity_next = raImg.get().getRealDouble();
						raImg.move( -1, 2 );

						double diff = Math.abs( intensity - intensity_next );
						if ( diff < eps ) diff = eps;

						double cost = costIsing + fktPairwiseZ.evaluate( diff ).doubleValue(); // k2*Ising + k3*Edge
						if ( raProbMap != null ) {
							cost *= ( 1.0 - raProbMap.get().getRealDouble() );
						}

						final long zNeighborId = pixelId + dims[ 0 ] * dims[ 1 ];
						parametric.AddPairwiseTerm( pixelId, zNeighborId, 0.0, cost, cost, 0.0 ); // add term with costs E00, E01, E10, and E11,
					}
				}
			}
	    }
	}

	public long solve( final double lambdaMin, final double lambdaMax ) {
		final long solutions = parametric.Solve( lambdaMin, lambdaMax );
		System.out.println( " >>>>> ParaMaxFlow solutions found: " + solutions + " <<<<<" );
		regionsImg = createRegionsImg();
		return solutions;
	}

	private Img< LongType > createRegionsImg() {
		long[] dims = new long[ rai.numDimensions() ];
		rai.dimensions( dims );
		final ImgFactory< LongType > imgFactory = new ArrayImgFactory< LongType >();
		final Img< LongType > ret = imgFactory.create( dims, new LongType() );

		//here now a trick to make <3d images also comply to the code below
		IntervalView< LongType > ivRet = Views.interval( ret, ret );
		for ( int i = 0; i < 3 - rai.numDimensions(); i++ ) {
			ivRet = Views.addDimension( ivRet, 0, 0 );
		}
		final RandomAccess< LongType > raRet = ivRet.randomAccess();

		dims = new long[ ivRet.numDimensions() ];
		ivRet.dimensions( dims );

		for ( long graphNodeId = 0; graphNodeId < Views.iterable( rai ).size(); graphNodeId++ ) {
			final long numRegions = parametric.GetRegionCount( parametric.GetRegion( graphNodeId ) );

			final long z = graphNodeId / ( dims[ 0 ] * dims[ 1 ] );
			final long remainder = graphNodeId - z * ( dims[ 0 ] * dims[ 1 ] );
			final long y = remainder / dims[ 0 ];
			final long x = remainder - y * dims[ 0 ];

			raRet.setPosition( new long[] { x, y, z } );
			raRet.get().set( numRegions );
		}

		return ret;
	}

	public Img< LongType > getRegionsImg() {
		return regionsImg;
	}

	public Img< BitType > getSolution( final long solutionId ) {
		final long[] dims = new long[ rai.numDimensions() ];
		rai.dimensions( dims );
		final ImgFactory< BitType > imgFactory = new ArrayImgFactory< BitType >();
		final Img< BitType > ret = imgFactory.create( dims, new BitType() );

		//here now a trick to make <3d images also comply to the code below
		IntervalView< BitType > ivRet = Views.interval( ret, ret );
		for ( int i = 0; i < 3 - rai.numDimensions(); i++ ) {
			ivRet = Views.addDimension( ivRet, 0, 0 );
		}
		final RandomAccess< BitType > raRet = ivRet.randomAccess();

		for ( long graphNodeId = 0; graphNodeId < Views.iterable( rai ).size(); graphNodeId++ ) {
			final long numRegions = parametric.GetRegionCount( parametric.GetRegion( graphNodeId ) );

			final long z = graphNodeId / ( dims[ 0 ] * dims[ 1 ] );
			final long remainder = graphNodeId - z * ( dims[ 0 ] * dims[ 1 ] );
			final long y = remainder / dims[ 0 ];
			final long x = remainder - y * dims[ 0 ];

			raRet.setPosition( new long[] { x, y, z } );
			raRet.get().set( ( numRegions < solutionId ) );
		}

		return ret;
	}

	// TODO extract duplicate code
	public Img< DoubleType > getUnariesImg() {
		long[] dims = new long[ rai.numDimensions() ];
		rai.dimensions( dims );
		final ImgFactory< DoubleType > imgFactory = new ArrayImgFactory< DoubleType >();
		final Img< DoubleType > ret = imgFactory.create( dims, new DoubleType() );

		//here now a trick to make <3d images also comply to the code below
		IntervalView< DoubleType > ivRet = Views.interval( ret, ret );
		for ( int i = 0; i < 3 - rai.numDimensions(); i++ ) {
			ivRet = Views.addDimension( ivRet, 0, 0 );
		}
		final RandomAccess< DoubleType > raRet = ivRet.randomAccess();

		dims = new long[ ivRet.numDimensions() ];
		ivRet.dimensions( dims );

		final IntervalView< T > ivImg = Views.interval( rai, rai );
		final RandomAccess< T > raImg = ivImg.randomAccess();
		final IntervalView< ? extends RealType > ivProbMap = ( probMap == null ) ? null : Views.interval( probMap, probMap );
		final RandomAccess< ? extends RealType > raProbMap = ( ivProbMap == null ) ? null : ivProbMap.randomAccess();

		for ( long graphNodeId = 0; graphNodeId < Views.iterable( rai ).size(); graphNodeId++ ) {

			final long z = graphNodeId / ( dims[ 0 ] * dims[ 1 ] );
			final long remainder = graphNodeId - z * ( dims[ 0 ] * dims[ 1 ] );
			final long y = remainder / dims[ 0 ];
			final long x = remainder - y * dims[ 0 ];

			raImg.setPosition( new long[] { x, y, z } );
			if ( raProbMap != null ) {
				raProbMap.setPosition( new long[] { x, y, z } );
			}

			final double intensity = raImg.get().getRealDouble();
			double likelihood = fktUnary.evaluate( intensity ).doubleValue();
			if ( raProbMap != null ) {
				likelihood *= ( 1.0 - raProbMap.get().getRealDouble() );
			}

			raRet.setPosition( new long[] { x, y, z } );
			raRet.get().set( likelihood );
		}

		return ret;
	}

	// TODO extract duplicate code
	public Img< DoubleType > getBinariesInXImg() {
		long[] dims = new long[ rai.numDimensions() ];
		rai.dimensions( dims );
		final ImgFactory< DoubleType > imgFactory = new ArrayImgFactory< DoubleType >();
		final Img< DoubleType > ret = imgFactory.create( dims, new DoubleType() );

		//here now a trick to make <3d images also comply to the code below
		IntervalView< DoubleType > ivRet = Views.interval( ret, ret );
		for ( int i = 0; i < 3 - rai.numDimensions(); i++ ) {
			ivRet = Views.addDimension( ivRet, 0, 0 );
		}
		final RandomAccess< DoubleType > raRet = ivRet.randomAccess();

		final float eps = 0.0000001f;

		dims = new long[ ivRet.numDimensions() ];
		ivRet.dimensions( dims );

		final IntervalView< T > ivImg = Views.interval( rai, rai );
		final RandomAccess< T > raImg = ivImg.randomAccess();
		final IntervalView< ? extends RealType > ivProbMap = ( probMap == null ) ? null : Views.interval( probMap, probMap );
		final RandomAccess< ? extends RealType > raProbMap = ( ivProbMap == null ) ? null : ivProbMap.randomAccess();

		for ( long graphNodeId = 0; graphNodeId < Views.iterable( rai ).size(); graphNodeId++ ) {

			final long z = graphNodeId / ( dims[ 0 ] * dims[ 1 ] );
			final long remainder = graphNodeId - z * ( dims[ 0 ] * dims[ 1 ] );
			final long y = remainder / dims[ 0 ];
			final long x = remainder - y * dims[ 0 ];

			raImg.setPosition( new long[] { x, y, z } );
			if ( raProbMap != null ) {
				raProbMap.setPosition( new long[] { x, y, z } );
			}

			final double intensity = raImg.get().getRealDouble();
			if ( x + 1 < dims[ 0 ] ) {
				raImg.move( 1, 0 );
				final double intensity_next = raImg.get().getRealDouble();
				raImg.move( -1, 0 );

				double diff = Math.abs( intensity - intensity_next );
				if ( diff < eps ) diff = eps;

				double cost = costIsing + fktPairwiseX.evaluate( diff ).doubleValue(); // k2*Ising + k3*Edge
				if ( raProbMap != null ) {
					cost *= ( 1.0 - raProbMap.get().getRealDouble() );
				}

				raRet.setPosition( new long[] { x, y, z } );
				raRet.get().set( cost );
			}
		}

		return ret;
	}

	// TODO extract duplicate code
	public Img< DoubleType > getBinariesInYImg() {
		long[] dims = new long[ rai.numDimensions() ];
		rai.dimensions( dims );
		final ImgFactory< DoubleType > imgFactory = new ArrayImgFactory< DoubleType >();
		final Img< DoubleType > ret = imgFactory.create( dims, new DoubleType() );

		//here now a trick to make <3d images also comply to the code below
		IntervalView< DoubleType > ivRet = Views.interval( ret, ret );
		for ( int i = 0; i < 3 - rai.numDimensions(); i++ ) {
			ivRet = Views.addDimension( ivRet, 0, 0 );
		}
		final RandomAccess< DoubleType > raRet = ivRet.randomAccess();

		final float eps = 0.0000001f;

		dims = new long[ ivRet.numDimensions() ];
		ivRet.dimensions( dims );

		final IntervalView< T > ivImg = Views.interval( rai, rai );
		final RandomAccess< T > raImg = ivImg.randomAccess();
		final IntervalView< ? extends RealType > ivProbMap = ( probMap == null ) ? null : Views.interval( probMap, probMap );
		final RandomAccess< ? extends RealType > raProbMap = ( ivProbMap == null ) ? null : ivProbMap.randomAccess();

		for ( long graphNodeId = 0; graphNodeId < Views.iterable( rai ).size(); graphNodeId++ ) {

			final long z = graphNodeId / ( dims[ 0 ] * dims[ 1 ] );
			final long remainder = graphNodeId - z * ( dims[ 0 ] * dims[ 1 ] );
			final long y = remainder / dims[ 0 ];
			final long x = remainder - y * dims[ 0 ];

			raImg.setPosition( new long[] { x, y, z } );
			if ( raProbMap != null ) {
				raProbMap.setPosition( new long[] { x, y, z } );
			}

			final double intensity = raImg.get().getRealDouble();
			if ( y+1 < dims[1] ) {
				raImg.move( 1,1 );
				final double intensity_next = raImg.get().getRealDouble();
				raImg.move( -1,1 );

				double diff = Math.abs( intensity - intensity_next );
				if ( diff < eps ) diff = eps;

				double cost = costIsing + fktPairwiseY.evaluate( diff ).doubleValue(); // k2*Ising + k3*Edge
				if ( raProbMap != null ) {
					cost *= ( 1.0 - raProbMap.get().getRealDouble() );
				}

				raRet.setPosition( new long[] { x, y, z } );
				raRet.get().set( cost );
			}
		}

		return ret;
	}
}
