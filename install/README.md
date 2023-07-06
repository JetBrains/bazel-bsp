### Server installer options

These sections are read during server [installation](https://github.com/JetBrains/bazel-bsp#installation)
-- the moment when `.bsp/` is created containing all the necessary information to start the server.

**Note**: It happens when the installer is invoked -- **before** starting the server!

---

#### java path (`--java-path <java path>`, `-j <java path>`)

Path to java which will be used to start the server (first argument in `argv` in `.bsp/bazelbsp.json`).

##### default:

The following code will be used to deduct a java path automatically:

```
System.getProperty("java.home").resolve("bin").resolve("java")
```

---

#### debugger address (`--debugger-address <debugger address>`, `-x <debugger address>`)

Address of debugger which will be attached to the java program by the flag:

```
<java path> <server runner> -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=<debugger address>`
```

##### default:

The server will be started without debugger

#### produce trace log (`--produce-trace-log`, `-l`)

A flag specifying if `./bazelbsp/bazelbsp.trace.log` should be created and used to store trace logs.

*NOTE: trace log may affect server-client communication performance! It is recommended that this flag be set to false.

##### default:

Log file won't be created.
