# Passthru Proxy

This is an experimentai  non-production quality Proxy implementation using Zuul that forwards HTTP requests to the target ser

### What it is
Passthru is a simple proxt


### How to build

```
gradle clean build
```
This will create the executable JAR under directory 
build/libs/

### How to run
java -jar passthru-0.0.1-SNAPSHOT.jar --server.port=<PORT> --passthru.target <TARGET>

Alternatively you can pass server.port and passthru.target as environment variables.

#### Docker
After building the docker image, you can run the docker image passing server.port and  passthru.target as environment variables


### Disclaimers
* This is an experimental PoC mainly for self-learning purposes.
* Code quality is not production grade.
* Passthru does not support HTTPS
* Response logging is not extensible or configurable, there is a single Logger implementation. 
