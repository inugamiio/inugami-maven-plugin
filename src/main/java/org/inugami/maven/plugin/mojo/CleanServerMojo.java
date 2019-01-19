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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * DeployArtifactMojo
 * 
 * @author patrick_guillerm
 * @since 20 juin 2017
 */
@Mojo(name = "cleanServer", defaultPhase = LifecyclePhase.NONE)
public class CleanServerMojo extends AbstractMojo {

	// =========================================================================
	// ATTRIBUTES
	// =========================================================================
	@Parameter(name = "server", required = true)
	private String server;

	@Parameter(defaultValue = "${project.basedir}", readonly = true)
	private File basedir;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	// =========================================================================
	// METHODS
	// =========================================================================
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		MojoHelper helper = new MojoHelper();
		helper.drawDeco("Clean server", "*");
		//TODO:implement server cleaning 
		throw new RuntimeException("Not implemented yet!");
	}

}
