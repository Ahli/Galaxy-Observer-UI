// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
package javax.xml.bind;

public interface ValidationEventLocator {
	java.net.URL getURL();
	
	int getOffset();
	
	int getLineNumber();
	
	int getColumnNumber();
	
	java.lang.Object getObject();
	
	org.w3c.dom.Node getNode();
}
