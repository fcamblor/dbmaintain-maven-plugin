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
package org.dbmaintain.script.repository.impl;

import org.dbmaintain.script.Script;
import org.dbmaintain.util.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.dbmaintain.util.CollectionUtils.asSet;
import static org.dbmaintain.util.TestUtils.createScript;
import static org.junit.Assert.assertEquals;

/**
 * @author Filip Neven
 * @author Tim Ducheyne
 * @since 28-dec-2008
 */
public class FileSystemScriptLocationTest {

    private FileSystemScriptLocation fileSystemScriptLocation;
    private File scriptRootLocation;
    private Script indexed1, repeatable1, postProcessing1;

    @Before
    public void init() throws Exception {
        indexed1 = createScript("01_indexed1.sql");
        repeatable1 = createScript("repeatable1.sql");
        postProcessing1 = createScript("postprocessing/01_post1.sql");

        scriptRootLocation = new File(getClass().getResource("testscripts").toURI());
        fileSystemScriptLocation = TestUtils.createFileSystemLocation(scriptRootLocation);
    }

    @Test
    public void testGetAllFiles() {
        assertEquals(asSet(indexed1, repeatable1, postProcessing1), fileSystemScriptLocation.getScripts());
    }
}
