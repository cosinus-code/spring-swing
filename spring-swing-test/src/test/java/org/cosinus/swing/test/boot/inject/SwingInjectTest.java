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

import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.form.Frame;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.cosinus.swing.test.boot.app.TestSpringSwingApplication;
import org.cosinus.swing.test.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = TestSwingInjectApplication.class)
public class SwingInjectTest {

    @Autowired
    private SwingApplicationContext swingContext;

    @Test
    public void testSwingInject() {
        TestSwingInjectObject swingInject = new TestSwingInjectObject(swingContext);

        assertNotNull(swingInject.getApplicationStorage());
        assertNotNull(swingInject.getSpringSwingComponent());
    }

    @Test
    public void testSwingInjectIntoFrame() {
        TestFrame frame = new TestFrame();

        assertNotNull(frame.getSpringSwingComponent());
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
        assertNotNull(dialog.springSwingComponent);
    }

    @Test
    public void testSwingInjectIntoPanel() {
        TestPanel panel = new TestPanel();

        assertNotNull(panel.getApplicationStorage());
        assertNotNull(panel.getSpringSwingComponent());
    }

    @Test
    public void testSwingInjectIntoTable() {
        TestTable table = new TestTable();

        assertNotNull(table.applicationStorage);
        assertNotNull(table.springSwingComponent);
    }

    @Test
    public void testSwingInjectIntoTableModel() {
        TestTableModel tableModel = new TestTableModel();

        assertNotNull(tableModel.applicationStorage);
        assertNotNull(tableModel.springSwingComponent);
    }

    @Test
    public void testSwingInjectIntoSplit() {
        TestSplit split = new TestSplit("testSplit", 100);

        assertNotNull(split.getApplicationStorage());
        assertNotNull(split.getTranslator());
        assertNotNull(split.getUIHandler());
    }

    @Test
    public void testSwingInjectIntoComponent() {
        TestComponent component = new TestComponent();

        assertNotNull(component.getApplicationStorage());
        assertNotNull(component.getSpringSwingComponent());
    }

    @Test
    public void testSwingInjectIntoSwingWorker() {
        TestSwingWorker component = new TestSwingWorker();

        assertNotNull(component.errorHandler);
        assertNotNull(component.springSwingComponent);
    }
}
