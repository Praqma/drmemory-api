package net.praqma.drmemory.errors;

import net.praqma.drmemory.DrMemoryError;

public class UninitializedRead extends DrMemoryError {

	@Override
	public void parseHeader( int number, String header ) {
		this.number = number;
		this.header = header;
	}

}
