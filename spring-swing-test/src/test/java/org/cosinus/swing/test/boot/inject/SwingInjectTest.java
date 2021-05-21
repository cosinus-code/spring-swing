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

package org.cosinus.swing.test.boot.inject;

import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.cosinus.swing.test.model.*;
import org.cosinus.swing.window.Frame;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = TestSwingInjectApplication.class)
public class SwingInjectTest {

    @Test
    public void testSwingInject() {
        TestSwingInjectObject swingInject = new TestSwingInjectObject();

        assertNotNull(swingInject.getApplicationStorage());
    }

    @Test
    public void testSwingInjectIntoFrame() {
        TestFrame frame = new TestFrame();

        assertNotNull(frame.getActionController());
        assertNotNull(frame.getTranslator());
        assertNotNull(frame.getErrorHandler());
        assertNotNull(frame.getResourceResolver());
        assertNotNull(frame.getWindowSettingsHandler());
        assertNotNull(frame.getMenuProvider());
        assertNotNull(frame.getUIHandler());
    }

    @Test
    public void testSwingInjectIntoDialog() {
        Frame frame = new TestFrame();
        TestDialog dialog = new TestDialog(frame);

        assertNotNull(dialog.errorHandler);
    }

    @Test
    public void testSwingInjectIntoPanel() {
        TestPanel panel = new TestPanel();

        assertNotNull(panel.getApplicationStorage());
    }

    @Test
    public void testSwingInjectIntoTable() {
        TestTable table = new TestTable();

        assertNotNull(table.applicationStorage);
    }

    @Test
    public void testSwingInjectIntoTableModel() {
        TestTableModel tableModel = new TestTableModel();

        assertNotNull(tableModel.applicationStorage);
    }

    @Test
    public void testSwingInjectIntoSplit() {
        TestSplit split = new TestSplit("testSplit", 100);

        assertNotNull(split.getApplicationStorage());
        assertNotNull(split.getTranslator());
    }

    @Test
    public void testSwingInjectIntoComponent() {
        TestComponent component = new TestComponent();

        assertNotNull(component.getApplicationStorage());
    }

    @Test
    public void testSwingInjectIntoSwingWorker() {
        TestSwingWorker component = new TestSwingWorker();

        assertNotNull(component.errorHandler);
    }
}
