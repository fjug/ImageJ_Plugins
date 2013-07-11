/**
 *
 */
package com.jug.ij.panels;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import com.jug.util.DataMover;


/**
 * @author jug
 */
public class InterpolatedCropPanel extends JPanel implements ActionListener {

	/**
	 * Serialization ID
	 */
	private static final long serialVersionUID = -8040344519142406774L;

	private final Frame frame;
	private final ImagePlus imgPlus;

	private JTable tableROIs;
	private Vector< String > tableColumnNames;
	private DefaultTableModel tmROIs;
	private Vector< Vector< String > > tableData;

	private JButton bAdd;
	private JButton bDel;
	private JButton bShowPrevious;
	private JButton bShowNext;
	private JButton bCrop;

	/**
	 * @param imgPlus
	 */
	public InterpolatedCropPanel( final Frame frame, final ImagePlus imgPlus ) {
		super( new BorderLayout( 5, 5 ) );
		setBorder( BorderFactory.createEmptyBorder( 10, 15, 5, 15 ) );
		this.frame = frame;
		this.imgPlus = imgPlus;
		buildGui();
		frame.setSize( 500, 350 );
	}

	private void buildGui() {
		final JPanel pContent = new JPanel( new BorderLayout() );
		final JPanel pControls = new JPanel();
		pControls.setLayout( new BoxLayout( pControls, BoxLayout.LINE_AXIS ) );

		final JTextArea textIntro = new JTextArea( "" +
				"Step 1: Rough Cell Crop\n" +
				"-----------------------\n" +
				"a) go to the first frame your cell is visible enough\n" +
				"b) use any IJ selection tool to select the cell of interest (with some margin)\n" +
				"c) hit the button below\n" +
				"d) go through frames and repeat b+c at the last frame of interest" );
		textIntro.setBackground( new JButton().getBackground() );
		textIntro.setEditable( false );
		textIntro.setBorder( BorderFactory.createEmptyBorder( 0, 2, 5, 2 ) );

		bAdd = new JButton( "+" );
		bAdd.addActionListener( this );
		bDel = new JButton( "-" );
		bDel.addActionListener( this );

		bShowPrevious = new JButton( "<" );
		bShowPrevious.addActionListener( this );
		bShowNext = new JButton( ">" );
		bShowNext.addActionListener( this );

		bCrop = new JButton( "Crop & Go" );
		bCrop.addActionListener( (ActionListener) frame );

		tableColumnNames = new Vector< String >();
		tableColumnNames.add( "frame" );
		tableColumnNames.add( "x" );
		tableColumnNames.add( "y" );
		tableColumnNames.add( "width" );
		tableColumnNames.add( "height" );
		tableData = new Vector< Vector< String > >();
		tmROIs = new DefaultTableModel( tableData, tableColumnNames );
		tableROIs = new JTable( tmROIs ) {

			private static final long serialVersionUID = -5757310501730411649L;
			DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer();

			{ //initializer block
				renderRight.setHorizontalAlignment( SwingConstants.RIGHT );
			}

			@Override
			public TableCellRenderer getCellRenderer( final int arg0, final int arg1 ) {
				return renderRight;

			}

			@Override
			public boolean isCellEditable( final int rowIndex, final int colIndex ) {
				return false; // disable edit for ALL cells
			}

			@Override
			public void changeSelection( final int rowIndex, final int columnIndex, final boolean toggle, final boolean extend ) {
				try {
					showFrameAndRoiFromTableData( rowIndex );
				}
				catch ( final Exception e ) {}
				// make the selection change
				super.changeSelection( rowIndex, columnIndex, toggle, extend );
			}
		};

		add( textIntro, BorderLayout.NORTH );
		pContent.add( tableROIs.getTableHeader(), BorderLayout.NORTH );
		pContent.add( tableROIs, BorderLayout.CENTER );
		final JScrollPane scrollPane = new JScrollPane( tableROIs );
		add( scrollPane, BorderLayout.CENTER );
		pControls.add( bAdd );
		pControls.add( bDel );
		pControls.add( Box.createHorizontalGlue() );
		pControls.add( bShowPrevious );
		pControls.add( bShowNext );
		pControls.add( Box.createHorizontalGlue() );
		pControls.add( bCrop );
		add( pControls, BorderLayout.SOUTH );
	}

	/**
	 * @param rowIndex
	 *            index of the row in <code>tableData</code> that should be
	 *            highlighted in <code>imgPlus</code>.
	 */
	protected void showFrameAndRoiFromTableData( final int rowIndex ) {
		final Vector< String > row = tableData.get( rowIndex );
		final int frameNum = Integer.parseInt( row.get( 0 ) );
		final int x = Integer.parseInt( row.get( 1 ) );
		final int y = Integer.parseInt( row.get( 2 ) );
		final int w = Integer.parseInt( row.get( 3 ) );
		final int h = Integer.parseInt( row.get( 4 ) );
		imgPlus.setPosition( imgPlus.getC(), imgPlus.getSlice(), frameNum );
		imgPlus.setRoi( new Rectangle( x, y, w, h ) );
	}

	/**
	 * Shows the frame with the given <code>frameIndex</code>, in case it
	 * exists, and
	 * adds a rectangular ROI at the location given in the table (or at a linear
	 * interpolated
	 * place in case the given <code>frameIndex</code> is between two table
	 * rows.
	 *
	 * @param channelIndex
	 *            index of the channel to be selected.
	 * @param timeIndex
	 *            index of the time to be shown in <code>imgPlus</code>.
	 */
	protected void showRoiOnFrame( final int channelIndex, final int timeIndex ) {
		final int rowIndex = getTableEntryForFrame( timeIndex );
		if ( rowIndex >= 0 ) {
			// No interpolation needed
			showFrameAndRoiFromTableData( rowIndex );
		} else {
			// Interpolation!!!
			imgPlus.setPosition( channelIndex, imgPlus.getSlice(), timeIndex );
			final int[][] interpolationData = getInterpolationData( timeIndex );
			if ( interpolationData[ 0 ][ 0 ] != -1 && interpolationData[ 1 ][ 0 ] != -1 ) {
				final int[] ret = getInterpolatedROI( timeIndex );
				imgPlus.setRoi( new Rectangle( ret[0], ret[1], ret[2], ret[3] ) );
			} else {
				imgPlus.deleteRoi();
				System.out.println( "This frame is NOT within current crop-bounds!" );
			}
		}
	}

	/**
	 * Computes and returns the interpolated ROI at a given time.
	 *
	 * @param timeIndex
	 *            the time
	 * @return an 1d int-array containing (x,y,w,h), being the interpolated ROI
	 *         for the requested time-point.
	 */
	private int[] getInterpolatedROI( final int timeIndex ) {
		final int[][] interpolationData = getInterpolationData( timeIndex );
		final int[] ret = new int[ 4 ];
		if ( interpolationData[ 0 ][ 0 ] != -1 && interpolationData[ 1 ][ 0 ] != -1 ) {
			final float interpolationFraction = ( ( float ) ( timeIndex - interpolationData[ 0 ][ 0 ] ) ) / ( interpolationData[ 1 ][ 0 ] - interpolationData[ 0 ][ 0 ] );
			ret[ 0 ] = interpolationData[ 0 ][ 1 ] + Math.round( ( interpolationData[ 1 ][ 1 ] - interpolationData[ 0 ][ 1 ] ) * interpolationFraction );
			ret[ 1 ] = interpolationData[ 0 ][ 2 ] + Math.round( ( interpolationData[ 1 ][ 2 ] - interpolationData[ 0 ][ 2 ] ) * interpolationFraction );
			ret[ 2 ] = interpolationData[ 0 ][ 3 ] + Math.round( ( interpolationData[ 1 ][ 3 ] - interpolationData[ 0 ][ 3 ] ) * interpolationFraction );
			ret[ 3 ] = interpolationData[ 0 ][ 4 ] + Math.round( ( interpolationData[ 1 ][ 4 ] - interpolationData[ 0 ][ 4 ] ) * interpolationFraction );
		} else {
			ret[ 0 ] = 0;
			ret[ 1 ] = 0;
			ret[ 2 ] = imgPlus.getWidth();
			ret[ 3 ] = imgPlus.getHeight();
		}
		return ret;
	}

	/**
	 * @param timeIndex
	 * @return
	 */
	private int getTableEntryForFrame( final int timeIndex ) {
		int i = 0;
		for ( final Vector< String > row : tableData ) {
			final int rowEntry = Integer.parseInt( row.get( 0 ) );
			if ( timeIndex == rowEntry ) { return i; }
			if ( rowEntry > timeIndex ) { break; }
			i++;
		}
		return -1;
	}

	/**
	 * Returns the to table entries embracing the given time point.
	 *
	 * @param timeIndex
	 *            the time point to get the data for
	 * @return a two dimensional int array. Demension one is of length 2,
	 *         containing the roi-data (x,z,w,h) of the adjacent earlier table
	 *         entry at 0 and the roi-data of the adjacent later table entry at
	 *         1. If one or both such entries do not exist the array contains
	 *         -1's.
	 */
	private int[][] getInterpolationData( final int timeIndex ) {
		final int[][] ret = { { -1, -1, -1, -1, -1 }, { -1, -1, -1, -1, -1 } };

		int i = 0;
		for (; i<tableData.size(); i++ ) {
			final int frame = Integer.parseInt( tableData.get( i ).get( 0 ) );
			if ( frame < timeIndex) {
				ret[0][0] = frame;
				for (int j=0; j<5; j++) {
					ret[ 0 ][ j ] = Integer.parseInt( tableData.get( i ).get( j ) );
				}
			}
			if ( frame >= timeIndex ) {
				ret[ 1 ][ 0 ] = frame;
				for ( int j = 0; j < 5; j++ ) {
					ret[ 1 ][ j ] = Integer.parseInt( tableData.get( i ).get( j ) );
				}
				break;
			}
		}

		return ret;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed( final ActionEvent e ) {
		// --- ADD ---
		if ( e.getSource().equals( bAdd ) ) {
			final Roi roi = this.imgPlus.getRoi();
			if ( roi == null ) {
				IJ.error( "Please use ImageJ to select a ROI containing your cell of interest." );
				return;
			}

			// if ROI for this frame exists -- remove it first
			final int rowId = getFrameInTable( imgPlus.getT() );
			if ( rowId >= 0 ) {
				tableData.remove( rowId );
			}

			final Rectangle rect = roi.getBounds();
			final Vector< String > row = new Vector< String >();
			row.add( "" + imgPlus.getT() );
			row.add( "" + rect.x );
			row.add( "" + rect.y );
			row.add( "" + rect.width );
			row.add( "" + rect.height );
			tableData.add( row );

			Collections.sort( tableData, new Comparator< Vector< String >>() {
				@Override
				public int compare( final Vector< String > v1, final Vector< String > v2 ) {
					final int n1 = Integer.parseInt( v1.get( 0 ) );
					final int n2 = Integer.parseInt( v2.get( 0 ) );
					return ( n1 - n2 );
				}
			} );

			tmROIs.setDataVector( tableData, tableColumnNames );

	    // --- DEL ---
		} else if ( e.getSource().equals( bDel ) ) {
			final int rowId = tableROIs.getSelectedRow();
			if ( rowId >= 0 ) {
				tableData.remove( rowId );
				tmROIs.setDataVector( tableData, tableColumnNames );
			}

	    // --- SHOW PREVIOUS ---
		} else if ( e.getSource().equals( bShowPrevious ) ) {
			int t = imgPlus.getT();
			t--;
			if ( t > 0 ) {
				showRoiOnFrame( imgPlus.getC(), t );
			} else {
				System.out.println( "There is no previous frame..." );
			}

	    // --- SHOW NEXT ---
		} else if ( e.getSource().equals( bShowNext ) ) {
			int t = imgPlus.getT();
			t++;
			if ( t <= imgPlus.getDimensions()[ 4 ] ) {
				showRoiOnFrame( imgPlus.getC(), t );
			} else {
				System.out.println( "There is no next frame..." );
			}
		}
	}

	/**
	 * @param frameId
	 *            the index of the active frame.
	 * @return the index of the corresponding row in the table-data, or -1 if it
	 *         is not contained.
	 */
	private int getFrameInTable( final int frameId ) {
		int ret = 0;
		for ( final Vector< String > row : tableData ) {
			if ( row.get( 0 ).equals( "" + frameId ) ) { return ret; }
			ret++;
		}
		return -1;
	}

	/**
	 * @return the 'crop'-button... in order to know if an external event was
	 *         caused by this button...
	 */
	public JButton getButtonCrop() {
		return bCrop;
	}

	/**
	 * @return an ImgLib2 <code>Img</code> of <code>DoubleType</code> containing
	 *         the cropped area.
	 */
	public Img< DoubleType > getCroppedImgNormalized() {
		// determine size
		int startFrame;
		int channels, frames;
		int maxW = 0, maxH = 0;
		if ( tableROIs.getRowCount() == 0 ) {
			startFrame = 0;
			channels = imgPlus.getDimensions()[ 2 ];
			frames = imgPlus.getDimensions()[ 4 ];
			maxW = imgPlus.getDimensions()[ 0 ];
			maxH = imgPlus.getDimensions()[ 1 ];
		} else if ( tableROIs.getRowCount() == 1 ) {
			startFrame = Integer.parseInt( tableData.get( 0 ).get( 0 ) );
			channels = imgPlus.getDimensions()[ 2 ];
			frames = 1;
			maxW = Integer.parseInt( tableData.get( 0 ).get( 3 ) );
			maxH = Integer.parseInt( tableData.get( 0 ).get( 4 ) );
		} else {
			startFrame = Integer.parseInt( tableData.get( 0 ).get( 0 ) );
			channels = imgPlus.getDimensions()[ 2 ];
			frames = Integer.parseInt( tableData.get( tableData.size() - 1 ).get( 0 ) );
			frames -= Integer.parseInt( tableData.get( 0 ).get( 0 ) );
			for ( final Vector< String > row : tableData ) {
				final int w = Integer.parseInt( row.get( 3 ) );
				final int h = Integer.parseInt( row.get( 4 ) );
				maxW = Math.max( maxW, w );
				maxH = Math.max( maxH, h );
			}

		}

		// create empty Img
		final ImgFactory< DoubleType > imgFactory = new ArrayImgFactory< DoubleType >();
		final Img< DoubleType > ret = imgFactory.create( new int[] { maxW, maxH, channels, frames }, new DoubleType() );

		// wrap source-image
		final Img< DoubleType > srcImg = ImagePlusAdapter.wrap( imgPlus );

		// copying data
		for ( int i = 0; i < frames; i++ ) {
			for ( int c = 0; c < channels; c++ ) {
				final int[] roi = getInterpolatedROI( startFrame + i );
				final int deltaW = maxW - roi[ 2 ];
				final int deltaH = maxH - roi[ 3 ];
				final long x = roi[ 0 ] - deltaW / 2;
				final long y = roi[ 1 ] - deltaH / 2;

				final IntervalView< DoubleType > src = Views.hyperSlice( Views.hyperSlice( srcImg, 3, i ), 2, c );
				final IntervalView< DoubleType > target = Views.hyperSlice( Views.hyperSlice( ret, 3, i ), 2, c );
				DataMover.copy( Views.offset( src, x, y ), target );
			}
		}

		// Normalization
		Normalize.normalize( ret, new DoubleType( 0. ), new DoubleType( 1. ) );

		// debugging idea:
		// ImageJFunctions.show( ret );

		// tadaaaa!
		return ret;
	}

}
