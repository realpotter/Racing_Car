public class Body {
	protected float mass = 1;
	protected float momentOfInertia = 1;

	private State currentState = new State();
	private State nextState    = new State();


	/** Get the "current" state of the body. **/
	public State currentState() {
		return currentState;
	}

	/** Get the potential state of the body at the next time step. **/
	public State nextState() {
		return nextState;
	}

	/** Integrate the current state to get the next state. **/
	public void integrate( float dt ) {
		currentState.integrate( dt, nextState );
	}

	/** Make the current state the next state. **/
	public void update() {
		currentState.copy( nextState );
	}

	public class State {

		public float x = 0;
		public float y = 0;

		public float[] xpoints = null;
		public float[] ypoints = null;
		public int numpoints = -1;

		public float vx = 0;
		public float vy = 0;
		protected float fx = 0;
		protected float fy = 0;

		public float angle = (float)(0);
		public float angularVelocity = 0.0f;
		protected float torque = 0;

		public float speed() {
			return (float)Math.sqrt( vx*vx + vy*vy );
		}
		
		protected boolean colliding = false;

		public boolean isColliding() {
			return colliding;
		}

		public void applyForce( float x, float y, float fx, float fy ) {
			this.fx = this.fx + fx;
			this.fy = this.fy + fy;

			// adjust torque
			float rx = (x - this.x);
			float ry = (y - this.y);

			torque -= (-ry*fx + rx*fy);
		}

		private float cx = 0, cy = 0, nx = 0, ny = 0;
		private int numcollisions = 0;

		/** Apply an impulse (collision force) to the Body,
  		 *  at x, y with the collision normal nx, ny.
		 **/
		public void collide( float x, float y, float nx, float ny ) {

			float rx = (x - this.x);
			float ry = (y - this.y);

			// velocity of point
			float vx = this.vx + angularVelocity*ry;
			float vy = this.vy + -angularVelocity*rx;

			// if we are heading away already, don't do anything
			if ( nx*vx + ny*vy > 0 )
				return;
			
			cx += x;
			cy += y;
			this.nx += nx;
			this.ny += ny;
			numcollisions++;
			
			colliding = true;
			
		}

		public void computeCollisions() {
			
			if ( numcollisions == 0 )
				return;
			
			cx /= numcollisions;
			cy /= numcollisions;
			nx /= numcollisions;
			ny /= numcollisions;
			
			float rx = (cx - this.x);
			float ry = (cy - this.y);

			// velocity of point
			float vx = this.vx + angularVelocity*ry;
			float vy = this.vy + -angularVelocity*rx;

			// if we are heading away already, don't do anything
			if ( nx*vx + ny*vy > 0 )
				return;

			

			float impulseSize = -1.1f*mass;

			float impulsefx = impulseSize*Math.abs( nx )*vx,
				  impulsefy = impulseSize*Math.abs( ny )*vy;

			float impulseTorque = -(-ry*impulsefx + rx*impulsefy);

			float ax = impulsefx/mass;
			float ay = impulsefy/mass;

			float friction = 0.2f;
			this.vx += (ax - friction*this.vx*Math.abs( ny ));  // friction
			this.vy += (ay - friction*this.vy*Math.abs( nx ));
			
			angularVelocity += (impulseTorque/momentOfInertia);
			
			if ( angularVelocity > Math.PI ) {
				angularVelocity = (float)Math.PI;
			}
			else if ( angularVelocity < -Math.PI ) {
				angularVelocity = (float)-Math.PI;
			}
			
			numcollisions = 0;
			cx = cy = nx = ny = 0;
			
			colliding = false;
		}

		/** Apply an impulse (collision force) to the Body,
  		 *  at x, y with the collision normal nx, ny.
		 **/
		public void collide( float x, float y, float nx, float ny, Body body, State state ) {
			colliding = true;
			state.colliding = true;
			// not done yet
			// needs to work out speed at collision point

			/*float rx1 = (x - this.x);
			float ry1 = (y - this.y);
			float rx2 = (x - state.x);
			float ry2 = (y - state.y);*/

			

			// velocities
			float vx1 = this.vx;// + angularVelocity*ry1;
			float vy1 = this.vy;// + -angularVelocity*rx1;
			float vx2 = state.vx;// + state.angularVelocity*ry2;
			float vy2 = state.vy;// + -state.angularVelocity*rx2;


			// velocity of point
			float vx = vx1 - vx2;
			float vy = vy1 - vy2;

			float impulseSize = -1.1f*(mass+body.mass);

			float impulsefx = impulseSize*Math.abs( nx )*vx,
				  impulsefy = impulseSize*Math.abs( ny )*vy;

			if ( nx*vx + ny*vy > 0 )
				return;

			float massInv = 1.0f/(mass + body.mass);

			float ax = impulsefx*massInv;
			float ay = impulsefy*massInv;

			this.vx += ax;
			this.vy += ay;

			state.vx -= ax;
			state.vy -= ay;
			
			
			//System.out.println( "ax " + ax + " ay " + ay );
			//System.out.println( "vx " + state.vx + " vy " + state.vy );
		}


		public void copy( State state ) {
			this.x = state.x;
			this.y = state.y;

			this.vx = state.vx;
			this.vy = state.vy;
			this.fx = state.fx;
			this.fy = state.fy;

			this.angle = state.angle;
			this.angularVelocity = state.angularVelocity;
			this.torque = state.torque;
			this.colliding = state.colliding;

			System.arraycopy( state.xpoints, 0, xpoints, 0, numpoints );
			System.arraycopy( state.ypoints, 0, ypoints, 0, numpoints );
		}

		/** Stop all movement, zero all forces. **/
		public void halt() {
			vx = vy = 0.0f;
			fx = fx = 0.0f;
			angularVelocity = 0.0f;
			torque = 0.0f;
		}

		/** Integrate this state to get the state after dt seconds.
   	 	 *  <CODE>next</CODE> will contain the new state.
	 	 **/
		public void integrate( float dt, State next ) {

			//System.out.println( "dt " + dt + " vx " + vx + " vy " + vy + " mass " + mass );
			
			// integrate linear (positional) stuff
			next.x = x + dt*vx;
			next.y = y + dt*vy;
			
			//System.out.println( x + " " + y );
			
			torque -= 200.0f*angularVelocity;
			applyForce( x, y, -1.5f*vx, -1.5f*vy );

			float ax = fx/mass;
			float ay = fy/mass;

			next.vx = vx + dt*ax;
			next.vy = vy + dt*ay;

			float speedSq = next.vx*next.vx + next.vy*next.vy;

			int maxSpd = 50;
			if ( speedSq > maxSpd*maxSpd ) {
				float speed = (float)Math.sqrt( speedSq );
				next.vx = (maxSpd*next.vx/speed);
				next.vy = (maxSpd*next.vy/speed);
			}

			// integrate angle
			next.angle = angle + dt*angularVelocity;
			next.angularVelocity = angularVelocity + dt*torque/momentOfInertia;
			// other angle stuff



			next.fx = 0.0f;
			next.fy = 0.0f;
			next.torque = 0.0f;

			next.colliding = false;
			
			//System.out.println( "x " + next.x + " y " + next.y );
		}

	}  // end State

}
