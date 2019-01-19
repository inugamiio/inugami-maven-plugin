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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Unzip
 * 
 * @author patrick_guillerm
 * @since 22 juin 2017
 */
public class Unzip {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final static Logger LOGGER = LoggerFactory.getLogger(Unzip.class.getSimpleName());

    // =========================================================================
    // METHODS
    // =========================================================================
    public void unzipLogLess(File zipFile, File destination) throws IOException {
        processUnzip(zipFile, destination, false);
    }

    public void unzip(File zipFile, File destination) throws IOException {
        processUnzip(zipFile, destination, true);
    }

    public void zip(File file, File zipFile) throws IOException {
        byte[] buffer = new byte[1024];

        final List<ZipEntryFile> files = loadZipEntryFiles(file, null);
        files.forEach(entry -> LOGGER.info("zipfile : {}", entry.getZipEntry()));

        final FileOutputStream output = new FileOutputStream(zipFile);
        final ZipOutputStream zip = new ZipOutputStream(output);

        try {
            for (ZipEntryFile entry : files) {
                final FileInputStream input = new FileInputStream(entry.getFile());
                try {
                    zip.putNextEntry(entry.getZipEntry());
                    int len;
                    while ((len = input.read(buffer)) > 0) {
                        zip.write(buffer, 0, len);
                    }
                } catch (ZipException e) {
                    LOGGER.error(e.getMessage());
                } finally {
                    input.close();
                    zip.closeEntry();
                }
            }
        } finally {
            zip.close();
        }
        LOGGER.info("zip file : {}", zipFile.getAbsolutePath());
    }

    // =========================================================================
    // PRIVATE
    // =========================================================================

    private List<ZipEntryFile> loadZipEntryFiles(File file, String path) {
        final List<ZipEntryFile> result = new ArrayList<>();
        final String fullPath = path == null ? "" : path;

        if (file != null) {
            if (file.isDirectory()) {
                final String subPath = path == null ? fullPath : fullPath + "/" + file.getName() + "/";
                final List<File> subFiles = Arrays.asList(file.listFiles());
                //@formatter:off
                subFiles.stream()
                        .map(subFile -> loadZipEntryFiles(subFile, subPath))
                        .forEach(result::addAll);
                //@formatter:on
            } else {
                result.add(new ZipEntryFile(fullPath + file.getName(), file));
            }
        }
        return result;
    }

    private void processUnzip(File zipFile, File destination, boolean verbose) throws IOException {
        final FileInputStream fileZipStream = openFileInputStream(zipFile);
        final ZipInputStream zip = new ZipInputStream(fileZipStream);
        try {
            ZipEntry entry;
            do {
                entry = zip.getNextEntry();
                if (entry != null) {
                    unzipFile(destination, zip, entry, verbose);
                }
            } while (entry != null);

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw e;
        } finally {
            close(() -> zip.closeEntry());
            close(() -> zip.close());
            close(() -> fileZipStream.close());
        }
    }

    private void unzipFile(File server, ZipInputStream zip, ZipEntry entry, boolean verbose)
            throws FileNotFoundException, IOException {
        byte[] buffer = new byte[1024];
        String fileName = entry.getName();
        final File newFile = buildFileEntry(server, fileName);

        if (verbose) {
            LOGGER.info("unzip : {}", newFile.getAbsolutePath());
        }

        if (entry.isDirectory()) {
            newFile.mkdirs();
        } else {
            final FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zip.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            close(() -> fos.close());
        }

        close(() -> zip.closeEntry());
        close(() -> zip.closeEntry());
    }

    private File buildFileEntry(File server, String fileName) {
        // @formatter:off
		final String path = new StringBuilder(server.getAbsolutePath()).append(File.separator).append(fileName)
				.toString();
		// @formatter:on
        final File result = new File(path);

        final File parent = result.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        return result;
    }

    // =========================================================================
    // TOOLS
    // =========================================================================
    private FileInputStream openFileInputStream(File tomcatZip) throws IOException {
        try {
            return new FileInputStream(tomcatZip);
        } catch (FileNotFoundException e) {
            throw e;
        }
    }

    private void close(AutoCloseable closable) {
        try {
            closable.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

    }

}
