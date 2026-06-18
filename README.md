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

| Module | Purpose |
|---|---|
| `spring-swing-core` | Swing wrappers, context injection, actions, dialogs, menus, forms, storage, translate, preferences |
| `spring-swing-boot` | Spring Boot auto-configuration, application lifecycle, OS-specific conditions, OAuth2 |
| `spring-swing-image` | Image loading — SVG (Batik), ICNS, TIFF, JPEG (TwelveMonkeys), scaling, metadata |
| `spring-swing-file` | File utilities — commons-io, MIME detection, OSHI hardware info, process execution |
| `spring-swing-test` | `@SpringSwingBootTest`, in-memory storage, integration test support |
| `spring-swing-boot-starter` | Aggregator starter — pulls in all modules |

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
    <spring-swing.version>3.0.1</spring-swing.version>
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

To set the application name and icon only once, use the values from pom.xml:

```xml
<properties>
    <application.display.name>Spring Swing Example</application.display.name>
    <application.name>spring-swing-example</application.name>
    <application.class>org.cosinus.swing.example.HelloWorld</application.class>
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
<properties>
    <application.display.name>Spring Swing Example</application.display.name>
    <application.name>spring-swing-example</application.name>
    <application.description>Spring Swing Exemple</application.description>
    <application.version>${project.version}</application.version>
    <application.category>Other</application.category>
    <application.icon.name>spring-swing-example.png</application.icon.name>
    <application.icns.name>spring.icns</application.icns.name>
    <application.class>org.cosinus.swing.example.HelloWorld</application.class>
    <application.home.env.name>SPRING_SWING_EXAMPLE_HOME</application.home.env.name>
    <application.splash.file.name>spring-splash.png</application.splash.file.name>
    <application.splash.progress.arguments>
        -splash-progress -splash-progress-color=56,123,44 -splash-progress-y=245 -splash-progress-x=5
    </application.splash.progress.arguments>

    <java.packager.version>1.7.6</java.packager.version>
    <java.packager.mac.startup>X86_64</java.packager.mac.startup>
    <java.packager.windows.application.version>2.0.0.0</java.packager.windows.application.version>
</properties>

<profiles>
    <profile>
        <id>mac-arm</id>
        <properties>
            <java.packager.mac.startup>ARM64</java.packager.mac.startup>
        </properties>
        <activation>
            <os>
                <family>mac</family>
                <arch>arm64</arch>
            </os>
        </activation>
    </profile>
    <profile>
        <id>java-package</id>
        <activation>
            <property>
                <name>!skipJavaInstall</name>
            </property>
        </activation>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <source>${maven.compiler.source}</source>
                        <target>${maven.compiler.target}</target>
                    </configuration>
                </plugin>
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
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-resources-plugin</artifactId>
                    <executions>
                        <execution>
                            <id>copy-run-resources</id>
                            <phase>package</phase>
                            <goals>
                                <goal>copy-resources</goal>
                            </goals>
                            <configuration>
                                <outputDirectory>${project.output}</outputDirectory>
                                <resources>
                                    <resource>
                                        <directory>src/main/resources</directory>
                                    </resource>
                                </resources>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
                <plugin>
                    <groupId>io.github.fvarrui</groupId>
                    <artifactId>javapackager</artifactId>
                    <version>${java.packager.version}</version>
                    <executions>
                        <execution>
                            <phase>package</phase>
                            <goals>
                                <goal>package</goal>
                            </goals>
                            <configuration>
                                <mainClass>org.springframework.boot.loader.launch.JarLauncher</mainClass>
                                <name>${application.name}</name>
                                <organizationName>${organization.name}</organizationName>
                                <displayName>${application.display.name}</displayName>
                                <bundleJre>false</bundleJre>
                                <runnableJar>${project.basedir}/target/${application.name}.jar</runnableJar>
                                <generateInstaller>true</generateInstaller>
                                <administratorRequired>false</administratorRequired>
                                <platform>auto</platform>
                                <additionalResources>
                                    <additionalResource>src/main/resources/image/${application.splash.file.name}
                                    </additionalResource>
                                    <additionalResource>src/main/resources/image/${application.icon.name}
                                    </additionalResource>
                                </additionalResources>
                                <extra>
                                    <macArguments>${application.splash.progress.arguments}</macArguments>
                                </extra>
                                <vmArgs>
                                    <vmArg>-Dapp.name=${application.name}</vmArg>
                                    <vmArg>-Dvisualvm.display.name=${application.name}</vmArg>
                                    <vmArg>-Dswing.aatext=true</vmArg>
                                    <vmArg>-splash:${application.splash.file.name}</vmArg>
                                    <vmArg>-Dapple.laf.useScreenMenuBar=true</vmArg>
                                </vmArgs>
                                <linuxConfig>
                                    <pngFile>${application.icon.name}</pngFile>
                                    <categories>
                                        <category>${application.category}</category>
                                    </categories>
                                    <generateAppImage>false</generateAppImage>
                                    <generateDeb>true</generateDeb>
                                    <generateRpm>true</generateRpm>
                                    <wrapJar>false</wrapJar>
                                </linuxConfig>
                                <macConfig>
                                    <appId>${application.name}</appId>
                                    <icnsFile>src/main/resources/image/${application.icns.name}</icnsFile>
                                    <generateDmg>true</generateDmg>
                                    <generatePkg>true</generatePkg>
                                    <relocateJar>false</relocateJar>
                                    <volumeIcon>src/main/resources/image/${application.icns.name}</volumeIcon>
                                    <volumeName>${application.display.name}</volumeName>
                                    <macStartup>${java.packager.mac.startup}</macStartup>
                                </macConfig>

                                <winConfig>
                                    <icoFile>src/main/resources/image/${application.icon.name}</icoFile>
                                    <generateSetup>true</generateSetup>
                                    <generateMsi>true</generateMsi>
                                    <generateMsm>false</generateMsm>

                                    <headerType>gui</headerType>
                                    <wrapJar>true</wrapJar>
                                    <companyName>${organization.name}</companyName>
                                    <fileVersion>${java.packager.windows.application.version}</fileVersion>
                                    <txtFileVersion>${java.packager.windows.application.version}</txtFileVersion>
                                    <productVersion>${java.packager.windows.application.version}</productVersion>
                                    <txtProductVersion>${java.packager.windows.application.version}
                                    </txtProductVersion>
                                    <fileDescription>${description}</fileDescription>
                                    <copyright>${organization.name}</copyright>
                                    <productName>${application.display.name}</productName>
                                    <internalName>${application.display.name}</internalName>
                                    <shortcutName>${application.display.name}</shortcutName>
                                    <originalFilename>${application.name}.exe</originalFilename>

                                    <exeCreationTool>launch4j</exeCreationTool>

                                    <setupMode>installForAllUsers</setupMode>
                                    <disableDirPage>true</disableDirPage>
                                    <disableProgramGroupPage>true</disableProgramGroupPage>
                                    <disableFinishedPage>true</disableFinishedPage>
                                    <disableRunAfterInstall>true</disableRunAfterInstall>
                                    <disableWelcomePage>true</disableWelcomePage>
                                    <createDesktopIconTask>true</createDesktopIconTask>
                                    <removeOldLibs>false</removeOldLibs>
                                </winConfig>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```
Run `./install.sh`:

```shell_session
#! /bin/bash
basedir=$(dirname "$0")
source "$basedir/echo-it.sh"
cd "$basedir" || exit

run_or_continue_if_fail() {
    if ! "$@"; then
        echo "❌ Failed to run $*" >&2
    fi
}

run_or_die() {
    if ! "$@"; then
        echo "❌ Failed to install ${application_display_name} while running: $*" >&2
        exit 1
    fi
}

maven_property() {
  mvn help:evaluate -Dexpression="$1" -q -DforceStdout
}

application_name=$(maven_property application.name)
application_display_name=$(maven_property application.display.name)
application_icon_name=$(maven_property application.icon.name)
application_version=$(maven_property project.version)
application_arguments=$(maven_property application.splash.progress.arguments)

show-info "Building ${application_display_name}..."
run_or_die mvn clean install -Pjava-package

show-info "Installing ${application_display_name}..."
if [ "$(uname -s)" = "Linux" ]; then
  application_deb_file="target/${application_name}_${application_version}.deb"
  application_rpm_file="target/${application_name}_${application_version}.rpm"
  application_desktop_file="target/assets/${application_name}.desktop"
  target_desktop_file="$HOME/.local/share/applications/${application_name}.desktop"

  if [ "$(grep -Ei 'debian|ubuntu|mint' /etc/os-release)" ]; then
    run_or_die sudo dpkg -i ${application_deb_file}
  fi

  if [ "$(grep -Ei 'fedora|redhat' /etc/os-release)" ]; then
    run_or_die sudo dnf install ${application_rpm_file}
  fi

  run_or_die sed -i "s/%U/${application_arguments}/" ${application_desktop_file}
  run_or_die cp ${application_desktop_file} ${target_desktop_file}
  run_or_die chmod +x ${target_desktop_file}
  run_or_die sudo update-desktop-database
fi

show-info "${application_display_name} was installed"
```

Known issue: progress bar doesn't show on Windows.

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
