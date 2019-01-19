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
import java.util.zip.ZipEntry;

/**
 * ZipEntryFile
 * 
 * @author pguillerm
 * @since 19 juil. 2017
 */
public class ZipEntryFile {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final ZipEntry zipEntry;

    private final File file;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    public ZipEntryFile(String zipEntryPath, File file) {
        super();
        this.zipEntry = new ZipEntry(zipEntryPath);
        this.file = file;
    }

    // =========================================================================
    // OVERRIDE
    // =========================================================================
    @Override
    public String toString() {
        return "ZipEntryFile [zipEntry=" + zipEntry + ", file=" + file + "]";
    }

    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================
    public ZipEntry getZipEntry() {
        return zipEntry;
    }

    public File getFile() {
        return file;
    }

}
