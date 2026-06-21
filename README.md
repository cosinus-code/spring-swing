# Spring Swing
Spring Swing is a framework for building Spring Boot–powered Swing desktop applications.

# Description

It starts a Swing application inside a full Spring context and injects Spring beans into Swing objects created at runtime.

For objects not managed by Spring (instantiated via `new`), call `injectContext(this)` in the constructor:

```java
import org.cosinus.swing.store.ApplicationStorage;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class SwingObject {

    @Autowired
    public ApplicationStorage applicationStorage;

    public SwingObject() {
        injectContext(this);
    }
}
```

All framework base classes (`Panel`, `Frame`, `Dialog`, `Table`, etc.) do this automatically — extend them instead of the raw Swing types:

```java
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.store.ApplicationStorage;

public class MyPanel extends Panel {

    @Autowired
    public ApplicationStorage applicationStorage;

}
```

## Modules

| Module                             | Purpose                                                                                            |
|------------------------------------|----------------------------------------------------------------------------------------------------|
| `spring-swing-core`                | Swing wrappers, context injection, actions, dialogs, menus, forms, storage, translate, preferences |
| `spring-swing-boot`                | Spring Boot auto-configuration, application lifecycle, OS-specific conditions, OAuth2              |
| `spring-swing-image`               | Image loading — SVG (Batik), ICNS, TIFF, JPEG (TwelveMonkeys), scaling, metadata                   |
| `spring-swing-file`                | File utilities — commons-io, MIME detection, OSHI hardware info, process execution                 |
| `spring-swing-test`                | `@SpringSwingBootTest`, in-memory storage, integration test support                                |
| `spring-swing-boot-starter`        | Aggregator starter — pulls in all modules                                                          |
| `spring-swing-boot-starter-parent` | Aggregator starter to be used as parent                                                            |

# Getting Started

```java
package org.cosinus.swing.example;

import org.cosinus.swing.boot.SpringSwingApplication;
import org.cosinus.swing.boot.SpringSwingBootApplication;
import org.cosinus.swing.boot.application.SwingApplicationFrame;

import javax.swing.*;
import java.awt.*;

@SpringSwingBootApplication
public class HelloWorld extends SwingApplicationFrame {

    @Override
    public void initApplicationFrame() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Hello World", SwingConstants.CENTER));
        add(panel);
    }

    public static void main(String[] args) {
        SpringSwingApplication.run(HelloWorld.class, args);
    }
}
```

## Dependencies

Add `spring-swing-boot-starter` to your project. Spring Swing requires **Java 21** and is built on **Spring Boot 3.x**.

```xml

<properties>
    <spring-swing.version>3.0.2</spring-swing.version>
</properties>
<dependencies>
<dependency>
    <groupId>org.cosinuscode.swing</groupId>
    <artifactId>spring-swing-boot-starter</artifactId>
    <version>${spring-swing.version}</version>
</dependency>
</dependencies>
```

## Package the Application

Use `spring-boot-maven-plugin` for packaging:

```xml

<properties>
    <application.name>spring-swing-example</application.name>
    <application.class>org.cosinus.swing.example.HelloWorld</application.class>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <executions>
                <execution>
                    <id>repackage</id>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                    <configuration>
                        <executable>true</executable>
                        <finalName>${application.name}</finalName>
                        <mainClass>${application.class}</mainClass>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

```shell_session
$ mvn package
```

## Run the Application

```shell_session
$ ./spring-swing-example.jar
```

## Application Name and Icon

Set the application name (used as the main window title) and icon in `application.yml`:

```yaml
swing:
  application:
    name: Spring Swing Example
    icon: spring.png
```

The icon file default location is the `image` resources folder.

Alternatively, use the values from pom.xml:

```xml
<properties>
    <application.display.name>Spring Swing Example</application.display.name>
    <application.name>spring-swing-example</application.name>
    <application.icon.name>spring-swing-example.png</application.icon.name>
</properties>
```
```yaml
swing:
  application:
    name: @application.display.name@
    icon: @application.icon.name@
```


## Logging

To use Apache Log4j 2, exclude the default logging and add `spring-boot-starter-log4j2`:

```xml

<dependency>
    <groupId>org.cosinuscode.swing</groupId>
    <artifactId>spring-swing-boot-starter</artifactId>
    <version>${spring-swing.version}</version>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
```

## Internationalization

Replace literal strings with `translate("hello.world")` and add translation properties files to the `i18n` resources folder:

`messages_en_us.properties`:
```properties
hello.world=Hello World
```

To set the default language, add `preferences.json` in the `conf` folder:

```json
{
  "appearance": {
    "language": {
      "type": "language",
      "value": "fr"
    }
  }
}
```

`messages_fr_fr.properties`:
```properties
hello.world=Salut monde
```

## Preferences

As seen above, the preferences are set in `preferences.json` in the `conf` folder.
An example of file content:

```json
{
  "appearance": {
    "look-and-feel": {
      "type": "laf",
      "value": ""
    },
    "language": {
      "type": "language",
      "value": "fr"
    },
    "background-color": {
      "type": "color",
      "value": "255,255,255"
    },
    "foreground-color": {
      "type": "color",
      "value": "0,0,0"
    }
  }
}
```
A default dialog for editing preferences is available out-of-the-box like in the corresponding menu action:

```java
import org.cosinus.swing.action.SwingAction;
import org.cosinus.swing.dialog.DialogHandler;
import org.springframework.stereotype.Component;

import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;
import static org.cosinus.swing.image.icon.IconProvider.ICON_SETTINGS;

@Component
public class EditPreferencesAction implements SwingAction {

    private static final String EDIT_PREFERENCES = "settings";

    private final DialogHandler dialogHandler;

    public EditPreferencesAction(DialogHandler dialogHandler) {
        this.dialogHandler = dialogHandler;
    }

    @Override
    public void run() {
        dialogHandler.showPreferencesDialog(applicationFrame);
    }

    @Override
    public String getIconName() {
        return ICON_SETTINGS;
    }

    @Override
    public String getId() {
        return EDIT_PREFERENCES;
    }
}
```
When preferences are updated, the changes are kept in "preferneces.json" file saved in ~/.<applicastion.name>/

## Application Menu

Add `menu.json` to the `conf` folder:

```json
{
  "menu": {
    "start": {
      "shortcut": "control S"
    },
    "settings": {
      "shortcut": "control P"
    },
    "separator-1": {},
    "quit": {
      "shortcut": "alt F4",
      "icon": "quit",
      "hideOnMac": true
    }
  }
}
```

Add the corresponding translation keys:

```properties
start=Start
quit=Quit
settings=Settings
```

## Splash Screen

Provide the image in the `image` resources folder and register it via `maven-jar-plugin`:

```xml

<properties>
    <application.splash.file.name>spring-splash.png</application.splash.file.name>
</properties>

<build>
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <configuration>
            <archive>
                <manifestEntries>
                    <SplashScreen-Image>BOOT-INF/classes/image/${application.splash.file.name}
                    </SplashScreen-Image>
                </manifestEntries>
            </archive>
        </configuration>
    </plugin>
</plugins>
</build>
```

## Startup Progress Bar

Pass `-splash-progress` when running the application:

```shell_session
$ ./spring-swing-example.jar -splash-progress
```

Customise the progress bar position and color:

```shell_session
$ ./spring-swing-example.jar \
-splash-progress \
-splash-progress-color=56,123,44 \
-splash-progress-y=245 \
-splash-progress-x=5
```

## Install the Application

```xml
<parent>
    <groupId>org.cosinuscode.swing</groupId>
    <artifactId>spring-swing-boot-starter-parent</artifactId>
    <version>3.0.2</version>
</parent>

<properties>
    <application.display.name>Spring Swing Example</application.display.name>
    <application.name>spring-swing-example</application.name>
    <application.description>Spring Swing Example</application.description>
    <application.version>${project.version}</application.version>
    <application.category>Other</application.category>
    <application.icon.name>spring-swing-example.png</application.icon.name>
    <application.ico.name>spring-swing-example.ico</application.ico.name>
    <application.icns.name>spring-swing-example.icns</application.icns.name>
    <application.class>org.cosinus.swing.example.HelloWorld</application.class>
    <application.home.env.name>SPRING_SWING_EXAMPLE_HOME</application.home.env.name>
    <application.splash.file.name>spring-splash.png</application.splash.file.name>
    <application.splash.progress>-Dsplash-progress=color=56,123,44;y=245;x=5</application.splash.progress>
</properties>
```
Run `mvn clean install -PinstallApp`:

Known issue: splash progress bar doesn't show on Windows.

## JSON descriptors for dialogs

Simple dialogs can be described in JSON format, like the following permissionsDialog.json:

```json
{
  "fields": [
    {
      "id": "owner-name",
      "type": "label",
      "label": "owner-name"
    },
    {
      "id": "group-name",
      "type": "combobox",
      "label": "group-name",
      "focus": true
    },
    {
      "id": "owner-permissions",
      "type": "boolean-list",
      "label": "owner-permissions",
      "action": "refresh"
    },
    {
      "id": "group-permissions",
      "type": "boolean-list",
      "label": "group-permissions",
      "action": "refresh"
    },
    {
      "id": "others-permissions",
      "type": "boolean-list",
      "label": "others-permissions",
      "action": "refresh"
    },
    {
      "id": "set-user-id",
      "type": "boolean",
      "label": "set-user-id",
      "action": "refresh"
    },
    {
      "id": "set-group-id",
      "type": "boolean",
      "label": "set-group-id",
      "action": "refresh"
    },
    {
      "id": "sticky",
      "type": "boolean",
      "label": "sticky",
      "action": "refresh"
    },
    {
      "id": "permissions-text-view",
      "type": "label",
      "label": "permissions-text-view"
    },
    {
      "id": "permissions-number-view",
      "type": "label",
      "label": "permissions-number-view"
    }
  ],
  "buttons": [
    {
      "id": "cancel",
      "label": "cancel",
      "action": "cancel"
    },
    {
      "id": "ok",
      "label": "ok",
      "action": "ok"
    }
  ]
}
```

```java
PermissionsModel permissionsModel = new PermissionsModel(permissions);
Permissions permissions = dialogHandler
    .showDialog(() -> dialogHandler.createDialog(applicationFrame, "permissionsDialog.json", permissionsModel))
    .response()
    .map(PermissionsModel::getPermissions);
```

## OS-Specific Conditions

Spring Swing provides Spring-style conditional annotations for platform-specific beans:

- `@ConditionalOnMac`, `@ConditionalOnWindows`, `@ConditionalOnLinux`
- `@ConditionalOnDesktop`, `@ConditionalOnGnome`, `@ConditionalOnKDE`, `@ConditionalOnXFCE`

## Testing

Use `@SpringSwingBootTest` in place of `@SpringBootTest`.

```java
@SpringSwingBootApplication
public class TestApplication extends SpringSwingApplication {
    public static void main(String[] args) {
        SpringSwingApplication.run(TestApplication.class, args);
    }
}

@SpringSwingBootTest(classes = TestApplication.class)
@RunWith(SpringRunner.class)
public class MyTest {
    @Autowired
    private MyService myService;

    @Test
    public void testSomething() {
        assertNotNull(myService);
    }
}
```

## Examples

https://github.com/cosinus-code/spring-swing-example

# License
Spring Swing is Open Source software released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
