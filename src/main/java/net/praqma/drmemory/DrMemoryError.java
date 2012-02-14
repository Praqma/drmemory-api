package net.praqma.drmemory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.drmemory.errors.InvalidHeapArgument;
import net.praqma.drmemory.errors.Leak;
import net.praqma.drmemory.errors.UnaddressableAccess;
import net.praqma.drmemory.errors.UninitializedRead;
import net.praqma.drmemory.errors.Unknown;
import net.praqma.drmemory.errors.Warning;
import net.praqma.drmemory.exceptions.InvalidErrorTypeException;
import net.praqma.drmemory.exceptions.InvalidInputException;
import net.praqma.util.debug.Logger;

public abstract class DrMemoryError {
	protected int identifier;
	protected String header;
	protected int duplicates = 1;
	
	protected List<StackTrace> trace = new ArrayList<StackTrace>();
	protected List<Note> notes = new ArrayList<Note>();
	
	private static Logger logger = Logger.getLogger();
	
	private static Map<String, Class<?>> errors = new HashMap<String, Class<?>>();
	private static Class<?> unknown = Unknown.class;
	
	static {
		errors.put( "LEAK", Leak.class );
		errors.put( "UNINITIALIZED READ", UninitializedRead.class );
		errors.put( "INVALID HEAP ARGUMENT", InvalidHeapArgument.class );
		errors.put( "UNADDRESSABLE ACCESS", UnaddressableAccess.class );
		errors.put( "WARNING", Warning.class );
	}
	
	public static class StackTrace {
		public String function;
		public int line;
		public String file;
		
		public String toString() {
			return "<" + file + "> " + function + " @ line " + line;
		}
	}
	
	public static class Note {
		public String note;
		
		public String toString() {
			return note;
		}
	}
	
	public void parseHeader( String header ) {
		this.header = header;
	}
	
	public static final Pattern rx_stackTrace = Pattern.compile( "^#\\s?(\\d+) (\\S+)\\s+\\[(.*?)\\]$", Pattern.MULTILINE );
	public static final Pattern rx_notes = Pattern.compile( "Note: (.*?)$", Pattern.MULTILINE );
	
	public void getBody( String error ) {
		String[] lines = error.split( "\\n" );
		
		for( String line : lines ) {
			
			/* Finding stack traces */
			Matcher mst = rx_stackTrace.matcher( line );
			if( mst.find() ) {
				
				StackTrace st = new StackTrace();
				st.file = mst.group( 3 );
				st.function = mst.group( 2 );
				st.line = Integer.parseInt( mst.group( 1 ) );
				
				onAddStackTrace( st );
				//logger.debug( "Adding:  " + st );
				trace.add( st );
			}
			
			/* Finding notes */
			Matcher mnotes = rx_notes.matcher( line );
			if( mnotes.find() ) {
				
				Note note = new Note();
				note.note = mnotes.group( 1 );
				
				onAddNote( note );
				//logger.debug( "Adding note:  " + note );
				notes.add( note );
			}
		}
	}
	
	/**
	 * Called just before stack trace is added
	 * @param st {@link StackTrace}
	 */
	public void onAddStackTrace( StackTrace st ) {}
	
	/**
	 * Listener for on add note
	 * @param note
	 */
	public void onAddNote( Note note ) {}
	
	/**
	 * Returns the identifier of the error
	 * @return
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	/**
	 * Returns the header of the error
	 * @return
	 */
	public String getheader() {
		return header;
	}
	
	/**
	 * Returns the number of duplicates of this error
	 * @return
	 */
	public int getDuplicates() {
		return duplicates;
	}
	
	/**
	 * Sets the number of duplicates
	 * @param number
	 */
	public void setDuplicates( int number ) {
		this.duplicates = number;
	}
	
	//public static final Pattern rx_header = Pattern.compile( ".?Error #(\\d+): (LEAK|UNINITIALIZED READ):? (.*?)$", Pattern.MULTILINE );
	public static final Pattern rx_header = Pattern.compile( ".?Error #(\\d+): ([A-Z\\s]+):? (.*?)$", Pattern.MULTILINE );
	
	public static DrMemoryError parse( String e ) throws InvalidInputException {
		
		/* Retrieve the header */
		Matcher m = rx_header.matcher( e );
		
		if( !m.find() ) {
			logger.error( "Invalid input: " + e );
			throw new InvalidInputException( "header" );
		}
		
		String errorType = m.group( 2 );
		int number = Integer.parseInt( m.group( 1 ) );
		Class<?> cls = null;
		try {
			logger.debug( "Error type is " + errorType );
			cls = DrMemoryError.resolve( errorType );
		} catch( InvalidErrorTypeException e1 ) {
			logger.warning( "Unknown error " + e1.getMessage() );
			cls = unknown;
		}
		
		DrMemoryError error = null;
		try {
			error = (DrMemoryError) cls.newInstance();
			error.identifier = number;
			error.parseHeader( m.group( 3 ) );
		} catch( Exception e1 ) {
			logger.warning( "Unable to instantiate error " + errorType );
			throw new InvalidInputException( "Instantiation of " + errorType + ": " + e1.getMessage() );
		}
		
		error.getBody( e );
		
		return error;
	}
	
	public static Class<?> resolve( String type ) throws InvalidErrorTypeException {
		if( errors.containsKey( type ) ) {
			return errors.get( type );
		} else {
			throw new InvalidErrorTypeException( type );
		}
	}
}
