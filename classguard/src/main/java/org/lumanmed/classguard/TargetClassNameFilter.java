/**
 * 
 */
package org.lumanmed.classguard;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Willard Wang
 *
 */
public class TargetClassNameFilter implements FilenameFilter {
    protected List<String> names = new ArrayList<String>();

    public TargetClassNameFilter(String patterns) {
        String[] tokens = patterns.split(",");
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            String[] paths = token.split("[./]");
            StringBuilder builder = new StringBuilder();
            for (String path : paths) {
                builder.append(File.separator);
                builder.append(path);
            }
            builder.deleteCharAt(0);
            names.add(builder.toString());
        }
    }

    /* (non-Javadoc)
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    public boolean accept(File dir, String name) {
        String path = dir.getAbsolutePath();
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        path = path + name;
        
        for (String n : names) {
            if (path.contains(n)) {
                return true;
            }
        }
        return false;
    }

}
