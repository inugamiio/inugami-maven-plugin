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

/**
 * Potentiometer
 * 
 * @author patrick_guillerm
 * @since 27 juin 2017
 */
public class Potentiometer {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final String key;

    private final boolean enable;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    public Potentiometer(String value, String key) {
        super();
        this.key = key;
        enable = value == null || "".equals(value.trim());
    }

    // =========================================================================
    // OVERRIDES
    // =========================================================================

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime * (enable ? 1231 : 1237);
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = this == obj;

        if (!result && obj != null && obj instanceof Potentiometer) {
            final Potentiometer other = (Potentiometer) obj;
            result = key == null ? other.getKey() == null : key.equals(other.getKey());
            result = result && (enable == other.isEnable());
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Potentiometer [key=");
        builder.append(key);
        builder.append(", enable=");
        builder.append(enable);
        builder.append("]");
        return builder.toString();
    }

    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================
    public boolean isEnable() {
        return enable;
    }

    public String getKey() {
        return key;
    }

}
