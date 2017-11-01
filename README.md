# IRISv3

A brand new version of IRISv3 (home automation system).
This is core part of smart home system

[![Build Status](https://travis-ci.org/MrNeuronix/IRISv3.png?branch=master)](https://travis-ci.org/MrNeuronix/IRISv3)

Please remember that this project is in deep alpha and now is mostly for developers than for users.

## Features

* Spring Boot based code
* JSON API
* JavaScript based dynamic scripts support for better automation

## Protocol support

* ZWave
* Noolite
* Xiaomi MiHome

## Requirements

* JDK 8
* Linux OS (work with Windows not tested)

## Install

**mvn package**

In project root directory you will find assembled **iris-{version}-application.zip**. Unpack them.
Database for IRIS will be created dynamically at first run.

## Configuration

* Copy configuration files in **/conf** directory from *.default to *.properties
* Change what you need in **/conf** directory

## Run

Type in command line: **./start.sh**

## Licence

Apache 2.0