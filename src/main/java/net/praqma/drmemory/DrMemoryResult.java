package net.praqma.drmemory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.drmemory.exceptions.InvalidInputException;
import net.praqma.util.debug.Logger;

public class DrMemoryResult {
	private static Logger logger = Logger.getLogger();
	
	private File file;
	
	String version;
	String date;
	String cmd;
	List<String> elements;
	Map<Integer, DrMemoryError> errors = new HashMap<Integer, DrMemoryError>();
	
	private DrMemoryResult() {
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getCmd() {
		return cmd;
	}
	
	public List<String> getElements() {
		return elements;
	}
	
	public Map<Integer, DrMemoryError> getErrors() {
		return errors;
	}
	
	
	public static final Pattern rx_version = Pattern.compile( "^.*version (.*?) built on (.*?)$", Pattern.MULTILINE );
	public static final Pattern rx_command = Pattern.compile( "^Application cmdline: \"(.*)\"\\s*$", Pattern.MULTILINE );
	//public static final Pattern rx_duplicates = Pattern.compile( "^DUPLICATE ERROR COUNTS:\\s*$(.*?)^\\s*$", Pattern.MULTILINE | Pattern.DOTALL );
	public static final Pattern rx_duplicates = Pattern.compile( "^DUPLICATE ERROR COUNTS:\\s*$", Pattern.MULTILINE | Pattern.DOTALL );
	
	public static final Pattern rx_duplicates_finder = Pattern.compile( "^\\s*Error #\\s*(\\d+):\\s*(\\d+)\\s*$", Pattern.MULTILINE | Pattern.DOTALL );

	public static final int __TOP_COUNT = 3;

	public static DrMemoryResult parse( File file ) throws IOException, InvalidInputException {
		String[] top = getTop( file );
		
		DrMemoryResult result = new DrMemoryResult();
		
		Matcher mv = rx_version.matcher( top[0] );
		result.version = "0";
		result.date = "?";
		if( mv.find() ) {
			result.version = mv.group( 1 );
			result.date = mv.group( 2 );
		} else {
			logger.error( "Possibly a style change!!!" );
			logger.error( "Could not get version" );
		}
		
		Matcher mcmd = rx_command.matcher( top[1] );
		result.cmd = "?";
		if( mcmd.find() ) {
			result.cmd = mcmd.group( 1 );
		} else {
			logger.error( "Possibly a style change!!!" );
			logger.error( "Could not get command line" );
		}
		
		/* Get the elements and retrieve the individual errors */
		result.elements = DrMemoryResult.getElements( file );
		
		for( String e : result.elements ) {
			if( e.startsWith( "Error #" ) ) {
				//logger.debug( "HERE" );
				DrMemoryError error = DrMemoryError.parse( e );
				result.errors.put( error.getIdentifier(), error );
				
				continue;
			}
			
			/* Get duplicate count */
			//logger.debug( "\n\nE IS: " + e );
			Matcher m = rx_duplicates.matcher( e );
			if( m.find() ) {
				logger.debug( "Found duplicates" );
				getDuplicates( result, e );
			}
		}
		
		
		return result;
	}
	
	
	public static void getDuplicates( DrMemoryResult result, String duplicates ) {
		Matcher m = rx_duplicates_finder.matcher( duplicates );
		
		while( m.find() ) {
			Integer id = Integer.parseInt( m.group( 1 ) );
			int cnt = Integer.parseInt( m.group( 2 ) );
			
			//logger.debug( "Setting " + cnt + " duplicate" + ( cnt == 1 ? "" : "s" ) + " to " + id );
			
			try {
				result.getErrors().get( id ).setDuplicates( cnt );
			} catch( Exception e ) {
				logger.warning( "Unable to set " + cnt + " duplicate" + ( cnt == 1 ? "" : "s" ) + " to " + id );
			}
		}
	}

	
	public static List<String> getElements( File file ) throws IOException {
		FileReader fr = new FileReader( file );
		BufferedReader br = new BufferedReader( fr );
		List<String> list = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		String line;
		int cnt = 0;
		while( ( line = br.readLine() ) != null ) {
			if( cnt > ( __TOP_COUNT ) ) {
				
				if( line.matches( "^\\s*$" ) ) {
					list.add( sb.toString() );
					sb.setLength( 0 );
					
				} else {
					sb.append( line + "\n" );
				}
			}			
			
			cnt++;
		}
		
		return list;
	}
	
	
	public static String[] getTop( File file ) throws IOException {
		FileReader fr = new FileReader( file );
		BufferedReader br = new BufferedReader( fr );
		String[] buffer = new String[__TOP_COUNT];
		
		for( int i = 0 ; i < __TOP_COUNT ; ++i ) {
			buffer[i] = br.readLine();
		}
		
		return buffer;
	}
}
