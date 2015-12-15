 
public class Race {
	private Track track = null;
	private Vehicle[] vehicles = null;
	private int[] currentSections = null;
	private int[] nextSections = null;
	private int[] ranks = null;
	private int[] trackCounts = null;

	private int numLaps = 4;

	private NetworkModule networkModule = null;
	private Vehicle localVehicle = null;

	public Race( Track track, Vehicle[] vehicles ) {
		this.track = track;
		this.vehicles = vehicles;
		int num_vehicles = vehicles.length;
		currentSections = new int[ num_vehicles ];
		nextSections    = new int[ num_vehicles ];
		ranks           = new int[ num_vehicles ];
		trackCounts     = new int[ num_vehicles ];
		reset();
	}

	public Race( Vehicle localVehicle, Track track, Vehicle[] vehicles, NetworkModule networkModule ) {
		this( track, vehicles );
		this.localVehicle = localVehicle;
		this.networkModule = networkModule;
	}

	public void reset() {
		for ( int i = 0; i < vehicles.length; i++ ) {
			currentSections[ i ] = nextSections[ i ] = 0;
			ranks[ i ] = 1;
			trackCounts[ i ] = 0;
		}
		
		if ( track.sections != null )		
			track.toStartingPositions( vehicles );
		
		lightsStarted = -1;
		lightsDone = false;
		currentLight = 0;
			
		lastNetworkUpdate = 0;
	}

	public int getRank( Vehicle vehicle ) {
		return ranks[ vehicle.getID() ];
	}

	public int[] getRanks() {
		return ranks;
	}

	public int getTrackCount( Vehicle vehicle ) {
		return trackCounts[ vehicle.getID() ];
	}
	
	public int getNumLaps() {
		return numLaps;
	}

	public int[] getTrackCounts() {
		return trackCounts;
	}

	public Track getTrack() {
		return track;
	}

	public Vehicle[] getVehicles() {
		return vehicles;
	}

	private boolean checkCollisions() {
		boolean collision = false;
		int numsections = track.sections.length;
		for ( int i = 0; i < vehicles.length; i++ ) {
			Vehicle vehiclei = vehicles[ i ];
			Body.State statei = vehiclei.nextState();
			int sectioni = nextSections[ i ];
			for ( int j = i+1; j < vehicles.length; j++ ) {
				Vehicle vehiclej = vehicles[ j ];
				Body.State statej = vehiclej.nextState();
				int sectionj = nextSections[ j ];
				if (    sectioni != sectionj
					 && ((sectioni +1) % numsections) != sectionj
					 && ((sectionj +1) % numsections) != sectioni )
					continue;

				float distMin = 0.5f*(vehiclei.getWidth() + vehiclej.getWidth());
				float dx = statei.x - statej.x;
				float dy = statei.y - statej.y;
				float distSq = dx*dx + dy*dy;
				if ( distSq > distMin*distMin || (dx == 0 && dy == 0) )
					continue;

				float dist = (float)Math.sqrt( distSq );
				
				if ( dist == 0.0f )
					continue;
				
				/*System.out.println( dist + " " + dx +" " + dy );
				try{
				Thread.sleep( 1000 );
				}catch( Exception e ){}*/
				
				dx /= dist;
				dy /= dist;

				statei.collide( 0.5f*(statei.x +statej.x),
							   0.5f*(statei.y +statej.y),
							   dx, dy, vehiclej, statej );

				collision = true;
			}
		}

		return collision;
	}

	private int calcRanking( int index ) {

		// this needs double checking it should work tho

		Vehicle vehicle = vehicles[ index ];
		int lap	    = trackCounts[ index ];
		int sectionIndex = currentSections[ index ];
		int nextSection = (sectionIndex + 1)%track.sections.length;
		Track.Section section = track.sections[ nextSection ];
		float dist = section.distanceToEnd( vehicle );
		int numInFront = 0;
		for ( int i = 0; i < vehicles.length; i++ ) {
			if ( i == index )
				continue;
			Vehicle competitor = vehicles[ i ];
			int competitorLap = trackCounts[ i ];
			if ( lap > competitorLap )
				continue;
			if ( lap < competitorLap ) {
				numInFront++;
				continue;
			}
			int competitorSectionIndex = currentSections[ i ];
			if (   sectionIndex > competitorSectionIndex )
				continue;
			if ( sectionIndex < competitorSectionIndex ) {
				numInFront++;
				continue;
			}

			int competitorNextSection = (competitorSectionIndex + 1)%track.sections.length;
			Track.Section competitorSection = track.sections[ competitorNextSection ];
			float competitorDist = competitorSection.distanceToEnd( competitor );

			if ( dist < competitorDist )
				continue;

			if ( dist > competitorDist ) {
				numInFront++;
				continue;
			}

		}
		return numInFront + 1;
	}

	private long lightsStarted = -1;
	private boolean lightsDone = false;
	private int currentLight = 0;
	
	private long lastNetworkUpdate = 0;
	
	public int getCurrentLight() {
		return currentLight;
	}

	public void integrate( float dt ) {

		boolean collision = false;

		boolean racing = false;
		
		if ( networkModule == null || networkModule.ready() ) {
			
			if ( !lightsDone ) {
				if ( lightsStarted == -1 )
					lightsStarted = System.currentTimeMillis();
	
				long time = System.currentTimeMillis();
				
				if ( time - lightsStarted > 4000 ) {
					currentLight = 4;
					lightsDone = true;
				}
				else if ( time - lightsStarted > 3000 ) {
					currentLight = 3;
				}
				else if ( time - lightsStarted > 2000 ) {
					currentLight = 2;
				}
				else if ( time - lightsStarted > 1000 ) {
					currentLight = 1;
				}
					
			} 
			else {	
				racing = true;
				
				for ( int i = 0; i < vehicles.length; i++ ) {
					Vehicle vehicle = vehicles[ i ];
					vehicle.integrate( dt );
					nextSections[ i ] = track.checkCollisions(
												vehicle,
												vehicle.nextState(),
												currentSections[ i ] );

					if ( vehicle.nextState().isColliding() ) {
						collision = true;
					}
				}
			}
			

			if ( checkCollisions() ) collision = true;
		}

		if ( collision && dt > 0.01f ) {
			integrate( dt*0.5f );
			integrate( dt*0.5f );
		}
		else {
			//if ( collision )
			//	System.out.println( "collision" );

			if ( racing ) {
				int lastSection = track.sections.length-1;
				for ( int i = 0; i < vehicles.length; i++ ) {
					Vehicle vehicle = vehicles[ i ];
					vehicle.nextState().computeCollisions();
					vehicle.update();
					int currentSection = currentSections[ i ];
					int nextSection = nextSections[ i ];
					if ( currentSection != nextSection ) {
						currentSections[ i ] = nextSection;
						// have we crossed the line?
						if ( currentSection == lastSection ) {
							if ( nextSection == 0 )
								trackCounts[ i ]++;
						}
						else if ( currentSection == 0 ) {
							if ( nextSection == lastSection )
								trackCounts[ i ]--;
						}
					}
				}

				for ( int i = 0; i < vehicles.length; i++ ) {
					ranks[ i ] = calcRanking( i );
				}
			}



			if ( networkModule != null ) {
				
				NetworkModule.State[] states = networkModule.getVehicleStates();
				for ( int i = 0; i < states.length; i++ ) {
					NetworkModule.State state = states[ i ];
					Vehicle vehicle = vehicles[ i ];
					if ( localVehicle == vehicle )
						continue;
					synchronized( state ) {
						if ( state.updated ) {
							vehicle.currentState().x = state.x;
							vehicle.currentState().y = state.y;
							vehicle.currentState().angle = state.angle;
							vehicle.currentState().vx = state.vx;
							vehicle.currentState().vy = state.vy;
							vehicle.currentState().angularVelocity = state.angularVelocity;
							state.updated = false;
						}
					}
				}

				long updateTime = System.currentTimeMillis();
				if ( updateTime - lastNetworkUpdate > 20 ) {
					lastNetworkUpdate = updateTime;
					networkModule.broadcastState( localVehicle, localVehicle.currentState() );
				}
			}
		}
	}
}
