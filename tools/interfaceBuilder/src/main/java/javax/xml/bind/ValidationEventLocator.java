package javax.xml.bind;

public interface ValidationEventLocator {
	java.net.URL getURL();
	
	int getOffset();
	
	int getLineNumber();
	
	int getColumnNumber();
	
	java.lang.Object getObject();
	
	org.w3c.dom.Node getNode();
}
