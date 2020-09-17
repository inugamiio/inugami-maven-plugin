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

/**
 * ServerServicesException
 * 
 * @author patrick_guillerm
 * @since 22 juin 2017
 */
public class ServerServicesException extends MavenPluginException {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private static final long serialVersionUID = 2826502089929468481L;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    public ServerServicesException(Object source, String shortMessage, String longMessage) {
        super(source, shortMessage, longMessage);
    }

    public ServerServicesException(String message, Exception cause) {
        super(message, cause);
    }

    public ServerServicesException(String message, Object... values) {
        super(message, values);
    }

    public ServerServicesException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerServicesException(String message) {
        super(message);
    }

    public ServerServicesException(Throwable cause, String message, Object... values) {
        super(cause, message, values);
    }
}
