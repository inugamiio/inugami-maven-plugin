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
package io.inugami.maven.plugin.services.tsConfig;

public class TypeScriptDependency {
    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final String name;
    
    private final String path;
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    public TypeScriptDependency(String name, String path) {
        super();
        this.name = name;
        this.path = path;
    }
    
    // =========================================================================
    // OVERRIDES
    // =========================================================================
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean result = this == obj;
        
        if (!result && obj != null && obj instanceof TypeScriptDependency) {
            TypeScriptDependency other = (TypeScriptDependency) obj;
            result = name == null ? other.getName() == null : name.equals(other.getName());
        }
        
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TypeScriptDependency [name=");
        builder.append(name);
        builder.append(", path=");
        builder.append(path);
        builder.append("]");
        return builder.toString();
    }
    
    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================
    public String getName() {
        return name;
    }
    
    public String getPath() {
        return path;
    }
    
}
