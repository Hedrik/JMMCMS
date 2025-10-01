    /*
     * Client.java
     *
     * Created on July 1, 2001, 9:21 PM
     */

/*
 * Part of the Java Massively Multi-Client Mutli-Server library.
 * Copyright (C) 2004 Richard Arnold Mead
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.InfoMontage.helper.clientServer; 

import com.InfoMontage.helper.clientServer.DefaultCommConstants.*;
import java.nio.channels.*;
import java.io.*;
import java.net.*;
import javax.net.*;
import javax.net.ssl.*;
import java.security.*;
import javax.security.cert.*;
import java.util.logging.*;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */

//public abstract class Client {
public class Client {
    
    public static final String ClientCodeVersion = "0.8.6.2";
    
//    Object app;
    ClientApp app;
    Logger log;
    
    //    public static ClientServerSocket serverSocket;
    public volatile static ClientServerSocket serverSocket;
    //    public static java.nio.channels.SocketChannel serverChannel;
    public volatile static java.net.Socket serverChannel;
    public volatile static String serverHost=CommSock.DefaultServerHostName;
    public volatile static int serverPort=CommSock.ClientConnectionListenerPort;
    private volatile boolean connectedToServer=false;
    private volatile byte loginReqsFlag;
    private volatile boolean loggedInToServer=false;
    com.InfoMontage.helper.clientServer.Client.ConnectionMonitor monitorThread;
    
    /** Creates new client */
//    public Client(Object t) {
    public Client(ClientApp t) {
        initClient(t,null,null,0);
    }
    
//    public Client(Object t, Logger l) {
    public Client(ClientApp t, Logger l) {
        initClient(t,l,null,0);
    }
    
//    public Client(Object t, Logger l, String s) {
    public Client(ClientApp t, Logger l, String s) {
        initClient(t,l,s,0);
    }
    
//    public Client(Object t, Logger l, String s, int p) {
    public Client(ClientApp t, Logger l, String s, int p) {
        initClient(t,l,s,p);
    }
    
//    void initClient(Object t, Logger l, String s, int p) {
    void initClient(ClientApp t, Logger l, String s, int p) {
        if (t==null)
            throw new NullPointerException("Attempt to create a client with a null application!");
        else
            app=t;
//        log=(l==null)?Logger.getLogger(this.getClass().getPackage().getName()):l;
        log=(l==null)?Logger.getLogger(app.getClass().getPackage().getName()):l;
//        new DefaultCommConstants(app);
        new DefaultCommConstants(app.getClass());
        setServerHostAndPort(s, p);        
        
        monitorThread=new com.InfoMontage.helper.clientServer.Client.ConnectionMonitor(this);
        monitorThread.start();
    }
    
    private void setServerHostAndPort( String s, int p) {
        serverHost=(s==null)?CommSock.DefaultServerHostName:s;
        serverPort=(p==0)?CommSock.ClientConnectionListenerPort:p;
    }
    
    private SocketFactory getSocketFactory(Object a, String type) {
        if (
        type.equals("TLS")
        //        || type.equals("SSL")
        ) {
            SSLSocketFactory sf = null;
            try {
                // set up key manager to do server authentication
                SSLContext ctx;
                TrustManagerFactory tmf;
                KeyStore ks;
                char[] passphrase = "changeit".toCharArray();
                
                //                ctx = SSLContext.getInstance("TLS");
                ctx = SSLContext.getInstance(type);
                //		kmf = KeyManagerFactory.getInstance("SunX509");
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                ks = KeyStore.getInstance("JKS");
                
                File f=new File(URI.create("file:///"+a.getClass().getResource("CSKSCerts").getPath()));
                ks.load(new FileInputStream(f), passphrase);
                tmf.init(ks);
                ctx.init(null,tmf.getTrustManagers(), null);
                
                sf = ctx.getSocketFactory();
                return sf;
            } catch (Exception e) {
                log.severe("Error defining a secure socket factory!\n"
                +e.getLocalizedMessage());
                e.printStackTrace();
            }
        } else {
            return SocketFactory.getDefault();
        }
        return null;
    }
    
    public boolean connectToServer( String s) {
        setServerHostAndPort( s, 0);
        return connectToServer();
    }
    
    public boolean connectToServer( int p) {
        setServerHostAndPort( null, p);
        return connectToServer();
    }
    
    public boolean connectToServer( String s, int p) {
        setServerHostAndPort( s, p);
        return connectToServer();
    }
    
/* To connect to a server, you can do the following:
 *
 * Thread ctst=new Thread("ConnectToServerThread") {
 *      public void run() {
 *          connectedToServer=connectToServer();
 *          }
 *      };
 * ctst.start();
 *
 */
    public boolean connectToServer() {
        boolean rv=false;
//        updateStatusText("Connecting", "Opening channel to server...");
        app.appendStatusText("Connecting", "Opening channel to server...");
        long st=System.currentTimeMillis();
        try {
            if (!CommSock.UseDefaultSocketFactory) {
                app.appendStatusText("Connecting", "Using default socket factory");
                if (CommSock.SecureConnection) {
                    app.appendStatusText("Connecting", "Creating secure socket to "+serverHost+":"+serverPort+"...");
                    serverChannel
                    //            = java.nio.channels.SocketChannel.open(new java.net.InetSocketAddress(serverHost,serverPort));
                    //            = SSLSocketFactory.getDefault().createSocket(serverHost, serverPort);
                    = getSocketFactory(app, "TLS").createSocket(serverHost, serverPort);
                } else {
                    // TBD: don't use default
                    app.appendStatusText("Connecting", "Creating non-secure socket to "+serverHost+":"+serverPort+"...");
                    serverChannel
                    = SocketFactory.getDefault().createSocket(serverHost, serverPort);
                }
            } else {
                app.appendStatusText("Connecting", "Using custom socket factory\n");
                if (CommSock.SecureConnection) {
                    app.appendStatusText("Connecting", "Creating secure socket to "+serverHost+":"+serverPort+"...\n");
                    // not sure default makes sense
                    serverChannel = SSLSocketFactory.getDefault()
                    .createSocket(serverHost, serverPort);
                } else {
                    // TBD: don't use default
                    app.appendStatusText("Connecting", "Creating non-secure socket to "+serverHost+":"+serverPort+"...");
                    serverChannel = SocketFactory.getDefault()
                    .createSocket(serverHost, serverPort);
                }
            }
        } catch( java.io.IOException e ) {
            //            System.err.println("Error while opening channel to server!");
            //            System.err.println(e.getMessage());
            log.severe("Error while opening channel to server!\n"
            +e.getMessage());
            e.printStackTrace();
//            updateStatusText("Connecting", "Connection to server failed!  "
            app.appendStatusText("Connecting", "Connection to server failed!  \n"
            +e.getMessage());
        }
        st-=System.currentTimeMillis();
        //            System.err.println("Socket creation took "+(-st)+" ms");
        log.finer("Socket creation took "+(-st)+" ms");
        app.appendStatusText("Connecting", "Socket creation took "+(-st)+" ms\n");
        if (serverChannel!=null) {
            try {
                //                serverChannel.configureBlocking(false);
                serverChannel.setSoLinger(false,0); // TBD: make tunable
                serverChannel.setSoTimeout(CommSock.SocketTimoutMs);
                // IPTOS_RELIABILITY | IPTOS_LOWDELAY = 0x4 + 0x10
                serverChannel.setTrafficClass(20);
                serverChannel.setKeepAlive(true);
            } catch ( java.io.IOException e ) {
                //                System.err.println("Error while unblocking channel!");
                //                System.err.println(e.getMessage());
                log.severe("Error while unblocking channel!\n"
                +e.getMessage());
                e.printStackTrace();
//                updateStatusText("Connecting", "Connection to server failed!  "
                app.appendStatusText("Connecting", "Connection to server failed!  \n"
                +e.getMessage());
                try {
                    serverChannel.close();
                } catch ( java.io.IOException se ) {
                    //                    System.err.println("Secondary error while closing channel!");
                    //                    System.err.println(se.getMessage());
                    log.severe("Secondary error while closing channel!"
                    +se.getMessage());
                    se.printStackTrace();
//                    updateStatusText("Connecting", "Connection to server failed!  "
                    app.appendStatusText("Connecting", "Connection to server failed!  \n"
                    +se.getMessage());
                } finally {
                    serverChannel=null;
                }
            }
        }
        
        if (serverChannel!=null) {
//            updateStatusText("Connecting", "Allocating buffers for channel...");
            app.appendStatusText("Connecting", "Allocating buffers for channel...\n");
            //            serverSocket=new ClientServerSocket(newSocket(serverChannel));
            st=System.currentTimeMillis();
            serverSocket=new ClientServerSocket(serverChannel);
            st-=System.currentTimeMillis();
            //            System.err.println("Buffer allocation took "+(-st)+" ms");
            log.finer("Buffer allocation took "+(-st)+" ms");
            app.appendStatusText("Connecting", "Buffer allocation took "+(-st)+" ms\n");
        }
        
        if (CommSock.SecureConnection) {
            app.appendStatusText("Connecting", "Initiating socket security...\n");
            st=System.currentTimeMillis();
            try {
                ((SSLSocket)serverSocket.getSock()).startHandshake();
            } catch(IOException e) {
                //                System.err.println("Error while negotiating secure connection!");
                //                System.err.println(e.getMessage());
                log.severe("Error while negotiating secure socket!"
                +e.getMessage());
                app.appendStatusText("Connecting", "Error while negotiating secure socket!\n");
        }
        
        if ((serverSocket!=null) && (CommSock.SecureConnection)) {
//            updateStatusText("Connecting", "Initiating secure connection...");
//            app.appendStatusText("Connecting", "Initiating secure connection...\n");
//                e.printStackTrace();
//                updateStatusText("Connecting", "Connection to server failed!  "
                app.appendStatusText("Connecting", "Secure socket to server failed!  \n");
//                +e.getMessage());
                serverSocket.close();
                serverSocket=null;
                serverChannel=null;
            }
            st-=System.currentTimeMillis();
            //            System.err.println("Secure connection negotiation took "+(-st)+" ms");
            log.fine("Secure socket negotiation took "+(-st)+" ms");
            app.appendStatusText("Connecting", "Socket security negotiation took \"+(-st)+\" ms\"\n");
        }
        if (serverSocket!=null) {
//            updateStatusText("Connecting", "Initiating connection protocol...");
            app.appendStatusText("Connecting", "Initiating connection protocol...\n");
            StringBuffer sb1=new StringBuffer();
            CommElement ne;
            st=System.currentTimeMillis();
            // should timeout here and inform app via callback
            if (serverSocket.recv(0)) {
                st-=System.currentTimeMillis();
                //            System.err.println("Initial recieve took "+(-st)+" ms");
                app.appendStatusText("Connecting", "Initial recieve took \"+(-st)+\" ms\n");
                log.finer("Initial recieve took "+(-st)+" ms");
                ne=CommElement.nextElement(serverSocket.getCommBuff());
                if (ne==null || ne.tag!=CommTrans.CommTagTransAck) {
                    //                System.err.println("Login protocol error!");
                    //                System.err.println("Expected '"+CommTrans.CommTagTransAck+"'");
                    //                System.err.println("Got '"+ne+"'");
                    log.severe("Connection protocol error!\nExpected '"
                    +CommTrans.CommTagTransAck+"'\nGot "+((ne!=null)?"'"+ne.toString()+"'":CommElement.displayByteBuffer(serverSocket.getCommBuff())));
//                    updateStatusText("Connecting", "Connection to server failed with login protocol error!");
                    app.appendStatusText("Connecting", "Connection to server failed during connection protocol!\n");
                } else {
//                    updateStatusText("Connecting", "Recieved initial acknowledgement...");
                    app.appendStatusText("Connecting", "Recieved initial acknowledgement...\n");
                    ne=CommElement.nextElement(serverSocket.getCommBuff());
                    if (ne==null || ne.tag!=CommTrans.CommTagTransVers) {
                        //                    System.err.println("Login protocol error!");
                        //                    System.err.println("Expected '"+CommTrans.CommTagTransVers+"'");
                        //                    System.err.println("Got '"+ne+"'");
                        log.severe("Connection protocol error!\nExpected '"
                        +CommTrans.CommTagTransVers+"'\nGot '"+ne+"'");
//                        updateStatusText("Connecting", "Connection to server failed with login protocol error!");
                        app.appendStatusText("Connecting", "Connection to server failed during connection protocol version exchange!\n");
                    } else {
                        byte v=ne.pByte.get();
                        if (v!=CommTrans.CommTransVersion) {
                            //                        System.err.println("Login protocol error!");
                            //                        System.err.println("Expected protocol version '"+CommTrans.CommTransVersion+"'");
                            //                        System.err.println("Got '"+v+"'");
                            log.severe("Connection protocol error!\nExpected protocol version '"
                            +CommTrans.CommTransVersion+"'\nGot '"+v+"'");
//                            updateStatusText("Connecting", "Connection to server failed with login protocol error!");
                            app.appendStatusText("Connecting", "Connection to server failed - expected connection protocol version!\n");
                        } else {
                            app.appendStatusText("Connecting", "Recieved transmission protocol version ID of '"+v+"'...\n");
//                            updateStatusText("Connecting", "Recieved transmission protocol version ID of '"+v+"'...");
                            ne=CommElement.nextElement(serverSocket.getCommBuff());
                            if (ne==null || (ne.tag!=CommTrans.CommTagHello && ne.tag!=CommTrans.CommTagTransNakConn)) {
                                //                            System.err.println("Login protocol error!");
                                //                            System.err.println("Expected '"+CommTrans.CommTagHello+"'");
                                //                            System.err.println("Got '"+ne+"'");
                                log.severe("Connection protocol error!\nExpected '"
                                +CommTrans.CommTagHello+"'\nGot '"+ne+"'");
//                                updateStatusText("Connecting", "Connection to server failed with login protocol error!");
                                app.appendStatusText("Connecting", "Connection to server failed during connection protocol - expected Hello or NakConn!\n");
                            } else {
                                if (ne.tag==CommTrans.CommTagTransNakConn) {
                                    //                                System.err.println("Got negative connect of '"+ne+"'");
                                    //                                System.err.println("Got negative connect of '"+ne+"'");
                                    //                                System.err.flush();
                                    log.fine("Got negative connect of '"+ne+"'");
//                                    updateStatusText("Welcome",ne.pString);
                                    app.appendStatusText("Welcome",ne.pString+"\n");
                                } else {
                                    //                                System.err.println("Got hello of '"+ne+"'");
                                    //                                System.err.flush();
                                    //                                System.err.println("Got hello of '"+ne+"'");
                                    //                                System.err.flush();
                                    log.fine("Got hello of '"+ne+"'");
//                                    updateStatusText("Welcome",ne.pString);
                                    app.appendStatusText("Welcome","Got Hello of "+ne.pString+"\n");
                                    ne=CommElement.nextElement(serverSocket.getCommBuff());
                                    if (ne==null || ne.tag!=CommTrans.CommTagTransLoginReq) {
                                        //                                    System.err.println("Login protocol error!");
                                        //                                    System.err.println("Expected '"+CommTrans.CommTagTransLoginReq+"'");
                                        //                                    System.err.println("Got '"+ne+"'");
                                        log.severe("Connection protocol error!\nExpected '"
                                        +CommTrans.CommTagTransLoginReq+"'\nGot '"+ne+"'");
//                                        updateStatusText("Connecting", "Connection to server failed with login protocol error!");
                                        app.appendStatusText("Welcome", "Connection to server failed with login protocol error!\n");
                                    } else {
                                        loginReqsFlag=ne.pByte.get();
                                        while (serverSocket.getCommBuff().hasRemaining()) {
                                            ne=CommElement.nextElement(serverSocket.getCommBuff());
                                            if (ne==null)
                                                break;
                                            sb1.append(ne.toString()+" ");
                                        }
                                        // if (sbl.length()>0)
//                                        updateStatusText("Connecting", sb1.toString());
                                        app.appendStatusText("Welcome", sb1.toString()+"\n");
                                        serverSocket.clearCommBuff();
                                        rv=serverSocket.put(CommTrans.CommTagTransAck).send();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        connectedToServer=rv;
        return rv;
    }
    
/* To log in to a server, you can do the following:
 *
 * Thread ctst=new Thread("LogInToServerThread") {
 *      public void run() {
 *          loggedInToServer=loginToServer();
 *          }
 *      };
 * ctst.start();
 *
 */
    public boolean loginToServer() {
        boolean rv=false;
        StringBuffer sb1=new StringBuffer();
        CommElement ne;
        //        log.severe("!severSocket="+serverSocket+" reports it is "
        //        +(((serverSocket !=null) && (serverSocket.isConnected()))?"":"NOT ")
        //        +"connected\nand var connectedToServer is "+connectedToServer);
        if ((serverSocket != null)
        && (serverSocket.isConnected())
        && (connectedToServer)) {
//            updateStatusText("Connecting", "Initiating login protocol...");
            app.appendStatusText("Connecting", "Initiating login protocol...\n");
            //            log.severe("!!severSocket="+serverSocket+" reports it is "
            //            +(((serverSocket !=null) && (serverSocket.isConnected()))?"":"NOT ")
            //            +"connected\nand var connectedToServer is "+connectedToServer);
            serverSocket.clearCommBuff();
            //            log.severe("!!!severSocket="+serverSocket+" reports it is "
            //            +(((serverSocket !=null) && (serverSocket.isConnected()))?"":"NOT ")
            //            +"connected\nand var connectedToServer is "+connectedToServer);
            serverSocket.put(CommTrans.CommTagLoginAttempt,loginReqsFlag);
            //            log.severe("!!!!severSocket="+serverSocket+" reports it is "
            //            +(((serverSocket !=null) && (serverSocket.isConnected()))?"":"NOT ")
            //            +"connected\nand var connectedToServer is "+connectedToServer);
            if (loginReqsFlag==CommTrans.LoginRequiresNameOnly)
//                updateStatusText("Connecting", "Login requires name only.");
                app.appendStatusText("Connecting", "Login requires name only.\n");
            else if (loginReqsFlag==CommTrans.LoginRequiresPasswordOnly)
//                updateStatusText("Connecting", "Login requires password only.");
                app.appendStatusText("Connecting", "Login requires password only.\n");
            else if (loginReqsFlag==CommTrans.LoginRequiresNameAndPassword)
//                updateStatusText("Connecting", "Login requires name and password.");
                app.appendStatusText("Connecting", "Login requires name and password.\n");
            else if (loginReqsFlag==CommTrans.LoginRequiresNameAndPassword)
//                updateStatusText("Connecting", "Login requires no authentication.");
                app.appendStatusText("Connecting", "Login requires no authentication.\n");
//            setUserLogin(serverSocket,loginReqsFlag);
            app.setUserLogin(serverSocket,loginReqsFlag);
            serverSocket.send();
            
            // should timeout here and inform app via callback
            if (serverSocket.recv(0)) {
                ne=CommElement.nextElement(serverSocket.getCommBuff());
                if (ne==null || ne.tag!=CommTrans.CommTagTransAck) {
                    //                                        System.err.println("Login protocol error!");
                    //                                        System.err.println("Expected '"+CommTrans.CommTagTransAck+"'");
                    //                                        System.err.println("Got '"+ne+"'");
                    log.severe("Login protocol error!\nExpected '"
                    +CommTrans.CommTagTransAck+"'\nGot '"+ne+"'");
//                    updateStatusText("Connecting", "Connection to server failed with login protocol error!");
                    app.appendStatusText("Connecting", "Connection to server failed with login protocol error!\n");
                } else {
                    ne=CommElement.nextElement(serverSocket.getCommBuff());
                    if (ne==null || ne.tag!=CommTrans.CommTagWelcomeMsg) {
                        //                                            System.err.println("Login protocol error!");
                        //                                            System.err.println("Expected '"+CommTrans.CommTagWelcomeMsg+"'");
                        //                                            System.err.println("Got '"+ne+"'");
                        log.severe("Login protocol error!\nExpected '"
                        +CommTrans.CommTagWelcomeMsg+"'\nGot '"+ne+"'");
//                        updateStatusText("Connecting", "Connection to server failed with login protocol error!");
                        app.appendStatusText("Connecting", "Connection to server failed with login protocol error!\n");
                    } else {
                        //                                            System.err.println("Got welcome of '"+ne+"'");
                        //                                            System.err.flush();
                        //                                            System.err.println("Got welcome of '"+ne+"'");
                        //                                            System.err.flush();
                        log.fine("Got welcome of '"+ne+"'");
//                        updateStatusText("Welcome",ne.pString);
                        app.appendStatusText("Welcome",ne.pString+"\n");
                        rv=true;
                        while (serverSocket.getCommBuff().hasRemaining()) {
                            ne=CommElement.nextElement(serverSocket.getCommBuff());
                            if (ne==null)
                                break;
                            //                                                System.err.println("Got additional of '"+ne+"'");
                            //                                                System.err.flush();
                            log.fine("Got additional of '"+ne+"'");
                            //                                                if (sbl.length()>0)
                            sb1.append(ne.toString()+" ");
                        }
//                        updateStatusText("Connecting", sb1.toString());
                        app.appendStatusText("Connecting", sb1.toString()+"\n");
                        serverSocket.clearCommBuff();
                    }
                }
            }
        }
        serverSocket.setLoggedIn(rv);
        loggedInToServer=rv;
        return rv;
    }
    
    class ConnectionMonitor extends Thread {
        
        //        Client client;
        Client client;
        
        //        public ConnectionMonitor(Client c) {
        public ConnectionMonitor(Client c) {
            client=c;
        }
        
        public void run() {
            for (;;) {
                if (client.connectedToServer && client.loggedInToServer) {
                    client.serverSocket.lock();
                    try {
                        if (client.serverSocket.recv(CommSock.ReadSocketTimoutMs)
                        && client.serverSocket.isRecvComplete()) {
                            //                            if (client.serverSocket.getCommBuff().hasRemaining()) {
                            // process it
                            CommElement e=CommElement.nextElement(client.serverSocket.getCommBuff());
                            if (e.tag==CommTrans.CommTagTransHB) {
                                // send HB reply - nothing else with a HB, so clear ok
                                client.serverSocket.clearCommBuff();
                                client.serverSocket.put(CommTrans.CommTagTransHBAck).send();
//                                updateStatusText("Heartbeat", "");
                                app.appendStatusText("Heartbeat", "\n");
                            }
                            else // some other message - pass to client code
                            {
                                client.serverSocket.rewindCommBuff();
//                                if (!client.acceptRecievedMsg(client.serverSocket)) {
                                if (!client.app.acceptRecievedMsg(client.serverSocket)) {
                                    log.severe("Recieved unparseable message!  Buffer is:\n"
                                    +CommElement.displayByteBuffer(client.serverSocket.getCommBuff()));
                                    client.serverSocket.clearCommBuff();
                                    client.serverSocket.put(CommTrans.CommTagTransNakConn,"Recieved unparseable message!").send();
                                    client.serverSocket.close();
                                    client.serverSocket=null;
                                    client.connectedToServer=false;
                                    client.loggedInToServer=false;
//                                    client.serverConnectionAborted();
                                    client.app.serverConnectionAborted();
                                }
                            }
                            //                            }
                        } else { // bad recv or timeout
                            if (!client.serverSocket.isConnected()) {
                                //                        System.err.println("Recieve error!!");
                                //                        System.err.flush();
                                log.severe("Recieve error!!");
                                client.serverSocket.close();
                                client.connectedToServer=false;
                                client.loggedInToServer=false;
//                                client.serverConnectionAborted();
                                client.app.serverConnectionAborted();
                            }
                        }
                    } finally {
                        client.serverSocket.unlock();
                    }
                }
                //yield();  // needed?  maybe sleep?
                try {
                    sleep(CommSock.ClientSocketLoopInactivePauseMs);
                } catch (InterruptedException e) {
                    log.severe("Unexpected interruption!\n"+e.getMessage());
                    log.throwing(this.getClass().getName(), "run", e);
                }
            }
        }
    }
}
