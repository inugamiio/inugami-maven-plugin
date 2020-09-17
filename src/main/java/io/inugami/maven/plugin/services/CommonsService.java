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
package io.inugami.maven.plugin.services;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import io.inugami.maven.plugin.models.Gav;
import io.inugami.maven.plugin.tools.FilesUtils;

public final class CommonsService {
    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private static final Pattern MANIFEST_ARTIFACT_TYPE = Pattern.compile("^ArtifactType:\\s*inugami_plugin.*");
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    private CommonsService() {
    }
    
    // =========================================================================
    // METHODS
    // =========================================================================
    public static boolean isPlugin(Gav gav) {
        boolean result = false;
        
        final File tmpDir = FilesUtils.getTmpDir();
        
        final File artifactTmpDir = FilesUtils.buildFile(tmpDir, FilesUtils.cleanFolderPath(gav.getHash()));
        
        if (!artifactTmpDir.exists()) {
            try {
                FilesUtils.unzipLogless(gav.getPath(), artifactTmpDir);
            }
            catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        
        final File manifest = FilesUtils.buildFile(artifactTmpDir, "META-INF", "MANIFEST.MF");
        if (manifest.exists() && manifest.canRead()) {
            final String[] content;
            try {
                content = FilesUtils.read(manifest).split("\n");
            }
            catch (IOException e) {
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
}
