
# Thread Agent
## About the project
Thread Agent enables tracking the behavior of
application threads, providing key insights into synchronization objects and the execution
flow of individual threads. Based on generated log files, the user can identify the causes of
synchronization issues, detect potential thread leaks, and analyze thread interleaving. Thread
Agent features a mechanism that analyzes and detects thread deadlocks, exceptions, and
other critical situations relevant to the user. The application is designed not only to support
debugging in multithreaded environments but also to serve an educational purpose, helping
users better understand the mechanisms of concurrency in Java.
## 📑 Table of Contents
- [🚀 Technologies](#technologies)
- [⚙️ Installation](#installation)
- [🔧 Configuration](#configuration)
- [▶️ Running](#running)
- [📚 Documentation](#documentation)
- [🧱 Build locally](#build-locally)
- [🧩 Contributing](#contributing)
- [📝 Current Version and Changelog](#current-version-and-changelog)
- [👥 Authors](#authors)

## Technologies
- **Language:** Java 11
- **Libraries:** Byte Buddy, ASM
- **Build Tool:** Gradle

## Installation
To install Thread Agent, download the prebuilt agent from the Releases page
. Please note that Java is not backwards compatible, so Thread Agent only works with applications running on Java 11 or higher.
## Configuration
To configure Thread Agent, you need to modify two files:

* `threadmonitoring/configuration/conf.yml`  
  This file allows you to specify which packages or classes should be monitored by Thread Agent. For minimal monitoring, include `java.lang.Thread` and `java.lang.Object`. For more comprehensive monitoring, consider including all packages from your application.

* `threadmonitoring/configuration/log4j2.yml`  
  This file contains the logging configuration settings for Thread Agent.
## Running
To run Thread Agent, add the `-javaagent` option to the Java command that starts your application. For example:

```bash
java -javaagent:"{path_to_threadmonitoring}/thread-agent.jar" -jar sample-app.jar
```
Replace `{path_to_threadmonitoring}` with the actual path to the thread-agent.jar file. For example:
```bash
java -javaagent:"C:\Users\Piotr\thread-agent.jar" -jar my-app.jar
```
After starting, you should see output like:
```
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes
Initializing ThreadAgent before the target application to enable thread and executor monitoring
Logging has been configured to "C:\Users\Piotr\logs"
Initialized advices
Advices and method substitutions created and installed
Attempting to retransform classes
Retransformation completed successfully
Transformation and Retransformation finished, Thread Agent is running and monitoring the target application
```
This message indicates that the application is being monitored by Thread Agent.
## Documentation
Thread Agent is a static Java agent that monitors user application. Its capabilities include:

* **Thread monitoring in Java applications**  
  Thread Agent allows observation and analysis of thread behavior, including creation, start, termination, and interactions with other threads. It monitors events such as new thread creation, Executor creation and operations, Executor shutdown, calls to `Lock.lock` and `Lock.unlock`, `synchronized` blocks and methods, and many other useful multithreading mechanisms.

* **Monitoring thread state on JVM shutdown**  
  When the JVM terminates, any threads that are in a state other than `TERMINATED` will be logged along with their stack trace at the moment of interruption.

* **Call analysis for detecting potential issues**, including:
  * Potential thread deadlocks
  * Calls to `notify`/`wait` on an object without holding its monitor
  * Interruptions of sleeping threads

These events are logged in two separate files: one for method tracking and one for critical situations, such as deadlocks.

## Build Locally
To deploy Thread Agent, follow these steps:

1. Clone the repository:
   ```bash
   git clone https://github.com/PiotrStoklosa/thread-agent.git
   ```
2. Build the project using Gradle:
   ```bash
   ./gradlew buildAllJars
   ```
3. After building, the project will be available in the threadmonitoring folder.

**Requirements**:
* Java Development Kit 11
* Git
## Contributing
We welcome contributions to Thread Agent! To contribute, please follow these steps:
1. Fork the repository and clone it locally.
2. Create a new branch for your feature or bug fix:
   ```bash
   git checkout -b my-feature
   ```
3. Make your changes and commit them.
4. Open a Pull Request in the main repository.

## Current Version and Changelog

**Current Version:** v1.4.0

**Last Updated:** 2025-10-06

For a detailed history of changes and past releases, see the [Changelog](CHANGELOG.md).

## Authors
- **Piotr Stokłosa** – main developer, author of the project

