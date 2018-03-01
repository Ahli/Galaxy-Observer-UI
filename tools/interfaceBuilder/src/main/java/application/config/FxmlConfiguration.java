package application.config;

import application.ui.home.AddProjectController;
import application.ui.home.HomeController;
import application.ui.home.ViewRuleSetController;
import application.ui.navigation.NavigationController;
import application.ui.progress.TabPaneController;
import application.ui.settings.SettingsCommandLineToolController;
import application.ui.settings.SettingsController;
import application.ui.settings.SettingsGamesPathsController;
import application.ui.settings.SettingsGuiToolController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Lazy
@Scope ("prototype")
@Configuration
public class FxmlConfiguration {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	//	@Bean
	//	public FXMLSpringLoader fxmlSpringLoader() {
	//		return new FXMLSpringLoader(applicationContext);
	//	}
	
	@Bean
	public ViewRuleSetController viewRuleSetController() {
		return new ViewRuleSetController();
	}
	
	@Bean
	public NavigationController navigationController() {
		return new NavigationController();
	}
	
	@Bean
	public HomeController homeController() {
		return new HomeController();
	}
	
	@Bean
	public SettingsCommandLineToolController settingsCommandLineToolController() {
		return new SettingsCommandLineToolController();
	}
	
	@Bean
	public SettingsController settingsController() {
		return new SettingsController();
	}
	
	@Bean
	public SettingsGamesPathsController settingsGamesPathsController() {
		return new SettingsGamesPathsController();
	}
	
	@Bean
	public SettingsGuiToolController settingsGuiToolController() {
		return new SettingsGuiToolController();
	}
	
	@Bean
	public TabPaneController tabPaneController() {
		return new TabPaneController();
	}
	
	@Bean
	public AddProjectController addProjectController() {
		return new AddProjectController();
	}
}
