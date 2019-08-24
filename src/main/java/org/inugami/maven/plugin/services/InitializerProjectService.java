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
package org.inugami.maven.plugin.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.inugami.maven.plugin.models.FileMovePath;
import org.inugami.maven.plugin.models.Gav;
import org.inugami.maven.plugin.services.tsConfig.TsConfigRenderer;
import org.inugami.maven.plugin.services.tsConfig.TypeScriptDependency;
import org.inugami.maven.plugin.tools.FilesUtils;
import org.inugami.maven.plugin.tools.FilesUtilsException;
import org.inugami.maven.plugin.tools.Unzip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flexjson.JSONDeserializer;

public class InitializerProjectService {
    
    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    private final static Logger LOGGER       = LoggerFactory.getLogger(InitializerProjectService.class.getSimpleName());
    
    private final static String SOURCES_DEST = "sources_plugins";
    
    // =========================================================================
    // METHODS
    // =========================================================================
    public void initializeProject(File basedir, List<Gav> dependencies,
                                  File inugamiRootPlugin) throws FilesUtilsException, IOException {
        final File dest = FilesUtils.buildFile(basedir, SOURCES_DEST);
        createSourceFolder(dest);
        final Set<TypeScriptDependency> tsDependencies = deploySourceDependencies(dependencies, inugamiRootPlugin, dest,
                                                                                  basedir);
        writeTsConfigFile(basedir, tsDependencies);
    }
    
    // =========================================================================
    // OVERRIDES
    // =========================================================================
    private void createSourceFolder(File dest) {
        if (dest.exists()) {
            dest.delete();
        }
        dest.mkdir();
    }
    
    private Set<TypeScriptDependency> deploySourceDependencies(List<Gav> dependencies, File inugamiRootPlugin,
                                                               File dest, File basedir) throws IOException {
        final Set<TypeScriptDependency> result = new HashSet<>();
        File tmpDeployPath = FilesUtils.buildFile(basedir, "target", "plugins");
        if (tmpDeployPath.exists()) {
            tmpDeployPath.delete();
        }
        deployPlugin(inugamiRootPlugin, tmpDeployPath);
        
        if (dependencies != null && !dependencies.isEmpty()) {
            for (Gav gav : dependencies) {
                deployPlugin(gav.getPath(), tmpDeployPath);
            }
            result.addAll(buildDependencies(tmpDeployPath, basedir));
        }
        
        copyTmpFolderToDest(tmpDeployPath, dest);
        return result;
    }
    
    private void writeTsConfigFile(File basedir,
                                   final Set<TypeScriptDependency> tsDependencies) throws FilesUtilsException {
        final Set<TypeScriptDependency> dependencies = new HashSet<>(tsDependencies);
        dependencies.addAll(addMainDependencies());
        String content = new TsConfigRenderer(SOURCES_DEST).render(dependencies);
        
        File tsConfigFile = FilesUtils.buildFile(basedir, "tsconfig.json");
        FilesUtils.write(content, tsConfigFile);
    }
    
    // =========================================================================
    // DEPLOY
    // =========================================================================
    private void deployPlugin(File jarFile, File tmpFolder) throws IOException {
        new Unzip().unzip(jarFile, FilesUtils.buildFile(tmpFolder, jarFile.getName()));
    }
    
    private void copyTmpFolderToDest(File tmpDeployPath, File dest) throws IOException {
        final List<FileMovePath> filesToMove = new ArrayList<>();
        String[] plugins = tmpDeployPath.list();
        for (String plugin : plugins) {
            File pluginPath = FilesUtils.buildFile(tmpDeployPath, plugin);
            filesToMove.addAll(searchFiles(pluginPath, pluginPath, dest));
        }
        
        for (FileMovePath fileToMove : filesToMove) {
            LOGGER.info("copy file : {}", fileToMove.getTo());
            FilesUtils.move(fileToMove.getFrom(), fileToMove.getTo());
        }
    }
    
    private List<FileMovePath> searchFiles(File currentPath, File pluginPath, File dest) {
        List<FileMovePath> result = new ArrayList<>();
        
        if (currentPath.isDirectory()) {
            String[] subPaths = currentPath.list();
            for (String subPath : subPaths) {
                result.addAll(searchFiles(FilesUtils.buildFile(currentPath, subPath), pluginPath, dest));
            }
        }
        else {
            String path = resolveCurrentPath(currentPath, pluginPath);
            if (path.startsWith("/META-INF/resources/")) {
                result.add(new FileMovePath(currentPath, buildDestPath(path, dest)));
            }
        }
        
        return result;
    }
    
    private String resolveCurrentPath(File currentPath, File pluginPath) {
        String fullPluginPath = String.join("/", pluginPath.getAbsolutePath().split(File.separator));
        String fullCurrentPath = String.join("/", currentPath.getAbsolutePath().split(File.separator));
        return fullCurrentPath.substring(fullPluginPath.length());
    }
    
    private File buildDestPath(String path, File dest) {
        String localPath = String.join(File.separator, path.substring("/META-INF/resources".length()).split("/"));
        return FilesUtils.buildFile(dest, localPath);
    }
    
    // =========================================================================
    // PLUGIN INUGAMI
    // =========================================================================
    private Set<TypeScriptDependency> buildDependencies(File tmpDeployPath, File currentPath) throws IOException {
        Set<TypeScriptDependency> result = new HashSet<>();
        
        List<File> pluginConfigurationJson = searchPluginConfigurationJson(tmpDeployPath);
        for (File config : pluginConfigurationJson) {
            result.addAll(resolveSystemJSDependencies(config));
        }
        result.addAll(resolveCurrentPluginDependencies(currentPath));
        return result;
    }
    
    private List<File> searchPluginConfigurationJson(File path) {
        List<File> result = new ArrayList<>();
        if (path.isDirectory()) {
            for (String file : path.list()) {
                result.addAll(searchPluginConfigurationJson(FilesUtils.buildFile(path, file)));
            }
            
        }
        else if (path.getName().equals("plugin-configuration.json")) {
            result.add(path);
        }
        return result;
    }
    
    private Set<TypeScriptDependency> resolveCurrentPluginDependencies(File currentPath) throws IOException {
        Set<TypeScriptDependency> result = new HashSet<>();
        //@formatter:off
        final File notFiltered = FilesUtils.buildFile(currentPath, "src","main","resources-filtered","META-INF","plugin-configuration.json");
        final File filtered = FilesUtils.buildFile(currentPath, "src","main","resources-filtered","META-INF","plugin-configuration.json");
        //@formatter:on
        
        if (notFiltered.exists()) {
            result.addAll(resolveSystemJSDependencies(notFiltered));
        }
        
        if (filtered.exists()) {
            result.addAll(resolveSystemJSDependencies(filtered));
        }
        return result;
    }
    
    private Set<TypeScriptDependency> resolveSystemJSDependencies(File currentPath) throws IOException {
        Set<TypeScriptDependency> result = new HashSet<>();
        
        if (currentPath.exists()) {
            final Map<String, Object> data = new JSONDeserializer<Map<String, Object>>().deserialize(new String(FilesUtils.readBytes(currentPath)));
            if (data != null) {
                Map<String, String> systemJS = (Map<String, String>) data.get("systemMap");
                if (systemJS != null) {
                    for (Map.Entry<String, String> entry : systemJS.entrySet()) {
                        result.add(new TypeScriptDependency(entry.getKey(), entry.getValue()));
                    }
                }
            }
        }
        
        return result;
    }
    
    // =========================================================================
    // MAIN PLUGIN
    // =========================================================================
    private Set<TypeScriptDependency> addMainDependencies() {
        final Set<TypeScriptDependency> result = new HashSet<>();
        //@formatter:off
        result.add(new TypeScriptDependency("js/app/*"                              ,"js/app/*"));
        result.add(new TypeScriptDependency("@angular/core"                         ,"js/vendors/@angular/core/bundles/core.umd.min.js"));
        result.add(new TypeScriptDependency("@angular/common"                       ,"js/vendors/@angular/common/bundles/common.umd.min.js"));
        result.add(new TypeScriptDependency("@angular/compiler"                     ,"js/vendors/@angular/compiler/bundles/compiler.umd.min.js"));
        result.add(new TypeScriptDependency("@angular/platform-browser"             ,"js/vendors/@angular/platform-browser/bundles/platform-browser.umd.js"));
        result.add(new TypeScriptDependency("@angular/platform-browser-dynamic"     ,"js/vendors/@angular/platform-browser-dynamic/bundles/platform-browser-dynamic.umd.min.js"));
        result.add(new TypeScriptDependency("@angular/common/http"                  ,"js/vendors/@angular/common/bundles/common-http.umd.js"));
        result.add(new TypeScriptDependency("@angular/forms"                        ,"js/vendors/@angular/forms/bundles/forms.umd.min.js"));
        result.add(new TypeScriptDependency("@angular/platform-browser/animations"  ,"js/vendors/@angular/platform-browser/bundles/platform-browser-animations.umd.min.js"));
        result.add(new TypeScriptDependency("@angular/animations/browser"           ,"js/vendors/@angular/animations/bundles/animations-browser.umd.min.js"));
        result.add(new TypeScriptDependency("@angular/animations"                   ,"js/vendors/@angular/animations/bundles/animations.umd.min.js"));
        result.add(new TypeScriptDependency("@angular/router"                       ,"js/vendors/@angular/router/bundles/router.umd.min.js"));
        result.add(new TypeScriptDependency("ts"                                    ,"js/vendors/plugin-typescript/lib/plugin.js"));
        result.add(new TypeScriptDependency("typescript"                            ,"js/vendors/typescript/lib/typescript.js"));
        result.add(new TypeScriptDependency("core-js"                               ,"js/vendors/core-js"));
        result.add(new TypeScriptDependency("tslib"                                 ,"js/vendors/tslib/tslib.js"));
        result.add(new TypeScriptDependency("rxjs"                                  ,"js/vendors/rxjs-system-bundle/Rx.system.min.js"));
        result.add(new TypeScriptDependency("primeng/api"                           ,"js/vendors/primeng/api.js"));
        result.add(new TypeScriptDependency("primeng/accordion"                     ,"js/vendors/primeng/accordion.js"));
        result.add(new TypeScriptDependency("primeng/autocomplete"                  ,"js/vendors/primeng/autocomplete.js"));
        result.add(new TypeScriptDependency("primeng/blockui"                       ,"js/vendors/primeng/blockui.js"));
        result.add(new TypeScriptDependency("primeng/breadcrumb"                    ,"js/vendors/primeng/breadcrumb.js"));
        result.add(new TypeScriptDependency("primeng/button"                        ,"js/vendors/primeng/button.js"));
        result.add(new TypeScriptDependency("primeng/calendar"                      ,"js/vendors/primeng/calendar.js"));
        result.add(new TypeScriptDependency("primeng/captcha"                       ,"js/vendors/primeng/captcha.js"));
        result.add(new TypeScriptDependency("primeng/card"                          ,"js/vendors/primeng/card.js"));
        result.add(new TypeScriptDependency("primeng/carousel"                      ,"js/vendors/primeng/carousel.js"));
        result.add(new TypeScriptDependency("primeng/chart"                         ,"js/vendors/primeng/chart.js"));
        result.add(new TypeScriptDependency("primeng/checkbox"                      ,"js/vendors/primeng/checkbox.js"));
        result.add(new TypeScriptDependency("primeng/chips"                         ,"js/vendors/primeng/chips.js"));
        result.add(new TypeScriptDependency("primeng/codehighlighter"               ,"js/vendors/primeng/codehighlighter.js"));
        result.add(new TypeScriptDependency("primeng/colorpicker"                   ,"js/vendors/primeng/colorpicker.js"));
        result.add(new TypeScriptDependency("primeng/confirmdialog"                 ,"js/vendors/primeng/confirmdialog.js"));
        result.add(new TypeScriptDependency("primeng/contextmenu"                   ,"js/vendors/primeng/contextmenu.js"));
        result.add(new TypeScriptDependency("primeng/datagrid"                      ,"js/vendors/primeng/datagrid.js"));
        result.add(new TypeScriptDependency("primeng/datalist"                      ,"js/vendors/primeng/datalist.js"));
        result.add(new TypeScriptDependency("primeng/datascroller"                  ,"js/vendors/primeng/datascroller.js"));
        result.add(new TypeScriptDependency("primeng/dataview"                      ,"js/vendors/primeng/dataview.js"));
        result.add(new TypeScriptDependency("primeng/defer"                         ,"js/vendors/primeng/defer.js"));
        result.add(new TypeScriptDependency("primeng/dialog"                        ,"js/vendors/primeng/dialog.js"));
        result.add(new TypeScriptDependency("primeng/dragdrop"                      ,"js/vendors/primeng/dragdrop.js"));
        result.add(new TypeScriptDependency("primeng/dropdown"                      ,"js/vendors/primeng/dropdown.js"));
        result.add(new TypeScriptDependency("primeng/dropdown/dropdown"             ,"js/vendors/primeng/components/dropdown/dropdown.js"));
        result.add(new TypeScriptDependency("primeng/dynamicdialog"                 ,"js/vendors/primeng/dynamicdialog.js"));
        result.add(new TypeScriptDependency("primeng/editor"                        ,"js/vendors/primeng/editor.js"));
        result.add(new TypeScriptDependency("primeng/fieldset"                      ,"js/vendors/primeng/fieldset.js"));
        result.add(new TypeScriptDependency("primeng/fileupload"                    ,"js/vendors/primeng/fileupload.js"));
        result.add(new TypeScriptDependency("primeng/focustrap"                     ,"js/vendors/primeng/focustrap.js"));
        result.add(new TypeScriptDependency("primeng/fullcalendar"                  ,"js/vendors/primeng/fullcalendar.js"));
        result.add(new TypeScriptDependency("primeng/galleria"                      ,"js/vendors/primeng/galleria.js"));
        result.add(new TypeScriptDependency("primeng/gmap"                          ,"js/vendors/primeng/gmap.js"));
        result.add(new TypeScriptDependency("primeng/growl"                         ,"js/vendors/primeng/growl.js"));
        result.add(new TypeScriptDependency("primeng/inplace"                       ,"js/vendors/primeng/inplace.js"));
        result.add(new TypeScriptDependency("primeng/inputmask"                     ,"js/vendors/primeng/inputmask.js"));
        result.add(new TypeScriptDependency("primeng/inputswitch"                   ,"js/vendors/primeng/inputswitch.js"));
        result.add(new TypeScriptDependency("primeng/inputtext"                     ,"js/vendors/primeng/inputtext.js"));
        result.add(new TypeScriptDependency("primeng/inputtextarea"                 ,"js/vendors/primeng/inputtextarea.js"));
        result.add(new TypeScriptDependency("primeng/keyfilter"                     ,"js/vendors/primeng/keyfilter.js"));
        result.add(new TypeScriptDependency("primeng/lightbox"                      ,"js/vendors/primeng/lightbox.js"));
        result.add(new TypeScriptDependency("primeng/listbox"                       ,"js/vendors/primeng/listbox.js"));
        result.add(new TypeScriptDependency("primeng/megamenu"                      ,"js/vendors/primeng/megamenu.js"));
        result.add(new TypeScriptDependency("primeng/menu"                          ,"js/vendors/primeng/menu.js"));
        result.add(new TypeScriptDependency("primeng/menubar"                       ,"js/vendors/primeng/menubar.js"));
        result.add(new TypeScriptDependency("primeng/message"                       ,"js/vendors/primeng/message.js"));
        result.add(new TypeScriptDependency("primeng/messages"                      ,"js/vendors/primeng/messages.js"));
        result.add(new TypeScriptDependency("primeng/multiselect"                   ,"js/vendors/primeng/multiselect.js"));
        result.add(new TypeScriptDependency("primeng/orderlist"                     ,"js/vendors/primeng/orderlist.js"));
        result.add(new TypeScriptDependency("primeng/organizationchart"             ,"js/vendors/primeng/organizationchart.js"));
        result.add(new TypeScriptDependency("primeng/overlaypanel"                  ,"js/vendors/primeng/overlaypanel.js"));
        result.add(new TypeScriptDependency("primeng/paginator"                     ,"js/vendors/primeng/paginator.js"));
        result.add(new TypeScriptDependency("primeng/paginator/paginator"           ,"js/vendors/primeng/components/paginator/paginator.js"));
        result.add(new TypeScriptDependency("primeng/panel"                         ,"js/vendors/primeng/panel.js"));
        result.add(new TypeScriptDependency("primeng/panelmenu"                     ,"js/vendors/primeng/panelmenu.js"));
        result.add(new TypeScriptDependency("primeng/password"                      ,"js/vendors/primeng/password.js"));
        result.add(new TypeScriptDependency("primeng/picklist"                      ,"js/vendors/primeng/picklist.js"));
        result.add(new TypeScriptDependency("primeng/progressbar"                   ,"js/vendors/primeng/progressbar.js"));
        result.add(new TypeScriptDependency("primeng/progressspinner"               ,"js/vendors/primeng/progressspinner.js"));
        result.add(new TypeScriptDependency("primeng/radiobutton"                   ,"js/vendors/primeng/radiobutton.js"));
        result.add(new TypeScriptDependency("primeng/rating"                        ,"js/vendors/primeng/rating.js"));
        result.add(new TypeScriptDependency("primeng/schedule"                      ,"js/vendors/primeng/schedule.js"));
        result.add(new TypeScriptDependency("primeng/scrollpanel"                   ,"js/vendors/primeng/scrollpanel.js"));
        result.add(new TypeScriptDependency("primeng/selectbutton"                  ,"js/vendors/primeng/selectbutton.js"));
        result.add(new TypeScriptDependency("primeng/shared"                        ,"js/vendors/primeng/shared.js"));
        result.add(new TypeScriptDependency("primeng/sidebar"                       ,"js/vendors/primeng/sidebar.js"));
        result.add(new TypeScriptDependency("primeng/slidemenu"                     ,"js/vendors/primeng/slidemenu.js"));
        result.add(new TypeScriptDependency("primeng/slider"                        ,"js/vendors/primeng/slider.js"));
        result.add(new TypeScriptDependency("primeng/spinner"                       ,"js/vendors/primeng/spinner.js"));
        result.add(new TypeScriptDependency("primeng/splitbutton"                   ,"js/vendors/primeng/splitbutton.js"));
        result.add(new TypeScriptDependency("primeng/steps"                         ,"js/vendors/primeng/steps.js"));
        result.add(new TypeScriptDependency("primeng/table"                         ,"js/vendors/primeng/table.js"));
        result.add(new TypeScriptDependency("primeng/tabmenu"                       ,"js/vendors/primeng/tabmenu.js"));
        result.add(new TypeScriptDependency("primeng/tabview"                       ,"js/vendors/primeng/tabview.js"));
        result.add(new TypeScriptDependency("primeng/terminal"                      ,"js/vendors/primeng/terminal.js"));
        result.add(new TypeScriptDependency("primeng/tieredmenu"                    ,"js/vendors/primeng/tieredmenu.js"));
        result.add(new TypeScriptDependency("primeng/toast"                         ,"js/vendors/primeng/toast.js"));
        result.add(new TypeScriptDependency("primeng/togglebutton"                  ,"js/vendors/primeng/togglebutton.js"));
        result.add(new TypeScriptDependency("primeng/toolbar"                       ,"js/vendors/primeng/toolbar.js"));
        result.add(new TypeScriptDependency("primeng/tooltip"                       ,"js/vendors/primeng/tooltip.js"));
        result.add(new TypeScriptDependency("primeng/tree"                          ,"js/vendors/primeng/tree.js"));
        result.add(new TypeScriptDependency("primeng/treetable"                     ,"js/vendors/primeng/treetable.js"));
        result.add(new TypeScriptDependency("primeng/tristatecheckbox"              ,"js/vendors/primeng/tristatecheckbox.js"));
        result.add(new TypeScriptDependency("primeng/virtualscroller"               ,"js/vendors/primeng/virtualscroller.js"));
        result.add(new TypeScriptDependency("primeng/dom/domhandler"                ,"js/vendors/primeng/components/dom/domhandler.js"));
        result.add(new TypeScriptDependency("primeng/common/shared"                 ,"js/vendors/primeng/components/common/shared.js"));
        result.add(new TypeScriptDependency("primeng/common/messageservice"         ,"js/vendors/primeng/components/common/messageservice.js"));
        result.add(new TypeScriptDependency("primeng/utils/objectutils"             ,"js/vendors/primeng/components/utils/objectutils.js"));
        result.add(new TypeScriptDependency("d3"                                    ,"js/vendors/d3/d3.min.js"));
        result.add(new TypeScriptDependency("chart.js"                              ,"js/vendors/chart.js/chart.min.js"));
        result.add(new TypeScriptDependency("quill"                                 ,"js/vendors/quill/quill.min.js"));
        result.add(new TypeScriptDependency("moment"                                ,"js/vendors/moment/moment-with-locales.js"));
        result.add(new TypeScriptDependency("@fullcalendar/core"                    ,"js/vendors/@fullcalendar/core/main.min.js"));
        //@formatter:on
        return result;
    }
    
}
