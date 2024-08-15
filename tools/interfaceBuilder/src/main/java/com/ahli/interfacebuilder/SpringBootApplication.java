// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder;

import com.ahli.interfacebuilder.config.AppConfiguration;
import com.ahli.interfacebuilder.config.FxmlConfiguration;
import com.ahli.interfacebuilder.integration.CommandLineParams;
import com.ahli.interfacebuilder.integration.ipc.IpcCommunication;
import com.ahli.interfacebuilder.integration.ipc.IpcServerThread;
import com.ahli.interfacebuilder.integration.ipc.TcpIpSocketCommunication;
import com.ahli.interfacebuilder.integration.ipc.UnixDomainSocketCommunication;
import javafx.application.Application;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static com.ahli.interfacebuilder.integration.h2.H2Integration.migrateH2Db;
import static com.ahli.interfacebuilder.ui.AppController.FATAL_ERROR;

// start with VM parameter if java modules are used: -add-opens=javafx.controls/javafx.scene.control=interfacex.builder

@Log4j2
@Import({ AppConfiguration.class, FxmlConfiguration.class, ConfigurationPropertiesAutoConfiguration.class,
		MessageSourceAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class, DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class, TransactionAutoConfiguration.class, FlywayAutoConfiguration.class })
@EnableAutoConfiguration(
		//		exclude = {
		//		// matched
		//		SslAutoConfiguration.class, TaskSchedulingAutoConfiguration.class, TaskExecutionAutoConfiguration.class,
		//		SqlInitializationAutoConfiguration.class, TransactionManagerCustomizationAutoConfiguration.class,
		//		PropertyPlaceholderAutoConfiguration.class, PersistenceExceptionTranslationAutoConfiguration.class,
		//		LifecycleAutoConfiguration.class, JtaAutoConfiguration.class, JdbcTemplateAutoConfiguration.class,
		//		GsonAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class,
		//		ApplicationAvailabilityAutoConfiguration.class, AopAutoConfiguration.class,
		//		// unmatched
		//		ActiveMQAutoConfiguration.class, ArtemisAutoConfiguration.class, BatchAutoConfiguration.class,
		//		CacheAutoConfiguration.class, CassandraAutoConfiguration.class, CassandraDataAutoConfiguration.class,
		//		CassandraReactiveDataAutoConfiguration.class, CassandraReactiveRepositoriesAutoConfiguration.class,
		//		CassandraRepositoriesAutoConfiguration.class, ClientHttpConnectorAutoConfiguration.class,
		//		CodecsAutoConfiguration.class, CouchbaseAutoConfiguration.class, CouchbaseDataAutoConfiguration.class,
		//		CouchbaseReactiveDataAutoConfiguration.class, CouchbaseReactiveRepositoriesAutoConfiguration.class,
		//		CouchbaseRepositoriesAutoConfiguration.class, DispatcherServletAutoConfiguration.class,
		//		ElasticsearchClientAutoConfiguration.class, ElasticsearchDataAutoConfiguration.class,
		//		ElasticsearchRepositoriesAutoConfiguration.class, ElasticsearchRestClientAutoConfiguration.class,
		//		EmbeddedLdapAutoConfiguration.class, EmbeddedWebServerFactoryCustomizerAutoConfiguration.class,
		//		ErrorMvcAutoConfiguration.class, ErrorWebFluxAutoConfiguration.class, FreeMarkerAutoConfiguration.class,
		//		GraphQlAutoConfiguration.class, GraphQlQueryByExampleAutoConfiguration.class,
		//		GraphQlQuerydslAutoConfiguration.class, GraphQlRSocketAutoConfiguration.class,
		//		GraphQlReactiveQueryByExampleAutoConfiguration.class, GraphQlReactiveQuerydslAutoConfiguration.class,
		//		GraphQlWebFluxAutoConfiguration.class, GraphQlWebFluxSecurityAutoConfiguration.class,
		//		GraphQlWebMvcAutoConfiguration.class, GraphQlWebMvcSecurityAutoConfiguration.class,
		//		GroovyTemplateAutoConfiguration.class, H2ConsoleAutoConfiguration.class, HazelcastAutoConfiguration.class,
		//		HazelcastJpaDependencyAutoConfiguration.class, HttpEncodingAutoConfiguration.class,
		//		HttpHandlerAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
		//		HypermediaAutoConfiguration.class, InfluxDbAutoConfiguration.class, IntegrationAutoConfiguration.class,
		//		JacksonAutoConfiguration.class, JdbcClientAutoConfiguration.class, JdbcRepositoriesAutoConfiguration.class,
		//		//		JerseyAutoConfiguration.class, // can't be excluded without issues for some reason
		//		JmsAutoConfiguration.class, JmxAutoConfiguration.class, JndiConnectionFactoryAutoConfiguration.class,
		//		JndiDataSourceAutoConfiguration.class, JooqAutoConfiguration.class, JsonbAutoConfiguration.class,
		//		KafkaAutoConfiguration.class, LdapAutoConfiguration.class, LdapRepositoriesAutoConfiguration.class,
		//		LiquibaseAutoConfiguration.class, MailSenderAutoConfiguration.class, MailSenderValidatorAutoConfiguration.class,
		//		MongoAutoConfiguration.class, MongoDataAutoConfiguration.class, MongoReactiveAutoConfiguration.class,
		//		MongoReactiveDataAutoConfiguration.class, MongoReactiveRepositoriesAutoConfiguration.class,
		//		MongoRepositoriesAutoConfiguration.class, MultipartAutoConfiguration.class, MustacheAutoConfiguration.class,
		//		Neo4jAutoConfiguration.class, Neo4jDataAutoConfiguration.class, Neo4jReactiveDataAutoConfiguration.class,
		//		Neo4jReactiveRepositoriesAutoConfiguration.class, Neo4jRepositoriesAutoConfiguration.class,
		//		NettyAutoConfiguration.class, OAuth2AuthorizationServerAutoConfiguration.class,
		//		OAuth2AuthorizationServerJwtAutoConfiguration.class, OAuth2ClientAutoConfiguration.class,
		//		OAuth2ResourceServerAutoConfiguration.class, ProjectInfoAutoConfiguration.class, PulsarAutoConfiguration.class,
		//		PulsarReactiveAutoConfiguration.class, QuartzAutoConfiguration.class, R2dbcAutoConfiguration.class,
		//		R2dbcDataAutoConfiguration.class, R2dbcRepositoriesAutoConfiguration.class,
		//		R2dbcTransactionManagerAutoConfiguration.class, RSocketGraphQlClientAutoConfiguration.class,
		//		RSocketMessagingAutoConfiguration.class, RSocketRequesterAutoConfiguration.class,
		//		RSocketSecurityAutoConfiguration.class, RSocketServerAutoConfiguration.class,
		//		RSocketStrategiesAutoConfiguration.class, RabbitAutoConfiguration.class,
		//		ReactiveElasticsearchClientAutoConfiguration.class, ReactiveElasticsearchRepositoriesAutoConfiguration.class,
		//		ReactiveMultipartAutoConfiguration.class, ReactiveOAuth2ClientAutoConfiguration.class,
		//		ReactiveOAuth2ResourceServerAutoConfiguration.class, ReactiveSecurityAutoConfiguration.class,
		//		ReactiveUserDetailsServiceAutoConfiguration.class, ReactiveWebServerFactoryAutoConfiguration.class,
		//		ReactorAutoConfiguration.class, RedisAutoConfiguration.class, RedisReactiveAutoConfiguration.class,
		//		RedisRepositoriesAutoConfiguration.class, RepositoryRestMvcAutoConfiguration.class,
		//		RestClientAutoConfiguration.class, RestTemplateAutoConfiguration.class,
		//		Saml2RelyingPartyAutoConfiguration.class, SecurityAutoConfiguration.class,
		//		SecurityFilterAutoConfiguration.class, SendGridAutoConfiguration.class,
		//		ServletWebServerFactoryAutoConfiguration.class, SessionAutoConfiguration.class,
		//		SpringApplicationAdminJmxAutoConfiguration.class, SpringDataWebAutoConfiguration.class,
		//		ThymeleafAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class, ValidationAutoConfiguration.class,
		//		WebClientAutoConfiguration.class, WebFluxAutoConfiguration.class, WebMvcAutoConfiguration.class,
		//		WebServiceTemplateAutoConfiguration.class, WebServicesAutoConfiguration.class,
		//		WebSessionIdResolverAutoConfiguration.class, WebSocketMessagingAutoConfiguration.class,
		//		WebSocketReactiveAutoConfiguration.class, WebSocketServletAutoConfiguration.class },
		excludeName = { "org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration",
				"org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration",
				"org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration",
				"org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration",
				"org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizationAutoConfiguration",
				"org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration",
				"org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration",
				"org.springframework.boot.autoconfigure.context.LifecycleAutoConfiguration",
				"org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration",
				"org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration",
				"org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration",
				"org.springframework.boot.autoconfigure.availability.ApplicationAvailabilityAutoConfiguration",
				"org.springframework.boot.autoconfigure.aop.AopAutoConfiguration",
				"org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration",
				"org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration",
				"org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration",
				"org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration",
				"org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration",
				"org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration",
				"org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.couchbase.CouchbaseDataAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveDataAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.couchbase.CouchbaseRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration",
				"org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration",
				"org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration",
				"org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration",
				"org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration",
				"org.springframework.boot.autoconfigure.graphql.data.GraphQlQueryByExampleAutoConfiguration",
				"org.springframework.boot.autoconfigure.graphql.data.GraphQlQuerydslAutoConfiguration",
				"org.springframework.boot.autoconfigure.graphql.rsocket.GraphQlRSocketAutoConfiguration",
				"org.springframework.boot.autoconfigure.graphql.data.GraphQlReactiveQueryByExampleAutoConfiguration",
				"org.springframework.boot.autoconfigure.graphql.data.GraphQlReactiveQuerydslAutoConfiguration",
				"org.springframework.boot.autoconfigure.graphql.reactive.GraphQlWebFluxAutoConfiguration",
				"org.springframework.boot.autoconfigure.graphql.security.GraphQlWebFluxSecurityAutoConfiguration",
				"org.springframework.boot.autoconfigure.graphql.servlet.GraphQlWebMvcAutoConfiguration",
				"org.springframework.boot.autoconfigure.graphql.security.GraphQlWebMvcSecurityAutoConfiguration",
				"org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration",
				"org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration",
				"org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration",
				"org.springframework.boot.autoconfigure.hazelcast.HazelcastJpaDependencyAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration",
				"org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration",
				"org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration",
				"org.springframework.boot.autoconfigure.influx.InfluxDbAutoConfiguration",
				"org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration",
				"org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration",
				"org.springframework.boot.autoconfigure.jdbc.JdbcClientAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration",
				"org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration",
				"org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration",
				"org.springframework.boot.autoconfigure.jdbc.JndiDataSourceAutoConfiguration",
				"org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration",
				"org.springframework.boot.autoconfigure.jsonb.JsonbAutoConfiguration",
				"org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
				"org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.ldap.LdapRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration",
				"org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration",
				"org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration",
				"org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
				"org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration",
				"org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration",
				"org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveDataAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.neo4j.Neo4jRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.netty.NettyAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerJwtAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
				"org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration",
				"org.springframework.boot.autoconfigure.pulsar.PulsarAutoConfiguration",
				"org.springframework.boot.autoconfigure.pulsar.PulsarReactiveAutoConfiguration",
				"org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration",
				"org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration",
				"org.springframework.boot.autoconfigure.graphql.rsocket.RSocketGraphQlClientAutoConfiguration",
				"org.springframework.boot.autoconfigure.rsocket.RSocketMessagingAutoConfiguration",
				"org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.rsocket.RSocketSecurityAutoConfiguration",
				"org.springframework.boot.autoconfigure.rsocket.RSocketServerAutoConfiguration",
				"org.springframework.boot.autoconfigure.rsocket.RSocketStrategiesAutoConfiguration",
				"org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration",
				"org.springframework.boot.autoconfigure.elasticsearch.ReactiveElasticsearchClientAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.reactive.ReactiveMultipartAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration",
				"org.springframework.boot.autoconfigure.reactor.ReactorAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
				"org.springframework.boot.autoconfigure.sendgrid.SendGridAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration",
				"org.springframework.boot.autoconfigure.session.SessionAutoConfiguration",
				"org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration",
				"org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration",
				"org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
				"org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration",
				"org.springframework.boot.autoconfigure.webservices.client.WebServiceTemplateAutoConfiguration",
				"org.springframework.boot.autoconfigure.webservices.WebServicesAutoConfiguration",
				"org.springframework.boot.autoconfigure.web.reactive.WebSessionIdResolverAutoConfiguration",
				"org.springframework.boot.autoconfigure.websocket.servlet.WebSocketMessagingAutoConfiguration",
				"org.springframework.boot.autoconfigure.websocket.reactive.WebSocketReactiveAutoConfiguration",
				"org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration" })
public final class SpringBootApplication {
	
	public static final int INTER_PROCESS_COMMUNICATION_PORT = 12317;
	private static final boolean USE_DOMAIN_SOCKET = true;
	
	public SpringBootApplication() {
		// explicit constructor
	}
	
	@SuppressWarnings("java:S2095") //
	public static void main(final String[] args) {
		try {
			if (getIpc().isAvailable()) {
				if (!actAsServer(args)) {
					log.warn("Failed to create Server Thread. Starting App without it...");
					launch(args, null);
				}
			} else {
				log.info("App already running. Passing over command line arguments.");
				actAsClient(args);
			}
		} catch (final InterruptedException e) {
			log.error("Interrupted server/client entry point", e);
			Thread.currentThread().interrupt();
		} catch (final SpringApplication.AbandonedRunException e) {
			throw e;
		} catch (final Exception e) {
			log.error(FATAL_ERROR, e);
		}
	}
	
	private static void actAsClient(final String[] args) {
		try (final IpcCommunication interProcessCommunication = getIpc()) {
			if (!interProcessCommunication.sendToServer(args)) {
				log.error("InterProcessCommunication as Client failed.");
			}
		} catch (final Exception e) {
			log.error("ClosingInterProcessCommunication as Client failed.", e);
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
		} catch (final InterruptedException e) {
			log.error("Interrupted during H2 DB migration check", e);
			Thread.currentThread().interrupt();
		} catch (final IOException e) {
			log.error("H2 DB migration check failed", e);
		} catch (final Exception e) {
			log.error("Closing InterProcessCommunication failed", e);
		}
		return false;
	}
	
	private static Path getIpcPath() {
		return Path.of(
				System.getProperty("user.home") + File.separator + ".GalaxyObsUI" + File.separator + "ipc.socket");
	}
	
	private static void launch(final String[] args, @Nullable final IpcServerThread serverThread)
			throws IOException, InterruptedException {
		
		log.trace("System's Log4j2 Configuration File: {}", () -> System.getProperty("log4j.configurationFile"));
		log.info(
				"Launch arguments: {}\nMax Heap Space: {}mb.",
				() -> Arrays.toString(args),
				() -> Runtime.getRuntime().maxMemory() / 1_048_576L);
		
		migrateH2Db();
		
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
	
	private static void launchNoGui(final String[] args, @Nullable final IpcServerThread serverThread) {
		new NoGuiApplication(args, serverThread);
	}
	
	private static void launchGui(final String[] args, @Nullable final IpcServerThread serverThread) {
		setJavaFxPreloader(AppPreloader.class.getCanonicalName());
		Application.launch(JavafxApplication.class, argsWithServerThread(args, serverThread));
	}
	
	private static void setJavaFxPreloader(final String canonicalPath) {
		System.setProperty("javafx.preloader", canonicalPath);
	}
	
	private static String[] argsWithServerThread(final String[] args, @Nullable final IpcServerThread serverThread) {
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
