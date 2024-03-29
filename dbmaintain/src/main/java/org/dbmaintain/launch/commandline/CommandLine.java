/*
 * Copyright DbMaintain.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dbmaintain.launch.commandline;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbmaintain.MainFactory;
import org.dbmaintain.config.DbMaintainConfigurationLoader;
import org.dbmaintain.config.DbMaintainProperties;
import org.dbmaintain.util.DbMaintainException;
import org.dbmaintain.util.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.Properties;

/**
 * Class that exposes a set of DbMaintain operations for command line execution.
 *
 * @author Filip Neven
 * @author Tim Ducheyne
 */
public class CommandLine {

    public static final String DBMAINTAIN_PROPERTIES = "dbmaintain.properties";

    /* The logger instance for this class */
    private static Log logger = LogFactory.getLog(CommandLine.class);

    /**
     * Enum that defines all DbMaintain operations that can be invoked using this class.
     */
    public enum DbMaintainOperation {

        CREATE_SCRIPT_ARCHIVE("createScriptArchive"),
        CHECK_SCRIPT_UPDATES("checkScriptUpdates"),
        UPDATE_DATABASE("updateDatabase"),
        MARK_ERROR_SCRIPT_PERFORMED("markErrorScriptPerformed"),
        MARK_ERROR_SCRIPT_REVERTED("markErrorScriptReverted"),
        MARK_DATABASE_AS_UPTODATE("markDatabaseAsUpToDate"),
        CLEAR_DATABASE("clearDatabase"),
        CLEAN_DATABASE("cleanDatabase"),
        DISABLE_CONSTRAINTS("disableConstraints"),
        UPDATE_SEQUENCES("updateSequences");

        private String operationName;

        private DbMaintainOperation(String operationName) {
            this.operationName = operationName;
        }

        /**
         * @return The name of the operation, that can be used as first command line argument to invoke an operation
         */
        public String getOperationName() {
            return operationName;
        }

        /**
         * @param operationName The name of the operation, that can be used as first command line argument to invoke an operation
         * @return The operation identified by the given operation name
         */
        public static DbMaintainOperation getByOperationName(String operationName) {
            for (DbMaintainOperation operation : values()) {
                if (operation.getOperationName().equalsIgnoreCase(operationName)) {
                    return operation;
                }
            }
            return null;
        }
    }


    /**
     * Executes a DbMaintain operation. The first command-line argument defines the operation that
     * must be executed. The second argument defines the properties file that is used to configure
     * DbMaintain.
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        CommandLineArguments commandLineArguments = parseCommandLineArguments(args);
        Properties configuration = loadConfiguration(commandLineArguments);
        DbMaintainOperation operation = getDbMaintainOperation(commandLineArguments);

        executeOperation(operation, configuration, commandLineArguments);
    }

    /**
     * Parses the command line arguments and gives back a {@link CommandLineArguments} instance that represents
     * these arguments in a more convenient way.
     *
     * @param args The command line arguments as an array of strings
     * @return An instance of {@link CommandLineArguments}
     */
    protected static CommandLineArguments parseCommandLineArguments(String[] args) {
        CommandLineArguments commandLineArguments = null;
        try {
            commandLineArguments = new CommandLineArguments(args);
        } catch (DbMaintainException e) {
            System.err.println("\n" + e.getMessage());
            printHelpMessage();
            System.exit(1);
        }
        return commandLineArguments;
    }

    /**
     * Gets the requested DbMaintain operation. If the requested operation cannot be recognized, an error message
     * is printed, a help message is shown and execution is ended.
     *
     * @param commandLineArguments The command line arguments
     * @return The requested DbMaintain operation
     */
    protected static DbMaintainOperation getDbMaintainOperation(CommandLineArguments commandLineArguments) {
        String dbMaintainOperation = commandLineArguments.getDbMaintainOperation();
        DbMaintainOperation operation = DbMaintainOperation.getByOperationName(dbMaintainOperation);
        if (operation == null) {
            System.err.println("Invalid operation " + dbMaintainOperation);
            printHelpMessage();
            System.exit(1);
        }
        return operation;
    }

    /**
     * Loads the configuration from custom config file or, if no custom config file was configured, from
     * {@link #DBMAINTAIN_PROPERTIES}, if this file exists. If a custom config file was configured and the config file
     * cannot be found, an error message is printed and execution is ended.
     *
     * @param commandLineArguments The command line arguments
     * @return The configuration as a <code>Properties</code> file
     */
    protected static Properties loadConfiguration(CommandLineArguments commandLineArguments) {
        URL propertiesAsURL = null;
        String customConfigFile = commandLineArguments.getConfigFile();
        if (customConfigFile == null) {
            if (new File(DBMAINTAIN_PROPERTIES).exists()) {
                propertiesAsURL = getPropertiesAsURL(DBMAINTAIN_PROPERTIES);
                logger.info("Loaded configuration from file " + DBMAINTAIN_PROPERTIES);
            }
        } else {
            propertiesAsURL = getPropertiesAsURL(customConfigFile);
            logger.info("Loaded configuration from file " + customConfigFile);
        }
        return new DbMaintainConfigurationLoader().loadConfiguration(propertiesAsURL);
    }

    /**
     * Executes the given operation using the given configuration.
     *
     * @param operation            The operation that must be executed
     * @param configuration        The dbMaintain configuration
     * @param commandLineArguments The command line arguments
     */
    public static void executeOperation(DbMaintainOperation operation, Properties configuration, CommandLineArguments commandLineArguments) {
        switch (operation) {
            case CREATE_SCRIPT_ARCHIVE:
                if (commandLineArguments.getFirstExtraArgument() == null) {
                    System.err.println("Archive file name must be specified as extra argument");
                    System.exit(1);
                }
                if (commandLineArguments.getSecondExtraArgument() != null) {
                    configuration.put(DbMaintainProperties.PROPERTY_SCRIPT_LOCATIONS, commandLineArguments.getSecondExtraArgument());
                }
                String jarFileName = commandLineArguments.getFirstExtraArgument();
                getMainFactory(configuration).createScriptArchiveCreator().createScriptArchive(jarFileName);
                break;
            case CHECK_SCRIPT_UPDATES:
                if (commandLineArguments.getFirstExtraArgument() != null) {
                    configuration.put(DbMaintainProperties.PROPERTY_SCRIPT_LOCATIONS, commandLineArguments.getFirstExtraArgument());
                }
                getMainFactory(configuration).createDbMaintainer().updateDatabase(true);
                break;
            case UPDATE_DATABASE:
                if (commandLineArguments.getFirstExtraArgument() != null) {
                    configuration.put(DbMaintainProperties.PROPERTY_SCRIPT_LOCATIONS, commandLineArguments.getFirstExtraArgument());
                }
                getMainFactory(configuration).createDbMaintainer().updateDatabase(false);
                break;
            case MARK_DATABASE_AS_UPTODATE:
                if (commandLineArguments.getFirstExtraArgument() != null) {
                    configuration.put(DbMaintainProperties.PROPERTY_SCRIPT_LOCATIONS, commandLineArguments.getFirstExtraArgument());
                }
                getMainFactory(configuration).createDbMaintainer().markDatabaseAsUpToDate();
                break;
            case MARK_ERROR_SCRIPT_PERFORMED:
                getMainFactory(configuration).createExecutedScriptInfoSource().markErrorScriptsAsSuccessful();
                break;
            case MARK_ERROR_SCRIPT_REVERTED:
                getMainFactory(configuration).createExecutedScriptInfoSource().removeErrorScripts();
                break;
            case CLEAR_DATABASE:
                getMainFactory(configuration).createDBClearer().clearDatabase();
                break;
            case CLEAN_DATABASE:
                getMainFactory(configuration).createDBCleaner().cleanDatabase();
                break;
            case DISABLE_CONSTRAINTS:
                getMainFactory(configuration).createConstraintsDisabler().disableConstraints();
                break;
            case UPDATE_SEQUENCES:
                getMainFactory(configuration).createSequenceUpdater().updateSequences();
                break;
        }
    }

    protected static MainFactory getMainFactory(Properties configuration) {
        return new MainFactory(configuration);
    }

    /**
     * @param fileName The name of the file
     * @return An inputStream giving access to the file with the given name.
     */
    protected static URL getPropertiesAsURL(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.err.println("Could not find config file" + DBMAINTAIN_PROPERTIES);
            System.exit(1);
        }
        return FileUtils.getUrl(file);
    }


    /**
     * Prints out a help message that explains the usage of this class
     */
    public static void printHelpMessage() {
        System.out.println();
        System.out.println("Usage:");
        System.out.println();
        System.out.println("java org.dbmaintain.launch.DbMaintain <operation> [extra operation arguments] [-config propertiesFile]");
        System.out.println();
        System.out.println("The -config argument is optional. If omitted, the file " + DBMAINTAIN_PROPERTIES + " is expected to be available in the execution directory.");
        System.out.println("The archive file/script folder argument is also optional, and only applicable to the operations " +
                DbMaintainOperation.CREATE_SCRIPT_ARCHIVE.getOperationName() + ", " + DbMaintainOperation.UPDATE_DATABASE.getOperationName() + " and " + DbMaintainOperation.MARK_DATABASE_AS_UPTODATE.getOperationName());
        System.out.println();
        System.out.println("Available operations are:");
        System.out.println();
        System.out.println("- " + DbMaintainOperation.CREATE_SCRIPT_ARCHIVE.getOperationName());
        System.out.println("     Creates an archive file containing all scripts in all configured script locations.");
        System.out.println("     Expects a second argument indicating the file name.");
        System.out.println("     Optionally, a third argument may be added indicating the scripts archive file or root folder.");
        System.out.println("     This argument overrides the value of the property " + DbMaintainProperties.PROPERTY_SCRIPT_LOCATIONS + ".");
        System.out.println();
        System.out.println("- " + DbMaintainOperation.UPDATE_DATABASE.getOperationName());
        System.out.println("     Updates the database to the latest version.");
        System.out.println("     Optionally, an extra argument may be added indicating the scripts archive file or root folder.");
        System.out.println("     This argument overrides the value of the property " + DbMaintainProperties.PROPERTY_SCRIPT_LOCATIONS + ".");
        System.out.println();
        System.out.println("- " + DbMaintainOperation.MARK_ERROR_SCRIPT_PERFORMED.getOperationName());
        System.out.println("     Task that indicates that the failed script was manually performed.");
        System.out.println("     The script will NOT be run again in the next update.");
        System.out.println("     No scripts will be executed by this task.");
        System.out.println();
        System.out.println("- " + DbMaintainOperation.MARK_ERROR_SCRIPT_REVERTED.getOperationName());
        System.out.println("     Task that indicates that the failed script was manually reverted.");
        System.out.println("     The script will be run again in the next update.");
        System.out.println("     No scripts will be executed by this task.");
        System.out.println();
        System.out.println("- " + DbMaintainOperation.MARK_DATABASE_AS_UPTODATE.getOperationName());
        System.out.println("     Marks the database as up-to-date, without executing any script.");
        System.out.println("     You can use this operation to prepare an existing database to be managed by DbMaintain, ");
        System.out.println("     or after fixing a problem manually.");
        System.out.println("     Optionally, an extra argument may be added indicating the scripts archive file or root folder.");
        System.out.println("     This argument overrides the value of the property " + DbMaintainProperties.PROPERTY_SCRIPT_LOCATIONS + ".");
        System.out.println();
        System.out.println("- " + DbMaintainOperation.CHECK_SCRIPT_UPDATES.getOperationName());
        System.out.println("     Checks if there are any script updates and prints them out, without executing any script.");
        System.out.println();
        System.out.println("- " + DbMaintainOperation.CLEAR_DATABASE.getOperationName());
        System.out.println("     Removes all database items, and empties the DBMAINTAIN_SCRIPTS table.");
        System.out.println();
        System.out.println("- " + DbMaintainOperation.CLEAN_DATABASE.getOperationName());
        System.out.println("     Removes the data of all database tables, except for the DBMAINTAIN_SCRIPTS table.");
        System.out.println();
        System.out.println("- " + DbMaintainOperation.DISABLE_CONSTRAINTS.getOperationName());
        System.out.println("     Disables or drops all foreign key and not null constraints.");
        System.out.println();
        System.out.println("- " + DbMaintainOperation.UPDATE_SEQUENCES.getOperationName());
        System.out.println("     Updates all sequences and identity columns to a minimal value.");
    }

}
