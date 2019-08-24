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
package org.inugami.maven.plugin.services.tsConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TsConfigRenderer {
    
    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final String sourcesDest;
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public TsConfigRenderer(String sourcesDest) {
        this.sourcesDest = sourcesDest;
    }
    
    // =========================================================================
    // METHODS
    // =========================================================================
    public String render(Set<TypeScriptDependency> dependencies) {
        StringBuilder json = new StringBuilder();
        
        json.append("{").append('\n');
        json.append("    \"compilerOptions\": {").append('\n');
        json.append("      \"baseUrl\": \"./\",").append('\n');
        json.append("      \"outDir\": \"./dist/out-tsc\",").append('\n');
        json.append("      \"typeRoots\": [").append('\n');
        json.append("        \"node_modules/@types\"").append('\n');
        json.append("      ],").append('\n');
        json.append("      \"rootDirs\": [").append('\n');
        json.append("        \"src/main/resources/META-INF/resources/js/**/*.ts\"").append('\n');
        json.append("      ],").append('\n');
        json.append("      \"paths\" : {").append('\n');
        
        final Iterator<TypeScriptDependency> iterator = order(dependencies).iterator();
        while (iterator.hasNext()) {
            renderDependency(json, iterator.next());
            if (iterator.hasNext()) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("      },").append('\n');
        json.append("      \"lib\": [").append('\n');
        json.append("        \"es2018\",").append('\n');
        json.append("        \"dom\"").append('\n');
        json.append("      ]").append('\n');
        json.append("    },").append('\n');
        json.append("    \"include\": [").append('\n');
        json.append("      \"src/**/*\",").append('\n');
        json.append("      \"sources_plugins/**/*\"").append('\n');
        json.append("    ]").append('\n');
        json.append("} ").append('\n');
        return json.toString();
    }
    
    private List<TypeScriptDependency> order(Set<TypeScriptDependency> dependencies) {
        List<TypeScriptDependency> result = new ArrayList<>(dependencies);
        result.sort(new Comparator<TypeScriptDependency>() {
            @Override
            public int compare(TypeScriptDependency ref, TypeScriptDependency value) {
                return ref.getName().compareTo(value.getName());
            }
        });
        return result;
    }
    
    // =========================================================================
    // OVERRIDES
    // =========================================================================
    private void renderDependency(StringBuilder json, TypeScriptDependency value) {
        json.append('"');
        json.append(value.getName());
        json.append('"');
        json.append(":[");
        json.append('"');
        
        json.append("./");
        json.append(sourcesDest);
        json.append('/');
        json.append(value.getPath());
        
        json.append('"');
        json.append(']');
    }
    
}
