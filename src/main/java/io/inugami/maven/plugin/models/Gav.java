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

/**
 * Gav
 * 
 * @author patrick_guillerm
 * @since 20 juin 2017
 */
public class Gav {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String hash;

    private final File path;

    private final String type;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    public Gav(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, null, null);
    }

    public Gav(Gav gav, File path) {
        this(gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getType(), path);
    }

    public Gav(String groupId, String artifactId, String version, String type, File path) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.path = path;
        this.type = type;
        this.hash = String.join(":", groupId, artifactId, version);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Gav [groupId=");
        builder.append(groupId);
        builder.append(", artifactId=");
        builder.append(artifactId);
        builder.append(", version=");
        builder.append(version);
        builder.append(", hash=");
        builder.append(hash);
        builder.append("]");
        return builder.toString();
    }

    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================

    public String getGroupId() {
        return groupId;
    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = this == obj;
        if (!result && obj != null && obj instanceof Gav) {
            final Gav other = (Gav) obj;
            result = hash.equals(other.getHash());
        }

        return result;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getHash() {
        return hash;
    }

    public File getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

}
