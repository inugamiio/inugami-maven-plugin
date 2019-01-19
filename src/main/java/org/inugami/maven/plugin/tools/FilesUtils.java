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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.inugami.maven.plugin.exceptions.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileUtils
 * 
 * @author patrick_guillerm
 * @since 22 juin 2017
 */
public class FilesUtils {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private static final Logger LOGGER = LoggerFactory.getLogger(FilesUtils.class.getSimpleName());

    public static final int MEGA = 1024 * 1024;

    public static final int DEFAULT_BUFFER_SIZE = 10 * MEGA;

    private static final String PATH_SEPARATOR = buildPathSeparator();

    private static String buildPathSeparator() {
        String result = File.separator;
        if (System.getProperty("os.name").toLowerCase().contains("win") && !"\\".equals(result)) {
            result = "\\";
        }
        return result;
    }

    private static final String UTF8 = "UTF-8";

    private final static Unzip unzip = new Unzip();

    // =========================================================================
    // ASSERTS
    // =========================================================================
    public static void assertFileExists(final File file) {
        Asserts.notNull("file mustn't be null!", file);
        Asserts.isTrue(String.format("file %s dosen't exists", getCanonicalPath(file)), file.exists());
    }

    public static void assertCanRead(final File file) {
        Asserts.notNull("file mustn't be null!", file);
        Asserts.isTrue(String.format("can't read file %s", getCanonicalPath(file)), file.canRead());
    }

    public static void assertCanWrite(final File file) {
        Asserts.notNull("file mustn't be null!", file);
        Asserts.isTrue(String.format("can't write file %s", getCanonicalPath(file)), file.canWrite());
    }

    public static void assertIsFolder(File file) {
        Asserts.notNull("file mustn't be null!", file);
        Asserts.isTrue(String.format("file %s isn't folder", getCanonicalPath(file)), file.isDirectory());
    }

    public static void assertIsFile(File file) {
        Asserts.notNull("file mustn't be null!", file);
        Asserts.isTrue(String.format("file %s isn't file", getCanonicalPath(file)), file.isFile());
    }

    // =========================================================================
    // BUILD FILES
    // =========================================================================
    public static File buildFile(File file, String... part) {
        return new File(buildPath(file, part));
    }

    public static String buildPath(File file, String... part) {
        String[] parts = new String[part.length + 1];
        parts[0] = file.getAbsolutePath();
        System.arraycopy(part, 0, parts, 1, part.length);
        return String.join(PATH_SEPARATOR, parts);
    }

    // =========================================================================
    // FILE INFO
    // =========================================================================
    public static String getCanonicalPath(final File file) {
        Asserts.notNull("file mustn't be null!", file);
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    // =========================================================================
    // METHODS
    // =========================================================================
    public static String readFileFromClassLoader(final String resourceName) throws FilesUtilsException {
        String result = null;
        try {
            byte[] data = new FilesUtils().readFromClassLoader(resourceName);
            result = new String(data);
        } catch (FilesUtilsException e) {
            throw new FilesUtilsException(e.getMessage(), e);
        }
        return result;
    }

    public byte[] readFromClassLoader(final String resourceName) throws FilesUtilsException {
        Asserts.notNull(resourceName);
        final String realResourceName = resourceName.trim();
        byte[] result = null;
        final InputStream resource = this.getClass().getClassLoader().getResourceAsStream(realResourceName);
        if (resource == null) {
            throw new FilesUtilsException("can't found file {0} in classPath", realResourceName);
        }

        try {
            result = IOUtils.toByteArray(resource);
        } catch (IOException e) {
            throw new FilesUtilsException(e.getMessage(), e);
        }

        return result;
    }

    // =========================================================================
    // READ
    // =========================================================================

    public static String read(File file, String encoding) throws IOException {
        return read(file, DEFAULT_BUFFER_SIZE, encoding);
    }

    public static String read(File file) throws IOException {
        return read(file, DEFAULT_BUFFER_SIZE, UTF8);
    }

    public static String read(File file, int bufferSize) throws IOException {
        return read(file, bufferSize, UTF8);
    }

    public static String read(File file, int bufferSize, String encoding) throws IOException {
        Asserts.notNull(file);

        final StringBuilder data = processReading(file, bufferSize, encoding);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("done reading : nb chars : {}", data.length());
        }

        String result = data.toString();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("reading done");
        }
        return result;
    }

    private static StringBuilder processReading(File file, int bufferSize, String encoding) throws IOException {
        final RandomAccessFile aFile = new RandomAccessFile(file, "r");
        final FileChannel inChannel = aFile.getChannel();

        final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        long fileSize = inChannel.size();

        int fileRead = 0;

        final Double max = (double) fileSize * 1.2;
        final StringBuilder data = new StringBuilder(max.intValue());

        while (inChannel.read(buffer) > 0) {
            buffer.flip();
            final ReadPartResult readPartResult = readPart(buffer, fileRead, bufferSize, encoding);
            fileRead = readPartResult.getCursor();
            data.append(readPartResult.getData());
            buffer.clear();
            if (fileRead % MEGA == 0) {
                LOGGER.info("read rest :  {}", fileSize - fileRead);
            }
        }
        inChannel.close();
        aFile.close();
        return data;
    }

    private static ReadPartResult readPart(final ByteBuffer buffer, int oldCursor, int bufferSize, String encoding)
            throws UnsupportedEncodingException {
        int cursor = oldCursor;

        byte[] data = new byte[bufferSize];
        for (int i = 0; i < buffer.limit(); i++) {
            data[i] = buffer.get();
            cursor++;
        }

        return new ReadPartResult(cursor, new String(data, encoding));
    }

    private static class ReadPartResult {
        final int cursor;

        final String data;

        public ReadPartResult(int cursor, String data) {
            super();
            this.cursor = cursor;
            this.data = data;
        }

        public int getCursor() {
            return cursor;
        }

        public String getData() {
            return data;
        }

    }

    // =========================================================================
    // WRITE
    // =========================================================================
    public static void write(String content, String file) throws FilesUtilsException {
        write(content, new File(file));
    }

    public static void write(String content, File file) throws FilesUtilsException {
        write(content, file, "UTF-8");

    }

    public static void write(String content, File file, String encoding) throws FilesUtilsException {
        Asserts.notNull(file);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, encoding);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new FilesUtilsException(e.getMessage(), e);
        }

        Asserts.notNull(writer);
        try {
            if (content != null) {
                writer.println(content);
                LOGGER.info("write file : {}", file.getCanonicalFile().getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } finally {
            writer.close();
        }

    }

    // =========================================================================
    // LIST FILES
    // =========================================================================
    public static List<File> list(File folder) {
        return list(folder, null);
    }

    public static List<File> list(File folder, FilenameFilter filter) {
        assertCanRead(folder);
        assertIsFolder(folder);

        final String[] names = filter == null ? folder.list() : folder.list(filter);
        final List<String> filesNames = Arrays.asList(names);

        //@formatter:off
        return filesNames.stream()
                         .map(name->buildFile(folder, name))
                         .collect(Collectors.toList());
        //@formatter:on
    }

    // =========================================================================
    // DELEGATE
    // =========================================================================
    public static void unzip(File zipFile, File destination) throws IOException {
        unzip.unzip(zipFile, destination);
        LOGGER.info("unzip {} to {}", zipFile, destination);
    }

    public static void unzipLogless(File zipFile, File destination) throws IOException {
        unzip.unzipLogLess(zipFile, destination);
        LOGGER.info("unzip {} to {}", zipFile, destination);
    }

    public static void zip(File warUnzipped, File zipFile) throws IOException {
        unzip.zip(warUnzipped, zipFile);
    }

    public static boolean delete(File file) {
        LOGGER.info("delete {}", file);
        boolean result = false;
        try {
            FileUtils.forceDelete(file);
            result = true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return result;
    }

    public static boolean copy(File source, File destination) {
        boolean result = false;
        LOGGER.info("copy {} to {}", source, destination);
        try {
            if (source.isDirectory()) {
                FileUtils.copyDirectory(source, destination);
            } else {
                FileUtils.copyFileToDirectory(source, destination);
            }
            result = true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return result;
    }

    public static void copyTemplate(File baseTemplate, File server, Map<String, String> values)
            throws FilesUtilsException {
        new CopyTemplateProcessor(baseTemplate, server, values).process();
    }

    public static byte[] readBytes(File file) throws IOException {
        return FileUtils.readFileToByteArray(file);
    }

    public static File getTmpDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    public static String cleanFolderPath(String path) {
        return path.replaceAll(":", "_").replaceAll("[.]", "_");
    }

}
