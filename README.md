Welcome to StackSync!
=====================

> **NOTE:** This is BETA quality code!

**Table of Contents**

- [Introduction](#introduction)
- [Architecture](#architecture)
- [Updater](#updater)
  - [REST API](#rest-api)
    - [Requirements](#requirements)
    - [Installation](#installation)
  - [Java Updater.jar](#java-updaterjar)
    - [Requirements](#requirements)
    - [Build and exectuion](#build-and-execution)
- [Issue Tracking](#issue-tracking)
- [Licensing](#licensing)
- [Contact](#contact)

# Introduction

StackSync (<http://stacksync.com>) is a scalable open source Personal Cloud
that implements the basic components to create a synchronization tool.


# Architecture

In general terms, StackSync can be divided into three main blocks: clients
(desktop and mobile), synchronization service (SyncService) and storage
service (Swift, Amazon S3, FTP...). An overview of the architecture
with the main components and their interaction is shown in the following image.

<p align="center">
  <img width="500" src="https://raw.github.com/stacksync/desktop/master/res/stacksync-architecture.png">
</p>

The StackSync client and the SyncService interact through the communication
middleware called ObjectMQ. The sync service interacts with the metadata
database. The StackSync client directly interacts with the storage back-end
to upload and download files.

As storage back-end we are using OpenStack Swift, an open source cloud storage
software where you can store and retrieve lots of data in virtual containers.
It's based on the Cloud Files offering from Rackspace. But it is also possible
to use other storage back-ends, such as a FTP server or S3.

# Updater

The updater is the service used to mantain desktop clients with the last release.

This service has two different parts:

## REST API

The client uses a REST API to get the last available version and, if necessary,
download the binaries. API specification:

GET VERSION
- URL structure: http://domain.ext/api/version
- Method: GET
- Response body: Json with version.
    Example: {"version":23}

GET BINARIES
- URL structure: http://domain.ext/api/files
- Method: GET
- Response body: Zip file with binaries.

### Requirements

- [Tonic](https://github.com/peej/tonic)
- PHP5

### Installation

- Install tonic under /usr/local/tonic
- Create a folder in /var/www to host your REST API.
- Copy the code from the [server](server) folder.
 
## Java Updater.jar
This JAR has to be in the same folder as the Stacksync.jar file. When it is
executed will get the version from the Stacksync.jar and the server API. If
the local version is lower than the server version, it will download binaries,
remove local files and extract the downloaded file to replace the old version.

Finally, it will launch StackSync client.

### Requirements

- Java 1.7
- Maven

### Build and execution

First of all intall the database

To create an executable jar:

    $ mvn assembly:assembly

To run the jar file:

    $ java -jar updater-XXX-jar-with-dependencies.jar

# Issue Tracking
For the moment, we are going to use the github issue tracking.

# Licensing
StackSync is licensed under the GPLv3. Check [license.txt](license.txt) for the latest
licensing information.

# Contact
Visit www.stacksync.com to contact information.
