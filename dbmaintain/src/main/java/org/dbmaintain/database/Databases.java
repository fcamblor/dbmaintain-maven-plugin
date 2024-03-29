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
package org.dbmaintain.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tim Ducheyne
 * @author Filip Neven
 */
public class Databases {

    private Database defaultDatabase;
    private List<String> disabledDatabaseNames;

    private List<Database> databases;
    private Map<String, Database> nameDatabaseMap = new HashMap<String, Database>();


    public Databases(Database defaultDatabase, List<Database> databases, List<String> disabledDatabaseNames) {
        if (databases.isEmpty()) {
            throw new DatabaseException("Unable to configure databases. No database instances provided.");
        }
        this.databases = databases;
        this.defaultDatabase = defaultDatabase;

        for (Database database : databases) {
            this.nameDatabaseMap.put(database.getDatabaseName(), database);
        }
        this.disabledDatabaseNames = disabledDatabaseNames;
    }


    public Database getDefaultDatabase() {
        return defaultDatabase;
    }

    /**
     * Returns the database with the given name.
     * Null is returned if the database is disabled. An exception is raised if the database is unknown.
     *
     * @param databaseName The name
     * @return The database, null if disabled
     */
    public Database getDatabase(String databaseName) {
        if (isDisabledDatabase(databaseName)) {
            return null;
        }
        Database database = nameDatabaseMap.get(databaseName);
        if (database == null) {
            throw new DatabaseException("No database configured with name: " + databaseName);
        }
        return database;
    }

    public List<Database> getDatabases() {
        return databases;
    }


    public boolean isConfiguredDatabase(String databaseName) {
        return nameDatabaseMap.containsKey(databaseName) || isDisabledDatabase(databaseName);
    }

    public boolean isDisabledDatabase(String databaseName) {
        return disabledDatabaseNames.contains(databaseName);
    }
}
