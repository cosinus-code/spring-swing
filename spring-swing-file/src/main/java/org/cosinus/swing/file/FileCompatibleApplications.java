package org.cosinus.swing.file;

import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;

public class FileCompatibleApplications extends LinkedHashMap<String, Application> {

    @Getter
    @Setter
    private Application defaultApplication;
}
