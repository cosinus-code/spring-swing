# Spring Swing
Spring Swing is a framework designed for building Spring-powered Swing applications.

# Description

It allows you to start a Swing application in full Spring context. 
It also allows injection of Spring beans into Swing objects created during application run:

```java
import org.cosinus.swing.store.ApplicationStorage;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class SwingObject {

    @Autowired
    public ApplicationStorage applicationStorage;

	public SwingApplicationContextAware() {
        injectContext(this);
	}
}
```

There are already Swing object rewritten to auto-inject the Spring context:

```java
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.store.ApplicationStorage;

public class MyPanel implements Panel {

    @Autowired
    public ApplicationStorage applicationStorage;

}
```
# Getting Started Using Spring Swing

Here is the Java code for starting a Spring Swing application:

```java
package org.cosinus.swing.example;

import org.cosinus.swing.boot.ApplicationFrame;
import org.cosinus.swing.boot.SpringSwingApplication;
import org.cosinus.swing.boot.SpringSwingBootApplication;

import javax.swing.*;
import java.awt.*;

@SpringSwingBootApplication
public class HelloWorld extends ApplicationFrame {

    @Override
    public void initComponents() {
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
```xml
  <parent>
    <groupId>org.cosinus.swing</groupId>
    <artifactId>spring-swing-parent</artifactId>
    <version>1.0-SNAPSHOT</version>
  </parent>

  <properties>
    ...
    <spring-swing.version>1.0-SNAPSHOT</spring-swing.version>
    ...
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.cosinus.swing</groupId>
      <artifactId>spring-swing-starter</artifactId>
      <version>${spring-swing.version}</version>
    </dependency>
  </dependencies>
```
Spring Swing runs with java 11, so the compiler should be configured for java 11.

## Package the Application
The spring-boot maven plugin can be used for packaging:  

```xml
  <properties>
    ...
    <project.output>${project.basedir}/output</project.output>
    <application.name>spring-swing-example</application.name>
    ...
  </properties>

  <build>
    <plugins>
      ...
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

Package command:

```shell_session
$ mvn package
```

## Run the Application
The jar can be executed from the `${project.output}` folder:

```shell_session
$ ./spring-swing-example.jar
```

## Application Name and Icon
The application name (which is the main window title) and the application icon 
can be specified in the application properties file `application.yml`:

```yaml
swing:
  application:
    name: Sping Swing Example
    icon: spring.png
```
The icon file default location is in the `image` resources folder.
 
## Logging
To add Apache Log4j 2 to the application exclude the `spring-boot-starter-logging` and add `spring-boot-starter-log4j2`:
```xml
    <dependency>
      <groupId>org.cosinus.swing</groupId>
      <artifactId>spring-swing-starter</artifactId>
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
Then add log4j2 file to configure the logging. 

## Internationalization
To translate the message, replace `"Hello World"` with `translate("hello.world")` 
and add in the `i18n` resources folder the standard translations properties file 
`messages_en_us.properties`:

```properties
hello.world=Hello World
```

To change the language of the application, add the `preferences.json` file in the `conf` folder 
and specify the preferred language:

```json
{
  "language": "fr_fr"
}
``` 
along with the translations file `messages_fr_fr.properties`:
```properties
hello.world=Salut monde
```

## Application Menu
To attach a menu to the application, add in the `conf` folder a `menu.json` file with the menu structure 
like the following:
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
To translate the specific keys (e.g. "start.application", "quit.application", "about.application"), 
add those in the translation files:

```properties
hello.world=Hello World
start.application=Start
quit.application=Quit
about.application=About
```

## Add Splash Screen
To add a slash screen to the application, just provide the image in `image` resources folder and 
add it in `SplashScreen-Image` entry of jar manifest using the `maven-jar-plugin`:

```xml
  <properties>
    ...
    <splash.file.name>spring-splash.png</splash.file.name>
  </properties>

  <build>
    <plugins>
      ...
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
To add progress bar to the splash screen, 
we need to pass the `-splash-progress` argument when running the application:

```shell_session
$ ./spring-swing-example.jar -splash-progress
```
The progress bar can be customized using dedicated arguments:
```shell_session
$ ./spring-swing-example.jar \ 
-splash-progress \ 
-splash-progress-color=56,123,44 \ 
-splash-progress-y=245 \ 
-splash-progress-x=5
```

## Distribute the Application 
To simplify the run of the application, 
we can add the `spring-swing-example.sh` bash file with start command:
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
and use `maven-resources-plugin` to copy all resources to the output folder:
```xml
  <build>
    <plugins>
      ...
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
Now simply start the application by running the bash file from the output folder:
```shell_session
$ ./spring-swing-example.sh
```
The application properties, preferences, translations and menu structure 
can now be updated directly from the output folder.

## Examples
https://github.com/cosinus-code/spring-swing-example

# License
The Spring Swing is Open Source software released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
