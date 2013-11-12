/**
 *
 */

import ij.plugin.PlugIn;

import com.jug.OrthoSlicer;

/**
 * @author jug
 */
public class CircularShapeApprox_ implements PlugIn {

	/**
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run( final String arg ) {
		OrthoSlicer.main( null );
		OrthoSlicer.getInstance().shapeFittingEllipses( true );
	}

}
