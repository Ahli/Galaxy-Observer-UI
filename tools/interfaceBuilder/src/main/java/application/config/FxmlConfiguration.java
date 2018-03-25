package application.config;

import application.ui.home.AddProjectController;
import application.ui.home.HomeController;
import application.ui.home.ViewRuleSetController;
import application.ui.navigation.NavigationController;
import application.ui.progress.CompressionMiningController;
import application.ui.progress.TabPaneController;
import application.ui.settings.SettingsCommandLineToolController;
import application.ui.settings.SettingsController;
import application.ui.settings.SettingsGamesPathsController;
import application.ui.settings.SettingsGuiToolController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Lazy
@Configuration
public class FxmlConfiguration {
	
	@Bean
	@Scope ("prototype")
	public ViewRuleSetController viewRuleSetController() {
		return new ViewRuleSetController();
	}
	
	@Bean
	public NavigationController navigationController() {
		return new NavigationController();
	}
	
	@Bean
	@Scope ("prototype")
	public HomeController homeController() {
		return new HomeController();
	}
	
	@Bean
	@Scope ("prototype")
	public SettingsCommandLineToolController settingsCommandLineToolController() {
		return new SettingsCommandLineToolController();
	}
	
	@Bean
	@Scope ("prototype")
	public SettingsController settingsController() {
		return new SettingsController();
	}
	
	@Bean
	@Scope ("prototype")
	public SettingsGamesPathsController settingsGamesPathsController() {
		return new SettingsGamesPathsController();
	}
	
	@Bean
	@Scope ("prototype")
	public SettingsGuiToolController settingsGuiToolController() {
		return new SettingsGuiToolController();
	}
	
	@Bean
	public TabPaneController tabPaneController() {
		return new TabPaneController();
	}
	
	@Bean
	@Scope ("prototype")
	public AddProjectController addProjectController() {
		return new AddProjectController();
	}
	
	@Bean
	@Scope ("prototype")
	public CompressionMiningController compressionMiningController() {
		return new CompressionMiningController();
	}
}
