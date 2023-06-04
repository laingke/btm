/**
 * The Bitronix Transaction Manager (BTM) is a simple but complete implementation of the JTA API. The goal is to
 * provide a fully working XA transaction manager that provides all services required by the JTA API while trying to
 * keep the code as simple as possible for easier understanding of the XA semantics.
 */
module bitronix.tm {
    exports bitronix.tm;
    exports bitronix.tm.jndi to java.naming;
    exports bitronix.tm.journal;
    exports bitronix.tm.utils;
    exports bitronix.tm.recovery;
    exports bitronix.tm.resource;
    exports bitronix.tm.resource.common;
    exports bitronix.tm.resource.jdbc;
    exports bitronix.tm.timer;

    opens bitronix.tm.resource.jdbc.proxy to org.javassist, cglib;

    requires cglib;
    requires java.management;
    requires java.naming;
    requires java.sql;
    requires jakarta.cdi;
    requires jakarta.servlet;
    requires jakarta.inject;
    requires jakarta.messaging;
    requires jakarta.transaction;
    requires com.google.common;
    requires org.javassist;
    requires org.slf4j;
}