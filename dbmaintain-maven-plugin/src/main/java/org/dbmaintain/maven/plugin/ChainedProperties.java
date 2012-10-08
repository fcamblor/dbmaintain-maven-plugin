package org.dbmaintain.maven.plugin;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fcamblor
 */
public class ChainedProperties {

    private static final Pattern ENV_VAR_EXTRACTION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private List<String> paths;

    public ChainedProperties() {
        this.paths = new ArrayList<String>();
    }

    public ChainedProperties chainProperties(String path) {
        this.paths.add(path);
        return this;
    }

    protected static String extrapolatePath(String path) {
        StringBuffer extrapolatedPath = new StringBuffer();
        Matcher extrapolatedPathMatcher = ENV_VAR_EXTRACTION_PATTERN.matcher(path);
        while (extrapolatedPathMatcher.find()) {
            String envVarName = extrapolatedPathMatcher.group(1);
            String envVarValue = System.getProperty(envVarName);
            if (envVarValue == null) {
                extrapolatedPathMatcher.appendReplacement(extrapolatedPath, "\\${" + envVarName + "}");
            } else {
                extrapolatedPathMatcher.appendReplacement(extrapolatedPath, envVarValue);
            }
        }
        extrapolatedPathMatcher.appendTail(extrapolatedPath);

        return extrapolatedPath.toString();
    }

    public Properties load() {
        Properties result = new Properties();

        for (String path : paths) {
            String extrapolatedPath = extrapolatePath(path);
            FileInputStream pathStream = null;
            try {
                pathStream = new FileInputStream(new File(extrapolatedPath));
                result.load(pathStream);
            } catch (IOException e) {
                // No problem... path _can_ not be found...
                System.out.println("Path not found : " + extrapolatedPath);
            } finally {
                if (pathStream != null) {
                    try {
                        pathStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return result;
    }
}
