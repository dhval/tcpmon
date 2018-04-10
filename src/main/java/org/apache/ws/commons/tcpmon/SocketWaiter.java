/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ws.commons.tcpmon;

import javax.swing.JLabel;
import java.awt.Color;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * wait for incoming connections, spawn a connection thread when
 * stuff comes in.
 */
class SocketWaiter extends Thread {

   /**
    * Field sSocket
    */
   ServerSocket sSocket = null;

   /**
    * Field listener
    */
   Listener listener;

   /**
    * Field port
    */
   int port;

   /**
    * Field pleaseStop
    */
   boolean pleaseStop = false;

   /**
    * Constructor SocketWaiter
    *
    * @param l
    * @param p
    */
   public SocketWaiter(Listener l, int p) {
       listener = l;
       port = p;
       start();
   }

   /**
    * Method run
    */
   public void run() {
       try {
           listener.setLeft(
                   new JLabel(
                		   TCPMon.getMessage("wait00",
                                   " Waiting for Connection...")));
           listener.repaint();
           sSocket = new ServerSocket(port);
           for (; ;) {
               Socket inSocket = sSocket.accept();
               if (pleaseStop) {
                   break;
               }
               new Connection(listener, inSocket);
               inSocket = null;
           }
       } catch (Exception exp) {
           if (!"socket closed".equals(exp.getMessage())) {
               JLabel tmp = new JLabel(exp.toString());
               tmp.setForeground(Color.red);
               listener.setLeft(tmp);
               listener.setRight(new JLabel(""));
               listener.stop();
           }
       }
   }

   /**
    * force a halt by connecting to self and then closing the server socket
    */
   public void halt() {
       try {
           pleaseStop = true;
           new Socket("127.0.0.1", port);
           if (sSocket != null) {
               sSocket.close();
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
}
