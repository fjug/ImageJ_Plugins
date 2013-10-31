/**
 *
 */

import ij.plugin.PlugIn;

import com.jug.OrthoSlicer;

/**
 * @author jug
 */
public class SumAlongLine_ implements PlugIn {

	/**
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run( final String arg ) {
		OrthoSlicer.main( null );
	}

}
