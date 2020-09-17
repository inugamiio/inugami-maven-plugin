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
package io.inugami.maven.plugin.mojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MojoHelper
 * 
 * @author patrick_guillerm
 * @since 20 juin 2017
 */
public class MojoHelper {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final static Logger LOGGER = LoggerFactory.getLogger("MVN");

    private static final String EMPTY_STR = "";

    private final Logger specificLogger;

    // =========================================================================
    // METHODS
    // =========================================================================
    public MojoHelper() {
        this(null);
    }

    public MojoHelper(Logger logger) {
        this.specificLogger = logger == null ? LOGGER : logger;
    }

    // =========================================================================
    // METHODS
    // =========================================================================
    public void drawDeco(final String message, final String deco) {
        final String line = createLine(deco, 80);
        specificLogger.info(EMPTY_STR);
        specificLogger.info(EMPTY_STR);
        specificLogger.info(line);
        specificLogger.info("{} {}", deco, message);
        specificLogger.info(line);
    }

    // =========================================================================
    // TOOLS
    // =========================================================================
    public void drawLine(String deco, int size) {
        specificLogger.info(createLine(deco, size));
    }

    public String createLine(String deco, int size) {
        final StringBuilder result = new StringBuilder();
        for (int i = size - 1; i >= 0; i--) {
            result.append(deco);
        }
        return result.toString();
    }

}
