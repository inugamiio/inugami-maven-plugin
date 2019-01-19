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

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

/**
 * CopyTemplateProcessorTest
 * 
 * @author patrick_guillerm
 * @since 23 juin 2017
 */
public class CopyTemplateProcessorTest {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================

    // =========================================================================
    // METHODS
    // =========================================================================
    @Test
    public void testBuildServerPath() throws Exception {
        final CopyTemplateProcessor processor = new CopyTemplateProcessor(new File("/foo/bar"), new File("/server"),
                null);

        assertEquals("/server/joe", processor.buildServerPath(new File("/foo/bar/joe")));
        assertEquals("/server/joe/titi.json", processor.buildServerPath(new File("/foo/bar/joe/titi.json")));
        

    }

}
