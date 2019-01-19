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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.inugami.maven.plugin.models.Potentiometer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeployArtifactMojo
 * 
 * @author patrick_guillerm
 * @since 20 juin 2017
 */
@Mojo(name = "buildArtifactType", defaultPhase = LifecyclePhase.INITIALIZE)
public class ArtifactTypeMojo extends AbstractMojo {
    
    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final static Logger LOGGER = LoggerFactory.getLogger(ArtifactTypeMojo.class);
    
    private static final String INUGAMI_PLUGIN = "inugami_plugin";
    
    private static final String INUGAMI_PLUGINS_SET = "inugami_plugins_set";
    
    private static final String SUPER_ARTIFACT_TYPE = "super.artifact.type";
    
    private static final String SUPER_DEV_WORKSPACE = "super.dev.workspace";
    
    @Parameter(name = "artifactTypePlugin")
    private String artifactTypePlugin;
    
    @Parameter(name = "artifactTypePluginsSet")
    private String artifactTypePluginsSet;
    
    @Parameter(name = "envDev")
    private String envDev;
    
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;
    
    // =========================================================================
    // METHODS
    // =========================================================================
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String artifactType = resolveArtifactType();
        project.getProperties().setProperty(SUPER_ARTIFACT_TYPE, artifactType);
        mavenSession.getCurrentProject().getProperties().setProperty(SUPER_ARTIFACT_TYPE, artifactType);
        
        LOGGER.info("artifact type : {}", artifactType);
        
        if(new Potentiometer(envDev, "envDev").isEnable()) {
            final String baseDir = buildBaseDir();
            project.getProperties().setProperty(SUPER_DEV_WORKSPACE, baseDir);
            mavenSession.getCurrentProject().getProperties().setProperty(SUPER_DEV_WORKSPACE, baseDir);
        }
    }
    
    private String resolveArtifactType() {
        final Potentiometer typePlugin = new Potentiometer(artifactTypePlugin, "artifactTypePlugin");
        final Potentiometer typePluginsSet = new Potentiometer(artifactTypePluginsSet, "artifactTypePluginsSet");
        
        String result = "";
        if (typePlugin.isEnable()) {
            result = INUGAMI_PLUGIN;
        }
        else if (typePluginsSet.isEnable()) {
            result = INUGAMI_PLUGINS_SET;
        }
        
        return result;
    }
    
    private String buildBaseDir() {
        String result;
        final File baseDir = project.getBasedir().getAbsoluteFile();
        try {
            result = baseDir.getCanonicalPath();
        }
        catch (IOException e) {
            result = baseDir.getPath();
        }
        return result;
    }
}
