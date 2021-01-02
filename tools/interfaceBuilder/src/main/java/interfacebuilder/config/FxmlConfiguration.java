// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.config;

import interfacebuilder.base_ui.BaseUiService;
import interfacebuilder.build.MpqBuilderService;
import interfacebuilder.compile.CompileService;
import interfacebuilder.compress.GameService;
import interfacebuilder.integration.FileService;
import interfacebuilder.projects.ProjectService;
import interfacebuilder.ui.AppController;
import interfacebuilder.ui.browse.BrowseController;
import interfacebuilder.ui.browse.BrowseTabController;
import interfacebuilder.ui.home.AddProjectController;
import interfacebuilder.ui.home.AddProjectDialogController;
import interfacebuilder.ui.home.HomeController;
import interfacebuilder.ui.home.NewProjectController;
import interfacebuilder.ui.home.NewProjectDialogController;
import interfacebuilder.ui.home.ViewRuleSetController;
import interfacebuilder.ui.navigation.NavigationController;
import interfacebuilder.ui.progress.BaseUiExtractionController;
import interfacebuilder.ui.progress.CompressionMiningController;
import interfacebuilder.ui.progress.TabPaneController;
import interfacebuilder.ui.settings.SettingsCommandLineToolController;
import interfacebuilder.ui.settings.SettingsController;
import interfacebuilder.ui.settings.SettingsGamesPathsController;
import interfacebuilder.ui.settings.SettingsGuiToolController;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ForkJoinPool;

@Lazy
@Configuration
public class FxmlConfiguration {
	
	// Prototype Scope should be used for everything but controllers that should remain alive long
	// If it is not a prototype, the bean will not be garbage collected when not used anymore.
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected ViewRuleSetController viewRuleSetController(final ProjectService projectService) {
		return new ViewRuleSetController(projectService);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected HomeController homeController(
			final ApplicationContext appContext,
			final ProjectService projectService,
			final FileService fileService,
			final GameService gameService,
			final AppController appController,
			final NavigationController navigationController) {
		return new HomeController(
				appContext,
				projectService,
				fileService,
				gameService,
				tabPaneController(appController),
				navigationController);
	}
	
	@Bean
	protected TabPaneController tabPaneController(final AppController appController) {
		return new TabPaneController(appController);
	}
	
	@Bean
	protected NavigationController navigationController(final ApplicationContext appContext) {
		return new NavigationController(appContext);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected SettingsCommandLineToolController settingsCommandLineToolController(final ConfigService configService) {
		return new SettingsCommandLineToolController(configService);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected SettingsController settingsController(final ApplicationContext appContext) {
		return new SettingsController(appContext);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected SettingsGamesPathsController settingsGamesPathsController(final ConfigService configService) {
		return new SettingsGamesPathsController(configService);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected SettingsGuiToolController settingsGuiToolController(final ConfigService configService) {
		return new SettingsGuiToolController(configService);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected NewProjectController newProjectController(
			final ProjectService projectService, final FileService fileService) {
		return new NewProjectController(projectService, fileService);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected NewProjectDialogController newProjectDialogController() {
		return new NewProjectDialogController();
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected AddProjectController addProjectController(
			final ProjectService projectService, final FileService fileService) {
		return new AddProjectController(projectService, fileService);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected AddProjectDialogController addProjectDialogController() {
		return new AddProjectDialogController();
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected CompressionMiningController compressionMiningController(
			final GameService gameService,
			final ProjectService projectService,
			final ConfigService configService,
			final FileService fileService,
			final ForkJoinPool executor) {
		return new CompressionMiningController(gameService, projectService, configService, fileService, executor);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected BrowseController browseController(
			final ApplicationContext appContext,
			final BaseUiService baseUiService,
			final ConfigService configService,
			final MpqBuilderService mpqBuilderService,
			final ProjectService projectService,
			final GameService gameService,
			final CompileService compileService,
			final FileService fileService,
			final NavigationController navigationController,
			final AppController appController,
			final ForkJoinPool executor) {
		return new BrowseController(
				appContext,
				baseUiService,
				configService,
				mpqBuilderService,
				projectService,
				gameService,
				compileService,
				fileService,
				navigationController,
				appController,
				executor);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected BrowseTabController browseTabController() {
		return new BrowseTabController();
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected BaseUiExtractionController baseUiExtractionController(
			final BaseUiService baseUiService,
			final GameService gameServic,
			final NavigationController navigationController,
			final ForkJoinPool executor,
			final AppController appController) {
		return new BaseUiExtractionController(baseUiService, gameServic, navigationController, executor, appController);
	}
}
