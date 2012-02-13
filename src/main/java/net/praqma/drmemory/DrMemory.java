package net.praqma.drmemory;

import java.io.File;
import java.io.IOException;

import net.praqma.util.debug.Logger;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLine;
import net.praqma.util.execute.CommandLineInterface;

public class DrMemory {

	private File application;
	private String parameters = "";
	
	private static File drmemory;
	
	private CommandLineInterface cli = CommandLine.getInstance();
	
	private static Logger logger = Logger.getLogger();

	public DrMemory() {
	}
	
	public DrMemory( File application ) {
		this.application = application;
	}
	
	public DrMemory( File application, String parameters ) {
		this.application = application;
		this.parameters = parameters;
	}
	
	public static void setExecutable( File drmemory ) {
		DrMemory.drmemory = drmemory;
	}
	
	public void start() throws IOException {
		String cmd = drmemory + " -- " + application + " " + parameters;
		
		CmdResult result = null;
		try {
			result = cli.run( cmd );
		} catch( Exception e ) {
			throw new IOException( "Unable to execute " + cmd + ": " + e.getMessage() );
		}
	}
	
	public void parse() {
		
	}
}
