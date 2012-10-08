package org.dbmaintain.maven.plugin;

import org.dbmaintain.launch.task.DbMaintainDatabase;

import java.util.List;
import java.util.Properties;

/**
 * @author fcamblor
 */
public class PropertyDatabase {

    /**
     * @parameter
     * @required
     */
    private List<String> propertyPaths;

    /**
     * @parameter
     */
    private String name;

    /**
     * @parameter
     */
    private String includedKey;
    /**
     * @parameter
     */
    private String dialectKey;
    /**
     * @parameter
     * @required
     */
    private String driverClassNameKey;
    /**
     * @parameter
     * @required
     */
    private String urlKey;
    /**
     * @parameter
     * @required
     */
    private String userNameKey;
    /**
     * @parameter
     * @required
     */
    private String passwordKey;
    /**
     * @parameter
     * @required
     */
    private String schemaNamesKey;

    public PropertyDatabase() {
    }

    public List<String> getPropertyPaths() {
        return propertyPaths;
    }

    public void setPropertyPaths(List<String> propertyPaths) {
        this.propertyPaths = propertyPaths;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIncludedKey() {
        return includedKey;
    }

    public void setIncludedKey(String includedKey) {
        this.includedKey = includedKey;
    }

    public String getDialectKey() {
        return dialectKey;
    }

    public void setDialectKey(String dialectKey) {
        this.dialectKey = dialectKey;
    }

    public String getDriverClassNameKey() {
        return driverClassNameKey;
    }

    public void setDriverClassNameKey(String driverClassNameKey) {
        this.driverClassNameKey = driverClassNameKey;
    }

    public String getUrlKey() {
        return urlKey;
    }

    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
    }

    public String getUserNameKey() {
        return userNameKey;
    }

    public void setUserNameKey(String userNameKey) {
        this.userNameKey = userNameKey;
    }

    public String getPasswordKey() {
        return passwordKey;
    }

    public void setPasswordKey(String passwordKey) {
        this.passwordKey = passwordKey;
    }

    public String getSchemaNamesKey() {
        return schemaNamesKey;
    }

    public void setSchemaNamesKey(String schemaNamesKey) {
        this.schemaNamesKey = schemaNamesKey;
    }

    public DbMaintainDatabase toDbMaintainDatabase() {
        ChainedProperties chainedProperties = new ChainedProperties();
        for (String propertyPath : propertyPaths) {
            chainedProperties.chainProperties(propertyPath);
        }

        Properties props = chainedProperties.load();

        DbMaintainDatabase dbMaintainDatabase = new DbMaintainDatabase();
        dbMaintainDatabase.setName(this.getName());
        dbMaintainDatabase.setIncluded(Boolean.valueOf(getStringValue(props, this.getIncludedKey(), Boolean.TRUE.toString())));
        dbMaintainDatabase.setDialect(getStringValue(props, this.getDialectKey(), null));
        dbMaintainDatabase.setDriverClassName(getStringValue(props, this.getDriverClassNameKey()));
        dbMaintainDatabase.setUrl(getStringValue(props, this.getUrlKey()));
        dbMaintainDatabase.setUserName(getStringValue(props, this.getUserNameKey()));
        dbMaintainDatabase.setPassword(getStringValue(props, this.getPasswordKey()));
        dbMaintainDatabase.setSchemaNames(getStringValue(props, this.getSchemaNamesKey()));
        return dbMaintainDatabase;
    }

    protected String getStringValue(Properties props, String key) {
        return getStringValue(props, key, null);
    }

    protected String getStringValue(Properties props, String key, String defaultValue) {
        if (key == null) {
            return defaultValue;
        }
        return props.getProperty(key, defaultValue);
    }
}
