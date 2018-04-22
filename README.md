### TCPMon

TCPMon [v1.0](http://ws.apache.org/tcpmon/) was a classic utility for viewing HTTP requests and responses. The code base
was not maintained for a while, I was surprised to see people still successfully using it for common WS troubleshooting.

#### Why 2.0

- There are not many open source tools available for testing SOAP/WS-Security based web services, SOAP UI Pro is now 
mostly a paid version and it is not being actively contributed by developers on github. Undoubtedly it has comprehensive
functionally available in market yet it is hard to customize from development perspective, hence there are not many free 
plugins. There are really outstanding tools like Postman but they cater to mostly REST style web service. 

#### TCPMon 2.0 Features

- XML syntax and code editor support.
- Uses Java Swing with Spring making it is easy to add your own custom tabs/panes. This works on most platforms with 
least efforts. It is most practical solution for IDE like tools.
- Instead of passing dozen of arguments from CLI, uses a JSON configuration file.
- Add Mock Server for serving canned responses from a directory.
- Add logger panel to display live logging.
- Add live Data Base monitor, in case you need it. Many web service that I developed interact with some kind of DB and it is
helpful to monitor that DB while developing. Since DB is tied to DB type specific drivers and schema, this is disabled 
unless you need it and ready to configure.
- There are many more minor changes like; client sending request at regular interval, open file with right click in
request editor pane.

#### Change log  

#### Contact


#### Collaborate

Any kind of help with the project will be well received, and there are two main ways to give such help:

- Reporting errors and asking for extensions through the issues management
- or forking the repository and extending the project

#### Issues management

Issues are managed at the GitHub [project issues tracker][issues], where any Github user may report bugs or ask for new features.

#### Getting the code

If you wish to fork or modify the code, visit the [GitHub project page][scm], where the latest versions are always kept.
Check the 'master' branch for the latest release, and the 'develop' for the current, and stable, development version.

#### Similar projects

- [TCPMon on Github](https://github.com/search?l=Java&q=tcpmon&type=Repositories)

#### License

The project has been released under the [MIT License][license].

[issues]: https://github.com/dhval/tcpmon/issues
[license]: http://www.opensource.org/licenses/mit-license.php
[scm]: https://github.com/dhval/tcpmon

[ws-security]: https://www.oasis-open.org/committees/wss/
[xwss]: https://docs.oracle.com/cd/E17802_01/webservices/webservices/docs/1.6/tutorial/doc/XWS-SecurityIntro4.html
[wss4j]: https://ws.apache.org/wss4j/

[spring-ws]: http://projects.spring.io/spring-ws/

