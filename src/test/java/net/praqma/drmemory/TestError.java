package net.praqma.drmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import net.praqma.drmemory.exceptions.InvalidInputException;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.ConsoleAppender;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestError {
	
	private static Logger logger = Logger.getLogger();
	
	@BeforeClass
	public static void start() {
		ConsoleAppender app = new ConsoleAppender();
		app.setMinimumLevel( LogLevel.DEBUG );
		Logger.addAppender( app );
	}

	@Test
	public void blaha() throws IOException, InvalidInputException {
		URL s = TestError.class.getClassLoader().getResource( "output1.txt" );
		
		DrMemoryError error = DrMemoryError.parse( UrlToString( s ) );
		
		assertTrue( error.getIdentifier() == 1 );
		
		assertEquals( error.getheader(), "reading 0x02979808-0x02979810 8 byte(s)" );
	}
	
	
	
	private String UrlToString( URL u ) throws IOException {
		logger.debug( "URL: " + u );
		File file = new File( u.getFile() );
		FileReader fr = new FileReader( file );
		BufferedReader br = new BufferedReader( fr );
		StringBuffer sb = new StringBuffer();
		String line;
		while( ( line = br.readLine() ) != null) {
			sb.append( line + "\n" );
		}
		
		return sb.toString();
	}
}
