package javax.xml.bind;

public interface ValidationEventLocator {
	public java.net.URL getURL();
	
	public int getOffset();
	
	public int getLineNumber();
	
	public int getColumnNumber();
	
	public java.lang.Object getObject();
	
	public org.w3c.dom.Node getNode();
}