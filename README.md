# dhcp4java

This is a fork of https://github.com/shadinger/dhcp4java from 2018-09-27

# Revision history for dhcp4java.

* v1.0.1 - work in progress
    * Mavenized (com.helger:dhcp4java)
    * Requires Java 8
    * Using SLF4J 1.7 for logging
    * Fixed JavaDocs
    * Added Travis build
* v1.00 - 2008-05-11
    * Corrected bug in makeDHCPAck that was not totally conformant to RFC2131 4.3.5. (Thanks to Thiago Figueiredo)

# Frequently Asked Questions

## What is DHCP?

*[Extract from RFC 2131]*

> The Dynamic Host Configuration Protocol (DHCP) provides configuration parameters to Internet hosts. DHCP consists of two components: a protocol for delivering host-specific configuration parameters from a DHCP server to a host and a mechanism for allocation of network addresses to hosts.


## Where can I find information about the DHCP protocol?

The first source of information are the RFCs themselves. See below about RFC compliance. I suggest an excellent book on DHCP: The DHCP Handbook (2nd edition), Ralph Droms and Ted Lemon.


## Is DHCP4Java free to use?

Yes. It is provided under L-GPL 3.0 licence, which means that you can you it in any code, open-source or not.


## What is a DHCP static/dynamic/algorithmic server?

There are three basic ways to allocate IP addresses to clients: static (pre-configured client by client), dynamic (allocated from address pools), algorithmic (calculated from data in the request - often taken from option 82).

**Static address allocation** is the simplest server to implement. It requires that every client is pre-provisioned in the server. Each client is always given the same address (hopefully) different from one client to another. However this allocation scheme is not very flexible and generates high administrative work to network administrators. It is still used for equipment needing fixed IP addresses such as printers…

**Dynamic address allocation** allows automatic address allocation generally on a first-connected first-served scheme. Leases are limited in time to allow address reuse if a client is permanently disconnected. This scheme requires persistence on the server side which raises some availability issues. Such servers should also be aware of denial of service caused by the exhaustion of all available addresses from a bogus/malicious client.

**Algorithmic address allocation** is a mix of the two previous schemes. It can be seen as an “automatic static server” in which addresses are not manually preallocated, but calculated on the fly with data taken from the request. This is generally used by ISPs (cable or DSL) to assign a fixed IP address to each physical line (cable or phone) allocated in sequence. This is done by analysing the option 82 added by the DHCP relay which marks the id of the physical line used. Simple calculation can be done to assign an IP address based on the subnet and line-id information. This scheme is quite easy to implement but has two drawbacks: it allows only 1 IP address per physical line, and it needs pre-allocation of all possible IP addresses even if there is no connected client. For this later reason, it is often limited to private non-routable IP addresses.


## Why use Java for a DHCP Server?

Traditional DHCP Server implementations are built using C or C++. It appeared quickly that such servers could be developed as well using higher productivity languages such as PERL or Java.

Java offers unique opportunities to server development such as: easy multi-threading/multi-CPU/mutli-core support, wide variety of connectivity APIs to databases through JDBC, wide community of developers…


## Are there limitations introduced by Java?

Very few.

The UdpDatagram API does not allow to send datagrams to 255.255.255.255 IP address (broadcast) using Ethernet unicast to a specific MAC address. This means that DHCPOFFER are always broadcast on the network, as if the “Broadcast bit” in client requests is always set. This has no impact on client, it only generates little additionnal traffic on the link, which is totally negligible on modern networks.

It is not possible to develop a DHCP Client in pure Java, due to a lack of API to read the MAC address of the network link. You can still develop a test Client or use system specific command/API.


## What version of Java is supported?

DHCP4Java requires Java5 (J2SE or J2EE), aka Java 1.5.

I switched to Java5 early in the development stage and I will not go back (do not ask!). Java5 simplifies the code dramatically thanks to generics, static imports, boxing/unboxing and concurrency libraries. This API will be mainly used to develop new servers, and I cannot see any use case of using this API in existing code prior to Java5.


## Does DHCP4Java support J2ME?

Not currently. However it should be relatively easy to simplify the API for a J2ME version. Let me know if there is such a need.


## Can DHCP4Java be used to implement a DHCP Client?

Yes at the protocol level. No for Java implementation reasons.

There is no standard way in Java to retrieve the MAC address of a network interface. This requires non-portable system specific calls.


## Do you provide a DHCP Client?

No, please so above about the Java limitations.

You can find sample code for DHCP client in the examples package, but it is merely for server testing purpose.


## Do you provide a DHCP Relay?

Not currently. First option would be to develop an independant relay such as the one from ISC. My plans are to provide a “proxy-mode” to the DHCP Server to allow DHCP requests relaying. This mode would be similar to the mod_proxy module for the Apache server.


## Do you provide a BOOTP client or server?

No. BOOTP is supported only at the API level, but largely untested.


## Do you support IPv6?

No. DHCP4Java only supports IPv4.

DHCP for IPv6 is a totally different story, maybe someday?


## Does Java offer enough performance compared to C?

Yes. Comparative tests are planned, but Java performance is largely enough even for high volume DHCP servers, see right below.


## How fast is DHCP4Java?

The API has been designed to be fast enough to support very large scale DHCP servers for millions of customers.

Formal performance tests are planned. However, preliminary tests show that the API is able to handle more than 10.000 req/s on a simple Laptop (Pentium-M 1.5GHz) with 512MB memory.

My original API was implemented in PERL, but I decided to switch to Java to broaden the potential developers audience. Porting the API from PERL to Java showed a ~10x performance boost with additionnal support for multi-threading.


## Which RFCs are supported by DHCP4Java?

Our design objectives are to be totally conformant to RFC regarding BOOTP/DHCP. Any non-conformant feature will be clearly documented and argumented. This may include special work-arounds for client bugs or future DHCP features not totally normalized yet.

This includes the following RFC:
    * [RFC 1534](http://www.ietf.org/rfc/rfc1534.txt): Interoperation Between DHCP and BOOTP
    * [RFC 2131](http://www.ietf.org/rfc/2131.txt): Dynamic Host Configuration Protocol, Obsoletes RFC 1541
    * [RFC 2132](http://www.ietf.org/rfc/2132.txt): DHCP Options and BOOTP Vendor Extensions
    * [RFC 2241](http://www.ietf.org/rfc/2241.txt): DHCP Options for Novell Directory Services
    * [RFC 2242](http://www.ietf.org/rfc/2242.txt): NetWare/IP Domain Name and Information
    * [RFC 2855](http://www.ietf.org/rfc/2855.txt): DHCP for IEEE 1394
    * [RFC 2937](http://www.ietf.org/rfc/2937.txt): The Name Service Search Option for DHCP
    * [RFC 3004](http://www.ietf.org/rfc/3004.txt): The User Class Option for DHCP
    * [RFC 3011](http://www.ietf.org/rfc/3011.txt): The IPv4 Subnet Selection Option for DHCP
    * [RFC 3046](http://www.ietf.org/rfc/3046.txt): DHCP Relay Agent Information Option
    * [RFC 3396](http://www.ietf.org/rfc/3396.txt): Encoding Long Options in the Dynamic Host Configuration Protocol (DHCPv4) [not yet implemented]
    * [RFC 3397](http://www.ietf.org/rfc/3397.txt): Dynamic Host Configuration Protocol (DHCP) Domain Search Option
    * [RFC 3442](http://www.ietf.org/rfc/3442.txt): The Classless Static Route Option for Dynamic Host Configuration Protocol (DHCP) version 4
    * [RFC 3679](http://www.ietf.org/rfc/3679.txt): Unused Dynamic Host Configuration Protocol (DHCP) Option Codes
    * [RFC 3942](http://www.ietf.org/rfc/3942.txt): Reclassifying Dynamic Host Configuration Protocol version 4 (DHCPv4) Options
    * [RFC 4388](http://www.ietf.org/rfc/4388.txt): Dynamic Host Configuration Protocol (DHCP) Leasequery


## Are there some non conformance to RFCs?

RFC 2131 - section 4.1 - the behaviour of the server can not be totally conformant due to Java limitation.

> [...]
>         If 'giaddr' is zero and 'ciaddr' is zero, and the broadcast bit is
>         set, then the server broadcasts DHCPOFFER and DHCPACK messages to
>         0xffffffff. If the broadcast bit is not set and 'giaddr' is zero and
>         'ciaddr' is zero, then
> [...]

Tt is not possible to unicast UDP datagrams to hardware addresses using the Java API. Only unicast to IP addresses are supported. As a side effect, servers act always as if the broadcast bit is always set. This has no functional impact on clients and negligible impact on network traffic.


# Design Goals

## Design goals

    * **Availability**: state-less front-end servers. A majority of maintenance operations should not require server restart, especially topology (subnets) modification and updates. High availability should be easy to achieve with standard and simple load balancers. In case of failure of one component (frontend or backend) other components should enter a fallback mode where they are serving "at best" clients: e.g. renewing politely leases.
    * **Adaptability**: being able to adapt server needs to many contexts: pre-provisioned, dynamic, calculated, using database (free or not) or LDAP storage, allowing connection to IT legacy systmes.
    * **Security**: frontend servers are exposed like any other internet servers. This server should not add any security breach in whatever form. Thanks to Java, buffer overflow are very unlikely. The server shoudl also defend itself against brute-force arracks and DOS/DDOS.
    * **Operations**: being designed to be integrated to a large scale network infrastructure, it should be easy to manage and monitor using different tools on the market. SNMP management interface would be good. Server implementation should be designed with easy installation in mind.
    * **Scalability**: designed for millions of customers, the whole server infrastructure must be highly performant, supporting multi-threading, and scalable, capable of serving >1000 requests/s with standard x86 servers. In frontend/backend architectures, components should be as loosely coupled as possible.

Design principles

    * **Simplicity**: mother of all virtues, it is the one design principle guiding all our developments. However we recognize it is hard to measure.
    * **100% pure Java**: yes! Java is capable of implementing a low-level network server with excellent performance, even compared with C-based products.

# History

## History (2004-2007)

In 2004 working in France Telecom, we were looking for a robust high performance DHCP server suitable for an xDSL IPTV solution. There was an existing prototype developped on an ISC dhcpd base, but it proved to be very hard to maintain and to plug to an existing Oracle database.

At that time, the DHCP algorithm was very simple. Each client had a fixed address depending on line-ID in the DSLAM marked via the option-82. This simple algorithm would calculate the client address then update the Oracle database to mark the client as connected. ISC dhpcd was absolutely not suited for such an "algorithmic" address alocation.

I have already believed that DHCP was a simple protocol and that developing a industrial-strength server solution was not so difficult. My thoughts were quickly confirmed.

And yet, we did not find any suitable solutions at that time. So I decided to develop a simple prototype in Perl to check the faisability. A few weeks later it was ready and gave excellent results. At the same time, I revived the Perl module

to offer a full DHCP API.

The full server was then industrialized by a small project team and eventually put into production by end 2004. Performance was still quite high despite the interprative nature of Perl, resulting in more than 600rq/s on a standard Xeon 3.2GHz server.

Note: the module Net::DHCP is not very active anymore but I still do some bug correction from time to time. The active version is the Java one - dhcp4java.

The IPTV service evolved to a more classical dynamic addresses allocation mecanism. We had lots of discussion about what would be the best software architecture with internal R&D people, and this is where the dhcpd-j architecture came into life. It is basically a transposition of the classical Web architecture - application frontend / Database persistant store.

We first plugged the Perl front-end to an Oracle database. It shortly became obvious that we had to share our developments with a broader community of people in the company, and Perl was not the most easily readable and sharable code. I decided to switch to Java. The good news at that time was that the Java version was nearly 10x faster than the Perl version (which was already not so bad).

*Stephan Hadinger*

## Credits

First PERL version (without database) was developed by: Bruno Crane, Stephan Hadinger with huge support from Eric Bottarlini, Pierre-Yves Eymard, Laurent Gaillard.

Second PERL version (with database) was developed by: Bruno Crane, Pierre Fischer (Oracle dev). Architecture and global design made by François Bourdaix and his team.