/* --------------------------------------------------------------------
 *  Inugami  
 * --------------------------------------------------------------------
 * 
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.inugami.maven.plugin.services;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.inugami.maven.plugin.exceptions.Asserts;
import org.inugami.maven.plugin.models.Gav;
import org.inugami.maven.plugin.tools.FilesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeployServices
 * 
 * @author patrick_guillerm
 * @since 20 juin 2017
 */
public class DeployServices {

	// =========================================================================
	// ATTRIBUTES
	// =========================================================================
	private static final Logger LOGGER = LoggerFactory.getLogger(DeployServices.class);

	private static final String PLUGIN_EXTENSION = ".jar";

	private static final Pattern MANIFEST_ARTIFACT_TYPE = Pattern.compile("^ArtifactType:\\s*inugami_plugin.*");

	// =========================================================================
	// METHODS
	// =========================================================================
	public void deployPlugin(String serverHome, File basedir, Gav gav, List<Gav> dependencies, boolean forceOverride) {
		LOGGER.info("deploy plugin into server...");
		Asserts.notNull("server path is mandatory!", serverHome);
		Asserts.notNull("project base directory is mandatory!", basedir);
		Asserts.notNull("project GAV is mandatory!", gav);

		final File server = new File(serverHome);
		FilesUtils.assertFileExists(server);
		FilesUtils.assertCanWrite(server);

		LOGGER.info("server home            : {}", serverHome);
		LOGGER.info("project base directory : {}", basedir);
		LOGGER.info("project gav            : {}", gav.getHash());

		LOGGER.info("deploy plugin into server...");

		deployCurrentArtifact(basedir, gav, server);
		deployDependencies(dependencies, server, forceOverride);
	}

	// =========================================================================
	// PRIVATE API
	// =========================================================================
	private void deployCurrentArtifact(File basedir, Gav gav, File server) {
		final File target = FilesUtils.buildFile(basedir, "target");
		FilesUtils.assertCanRead(target);
		FilesUtils.assertIsFolder(target);

		final String[] files = target.list((dir, fileName) -> fileName.endsWith(PLUGIN_EXTENSION));
		Asserts.isTrue("Plugin artifact not found :" + gav, files.length > 0);

		final Pattern pattern = buildRegExArtifact(gav);
		final List<String> filesNames = Arrays.asList(files);
		// @formatter:off
		final Optional<String> artifact = filesNames.stream().filter(name -> pattern.matcher(name).matches())
				.findFirst();
		// @formatter:on
		Asserts.isTrue(artifact.isPresent());
		final File artifactFile = FilesUtils.buildFile(target, artifact.get());
		final File destination = FilesUtils.buildFile(server, ServerServices.PLUGINS_FOLDER);

		FilesUtils.copy(artifactFile, destination);
		LOGGER.info("deploy plugin : {}", FilesUtils.getCanonicalPath(destination));
	}

	private void deployDependencies(List<Gav> dependencies, File server, boolean forceOverride) {
		final List<Gav> plugins = dependencies.stream().filter(this::isPlugin).collect(Collectors.toList());
		final List<Gav> libs = dependencies.stream().filter(this::isNotPlugin).collect(Collectors.toList());

		processDeployDependencies(plugins, DependencyType.plugin, server, forceOverride);
		processDeployDependencies(libs, DependencyType.lib, server, forceOverride);
	}

	private void processDeployDependencies(List<Gav> dependencies, DependencyType type, File server,
			boolean forceOverride) {
		// @formatter:off
		dependencies.stream().filter(gav -> isNotInMainApplication(gav, server))
				.filter(gav -> isNotInExternalFolder(gav, type, server, forceOverride))
				.forEach(gav -> processDeployDependencyInExternalFolder(gav, type, server));
		// @formatter:on
	}

	private void processDeployDependencyInExternalFolder(Gav gav, DependencyType type, File server) {
		File destination = destination = buildLibFolder(type, server);
		FilesUtils.copy(gav.getPath(), destination);
	}

	// =========================================================================
	// IS PLUGIN OR NOT
	// =========================================================================
	private boolean isNotPlugin(Gav gav) {
		return !isPlugin(gav);
	}

	private boolean isPlugin(Gav gav) {
		boolean result = false;

		final File tmpDir = FilesUtils.getTmpDir();

		final File artifactTmpDir = FilesUtils.buildFile(tmpDir, FilesUtils.cleanFolderPath(gav.getHash()));

		if (!artifactTmpDir.exists()) {
			try {
				FilesUtils.unzipLogless(gav.getPath(), artifactTmpDir);
			} catch (IOException e) {
				throw new IllegalArgumentException(e.getMessage());
			}
		}

		final File manifest = FilesUtils.buildFile(artifactTmpDir, "META-INF", "MANIFEST.MF");
		if (manifest.exists() && manifest.canRead()) {
			final String[] content;
			try {
				content = FilesUtils.read(manifest).split("\n");
			} catch (IOException e) {
				throw new IllegalArgumentException(e.getMessage());
			}

			for (int i = content.length - 1; i >= 0; i--) {
				if (MANIFEST_ARTIFACT_TYPE.matcher(content[i]).matches()) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	// =========================================================================
	// NOT IN CONTEXT
	// =========================================================================
	private boolean isNotInMainApplication(Gav gav, File server) {
		// @formatter:off
		File webapp = FilesUtils.buildFile(server, ServerServices.TOMCAT, ServerServices.WEBAPPS,
				ServerServices.INUGAMI_WEBAPP);
		// @formatter:on

		if (!webapp.exists()) {
			webapp = processUnzipMainApplication(server);
		}

		final File libFolder = FilesUtils.buildFile(webapp, "WEB-INF", "lib");
		FilesUtils.assertCanRead(libFolder);

		return !findArtifact(libFolder, gav, this::throwRuntime);
	}

	private boolean isNotInExternalFolder(Gav gav, DependencyType type, File server, boolean forceOverride) {
		File folder = buildLibFolder(type, server);

		boolean foundArtifacts = findArtifact(folder, gav);

		if (forceOverride) {
			deleteExistingArtifact(folder, gav);
			foundArtifacts = false;
		}

		return !foundArtifacts;
	}

	// =========================================================================
	// FIND ARTIFACTS
	// =========================================================================
	private boolean findArtifact(File libFolder, Gav gav) {
		return findArtifact(libFolder, gav, null);
	}

	private boolean findArtifact(File libFolder, Gav gav, MultiVersionHandler handler) {
		// @formatter:off
		boolean result = FilesUtils.list(libFolder).stream()
				.filter(file -> matchArtifactWithoutVersion(file, gav, handler)).findFirst().isPresent();
		// @formatter:on
		return result;
	}

	@FunctionalInterface
	private interface MultiVersionHandler {
		void detected(File file, Gav gav);
	}

	private boolean matchArtifactWithoutVersion(File file, Gav gav, MultiVersionHandler handler) {
		Pattern regex = Pattern.compile(gav.getArtifactId() + "-[0-9]+.*");
		file.getName().startsWith(gav.getArtifactId() + "-");
		boolean found = regex.matcher(file.getName()).matches();
		if (found && !file.getName().contains(gav.getVersion())) {
			// @formatter:off
			LOGGER.warn("Mutli version dependency found: {} -> {}", gav.getHash(), FilesUtils.getCanonicalPath(file));
			if (handler != null) {
				handler.detected(file, gav);
			}
			// @formatter:on
		}

		return found;
	}

	// =========================================================================
	// TOOLS
	// =========================================================================
	private void deleteExistingArtifact(File folder, Gav gav) {
		// @formatter:off
		FilesUtils.list(folder).stream().filter(file -> matchArtifactWithoutVersion(file, gav, null))
				.forEach(FilesUtils::delete);
		// @formatter:on
	}

	private File processUnzipMainApplication(File server) {
		// @formatter:off
		File war = FilesUtils.buildFile(server, ServerServices.TOMCAT, ServerServices.WEBAPPS,
				ServerServices.INUGAMI_WEBAPP_WAR);
		// @formatter:on

		final File destination = FilesUtils.buildFile(FilesUtils.getTmpDir(), ServerServices.INUGAMI_WEBAPP);
		if (!destination.exists()) {
			try {
				FilesUtils.unzipLogless(war, destination);
			} catch (IOException e) {
				throw new IllegalArgumentException(e.getMessage());
			}
		}

		return destination;
	}

	private Pattern buildRegExArtifact(Gav gav) {
		final StringBuilder regex = new StringBuilder();
		regex.append(gav.getArtifactId());
		regex.append('-');
		regex.append(gav.getVersion());
		regex.append(PLUGIN_EXTENSION);

		return Pattern.compile(regex.toString().replaceAll("[.]", "[.]"));
	}

	private File buildLibFolder(DependencyType type, File server) {
		File destination;
		if (DependencyType.plugin == type) {
			destination = FilesUtils.buildFile(server, ServerServices.PLUGINS_FOLDER);
		} else {
			destination = FilesUtils.buildFile(server, ServerServices.LIBS_FOLDER);
		}
		return destination;
	}

	private enum DependencyType {
		plugin, lib;
	}

	// =========================================================================
	// EXCEPTIONS
	// =========================================================================
	private void throwRuntime(File file, Gav artifactGav) {
		// @formatter:off
		throw new IllegalArgumentException(
				String.format("artifact error : %s -> %s", artifactGav.getHash(), FilesUtils.getCanonicalPath(file)));
		// @formatter:on
	}
}
