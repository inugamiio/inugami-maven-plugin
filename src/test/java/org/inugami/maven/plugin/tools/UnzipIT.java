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
package org.inugami.maven.plugin.tools;

import java.io.File;
import java.io.IOException;

import org.inugami.maven.plugin.tools.FilesUtils;
import org.inugami.maven.plugin.tools.Unzip;

/**
 * UnzipTest
 * 
 * @author patrick_guillerm
 * @since 20 juil. 2017
 */
public class UnzipIT {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final static File CURRENT_PATH = initCurrentPath();

    private static File initCurrentPath() {
        File file = new File(".");
        return file.getAbsoluteFile().getParentFile();
    }

    // =========================================================================
    // IT
    // =========================================================================
    public static void main(String[] args) {
        try {
            zipTest();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // =========================================================================
    // METHODS
    // =========================================================================
    public static void zipTest() throws IOException {
        Unzip unzip = new Unzip();

        File folderPath = FilesUtils.buildFile(CURRENT_PATH, "target", "surefire-reports");
        File zipPath = FilesUtils.buildFile(CURRENT_PATH, "target", "surefire-reports.zip");

        unzip.zip(folderPath, zipPath);
    }
    // =========================================================================
    // OVERRIDES
    // =========================================================================

    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================
}
