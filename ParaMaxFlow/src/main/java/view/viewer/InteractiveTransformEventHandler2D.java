package view.viewer;

import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandler2D;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.imglib2.ui.TransformListener;


/**
 * This class is inherited from {@link TransformEventHandler2D} overriding setCanvasSize which is
 * able to scale it according to the minimum value between height and width.
 *
 * @author HongKee Moon
 */
public class InteractiveTransformEventHandler2D extends TransformEventHandler2D
{
	final static private TransformEventHandlerFactory< AffineTransform2D > factory = new TransformEventHandlerFactory< AffineTransform2D >()
	{
		@Override
		public TransformEventHandler< AffineTransform2D > create( final TransformListener< AffineTransform2D > transformListener )
		{
			return new InteractiveTransformEventHandler2D( transformListener );
		}
	};

	public static TransformEventHandlerFactory< AffineTransform2D > factory()
	{
		return factory;
	}
	
    public InteractiveTransformEventHandler2D( final TransformListener< AffineTransform2D > listener )
    {
        super(listener);
    }

    @Override
    public void setCanvasSize( final int width, final int height, final boolean updateTransform )
    {
        if ( updateTransform )
        {
            synchronized ( affine )
            {
                affine.set( affine.get( 0, 2 ) - canvasW / 2, 0, 2 );
                affine.set( affine.get( 1, 2 ) - canvasH / 2, 1, 2 );

                if(width - height < 0)
                    affine.scale( ( double ) width / canvasW );
                else
                    affine.scale( ( double ) height / canvasH );

                affine.set( affine.get( 0, 2 ) + width / 2, 0, 2 );
                affine.set( affine.get( 1, 2 ) + height / 2, 1, 2 );
                update();
            }
        }
        canvasW = width;
        canvasH = height;
        centerX = width / 2;
        centerY = height / 2;
    }
}
