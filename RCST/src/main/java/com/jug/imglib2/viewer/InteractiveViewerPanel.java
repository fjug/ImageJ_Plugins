/**
 *
 */
package com.jug.imglib2.viewer;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DrawingEditor;

/**
 * @author jug
 */
public class InteractiveViewerPanel extends javax.swing.JPanel {

	private final InteractiveViewerDrawingView imglib2viewer;

	private JScrollPane sp;

	private final DefaultDrawing background = new DefaultDrawing();
	private final DefaultDrawing drawing = new DefaultDrawing();

	private final DrawingEditor editor = new DefaultDrawingEditor();

	public InteractiveViewerPanel() {
		this( 1, 1 );
	}

	/**
	 * @param w
	 *            the width of the content to be displayed
	 * @param h
	 *            the height of the content to be displayed
	 */
	public InteractiveViewerPanel( final int w, final int h ) {
		imglib2viewer = new InteractiveViewerDrawingView( drawing, this.getPreferredSize().width, this.getPreferredSize().height );
		editor.add( imglib2viewer );

		buildGui();

		sp.setPreferredSize( imglib2viewer.getPreferredSize() );
	}

	private void buildGui() {
		this.setLayout( new BorderLayout() );
	}

	/**
	 * @return the imglib2viewer
	 */
	public InteractiveViewerDrawingView getImglib2viewer() {
		return imglib2viewer;
	}

//	public static void mainnnnnnn( final String[] args ) {
//		final JFrame f = new JFrame( "Demo" );
//		f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
//		f.setSize( 400, 300 );
//		f.getContentPane().add( new InteractiveViewerPanel() );
//		f.setVisible( true );
//	}

	/**
	 * @return
	 * @return
	 */
	public DrawingEditor getEditor() {
		return this.editor;
	}
}
