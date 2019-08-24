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
package org.inugami.maven.plugin.mojo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.inugami.maven.plugin.exceptions.Asserts;
import org.inugami.maven.plugin.models.Gav;
import org.inugami.maven.plugin.services.InitializerProjectService;
import org.inugami.maven.plugin.tools.DependenciesResolver;

@Mojo(name = "initializeProject", defaultPhase = LifecyclePhase.PACKAGE)
public class InitializeProject extends AbstractMojo {
    
    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File                    basedir;
    
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject            project;
    
    @Component
    private RepositorySystem        repoSystem;
    
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession repoSession;
    
    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
    private List<RemoteRepository>  repositories;
    
    @Component
    private PluginDescriptor        pluginDescriptor;
    
    // =========================================================================
    // METHODS
    // =========================================================================
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MojoHelper helper = new MojoHelper();
        helper.drawDeco("Initialize Project for Visual Code Studio", "*");
        final File inugamiRootPlugin = searchArtifact("org.inugami.plugins:inugami_plugins_root");
                
        //@formatter:off
        List<Gav> dependencies = new DependenciesResolver(repositories,repoSystem,repoSession)
                                            .resolve(project);
        //@formatter:on        
        try {
            new InitializerProjectService().initializeProject(basedir, dependencies,inugamiRootPlugin);
        }
        catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(),e);
        }
    }
    
    private File searchArtifact(final String artifactName) {
        final Artifact result = pluginDescriptor.getArtifactMap().get(artifactName);
        Asserts.notNull(String.format("can't find %s artifact!", artifactName), result);
        return result.getFile();
    }
    
}
