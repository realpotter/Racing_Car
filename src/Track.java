
import java.awt.Rectangle;
import java.io.*;
import java.util.*;

public class Track {
	public Section[] sections = null;
	private int width = -1, height = -1;
	
	private String version = "1.0";

	public String getVersion() {
		return version;
	}	

	private String nextStringOrThrow( StreamTokenizer tokenizer ) throws IOException {
		int tt = tokenizer.nextToken();
		if ( tt == StreamTokenizer.TT_EOF )
			throw new IOException( "Unexpected end of file, line: " + tokenizer.lineno() );
		return tokenizer.sval;		
	}

	private double nextDoubleOrThrow( StreamTokenizer tokenizer ) throws IOException {
		int tt = tokenizer.nextToken();
		if ( tt == StreamTokenizer.TT_EOF )
			throw new IOException( "Unexpected end of file, line: " + tokenizer.lineno() );
		if ( tt != StreamTokenizer.TT_NUMBER )
			throw new IOException( "Expected a number on line: " + tokenizer.lineno() + " got: " + tokenizer.sval );
		return tokenizer.nval;		
	}

	// int lineno()
	// lowerCaseMode(boolean)
	// commentChar(int)
	// quoteChar(int)
	// pushBack()
	
	public void load( Reader in ) throws IOException {
		StreamTokenizer tokenizer = new StreamTokenizer( in );

		tokenizer.eolIsSignificant( false );

		tokenizer.nextToken();

		if ( !tokenizer.sval.equals( "version" ) )
			throw new IOException( "missing version header" );

		tokenizer.nextToken();

		version = tokenizer.sval == null ? tokenizer.nval + "" : tokenizer.sval;

		Vector sectionsVector = new Vector();

		while( tokenizer.nextToken() != StreamTokenizer.TT_EOF ) {
			String sectionType = tokenizer.sval;
			if ( sectionType.equals( "straight" ) ) {
				
				String param = nextStringOrThrow( tokenizer );
				boolean vertical = param.equals( "vert" );
				if ( !vertical && !param.equals( "horiz" ) )
					throw new IOException( "line: " + tokenizer.lineno() + " straight sections, must be horiz or vert, not " + param );
					
				double x = nextDoubleOrThrow( tokenizer );
				double y = nextDoubleOrThrow( tokenizer );

				StraightSection straightSection = new StraightSection( vertical, (int)x, (int)y );
				sectionsVector.addElement( straightSection );

			}
			else if ( sectionType.equals( "corner" ) ) {

				String param1 = nextStringOrThrow( tokenizer );
				boolean left = param1.equals( "left" );
				if ( !left && !param1.equals( "right" ) )
					throw new IOException( "line: " + tokenizer.lineno() + " corner sections, must be left or right, not " + param1 );
				
				String param2 = nextStringOrThrow( tokenizer );
				boolean top = param2.equals( "top" );
				if ( !top && !param2.equals( "bottom" ) )
					throw new IOException( "line: " + tokenizer.lineno() + " corner sections, must be top or bottom, not " + param2 );
				
				double x = nextDoubleOrThrow( tokenizer );
				double y = nextDoubleOrThrow( tokenizer );
				
				CornerSection cornerSection = new CornerSection( left, top, (int)x, (int)y );
				sectionsVector.addElement(	cornerSection );

			}
			else {
				throw new IOException( 
					"line " +tokenizer.lineno()+ 
					" unknown section type: " + sectionType ); 
			}			
		}

		sections = new Section[ sectionsVector.size() ];
		sectionsVector.copyInto( sections );

		for ( int i = 0; i < sections.length; i++ ) {
			sections[ i ].setNextSection( sections[ (i+1)%sections.length ] );
		}
		
		calcDimensions();
	}

	public void save( Writer out ) {
		PrintWriter printer = new PrintWriter( out );
		printer.println( "version " + version );
		printer.println();

		for ( int i = 0; i < sections.length; i++ ) {
			Section section = sections[ i ];
			Rectangle bounds = section.getBounds();
			int type = section.type();
			
			switch( type ) {
				case Section.STRAIGHT:

					StraightSection straight = (StraightSection)section;
					boolean vertical = straight.isVertical();
					printer.print( "straight " );
					printer.print( (vertical ? "vert " : "horiz ") );
					printer.println( bounds.x + " " + bounds.y );
					break;

				case Section.CORNER:

					CornerSection corner = (CornerSection)section;
					boolean left = corner.toLeft();
					boolean top = corner.toTop();
					printer.print( "corner " );
					printer.print( (left ? "left " : "right ") );
					printer.print( (top ? "top " : "bottom ") );
					printer.println( bounds.x + " " + bounds.y );
					break;

				default:
					throw new RuntimeException( "Unknown section type" );
			}
		}
	}

	/** Does collisions between the vehicle (in the state specified)
     *  and the track.  <CODE>currentSection</CODE> is the index of the
	 * section the vehicle is/was in last.  The returned value is the 
	 * section the vehicle is now in.
	 **/
	public int checkCollisions( Vehicle vehicle, Body.State state, int currentSection ) {
		
		Section section = sections[ currentSection ];
		
		boolean inThisSection = section.intersects( vehicle, state );

		if ( inThisSection ) {
			section.checkCollision( vehicle, state );
		}

		Section nextSection = section.getNextSection();
		if ( nextSection.intersects( vehicle, state ) ) {
			nextSection.checkCollision( vehicle, state );
			if ( !inThisSection )
				currentSection = (currentSection +1)%sections.length;
			return currentSection;
		}

		Section prevSection = section.getPreviousSection();
		if ( prevSection.intersects( vehicle, state ) ) {
			prevSection.checkCollision( vehicle, state );
			if ( !inThisSection ) {
				currentSection = (currentSection -1)%sections.length;
				if ( currentSection < 0 ) 
					currentSection += sections.length;
			}
			return currentSection;
		}
		
		return currentSection;
	}

	/** Put the vehicles on the starting grid. **/
	
	public void toStartingPositions( Vehicle[] vehicles ) {
		
		if ( sections == null || sections.length < 2 )
			throw new RuntimeException( "A track must have at least two sections" );
		
		Section start = sections[ 0 ];
		Section next = sections[ 1 ];
		
		Rectangle startBounds = start.getBounds();
		Rectangle nextBounds = next.getBounds();
		
		float dx = nextBounds.x - startBounds.x;
		float dy = nextBounds.y - startBounds.y;
		
		float dist = (float)Math.sqrt( dx*dx + dy*dy );
		dx /= dist;
		dy /= dist;
		
		float x = startBounds.x + 0.5f*startBounds.width;
		float y = startBounds.y + 0.5f*startBounds.height;
		float width = startBounds.width*0.75f;
		
		float dir = (float)Math.atan2( dx, dy );
		
		for ( int i = 0; i < vehicles.length; i++ ) {
			Body.State state = vehicles[ i ].currentState();
			state.halt();
			state.angle = dir;
			float rel = (i/(float)(Math.max( 1, vehicles.length -1 ))) - 0.5f;
			state.x = x - rel*width*dy;
			state.y = y + rel*width*dx;
			vehicles[ i ].integrate( 0.0f );
			vehicles[ i ].update();
			//System.out.println( state.x + " " + state.y + " " + rel );
		}
	}
	
	public void calcDimensions() {
		width = -1;
		height = -1;
		for ( int i = 0; i < sections.length; i++ ) {
			Rectangle bounds = sections[ i ].getBounds();
			width  = Math.max( width, bounds.x + bounds.width );
			height = Math.max( height, bounds.y + bounds.height );
		}
	}
	

	public int getWidth() {
		if ( width == -1 )
			calcDimensions();
		return width;
	}
	
	public int getHeight() {
		if ( height == -1 )
			calcDimensions();
		return height;
	}
	
	public static abstract class Section {
		private Section next = null;
		private Section prev = null;
		private Rectangle bounds = null;
		public final static int STRAIGHT = 0, CORNER = 1;

		public Section( int x, int y ) {
			bounds = new Rectangle( x, y, 64, 64 );
		}			

		public abstract int type();

		public abstract float distanceToEnd( Vehicle vehicle );

		public Section getNextSection() {
			return next;
		}

		public Section getPreviousSection() {
			return prev;
		}

		public void setNextSection( Section section ) {
			next = section;
			section.prev = this;
		}

		public abstract boolean intersects( Vehicle vehicle, Body.State state );

		public Rectangle getBounds() {
			return bounds;
		}
		
		public abstract void checkCollision( Vehicle vehicle, Body.State state );

	}
}
