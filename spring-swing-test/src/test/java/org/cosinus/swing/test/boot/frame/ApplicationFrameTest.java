/*
 * Copyright 2020 Cosinus Software
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

package org.cosinus.swing.test.boot.frame;

import org.cosinus.swing.store.ApplicationStorage;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.cosinus.swing.window.WindowSettings.DEFAULT_HEIGHT;
import static org.cosinus.swing.window.WindowSettings.DEFAULT_WIDTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = TestSpringSwingFrameApplication.class, useMainMethod = SpringBootTest.UseMainMethod.WHEN_AVAILABLE)
public class ApplicationFrameTest {

    @Autowired
    private TestSwingApplicationFrame applicationFrame;

    @Autowired
    private ApplicationStorage applicationStorage;

    @Test
    public void testApplicationFrame() {
        assertNotNull(applicationFrame);
        assertNotNull(applicationStorage);
        assertEquals(DEFAULT_WIDTH, applicationFrame.getWidth());
        assertEquals(DEFAULT_HEIGHT, applicationFrame.getHeight());
    }
}
