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
	
	public static class ErrorSummary {
		public int unique = 0;
		public int total = 0;
		public long info = 0;
	}
	
	ErrorSummary unaddressableAccesses = new ErrorSummary();
	ErrorSummary uninitializedAccess = new ErrorSummary();
	ErrorSummary invalidHeapArguments = new ErrorSummary();
	ErrorSummary warnings = new ErrorSummary();
	ErrorSummary bytesOfLeaks = new ErrorSummary();
	ErrorSummary bytesOfPossibleLeaks = new ErrorSummary();
	
	int stillReachableAllocations = 0;
	
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
	
	


	public ErrorSummary getUnaddressableAccesses() {
		return unaddressableAccesses;
	}

	public ErrorSummary getUninitializedAccess() {
		return uninitializedAccess;
	}

	public ErrorSummary getInvalidHeapArguments() {
		return invalidHeapArguments;
	}

	public ErrorSummary getWarnings() {
		return warnings;
	}

	public ErrorSummary getBytesOfLeaks() {
		return bytesOfLeaks;
	}

	public ErrorSummary getBytesOfPossibleLeaks() {
		return bytesOfPossibleLeaks;
	}

	public int getStillReachableAllocations() {
		return stillReachableAllocations;
	}




	public static final Pattern rx_version = Pattern.compile( "^.*version (.*?) built on (.*?)$", Pattern.MULTILINE );
	public static final Pattern rx_command = Pattern.compile( "^Application cmdline: \"(.*)\"\\s*$", Pattern.MULTILINE );
	//public static final Pattern rx_duplicates = Pattern.compile( "^DUPLICATE ERROR COUNTS:\\s*$(.*?)^\\s*$", Pattern.MULTILINE | Pattern.DOTALL );
	public static final Pattern rx_duplicates = Pattern.compile( "^DUPLICATE ERROR COUNTS:\\s*$", Pattern.MULTILINE | Pattern.DOTALL );
	
	public static final Pattern rx_duplicates_finder = Pattern.compile( "^\\s*Error #\\s*(\\d+):\\s*(\\d+)\\s*$", Pattern.MULTILINE | Pattern.DOTALL );
	public static final Pattern rx_errors_found = Pattern.compile( "^\\s*ERRORS FOUND:\\s*$", Pattern.MULTILINE | Pattern.DOTALL );

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
			Matcher m = rx_duplicates.matcher( e );
			if( m.find() ) {
				logger.debug( "Found duplicates" );
				getDuplicates( result, e );
				
				continue;
			}
			
			/* Get error summary */
			Matcher sum = rx_errors_found.matcher( e );
			if( sum.find() ) {
				logger.debug( "Found error summary" );
				getErrorSummary( result, e );
				
				continue;
			}
			
			logger.debug( "\n\nE IS: " + e );
			
			
		}
		
		
		return result;
	}
	
	public static final Pattern rx_error_unaddr = Pattern.compile( "^\\s*(\\d+) unique,\\s*(\\d+) total unaddressable access\\(es\\)\\s*$", Pattern.MULTILINE );
	public static final Pattern rx_error_uninit = Pattern.compile( "^\\s*(\\d+) unique,\\s*(\\d+) total uninitialized access\\(es\\)\\s*$", Pattern.MULTILINE );
	public static final Pattern rx_error_unvali = Pattern.compile( "^\\s*(\\d+) unique,\\s*(\\d+) total invalid heap argument\\(s\\)\\s*$", Pattern.MULTILINE );
	public static final Pattern rx_error_warnin = Pattern.compile( "^\\s*(\\d+) unique,\\s*(\\d+) total warning\\(s\\)\\s*$", Pattern.MULTILINE );
	
	public static final Pattern rx_error_leaks  = Pattern.compile( "^\\s*(\\d+) unique,\\s*(\\d+) total,\\s*(\\d+) byte\\(s\\) of leak\\(s\\)\\s*$", Pattern.MULTILINE );
	public static final Pattern rx_error_possib = Pattern.compile( "^\\s*(\\d+) unique,\\s*(\\d+) total,\\s*(\\d+) byte\\(s\\) of possible leak\\(s\\)\\s*$", Pattern.MULTILINE );
	
	public static void getErrorSummary( DrMemoryResult result, String summary ) {
		logger.debug( summary );
		
		
		Matcher m_unaddr = rx_error_unaddr.matcher( summary );
		if( m_unaddr.find() ) {
			logger.debug( "Found unaddressble accesses!" );
			ErrorSummary es = new ErrorSummary();
			es.unique = Integer.parseInt( m_unaddr.group( 1 ) );
			es.total = Integer.parseInt( m_unaddr.group( 2 ) );
			result.unaddressableAccesses = es;
		}
		
		
		Matcher m_uninit = rx_error_uninit.matcher( summary );
		if( m_uninit.find() ) {
			logger.debug( "Found uninitialized accesses!" );
			ErrorSummary es = new ErrorSummary();
			es.unique = Integer.parseInt( m_uninit.group( 1 ) );
			es.total = Integer.parseInt( m_uninit.group( 2 ) );
			result.uninitializedAccess = es;
		}
		
		
		Matcher m_invali = rx_error_unvali.matcher( summary );
		if( m_invali.find() ) {
			logger.debug( "Found invalid heap argument!" );
			ErrorSummary es = new ErrorSummary();
			es.unique = Integer.parseInt( m_invali.group( 1 ) );
			es.total = Integer.parseInt( m_invali.group( 2 ) );
			result.invalidHeapArguments = es;
		}
		
		
		Matcher m_warnin = rx_error_warnin.matcher( summary );
		if( m_warnin.find() ) {
			logger.debug( "Found warning!" );
			ErrorSummary es = new ErrorSummary();
			es.unique = Integer.parseInt( m_warnin.group( 1 ) );
			es.total = Integer.parseInt( m_warnin.group( 2 ) );
			result.warnings = es;
		}
		

		Matcher m_leaks = rx_error_leaks.matcher( summary );
		if( m_leaks.find() ) {
			logger.debug( "Found leaks!" );
			ErrorSummary es = new ErrorSummary();
			es.unique = Integer.parseInt( m_leaks.group( 1 ) );
			es.total = Integer.parseInt( m_leaks.group( 2 ) );
			es.info =  Integer.parseInt( m_leaks.group( 3 ) );
			result.bytesOfLeaks = es;
		}
		
		
		Matcher m_possib = rx_error_possib.matcher( summary );
		if( m_possib.find() ) {
			logger.debug( "Found possible leaks!" );
			ErrorSummary es = new ErrorSummary();
			es.unique = Integer.parseInt( m_possib.group( 1 ) );
			es.total = Integer.parseInt( m_possib.group( 2 ) );
			es.info =  Integer.parseInt( m_possib.group( 3 ) );
			result.bytesOfPossibleLeaks = es;
		}
		
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
		
		if( sb.length() > 0 ) {
			list.add( sb.toString() );
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
