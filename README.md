# PSJRS

__PSJRS__ is just a short name of "Play Silhouette Jwt Restful Seed" and it is based on [play-silhouette-seed](https://github.com/mohiva/play-silhouette-seed ) written by [mohiva](https://github.com/mohiva ).


## Quick Start

You may just clone this project and type `sbt run` and it will start a new web service and listening port 9000 in default.

```
~ $ git clone --recurse-submodules git@github.com:yuikns/psjrs.git
~ $ cd psjrs
~/psjrs $ sbt run
[info] Loading project definition from ~/psjrs/project
[info] Updating {file:~/psjrs/project/}psjrs-build...
[info] Resolving org.fusesource.jansi#jansi;1.4 ...
[info] Done updating.
[info] Set current project to psjrs (in build file:~/psjrs/)
[info] Updating {file:~/psjrs/}psjrs...
[info] Resolving jline#jline;2.12.1 ...
[info] Done updating.

--- (Running the application, auto-reloading is enabled) ---

[info] p.a.l.c.ActorSystemProvider - Starting application default Akka system: application
[info] p.c.s.NettyServer - Listening for HTTP on /0:0:0:0:0:0:0:0:9000

(Server started, use Ctrl+D to stop and go back to the console...)

```

If you wanna to have a release, you may just type `sbt dist`

```
$ sbt dist
...
[info] Your package is ready in ~/psjrs/target/universal/psjrs-0.0.1.zip
[info]
[success] Total time: 35 s, completed Sep 30, 2015 8:39:20 PM
```

and you will find a `.zip` file in `target/universal`, unzip it, and execute the project in `bin/`



## Configure Your Service

You may edit files in conf first. Files are as follow :

```
conf
├── application.conf  # this file configured play framework
├── application.prod.conf # this file will override the keys in application.conf
├── logger.xml
├── messages
├── routes
└── silhouette.conf

```

You may also add some more classes like `controllers.ApplicationController` and configure the route in `conf/routes`



enjoy it.


