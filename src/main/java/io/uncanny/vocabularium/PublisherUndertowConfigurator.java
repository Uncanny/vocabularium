/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Uncanny Vocabularium project:
 *     http://uncanny.io/vocabularium/
 *
 *   Uncanny Software Projects
 *     http://uncanny.io/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2016-2017 Uncanny Software Projects.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package io.uncanny.vocabularium;

import static io.uncanny.vocabularium.handlers.MoreHandlers.contentNegotiation;
import static io.uncanny.vocabularium.handlers.MoreHandlers.methodController;
import static io.uncanny.vocabularium.handlers.MoreHandlers.*;
import static io.undertow.Handlers.path;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ldp4j.http.CharacterEncodings;
import org.ldp4j.http.MediaType;
import org.ldp4j.http.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.uncanny.vocabularium.config.ConfigurationException;
import io.uncanny.vocabularium.config.DocumentationConfig;
import io.uncanny.vocabularium.config.PublisherConfig;
import io.uncanny.vocabularium.handlers.ContentNegotiationHandler;
import io.uncanny.vocabularium.handlers.NegotiableContent;
import io.uncanny.vocabularium.model.Model;
import io.uncanny.vocabularium.model.Owner;
import io.uncanny.vocabularium.model.Site;
import io.uncanny.vocabularium.spi.DocumentationDeployment;
import io.uncanny.vocabularium.spi.DocumentationDeploymentFactory;
import io.uncanny.vocabularium.spi.DocumentationProvider;
import io.uncanny.vocabularium.spi.DocumentationProviderFactory;
import io.uncanny.vocabularium.util.ApplicationBootstrapException;
import io.uncanny.vocabularium.util.ApplicationInstance;
import io.uncanny.vocabularium.vocabulary.Catalog;
import io.uncanny.vocabularium.vocabulary.Catalogs;
import io.uncanny.vocabularium.vocabulary.Module;
import io.uncanny.vocabularium.vocabulary.Module.Format;
import io.uncanny.vocabularium.vocabulary.Result;
import io.uncanny.vocabularium.vocabulary.SerializationManager;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CanonicalPathHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

final class PublisherUndertowConfigurator {

	private static final class LogoProvider implements HttpHandler {

		private final Path logo;
		private final String assetBase;
		private final String relativePath;
		private final HttpHandler next;

		private LogoProvider(Path logo, String assetBase) {
			this.logo=logo;
			this.assetBase=assetBase;
			this.relativePath=logo.getParent().getFileName()+"/"+logo.getFileName();
			this.next=
				Handlers.
					resource(new PathResourceManager(logo.getParent(),100)).
						setDirectoryListingEnabled(false);
		}

		@Override
		public void handleRequest(HttpServerExchange exchange) throws Exception {
			if(exchange.getRequestPath().equals(this.assetBase+this.relativePath)) {
				this.next.handleRequest(exchange);
			} else {
				exchange.setStatusCode(StatusCodes.NOT_FOUND);
			}
		}

		String getRelativePath() {
			return this.relativePath;
		}

		public String getBase() {
			return this.assetBase+this.logo.getParent().getFileName();
		}
	}

	private static final class DefaultDocumentationProviderFactory implements DocumentationProviderFactory {

		private final DocumentationStrategy strategy;

		private DefaultDocumentationProviderFactory(final DocumentationStrategy strategy) {
			this.strategy = strategy;
		}

		@Override
		public DocumentationProvider create(final Module module) {
			return ImmutableDocumentationProvider.create(this.strategy,module);
		}

	}

	private static final class DefaultDocumentationDeploymentFactory implements DocumentationDeploymentFactory {

		private final DocumentationStrategy strategy;

		private DefaultDocumentationDeploymentFactory(final DocumentationStrategy strategy) {
			this.strategy = strategy;
		}

		@Override
		public DocumentationDeployment create(final Module module) {
			return ImmutableDocumentationDeployment.create(this.strategy,module);
		}

	}

	public static final int SERVER_CONFIGURATION_FAILURE_STATUS_CODE = -14;
	public static final int UNEXPECTED_FAILURE_STATUS_CODE           = -13;
	public static final int CANNOT_LOAD_CATALOG_STATUS_CODE          = -12;
	public static final int CANNOT_EXPLORE_MODULES_STATUS_CODE       = -11;

	private static final Logger LOGGER=LoggerFactory.getLogger(VocabularyPublisher.class);

	private static final MediaType HTML=MediaTypes.of("text","html");

	private final ApplicationInstance instance;

	PublisherUndertowConfigurator(ApplicationInstance instance) {
		this.instance = instance;
	}

	Undertow configure(PublisherConfig config) {
		try {
			return setup(config,this.instance.home().resolve("cache"));
		} catch (final ConfigurationException e) {
			throw new ApplicationBootstrapException(SERVER_CONFIGURATION_FAILURE_STATUS_CODE,"Could not configure server",e);
		} catch (final IOException e) {
			throw new ApplicationBootstrapException(CANNOT_EXPLORE_MODULES_STATUS_CODE,"Could not explore modules",e);
		} catch (final RuntimeException e) {
			throw new ApplicationBootstrapException(UNEXPECTED_FAILURE_STATUS_CODE,"Unexpected application failure",e);
		}
	}

	private Undertow setup(final PublisherConfig config, final Path cachePath) throws IOException, ConfigurationException {
		System.out.printf("- Base URI: %s%n",config.getBase());
		System.out.printf("- Server..: %s:%s%n",config.getServer().getHost(),config.getServer().getPort());
		System.out.printf("- Source directory: %s%n",config.getRoot().toAbsolutePath());
		final Result<Catalog> result = Catalogs.loadFrom(config.getRoot(), config.getBase());
		if(!result.isAvailable()) {
			throw new ApplicationBootstrapException(CANNOT_LOAD_CATALOG_STATUS_CODE,"Could not prepare catalog");
		}
		final Catalog catalog = result.get();
		showCatalog(catalog);
		return
			publish(
				catalog,
				config.extension("site",Site.class),
				"vocabularium/assets/",
				config.getBase().getPath(),
				cachePath,
				config.getServer().getPort(),
				config.getServer().getHost(),
				getDocumentationStrategy(config));
	}

	private void showCatalog(final Catalog catalog) {
		LOGGER.debug("Found {} modules",catalog.size());
		final List<Module> externals=Lists.newArrayList();
		for(final String moduleId:catalog.modules()) {
			final Module module = catalog.get(moduleId);
			if(module.isExternal()) {
				externals.add(module);
				continue;
			}
			showModule(module);
		}
		for(final Module module:externals) {
			showDependency(module);
		}
	}

	private void showDependency(final Module module) {
		final StringBuilder builder=
			new StringBuilder("- [EXTERNAL] Module '").
				append(module.location()).
				append("' [").
				append(module.relativePath()).
				append("] (").
				append(module.format().getName()).
				append(")");
		if(module.isOntology()) {
			builder.append(" refers to ");
			if(module.isVersion()) {
				builder.append("version '").append(module.versionIRI()).append("' of ");
			}
			builder.append("IRI '").append(module.ontology()).append("'");
		}
		LOGGER.debug(builder.toString());
	}

	private void showModule(final Module module) {
		LOGGER.debug("- Module '{}':",module.location());
		LOGGER.debug("  + Relative path: {}",module.relativePath());
		LOGGER.debug("  + Format: {}",module.format().getName());
		if(module.isOntology()) {
			LOGGER.debug("  + Ontology:");
			LOGGER.debug("    * IRI: {}",module.ontology());
			if(module.isVersion()) {
				LOGGER.debug("    * VersionIRI: {}",module.versionIRI());
			}
			if(module.hasImports()) {
				LOGGER.debug("    * Imports:");
				for(final String importedModule:module.imports()) {
					LOGGER.debug("      - {}",importedModule);
				}
			}
		}
	}

	private Undertow publish(final Catalog catalog, final Site site, final String vocabAssetsPath, final String basePath, final Path serializationCachePath, final int port, final String host, final DocumentationStrategy strategy) throws IOException, ConfigurationException {
		LOGGER.debug("* Publishing vocabularies under {}",basePath);
		final PathHandler pathHandler=path();
		// Module serializations
		final SerializationManager manager=publishSerializations(catalog,pathHandler,serializationCachePath);
		// Catalog documentation
		final DocumentationDeploymentFactory factory=publishDocumentation(catalog,pathHandler,strategy);
		// Canonical namespaces
		publishCanonicalNamespace(catalog,basePath,pathHandler,manager,factory);
		// Vocab site
		if(site!=null) {
			validateSiteConfiguration(site);
			publishVocabSite(catalog,pathHandler,basePath,site,vocabAssetsPath);
		}
		return
			Undertow.
				builder().
					addHttpListener(port,host).
					setHandler(new CanonicalPathHandler(pathHandler)).
					build();
	}

	private void validateSiteConfiguration(final Site site) throws ConfigurationException {
		if(site.getOwner()==null) {
			throw new ConfigurationException("No owner defined");
		}
		if(!StringUtils.isNotEmpty(site.getOwner().getName())) {
			throw new ConfigurationException("No owner name defined");
		}
		if(!StringUtils.isNotEmpty(site.getOwner().getUri())) {
			throw new ConfigurationException("No owner URI defined");
		}
		String logo = site.getOwner().getLogo();
		if(!StringUtils.isNotEmpty(logo)) {
			throw new ConfigurationException("No owner logo path defined");
		}
		try {
			if(!Paths.get(logo).toFile().isFile()) {
				throw new ConfigurationException("Could not find owner logo");
			}
		} catch (InvalidPathException e) {
			throw new ConfigurationException("Invalid owner logo path defined",e);
		}
	}

	private void publishVocabSite(final Catalog catalog, final PathHandler pathHandler, final String basePath, final Site site, final String vocabAssetsPath) {
		final LogoProvider handler=new LogoProvider(Paths.get(site.getOwner().getLogo()), basePath+vocabAssetsPath);
		final Site updated=Model.clone(site);
		final Owner owner=updated.getOwner();
		owner.setLogo(handler.getRelativePath());
		pathHandler.
			addPrefixPath(
				basePath+vocabAssetsPath,
				new AssetProvider(vocabAssetsPath)
			).
			addPrefixPath(handler.getBase(),handler).
			addExactPath(
				basePath,
				catalogReverseProxy(
					catalog,
					methodController(
						contentNegotiation().
							negotiate(
								htmlContent(),
								catalogRepresentation(updated))).
						allow(Methods.GET)));
	}

	private void publishCanonicalNamespace(final Catalog catalog, final String basePath, final PathHandler pathHandler, final SerializationManager manager, final DocumentationDeploymentFactory factory) {
		final ContentNegotiationHandler contentNegotiation = contentNegotiation().
			negotiate(
				negotiableModuleContent(),
				new ModuleRepresentionGenerator(manager));
		if(factory!=null) {
			contentNegotiation.
				negotiate(
					NegotiableContent.newInstance().support(HTML),
					new ModuleDocumentationRedirector(factory));
		}
		pathHandler.
			addPrefixPath(
				basePath,
				moduleReverseProxy(
					catalog,
					methodController(
						contentNegotiation).
						allow(Methods.GET)
				)
			);
	}

	private DocumentationDeploymentFactory publishDocumentation(final Catalog catalog, final PathHandler pathHandler, final DocumentationStrategy strategy) {
		if(strategy==null) {
			return null;
		}
		final DocumentationDeploymentFactory deploymentFactory=new DefaultDocumentationDeploymentFactory(strategy);
		final DocumentationDeployer deployer=
			DocumentationDeployer.
				create(
					deploymentFactory,
					new DefaultDocumentationProviderFactory(strategy));
		deployer.deploy(catalog,pathHandler);
		return deploymentFactory;
	}

	private SerializationManager publishSerializations(final Catalog catalog, final PathHandler pathHandler, final Path cachePath) throws IOException {
		final SerializationManager manager=SerializationManager.create(catalog,cachePath);
		for(final String moduleId:catalog.modules()) {
			final Module module=catalog.get(moduleId);
			LOGGER.debug("- Module ({}):",module.implementationIRI(),module.location());
			for(final Format format:Format.values()) {
				final String resourceName = module.relativePath()+"."+format.fileExtension();
				final URI location=catalog.getBase().resolve(resourceName);
				LOGGER.debug("  + {} : {} --> {} ({})",format.getName(),location,location.getPath(),manager.getSerialization(module, format).toAbsolutePath());
				pathHandler.
					addExactPath(
						location.getPath(),
						methodController(
							contentNegotiation().
								negotiate(
									NegotiableContent.
										newInstance().
											support(Formats.toMediaType(format)).
											support(CharacterEncodings.of(StandardCharsets.UTF_8)).
											support(CharacterEncodings.of(StandardCharsets.ISO_8859_1)).
											support(CharacterEncodings.of(StandardCharsets.US_ASCII)),
									new SerializationHandler(manager,module,format))).
						allow(Methods.GET));
			}
		}
		return manager;
	}

	private DocumentationStrategy getDocumentationStrategy(final PublisherConfig config) throws ConfigurationException {
		final DocumentationConfig docConfig = getDocumentationConfig(config);
		if(docConfig==null) {
			return null;
		}
		return new DocumentationStrategy(docConfig.getRoot(),docConfig.getRelativePath());
	}

	private DocumentationConfig getDocumentationConfig(final PublisherConfig config) throws ConfigurationException {
		final DocumentationConfig docConfig = config.extension(DocumentationConfig.class);
		if(docConfig==null) {
			return null;
		}
		if(docConfig.getRoot()==null) {
			docConfig.setRoot(config.getRoot().getParent().resolve("docs/"));
		}
		if(docConfig.getRelativePath()==null) {
			docConfig.setRelativePath("html");
		}
		System.out.printf("- Documentation directory....: %s%n",docConfig.getRoot().toAbsolutePath());
		System.out.printf("- Documentation relative path: %s%n",docConfig.getRelativePath());
		return docConfig;
	}

	private NegotiableContent negotiableModuleContent() {
		return
			NegotiableContent.
				newInstance().
					support(Formats.toMediaType(Format.TURTLE)).
					support(Formats.toMediaType(Format.RDF_XML)).
					support(Formats.toMediaType(Format.JSON_LD)).
					support(CharacterEncodings.of(StandardCharsets.UTF_8)).
					support(CharacterEncodings.of(StandardCharsets.ISO_8859_1)).
					support(CharacterEncodings.of(StandardCharsets.US_ASCII));
	}

	private NegotiableContent htmlContent() {
		return
			NegotiableContent.
				newInstance().
					support(HTML).
					support(CharacterEncodings.of(StandardCharsets.UTF_8)).
					support(CharacterEncodings.of(StandardCharsets.ISO_8859_1)).
					support(CharacterEncodings.of(StandardCharsets.US_ASCII));
	}

}