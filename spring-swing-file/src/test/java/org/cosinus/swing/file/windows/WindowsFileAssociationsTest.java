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
package org.cosinus.swing.file.windows;

import org.cosinus.swing.file.api.Application;
import org.cosinus.swing.file.api.FileCompatibleApplications;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cosinus.swing.file.windows.InMemoryRegistry.path;
import static org.cosinus.swing.file.windows.WindowsFileAssociations.SHELL_OPEN_COMMAND;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_FILE_EXTS;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_OPEN_WITH_LIST;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_OPEN_WITH_PROGIDS;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_USER_CHOICE;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_USER_CLASSES;
import static org.cosinus.swing.window.registry.RegistryRoot.CLASSES_ROOT;
import static org.cosinus.swing.window.registry.RegistryRoot.CURRENT_USER;
import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.ASSOCF_INIT_IGNOREUNKNOWN;
import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.ASSOCF_NONE;
import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.ASSOCF_OPEN_BYEXENAME;
import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.ASSOCSTR_FRIENDLYAPPNAME;
import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.ASSOCSTR_PROGID;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Finding the applications of a file on Windows and setting the default one,
 * with the associations and the registry in memory.
 */
@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = TestWindowsFileApplication.class)
public class WindowsFileAssociationsTest {

    private static final String NOTEPAD = "C:\\Windows\\system32\\notepad.exe";
    private static final String EDITOR = "C:\\Program Files\\Editor\\editor.exe";

    @Autowired
    private WindowsFileAssociations fileAssociations;

    @Autowired
    private InMemoryFileAssociationQuery query;

    @Autowired
    private InMemoryRegistry registry;

    @Autowired
    private AssociationsChangedCounter changedCounter;

    @Before
    public void setUp() {
        query.reset();
        registry.reset();
        changedCounter.reset();
    }

    @Test
    public void testToExecutable() {
        assertEquals("\"C:\\App\\app.exe\" %f", fileAssociations.toExecutable("\"C:\\App\\app.exe\" \"%1\""));
        assertEquals("C:\\Windows\\notepad.exe %f", fileAssociations.toExecutable("%SystemRoot%\\notepad.exe %1"));
        assertEquals("app.exe /open %f", fileAssociations.toExecutable("app.exe /open \"%L\" %*"));
        assertEquals("app.exe %f", fileAssociations.toExecutable("app.exe"));
        assertEquals("app.exe -file %f", fileAssociations.toExecutable("app.exe -file \"%V\" \"%2\""));
    }

    @Test
    public void testFileWithoutExtension() {
        assertTrue(fileAssociations.findCompatibleApplications(new File("README")).isEmpty());
    }

    @Test
    public void testUnknownExtension() {
        FileCompatibleApplications applications = fileAssociations.findCompatibleApplications(new File("file.unknown"));

        assertTrue(applications.isEmpty());
        assertNull(applications.getDefaultApplication());
    }

    @Test
    public void testDefaultApplication() {
        query.putAssociation(ASSOCF_INIT_IGNOREUNKNOWN, ".txt", "txtfile", NOTEPAD + " %1", NOTEPAD, "Notepad");

        FileCompatibleApplications applications =
            fileAssociations.findCompatibleApplications(new File("C:\\docs\\a.TXT"));

        assertEquals(List.of("txtfile"), List.copyOf(applications.keySet()));
        Application notepad = applications.get("txtfile");
        assertSame(notepad, applications.getDefaultApplication());
        assertEquals("Notepad", notepad.getName());
        assertEquals(NOTEPAD, notepad.getIconName());
        // commons-exec uses the file separator of the current platform for the executable
        assertArrayEquals(new String[]{NOTEPAD.replace('\\', File.separatorChar),
                new File("C:\\docs\\a.TXT").getAbsolutePath()},
            notepad.getCommandToExecuteFile(new File("C:\\docs\\a.TXT")));
    }

    @Test
    public void testOpenWithApplicationsWithoutDuplicates() {
        query.putAssociation(ASSOCF_INIT_IGNOREUNKNOWN, ".txt", "txtfile", NOTEPAD + " %1", NOTEPAD, "Notepad");
        query.putAssociation(ASSOCF_NONE, "txtfile", "txtfile", NOTEPAD + " %1", NOTEPAD, "Notepad");
        query.putAssociation(ASSOCF_NONE, "Editor.txt", "Editor.txt", "\"" + EDITOR + "\" \"%1\"", EDITOR, "Editor");
        query.putAssociation(ASSOCF_OPEN_BYEXENAME, "notepad.exe", null, NOTEPAD + " %1", NOTEPAD, "Notepad");
        query.putAssociation(ASSOCF_OPEN_BYEXENAME, "wordpad.exe", null, "wordpad.exe \"%1\"", "C:\\wordpad.exe", null);
        registry.putValues(CURRENT_USER, REGISTRY_FILE_EXTS + ".txt" + REGISTRY_OPEN_WITH_PROGIDS,
            Map.of("Editor.txt", new byte[0]));
        registry.putValues(CLASSES_ROOT, ".txt" + REGISTRY_OPEN_WITH_PROGIDS, Map.of("txtfile", ""));
        Map<String, Object> openWithList = new LinkedHashMap<>();
        openWithList.put("a", "notepad.exe");
        openWithList.put("b", "wordpad.exe");
        openWithList.put("MRUList", "ba");
        registry.putValues(CURRENT_USER, REGISTRY_FILE_EXTS + ".txt" + REGISTRY_OPEN_WITH_LIST, openWithList);
        registry.putKeys(CLASSES_ROOT, ".txt" + REGISTRY_OPEN_WITH_LIST, List.of("notepad.exe"));

        FileCompatibleApplications applications = fileAssociations.findCompatibleApplications(new File("a.txt"));

        assertEquals(List.of("txtfile", "Editor.txt", "wordpad.exe"), List.copyOf(applications.keySet()));
        assertEquals("txtfile", applications.getDefaultApplication().getId());
        assertEquals("\"" + EDITOR + "\" %f", applications.get("Editor.txt").getExecutable());
        assertEquals("wordpad.exe", applications.get("wordpad.exe").getName());
    }

    @Test
    public void testSkipApplicationsWithoutCommand() {
        query.putAssociation(ASSOCF_INIT_IGNOREUNKNOWN, ".txt", "txtfile", NOTEPAD + " %1", NOTEPAD, "Notepad");
        query.put(ASSOCF_NONE, ASSOCSTR_FRIENDLYAPPNAME, "AppX123", "Store App");
        registry.putValues(CLASSES_ROOT, ".txt" + REGISTRY_OPEN_WITH_PROGIDS, Map.of("AppX123", ""));

        assertEquals(List.of("txtfile"),
            List.copyOf(fileAssociations.findCompatibleApplications(new File("a.txt")).keySet()));
    }

    @Test
    public void testDefaultStoreApplicationOpensThroughShell() {
        query.put(ASSOCF_INIT_IGNOREUNKNOWN, ASSOCSTR_FRIENDLYAPPNAME, ".jpg", "Photos");
        query.put(ASSOCF_INIT_IGNOREUNKNOWN, ASSOCSTR_PROGID, ".jpg", "AppX43hnxtbyyps62jhe9sqpdzxn1790zetc");

        FileCompatibleApplications applications = fileAssociations.findCompatibleApplications(new File("photo.jpg"));

        Application photos = applications.getDefaultApplication();
        assertEquals("Photos", photos.getName());
        assertEquals("AppX43hnxtbyyps62jhe9sqpdzxn1790zetc", photos.getId());
        assertEquals(SHELL_OPEN_COMMAND, photos.getExecutable());
        assertNull(photos.getIconName());
    }

    @Test
    public void testSetDefaultApplicationByProgId() {
        registry.addKey(CLASSES_ROOT, "Editor.txt");
        String userChoice = REGISTRY_FILE_EXTS + ".txt" + REGISTRY_USER_CHOICE;
        registry.addKey(CURRENT_USER, userChoice);

        fileAssociations.setDefaultApplication(new File("a.TXT"), "Editor.txt");

        assertEquals(List.of(
                "set " + path(CURRENT_USER, REGISTRY_USER_CLASSES + ".txt") + "||Editor.txt",
                "set " + path(CURRENT_USER, REGISTRY_USER_CLASSES + ".txt" + REGISTRY_OPEN_WITH_PROGIDS) +
                    "|Editor.txt|",
                "delete " + path(CURRENT_USER, userChoice)),
            registry.getWrites());
        assertFalse(registry.keyExists(CURRENT_USER, userChoice));
        assertEquals(1, changedCounter.getCount());
    }

    @Test
    public void testSetDefaultApplicationByExecutableName() {
        registry.addKey(CLASSES_ROOT, "Applications\\notepad.exe");

        fileAssociations.setDefaultApplication(new File("a.log"), "notepad.exe");

        assertEquals("set " + path(CURRENT_USER, REGISTRY_USER_CLASSES + ".log") + "||Applications\\notepad.exe",
            registry.getWrites().getFirst());
        assertEquals(1, changedCounter.getCount());
    }

    @Test
    public void testSetDefaultApplicationAlreadyDefault() {
        registry.addKey(CLASSES_ROOT, "txtfile");
        query.put(ASSOCF_INIT_IGNOREUNKNOWN, ASSOCSTR_PROGID, ".txt", "txtfile");

        fileAssociations.setDefaultApplication(new File("a.txt"), "txtfile");

        assertTrue(registry.getWrites().isEmpty());
        assertEquals(0, changedCounter.getCount());
    }

    @Test
    public void testSetDefaultApplicationUnknownApplication() {
        fileAssociations.setDefaultApplication(new File("a.txt"), "unknown.exe");

        assertTrue(registry.getWrites().isEmpty());
        assertEquals(0, changedCounter.getCount());
    }

    @Test
    public void testSetDefaultApplicationWithoutExtension() {
        registry.addKey(CLASSES_ROOT, "txtfile");

        fileAssociations.setDefaultApplication(new File("README"), "txtfile");

        assertTrue(registry.getWrites().isEmpty());
    }

    @Test
    public void testSetDefaultApplicationWithProtectedUserChoice() {
        registry.addKey(CLASSES_ROOT, "Editor.pdf");
        String userChoice = REGISTRY_FILE_EXTS + ".pdf" + REGISTRY_USER_CHOICE;
        registry.addProtectedKey(CURRENT_USER, userChoice);

        fileAssociations.setDefaultApplication(new File("a.pdf"), "Editor.pdf");

        assertTrue(registry.keyExists(CURRENT_USER, userChoice));
        assertEquals(3, registry.getWrites().size());
        assertEquals(1, changedCounter.getCount());
    }
}
