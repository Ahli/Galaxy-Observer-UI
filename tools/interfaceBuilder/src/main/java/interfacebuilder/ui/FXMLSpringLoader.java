// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * A FXMLLoader with its ControllerFactory set to be the ApplicationContext's Bean. Controllers are required to be jpa
 * now.
 */
public class FXMLSpringLoader extends FXMLLoader implements ApplicationContextAware {
	private boolean contextSet;
	
	public FXMLSpringLoader(final ApplicationContext applicationContext) {
		setApplicationContext(applicationContext);
	}
	
	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		setControllerFactory(applicationContext::getBean);
		setResources(
				new MessageSourceResourceBundle(applicationContext.getBean(MessageSource.class), Locale.getDefault()));
		contextSet = true;
	}
	
	@Override
	public <T> T load(final InputStream inputStream) throws IOException {
		if (contextSet) {
			return super.load(inputStream);
		} else {
			throw new IllegalStateException(
					"Spring context was not set. Use 'new FXMLLoader', if no Spring Bean is desired.");
		}
	}
	
}
