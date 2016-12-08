# IRISv3

A brand new version of IRISv2 (home automation system).
This is core part of smart home system

[![Build Status](https://travis-ci.org/Neuronix2/IRISv3.png?branch=master)](https://travis-ci.org/Neuronix2/IRISv3)

Please remember that this project is in deep alpha and now is mostly for developers than for users.

## Features

* Spring Boot
* JavaScript based dynamic scripts support for better automation

## Protocol support

* ZWave
* Noolite

## Requirements

* JDK 8
* Linux OS (work with Windows not tested)

## Install

**mvn package**

In project root directory you will find assembled **IRISv2-linux-release.zip**. Unpack them.
Database for IRISv2 will be created dynamically at first run.

## Configuration

* Rename **main.property.example** to **main.property** in **/conf** directory
* Rename **log4j2.property.example** to **log4j2.property** in **/conf** directory
* Change what you need in **main.property** and **log4j2.property**

## Run

Type in command line: **java -jar iris.jar** or use **conf/irisd** script

## Licence

Apache 2.0