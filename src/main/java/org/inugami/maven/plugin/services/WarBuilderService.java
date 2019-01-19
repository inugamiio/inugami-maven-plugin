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
import java.util.List;

import org.inugami.maven.plugin.tools.FilesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WarBuilderService
 * 
 * @author pguillerm
 * @since 19 juil. 2017
 */
public class WarBuilderService {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private static final Logger LOGGER = LoggerFactory.getLogger(WarBuilderService.class.getSimpleName());

    // =========================================================================
    // METHODS
    // =========================================================================
    public File buildWar(File currentArtifact, File webapp, List<File> dependencies) throws IOException {
        LOGGER.info("verify input data");
        FilesUtils.assertCanRead(currentArtifact);
        FilesUtils.assertCanRead(webapp);

        File targetFolder = currentArtifact.getParentFile();
        FilesUtils.assertCanWrite(targetFolder);

        File webappFolder = FilesUtils.buildFile(targetFolder, "webapp-generated");

        LOGGER.info("cleaning...");
        cleanGeneratedFolder(webappFolder);

        LOGGER.info("unzip webapp...");
        unzipWebApp(webapp, webappFolder);

        LOGGER.info("copy artifact");
        copyArtifact(currentArtifact, webappFolder, dependencies);

        LOGGER.info("rebuild war");
        final File result = rebuildWar(webappFolder, currentArtifact, targetFolder);

        return result;
    }

    // =========================================================================
    // OVERRIDES
    // =========================================================================
    private void cleanGeneratedFolder(File webappFolder) {
        if (webappFolder.exists()) {
            FilesUtils.delete(webappFolder);
        }
    }

    private void unzipWebApp(File webapp, File webappFolder) throws IOException {
        FilesUtils.unzip(webapp, webappFolder);
    }

    private void copyArtifact(File currentArtifact, File warUnzipped, List<File> dependencies) {
        File libFolder = FilesUtils.buildFile(warUnzipped, "WEB-INF", "lib");
        FilesUtils.copy(currentArtifact, libFolder);

        if (dependencies != null) {
            dependencies.forEach(file -> FilesUtils.copy(file, libFolder));
        }
    }

    private File rebuildWar(File warUnzipped, File currentArtifact, File targetFolder) throws IOException {

        final String artifactName = currentArtifact.getName();
        final String warArtifact = artifactName.substring(0, artifactName.lastIndexOf(".")) + ".war";

        final File zipFile = FilesUtils.buildFile(targetFolder, warArtifact);

        FilesUtils.zip(warUnzipped, zipFile);

        return zipFile;
    }
    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================
}
