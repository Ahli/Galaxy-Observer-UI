// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.config;

import com.ahli.interfacebuilder.base_ui.BaseUiService;
import com.ahli.interfacebuilder.build.MpqBuilderService;
import com.ahli.interfacebuilder.compile.CompileService;
import com.ahli.interfacebuilder.compress.GameService;
import com.ahli.interfacebuilder.integration.FileService;
import com.ahli.interfacebuilder.projects.ProjectService;
import com.ahli.interfacebuilder.threads.CleaningForkJoinPool;
import com.ahli.interfacebuilder.ui.PrimaryStageHolder;
import com.ahli.interfacebuilder.ui.browse.BrowseController;
import com.ahli.interfacebuilder.ui.browse.BrowseTabController;
import com.ahli.interfacebuilder.ui.home.AddProjectController;
import com.ahli.interfacebuilder.ui.home.AddProjectDialogController;
import com.ahli.interfacebuilder.ui.home.HomeController;
import com.ahli.interfacebuilder.ui.home.NewProjectController;
import com.ahli.interfacebuilder.ui.home.NewProjectDialogController;
import com.ahli.interfacebuilder.ui.home.ViewRuleSetController;
import com.ahli.interfacebuilder.ui.navigation.NavigationController;
import com.ahli.interfacebuilder.ui.progress.BaseUiExtractionController;
import com.ahli.interfacebuilder.ui.progress.BuildUiController;
import com.ahli.interfacebuilder.ui.progress.CompressionMiningController;
import com.ahli.interfacebuilder.ui.progress.ProgressController;
import com.ahli.interfacebuilder.ui.progress.TabPaneController;
import com.ahli.interfacebuilder.ui.settings.SettingsCommandLineToolController;
import com.ahli.interfacebuilder.ui.settings.SettingsController;
import com.ahli.interfacebuilder.ui.settings.SettingsGamesPathsController;
import com.ahli.interfacebuilder.ui.settings.SettingsGuiToolController;
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
	
	public FxmlConfiguration() {
		// explicit constructor
	}
	
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
			final TabPaneController tabPaneController,
			final NavigationController navigationController,
			final MpqBuilderService mpqBuilderService) {
		return new HomeController(
				appContext,
				projectService,
				fileService,
				gameService,
				tabPaneController,
				navigationController,
				mpqBuilderService);
	}
	
	@Bean
	protected TabPaneController tabPaneController() {
		return new TabPaneController();
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
	protected SettingsGamesPathsController settingsGamesPathsController(
			final ConfigService configService, final FileService fileService, final GameService gameService) {
		return new SettingsGamesPathsController(configService, fileService, gameService);
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
			final CleaningForkJoinPool executor,
			final ProgressController progressController,
			final PrimaryStageHolder primaryStage) {
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
				executor,
				progressController,
				primaryStage);
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
			final CleaningForkJoinPool executor) {
		return new BaseUiExtractionController(baseUiService, gameServic, navigationController, executor);
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	protected BuildUiController buildUiController(final MpqBuilderService mpqBuilderService) {
		return new BuildUiController(mpqBuilderService);
	}
}
