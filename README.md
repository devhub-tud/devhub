[![Stories in Ready](https://badge.waffle.io/devhub-tud/devhub.png?label=ready&title=Ready)](https://waffle.io/devhub-tud/devhub)
[![Build Status](https://travis-ci.org/devhub-tud/devhub.svg?branch=master)](https://travis-ci.org/devhub-tud/devhub)
[![Coverage Status](https://coveralls.io/repos/github/devhub-tud/devhub/badge.svg?branch=master)](https://coveralls.io/github/devhub-tud/devhub?branch=master)
DevHub
======

DevHub is a software system designed to give students a simple practical introduction into modern software development. It provides an environment in which they can work on practical assignments without setting up their own (private) code repository or a Continuous Integration server. The environment is also designed to give students a working simple workflow largely based on GitHub's pull requests system. 

Architecture
------------

DevHub is comprised of several components which integrate nicely. The `devhub-server` project is a web application which can be run from the command line, and provides easy overviews of projects and their activity. The `devhub-client` project is an application which is able to manage one or multiple Docker containers on which builds and other activities can take place in a secure and shielded environment.
