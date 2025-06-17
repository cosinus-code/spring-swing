/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.swing.test.boot.app;

import org.cosinus.swing.boot.SpringSwingApplication;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

/**
 * Test {@link SpringSwingApplication}
 */
@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = TestSpringSwingApplication.class)
public class SpringSwingApplicationTest {

    @Autowired
    private TestSpringComponent testSpringComponent;

    @Test
    public void testSpringSwingRunningApplication() {
        assertNotNull(testSpringComponent);
    }
}
