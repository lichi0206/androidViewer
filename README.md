## Android Viewer

### Tools

#### JavaFX Scene Builder

Version 2.0: https://www.oracle.com/java/technologies/javafxscenebuilder-1x-archive-downloads.html

Version 8.0: https://gluonhq.com/products/scene-builder/

Use this tool open fxml files.

#### JFoeniX

http://www.jfoenix.com/

##### Add into JavaFX Scene Builder

1. Library panel -> Gear button at the upper right corner -> "JAR/FXML Manager"
2. "Library Manager" dialog popup
3. "Actions: Search repositories" or "Add Library/FXML from file system"
4. Search and import the JFoeniX or import the JFoeniX from local file system.

##### Add into Maven JavaFX project

```xml
<dependency>
    <groupId>com.jfoenix</groupId>
    <artifactId>jfoenix</artifactId>
    <version>8.0.10</version>
</dependency>
```

### Build binary executable file

#### Package JAR with libraries file

##### Add assembly plugin

```xml
<plugin>
    <artifactId>maven-assembly-plugin</artifactId>
    <configuration>
        <descriptorRefs>
            <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
        <archive>
            <manifest>
                <mainClass>com.fendo.analysis.AnalysisStart</mainClass>
            </manifest>
        </archive>
    </configuration>
    <executions>
        <execution>
            <id>make-assembly</id>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

##### Assemble JAR with libraries file

```bash
# Terminal to run
mvn assembly:assembly

# or run
install -Dmaven.test.skip=true
```

After running above command, will generate a jar file end with "-jar-with-dependencies" in target folder.

#### Package Jar file to Exe file

##### Use Exe4j to package Jar file to binary executable file

Download and install Exe4J: https://www.ej-technologies.com/download/exe4j/files, pay attention to the "Installation Notes" part.

Run the Exe4J:

1. Skip Welcome page
2. Choose project type: "JAR in EXE" mode
3. Configure application
   1. Short name of your application
   2. Output directory
4. Configure executable
   1. Executable name
   2. Advanced Options: select "32-bit or 64-bit"
   3. Check "Generate 64-bit executable"
   4. Keep default for other settings
5. Configure Java invocation
   1. Class path -> Add button
   2. Define Class Path Entry dialog popup
   3. Entry Type: Archive
   4. Detail -> Archive: select the jar-with-dependencies.jar file
   5. Go back to Configure Java invocation
   6. Main class from: select the main class
6. Configure JRE
   1. Minimum version
   2. Maximum version
7. Keep other settings as default
8. Generate the EXE file