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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TemplateRenderingTest
 * 
 * @author patrick_guillerm
 * @since 23 juin 2017
 */
public class TemplateRenderingTest {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final static Logger LOGGER = LoggerFactory.getLogger(TemplateRenderingTest.class);

    // =========================================================================
    // METHODS
    // =========================================================================
    @Test
    public void testApplyProperty() throws Exception {
        final TemplateRendering engine = new TemplateRendering();
        assertEquals("hello foobar", engine.applyProperty("${foobar}", "foobar", "hello foobar"));
        assertEquals("${foobar} TITI", engine.applyProperty("${foobar} ${titi_foo}", "titi_foo", "TITI"));

        assertEquals("C:\\foo\\bar", engine.applyProperty("C:\\foo\\${bar}", "bar", "bar"));

    }

    @Test
    public void testRender() throws Exception {
        final TemplateRendering engine = new TemplateRendering();
        final Map<String, String> properties = new HashMap<>();
        properties.put("foobar", "Hello foo bar");

        File testAFile = loadFile("/testA.txt");
        String testA = engine.render(testAFile, properties);

        assertEquals("Hello foo bar", testA.trim());
    }

    @Test
    public void testRenderSetEnvWindows() throws Exception {
        final TemplateRendering engine = new TemplateRendering();

        final Map<String, String> values = new HashMap<>();
        //@formatter:off
        values.put("super.inugami.plugin.folder.plugin", "C:\\foo\\server\\plugins");
        values.put("super.inugami.plugin.folder.server", "C:\\foo\\server\\tomcat");
        values.put("super.inugami.plugin.folder.logs"  , "C:\\foo\\server\\logs");
        values.put("super.inugami.plugin.folder.lib"  , "C:\\foo\\server\\lib");
        values.put("super.inugami.plugin.server"  , "C:\\foo\\server");
        //@formatter:on

        File file = loadFile("/setenv.bat");
        String content = engine.render(file, values);

        LOGGER.info(content);
        assertFalse(content.contains("super[.]inugami[.]plugin[.]folder[.]server"));
        assertEquals("set JAVA_OPTS=\"-Dlogback.configurationFile=C:\\foo\\server\\tomcat\\conf\\logback.xml -Dinugami-home=C:\\foo\\server\\configurations\"",
                     content);
    }

    // =========================================================================
    // TOOLS
    // =========================================================================
    private File loadFile(String path) throws URISyntaxException {
        URL fileURL = this.getClass().getResource(path);
        return new File(fileURL.toURI());
    }

}
