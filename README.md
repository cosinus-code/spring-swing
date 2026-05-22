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
  <dependencies>
    <dependency>
      <groupId>org.cosinuscode.swing</groupId>
      <artifactId>spring-swing-boot-starter</artifactId>
    </dependency>
  </dependencies>
```

## Package the Application

Use `spring-boot-maven-plugin` for packaging:

```xml
  <properties>
    <project.output>${project.basedir}/output</project.output>
    <application.name>spring-swing-example</application.name>
  </properties>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <version>${spring-boot.version}</version>
        <executions>
          <execution>
            <id>repackage</id>
            <configuration>
              <executable>true</executable>
              <outputDirectory>${project.output}</outputDirectory>
              <finalName>${application.name}</finalName>
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

## Logging

To use Apache Log4j 2, exclude the default logging and add `spring-boot-starter-log4j2`:

```xml
    <dependency>
      <groupId>org.cosinuscode.swing</groupId>
      <artifactId>spring-swing-boot-starter</artifactId>
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

## Application Menu

Add `menu.json` to the `conf` folder:

```json
{
    "menu": {
        "start.application": "control S",
        "quit.application": "control Q"
    },
    "help": {
        "about.application": "F1"
    }
}
```

Add the corresponding translation keys:

```properties
start.application=Start
quit.application=Quit
about.application=About
```

## Add Splash Screen

Provide the image in the `image` resources folder and register it via `maven-jar-plugin`:

```xml
  <properties>
    <splash.file.name>spring-splash.png</splash.file.name>
  </properties>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <configuration>
          <archive>
            <manifestEntries>
              <SplashScreen-Image>BOOT-INF/classes/image/${splash.file.name}</SplashScreen-Image>
            </manifestEntries>
          </archive>
        </configuration>
      </plugin>
    </plugins>
  </build>
```

## Add Startup Progress Bar

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

## Distribute the Application

Add a `spring-swing-example.sh` launcher script:

```bash
#! /bin/bash
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  APPLICATION_DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
done
APPLICATION_DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"

"$JAVA_HOME/bin/java" \
-jar $APPLICATION_DIR/spring-swing-example.jar \
-splash-progress \
-splash-progress-color=56,123,44 \
-splash-progress-y=245 \
-splash-progress-x=5
```

Use `maven-resources-plugin` to copy resources to the output folder:

```xml
  <build>
    <plugins>
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
    </plugins>
  </build>
```

Start the application from the output folder:

```shell_session
$ ./spring-swing-example.sh
```

Application properties, preferences, translations, and menu structure can be updated directly from the output folder.

## OS-Specific Conditions

Spring Swing provides Spring-style conditional annotations for platform-specific beans:

- `@ConditionalOnMac`, `@ConditionalOnWindows`, `@ConditionalOnLinux`
- `@ConditionalOnDesktop`, `@ConditionalOnGnome`, `@ConditionalOnKDE`, `@ConditionalOnXFCE`

## Testing

Use `@SpringSwingBootTest` in place of `@SpringBootTest`. It configures an in-memory `ApplicationStorage` (no file I/O) and forces `useMainMethod = NEVER`.

Each test needs a minimal `@SpringSwingBootApplication`-annotated class:

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

Tests use **JUnit 4** (`@RunWith(SpringRunner.class)`) — not JUnit 5.

## Examples

https://github.com/cosinus-code/spring-swing-example

# License
Spring Swing is Open Source software released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
