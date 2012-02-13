package net.praqma.drmemory.errors;

import net.praqma.drmemory.DrMemoryError;
import net.praqma.util.debug.Logger;

public class UninitializedRead extends DrMemoryError {

	private static Logger logger = Logger.getLogger();
	
	@Override
	public void parseHeader( String header ) {
		this.header = header;
	}
	
	@Override
	public void onAddNote( Note note ) {
		
	}

}
