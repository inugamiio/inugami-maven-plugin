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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.inugami.maven.plugin.tools.FilesUtils;

/**
 * TemplateRendering
 * 
 * @author patrick_guillerm
 * @since 23 juin 2017
 */
public class TemplateRendering {

    // =========================================================================
    // render
    // =========================================================================
    public String render(File template, Map<String, String> properties) throws IOException {
        FilesUtils.assertIsFile(template);
        FilesUtils.assertCanRead(template);

        String content = "";
        byte[] data = FilesUtils.readBytes(template);

        if (data == null) {
            content = "";
        } else {
            content = new String(data);
        }

        if (!properties.isEmpty()) {
            for (String key : properties.keySet()) {
                content = applyProperty(content, key, properties.get(key));
            }
        }
        return content;
    }

    // =========================================================================
    // applyProperty
    // =========================================================================
    public String applyProperty(String content, String key, String value) {
        String result = content;
        //@formatter:off
        final String regex = new StringBuilder()
                                    .append("[$][{]")
                                    .append(key)
                                    .append("[}]")
                                    .toString();
        //@formatter:on
        final Pattern pattern = Pattern.compile(regex);

        String modify = content;
        Matcher matcher = pattern.matcher(modify);
        while (matcher.find()) {
            final int begin = matcher.start();
            final int end = matcher.end();

            final StringBuilder buffer = new StringBuilder();
            buffer.append(modify.substring(0, begin));
            buffer.append(value);
            buffer.append(modify.substring(end, modify.length()));

            modify = buffer.toString();
            matcher = pattern.matcher(modify);
        }

        if (modify.length() != result.length()) {
            result = modify;
        }

        return result;
    }

}
