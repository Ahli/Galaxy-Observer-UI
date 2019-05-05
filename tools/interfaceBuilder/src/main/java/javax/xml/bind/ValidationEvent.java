// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
package javax.xml.bind;

public interface ValidationEvent {
	public static final int WARNING = 0;
	
	public static final int ERROR = 1;
	
	public static final int FATAL_ERROR = 2;
	
	int getSeverity();
	
	String getMessage();
	
	Throwable getLinkedException();
	
	ValidationEventLocator getLocator();
}
