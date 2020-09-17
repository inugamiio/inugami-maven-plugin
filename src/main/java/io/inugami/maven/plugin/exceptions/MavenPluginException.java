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
package io.inugami.maven.plugin.exceptions;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * MavenPluginException
 * 
 * @author patrick_guillerm
 * @since 22 juin 2017
 */
public class MavenPluginException extends MojoExecutionException {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private static final long serialVersionUID = 4257892043563931393L;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    public MavenPluginException(Object source, String shortMessage, String longMessage) {
        super(source, shortMessage, longMessage);
    }

    public MavenPluginException(String message, Exception cause) {
        super(message, cause);
    }

    public MavenPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public MavenPluginException(String message) {
        super(message);
    }

    public MavenPluginException(final String message, final Object... values) {
        this(MessagesFormatter.format(message, values));
    }

    public MavenPluginException(final Throwable cause, final String message, final Object... values) {
        this(MessagesFormatter.format(message, values), cause);
    }
}
