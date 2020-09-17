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
package io.inugami.maven.plugin.models;

import java.io.File;

public class FileMovePath {
    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final File from;
    
    private final File to;
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    public FileMovePath(File from, File to) {
        super();
        this.from = from;
        this.to = to;
    }
    
    // =========================================================================
    // OVERRIDES
    // =========================================================================
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((to == null) ? 0 : to.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean result = this == obj;
        
        if (!result && obj != null && obj instanceof FileMovePath) {
            FileMovePath other = (FileMovePath) obj;
            result = from == null ? other.getFrom() == null : from.equals(other.getFrom());
        }
        
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FileMovePath [from=");
        builder.append(from);
        builder.append(", to=");
        builder.append(to);
        builder.append("]");
        return builder.toString();
    }
    
    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================
    public File getFrom() {
        return from;
    }
    
    public File getTo() {
        return to;
    }
    
}
