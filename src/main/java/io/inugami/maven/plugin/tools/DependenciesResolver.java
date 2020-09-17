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
package io.inugami.maven.plugin.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.inugami.maven.plugin.models.Gav;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * DependenciesResolver
 * 
 * @author patrick_guillerm
 * @since 20 juil. 2017
 */
public class DependenciesResolver {

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final List<RemoteRepository> repositories;

    private final RepositorySystem repoSystem;

    private final RepositorySystemSession repoSession;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    public DependenciesResolver(List<RemoteRepository> repositories, final RepositorySystem repoSystem,
            final RepositorySystemSession repoSession) {
        super();
        this.repositories = repositories;
        this.repoSystem = repoSystem;
        this.repoSession = repoSession;
    }

    // =========================================================================
    // METHODS
    // =========================================================================
    public List<Gav> resolve(MavenProject project) {
        final List<Gav> result = new ArrayList<>();

        //@formatter:off
        final List<Dependency> dependencies = ((List<Dependency>)project.getDependencies())
                                                                        .stream()
                                                                        .filter(this::matchScopes)
                                                                        .collect(Collectors.toList());
        //@formatter:on
        if (!dependencies.isEmpty()) {
            result.addAll(resolveDependenciesPath(dependencies));
        }
        return result;
    }

    // =========================================================================
    // PRIVATE
    // =========================================================================
    private List<Gav> resolveDependenciesPath(List<Dependency> dependencies) {
        //@formatter:off
        return dependencies.stream()
                    .map(this::mapArtifact)
                    .map(this::resolveArtifact)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        //@formatter:on
    }

    private Artifact mapArtifact(Dependency item) {
        //@formatter:off
        return new DefaultArtifact(item.getGroupId(),
                            item.getArtifactId(),
                            item.getClassifier(),
                            item.getType(),
                            item.getVersion());
        //@formatter:on
    }

    private Gav resolveArtifact(Artifact artifact) {
        //@formatter:off
        final ArtifactRequest req = new ArtifactRequest().setRepositories(this.repositories)
                                                         .setArtifact(artifact);
        //@formatter:on
        ArtifactResult resolutionResult = null;
        try {
            resolutionResult = repoSystem.resolveArtifact(this.repoSession, req);
        } catch (ArtifactResolutionException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        final File file = resolutionResult.getArtifact().getFile();

        //@formatter:off
        return new Gav(artifact.getGroupId(),
                       artifact.getArtifactId(),
                       artifact.getVersion(),
                       artifact.getExtension(),
                       file);
      //@formatter:on
    }

    // =========================================================================
    // TOOLS
    // =========================================================================
    private boolean matchScopes(Dependency depend) {
        return "compile".equals(depend.getScope());
    }
}
