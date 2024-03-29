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
package org.dbmaintain.structure.constraint.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbmaintain.database.Database;
import org.dbmaintain.database.Databases;
import org.dbmaintain.structure.constraint.ConstraintsDisabler;

/**
 * Default implementation of {@link ConstraintsDisabler}.
 * This will disable all foreign key, check and not-null constraints on the configured database schemas.
 * Primary key constraints will not be disabled.
 *
 * @author Tim Ducheyne
 * @author Filip Neven
 * @author Bart Vermeiren
 */
public class DefaultConstraintsDisabler implements ConstraintsDisabler {

    /* The logger instance for this class */
    private static Log logger = LogFactory.getLog(DefaultConstraintsDisabler.class);

    /* The database supports to disable the constraints for */
    protected Databases databases;

    /**
     * Creates the constraints disabler.
     *
     * @param databases The database supports to disable the constraints for, not null
     */
    public DefaultConstraintsDisabler(Databases databases) {
        this.databases = databases;
    }


    /**
     * Disable every foreign key or not-null constraint
     */
    public void disableConstraints() {
        // first disable referential constraints to avoid conflicts
        disableReferentialConstraints();
        // disable not-null and check constraints
        disableValueConstraints();
    }

    @Override
    public void disableReferentialConstraints() {
         for (Database database : databases.getDatabases()) {
            for (String schemaName : database.getSchemaNames()) {
                logger.info("Disabling referential constraints in database schema " + schemaName);
                database.disableReferentialConstraints(schemaName);
            }
        }
    }

    @Override
    public void disableValueConstraints() {
        for (Database database : databases.getDatabases()) {
            for (String schemaName : database.getSchemaNames()) {
                logger.info("Disabling value constraints in database schema " + schemaName);
                database.disableValueConstraints(schemaName);
            }
        }
    }


}