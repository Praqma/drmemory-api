package net.praqma.drmemory;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestDrMemory {
	
	private static Logger logger = Logger.getLogger();
	private static Appender app;

	@BeforeClass
	public static void setupExecutable() {
		app = new ConsoleAppender();
		app.setMinimumLevel( LogLevel.DEBUG );
		Logger.addAppender( app );
		
		DrMemory.skipRun();
	}
	
	@AfterClass
	public static void end() {
		Logger.removeAppender( app );
	}
	
	@Test
	public void testCmdDefaultConstructor() throws IOException {
		DrMemory dm = new DrMemory();
		
		dm.start();
	}
	
	@Test
	public void testDrMemory1() throws IOException {
		DrMemory dm = new DrMemory( new File( "somefile.exe" ) );
		
		dm.start();
	}
	
	@Test
	public void testDrMemory2() throws IOException {
		DrMemory dm = new DrMemory( new File( "somefile.exe" ), "app args 1" );
		
		dm.start();
	}
	
	@Test
	public void testDrMemoryWithLogDir() throws IOException {
		DrMemory dm = new DrMemory( new File( "somefile.exe" ), "app args 1" );
		dm.setLogDir( new File( "mylogs/drmemory" ) );
		
		dm.start();
	}
}
