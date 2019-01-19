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
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.inugami.maven.plugin.exceptions.Asserts;
import org.inugami.maven.plugin.exceptions.ServerServicesException;
import org.inugami.maven.plugin.mojo.MojoHelper;
import org.inugami.maven.plugin.tools.FilesUtils;
import org.inugami.maven.plugin.tools.FilesUtilsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * ServerServices
 * 
 * @author patrick_guillerm
 * @since 22 juin 2017
 */
public class ServerServices {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerServices.class.getSimpleName());

    public static final String PLUGINS_FOLDER = "plugins";

    public static final String LIBS_FOLDER = "libs";

    public static final String WEBAPPS = "webapps";

    public static final String TOMCAT = "tomcat";

    public static final String LOGS_FOLDER = "logs";

    public static final String INUGAMI_WEBAPP = "inugami_webapp";

    public static final String INUGAMI_WEBAPP_WAR = INUGAMI_WEBAPP + ".war";

    private static final String FILE_PROTOCOL = "file:";

    private static final String JAR_EXTENSION = ".jar";

    private static final String BIN_FOLDER = "bin";

    private final static MojoHelper helper = new MojoHelper(LOGGER);

    private final List<String> serverFolders = Arrays.asList(new String[] { PLUGINS_FOLDER, LOGS_FOLDER,
            "configurations", LIBS_FOLDER });

    // =========================================================================
    // BUILD SERVER
    // =========================================================================
    public void buildServer(String server, File tomcatZip, File inugamiFile, File basedir,
            Map<String, String> properties, boolean forceClean) throws ServerServicesException {
        Asserts.notNull("Server home mustn't be null!", server);
        Asserts.notNull("project base dir mustn't be null!", basedir);
        Asserts.notNull("tomcat zip mustn't be null", tomcatZip);
        FilesUtils.assertCanRead(tomcatZip);

        LOGGER.info("build server....");
        helper.drawLine("+", 80);
        processBuildServer(new File(server), tomcatZip, inugamiFile, properties, forceClean);
        helper.drawLine("+", 80);
        LOGGER.info("build server done : {}", server);
    }

    private void processBuildServer(File server, File tomcatZip, File inugamiFile, Map<String, String> properties,
            boolean forceClean) throws ServerServicesException {
        buildServerFolders(server, forceClean);
        unzipTomcatServer(tomcatZip, server);
        cleanTomcatServer(server);
        deployApplication(server, inugamiFile);
        deployTomcatConfiguration(server, properties);
        setScriptExecutable(server);
    }

    // -------------------------------------------------------------------------
    // BUILD SERVER - steps buildServerFolders
    // -------------------------------------------------------------------------
    private void buildServerFolders(File server, boolean forceClean) throws ServerServicesException {
        printStep("build server folders");
        if (server.exists()) {
            if (forceClean) {
                LOGGER.warn("server already exists : {0}...", server);
                LOGGER.warn("process delete server....");
                FilesUtils.delete(server);
            } else {
                throw new ServerServicesException("server already exists : {0}", server);
            }

        }
        LOGGER.info("create folders : {}", server);
        boolean mkdirResult = server.mkdirs();
        if (!mkdirResult) {
            throw new ServerServicesException("can't create server in folder : {0}", server);
        }

        //@formatter:off
        serverFolders.stream()
                     .map(fileName-> new File(FilesUtils.buildPath(server, fileName)))
                     .forEach(file-> file.mkdirs());
        //@formatter:on
    }

    // -------------------------------------------------------------------------
    // BUILD SERVER - steps unzipTomcatServer
    // -------------------------------------------------------------------------
    private void unzipTomcatServer(File tomcatZip, File server) throws ServerServicesException {
        printStep("unzip tomcat server");
        try {
            FilesUtils.unzip(tomcatZip, server);
        } catch (IOException e) {
            throw new ServerServicesException(e.getMessage());
        }

        final List<String> files = Arrays.asList(server.list());
        final Optional<String> tomcatName = files.stream().filter(file -> file.contains(TOMCAT)).findFirst();
        if (!tomcatName.isPresent()) {
            throw new ServerServicesException("error on unzipping tomcat server!");
        }

        final File tomcatDir = FilesUtils.buildFile(server, tomcatName.get());
        tomcatDir.renameTo(new File(FilesUtils.buildPath(server, TOMCAT)));

    }

    // -------------------------------------------------------------------------
    // BUILD SERVER - steps cleanTomcatServer
    // -------------------------------------------------------------------------
    private void setScriptExecutable(File server) throws ServerServicesException {
        final File bin = new File(FilesUtils.buildPath(server, TOMCAT, BIN_FOLDER));
        FilesUtils.assertCanRead(bin);

        final List<String> scripts = Arrays.asList(bin.list((dir, name) -> name.endsWith(".sh")));
        //@formatter:off
        scripts.stream()
               .map(name-> FilesUtils.buildFile(server,TOMCAT,BIN_FOLDER,name))
               .forEach(file -> file.setExecutable(true));
        //@formatter:on

    }

    // -------------------------------------------------------------------------
    // BUILD SERVER - steps cleanTomcatServer
    // -------------------------------------------------------------------------
    private void cleanTomcatServer(File server) {
        final File tomcat = new File(FilesUtils.buildPath(server, TOMCAT));
        FilesUtils.assertFileExists(tomcat);

        final File webappFolder = new File(FilesUtils.buildPath(tomcat, WEBAPPS));
        FilesUtils.assertFileExists(webappFolder);

        final String[] subFolders = webappFolder.list();
        for (String fileName : subFolders) {
            final File dir = FilesUtils.buildFile(webappFolder, fileName);
            FilesUtils.assertFileExists(dir);
            FilesUtils.delete(dir);
        }

    }

    // -------------------------------------------------------------------------
    // BUILD SERVER - steps deployApplication
    // -------------------------------------------------------------------------
    private void deployApplication(File server, File inugamiFile) throws ServerServicesException {
        printStep("deploy application");

        final File webapp = FilesUtils.buildFile(server, TOMCAT, WEBAPPS);
        FilesUtils.assertCanWrite(webapp);
        FilesUtils.copy(inugamiFile, webapp);

        final List<String> files = Arrays.asList(webapp.list());
        Optional<String> fileOpt = files.stream().filter(name -> name.contains("inugami")).findFirst();
        if (!fileOpt.isPresent()) {
            throw new ServerServicesException("error on install inugami application!");
        }

        final File inugamiWebapp = FilesUtils.buildFile(server, TOMCAT, WEBAPPS, fileOpt.get());
        final File warFile = FilesUtils.buildFile(server, TOMCAT, WEBAPPS, INUGAMI_WEBAPP_WAR);
        inugamiWebapp.renameTo(warFile);
    }

    // -------------------------------------------------------------------------
    // BUILD SERVER - steps deployTomcatConfiguration
    // -------------------------------------------------------------------------
    private void deployTomcatConfiguration(File server, Map<String, String> properties) throws ServerServicesException {
        printStep("deploy tomcat configuration");

        final Map<String, String> values = new HashMap<>();
        //@formatter:off
        values.put("super.inugami.plugin.folder.plugin", FilesUtils.buildFile(server, PLUGINS_FOLDER).getAbsolutePath());
        values.put("super.inugami.plugin.folder.server", FilesUtils.buildFile(server, TOMCAT).getAbsolutePath());
        values.put("super.inugami.plugin.folder.logs"  , FilesUtils.buildFile(server, LOGS_FOLDER).getAbsolutePath());
        values.put("super.inugami.plugin.folder.lib"  , FilesUtils.buildFile(server, LIBS_FOLDER).getAbsolutePath());
        values.put("super.inugami.plugin.server"  , server.getAbsolutePath());
        //@formatter:on

        if (properties != null) {
            values.putAll(properties);
        }

        final URL baseTemplateURL = this.getClass().getResource("/META-INF/inugami");
        Asserts.notNull(baseTemplateURL);
        File baseTemplate = new File(baseTemplateURL.getFile());
        if ("jar".equals(baseTemplateURL.getProtocol())) {
            baseTemplate = extractTemplate(baseTemplateURL);
        }

        try {
            FilesUtils.copyTemplate(baseTemplate, server, values);
        } catch (FilesUtilsException e) {
            throw new ServerServicesException(e.getMessage());
        }
    }

    // =========================================================================
    // TOOLS
    // =========================================================================
    private File extractTemplate(URL baseTemplateURL) throws ServerServicesException {
        final String fullPath = baseTemplateURL.getFile();
        final int indexExtension = fullPath.lastIndexOf(JAR_EXTENSION);

        String path = fullPath.substring(0, indexExtension + 4);
        if (path.startsWith(FILE_PROTOCOL)) {
            path = path.substring(FILE_PROTOCOL.length());
        }

        final String tempDir = System.getProperty("java.io.tmpdir");
        final File tmpDirectory = new File(tempDir + File.separator + "inugami-plugin-maven");
        try {
            FilesUtils.unzip(new File(path), tmpDirectory);
        } catch (IOException e) {
            throw new ServerServicesException(e.getMessage());
        }

        String context = fullPath.substring(path.length() + JAR_EXTENSION.length() + 1);
        if (context.startsWith("!")) {
            context = context.substring(1);
        }

        final StringBuilder newPath = new StringBuilder();
        newPath.append(FilesUtils.getCanonicalPath(tmpDirectory));
        newPath.append(context);

        return new File(newPath.toString());
    }

    private void printStep(final String message) {
        helper.drawDeco(message, "-");
    }

}
