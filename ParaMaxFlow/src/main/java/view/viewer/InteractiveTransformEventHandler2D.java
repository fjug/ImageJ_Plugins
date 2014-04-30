package view.viewer;

import java.awt.event.MouseEvent;

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
    
	@Override
	public void mousePressed( final MouseEvent e )
	{
		synchronized ( affine )
		{
			oX = e.getX();
			oY = e.getY();
			affineDragStart.set( affine );
		}
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		synchronized ( affine )
		{
			final int modifiers = e.getModifiersEx();

			if ( ( modifiers & MouseEvent.BUTTON1_DOWN_MASK ) != 0 ) // rotate
			{
				affine.set( affineDragStart );

				final double dX = e.getX() - oX;
				final double dY = e.getY() - oY;
				final double theta = Math.atan2( dY, dX ) - Math.atan2( oY, oX );

				rotate( theta );
			}
			else if ( ( modifiers & ( MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK ) ) != 0 ) // translate
			{
				affine.set( affineDragStart );

				final double dX = oX - e.getX();
				final double dY = oY - e.getY();

				affine.set( affine.get( 0, 2 ) - dX, 0, 2 );
				affine.set( affine.get( 1, 2 ) - dY, 1, 2 );
			}

			update();
		}
	}
	
	/**
	 * Rotate by d radians. Mouse X and Y should be kept.
	 * Florian requested this feature(#1066 in redmine)
	 */
	private void rotate( final double d )
	{
		// center shift
		affine.set( affine.get( 0, 2 ) - oX, 0, 2 );
		affine.set( affine.get( 1, 2 ) - oY, 1, 2 );

		// rotate
		affine.rotate( d );

		// center un-shift
		affine.set( affine.get( 0, 2 ) + oX, 0, 2 );
		affine.set( affine.get( 1, 2 ) + oY, 1, 2 );
	}
}
