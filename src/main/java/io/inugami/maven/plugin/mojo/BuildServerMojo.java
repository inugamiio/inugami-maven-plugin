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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.inugami.maven.plugin.services.ServerServices;
import org.apache.maven.artifact.Artifact;
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
import io.inugami.maven.plugin.exceptions.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeployArtifactMojo
 * 
 * @author patrick_guillerm
 * @since 20 juin 2017
 */
@Mojo(name = "buildServer", defaultPhase = LifecyclePhase.NONE)
public class BuildServerMojo extends AbstractMojo {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final static Logger LOGGER = LoggerFactory.getLogger(BuildServerMojo.class);
    @Parameter(name = "server", required = true)
    private String server;
    
    @Parameter(name = "forceClean")
    private boolean forceClean;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File basedir;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    @Component
    private PluginDescriptor pluginDescriptor;

    // =========================================================================
    // METHODS
    // =========================================================================
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final File tomcatZip = searchArtifact("org.apache.tomcat:tomcat");
        final File inugami = searchArtifact("io.inugami:inugami_webapp");

        LOGGER.info("server path : {}",server);
        final Map<String, String> properties = loadProperties();
        new ServerServices().buildServer(server, tomcatZip, inugami, basedir, properties, forceClean);
    }

    // =========================================================================
    // PRIVATE
    // =========================================================================
    private File searchArtifact(final String artifactName) {
        final Artifact result = pluginDescriptor.getArtifactMap().get(artifactName);
        Asserts.notNull(String.format("can't find %s artifact!", artifactName), result);
        return result.getFile();
    }

    private Map<String, String> loadProperties() {
        final Map<String, String> result = new HashMap<>();
        Properties sysProperties = null;
        Properties userProperties = null;
        Properties projectProperties = null;

        if (mavenSession != null) {
            sysProperties = mavenSession.getSystemProperties();
            userProperties = mavenSession.getUserProperties();
            projectProperties = project.getProperties();
        }

        result.putAll(buildMap(sysProperties));
        result.putAll(buildMap(userProperties));
        result.putAll(buildMap(projectProperties));

        return result;
    }

    private Map<String, String> buildMap(Properties properties) {
        final Map<String, String> result = new HashMap<>();

        if (properties != null) {
            //@formatter:off
            properties.entrySet()
                      .stream()
                      .forEach(item -> result.put(String.valueOf(item.getKey()),String.valueOf(item.getValue())));
            //@formatter:on
        }
        return result;
    }
}
