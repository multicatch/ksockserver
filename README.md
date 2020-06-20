# ksockserver

A little HTTP server in Kotlin

### Description

This project aims to develop a simple, but extensible framework for building
TCP servers of any purpose. It comes with HTTP/1.1 by default, but it can be used with
any protocol, as long as the support for it is implemented.

### Features

Server features:
* Event-based processing (based on _Interruptible Tasks_)
* Secure TLS Sockets (with HTTPS)
* Slowloris-proof and handles many connections well

HTTP implementation features:
* GZIP response compression
* HTTP Proxy
* Static resources support
* URL aliasing support
* Exception Mappers for global exception mapping support

### Sample configuration 

See [Example.kt](ksockserver-example/src/main/java/io/github/multicatch/ksock/example/Example.kt)

```kotlin
fun main() {
    // bind plain TCP on 8080, use HTTP
    bindTCP(port = 8080, protocol = Http) {
        // use HTTP 1.1 on this port
        useHttp11()
        // use GZIP
        withResponseWriter(GZipResponseWriter())

        // configure /* mapping
        url(index("/")) {
            // a static index of classpath resources
            staticIndex("classpath:/")
        }

        // configure /example mapping
        url(exact("/example")) {
            // a static page
            staticPage("classpath:/index.html")
        }
   
        // configure /proxy mapping
        url(exact("/proxy")) {
            // a proxy of https://httpbin.org
            proxy("https://httpbin.org/")
        }

        // make / an alias of /index.html
        alias(exact("/") to "/index.html")
    }.start() // start server on port 8080

    // bind secure TCP on 8443, use HTTP and a self signed certificate
    bindSecureTCP(
            port = 8443,
            protocol = Http,
            serverCertificate = selfSignedCertificate()
    ) {
        // use HTTP 1.1 on this port
        useHttp11()

        // configure /* mapping
        url(index("/")) {
            // a static index of classpath resources
            staticIndex("classpath:/")
        }

        // configure /proxy mapping
        url(exact("/proxy")) {
            // a proxy of https://httpbin.org
            proxy("https://httpbin.org/")
        }

        // make / an alias of /index.html
        alias(exact("/") to "/index.html")
    }.start() // start server on port 8443
}
```

### Project Modules

#### ksockserver-example
This is an example usage of the ksockserver. It contains one Kotlin file, which starts the application 
on ports 8080 and 8443 (SSL). There are the following endpoints available:
* `/` or `/index.html` - a sample HTML file
* `/proxy` - a proxy to https://httpbin.org/
* `/example` (8080 only) - a static resource (HTML file)

#### ksockserver-dispatcher

This module contains the logic of managing the socket and handling connections. There are socket configuration classes 
and dispatchers for plain TCP and secure TCP sockets.

#### ksockserver-http-core

The `ksockserver-http-core` module contains the base models and interfaces used in HTTP-related libraries, and it also
contains a few common helper functions.

#### ksockserver-http-server

This module relies on `ksockserver-http-core` and `ksockserver-dispatcher` and it contains the logic of handling 
reading of HTTP requests, mapping URLs to resource handlers and writing HTTP responses. It also handles the exceptions
during request processing and maps them according to registered ExceptionMappers

#### ksockserver-static-pages

In this module there is logic that handles reading files and serving them as static resources. It is meant to be used
with a `ksockserver-http-server`.

#### ksockserver-http-gzip

This little module enables GZip compression algorithm in HTTP responses.

#### ksockserver-http-proxy

This module is used to configure a URL mapping as a proxy of another HTTP(S) application.
