# Requirement Clustering evaluation

This repository contains the core algorithms for a systematic clustering evaluation of requirements.




# License 
The source code contained in this repository is licensed under [MIT conditions](LICENSE).

The code depends on additional components which may have their individual licenses:

| License | Component | 
| ----- | ----- |
|(BSD License) | AntLR Parser Generator (antlr:antlr:2.7.7 - http://www.antlr.org/) |
| (Eclipse Public License - v 1.0) | (GNU Lesser General Public License) | Logback Classic Module (ch.qos.logback:logback-classic:1.2.3 - http://logback.qos.ch/logback-classic) |
| (Eclipse Public License - v 1.0) | (GNU Lesser General Public License) | Logback Core Module (ch.qos.logback:logback-core:1.1.8 - http://logback.qos.ch/logback-core)|
| (The Apache Software License, Version 2.0) | ClassMate (com.fasterxml:classmate:1.3.0 - http://github.com/cowtowncoder/java-classmate) |
| (The Apache Software License, Version 2.0) | Jackson-annotations (com.fasterxml.jackson.core:jackson-annotations:2.9.0.pr2 - http://github.com/FasterXML/jackson) |
| (The Apache Software License, Version 2.0) | Jackson-core (com.fasterxml.jackson.core:jackson-core:2.9.0.pr2 - https://github.com/FasterXML/jackson-core) |
| (The Apache Software License, Version 2.0) | jackson-databind (com.fasterxml.jackson.core:jackson-databind:2.9.0.pr2 - http://github.com/FasterXML/jackson) |
| (LGPL) | JniLoader (com.github.fommil:jniloader:1.1 - https://github.com/fommil/jniloader) |
| (BSD 3 Clause) | all (com.github.fommil.netlib:all:1.1.2 - https://github.com/fommil/netlib-java/all) |
| (BSD 3 Clause) | core (com.github.fommil.netlib:core:1.1 - https://github.com/fommil/netlib-java/core) |
| (BSD 3 Clause) | native_ref-java (com.github.fommil.netlib:native_ref-java:1.1 - https://github.com/fommil/netlib-java/native_ref/native_ref-java) |
| (BSD 3 Clause) | native_system-java (com.github.fommil.netlib:native_system-java:1.1 - https://github.com/fommil/netlib-java/native_system/native_system-java) |
| (BSD 3 Clause) | netlib-native_ref-linux-armhf (com.github.fommil.netlib:netlib-native_ref-linux-armhf:1.1 - https://github.com/fommil/netlib-java/native_ref/native_ref-xbuilds/netlib-native_ref-linux-armhf) |
| (BSD 3 Clause) | netlib-native_ref-linux-i686 (com.github.fommil.netlib:netlib-native_ref-linux-i686:1.1 - https://github.com/fommil/netlib-java/native_ref/native_ref-xbuilds/netlib-native_ref-linux-i686) |
| (BSD 3 Clause) | netlib-native_ref-linux-x86_64 (com.github.fommil.netlib:netlib-native_ref-linux-x86_64:1.1 - https://github.com/fommil/netlib-java/native_ref/native_ref-xbuilds/netlib-native_ref-linux-x86_64) |
| (BSD 3 Clause) | netlib-native_ref-osx-x86_64 (com.github.fommil.netlib:netlib-native_ref-osx-x86_64:1.1 - https://github.com/fommil/netlib-java/native_ref/netlib-native_ref-osx-x86_64) |
| (BSD 3 Clause) | netlib-native_ref-win-i686 (com.github.fommil.netlib:netlib-native_ref-win-i686:1.1 - https://github.com/fommil/netlib-java/native_ref/netlib-native_ref-win-i686) |
| (BSD 3 Clause) | netlib-native_ref-win-x86_64 (com.github.fommil.netlib:netlib-native_ref-win-x86_64:1.1 - https://github.com/fommil/netlib-java/native_ref/netlib-native_ref-win-x86_64) |
| (BSD 3 Clause) | netlib-native_system-linux-armhf (com.github.fommil.netlib:netlib-native_system-linux-armhf:1.1 - https://github.com/fommil/netlib-java/native_system/native_system-xbuilds/netlib-native_system-linux-armhf) |
| (BSD 3 Clause) | netlib-native_system-linux-i686 (com.github.fommil.netlib:netlib-native_system-linux-i686:1.1 - https://github.com/fommil/netlib-java/native_system/native_system-xbuilds/netlib-native_system-linux-i686) |
| (BSD 3 Clause) | netlib-native_system-linux-x86_64 (com.github.fommil.netlib:netlib-native_system-linux-x86_64:1.1 - https://github.com/fommil/netlib-java/native_system/native_system-xbuilds/netlib-native_system-linux-x86_64) |
| (BSD 3 Clause) | netlib-native_system-osx-x86_64 (com.github.fommil.netlib:netlib-native_system-osx-x86_64:1.1 - https://github.com/fommil/netlib-java/native_system/netlib-native_system-osx-x86_64) |
| (BSD 3 Clause) | netlib-native_system-win-i686 (com.github.fommil.netlib:netlib-native_system-win-i686:1.1 - https://github.com/fommil/netlib-java/native_system/netlib-native_system-win-i686) |
| (BSD 3 Clause) | netlib-native_system-win-x86_64 (com.github.fommil.netlib:netlib-native_system-win-x86_64:1.1 - https://github.com/fommil/netlib-java/native_system/netlib-native_system-win-x86_64) |
| (Apache 2.0) | Gson (com.google.code.gson:gson:2.8.0 - https://github.com/google/gson/gson) |
| (GNU Lesser General Public License) | Matrix Toolkits for Java (com.googlecode.matrix-toolkits-java:mtj:1.0.4 - https://github.com/fommil/matrix-toolkits-java/) |
| (BSD 3 Clause) | netlib-java (com.googlecode.netlib-java:netlib-java:1.1 - https://github.com/fommil/netlib-java/netlib-java) |
| (MPL 2.0 or EPL 1.0) | H2 Database Engine (com.h2database:h2:1.4.195 - http://www.h2database.com) |
| (The Apache Software License, Version 2.0) | HikariCP (com.zaxxer:HikariCP:2.6.1 - https://github.com/brettwooldridge/HikariCP) |
| (The Apache Software License, Version 2.0) | Apache Commons Codec (commons-codec:commons-codec:1.9 - http://commons.apache.org/proper/commons-codec/) |
| (The Apache Software License, Version 2.0) | Apache Commons Logging (commons-logging:commons-logging:1.2 - http://commons.apache.org/proper/commons-logging/) |
| (GNU Lesser General Public License, Version 3) | germanet-api (de.tuebingen.uni.sfs.germanet:germanet-api:9.0.3 - http://www.sfs.uni-tuebingen.de/GermaNet/) |
| (Unknown license) | dom4j (dom4j:dom4j:1.6.1 - http://dom4j.org) |
| (Eclipse Public License 1.0) | JUnit (junit:junit:4.12 - http://junit.org) |
| (The Apache Software License, Version 2.0) | Byte Buddy (without dependencies) | (net.bytebuddy:byte-buddy:1.6.6 - http://bytebuddy.net/byte-buddy) |
| (The BSD License) | Fortran to Java ARPACK (net.sourceforge.f2j:arpack_combined_all:0.1 - http://f2j.sourceforge.net) |
| (GNU General Public License 3) | weka-stable (nz.ac.waikato.cms.weka:weka-stable:3.8.1 - http://www.cms.waikato.ac.nz/ml/weka/) |
| (AS-IS) | (BSD) | bounce (nz.ac.waikato.cms.weka.thirdparty:bounce:0.18 - http://www.edankert.com/bounce/) |
| (BSD-like) | java-cup-11b (nz.ac.waikato.cms.weka.thirdparty:java-cup-11b:2015.03.26 - http://www2.cs.tum.edu/projects/cup/) |
| (BSD-like) | java-cup-11b-runtime (nz.ac.waikato.cms.weka.thirdparty:java-cup-11b-runtime:2015.03.26 - http://www2.cs.tum.edu/projects/cup/) |
| (Apache License, Version 2.0) | Apache HttpClient (org.apache.httpcomponents:httpclient:4.5.2 - http://hc.apache.org/httpcomponents-client) |
| (Apache License, Version 2.0) | Apache HttpCore (org.apache.httpcomponents:httpcore:4.4.4 - http://hc.apache.org/httpcomponents-core-ga) |
| (The Apache Software License, Version 2.0) | Apache POI (org.apache.poi:poi:3.13 - http://poi.apache.org/) |
| (The Apache Software License, Version 2.0) | Apache POI (org.apache.poi:poi-ooxml:3.13 - http://poi.apache.org/) |
| (The Apache Software License, Version 2.0) | Apache POI (org.apache.poi:poi-ooxml-schemas:3.13 - http://poi.apache.org/) |
| (The Apache Software License, Version 2.0) | XmlBeans (org.apache.xmlbeans:xmlbeans:2.6.0 - http://xmlbeans.apache.org) |
| (Eclipse Public License - v 1.0) | AspectJ runtime (org.aspectj:aspectjrt:1.8.10 - http://www.aspectj.org) |
| (Apache License, Version 2.0) | Apache FreeMarker (org.freemarker:freemarker:2.3.25-incubating - http://freemarker.org/) |
| (New BSD License) | Hamcrest Core (org.hamcrest:hamcrest-core:1.3 - https://github.com/hamcrest/JavaHamcrest/hamcrest-core) |
| (GNU Lesser General Public License) | Core Hibernate O/RM functionality (org.hibernate:hibernate-core:5.2.10.Final - http://hibernate.org) |
| (GNU Lesser General Public License) | (deprecated - use hibernate-core instead) | Hibernate JPA Support (org.hibernate:hibernate-entitymanager:5.2.10.Final - http://hibernate.org) |
| (GNU Lesser General Public License) | Hibernate Commons Annotations (org.hibernate.common:hibernate-commons-annotations:5.0.1.Final - http://hibernate.org) |
| (Eclipse Distribution License (EDL), Version 1.0) | (Eclipse Public License (EPL), Version 1.0) | Java Persistence API, Version 2.1 (org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.0.Final - http://hibernate.org) |
| (Apache License 2.0) | (LGPL 2.1) | (MPL 1.1) | Javassist (org.javassist:javassist:3.20.0-GA - http://www.javassist.org/) |
| (Apache License, Version 2.0) | Java Annotation Indexer (org.jboss:jandex:2.0.3.Final - http://www.jboss.org/jandex) |
| (Apache License, version 2.0) | JBoss Logging 3 (org.jboss.logging:jboss-logging:3.3.0.Final - http://www.jboss.org) |
| (Common Development and Distribution License) | (GNU General Public License, Version 2 with the Classpath Exception) | Java Transaction API (org.jboss.spec.javax.transaction:jboss-transaction-api_1.2_spec:1.0.1.Final - http://www.jboss.org/jboss-transaction-api_1.2_spec) |
| (The JSON License) | JSON in Java (org.json:json:20160810 - https://github.com/douglascrockford/JSON-java) |
| (MIT License) | JCL 1.2 implemented over SLF4J (org.slf4j:jcl-over-slf4j:1.7.24 - http://www.slf4j.org) |
| (MIT License) | SLF4J API Module (org.slf4j:slf4j-api:1.7.23 - http://www.slf4j.org) |
| (MIT License) | SLF4J Simple Binding (org.slf4j:slf4j-simple:1.7.23 - http://www.slf4j.org) |
| (Apache License, Version 2.0) | Spring AOP (org.springframework:spring-aop:4.3.8.RELEASE - https://github.com/spring-projects/spring-framework) |
| (Apache License, Version 2.0) | Spring Beans (org.springframework:spring-beans:4.3.8.RELEASE - https://github.com/spring-projects/spring-framework) |
| (Apache License, Version 2.0) | Spring Context (org.springframework:spring-context:4.3.8.RELEASE - https://github.com/spring-projects/spring-framework) |
| (Apache License, Version 2.0) | Spring Core (org.springframework:spring-core:4.3.8.RELEASE - https://github.com/spring-projects/spring-framework) |
| (Apache License, Version 2.0) | Spring Expression Language (SpEL) | (org.springframework:spring-expression:4.3.8.RELEASE - https://github.com/spring-projects/spring-framework) |
| (Apache License, Version 2.0) | Spring JDBC (org.springframework:spring-jdbc:4.3.8.RELEASE - https://github.com/spring-projects/spring-framework) |
| (Apache License, Version 2.0) | Spring Object/Relational Mapping (org.springframework:spring-orm:4.3.8.RELEASE - https://github.com/spring-projects/spring-framework) |
| (Apache License, Version 2.0) | Spring Transaction (org.springframework:spring-tx:4.3.8.RELEASE - https://github.com/spring-projects/spring-framework) |
| (Apache License, Version 2.0) | Spring Data Core (org.springframework.data:spring-data-commons:1.13.3.RELEASE - http://www.spring.io/spring-data/spring-data-commons) |
| (Apache License, Version 2.0) | Spring Data JPA (org.springframework.data:spring-data-jpa:1.11.3.RELEASE - http://projects.spring.io/spring-data-jpa) |
| (The Apache Software License, Version 2.0) | SQLite JDBC (org.xerial:sqlite-jdbc:3.15.1 - http://nexus.sonatype.org/oss-repository-hosting.html/sqlite-jdbc) |
| (The Apache Software License, Version 2.0) | StAX API (stax:stax-api:1.0.1 - http://stax.codehaus.org/) |
|(GNU Lesser General Public License (LGPL))  |mathz (net.mikera:mathz:0.3.0 - git@github.com:mikera/mathz.git)|
|(GNU Lesser General Public License (LGPL)) |randomz (net.mikera:randomz:0.3.0 - https://github.com/mikera/randomz.git/randomz)|
|(GNU Lesser General Public License (LGPL))| Vectorz (net.mikera:vectorz:0.65.0 - https://github.com/mikera/vectorz/vectorz)|
