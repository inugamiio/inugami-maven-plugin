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

import org.inugami.maven.plugin.exceptions.MavenPluginException;

/**
 * FileUtilsException
 * 
 * @author patrick_guillerm
 * @since 22 juin 2017
 */
public class FilesUtilsException extends MavenPluginException {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private static final long serialVersionUID = -179575061212853038L;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    public FilesUtilsException(Object source, String shortMessage, String longMessage) {
        super(source, shortMessage, longMessage);
    }

    public FilesUtilsException(String message, Exception cause) {
        super(message, cause);
    }

    public FilesUtilsException(String message, Object... values) {
        super(message, values);
    }

    public FilesUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilesUtilsException(String message) {
        super(message);
    }

    public FilesUtilsException(Throwable cause, String message, Object... values) {
        super(cause, message, values);
    }
}
