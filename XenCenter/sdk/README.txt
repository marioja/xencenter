XenServerJava
=============

Version 6.5.0

XenServerJava is a complete SDK for Citrix XenServer, exposing the XenServer
API as Java classes.

It is available in the XenServer-SDK-6.5.0.zip, which can be downloaded
from http://www.citrix.com/downloads/xenserver/.

For XenServer documentation see http://docs.xensource.com.
XenServerJava includes a class for every XenServer class, and a method for
each XenServer API call, so API documentation and examples written for
for other languages will apply equally well to Java.
In particular, the SDK Guide and API Documentation are ideal for developers
wishing to use XenServerJava.

For community content, blogs, and downloads, visit the XenServer Developer
Network at https://www.citrix.com/community.html.

XenServerJava is free sofware. You can redistribute and modify it under the
terms of the BSD license. See LICENSE.txt for details.


Dependencies
------------

XenServerJava is dependent upon Apache XML-RPC and WS-Commons, both by The
Apache Software Foundation. We would like to thank the ASF and the
Apache XML-RPC development team in particular. Both are licensed under the
Apache Software License 2.0. See LICENSE.Apache-2.0.txt for details.

We test with version 3.1 of Apache XML-RPC and version 1.0.2 of WS-Commons.
We recommend that you use these versions, though others may work.

Apache XML-RPC is available from http://ws.apache.org/xmlrpc/.
WS-Commons is available from http://ws.apache.org/commons/.


Folder structure
----------------

XenServerJava consists of three separate folders:
- XenServerJava/bin: contains the compiled binaries
- XenServerJava/javadoc: contains the documentation
- XenServerJava/src: contains the source code and tests; the tests can also
be used as pedagogical examples.


Compiling from source
---------------------

Extract XenServerJava from XenServer-SDK-6.5.0.zip.
Copy the dependency jars from XenServerJava/bin to XenServerJava/src.

From XenServerJava/src do:
- "make all" to build the XenServer binary
- "make docs" to build the documentation.

To build and run the tests, copy them from XenServerJava/samples to
XenServerJava/src, then from XenServerJava/src "make all" and
$ java RunTests <host> <username> <password> [nfs server] [nfs path]


