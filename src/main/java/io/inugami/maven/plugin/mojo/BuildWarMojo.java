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
package io.inugami.maven.plugin.mojo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import io.inugami.maven.plugin.models.Gav;
import io.inugami.maven.plugin.services.WarBuilderService;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.AttachedArtifact;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import io.inugami.maven.plugin.exceptions.Asserts;
import io.inugami.maven.plugin.tools.DependenciesResolver;

/**
 * DeployArtifactMojo
 * 
 * @author patrick_guillerm
 * @since 20 juin 2017
 */
@Mojo(name = "buildWar", defaultPhase = LifecyclePhase.NONE)
public class BuildWarMojo extends AbstractMojo {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File basedir;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    @Component
    private PluginDescriptor pluginDescriptor;

    @Component
    private RepositorySystem repoSystem;

    @Component
    private ArtifactHandler artifactHandler;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
    private List<RemoteRepository> repositories;

    // =========================================================================
    // METHODS
    // =========================================================================
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final File inugamiWebapp = searchArtifact("io.inugami:inugami_webapp");
        final File artifactFile = project.getArtifact().getFile();

        //@formatter:off
        List<File> dependencies = new DependenciesResolver(repositories,repoSystem,repoSession)
                                            .resolve(project)
                                            .stream()
                                            .map(Gav::getPath)
                                            .collect(Collectors.toList());
        //@formatter:on
        File warFile = null;
        try {
            warFile = new WarBuilderService().buildWar(artifactFile, inugamiWebapp, dependencies);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        Asserts.notNull("error on create war file!", warFile);

        final Artifact artifactWar = buildArtifact(project.getArtifact(), warFile);
        project.addAttachedArtifact(artifactWar);

    }

    // =========================================================================
    // PRIVATE
    // =========================================================================
    private File searchArtifact(final String artifactName) {
        final Artifact result = pluginDescriptor.getArtifactMap().get(artifactName);
        Asserts.notNull(String.format("can't find %s artifact!", artifactName), result);
        return result.getFile();
    }

    private Artifact buildArtifact(Artifact parent, File warFile) {
        Artifact artifact = new AttachedArtifact(parent, "war", null, new CustomHandler(artifactHandler));
        artifact.setFile(warFile);
        return artifact;
    }

    private class CustomHandler implements ArtifactHandler {

        private final ArtifactHandler artifactHandler;

        public CustomHandler(ArtifactHandler artifactHandler) {
            this.artifactHandler = artifactHandler;
        }

        @Override
        public String getExtension() {
            return "war";
        }

        @Override
        public String getDirectory() {
            return artifactHandler.getDirectory();
        }

        @Override
        public String getClassifier() {
            return artifactHandler.getClassifier();
        }

        @Override
        public String getPackaging() {
            return "war";
        }

        @Override
        public boolean isIncludesDependencies() {
            return artifactHandler.isIncludesDependencies();
        }

        @Override
        public String getLanguage() {
            return artifactHandler.getLanguage();
        }

        @Override
        public boolean isAddedToClasspath() {
            return artifactHandler.isAddedToClasspath();
        }

    }

}
