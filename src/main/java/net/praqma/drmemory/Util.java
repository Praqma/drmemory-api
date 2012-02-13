package net.praqma.drmemory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class Util {
	
	
	public static String toString( URL u ) throws IOException {
		return toString( new File( u.getFile() ) );
	}
	
	public static String toString( File file ) throws IOException {
		
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
