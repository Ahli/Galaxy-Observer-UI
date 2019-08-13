// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.config;

import interfacebuilder.ui.browse.BrowseController;
import interfacebuilder.ui.browse.BrowseTabController;
import interfacebuilder.ui.home.AddProjectController;
import interfacebuilder.ui.home.AddProjectDialogController;
import interfacebuilder.ui.home.HomeController;
import interfacebuilder.ui.home.NewProjectController;
import interfacebuilder.ui.home.NewProjectDialogController;
import interfacebuilder.ui.home.ViewRuleSetController;
import interfacebuilder.ui.navigation.NavigationController;
import interfacebuilder.ui.progress.CompressionMiningController;
import interfacebuilder.ui.progress.TabPaneController;
import interfacebuilder.ui.settings.SettingsCommandLineToolController;
import interfacebuilder.ui.settings.SettingsController;
import interfacebuilder.ui.settings.SettingsGamesPathsController;
import interfacebuilder.ui.settings.SettingsGuiToolController;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Lazy
@Configuration
public class FxmlConfiguration {
	
	// Prototype Scope should be used for everything but controllers that should remain alive long
	// If it is not a prototype, the bean will not be garbage collected when not used anymore.
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected ViewRuleSetController viewRuleSetController() {
		return new ViewRuleSetController();
	}
	
	@Bean
	protected NavigationController navigationController() {
		return new NavigationController();
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected HomeController homeController() {
		return new HomeController();
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected SettingsCommandLineToolController settingsCommandLineToolController() {
		return new SettingsCommandLineToolController();
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected SettingsController settingsController() {
		return new SettingsController();
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected SettingsGamesPathsController settingsGamesPathsController() {
		return new SettingsGamesPathsController();
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected SettingsGuiToolController settingsGuiToolController() {
		return new SettingsGuiToolController();
	}
	
	@Bean
	protected TabPaneController tabPaneController() {
		return new TabPaneController();
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected NewProjectController newProjectController() {
		return new NewProjectController();
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected NewProjectDialogController newProjectDialogController() {
		return new NewProjectDialogController();
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected AddProjectController addProjectController() {
		return new AddProjectController();
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected AddProjectDialogController addProjectDialogController() {
		return new AddProjectDialogController();
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected CompressionMiningController compressionMiningController() {
		return new CompressionMiningController();
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected BrowseController browseController() {
		return new BrowseController();
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected BrowseTabController browseTabController() {
		return new BrowseTabController();
	}
}
