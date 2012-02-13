package net.praqma.drmemory.exceptions;

public class DrMemoryException extends Exception {

	public DrMemoryException() {
		super();
	}
	
	public DrMemoryException( String s ) {
		super( s );
	}
	
	public DrMemoryException( Exception e ) {
		super( e );
	}
	
	public DrMemoryException( String s, Exception e ) {
		super( s, e );
	}
}
