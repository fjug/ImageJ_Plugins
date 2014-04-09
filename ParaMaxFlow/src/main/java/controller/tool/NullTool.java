package controller.tool;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.tool.AbstractTool;

import view.display.JHotDrawInteractiveDisplay2D;

/**
 * This tool does literally nothing.
 * We use this silly appearing tool e.g. when we want to interacti with the
 * transformation in the IddeaComponent without touching any existing
 * annotation.
 * 
 * @since 8/12/13 5:12 PM
 * @author HongKee Moon, Florian Jug
 */

public class NullTool extends AbstractTool {

	private static final long serialVersionUID = -7989251158136970679L;

	@Override
	public void mouseDragged( final MouseEvent arg0 ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activate( final DrawingEditor editor ) {
		super.activate( editor );

		final DrawingView view = this.editor.getActiveView();
		if ( JHotDrawInteractiveDisplay2D.class.isInstance( view ) ) {
			( ( JHotDrawInteractiveDisplay2D< ? > ) view ).activateHandler();
		}

	}

	@Override
	public void deactivate( final DrawingEditor editor ) {
		super.deactivate( editor );

		final DrawingView view = this.editor.getActiveView();
		if ( JHotDrawInteractiveDisplay2D.class.isInstance( view ) ) {
			( ( JHotDrawInteractiveDisplay2D< ? > ) view ).deactivateHandler();
		}
	}

	@Override
	public void keyPressed( final KeyEvent evt ) {}

	@Override
	public void keyReleased( final KeyEvent evt ) {}

	@Override
	public void keyTyped( final KeyEvent evt ) {}
}
