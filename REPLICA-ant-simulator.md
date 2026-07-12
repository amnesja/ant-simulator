# Replica completa del progetto: Ant Simulator (PSS25-antsim)

Guida esaustiva per ricostruire da zero il progetto di simulazione di un ecosistema di formiche (ispirato ad Ant Colony Optimization). Contiene **tutto**: setup repository, branching, ogni file di configurazione, l'intero codice sorgente, le risorse, i test e le istruzioni di build/esecuzione. Ogni listato e' copiato fedelmente dai file reali.

> Nota: il gradle-wrapper.jar e' un file binario (48 KB) e non viene riportato qui come testo: lo si rigenera con `gradle wrapper --gradle-version 9.5.1` (vedi Parte 3).

## 0. Prerequisiti

- **Git** installato e account **GitHub**.
- **Java**: il progetto usa il *toolchain* Gradle (Java 26). Non serve installare Java a mano: Gradle lo scarica automaticamente (plugin `foojay-resolver-convention`).
- **Gradle**: NON serve installarlo; si usa il wrapper (`gradlew`). Eventualmente per generare il wrapper serve Gradle >= 9.5.1.
- Sistema operativo: Linux (testato), Windows e macOS (cross-platform JavaFX).

## 1. Creazione del repository (regole d'esame P8, P9, M1)

1. Su GitHub creare un repository **pubblico** chiamato `PSS25-antsim` (acronimo `antsim`, <=10 caratteri alfanumerici minuscoli).
2. Clonarlo in locale:
   ```bash
   git clone https://github.com/<utente>/PSS25-antsim.git
   cd PSS25-antsim
   ```
3. Branch principali (git flow semplificato):
   - `main` : sempre integro, contiene la consegna finale (report.pdf + jar).
   - `develop` : ramo di integrazione.
   - `feature/<nome>` : un branch per ogni funzionala' (es. `feature/domain-core`, `feature/aco-implementation`, `feature/ui-javafx`, `feature/simulation-engine`).
4. Aggiungere `.gitattributes` prima del primo commit per normalizzare gli EOL.
5. **Non usare Git LFS** (vietato da P10).
6. Alla consegna il repo deve contenere nella radice: `report.pdf` e l'unico `jar` eseguibile (costruito dallo shadowJar), piu' `build.gradle.kts` e la configurazione del wrapper.

## 2. Struttura delle directory

```text
PSS25-antsim/
├── .github/workflows/build-and-deploy.yml
├── gradle/wrapper/gradle-wrapper.jar
├── gradle/wrapper/gradle-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/it/unibo/antsim/
│   │   │       ├── Main.java
│   │   │       ├── config/SimulationConfig.java
│   │   │       ├── config/ViewConfig.java
│   │   │       ├── controller/SimulationController.java
│   │   │       ├── model/SimulationState.java
│   │   │       ├── model/agent/Ant.java
│   │   │       ├── model/agent/AntState.java
│   │   │       ├── model/environment/Cell.java
│   │   │       ├── model/environment/CellType.java
│   │   │       ├── model/environment/Environment.java
│   │   │       ├── model/environment/Grid.java
│   │   │       ├── model/environment/Position.java
│   │   │       ├── simulation/SimulationEngine.java
│   │   │       ├── simulation/SimulationStatus.java
│   │   │       ├── view/Camera.java
│   │   │       ├── view/ControlPanel.java
│   │   │       ├── view/PheromoneViewMode.java
│   │   │       ├── view/SimulationView.java
│   │   │       └── view/StatsPanel.java
│   │   └── resources/style.css
│   └── test/java/it/unibo/antsim/
│       ├── model/agent/AntTest.java
│       ├── model/agent/AcoTest.java
│       ├── model/agent/ConvergenceTest.java
│       └── model/environment/EnvironmentTest.java
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew  (script unix, eseguibile)
├── gradlew.bat
├── .gitignore
├── .gitattributes
├── .mergify.yml
├── renovate.json
└── report.pdf  (alla consegna)
```

## 3. File di root e di build

### `build.gradle.kts`

```kotlin
/*
plugins {
    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building a CLI application
    // You can run your app via task "run": ./gradlew run
    application

    /*
     * Adds tasks to export a runnable jar.
     * In order to create it, launch the "shadowJar" task.
     * The runnable jar will be found in build/libs/projectname-all.jar
     */
    id("com.gradleup.shadow") version "9.4.1"
    id("org.danilopianini.gradle-java-qa") version "1.178.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        // Java version used to compile and run the project
        languageVersion.set(JavaLanguageVersion.of(26))
    }
}

val javaFXModules = listOf("base", "controls", "fxml", "swing", "graphics")

val supportedPlatforms = listOf("linux", "mac", "win") // All required for OOP
dependencies {
    // Suppressions for SpotBugs
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")

    // Example library: Guava. Add what you need (and use the latest version where appropriate).
    // implementation("com.google.guava:guava:28.1-jre")

    // JavaFX: comment out if you do not need them
    val javaFxVersion = "23.0.2"
    implementation("org.openjfx:javafx:$javaFxVersion")
    for (platform in supportedPlatforms) {
        for (module in javaFXModules) {
            implementation("org.openjfx:javafx-$module:$javaFxVersion:$platform")
        }
    }

    for (module in javaFXModules) {
        implementation("org.openjfx:javafx-$module:$javaFxVersion:win")
    }

    // The BOM (Bill of Materials) synchronizes all the versions of Junit coherently.
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    // The annotations, assertions and other elements we want to have access when compiling our tests.
    testImplementation("org.junit.jupiter:junit-jupiter")
    // The engine that must be available at runtime to run the tests.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    // Enables JUnit 5 Jupiter module
    useJUnitPlatform()
}

val main: String by project

application {
    // Define the main class for the application
    mainClass.set("it.unibo.antsim.Main")
}

tasks.named<JavaExec>("run").configure {
    jvmArgs = listOf(
        "--module-path", classpath.asPath,
        "--add-modules", "javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.swing"
    )
}
*/


plugins {
    java
    application
    id("com.gradleup.shadow") version "9.4.1"
    id("org.danilopianini.gradle-java-qa") version "1.178.0"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(26))
    }
}

javafx {
    version = "23.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClass.set("it.unibo.antsim.Main")
}

```

### `settings.gradle.kts`

```kotlin
plugins {
    // Automatically downloads the correct java version to run the static analyzers
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ant-simulator"

```

### `gradle.properties`

```properties
main=it.unibo.antsim.main
```

### `.gitignore`

```text
# Ignore Gradle project-specific cache directory
.gradle

# Ignore Gradle build output directory
build

bin/

.idea

.settings
```

### `.gitattributes`

```text
* text=auto eol=lf
*.[cC][mM][dD] text eol=crlf
*.[bB][aA][tT] text eol=crlf
*.[pP][sS]1 text eol=crlf

```

### `.mergify.yml`

```yaml
extends: mergify-config

```

### `renovate.json`

```json
{
   "$schema": "https://docs.renovatebot.com/renovate-schema.json",
   "extends": [
      "github>DanySK/renovate-config"
   ]
}

```

### `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionSha256Sum=bafc141b619ad6350fd975fc903156dd5c151998cc8b058e8c1044ab5f7b031f
distributionUrl=https\://services.gradle.org/distributions/gradle-9.5.1-bin.zip
networkTimeout=10000
retries=0
retryBackOffMs=500
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists

```

### `gradlew`

```bash
#!/bin/sh

#
# Copyright © 2015 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
#

##############################################################################
#
#   Gradle start up script for POSIX generated by Gradle.
#
#   Important for running:
#
#   (1) You need a POSIX-compliant shell to run this script. If your /bin/sh is
#       noncompliant, but you have some other compliant shell such as ksh or
#       bash, then to run this script, type that shell name before the whole
#       command line, like:
#
#           ksh Gradle
#
#       Busybox and similar reduced shells will NOT work, because this script
#       requires all of these POSIX shell features:
#         * functions;
#         * expansions «$var», «${var}», «${var:-default}», «${var+SET}»,
#           «${var#prefix}», «${var%suffix}», and «$( cmd )»;
#         * compound commands having a testable exit status, especially «case»;
#         * various built-in commands including «command», «set», and «ulimit».
#
#   Important for patching:
#
#   (2) This script targets any POSIX shell, so it avoids extensions provided
#       by Bash, Ksh, etc; in particular arrays are avoided.
#
#       The "traditional" practice of packing multiple parameters into a
#       space-separated string is a well documented source of bugs and security
#       problems, so this is (mostly) avoided, by progressively accumulating
#       options in "$@", and eventually passing that to Java.
#
#       Where the inherited environment variables (DEFAULT_JVM_OPTS, JAVA_OPTS,
#       and GRADLE_OPTS) rely on word-splitting, this is performed explicitly;
#       see the in-line comments for details.
#
#       There are tweaks for specific operating systems such as AIX, CygWin,
#       Darwin, MinGW, and NonStop.
#
#   (3) This script is generated from the Groovy template
#       https://github.com/gradle/gradle/blob/3d91ce3b8caaf77ad09f381f43615b715b53f72c/platforms/jvm/plugins-application/src/main/resources/org/gradle/api/internal/plugins/unixStartScript.txt
#       within the Gradle project.
#
#       You can find Gradle at https://github.com/gradle/gradle/.
#
##############################################################################

# Attempt to set APP_HOME

# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
    [ -h "$app_path" ]
do
    ls=$( ls -ld "$app_path" )
    link=${ls#*' -> '}
    case $link in             #(
      /*)   app_path=$link ;; #(
      *)    app_path=$APP_HOME$link ;;
    esac
done

# This is normally unused
# shellcheck disable=SC2034
APP_BASE_NAME=${0##*/}
# Discard cd standard output in case $CDPATH is set (https://github.com/gradle/gradle/issues/25036)
APP_HOME=$( cd -P "${APP_HOME:-./}" > /dev/null && printf '%s\n' "$PWD" ) || exit

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$( uname )" in                #(
  CYGWIN* )         cygwin=true  ;; #(
  Darwin* )         darwin=true  ;; #(
  MSYS* | MINGW* )  msys=true    ;; #(
  NONSTOP* )        nonstop=true ;;
esac



# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD=java
    if ! command -v java >/dev/null 2>&1
    then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in #(
      max*)
        # In POSIX sh, ulimit -H is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039,SC3045
        MAX_FD=$( ulimit -H -n ) ||
            warn "Could not query maximum file descriptor limit"
    esac
    case $MAX_FD in  #(
      '' | soft) :;; #(
      *)
        # In POSIX sh, ulimit -n is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039,SC3045
        ulimit -n "$MAX_FD" ||
            warn "Could not set maximum file descriptor limit to $MAX_FD"
    esac
fi

# Collect all arguments for the java command, stacking in reverse order:
#   * args from the command line
#   * the main class name
#   * -classpath
#   * -D...appname settings
#   * --module-path (only if needed)
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and GRADLE_OPTS environment variables.

# For Cygwin or MSYS, switch paths to Windows format before running java
if "$cygwin" || "$msys" ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )

    JAVACMD=$( cygpath --unix "$JAVACMD" )

    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    for arg do
        if
            case $arg in                                #(
              -*)   false ;;                            # don't mess with options #(
              /?*)  t=${arg#/} t=/${t%%/*}              # looks like a POSIX filepath
                    [ -e "$t" ] ;;                      #(
              *)    false ;;
            esac
        then
            arg=$( cygpath --path --ignore --mixed "$arg" )
        fi
        # Roll the args list around exactly as many times as the number of
        # args, so each arg winds up back in the position where it started, but
        # possibly modified.
        #
        # NB: a `for` loop captures its iteration list before it begins, so
        # changing the positional parameters here affects neither the number of
        # iterations, nor the values presented in `arg`.
        shift                   # remove old arg
        set -- "$@" "$arg"      # push replacement arg
    done
fi


# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Collect all arguments for the java command:
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and optsEnvironmentVar are not allowed to contain shell fragments,
#     and any embedded shellness will be escaped.
#   * For example: A user cannot expect ${Hostname} to be expanded, as it is an environment variable and will be
#     treated as '${Hostname}' itself on the command line.

set -- \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
        -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
        "$@"

# Stop when "xargs" is not available.
if ! command -v xargs >/dev/null 2>&1
then
    die "xargs is not available"
fi

# Use "xargs" to parse quoted args.
#
# With -n1 it outputs one arg per line, with the quotes and backslashes removed.
#
# In Bash we could simply go:
#
#   readarray ARGS < <( xargs -n1 <<<"$var" ) &&
#   set -- "${ARGS[@]}" "$@"
#
# but POSIX shell has neither arrays nor command substitution, so instead we
# post-process each arg (as a line of input to sed) to backslash-escape any
# character that might be a shell metacharacter, then use eval to reverse
# that process (while maintaining the separation between arguments), and wrap
# the whole thing up as a single "set" statement.
#
# This will of course break if any of these variables contains a newline or
# an unmatched quote.
#

eval "set -- $(
        printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" |
        xargs -n1 |
        sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
        tr '\n' ' '
    )" '"$@"'

exec "$JAVACMD" "$@"

```

### `gradlew.bat`

```bat
@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables, and ensure extensions are enabled
setlocal EnableExtensions

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

"%COMSPEC%" /c exit 1

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

"%COMSPEC%" /c exit 1

:execute
@rem Setup the command line



@rem Execute Gradle
@rem endlocal doesn't take effect until after the line is parsed and variables are expanded
@rem which allows us to clear the local environment before executing the java command
endlocal & "%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %* & call :exitWithErrorLevel

:exitWithErrorLevel
@rem Use "%COMSPEC%" /c exit to allow operators to work properly in scripts
"%COMSPEC%" /c exit %ERRORLEVEL%

```

### `.github/workflows/build-and-deploy.yml`

```yaml
name: CI
on:
  push:
    tags: '*'
    branches-ignore:
      - 'autodelivery**'
      - 'bump-**'
      - 'renovate/**'
    paths-ignore:
      - 'CHANGELOG.md'
      - 'LICENSE'
      - 'README.md'
      - 'renovate.json'
  pull_request:
  workflow_dispatch:

jobs:
  build:
    if: contains(github.repository, 'sample-javafx-project') && !github.event.repository.fork
    strategy:
      fail-fast: false
      matrix:
        os:
          - windows-2025
          - macos-15
          - ubuntu-24.04
        jvm_version: [21, 25]
    runs-on: ${{ matrix.os }}
    steps:
      - name: Checkout
        uses: DanySK/action-checkout@0.2.28
      - uses: DanySK/build-check-deploy-gradle-action@4.0.32
        with:
          java-version: ${{ matrix.jvm_version }}
          should-run-codecov: false
          should-deploy: false
          should-validate-wrapper: ${{ matrix.os == 'ubuntu' && matrix.jvm_version == '17' }}
  success:
    runs-on: ubuntu-24.04
    needs:
      - build
    if: >-
      always() && (
        contains(join(needs.*.result, ','), 'failure')
        || !contains(join(needs.*.result, ','), 'cancelled')
      )
    steps:
      - name: Verify that there were no failures
        run: ${{ !contains(join(needs.*.result, ','), 'failure') }}

```

## 4. Codice sorgente (`src/main/java`)

### `src/main/java/it/unibo/antsim/Main.java`

```java
package it.unibo.antsim;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.config.ViewConfig;
import it.unibo.antsim.controller.SimulationController;
import it.unibo.antsim.model.agent.Ant;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Position;
import it.unibo.antsim.simulation.SimulationEngine;
import it.unibo.antsim.view.ControlPanel;
import it.unibo.antsim.view.SimulationView;
import it.unibo.antsim.view.StatsPanel;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    private SimulationController controller;
    private SimulationEngine engine;
    private SimulationView view;
    private StatsPanel statsPanel;
    private ControlPanel controlPanel;

    @Override
    public void start(Stage primaryStage) {
        Environment environment = createEnvironment();
        engine = createEngine(environment);
        controller = new SimulationController(engine);
        view = new SimulationView(environment);
        statsPanel = new StatsPanel();

        BorderPane root = createLayout();
        Scene scene = new Scene(root, ViewConfig.SCENE_WIDTH, ViewConfig.SCENE_HEIGHT);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        view.render(engine.getAnts());
        statsPanel.update(engine);

        primaryStage.setTitle("Ant Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        bindViewSize(scene);
        setupRenderingLoop();
        primaryStage.setOnCloseRequest(e -> {
            controller.stop();
            System.out.println("Simulation stopped!");
        });
    }

    private Environment createEnvironment() {
        Environment environment = new Environment(SimulationConfig.WORLD_WIDTH, SimulationConfig.WORLD_HEIGHT);
        environment.setNestPosition(new Position(SimulationConfig.NEST_X, SimulationConfig.NEST_Y));
        environment.generateRockClusters();
        environment.generateRandomFoodCluster();
        return environment;
    }

    private SimulationEngine createEngine(Environment environment) {
        SimulationEngine simulationEngine = new SimulationEngine(environment);
        simulationEngine.setFoodGenerationInterval(SimulationConfig.FOOD_GENERATION_INTERVAL);

        Position nest = environment.getNestPosition();
        for (int i = 0; i < SimulationConfig.INITIAL_AGENT_COUNT; i++) {
            simulationEngine.addAnt(new Ant(nest.x(), nest.y()));
        }

        return simulationEngine;
    }

    private BorderPane createLayout() {
        BorderPane root = new BorderPane();

        controlPanel = new ControlPanel(controller, engine, view, statsPanel);

        root.setCenter(view);
        root.setRight(controlPanel);
        root.setBottom(statsPanel);

        return root;
    }

    /**
     * Binds the simulation canvas to fill the center area (right panel and
     * bottom stats bar excluded). The camera follows the canvas size via a
     * listener in SimulationView, so the world always occupies the whole view
     * instead of being letterboxed into a square.
     */
    private void bindViewSize(Scene scene) {
        view.widthProperty().bind(scene.widthProperty().subtract(controlPanel.widthProperty()));
        view.heightProperty().bind(scene.heightProperty().subtract(statsPanel.heightProperty()));
    }

    private void setupRenderingLoop() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                view.render(engine.getAnts());
                statsPanel.update(engine);
            }
        };
        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

```

### `src/main/java/it/unibo/antsim/config/SimulationConfig.java`

```java
package it.unibo.antsim.config;

public final class SimulationConfig {
    // World (continuous space sampled on a cell grid). Large enough to be explored
    // by zooming/panning the camera instead of being shown all at once.
    public static final int WORLD_WIDTH = 600;
    public static final int WORLD_HEIGHT = 600;

    // Colony (mutable: tunable from the UI at runtime). Sized for a large world.
    public static int INITIAL_AGENT_COUNT = 2000;

    // Nest and food cluster (world cell coordinates)
    public static final int NEST_X = 24;
    public static final int NEST_Y = 80;
    // Mutable: tunable from the UI (applied on "Nuovo mondo")
    public static double FOOD_CLUSTER_RADIUS = 14.0;
    public static int FOOD_CLUSTER_HP = 120;

    // Optional periodic food regeneration (set false to keep a single cluster)
    public static final boolean ENABLE_FOOD_REGEN = false;
    public static final int FOOD_GENERATION_INTERVAL = 1000;

    // Continuous movement (sensor-based steering, like Ant Simulator 2)
    // All mutable so the user can tweak them live from the control panel.
    public static double ANT_SPEED = 0.5;            // cells per simulation step
    public static double SENSOR_DISTANCE = 5.0;       // how far ahead ants sense
    public static double SENSOR_ANGLE = 0.6;          // radians from heading (left/right)
    public static double TURN_STRENGTH = 0.4;         // max turn toward sensed target
    public static double WANDER_STRENGTH = 0.22;       // random jitter each step
    public static double FOOD_SENSE_BONUS = 12.0;     // attraction when food is in sight
    public static double NEST_SENSE_BONUS = 12.0;     // attraction when nest is in sight

    // Pheromones
    public static double PHEROMONE_DEPOSIT_AMOUNT = 0.9;
    // Total pheromone a returning ant lays along its trip; divided by the trip
    // length so shorter (optimal) paths are reinforced more strongly.
    public static double PHEROMONE_DEPOSIT_BUDGET = 60.0;
    public static double PHEROMONE_EVAPORATION_RATE = 0.96;
    public static double MAX_PHEROMONE_LEVEL = 80.0;

    // The nest constantly emits a "home" gradient so returning ants can find their way back
    public static double NEST_HOME_EMIT_RADIUS = 26.0;
    public static double NEST_HOME_EMIT_STRENGTH = 1.5;

    // Natural obstacles: organic rock clusters that block some routes so ants
    // must discover the best path around them.
    // Mutable: tunable from the UI (applied on "Nuovo mondo")
    public static int OBSTACLE_CLUSTERS = 16;
    public static final double OBSTACLE_MIN_RADIUS = 6.0;
    public static final double OBSTACLE_MAX_RADIUS = 13.0;
    public static final double OBSTACLE_CLEAR_RADIUS = 18.0; // keep clear around nest & food
    public static final double OBSTACLE_EDGE_MARGIN = 8.0;

    public static final boolean ENABLE_CLI_LOGS = false;

    private SimulationConfig() {
    }
}

```

### `src/main/java/it/unibo/antsim/config/ViewConfig.java`

```java
package it.unibo.antsim.config;

import it.unibo.antsim.view.PheromoneViewMode;

public final class ViewConfig {
    public static final int SCENE_WIDTH = 900;
    public static final int SCENE_HEIGHT = 740;
    public static final int VIEW_WIDTH = 760;
    public static final int VIEW_HEIGHT = 700;
    public static final boolean SHOW_GRID = false;
    public static final PheromoneViewMode PHEROMONE_VIEW_MODE = PheromoneViewMode.BOTH;
    public static final double PHEROMONE_SOFT_RADIUS_MULTIPLIER = 0.95;

    private ViewConfig() {
    }
}

```

### `src/main/java/it/unibo/antsim/controller/SimulationController.java`

```java
package it.unibo.antsim.controller;

import it.unibo.antsim.simulation.SimulationEngine;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * SimulationController is responsible for controlling the simulation flow, including starting, stopping,
 */
public class SimulationController {
    private final SimulationEngine engine;
    private Timeline timeline;

    public SimulationController(SimulationEngine engine) {
        this.engine = engine;
        setupTimeline();
    }

    private void setupTimeline() {
        timeline = new Timeline(
                new KeyFrame(Duration.millis(100), e -> engine.step())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void start() {
        engine.start();
        timeline.play();
    }

    public void resume() {
        engine.resume();
        timeline.play();
    }

    public void stop() {
        engine.stop();
        timeline.stop();
    }

    public void pause() {
        engine.pause();
        timeline.pause();
    }

    public void reset(int agentCount) {
        stop();
        engine.reset(agentCount);
    }

}

```

### `src/main/java/it/unibo/antsim/model/agent/Ant.java`

```java
package it.unibo.antsim.model.agent;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.model.environment.Environment;

import java.util.Random;

public class Ant {
    private static int nextId = 1;
    private static final Random RANDOM = new Random();

    private final int id;
    private double x;
    private double y;
    private double heading;
    private AntState state;
    private int tripSteps;

    public Ant(int cellX, int cellY) {
        this.id = nextId++;
        this.x = cellX + 0.5;
        this.y = cellY + 0.5;
        this.heading = randomAngle();
        this.state = AntState.SEARCHING_FOOD;
    }

    public void move(Environment env) {
        tripSteps++;

        // Deposit the trail that the other phase follows:
        //  - searching ants lay a "home" trail (so carriers can return)
        //  - returning ants lay a "food" trail (so searchers can find food)
        if (state == AntState.RETURNING_TO_NEST) {
            // ACO reinforcement: shorter foraging trips deposit more pheromone
            // per step, so the shortest nest->food path is strengthened most
            // and emerges as the optimal trail.
            env.depositFoodPheromoneAt(x, y, SimulationConfig.PHEROMONE_DEPOSIT_BUDGET / tripSteps);
        } else {
            env.depositHomePheromoneAt(x, y, SimulationConfig.PHEROMONE_DEPOSIT_AMOUNT);
        }

        steer(env);

        double nx = x + Math.cos(heading) * SimulationConfig.ANT_SPEED;
        double ny = y + Math.sin(heading) * SimulationConfig.ANT_SPEED;

        if (env.isBlockedAt(nx, ny)) {
            // Blocked: turn away and stay put this step.
            heading += Math.PI * (0.5 + RANDOM.nextDouble());
        } else {
            x = nx;
            y = ny;
        }
    }

    private void steer(Environment env) {
        double leftAngle = heading - SimulationConfig.SENSOR_ANGLE;
        double rightAngle = heading + SimulationConfig.SENSOR_ANGLE;

        double center = sense(env, heading);
        double left = sense(env, leftAngle);
        double right = sense(env, rightAngle);

        double best = Math.max(center, Math.max(left, right));

        if (best > 0) {
            double target;
            if (best == center) {
                target = heading;
            } else if (best == left) {
                target = leftAngle;
            } else {
                target = rightAngle;
            }
            turnToward(target);
        }

        // Random wander keeps exploration alive and breaks symmetry.
        heading += (RANDOM.nextDouble() * 2.0 - 1.0) * SimulationConfig.WANDER_STRENGTH;
    }

    private double sense(Environment env, double angle) {
        double sx = x + Math.cos(angle) * SimulationConfig.SENSOR_DISTANCE;
        double sy = y + Math.sin(angle) * SimulationConfig.SENSOR_DISTANCE;

        if (state == AntState.SEARCHING_FOOD) {
            double value = env.getFoodPheromoneAt(sx, sy);
            if (env.isFoodAt(sx, sy)) value += SimulationConfig.FOOD_SENSE_BONUS;
            return value;
        } else {
            double value = env.getHomePheromoneAt(sx, sy);
            if (env.isNestAt(sx, sy)) value += SimulationConfig.NEST_SENSE_BONUS;
            return value;
        }
    }

    private void turnToward(double targetAngle) {
        double diff = normalizeAngle(targetAngle - heading);
        diff = Math.max(-SimulationConfig.TURN_STRENGTH, Math.min(SimulationConfig.TURN_STRENGTH, diff));
        heading = normalizeAngle(heading + diff);
    }

    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    private double randomAngle() {
        return RANDOM.nextDouble() * 2 * Math.PI;
    }

    public void pickFood() {
        state = AntState.RETURNING_TO_NEST;
        tripSteps = 0;
    }

    public void dropFood() {
        state = AntState.SEARCHING_FOOD;
    }

    public boolean isCarryingFood() {
        return state == AntState.RETURNING_TO_NEST;
    }

    public AntState getState() {
        return state;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return (int) Math.floor(x);
    }

    public int getY() {
        return (int) Math.floor(y);
    }

    public double getDoubleX() {
        return x;
    }

    public double getDoubleY() {
        return y;
    }

    public double getHeading() {
        return heading;
    }
}

```

### `src/main/java/it/unibo/antsim/model/agent/AntState.java`

```java
package it.unibo.antsim.model.agent;

public enum AntState {
    SEARCHING_FOOD,
    RETURNING_TO_NEST
}

```

### `src/main/java/it/unibo/antsim/model/environment/Cell.java`

```java
package it.unibo.antsim.model.environment;

import it.unibo.antsim.config.SimulationConfig;

/**
 * Class representing a cell in the grid.
 * Each cell has a type (EMPTY, FOOD, OBSTACLE, NEST) and a pheromone level.
 */
public class Cell {
    private CellType type;
    private double pheromoneLevel;
    private double homePheromoneLevel;
    private int foodHP = 0;

    public Cell(CellType type) {
        this.type = type;
        this.pheromoneLevel = 0.0;
        this.homePheromoneLevel = 0.0;
    }

    public CellType getType(){

        return type;
    }

    public void setType(CellType type){

        this.type = type;
    }

    public double getPheromoneLevel() {

        return pheromoneLevel;
    }

    public double getHomePheromoneLevel() {

        return homePheromoneLevel;
    }

    public void addPheromoneLevel(double value){

        this.pheromoneLevel = Math.min(this.pheromoneLevel + value, SimulationConfig.MAX_PHEROMONE_LEVEL);
    }

    public void addHomePheromoneLevel(double value){

        this.homePheromoneLevel = Math.min(
                Math.max(this.homePheromoneLevel, value),
                SimulationConfig.MAX_PHEROMONE_LEVEL
        );
    }

    public void evaporate(double rate){

        this.pheromoneLevel *= rate;
        this.homePheromoneLevel *= rate;
        if(this.pheromoneLevel < 0.0){
            this.pheromoneLevel = 0.0;
        }
        if(this.homePheromoneLevel < 0.0){
            this.homePheromoneLevel = 0.0;
        }
    }

    public boolean hasFood(){
        return type == CellType.FOOD;
    }

    public int getFoodHP(){
        return foodHP;
    }

    public void setFoodHP(int foodHP){
        this.foodHP = Math.max(0, foodHP);
        if(this.foodHP == 0){
            setType(CellType.EMPTY);
        }
    }

    public void consumeFood(int amount){
        foodHP -= amount;
        if(foodHP <= 0){
            foodHP = 0;
            setType(CellType.EMPTY);
        }
    }

    public boolean isObstacle(){
        return type == CellType.OBSTACLE;
    }

    public boolean isNest(){
        return type == CellType.NEST;
    }
}

```

### `src/main/java/it/unibo/antsim/model/environment/CellType.java`

```java
package it.unibo.antsim.model.environment;

/**
 * Enum representing the type of a cell in the grid.
 * It can be EMPTY, FOOD, OBSTACLE, or NEST.
 */
public enum CellType {
    EMPTY,
    FOOD,
    OBSTACLE,
    NEST
}

```

### `src/main/java/it/unibo/antsim/model/environment/Environment.java`

```java
package it.unibo.antsim.model.environment;

import it.unibo.antsim.config.SimulationConfig;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class representing the environment of the ant simulation.
 * It contains a grid of cells and provides methods to update the environment.
 */
public class Environment {
    private Position nestPosition;
    private final Grid grid;
    private static final Random RANDOM = new Random();
    private static final int DEFAULT_FOOD_HP = 100;

    public Environment(int width, int height) {
        this.grid = new Grid(width, height);
        setNestPosition(new Position(0, 0));
        log("created grid=%dx%d nest=%s", width, height, nestPosition);
    }

    public void setNestPosition(Position position){
        if(!grid.isInside(position.x(), position.y())){
            throw new IllegalArgumentException("Nest position is out of bounds, it must be inside the grid!");
        }

        if(nestPosition != null && grid.isInside(nestPosition.x(), nestPosition.y())){
            grid.getCell(nestPosition.x(), nestPosition.y()).setType(CellType.EMPTY);
        }

        nestPosition = position;
        grid.getCell(position.x(), position.y()).setType(CellType.NEST);
        log("nest set at %s", position);
    }

    public Position getNestPosition(){
        return nestPosition;
    }
    public Grid getGrid() {
        return grid;
    }

    public Cell getCell(int x, int y) {
        return grid.getCell(x, y);
    }

    public List<Position> getWalkableNeighborPositions(int x, int y) {
        List<Position> positions = new ArrayList<>();

        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, -1, 0, 1};

        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (grid.isInside(newX, newY) && !grid.getCell(newX, newY).isObstacle()) {
                positions.add(new Position(newX, newY));
            }
        }

        return positions;
    }

    /**
        * Returns a list of neighboring cells (up, down, left, right) for the given coordinates.
     */
    public List<Cell> getNeighbors(int x, int y) {
        List<Cell> neighbors = new ArrayList<>();

        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, -1, 0, 1};

        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (grid.isInside(newX, newY)) {
                neighbors.add(grid.getCell(newX, newY));
            }
        }
        return neighbors;
    }

    public void update() {
        // Evaporate pheromones in all cells
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                grid.getCell(x, y).evaporate(SimulationConfig.PHEROMONE_EVAPORATION_RATE); // Evaporation rate
            }
        }
        emitHomeGradient();
        log("evaporated pheromones rate=%.2f foodTrailTotal=%.2f homeTrailTotal=%.2f",
                SimulationConfig.PHEROMONE_EVAPORATION_RATE,
                totalFoodPheromone(),
                totalHomePheromone());
    }

    /**
     * The nest constantly emits a "home" pheromone gradient so that ants carrying
     * food can always sense their way back, even far from any trail.
     */
    private void emitHomeGradient() {
        int nx = nestPosition.x();
        int ny = nestPosition.y();
        int r = (int) SimulationConfig.NEST_HOME_EMIT_RADIUS;

        for (int x = Math.max(0, nx - r); x <= Math.min(grid.getWidth() - 1, nx + r); x++) {
            for (int y = Math.max(0, ny - r); y <= Math.min(grid.getHeight() - 1, ny + r); y++) {
                double dist = Math.hypot(x - nx, y - ny);
                if (dist > r) continue;
                double strength = SimulationConfig.NEST_HOME_EMIT_STRENGTH * (1.0 - dist / r);
                grid.getCell(x, y).addHomePheromoneLevel(strength);
            }
        }
    }

    public void generateFood(int foodCount) {
        int attempts = 0;
        int maxAttempts = foodCount * 10;
        int generated = 0;

        for(; generated < foodCount && attempts < maxAttempts; attempts++){
            int x = RANDOM.nextInt(grid.getWidth());
            int y = RANDOM.nextInt(grid.getHeight());

            Cell cell = grid.getCell(x, y);
            if (cell.getType() == CellType.EMPTY) {
                cell.setType(CellType.FOOD);
                cell.setFoodHP(DEFAULT_FOOD_HP);
                log("food generated at (%d,%d) hp=%d attempt=%d", x, y, DEFAULT_FOOD_HP, attempts + 1);
                generated++;
            } else {
                log("food generation skipped at (%d,%d) type=%s attempt=%d", x, y, cell.getType(), attempts + 1);
            }
        }
        log("generateFood requested=%d generated=%d attempts=%d totalFoodHP=%d",
                foodCount, generated, attempts, getTotalFoodHP());
    }

    public void consumeFood(int x, int y, int amount) {
        int before = grid.getCell(x, y).getFoodHP();
        grid.getCell(x, y).consumeFood(amount);
        int after = grid.getCell(x, y).getFoodHP();
        log("food consumed at (%d,%d) amount=%d hp=%d->%d type=%s",
                x, y, amount, before, after, grid.getCell(x, y).getType());
    }

    public int countCellsOfType(CellType type) {
        int count = 0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                if (grid.getCell(x, y).getType() == type) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getTotalFoodHP() {
        int totalFoodHP = 0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                if (cell.hasFood()) {
                    totalFoodHP += cell.getFoodHP();
                }
            }
        }
        return totalFoodHP;
    }

    public void generateObstacle(int obstacleCount) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int totalCells = width * height;

        // non superare il 30% della griglia con ostacoli (configurabile)
        int maxObstaclesAllowed = (int) (totalCells * 0.30);
        int currentObstacles = countCellsOfType(CellType.OBSTACLE);
        int canAdd = Math.max(0, maxObstaclesAllowed - currentObstacles);
        int toGenerate = Math.min(obstacleCount, canAdd);
        if (toGenerate <= 0) {
            // niente da fare
            log("generateObstacle requested=%d added=0 current=%d maxAllowed=%d",
                    obstacleCount, currentObstacles, maxObstaclesAllowed);
            return;
        }

        int generated = 0;
        int attempts = 0;
        int maxAttempts = toGenerate * 20; // più tentativi per trovare celle libere

        while (generated < toGenerate && attempts < maxAttempts) {
            int x = RANDOM.nextInt(width);
            int y = RANDOM.nextInt(height);
            attempts++;
            Cell cell = grid.getCell(x, y);
            if (cell.getType() == CellType.EMPTY && !cell.isNest()) {
                cell.setType(CellType.OBSTACLE);
                generated++;
                log("obstacle generated at (%d,%d) attempt=%d", x, y, attempts);
            } else {
                log("obstacle skipped at (%d,%d) type=%s attempt=%d", x, y, cell.getType(), attempts);
            }
        }

        log("generateObstacle requested=%d added=%d current=%d",
                obstacleCount, generated, currentObstacles + generated);
    }

    public void resetObstacles(int obstacleCount) {
        log("resetObstacles requested=%d", obstacleCount);
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                if (cell.getType() == CellType.OBSTACLE) {
                    cell.setType(CellType.EMPTY);
                    log("obstacle cleared at (%d,%d)", x, y);
                }
            }
        }
        generateObstacle(obstacleCount);
    }

    /**
     * Generates a single, dense cluster of food with a large amount of resources.
     */
    public void generateFoodCluster(int cx, int cy, double radius, int hpPerCell) {
        int r = (int) Math.ceil(radius);
        int generated = 0;

        for (int x = cx - r; x <= cx + r; x++) {
            for (int y = cy - r; y <= cy + r; y++) {
                if (!grid.isInside(x, y)) continue;
                if (Math.hypot(x - cx, y - cy) > radius) continue;
                Cell cell = grid.getCell(x, y);
                if (cell.isNest() || cell.isObstacle()) continue;
                cell.setType(CellType.FOOD);
                cell.setFoodHP(hpPerCell);
                generated++;
            }
        }
        log("food cluster generated center=(%d,%d) radius=%.1f cells=%d hp=%d",
                cx, cy, radius, generated, hpPerCell);
    }

    /**
     * Places a single dense food cluster at a random location that keeps a safe
     * distance from the nest and avoids existing rock cells, so every reset
     * produces a fresh world layout (the food no longer stays in a fixed spot).
     */
    public void generateRandomFoodCluster() {
        int margin = (int) Math.ceil(SimulationConfig.FOOD_CLUSTER_RADIUS) + 2;
        int minDistFromNest = (int) SimulationConfig.OBSTACLE_CLEAR_RADIUS;

        int cx = -1;
        int cy = -1;
        for (int attempts = 0; attempts < 200; attempts++) {
            int x = margin + RANDOM.nextInt(Math.max(1, grid.getWidth() - 2 * margin));
            int y = margin + RANDOM.nextInt(Math.max(1, grid.getHeight() - 2 * margin));

            if (Math.hypot(x - nestPosition.x(), y - nestPosition.y()) < minDistFromNest) continue;
            if (grid.getCell(x, y).isObstacle()) continue;

            cx = x;
            cy = y;
            break;
        }
        if (cx < 0) {
            cx = grid.getWidth() / 2;
            cy = grid.getHeight() / 2;
        }

        generateFoodCluster(cx, cy, SimulationConfig.FOOD_CLUSTER_RADIUS, SimulationConfig.FOOD_CLUSTER_HP);
    }

    /**
     * Scatters organic rock clusters across the world. They block some routes
     * between the nest and the food so the colony must discover the best path.
     */
    public void generateRockClusters() {
        int placed = 0;
        int attempts = 0;
        int maxAttempts = SimulationConfig.OBSTACLE_CLUSTERS * 40;

        while (placed < SimulationConfig.OBSTACLE_CLUSTERS && attempts < maxAttempts) {
            attempts++;
            int cx = (int) SimulationConfig.OBSTACLE_EDGE_MARGIN
                    + RANDOM.nextInt(grid.getWidth() - (int) (2 * SimulationConfig.OBSTACLE_EDGE_MARGIN));
            int cy = (int) SimulationConfig.OBSTACLE_EDGE_MARGIN
                    + RANDOM.nextInt(grid.getHeight() - (int) (2 * SimulationConfig.OBSTACLE_EDGE_MARGIN));
            double radius = SimulationConfig.OBSTACLE_MIN_RADIUS
                    + RANDOM.nextDouble() * (SimulationConfig.OBSTACLE_MAX_RADIUS - SimulationConfig.OBSTACLE_MIN_RADIUS);

            if (Math.hypot(cx - nestPosition.x(), cy - nestPosition.y()) < SimulationConfig.OBSTACLE_CLEAR_RADIUS) continue;

            int r = (int) Math.ceil(radius);
            int cells = 0;
            for (int x = cx - r; x <= cx + r; x++) {
                for (int y = cy - r; y <= cy + r; y++) {
                    if (!grid.isInside(x, y)) continue;
                    if (Math.hypot(x - cx, y - cy) > radius) continue;
                    Cell cell = grid.getCell(x, y);
                    if (cell.isNest() || cell.hasFood()) continue;
                    cell.setType(CellType.OBSTACLE);
                    cells++;
                }
            }
            placed += (cells > 0 ? 1 : 0);
        }
        log("rock clusters generated=%d", placed);
    }

    public void resetDynamicElements(int obstacleCount, int foodCount) {
        log("resetDynamicElements obstacleCount=%d foodCount=%d", obstacleCount, foodCount);
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                cell.evaporate(0.0);

                if (!cell.isNest()) {
                    cell.setType(CellType.EMPTY);
                    cell.setFoodHP(0);
                }
            }
        }

        generateRockClusters();
        generateRandomFoodCluster();
    }

    public boolean isNest(int x, int y) {
        return grid.getCell(x, y).isNest();
    }

    // --- Continuous-space sampling (used by sensor-based ant movement) ---

    public boolean isObstacleAt(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (!grid.isInside(cx, cy)) return true; // out of bounds acts as a wall
        return grid.getCell(cx, cy).isObstacle();
    }

    /**
     * A position ants cannot enter: out of bounds, an obstacle, or a food cell.
     * Food blocks movement exactly like an obstacle (ants route around it and
     * pick it up from an adjacent cell instead of standing on top of it).
     */
    public boolean isBlockedAt(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (!grid.isInside(cx, cy)) return true;
        Cell cell = grid.getCell(cx, cy);
        return cell.isObstacle() || cell.hasFood();
    }

    /**
     * Returns the coordinates of a food cell the given cell touches (itself or a
     * 4-neighbor), or null when no food is adjacent. Used so ants collect food
     * from the perimeter of a food pile without ever stepping onto it.
     */
    public Position findFoodCellNear(int x, int y) {
        if (grid.isInside(x, y) && grid.getCell(x, y).hasFood()) {
            return new Position(x, y);
        }
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, -1, 0, 1};
        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (grid.isInside(nx, ny) && grid.getCell(nx, ny).hasFood()) {
                return new Position(nx, ny);
            }
        }
        return null;
    }

    public boolean isFoodAt(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        return grid.isInside(cx, cy) && grid.getCell(cx, cy).hasFood();
    }

    public boolean isNestAt(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        return grid.isInside(cx, cy) && grid.getCell(cx, cy).isNest();
    }

    public double getFoodPheromoneAt(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (!grid.isInside(cx, cy)) return 0.0;
        return grid.getCell(cx, cy).getPheromoneLevel();
    }

    public double getHomePheromoneAt(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (!grid.isInside(cx, cy)) return 0.0;
        return grid.getCell(cx, cy).getHomePheromoneLevel();
    }

    public void depositFoodPheromoneAt(double x, double y, double value) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (grid.isInside(cx, cy)) grid.getCell(cx, cy).addPheromoneLevel(value);
    }

    public void depositHomePheromoneAt(double x, double y, double value) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (grid.isInside(cx, cy)) grid.getCell(cx, cy).addHomePheromoneLevel(value);
    }

    private double totalFoodPheromone() {
        double total = 0.0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                total += grid.getCell(x, y).getPheromoneLevel();
            }
        }
        return total;
    }

    private double totalHomePheromone() {
        double total = 0.0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                total += grid.getCell(x, y).getHomePheromoneLevel();
            }
        }
        return total;
    }

    private void log(String format, Object... args) {
        if (SimulationConfig.ENABLE_CLI_LOGS) {
            System.out.printf("[ENV] %s%n", String.format(format, args));
        }
    }
}

```

### `src/main/java/it/unibo/antsim/model/environment/Grid.java`

```java
package it.unibo.antsim.model.environment;

/**
 * Class representing the grid of the environment.
 * The grid is a 2D array of cells, where each cell can be empty, contain food, be an obstacle, or be the nest.
 * The grid provides methods to access and modify the cells, as well as to get the dimensions of the grid.
 */
public class Grid {
    private final int width;
    private final int height;
    private final Cell[][] cells;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];

        initializeCells();
    }

    private void initializeCells() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(CellType.EMPTY);
            }
        }
    }

    public Cell getCell(int x, int y) {

        return cells[x][y];
    }

    public int getWidth() {

        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Checks if the given coordinates are within the bounds of the grid.
     */
    public boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}

```

### `src/main/java/it/unibo/antsim/model/environment/Position.java`

```java
package it.unibo.antsim.model.environment;

public record Position(int x, int y){
    public int manhattanDistanceFrom(Position other){
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }
}


```

### `src/main/java/it/unibo/antsim/model/SimulationState.java`

```java
package it.unibo.antsim.model;

public class SimulationState {
    private long stepCount;
    private int foodPicked;
    private int foodAtNest;
    private int agentCount;

    public SimulationState() {
        this.stepCount = 0;
        this.foodPicked = 0;
        this.foodAtNest = 0;
        this.agentCount = 0;
    }

    /**
     * Getters
     */
    public long getStepCount() {
        return stepCount;
    }

    public int  getFoodPicked() {
        return foodPicked;
    }

    public int getFoodAtNest() {
        return foodAtNest;
    }

    public int getAgentCount() {
        return agentCount;
    }

    /**
     * Setters || mutators
     */
    public void setStepCount(long stepCount) {
        this.stepCount = stepCount;
    }

    public void incrementFoodPicked() {
        this.foodPicked++;
    }

    public void incrementFoodAtNest() {
        this.foodAtNest++;
    }

    public void setAgentCount(int agentCount) {
        this.agentCount = agentCount;
    }

    public void reset() {
        this.stepCount = 0;
        this.foodPicked = 0;
        this.foodAtNest = 0;
    }

    @Override
    public String toString() {
        return "SimulationState{" +
                "step=" + stepCount +
                ", foodPicked=" + foodPicked +
                ", foodAtNest=" + foodAtNest +
                ", agents=" + agentCount +
                '}';
    }
}

```

### `src/main/java/it/unibo/antsim/simulation/SimulationEngine.java`

```java
package it.unibo.antsim.simulation;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.SimulationState;
import it.unibo.antsim.model.agent.Ant;
import it.unibo.antsim.model.environment.Cell;
import it.unibo.antsim.model.environment.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SimulationEngine is responsible for managing the simulation loop, updating the environment
 * and handling interactions between entities.
 */
public class SimulationEngine {
    private final Environment environment;
    private final List<Ant> ants = new ArrayList<>();
    private final SimulationState state =  new SimulationState();
    private SimulationStatus status = SimulationStatus.STOPPED;
    private int foodGenerationInterval = 100;
    private static final int FOOD_CONSUMPTION_PER_AGENT = 5;
    private long stepCount = 0;

    public SimulationEngine(Environment environment) {
        this.environment = environment;
        log("created");
    }

    public void start() {
        if (status == SimulationStatus.RUNNING) return;
        this.status = SimulationStatus.RUNNING;
        log("start");
    }

    public void pause() {
        if (status == SimulationStatus.RUNNING) {
            this.status = SimulationStatus.PAUSED;
            log("pause at step=%d", stepCount);
        }
    }

    public void resume() {
        if (status == SimulationStatus.PAUSED) {
            this.status = SimulationStatus.RUNNING;
            log("resume at step=%d", stepCount);
        }
    }

    public void stop() {
        this.status = SimulationStatus.STOPPED;
        log("stop at step=%d", stepCount);
    }

    public void reset(int agentCount) {
        this.stepCount = 0;
        this.status = SimulationStatus.STOPPED;
        log("reset agentCount=%d", agentCount);

        state.reset();
        ants.clear();

        // Regenerate the world so a reset truly restarts the simulation.
        environment.resetDynamicElements(0, 0);

        Position nest = environment.getNestPosition();
        for(int i = 0;i < agentCount; i++){
            addAnt(new Ant(nest.x(), nest.y()));
        }

        state.setAgentCount(ants.size());
    }

    /**
     * Regenerates obstacles and food without stopping the simulation, re-spawning
     * the colony at the nest. Lets the user roll a fresh world from the UI
     * (the "Nuovo mondo" button) while keeping the current run state.
     */
    public void regenerateWorld() {
        environment.resetDynamicElements(0, 0);
        ants.clear();

        Position nest = environment.getNestPosition();
        for (int i = 0; i < SimulationConfig.INITIAL_AGENT_COUNT; i++) {
            addAnt(new Ant(nest.x(), nest.y()));
        }
        state.setAgentCount(ants.size());
        log("regenerateWorld agents=%d", ants.size());
    }

    public void step() {
        if (status != SimulationStatus.RUNNING) return;

        log("step=%d begin ants=%d foodHP=%d", stepCount, ants.size(), environment.getTotalFoodHP());

        if (SimulationConfig.ENABLE_FOOD_REGEN
                && stepCount % foodGenerationInterval == 0 && stepCount > 0) {
            log("step=%d periodic food generation", stepCount);
            environment.generateFood(1);
        }

        updateAgents();
        updateEnvironment();
        stepCount++;
        handleInteractions();
        log("step=%d end foodPicked=%d foodAtNest=%d foodHP=%d",
                stepCount,
                state.getFoodPicked(),
                state.getFoodAtNest(),
                environment.getTotalFoodHP());
    }

    public void setFoodGenerationInterval(int foodGenerationInterval) {
        this.foodGenerationInterval = foodGenerationInterval;
        log("foodGenerationInterval=%d", foodGenerationInterval);
    }

    public void updateEnvironment() {
        environment.update();
    }

    public void addAnt(Ant ant) {
        ants.add(ant);
        log("ant added id=%02d position=(%d,%d)", ant.getId(), ant.getX(), ant.getY());
    }

    /**
     * Live-adjusts the colony size by adding new ants at the nest or removing
     * the most recently added ones. Lets the user change population from the UI
     * without restarting the simulation.
     */
    public void setAgentCount(int desired) {
        Position nest = environment.getNestPosition();
        while (ants.size() < desired) {
            addAnt(new Ant(nest.x(), nest.y()));
        }
        while (ants.size() > desired && !ants.isEmpty()) {
            ants.remove(ants.size() - 1);
        }
        state.setAgentCount(ants.size());
    }

    public void updateAgents() {
        log("updateAgents count=%d", ants.size());
        for (Ant ant : ants) {
            ant.move(environment);
        }
    }

    public void handleInteractions() {
        // food consumption (consumazione cibo)
        // global states update (aggiornamento stati globali)
        // interaction between ants (interazione tra formiche)
        for (Ant ant : ants) {
            if (!ant.isCarryingFood()) {
                // Food blocks movement, so ants collect it from an adjacent cell.
                Position food = environment.findFoodCellNear(ant.getX(), ant.getY());
                if (food != null) {
                    ant.pickFood();

                    int nearbyAnts = countAntsNearFood(food.x(), food.y());
                    int consumeAmount = FOOD_CONSUMPTION_PER_AGENT * nearbyAnts;

                    environment.consumeFood(food.x(), food.y(), consumeAmount);
                    state.incrementFoodPicked();
                    log("ant=%02d picked food at (%d,%d) nearbyAnts=%d consumeAmount=%d",
                            ant.getId(), food.x(), food.y(), nearbyAnts, consumeAmount);
                }
            }

            if (ant.isCarryingFood() && environment.isNest(ant.getX(), ant.getY())) {
                ant.dropFood();
                state.incrementFoodAtNest();
                log("ant=%02d delivered food to nest at (%d,%d)",
                        ant.getId(), ant.getX(), ant.getY());
            }
        }
        state.setStepCount(stepCount);
        state.setAgentCount(ants.size());
    }

    private int countAntsNearFood(int x, int y) {
        int count = 0;
        List<Cell> neighbors = environment.getNeighbors(x, y);

        for (Ant ant : ants) {
            if ((ant.getX() == x && ant.getY() == y)
                    || neighbors.contains(environment.getCell(ant.getX(), ant.getY()))) {
                count++;
            }
        }
        return count;
    }

    public List<Ant> getAnts() {
        return Collections.unmodifiableList(ants);
    }

    public SimulationState getState() {
        return state;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public SimulationStatus getStatus() {
        return status;
    }

    private void log(String format, Object... args) {
        if (SimulationConfig.ENABLE_CLI_LOGS) {
            System.out.printf("[ENGINE] %s%n", String.format(format, args));
        }
    }
}

```

### `src/main/java/it/unibo/antsim/simulation/SimulationStatus.java`

```java
package it.unibo.antsim.simulation;

public enum SimulationStatus {
    STOPPED,
    RUNNING,
    PAUSED
}

```

### `src/main/java/it/unibo/antsim/view/Camera.java`

```java
package it.unibo.antsim.view;

/**
 * 2D camera that maps world coordinates (in cells) to screen pixels.
 * Supports zoom (toward the cursor) and panning by drag.
 */
public class Camera {
    private double centerX;
    private double centerY;
    private double zoom; // pixels per cell

    private final double worldWidth;
    private final double worldHeight;
    private double viewWidth;
    private double viewHeight;

    public Camera(double worldWidth, double worldHeight, double viewWidth, double viewHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.centerX = worldWidth / 2.0;
        this.centerY = worldHeight / 2.0;
        fit();
    }

    /**
     * Updates the viewport size (in screen pixels) and re-fits the camera so the
     * world keeps filling the available area. Called whenever the view is resized.
     */
    public void setViewSize(double viewWidth, double viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        fit();
    }

    public void fit() {
        // "cover": scale so the world always fills the whole view (no letterbox
        // bands). A giant world is then explored by zooming/panning.
        double fit = Math.max(viewWidth / worldWidth, viewHeight / worldHeight);
        this.zoom = fit;
        this.centerX = worldWidth / 2.0;
        this.centerY = worldHeight / 2.0;
    }

    public double screenToWorldX(double screenX) {
        return (screenX - viewWidth / 2.0) / zoom + centerX;
    }

    public double screenToWorldY(double screenY) {
        return (screenY - viewHeight / 2.0) / zoom + centerY;
    }

    public double worldToScreenX(double worldX) {
        return (worldX - centerX) * zoom + viewWidth / 2.0;
    }

    public double worldToScreenY(double worldY) {
        return (worldY - centerY) * zoom + viewHeight / 2.0;
    }

    public void zoomAt(double screenX, double screenY, double factor) {
        double beforeX = screenToWorldX(screenX);
        double beforeY = screenToWorldY(screenY);

        zoom = clampZoom(zoom * factor);

        double afterX = screenToWorldX(screenX);
        double afterY = screenToWorldY(screenY);

        centerX += beforeX - afterX;
        centerY += beforeY - afterY;
    }

    public void pan(double dxScreen, double dyScreen) {
        centerX -= dxScreen / zoom;
        centerY -= dyScreen / zoom;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getZoom() {
        return zoom;
    }

    private double clampZoom(double z) {
        double min = Math.min(viewWidth / worldWidth, viewHeight / worldHeight) * 0.4;
        double max = 12.0;
        return Math.max(min, Math.min(max, z));
    }
}

```

### `src/main/java/it/unibo/antsim/view/ControlPanel.java`

```java
package it.unibo.antsim.view;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.controller.SimulationController;
import it.unibo.antsim.simulation.SimulationEngine;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.DoubleConsumer;

public class ControlPanel extends ScrollPane {
    private final SimulationController controller;
    private final SimulationEngine engine;
    private final SimulationView view;
    private final StatsPanel statsPanel;
    private final Button startBtn;
    private final Button pauseBtn;
    private final Button resumeBtn;
    private final Button resetBtn;
    private final Button newWorldBtn;
    private final VBox content = new VBox(10);

    public ControlPanel(
            SimulationController controller,
            SimulationEngine engine,
            SimulationView view,
            StatsPanel statsPanel
    ) {
        super();
        this.controller = controller;
        this.engine = engine;
        this.view = view;
        this.statsPanel = statsPanel;

        setContent(content);
        setFitToWidth(true);
        setPadding(new Insets(12));
        setPrefWidth(220);
        getStyleClass().add("control-panel");

        Label title = new Label("CONTROLS");
        title.getStyleClass().add("panel-title");

        startBtn = createButton("Start");
        pauseBtn = createButton("Pause");
        resumeBtn = createButton("Resume");
        resetBtn = createButton("Reset");
        newWorldBtn = createButton("Nuovo mondo");

        startBtn.setOnAction(e -> {
            controller.start();
            updateControls();
        });

        pauseBtn.setOnAction(e -> {
            controller.pause();
            updateControls();
        });

        resumeBtn.setOnAction(e -> {
            controller.resume();
            updateControls();
        });

        resetBtn.setOnAction(e -> {
            controller.reset(SimulationConfig.INITIAL_AGENT_COUNT);
            view.resetCamera();
            view.render(engine.getAnts());
            statsPanel.update(engine);
            updateControls();
        });

        newWorldBtn.setOnAction(e -> {
            engine.regenerateWorld();
            view.resetCamera();
            view.render(engine.getAnts());
            statsPanel.update(engine);
        });


        content.getChildren().addAll(
                title,
                new Separator(),
                startBtn,
                pauseBtn,
                resumeBtn,
                resetBtn,
                newWorldBtn,
                new Separator()
        );

        addParamSliders();
        updateControls();
    }

    public void updateControls() {
        switch (engine.getStatus()) {
            case STOPPED:
                startBtn.setDisable(false);
                pauseBtn.setDisable(true);
                resumeBtn.setDisable(true);
                resetBtn.setDisable(false);
                break;
            case RUNNING:
                startBtn.setDisable(true);
                pauseBtn.setDisable(false);
                resumeBtn.setDisable(true);
                resetBtn.setDisable(false);
                break;
            case PAUSED:
                startBtn.setDisable(true);
                pauseBtn.setDisable(true);
                resumeBtn.setDisable(false);
                resetBtn.setDisable(false);
                break;
        }
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("control-button");
        return button;
    }

    private void addParamSliders() {
        addSection("MOVIDENTO");
        addParamSlider("Velocità", 0.1, 3.0, SimulationConfig.ANT_SPEED,
                v -> SimulationConfig.ANT_SPEED = v, false);
        addParamSlider("Distanza sensori", 1, 15, SimulationConfig.SENSOR_DISTANCE,
                v -> SimulationConfig.SENSOR_DISTANCE = v, false);
        addParamSlider("Angolo sensori", 0.1, 1.5, SimulationConfig.SENSOR_ANGLE,
                v -> SimulationConfig.SENSOR_ANGLE = v, false);
        addParamSlider("Rotazione max", 0.05, 1.0, SimulationConfig.TURN_STRENGTH,
                v -> SimulationConfig.TURN_STRENGTH = v, false);
        addParamSlider("Erraticità", 0.0, 0.6, SimulationConfig.WANDER_STRENGTH,
                v -> SimulationConfig.WANDER_STRENGTH = v, false);

        addSection("FEROMONI");
        addParamSlider("Attraz. cibo", 0, 30, SimulationConfig.FOOD_SENSE_BONUS,
                v -> SimulationConfig.FOOD_SENSE_BONUS = v, false);
        addParamSlider("Attraz. nido", 0, 30, SimulationConfig.NEST_SENSE_BONUS,
                v -> SimulationConfig.NEST_SENSE_BONUS = v, false);
        addParamSlider("Deposito fero.", 0.1, 3.0, SimulationConfig.PHEROMONE_DEPOSIT_AMOUNT,
                v -> SimulationConfig.PHEROMONE_DEPOSIT_AMOUNT = v, false);
        addParamSlider("Evaporazione", 0.90, 0.999, SimulationConfig.PHEROMONE_EVAPORATION_RATE,
                v -> SimulationConfig.PHEROMONE_EVAPORATION_RATE = v, false);
        addParamSlider("Max fero.", 10, 200, SimulationConfig.MAX_PHEROMONE_LEVEL,
                v -> SimulationConfig.MAX_PHEROMONE_LEVEL = v, true);

        addSection("NIDO");
        addParamSlider("Raggio nido", 5, 50, SimulationConfig.NEST_HOME_EMIT_RADIUS,
                v -> SimulationConfig.NEST_HOME_EMIT_RADIUS = v, true);
        addParamSlider("Forza nido", 0, 5, SimulationConfig.NEST_HOME_EMIT_STRENGTH,
                v -> SimulationConfig.NEST_HOME_EMIT_STRENGTH = v, false);

        addSection("MONDO");
        addParamSlider("Ostacoli", 0, 40, SimulationConfig.OBSTACLE_CLUSTERS,
                v -> SimulationConfig.OBSTACLE_CLUSTERS = (int) v, true);
        addParamSlider("Raggio cibo", 4, 30, SimulationConfig.FOOD_CLUSTER_RADIUS,
                v -> SimulationConfig.FOOD_CLUSTER_RADIUS = v, false);
        addParamSlider("HP cibo", 20, 400, SimulationConfig.FOOD_CLUSTER_HP,
                v -> SimulationConfig.FOOD_CLUSTER_HP = (int) v, true);
        addParamSlider("Formiche", 10, 5000, SimulationConfig.INITIAL_AGENT_COUNT, v -> {
            SimulationConfig.INITIAL_AGENT_COUNT = (int) v;
            engine.setAgentCount((int) v);
        }, true);
    }

    private void addSection(String title) {
        Label header = new Label(title);
        header.getStyleClass().add("param-section");
        content.getChildren().add(header);
    }

    private void addParamSlider(String label, double min, double max, double value,
                               DoubleConsumer setter, boolean integer) {
        Label name = new Label(label);
        Label valueLabel = new Label(formatValue(value, integer));
        valueLabel.getStyleClass().add("param-value");

        HBox header = new HBox(4, name, valueLabel);
        HBox.setHgrow(name, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        Slider slider = new Slider(min, max, value);
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.valueProperty().addListener((obs, oldV, newV) -> {
            double v = newV.doubleValue();
            setter.accept(v);
            valueLabel.setText(formatValue(v, integer));
        });

        VBox row = new VBox(2, header, slider);
        row.setPadding(new Insets(0, 0, 6, 0));
        content.getChildren().add(row);
    }

    private static String formatValue(double v, boolean integer) {
        if (integer) return String.valueOf((int) Math.round(v));
        if (v >= 100) return String.format("%.0f", v);
        if (v >= 10) return String.format("%.1f", v);
        if (v >= 1) return String.format("%.2f", v);
        return String.format("%.3f", v);
    }
}

```

### `src/main/java/it/unibo/antsim/view/PheromoneViewMode.java`

```java
package it.unibo.antsim.view;

public enum PheromoneViewMode {
    NONE,
    FOOD,
    HOME,
    BOTH
}

```

### `src/main/java/it/unibo/antsim/view/SimulationView.java`

```java
package it.unibo.antsim.view;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.config.ViewConfig;
import it.unibo.antsim.model.environment.CellType;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Position;
import it.unibo.antsim.model.agent.Ant;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class SimulationView extends Canvas {
    private final Environment environment;
    private final Camera camera;
    private final WritableImage pheromoneField;
    private final PixelWriter pheromoneWriter;

    // Dark, professional palette
    private static final int SOIL_R = 13;
    private static final int SOIL_G = 15;
    private static final int SOIL_B = 21;
    private static final double PHERO_REF = 18.0;
    private static final Font FONT = Font.font("System", 12);

    private double lastMouseX;
    private double lastMouseY;

    public SimulationView(Environment environment) {
        super(ViewConfig.VIEW_WIDTH, ViewConfig.VIEW_HEIGHT);
        this.environment = environment;
        this.camera = new Camera(
                environment.getGrid().getWidth(),
                environment.getGrid().getHeight(),
                ViewConfig.VIEW_WIDTH,
                ViewConfig.VIEW_HEIGHT
        );
        this.pheromoneField = new WritableImage(
                environment.getGrid().getWidth(),
                environment.getGrid().getHeight()
        );
        this.pheromoneWriter = pheromoneField.getPixelWriter();

        setupMouseControls();

        // Keep the camera matched to the actual canvas size. The canvas is bound
        // to fill the center area in Main, so this makes the world always occupy
        // the whole view instead of being letterboxed into a square.
        widthProperty().addListener((obs, oldV, newV) -> camera.setViewSize(getWidth(), getHeight()));
        heightProperty().addListener((obs, oldV, newV) -> camera.setViewSize(getWidth(), getHeight()));
    }

    public void resetCamera() {
        camera.fit();
    }

    public void render(List<Ant> ants) {
        // Pheromones only change on a simulation step (10 Hz), so recomputing the
        // field every render frame (60 Hz) is wasteful. Throttle it.
        if (frame++ % 3 == 0) {
            updatePheromoneField();
        }

        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.rgb(SOIL_R, SOIL_G, SOIL_B));
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.save();
        gc.translate(getWidth() / 2.0, getHeight() / 2.0);
        gc.scale(camera.getZoom(), camera.getZoom());
        gc.translate(-camera.getCenterX(), -camera.getCenterY());
        gc.setImageSmoothing(true);

        drawPheromoneField(gc);
        drawObstacles(gc);
        drawFood(gc);
        drawNest(gc);

        gc.restore();

        drawAnts(gc, ants);
        drawLegend(gc);
        drawMinimap(gc);
    }

    private int frame = 0;

    /**
     * Returns the inclusive cell range currently visible through the camera,
     * so the (potentially huge) grid is only iterated for on-screen cells.
     */
    private int[] visibleCellBounds() {
        int gx = environment.getGrid().getWidth();
        int gy = environment.getGrid().getHeight();
        int x0 = (int) Math.floor(camera.screenToWorldX(0));
        int y0 = (int) Math.floor(camera.screenToWorldY(0));
        int x1 = (int) Math.ceil(camera.screenToWorldX(getWidth()));
        int y1 = (int) Math.ceil(camera.screenToWorldY(getHeight()));
        x0 = Math.max(0, x0);
        y0 = Math.max(0, y0);
        x1 = Math.min(gx - 1, x1);
        y1 = Math.min(gy - 1, y1);
        return new int[]{x0, x1, y0, y1};
    }

    private void updatePheromoneField() {
        int[] b = visibleCellBounds();
        int w = environment.getGrid().getWidth();

        for (int x = b[0]; x <= b[1]; x++) {
            for (int y = b[2]; y <= b[3]; y++) {
                var cell = environment.getCell(x, y);
                double food = Math.min(cell.getPheromoneLevel() / PHERO_REF, 1.0);
                double home = Math.min(cell.getHomePheromoneLevel() / PHERO_REF, 1.0);

                // Food trails glow warm/orange, home trails glow cool/blue.
                int r = SOIL_R + (int) (food * 235) + (int) (home * 28);
                int g = SOIL_G + (int) (food * 120) + (int) (home * 150);
                int b2 = SOIL_B + (int) (food * 45) + (int) (home * 235);

                pheromoneWriter.setArgb(x, y, argb(clamp(r), clamp(g), clamp(b2)));
            }
        }
    }

    private void drawPheromoneField(GraphicsContext gc) {
        gc.drawImage(pheromoneField, 0, 0,
                environment.getGrid().getWidth(),
                environment.getGrid().getHeight());
    }

    private void drawObstacles(GraphicsContext gc) {
        int[] b = visibleCellBounds();

        for (int x = b[0]; x <= b[1]; x++) {
            for (int y = b[2]; y <= b[3]; y++) {
                if (environment.getCell(x, y).getType() != CellType.OBSTACLE) continue;

                // Stable per-cell shading so rocks read as organic blobs, not a grid.
                double shade = 0.82 + 0.18 * (((x * 31 + y * 17) % 7) / 6.0);
                int base = (int) (58 * shade);
                gc.setFill(Color.rgb(base, base + 8, base + 18));
                gc.fillOval(x - 0.08, y - 0.08, 1.16, 1.16);
            }
        }
    }

    private void drawFood(GraphicsContext gc) {
        int[] b = visibleCellBounds();

        for (int x = b[0]; x <= b[1]; x++) {
            for (int y = b[2]; y <= b[3]; y++) {
                var cell = environment.getCell(x, y);
                if (cell.getType() != CellType.FOOD) continue;

                double frac = clamp01(cell.getFoodHP() / SimulationConfig.FOOD_CLUSTER_HP);
                double radius = 0.32 + 0.30 * frac;

                if (frac > 0.15) {
                    gc.setFill(Color.rgb(40, 120, 60, 0.25 * frac));
                    gc.fillOval(x + 0.5 - radius * 1.6, y + 0.5 - radius * 1.6,
                            radius * 3.2, radius * 3.2);
                }

                int r = (int) (40 + 30 * frac);
                int g = (int) (150 + 90 * frac);
                int b2 = (int) (60 + 40 * frac);
                gc.setFill(Color.rgb(r, g, b2));
                gc.fillOval(x + 0.5 - radius, y + 0.5 - radius, radius * 2, radius * 2);
            }
        }
    }

    private void drawNest(GraphicsContext gc) {
        int nx = environment.getNestPosition().x();
        int ny = environment.getNestPosition().y();

        gc.setFill(Color.rgb(30, 24, 20));
        gc.fillOval(nx - 1.4, ny - 1.4, 2.8, 2.8);
        gc.setFill(Color.rgb(20, 14, 10));
        gc.fillOval(nx - 0.8, ny - 0.8, 1.6, 1.6);
        gc.setFill(Color.rgb(255, 176, 92, 0.9));
        gc.fillOval(nx - 0.28, ny - 0.28, 0.56, 0.56);
    }

    private void drawAnts(GraphicsContext gc, List<Ant> ants) {
        for (Ant ant : ants) {
            double sx = camera.worldToScreenX(ant.getDoubleX());
            double sy = camera.worldToScreenY(ant.getDoubleY());

            if (sx < -10 || sx > getWidth() + 10 || sy < -10 || sy > getHeight() + 10) continue;

            double angle = ant.getHeading();
            boolean carrying = ant.isCarryingFood();

            // Motion tail pointing back along the heading.
            gc.setStroke(carrying ? Color.rgb(120, 230, 140, 0.5) : Color.rgb(255, 210, 140, 0.45));
            gc.setLineWidth(1.4);
            gc.strokeLine(sx, sy, sx - Math.cos(angle) * 5, sy - Math.sin(angle) * 5);

            // Glow halo.
            gc.setFill(carrying ? Color.rgb(120, 230, 140, 0.30) : Color.rgb(255, 214, 150, 0.30));
            gc.fillOval(sx - 4, sy - 4, 8, 8);

            // Bright core.
            gc.setFill(carrying ? Color.rgb(190, 255, 200) : Color.rgb(255, 238, 210));
            gc.fillOval(sx - 1.7, sy - 1.7, 3.4, 3.4);
        }
    }

    private void drawLegend(GraphicsContext gc) {
        String[] labels = {"Scia verso il cibo", "Scia verso il nido", "Cibo", "Formiche"};
        Color[] colors = {
                Color.rgb(255, 170, 70),
                Color.rgb(110, 170, 255),
                Color.rgb(110, 220, 120),
                Color.rgb(255, 222, 165)
        };

        double x = 14;
        double y = 14;
        double w = 132;
        double h = 12 + labels.length * 18;

        gc.setFill(Color.rgb(10, 12, 18, 0.72));
        gc.fillRoundRect(x, y, w, h, 8, 8);
        gc.setStroke(Color.rgb(80, 90, 110, 0.5));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, w, h, 8, 8);

        gc.setFont(FONT);
        for (int i = 0; i < labels.length; i++) {
            double rowY = y + 22 + i * 18;
            gc.setFill(colors[i]);
            gc.fillOval(x + 12, rowY - 5, 9, 9);
            gc.setFill(Color.rgb(220, 226, 236));
            gc.fillText(labels[i], x + 30, rowY + 3);
        }
    }

    private void drawMinimap(GraphicsContext gc) {
        int mw = 150;
        int mh = 150;
        double mx = getWidth() - mw - 14;
        double my = getHeight() - mh - 14;

        // Semi-transparent frame.
        gc.setFill(Color.rgb(8, 10, 16, 0.72));
        gc.fillRect(mx, my, mw, mh);
        gc.setStroke(Color.rgb(90, 100, 120, 0.7));
        gc.setLineWidth(1);
        gc.strokeRect(mx, my, mw, mh);

        int gx = environment.getGrid().getWidth();
        int gy = environment.getGrid().getHeight();
        double sx = mw / (double) gx;
        double sy = mh / (double) gy;

        // Nest marker.
        Position nest = environment.getNestPosition();
        gc.setFill(Color.rgb(255, 176, 92));
        gc.fillOval(mx + nest.x() * sx - 3, my + nest.y() * sy - 3, 6, 6);

        // Current camera viewport (so you can tell where you are in the world).
        double wx0 = camera.screenToWorldX(0);
        double wy0 = camera.screenToWorldY(0);
        double wx1 = camera.screenToWorldX(getWidth());
        double wy1 = camera.screenToWorldY(getHeight());
        gc.setStroke(Color.rgb(210, 220, 235, 0.9));
        gc.setLineWidth(1.5);
        gc.strokeRect(mx + wx0 * sx, my + wy0 * sy, (wx1 - wx0) * sx, (wy1 - wy0) * sy);
    }

    private void setupMouseControls() {
        setOnScroll(e -> {
            double factor = e.getDeltaY() < 0 ? 0.9 : 1.1;
            camera.zoomAt(e.getX(), e.getY(), factor);
            e.consume();
        });

        setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                camera.pan(e.getX() - lastMouseX, e.getY() - lastMouseY);
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static int argb(int r, int g, int b) {
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }
}

```

### `src/main/java/it/unibo/antsim/view/StatsPanel.java`

```java
package it.unibo.antsim.view;

import it.unibo.antsim.simulation.SimulationEngine;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StatsPanel extends HBox {
    private final Label statusLabel = new Label();
    private final Label stepLabel = new Label();
    private final Label foodPickedLabel = new Label();
    private final Label foodAtNestLabel = new Label();
    private final Label foodHpLabel = new Label();
    private final Label agentsLabel = new Label();

    public StatsPanel() {
        super(16);
        setPadding(new Insets(10));
        getStyleClass().add("stats-panel");

        getChildren().addAll(
                statusLabel,
                stepLabel,
                foodPickedLabel,
                foodAtNestLabel,
                foodHpLabel,
                agentsLabel
        );
    }

    public void update(SimulationEngine engine) {
        statusLabel.setText("Status: " + engine.getStatus());
        stepLabel.setText("Step: " + engine.getState().getStepCount());
        foodPickedLabel.setText("Food picked: " + engine.getState().getFoodPicked());
        foodAtNestLabel.setText("Food at nest: " + engine.getState().getFoodAtNest());
        foodHpLabel.setText("Food HP: " + engine.getEnvironment().getTotalFoodHP());
        agentsLabel.setText("Agents: " + engine.getState().getAgentCount());
    }
}

```

## 5. Risorse (`src/main/resources`)

### `src/main/resources/style.css`

```css
.root {
    -fx-background-color: #20242a;
    -fx-font-family: "Segoe UI", "Inter", "Arial", sans-serif;
}

.control-panel {
    -fx-background-color: #2b3038;
    -fx-border-color: #3a414d;
    -fx-border-width: 0 0 0 1;
    -fx-padding: 14;
    -fx-spacing: 10;
    -fx-pref-width: 220;
}

.control-panel .viewport {
    -fx-background-color: transparent;
}

.param-section {
    -fx-text-fill: #9fb3c8;
    -fx-font-size: 11px;
    -fx-font-weight: bold;
    -fx-padding: 10 0 2 0;
    -fx-border-color: #3a414d;
    -fx-border-width: 1 0 0 0;
}

.stats-panel {
    -fx-background-color: #252a31;
    -fx-border-color: #3a414d;
    -fx-border-width: 1 0 0 0;
    -fx-padding: 10 14;
    -fx-spacing: 18;
}

.panel-title {
    -fx-text-fill: #d8dee9;
    -fx-font-size: 13px;
    -fx-font-weight: bold;
}

.label {
    -fx-text-fill: #c8d0dc;
    -fx-font-size: 12px;
}

.button {
    -fx-background-color: #3a4656;
    -fx-text-fill: #edf2f7;
    -fx-background-radius: 4;
    -fx-border-radius: 4;
    -fx-padding: 8 14;
    -fx-font-size: 12px;
}

.button:hover {
    -fx-background-color: #46576b;
}

.button:focused {
    -fx-border-color: #5e81ac;
    -fx-border-width: 2;
    -fx-border-radius: 4;
}

.button:pressed {
    -fx-background-color: #53677f;
}

.button:disabled {
    -fx-opacity: 0.45;
}

.control-button {
    -fx-background-color: #3a4656;
    -fx-text-fill: #edf2f7;
    -fx-background-radius: 4;
    -fx-border-radius: 4;
    -fx-padding: 8 14;
    -fx-font-size: 12px;
    -fx-cursor: hand;
}

.control-button:hover {
    -fx-background-color: #4e5b70;
    -fx-border-color: #6b7a96;
    -fx-border-width: 1;
    -fx-border-radius: 4;
}

.control-button:focused {
    -fx-background-color: #4c566a;
    -fx-border-color: #81a1c1;
    -fx-border-width: 2;
}

.control-button:pressed {
    -fx-background-color: #2c3440;
}

.control-button:disabled {
    -fx-opacity: 0.45;
    -fx-cursor: default;
}

.separator .line {
    -fx-border-color: #49515f;
}


```

## 6. Test (`src/test/java`)

### `src/test/java/it/unibo/antsim/model/agent/AntTest.java`

```java
package it.unibo.antsim.model.agent;

import it.unibo.antsim.model.environment.CellType;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntTest {

    @Test
    void antStartsSearchingFood() {
        Ant ant = new Ant(0, 0);

        assertEquals(AntState.SEARCHING_FOOD, ant.getState());
        assertFalse(ant.isCarryingFood());
    }

    @Test
    void pickAndDropFoodChangeState() {
        Ant ant = new Ant(0, 0);

        ant.pickFood();

        assertEquals(AntState.RETURNING_TO_NEST, ant.getState());
        assertTrue(ant.isCarryingFood());

        ant.dropFood();

        assertEquals(AntState.SEARCHING_FOOD, ant.getState());
        assertFalse(ant.isCarryingFood());
    }

    @Test
    void searchingAntDepositsHomePheromone() {
        Environment environment = new Environment(10, 10);
        Ant ant = new Ant(5, 5);

        ant.move(environment);

        assertTrue(environment.getCell(5, 5).getHomePheromoneLevel() > 0);
    }

    @Test
    void returningAntDepositsFoodPheromone() {
        Environment environment = new Environment(10, 10);
        environment.setNestPosition(new Position(0, 0));
        Ant ant = new Ant(0, 0);
        ant.pickFood();

        ant.move(environment);

        assertTrue(environment.getCell(0, 0).getPheromoneLevel() > 0);
    }

    @Test
    void antStaysInsideWorldAndAvoidsObstacles() {
        Environment environment = new Environment(30, 30);
        environment.getCell(15, 14).setType(CellType.OBSTACLE);

        Ant ant = new Ant(15, 15);

        for (int i = 0; i < 400; i++) {
            ant.move(environment);
        }

        int x = ant.getX();
        int y = ant.getY();
        assertTrue(x >= 0 && x < 30, "x out of bounds: " + x);
        assertTrue(y >= 0 && y < 30, "y out of bounds: " + y);
        assertFalse(environment.getCell(x, y).isObstacle(), "ant ended on an obstacle");
    }
}

```

### `src/test/java/it/unibo/antsim/model/agent/AcoTest.java`

```java
package it.unibo.antsim.model.agent;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Position;
import it.unibo.antsim.simulation.SimulationEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AcoTest {

    @Test
    public void searchingAntsEventuallyFindTheFoodCluster() {
        Environment env = new Environment(80, 80);
        env.setNestPosition(new Position(6, 40));
        env.generateFoodCluster(70, 40, 10.0, SimulationConfig.FOOD_CLUSTER_HP);

        SimulationEngine engine = new SimulationEngine(env);
        for (int i = 0; i < 300; i++) {
            engine.addAnt(new Ant(6, 40));
        }

        engine.start();
        for (int step = 0; step < 3000; step++) {
            engine.step();
        }

        // With hundreds of ants and a large food source, some must reach it.
        assertTrue(engine.getState().getFoodPicked() > 0,
                "no ant ever reached the food cluster");
    }
}

```

### `src/test/java/it/unibo/antsim/model/agent/ConvergenceTest.java`

```java
package it.unibo.antsim.model.agent;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Grid;
import it.unibo.antsim.model.environment.Position;
import it.unibo.antsim.simulation.SimulationEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConvergenceTest {

    @Test
    public void convergenceShowsEmergentTrail() {
        Environment env = new Environment(80, 80);
        env.setNestPosition(new Position(6, 40));
        env.generateFoodCluster(70, 40, 10.0, SimulationConfig.FOOD_CLUSTER_HP);

        SimulationEngine engine = new SimulationEngine(env);
        for (int i = 0; i < 300; i++) {
            engine.addAnt(new Ant(6, 40));
        }

        engine.start();

        int totalSteps = 6000;
        for (int step = 0; step < totalSteps; step++) {
            engine.step();

            if (step % 500 == 0) {
                double maxPheromone = computeMaxFoodPheromone(env);
                System.out.printf("step=%d foodPicked=%d foodAtNest=%d maxFoodPheromone=%.2f%n",
                        step,
                        engine.getState().getFoodPicked(),
                        engine.getState().getFoodAtNest(),
                        maxPheromone);
            }
        }

        double finalMax = computeMaxFoodPheromone(env);
        System.out.printf("FINAL step=%d foodPicked=%d foodAtNest=%d maxFoodPheromone=%.2f%n",
                totalSteps,
                engine.getState().getFoodPicked(),
                engine.getState().getFoodAtNest(),
                finalMax);

        assertTrue(engine.getState().getFoodAtNest() > 0,
                "no food delivered; convergence failed");
    }

    private double computeMaxFoodPheromone(Environment env) {
        double max = 0;
        Grid grid = env.getGrid();
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                double val = grid.getCell(x, y).getPheromoneLevel();
                if (val > max) max = val;
            }
        }
        return max;
    }
}

```

### `src/test/java/it/unibo/antsim/model/environment/EnvironmentTest.java`

```java
package it.unibo.antsim.model.environment;

import it.unibo.antsim.config.SimulationConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentTest {

    @Test
    void defaultNestPositionIsOrigin() {
        Environment environment = new Environment(10, 10);

        assertEquals(new Position(0, 0), environment.getNestPosition());
        assertTrue(environment.isNest(0, 0));
    }

    @Test
    void setNestPositionMovesNestCell() {
        Environment environment = new Environment(10, 10);

        environment.setNestPosition(new Position(3, 4));

        assertFalse(environment.isNest(0, 0));
        assertTrue(environment.isNest(3, 4));
        assertEquals(new Position(3, 4), environment.getNestPosition());
    }

    @Test
    void cannotSetNestOutsideGrid() {
        Environment environment = new Environment(10, 10);

        assertThrows(
                IllegalArgumentException.class,
                () -> environment.setNestPosition(new Position(10, 0))
        );
    }

    @Test
    void walkableNeighborsExcludeObstaclesAndOutOfBounds() {
        Environment environment = new Environment(3, 3);
        environment.getCell(1, 0).setType(CellType.OBSTACLE);

        var positions = environment.getWalkableNeighborPositions(0, 0);

        assertFalse(positions.contains(new Position(1, 0)));
        assertTrue(positions.contains(new Position(0, 1)));
        assertEquals(1, positions.size());
    }

    @Test
    void foodClusterGeneratesManyFoodCells() {
        Environment environment = new Environment(60, 60);
        environment.generateFoodCluster(30, 30, 10.0, 200);

        int foodCells = environment.countCellsOfType(CellType.FOOD);
        assertTrue(foodCells > 200, "expected a dense cluster, got " + foodCells);
        assertEquals(200 * foodCells, environment.getTotalFoodHP());
    }

    @Test
    void rockClustersSpawnObstaclesAndKeepNestClear() {
        Environment environment = new Environment(160, 160);
        environment.setNestPosition(new Position(SimulationConfig.NEST_X, SimulationConfig.NEST_Y));
        environment.generateRockClusters();

        int obstacles = environment.countCellsOfType(CellType.OBSTACLE);
        assertTrue(obstacles > 100, "expected several rock cells, got " + obstacles);

        // The nest and its surroundings are never buried by rocks.
        assertFalse(environment.getCell(SimulationConfig.NEST_X, SimulationConfig.NEST_Y).isObstacle());
    }

    @Test
    void continuousSamplingRespectsCellContents() {
        Environment environment = new Environment(20, 20);
        environment.setNestPosition(new Position(5, 5));
        environment.getCell(8, 8).setType(CellType.OBSTACLE);

        assertTrue(environment.isNestAt(5.2, 5.2));
        assertFalse(environment.isNestAt(8.2, 8.2));

        assertTrue(environment.isObstacleAt(8.4, 8.6));
        assertFalse(environment.isObstacleAt(5.2, 5.2));

        environment.depositFoodPheromoneAt(3.5, 3.5, 5.0);
        assertTrue(environment.getFoodPheromoneAt(3.1, 3.9) > 0);
    }
}

```

## 7. Build, test ed esecuzione

```bash
# 1) Rendi eseguibile lo wrapper (solo Linux/macOS)
chmod +x gradlew

# 2) Compila, esegui i test e costruisci il fat-jar eseguibile
./gradlew build

# 3) Esegui l'applicazione (GUI JavaFX)
./gradlew run

# 4) Produci il jar eseguibile standalone (shadowJar)
./gradlew shadowJar
#   -> build/libs/ant-simulator-all.jar

# 5) Rigenera il wrapper (per ottenere gradle-wrapper.jar identico)
gradle wrapper --gradle-version 9.5.1
```
Il task `build` deve concludersi con esito positivo ed eseguire i test JUnit 6 (configurati in `tasks.withType<Test> { useJUnitPlatform() }`).
Il fat-jar e' eseguibile con `java -jar build/libs/ant-simulator-all.jar` su qualsiasi piattaforma con Java 26 (il plugin `org.openjfx.javafxplugin` include i moduli per linux/mac/win).

## 8. Flusso di lavoro Git (branch per funzionalita')

Ogni membro lavora su un `feature/*` e apre una PR verso `develop`; `develop` viene integrato in `main` solo alla consegna.
```bash
git checkout -b feature/aco-implementation develop
# ... lavoro, commit frequenti e ben descritti ...
git push -u origin feature/aco-implementation
# apri PR su GitHub, merge con squash/merge in develop
git checkout develop && git merge feature/aco-implementation
```
Tracciare **solo** i sorgenti: `.gradle/`, `build/`, `.idea/`, `bin/` sono ignorati (vedi `.gitignore`). Non committare file `.class` ne' il `gradle-wrapper.jar` tramite LFS.

## 9. Ricostruzione "migliore ed efficiente" (consigli)

Rispetto alla copia identica, per ottenere un progetto piu' pulito e valutabile:
1. **Separazione dominio/vista**: il dominio (`model.*`) non deve importare JavaFX; eventuali dipendenze dalla UI vanno tolte (gia' rispettato, ma verificare sempre).
2. **Config centralizzata**: `SimulationConfig` ha campi `public static` mutabili usati dall'UI; meglio un `record` immutabile iniettato, cosi' i test sono deterministici.
3. **Test senza GUI**: i test attuali coprono dominio e ACO senza JavaFX (corretto). Aggiungere test su `SimulationEngine.step()` per food-delivered > 0 in modo riproducibile.
4. **Performance**: `Environment.update()` e i totali feromoni iterano tutta la griglia (600x600); con mondo grande e 2000 formiche conviene iterare solo sulle celle visitate o usare una matrice densa piu' efficiente. La `Camera` e il rendering usano gia' il viewport visibile (buono).
5. **Report**: redigere `report.pdf` seguendo il template d'esame (M2): non aggiungere sezioni non richieste, includere diagrammi di dominio e di architettura leggibili.
6. **README**: il `README.md` attuale e' la bozza della proposta (P5); alla consegna sostituirlo con istruzioni d'uso e crediti/librerie (M5).
