// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder;

import com.ahli.interfacebuilder.integration.ipc.TcpIpSocketCommunication;
import com.ahli.interfacebuilder.integration.ipc.UnixDomainSocketCommunication;
import com.ahli.interfacebuilder.config.AppConfiguration;
import com.ahli.interfacebuilder.config.FxmlConfiguration;
import com.ahli.interfacebuilder.integration.CommandLineParams;
import com.ahli.interfacebuilder.integration.ipc.IpcCommunication;
import com.ahli.interfacebuilder.integration.ipc.IpcServerThread;
import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import static com.ahli.interfacebuilder.ui.AppController.FATAL_ERROR;

// start with VM parameter if java modules are used: -add-opens=javafx.controls/javafx.scene.control=interfacex.builder

@Import({ AppConfiguration.class, FxmlConfiguration.class, ConfigurationPropertiesAutoConfiguration.class,
		MessageSourceAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class, DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class, TransactionAutoConfiguration.class, FlywayAutoConfiguration.class })
public final class SpringBootApplication {
	
	public static final int INTER_PROCESS_COMMUNICATION_PORT = 12317;
	private static final boolean USE_DOMAIN_SOCKET = false;
	private static final Logger logger = LogManager.getLogger(SpringBootApplication.class);
	
	@SuppressWarnings("java:S2095") //
	public static void main(final String[] args) {
		try {
			if (getIpc().isAvailable()) {
				if (!actAsServer(args)) {
					logger.warn("Failed to create Server Thread. Starting App without it...");
					launch(args, null);
				}
			} else {
				logger.info("App already running. Passing over command line arguments.");
				actAsClient(args);
			}
		} catch (final Exception e) {
			logger.error(FATAL_ERROR, e);
		}
	}
	
	private static void actAsClient(final String[] args) {
		try (final IpcCommunication interProcessCommunication = getIpc()) {
			if (!interProcessCommunication.sendToServer(args)) {
				logger.error("InterProcessCommunication as Client failed.");
			}
		} catch (final Exception e) {
			logger.error("ClosingInterProcessCommunication as Client failed.", e);
		}
	}
	
	private static IpcCommunication getIpc() {
		return USE_DOMAIN_SOCKET ? new UnixDomainSocketCommunication(getIpcPath()) :
				new TcpIpSocketCommunication(INTER_PROCESS_COMMUNICATION_PORT);
	}
	
	private static boolean actAsServer(final String[] args) {
		try (final IpcCommunication interProcessCommunication = getIpc()) {
			final IpcServerThread serverThread = interProcessCommunication.actAsServer();
			launch(args, serverThread);
			return true;
		} catch (final Exception e) {
			logger.error("Closing InterProcessCommunication failed", e);
		}
		return false;
	}
	
	private static Path getIpcPath() {
		return Path.of(
				System.getProperty("user.home") + File.separator + ".GalaxyObsUI" + File.separator + "ipc.socket");
	}
	
	private static void launch(final String[] args, final IpcServerThread serverThread) {
		logger.trace("System's Log4j2 Configuration File: {}", () -> System.getProperty("log4j.configurationFile"));
		logger.info(
				"Launch arguments: {}\nMax Heap Space: {}mb.",
				() -> Arrays.toString(args),
				() -> Runtime.getRuntime().maxMemory() / 1_048_576L);
		
		boolean noGui = false;
		for (final String arg : args) {
			if ("--noGUI".equalsIgnoreCase(arg)) {
				noGui = true;
				break;
			}
		}
		if (noGui) {
			launchNoGui(args, serverThread);
		} else {
			launchGui(args, serverThread);
		}
	}
	
	private static void launchNoGui(final String[] args, final IpcServerThread serverThread) {
		new NoGuiApplication(args, serverThread);
	}
	
	private static void launchGui(final String[] args, final IpcServerThread serverThread) {
		setJavaFxPreloader(AppPreloader.class.getCanonicalName());
		Application.launch(JavafxApplication.class, argsWithServerThread(args, serverThread));
	}
	
	private static void setJavaFxPreloader(final String canonicalPath) {
		System.setProperty("javafx.preloader", canonicalPath);
	}
	
	private static String[] argsWithServerThread(final String[] args, final IpcServerThread serverThread) {
		if (serverThread != null) {
			final String[] destArray = Arrays.copyOf(args, args.length + 1);
			destArray[destArray.length - 1] =
					CommandLineParams.PARAM_PREFIX + CommandLineParams.SERVER + CommandLineParams.EQUAL +
							serverThread.getId();
			return destArray;
		}
		return args;
	}
	
}