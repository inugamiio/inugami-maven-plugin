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
import java.util.List;

import io.inugami.maven.plugin.models.Gav;
import io.inugami.maven.plugin.services.DeployServices;
import io.inugami.maven.plugin.tools.DependenciesResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * DeployArtifactMojo
 * 
 * @author patrick_guillerm
 * @since 20 juin 2017
 */
@Mojo(name = "deployPlugin", defaultPhase = LifecyclePhase.PACKAGE)
public class DeployPluginMojo extends AbstractMojo {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    @Parameter(name = "server", required = true)
    private String server;

    @Parameter(name = "override")
    private boolean override;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File basedir;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Component
    private RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
    private List<RemoteRepository> repositories;

    // =========================================================================
    // METHODS
    // =========================================================================
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MojoHelper helper = new MojoHelper();
        helper.drawDeco("Deploy plugin into server", "*");
        //@formatter:off
        List<Gav> dependencies = new DependenciesResolver(repositories,repoSystem,repoSession)
                                            .resolve(project);
        //@formatter:on        
        new DeployServices().deployPlugin(server, basedir, buildGav(), dependencies, override);
    }

    // =========================================================================
    // TOOLS
    // =========================================================================
    private Gav buildGav() {
        return new Gav(project.getGroupId(), project.getArtifactId(), project.getVersion());
    }

}
