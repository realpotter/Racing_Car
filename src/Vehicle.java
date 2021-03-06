 
import java.awt.Rectangle;

public class Vehicle extends Body {

	private boolean motoron = false;
	private boolean reversing = false;
	public float steering_angle = 0.0f;

	public float wx1, wy1;
	public float wx2, wy2;

	private float length = 10;
	private float width  = 6;

	private byte id = (byte)0;

	public Vehicle( byte id, float x, float y ) {
		this( x, y );
		this.id = id;
	}

	public Vehicle( float x, float y ) {

		currentState().x = x;
		currentState().y = y;
		currentState().xpoints = new float[ 4 ];
		currentState().ypoints = new float[ 4 ];
		currentState().numpoints = 4;

		nextState().xpoints = new float[ 4 ];
		nextState().ypoints = new float[ 4 ];
		nextState().numpoints = 4;

		momentOfInertia = (length*length/4)*(width*width/4)/4;
		mass = 4;
	}

	public byte getID() {
		return id;
	}

	private boolean left = false;
	private boolean right = false;
	
	private void setAngle() {
		if ( left && !right ) {
			steering_angle = (float)(Math.PI/4);
		}
		else if ( right && !left ) {
			steering_angle = (float)(-Math.PI/4);
		}
		else {
			steering_angle = 0;
		}
	}

	public void steerLeft( boolean left ) {
		this.left = left;
		setAngle();
	}
	
	public void steerRight( boolean right ) {
		this.right = right;
		setAngle();
	}

	public float getLength() {
		return length;
	}

	public float getWidth() {
		return width;
	}


	public void setMotorOn( boolean on ) {
		motoron = on;
	}

	public void reverse( boolean reversing ) {
		this.reversing = reversing;
	}

	public void integrate( float dt ) {
		State state = currentState();

		/*System.out.println( state.x + " " + state.y );
		try {
			Thread.sleep( 500 );
		}
		catch( Exception e ) {
		}
		;*/
		
		
		float angle = state.angle;

		float dx = (float)Math.sin( angle );
		float dy = (float)Math.cos( angle );

		wx1 = 4*dx - 4*dy;
		wy1 = 4*dy + 4*dx;
		wx2 = 4*dx + 4*dy;
		wy2 = 4*dy - 4*dx;


		if ( motoron || reversing || steering_angle != 0 ) {
			//System.out.println( '.' );

			float forceAngle = angle + steering_angle;

			float force = reversing ? -20 : motoron ? 40 : 20;
			float steering_force_x = (float)(force*Math.sin( forceAngle ));
			float steering_force_y = (float)(force*Math.cos( forceAngle ));


			float x = state.x;
			float y = state.y;

			state.applyForce( x + wx1, y + wy1, steering_force_x, steering_force_y );
			state.applyForce( x + wx2, y + wy2, steering_force_x, steering_force_y );

			//state.applyForce( x +10, y +10, steering_force_x, steering_force_y );
		}


		super.integrate( dt );

		float halfLength = 0.5f*length;
		float halfWidth  = 0.5f*width;

		State next_state = nextState();

		dx = (float)Math.sin( next_state.angle );
		dy = (float)Math.cos( next_state.angle );

		float x = next_state.x;
		float y = next_state.y;

		next_state.xpoints[ 0 ] = x + dx*halfLength -dy*halfWidth;
		next_state.ypoints[ 0 ] = y + dy*halfLength +dx*halfWidth;

		next_state.xpoints[ 1 ] = x - dx*halfLength -dy*halfWidth;
		next_state.ypoints[ 1 ] = y - dy*halfLength +dx*halfWidth;

		next_state.xpoints[ 2 ] = x - dx*halfLength +dy*halfWidth;
		next_state.ypoints[ 2 ] = y - dy*halfLength -dx*halfWidth;

		next_state.xpoints[ 3 ] = x + dx*halfLength +dy*halfWidth;
		next_state.ypoints[ 3 ] = y + dy*halfLength -dx*halfWidth;

	}

}



