import ij.IJ;
import ij.ImagePlus;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.jug.InterpolatedCropPanel;

import fiji.tool.AbstractTool;
import fiji.tool.SliceListener;
import fiji.tool.ToolToggleListener;
import fiji.tool.ToolWithOptions;

/**
 * This is a template for a generic tool using Fiji's AbstractTool
 * infrastructure.
 */
public class InterpolatedManualCrop_Tool extends AbstractTool implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, SliceListener, ToolToggleListener, ToolWithOptions {

	private final InterpolatedCropPanel panel;

	{
		// for debugging, all custom tools can be removed to make space for this one if necessary
		// clearToolsIfNecessary = true;
	}

	public InterpolatedManualCrop_Tool() {
		this.panel = InterpolatedCropPanel.getInstance();
	}

	public InterpolatedManualCrop_Tool( final InterpolatedCropPanel panel ) {
		this.panel = panel;
	}

	// The methods defined by the interfaces implemented by this tool
	@Override
	public void keyPressed( final KeyEvent e ) {
		e.consume(); // prevent ImageJ from handling this event
	}

	@Override
	public void keyReleased( final KeyEvent e ) {
		e.consume(); // prevent ImageJ from handling this event
	}

	@Override
	public void keyTyped( final KeyEvent e ) {
		if ( KeyEvent.getKeyText( e.getKeyCode() ).equals( "a" ) ) {
			panel.clickAdd();
		}
		if ( KeyEvent.getKeyText( e.getKeyCode() ).equals( "d" ) ) {
			panel.clickDel();
		}
		if ( KeyEvent.getKeyText( e.getKeyCode() ).equals( "." ) ) {
			panel.clickShowNext();
		}
		if ( KeyEvent.getKeyText( e.getKeyCode() ).equals( "," ) ) {
			panel.clickShowPrevious();
		}
		e.consume(); // prevent ImageJ from handling this event
	}

	@Override
	public void mouseClicked( final MouseEvent e ) {
		e.consume(); // prevent ImageJ from handling this event
	}

	@Override
	public void mousePressed( final MouseEvent e ) {
		IJ.log( "mouse pressed: " + getOffscreenX( e ) + ", " + getOffscreenY( e ) );
		e.consume(); // prevent ImageJ from handling this event
	}

	@Override
	public void mouseReleased( final MouseEvent e ) {
		IJ.log( "mouse released: " + getOffscreenX( e ) + ", " + getOffscreenY( e ) );
		e.consume(); // prevent ImageJ from handling this event
	}

	@Override
	public void mouseEntered( final MouseEvent e ) {
		IJ.log( "mouse entered: " + getOffscreenX( e ) + ", " + getOffscreenY( e ) );
		e.consume(); // prevent ImageJ from handling this event
	}

	@Override
	public void mouseExited( final MouseEvent e ) {
		IJ.log( "mouse exited: " + getOffscreenX( e ) + ", " + getOffscreenY( e ) );
		e.consume(); // prevent ImageJ from handling this event
	}

	@Override
	public void mouseMoved( final MouseEvent e ) {
		IJ.log( "mouse moved: " + getOffscreenX( e ) + ", " + getOffscreenY( e ) );
		e.consume(); // prevent ImageJ from handling this event
	}

	@Override
	public void mouseDragged( final MouseEvent e ) {
		IJ.log( "mouse dragged: " + getOffscreenX( e ) + ", " + getOffscreenY( e ) );
		e.consume(); // prevent ImageJ from handling this event
	}

	@Override
	public final void mouseWheelMoved( final MouseWheelEvent e ) {
		IJ.log( "mouse wheel moved: " + e.getWheelRotation() + " at " + getOffscreenX( e ) + ", " + getOffscreenY( e ) );
		e.consume(); // prevent ImageJ from handling this event
	}

	@Override
	public void sliceChanged( final ImagePlus image ) {
		IJ.log( "slice changed to " + image.getCurrentSlice() + " in " + image.getTitle() );
	}

	@Override
	public void showOptionDialog() {
		IJ.showMessage( "Here could be your option dialog!" );
	}

	@Override
	public void toolToggled( final boolean enabled ) {
		IJ.log( getToolName() + " was switched " + ( enabled ? "on" : "off" ) );
	}
}
