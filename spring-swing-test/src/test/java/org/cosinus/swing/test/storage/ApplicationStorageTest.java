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

package org.cosinus.swing.test.storage;

import org.cosinus.swing.store.ApplicationStorage;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.cosinus.swing.test.boot.app.TestSpringSwingApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = TestSpringSwingApplication.class)
public class ApplicationStorageTest {

    @Autowired
    private ApplicationStorage applicationStorage;

    @Test
    public void testSaveString() {
        applicationStorage.saveString("rover-name", "Perseverance");
        String roverName = applicationStorage.getString("rover-name");

        assertEquals("Perseverance", roverName);
    }

    @Test
    public void testSaveInt() {
        applicationStorage.saveInt("rover-minutes-of-terror", 7);
        int minutes = applicationStorage.getInt("rover-minutes-of-terror", 0);

        assertEquals(7, minutes);
    }

    @Test
    public void testSaveBoolean() {
        applicationStorage.saveBoolean("rover-touchdown", true);
        boolean touchdown = applicationStorage.getBoolean("rover-touchdown", false);

        assertTrue(touchdown);
    }

    @Test
    public void testSaveObject() {
        applicationStorage.save("rover", new Object() {
            @Override
            public String toString() {
                return "Rover Perseverance";
            }
        });
        String rover = applicationStorage.getString("rover");

        assertEquals("Rover Perseverance", rover);
    }

    @Test
    public void testRemove() {
        applicationStorage.saveString("rover-name", "Perseverance");
        applicationStorage.remove("rover-name");
        String roverName = applicationStorage.getString("rover-name");

        assertNull(roverName);
    }
}
