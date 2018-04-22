### TCPMon

TCPMon [v1.0](http://ws.apache.org/tcpmon/) was a classic utility for viewing HTTP requests and responses. However it is 
no longer maintained. Since the code base has not been updated for a while, it is hard to extend.

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

## Change log  

## Contact
