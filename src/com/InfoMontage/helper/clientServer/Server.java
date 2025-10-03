/*
 * Server.java
 *
 * Created on July 1, 2001, 8:14 PM
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
import java.io.*;
import java.net.*;
import javax.net.*;
import javax.net.ssl.*;
import java.nio.channels.*;
import java.security.*;
import javax.security.cert.*;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public class Server {
    
    public static final String ServerCodeVersion = "0.8.6.2";
    
    final ServerApp app;
    
    //    com.InfoMontage.helper.clientServer.Server.ClientSocketHashtable clientSockets=null;
    //    com.InfoMontage.helper.clientServer.Server.ClientSocketHashtable clientSocketsToAdd=null;
    //    com.InfoMontage.helper.clientServer.Server.ClientListener clientSocketListener=null;
    public com.InfoMontage.helper.clientServer.Server.ClientSocketHashtable clientSockets=null;
    com.InfoMontage.helper.clientServer.Server.ClientSocketHashtable clientSocketsToAdd=null;
    java.util.LinkedList clientSocketsConnecting=new java.util.LinkedList();
    com.InfoMontage.helper.clientServer.Server.ClientListener clientSocketListener=null;
    java.util.Vector users=new java.util.Vector();
    //    com.InfoMontage.helper.clientServer.Server.ClientChannelProcessor clientChannelProcessor=null;
    com.InfoMontage.helper.clientServer.Server.ClientChannelProcessor clientChannelProcessor=null;
    ThreadGroup clientConnectionThreadGroup=null;
    ThreadGroup messageProcessorThreadGroup=null;
    java.util.List activeClientConnectionThreadsList=null;
    java.util.List inactiveClientConnectionThreadsList=null;
    java.util.List activeMessageProcessorThreadsList=null;
    java.util.List inactiveMessageProcessorThreadsList=null;
    
    // thread priorities
    static final int clientSocketListenerThreadPriority=Thread.NORM_PRIORITY;
    static final int clientChannelProcessorThreadPriority=Thread.MAX_PRIORITY;
    static final int clientChannelConnectionThreadPriority=Thread.NORM_PRIORITY;
    static final int clientMessageProcessorThreadPriority
    =(Thread.NORM_PRIORITY+Thread.MAX_PRIORITY)/2;
    
    // stats
    private class ClientServerServerStats {
        protected int clientsConnecting=0;
        protected int clientsConnected=0;
        protected int clientsLoggingIn=0;
        protected int clientsLoggedIn=0;
        protected int clientsForciblyDisconnected=0;
        protected int clientHeartbeatsPending=0;
        protected int clientHeartbeatsMissedCurrent=0;
        protected int clientHeartbeatsMissedTotal=0;
    }
    
    private ClientServerServerStats stats=new ClientServerServerStats();
    
//    protected abstract void updateClientsConnecting(int n);
//    protected abstract void updateClientsConnected(int n);
//    protected abstract void updateClientsLoggingIn(int n);
//    protected abstract void updateClientsLoggedIn(int n);
//    protected abstract void updateClientsForceDisconnected(int n);
//    protected abstract void updateHbsPending(int n);
//    protected abstract void updateHbsFailedCurrent(int n);
//    protected abstract void updateHbsFailedTotal(int n);
//    protected abstract byte LoginReqs();
//    //    protected abstract String HelloMessage(SocketChannel sc);
//    protected abstract String HelloMessage(Socket s);
//    protected abstract String WelcomeMessage(UserData u);
//    protected abstract boolean ProcessClientMessage(ClientServerSocket css);
    
    private void internalUpdateClientsConnecting(int d) {
        synchronized(stats) {
            stats.clientsConnecting+=d;
//            updateClientsConnecting(stats.clientsConnecting);
            app.updateClientsConnecting(stats.clientsConnecting);
        }
    }
    
    private void internalUpdateClientsConnected() {
        synchronized(stats) {
            synchronized(clientSockets) { stats.clientsConnected=clientSockets.size(); }
            synchronized(clientSocketsToAdd) {
                stats.clientsConnected+=clientSocketsToAdd.size();
            }
            synchronized(clientSocketsConnecting) {
                stats.clientsConnected+=clientSocketsConnecting.size();
            }
//            updateClientsConnected(stats.clientsConnected);
            app.updateClientsConnected(stats.clientsConnected);
        }
    }
    
    private void internalUpdateClientsLoggingIn(int d) {
        synchronized(stats) {
            stats.clientsLoggingIn+=d;
//            updateClientsLoggingIn(stats.clientsLoggingIn);
            app.updateClientsLoggingIn(stats.clientsLoggingIn);
        }
    }
    
    private void internalUpdateClientsLoggedIn(int d) {
        synchronized(stats) {
            stats.clientsLoggedIn+=d;
//            updateClientsLoggedIn(stats.clientsLoggedIn);
            app.updateClientsLoggedIn(stats.clientsLoggedIn);
        }
    }
    
    private void internalUpdateClientsForceDisconnected() {
        synchronized(stats) {
            stats.clientsForciblyDisconnected++;
//            updateClientsForceDisconnected(stats.clientsForciblyDisconnected);
            app.updateClientsForceDisconnected(stats.clientsForciblyDisconnected);
            internalUpdateClientsConnected();
        }
    }
    
    private void internalUpdateHbsPending(int d) {
        synchronized(stats) {
            stats.clientHeartbeatsPending+=d;
//            updateHbsPending(stats.clientHeartbeatsPending);
            app.updateHbsPending(stats.clientHeartbeatsPending);
        }
    }
    
    private void internalUpdateHbsFailedCurrent(int d) {
        synchronized(stats) {
            stats.clientHeartbeatsMissedCurrent+=d;
//            updateHbsFailedCurrent(stats.clientHeartbeatsMissedCurrent);
            app.updateHbsFailedCurrent(stats.clientHeartbeatsMissedCurrent);
        }
    }
    
    private void internalIncrementHbsFailedTotal() {
        synchronized(stats) {
            stats.clientHeartbeatsMissedTotal++;
//            updateHbsFailedTotal(stats.clientHeartbeatsMissedTotal);
            app.updateHbsFailedTotal(stats.clientHeartbeatsMissedTotal);
        }
    }
    
    java.nio.ByteBuffer stdErrBuff=java.nio.ByteBuffer.allocate(CommSock.StdErrChannelBufferCapacity);
    java.nio.ByteBuffer stdTmpBuff=java.nio.ByteBuffer.allocate(CommSock.ClientChannelBufferCapacity);
    java.util.HashSet byteBufferPool=null;
    
    public class ClientSocketHashtable extends java.util.Hashtable {
        
        public ClientSocketHashtable(int c) {
            super(c);
        }
        protected UserData user=null;
        
        public synchronized UserData put(UserData u, ClientServerSocket s) {
            UserData oldVal=(UserData)super.put(u,s);
            //            // update numClients form field
            //            clientsConnected++;
            return oldVal;
        }
        
        public synchronized ClientServerSocket get(UserData u) {
            return (ClientServerSocket)super.get(u);
        }
    }
    
    private static ServerSocketFactory getServerSocketFactory(Object a, String type) {
        if (type.equals("TLS") || type.equals("SSL")) {
            // why does this take so LONG!!?
            SSLServerSocketFactory ssf = null;
            try {
                // set up key manager to do server authentication
                SSLContext ctx;
                KeyManagerFactory kmf;
                KeyStore ks;
                char[] passphrase = "passphrase".toCharArray();
                
                //                ctx = SSLContext.getInstance("TLS");
                ctx = SSLContext.getInstance(type);
                //		kmf = KeyManagerFactory.getInstance("SunX509");
                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                ks = KeyStore.getInstance("JKS");
                
                File f=new File(URI.create("file:///"+a.getClass().getResource("CSKSKeys").getPath()));
                ks.load(new FileInputStream(f), passphrase);
                kmf.init(ks, passphrase);
                ctx.init(kmf.getKeyManagers(), null, null);
                
                ssf = ctx.getServerSocketFactory();
                return ssf;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return ServerSocketFactory.getDefault();
        }
        return null;
    }
    
    class ClientListener extends Thread {
        java.nio.channels.ServerSocketChannel ssc=null;
        //        java.net.ServerSocket ss=null;
        static final boolean debugLogging=true;
        
        /**
         * @param port
         * @throws IOException
         */
        public ClientListener(int port) throws IOException {
            super("ClientListenerThread");
            ssc=java.nio.channels.ServerSocketChannel.open();
            java.net.ServerSocket ss=ssc.socket();
            //            ServerSocketFactory ssf=null;
            //            if (!CommSock.UseDefaultSocketFactory)
            //                if (CommSock.SecureConnection)
            //                    ssf = getServerSocketFactory(app, "TLS");
            //                else
            //                    // TBD: don't use default...
            //                    ssf = ServerSocketFactory.getDefault();
            //            else
            //                if (CommSock.SecureConnection)
            //                    // not sure default makes sense
            //                    ssf = SSLServerSocketFactory.getDefault();
            //                else
            //                    ssf = ServerSocketFactory.getDefault();
            //            ss = ssf.createServerSocket(port);
            //            if (CommSock.SecureConnection) {
            //                if (debugLogging) { // TBD: use logging
            //                    String[] sa;
            //                    System.out.println("Enabled Protocols:");
            //                    sa=((SSLServerSocket)ss).getEnabledProtocols();
            //                    for (int i=0; i<sa.length; i++)
            //                        System.out.println("["+(i+1)+"] "+sa[i]);
            //                    System.out.println("Enabled CipherSuites:");
            //                    sa=((SSLServerSocket)ss).getEnabledCipherSuites();
            //                    for (int i=0; i<sa.length; i++)
            //                        System.out.println("["+(i+1)+"] "+sa[i]);
            //                }
            //                ((SSLServerSocket)ss).setNeedClientAuth(CommSock.AuthenticateClient);
            //                ((SSLServerSocket)ss).setEnableSessionCreation(true);
            //                ((SSLServerSocket)ss).setUseClientMode(false);
            //            }
            ss.bind(new java.net.InetSocketAddress(port));
            ssc.configureBlocking(false);
        }
        
        public void run() {
            java.nio.channels.SocketChannel sc=null;
            //            Socket s = null;
            boolean didSomething=false;
            while(true) {
                didSomething=false;
                try {
                    // blocks until connection made
                    sc=ssc.accept();
                    //                    s=ss.accept();
                } catch ( IOException e ) {
                    System.err.println("Error while listening for clients!");
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    sc=null;
                    //                    s=null;
                }
                if (sc!=null) {
                    //                if (s!=null) {
                    didSomething=true;
                    try {
                        sc.configureBlocking(false);
                        //                        s.setSoLinger(false,0); // TBD: make tunable
                        //                        s.setSoTimeout(CommSock.SocketTimoutMs);
                        // IPTOS_RELIABILITY | IPTOS_LOWDELAY = 0x4 + 0x10
                        //                        s.setTrafficClass(20);
                        //                        s.setKeepAlive(true);
                    } catch ( IOException e ) {
                        System.err.println("Error while unblocking client channel!");
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                        try {
                            sc.close();
                            //                            s.close();
                        } catch ( IOException se ) {
                            System.err.println("Secondary error while closing client channel!");
                            System.err.println(se.getMessage());
                            se.printStackTrace();
                        } finally {
                            sc=null;
                            //                            s=null;
                        }
                    }
                }
                
                // offload the rest to another thread
                // this one should only do listening, not processing
                if (sc!=null) {
                    //                if (s!=null) {
                    // internalUpdateClientsConnecting(1);
                    com.InfoMontage.helper.clientServer.Server.ClientConnector tcct=null;
                    //                    com.InfoMontage.helper.clientServer.Server.ClientConnector tcct=null;
                    // this next is questionable...
                    //                    while (inactiveClientConnectionThreadsList.isEmpty())
                    //                        yield();
                    // possibly just create one each time?
                    while (tcct==null)
                        tcct=(com.InfoMontage.helper.clientServer.Server.ClientConnector)inactiveClientConnectionThreadsList.remove(0);
                    activeClientConnectionThreadsList.add(tcct);
                    tcct.setSocketChannel(sc);
                    //                    tcct.setSocket(s);
                    // long st=System.currentTimeMillis();
                    tcct.start();
                    // st-=System.currentTimeMillis();
                    // System.err.println("Thread start took "+(-st)+" ms");
                    // new Thread(new ClientConnector(sc)).start();
                }
                // bah - we sleep in accept(), so forget this...
                //                try { // this may not be needed, but unpaused tight loops...?
                //                                        if (didSomething) sleep(CommSock.ClientSocketLoopActivePauseMs);
                //                    else sleep(CommSock.ClientSocketLoopInactivePauseMs);
                //                } catch (InterruptedException e) {
                //                    System.err.println("Unexpected interruption!");
                //                    System.err.println(e.getMessage());
                //                    e.printStackTrace();
                //                }
                sc=null;
                //                s=null;
            }
        }
    }
    
//    protected abstract UserData ProcessLogin(ClientServerSocket s);
    
    class ClientConnector extends Thread {
        java.nio.channels.SocketChannel sc;
        //        Socket s;
        
        public ClientConnector(ThreadGroup tg) {
            super(tg,"");
            sc=null;
            //            s=null;
        }
        
        public ClientConnector(ThreadGroup tg, java.nio.channels.SocketChannel s) {
            //        public ClientConnector(ThreadGroup tg, Socket is) {
            super(tg,"");
            sc=s;
            //            s=is;
        }
        
        public void setSocketChannel(java.nio.channels.SocketChannel s) {
            //        public void setSocket(Socket is) {
            sc=s;
            //            s=is;
        }
        
        public void run() {
            internalUpdateClientsConnecting(1);
            if (sc!=null) {
                //            if (s!=null) {
                // do initial connection protocol
                if (clientSockets.size() < CommSock.MaxClientSocketCapacity) {
                    long st=System.currentTimeMillis();
                    ClientServerSocket css=new ClientServerSocket(sc);
                    st-=System.currentTimeMillis();
                    System.err.println("ClientServerSocket creation took "+(-st)+" ms");
                    System.err.flush();
                    boolean validLoginToAdd = false;
                    boolean validLogin = false;
                    String loginNakReason = null;
                    if (css.sc!=null) {
                        //                    if (css!=null && css.getSock()!=null) {
                        CommElement ne;
                        //                        css.put(CommTrans.CommTagTransAck)
                        //                        .put(CommTrans.CommTagTransVers,CommTrans.CommTransVersion)
                        //                        .put(CommTrans.CommTagHello,HelloMessage(sc.socket()))
                        //                        .put(CommTrans.CommTagTransLoginReq,LoginReqs());
                        css.put(CommTrans.CommSeqBegin.buildBuffer( new Object[] {
                            Byte.valueOf(CommTrans.CommTransVersion),
                            //                            HelloMessage(s),
                            //                            new Byte(LoginReqs())
                            app.HelloMessage(sc.socket()),
                            Byte.valueOf(app.LoginReqs())
                        } ) );
                        if (css.send())
                            if (css.recv(CommTrans.ClientAcknowledgementTimeout)) {
                                // best have gotten an Ack...
                                ne=CommElement.nextElement(css.getCommBuff());
                                if (ne==null || ne.tag!=CommTrans.CommTagTransAck) {
                                    css.clearCommBuff();
                                    css.put(CommTrans.CommTagTransNakConn
                                    ,"Connection protocol error").send();
                                    css.close();
                                    css=null;
                                    //                                    internalUpdateClientsConnecting(-1);
                                } else {
                                    // proper connection protocol...
                                    // next, the client must send login msg
                                    // which is processed by a MessageProcessor
                                    css.clearCommBuff();
                                    synchronized (clientSocketsConnecting) {
                                        clientSocketsConnecting.add(css);
                                    }
                                }
                            } else { // timed out
                                css.close();
                                css=null;
                                //                                internalUpdateClientsConnecting(-1);
                            }
                        else { // send failed
                            css.close();
                            css=null;
                            //                            internalUpdateClientsConnecting(-1);
                        }
                    } else { // null socket
                        css.close();
                        css=null;
                        //                        internalUpdateClientsConnecting(-1);
                    }
                } else { // too many client sockets
                    //                    CommTrans.CommTagTransAck
                    //                    .encode(stdTmpBuff);
                    //                    CommTrans.CommTagTransVers
                    //                    .encode(stdTmpBuff,CommTrans.CommTransVersion);
                    //                    CommTrans.CommTagTransNakConn
                    //                    .encode(stdTmpBuff,"Maximum of "
                    //                    +CommSock.MaxClientSocketCapacity
                    //                    +" clients has been reached, please try again later.");
                    //                    stdTmpBuff.put(CommTrans.CommDelimiterByte);
                    stdTmpBuff.put(CommTrans.CommSeqMaxConnections.buildBuffer(new Object[] {
                        "Maximum of "+CommSock.MaxClientSocketCapacity
                        +" clients has been reached, please try again later." } ) )
                        .put(CommTrans.CommDelimiterByte);
                        try {
                            sc.write(stdTmpBuff);
                            //                            s.getOutputStream()
                            //                            .write(stdTmpBuff.array(),stdTmpBuff.arrayOffset(),stdTmpBuff.remaining());
                        } catch ( IOException e ) {
                            System.err.println("Error while writing client channel!");
                            System.err.println(e.getMessage());
                            e.printStackTrace();
                            sc=null;
                        }
                        stdTmpBuff.clear();
                        //                       internalUpdateClientsConnecting(-1);
                }
                sc=null;
            }
            internalUpdateClientsConnecting(-1);
            internalUpdateClientsConnected();
            // return to thread pool
            activeClientConnectionThreadsList.remove(this);
            inactiveClientConnectionThreadsList.add(this);
        }
    }
    
    class ClientChannelProcessor extends Thread {
        
        public ClientChannelProcessor() {
            super("ClientChannelProcessorThread");
        }
        
        public void run() {
            java.util.Iterator i;
            // this must be only thread to modify clientSocketCollection
            // and clientSockets
            java.util.Collection clientSocketCollection=clientSockets.values();
            boolean didSomething=false;
            CommElement ne;
            // loop around clients checking for closed connections
            // and assigning threads for actions
            while (true) {
                // each time thru the loop, be sure to check if there are new
                // socket connections to check for messages on
                synchronized (clientSocketsConnecting) {
                    i=clientSocketsConnecting.iterator();
                    while (i.hasNext()) {
                        ClientServerSocket s=(ClientServerSocket)i.next();
                        //                    if (s.sc!=null) {
                        if (s!=null && s.isConnected()) {
                            if (!s.recv(CommSock.PollSocketTimoutMs)) {
                                if (!s.isConnected())
                                    // socket disconnected, remove from list
                                    i.remove();
                                //internalUpdateClientsConnected();
                            } else {
                                // got something!  Send it to processor
                                // maybe just create new one
                                com.InfoMontage.helper.clientServer.Server.ClientMessageProcessor tmpt=null;
                                while (tmpt==null)
                                    tmpt=(com.InfoMontage.helper.clientServer.Server.ClientMessageProcessor)inactiveMessageProcessorThreadsList.remove(0);
                                activeClientConnectionThreadsList.add(tmpt);
                                // tmpt.setByteBuffer(s.commBuff.asReadOnlyBuffer());
                                tmpt.setClientServerSocket(s);
                                tmpt.start();
                            }
                        } else { // socket null or disconnected, remove from list
                            s=null;
                            i.remove();
                            //internalUpdateClientsConnected();
                        }
                    }
                }
                internalUpdateClientsConnected();
                // each time thru the loop, check if there are new sockets to
                // add to the set of logged in clients
                synchronized (clientSocketsToAdd) {
                    if (clientSocketsToAdd.size()>0) {
                        System.err.println("Adding "+clientSocketsToAdd.size()+" connections");
                        clientSockets.putAll(clientSocketsToAdd);
                        clientSocketsToAdd.clear();
                        clientSocketCollection=clientSockets.values();
                        System.err.println("New connection list has "+clientSocketCollection.size()+" connections");
                        System.err.flush();
                    }
                }
                // Now do those already logged in
                i=clientSocketCollection.iterator();
                didSomething=false;
                while (i.hasNext()) {
                    ClientServerSocket s=(ClientServerSocket)i.next();
                    //                    boolean recvComplete=false;
                    //                    if (s.sc!=null) {
                    if (s!=null && s.isConnected()) {
                        didSomething=true;
                        if (!s.recv(CommSock.PollSocketTimoutMs)) {
                            //                                    if (s.sc!=null) { // got nothing - do heartbeat processing
                            if (s.isConnected() && s.isLoggedIn()) { // got nothing - do heartbeat processing
                                // HB timeout passed yet? If not, do nothing...
                                if (s.nextHbTime<=System.currentTimeMillis()) {
                                    // already waiting for Hb?
                                    if (s.numHbAckNeeded>0) {
                                            internalUpdateHbsFailedCurrent(1);
                                            internalIncrementHbsFailedTotal();
                                        if (s.numFailedHbs<CommTrans.MaxFailedHeartbeats) {
                                            s.numFailedHbs+=1;
//                                            internalUpdateHbsFailedCurrent(1);
//                                            internalIncrementHbsFailedTotal();
                                        } else { // Hb failure - loss of connection
                                            s.close();
                                            internalUpdateHbsFailedCurrent(-s.numFailedHbs);
                                            internalUpdateClientsForceDisconnected();
                                        }
                                    }
                                    if (s.isConnected()) {
                                    // request heartbeat response
                                    // clear buffer for use
                                    System.err.println("Sending '"
                                    +CommTrans.CommTagTransHB.toString()
                                    +" to '"+s.getSc().socket().getRemoteSocketAddress().toString()
                                    +"'");
                                    s.clearCommBuff();
                                    if (s.put(CommTrans.CommTagTransHB).send()) {
                                        s.numHbAckNeeded+=1;
                                        internalUpdateHbsPending(1);
                                    } else // didn't get sent, close socket
                                        s.close();
                                    s.nextHbTime=System.currentTimeMillis()+CommTrans.HeartbeatTimeoutMs;
                                }
                                }
                            } // otherwise socket is not connected, cleanup next time
                        } else { // got something!
                            s.nextHbTime=System.currentTimeMillis()+CommTrans.HeartbeatTimeoutMs;
                            internalUpdateHbsFailedCurrent(-s.numFailedHbs);
                            s.numFailedHbs=0;
                            //                            System.err.println("Recieved '"+java.nio.charset.Charset.forName("US-ASCII").decode(s.commBuff.asReadOnlyBuffer())+"'="+s.commBuff.position()+","+s.commBuff.limit()+","+s.commBuff.capacity());
                            // TBD: next bit should be seperate thread
                            // keep reading until end of comm
                            //                                    while (s.sc.isConnected()
                            // should just skip if not yet a complete transmission yet
                            // also should modify ClientServerSocket to prevent need
                            // for knowing internals (commBuff)
                            //                            while (s.isConnected()
                            //                            if (s.isConnected()
                            //                            && (s.getCommBuff().get(s.getCommBuff().limit()-1)
                            //                            !=CommTrans.CommDelimiterByte)) {
                            //                            && s.isRecvComplete()) {
                            //                                s.getCommBuff().position(s.getCommBuff().limit());
                            //                                s.commBuff.limit(s.commBuff.capacity());
                            // following should only be needed if
                            // recv() spins rather than sleeping
                            // when there is nothing to read
                            //                                        try {
                            //                                            sleep(CommSock.ClientSocketReadPauseMs);
                            //                                        } catch (InterruptedException e) {
                            //                                            System.err.println("Unexpected interruption!");
                            //                                            System.err.println(e.getMessage());
                            //                                            e.printStackTrace();
                            //                                        }
                            //                                s.recv(CommSock.ReadSocketTimoutMs);
                            //                                System.err.println("Recieved '"+java.nio.charset.Charset.forName("US-ASCII").decode(s.getCommBuff().asReadOnlyBuffer())+"'="+s.getCommBuff().position()+","+s.getCommBuff().limit()+","+s.getCommBuff().capacity());
                            //                            }
                            //                                    if (s.sc.isConnected()) {
                            if ((s.isRecvComplete())&&(s.isConnected())) {
                                //                                System.err.println("Recieved '"+java.nio.charset.Charset.forName("US-ASCII").decode(s.getCommBuff().asReadOnlyBuffer())+"'="+s.getCommBuff().position()+","+s.getCommBuff().limit()+","+s.getCommBuff().capacity());
                                System.err.println("Recieved '"
                                +CommElement.displayByteBuffer(s.getCommBuff())
                                +" from '"+s.getSc().socket().getRemoteSocketAddress().toString()
                                +"'");
                                s.getCommBuff().mark();
                                ne=CommElement.nextElement(s.getCommBuff());
                                if (ne != null && ne.tag==CommTrans.CommTagTransHBAck) {
                                    System.err.println("Got HB");
                                    // only one HB at a time, and HB can be only thing sent
                                    if (s.numHbAckNeeded>0) {
                                        s.numHbAckNeeded--;
                                        internalUpdateHbsPending(-1);
                                    } else { // wierd - got HB but didn't need it
                                    }
                                    s.clearCommBuff();
                                } else if (ne != null) {
                                    s.getCommBuff().reset();
                                    com.InfoMontage.helper.clientServer.Server.ClientMessageProcessor tmpt=null;
                                    while (tmpt==null)
                                        tmpt=(com.InfoMontage.helper.clientServer.Server.ClientMessageProcessor)inactiveMessageProcessorThreadsList.remove(0);
                                    activeClientConnectionThreadsList.add(tmpt);
                                    // tmpt.setByteBuffer(s.commBuff.asReadOnlyBuffer());
                                    tmpt.setClientServerSocket(s);
                                    tmpt.start();
                                } else {
                                    System.err.println("Recieved unparseable message!  Buffer is:\n"
                                    +CommElement.displayByteBuffer(s.getCommBuff()));
                                    s.clearCommBuff();
                                }
                            } // else recieve failed - just continue
                            // (cleanup will happen next pass thru loop)
                            // or recv not yet complete
                        }
                    } else { // lost channel!
                        System.err.println("Lost connection while reading client channel!");
                        System.err.flush();
                        s.close();
                        internalUpdateHbsPending(-s.numHbAckNeeded);
                        internalUpdateHbsFailedCurrent(-s.numFailedHbs);
                        i.remove();
                        internalUpdateClientsLoggedIn(-1);
                        internalUpdateClientsConnected();
                    }
                    // pause, then keep looping thru client sockets
                    try {
                        sleep(CommSock.ClientSocketLoopActivePauseMs);
                    } catch (InterruptedException e) {
                        System.err.println("Unexpected interruption!");
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                    }
                }
                // pause if did nothing, then start a new loop
                try {
                    if (!didSomething)
                        sleep(CommSock.ClientSocketLoopInactivePauseMs);
                } catch (InterruptedException e) {
                    System.err.println("Unexpected interruption!");
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
                internalUpdateClientsConnected();
            }
        }
    }
    
    class ClientMessageProcessor extends Thread {
        
        //        java.nio.ByteBuffer b;
        ClientServerSocket s;
        
        public ClientMessageProcessor(ThreadGroup tg) {
            super(tg,"");
            //            sc=null;
            //            b=null;
            s=null;
        }
        
        //        public ClientConnector(java.nio.channels.SocketChannel s) {
        //        public void setSocketChannel(java.nio.channels.SocketChannel s) {
        //        public void setByteBuffer(java.nio.ByteBuffer ib) {
        //            //            sc=s;
        //            b=ib;
        //        }
        public void setClientServerSocket(ClientServerSocket is) {
            //            sc=s;
            s=is;
        }
        
        public void run() {
            CommElement ne;
            java.nio.ByteBuffer b;
            boolean validLoginToAdd = false;
            boolean validLogin = false;
            String loginNakReason = null;
            if (s!=null) {
                b=s.getCommBuff().asReadOnlyBuffer();
                ne=CommElement.nextElement(b);
                if (ne==null) {
                    System.err.println("Recieved unparseable message!  Buffer is:\n"
                    +CommElement.displayByteBuffer(s.getCommBuff()));
                    System.err.flush();
                    s.clearCommBuff();
                    s.put(CommTrans.CommTagTransNakConn,"Recieved unparseable message!").send();
                    s.close();
                    s=null;
                } else {
                    if (ne.tag==CommTrans.CommTagLoginAttempt) {
                        if (s.isLoggedIn()) {
                            System.err.println("Relogin attempted!  Buffer is:\n"
                            +CommElement.displayByteBuffer(s.getCommBuff()));
                            System.err.flush();
                            s.clearCommBuff();
                            s.put(CommTrans.CommTagTransNakConn,"Already logged in!").send();
                        } else {
                            internalUpdateClientsLoggingIn(1);
                            b=null;
                            ne=CommElement.nextElement(s.getCommBuff());
//                            UserData u=ProcessLogin(s);
                            UserData u=app.ProcessLogin(s);
                            if (u==null) {
                                System.err.println("Invalid login attempt!");
                                System.err.flush();
                                s.clearCommBuff();
                                s.put(CommTrans.CommTagTransNakConn,"Login authorization failed!").send();
                            } else
                                synchronized (clientSockets) {
                                    // use client's player ID as hash key
                                    // don't add concurrent with read loop
                                    if (clientSockets.containsKey(u)) {
                                        // verify same player
                                        System.err.println("Duplicate user login attempt!");
                                        System.err.flush();
                                        loginNakReason="Duplicate user login attempt!  If you were logged out improperly, please wait a while and try again.";
                                    } else if (clientSockets.containsValue(s)) {
                                        // socket re-use!
                                        System.err.println("Duplicate ClientServerSocket used for login attempt!");
                                        System.err.flush();
                                        loginNakReason="Server error!  (Attempt to use duplicate ClientServerSocket)  Please try again.";
                                    } else
                                        validLoginToAdd=true;
                                }
                                if (validLoginToAdd) // new user, new socket
                                    synchronized (clientSocketsToAdd) {
                                        if (clientSocketsToAdd.containsKey(u)) {
                                            // already trying to join
                                            System.err.println("Duplicate user login attempt!  (Already waiting to join)");
                                            System.err.flush();
                                            loginNakReason="Duplicate user login attempt!  (Already waiting to join)  Please wait a while and try again.";
                                        } else if (clientSocketsToAdd.containsValue(s)) {
                                            // socket re-use!
                                            System.err.println("Duplicate ClientServerSocket (in ToAdd!) used for login attempt!");
                                            System.err.flush();
                                            loginNakReason="Server error!  (Attempt to use duplicate ClientServerSocket in waiting list)  Please try again.";
                                        } else synchronized (clientSocketsConnecting) {
                                           if (clientSocketsToAdd.put(u,s)!=null) {
                                            // Should never get here - we checked this already!
                                                System.err.println("Duplicate user login attempt!");
                                                System.err.flush();
                                                loginNakReason="Server error!  (Concurrency violation in waiting list)  Please try again.";
                                           } else {
                                                clientSocketsConnecting.remove(s);
                                                validLogin = true;
                                           }
                                        }
                                    }
                                    if (validLogin) {
                                        // valid login, acknowledge it
                                        s.clearCommBuff();
                                        s.put(CommTrans.CommSeqLoginSuccess.buildBuffer(new Object[] {
//                                            WelcomeMessage(u) } ) );
                                            app.WelcomeMessage(u) } ) );
                                            s.send();
                                            s.setLoggedIn(true);
                                            internalUpdateClientsLoggedIn(1);
                                            s.nextHbTime=System.currentTimeMillis()+CommTrans.HeartbeatTimeoutMs;
                                    } else {
                                        // login invalid, negative acknowledge
                                        s.clearCommBuff();
                                        s.put(CommTrans.CommTagTransNakConn,loginNakReason).send();
                                    }
                                    internalUpdateClientsLoggingIn(-1);
                        }
                    } else {
                        if (!s.isLoggedIn()) { // should check for re-connect?
                            System.err.println("Recieved non-login message before login!  Buffer is:\n"
                            +CommElement.displayByteBuffer(s.getCommBuff()));
                            System.err.flush();
                            s.clearCommBuff();
                            s.put(CommTrans.CommTagTransNakConn,"Recieved non-login message before login!").send();
                        } else {
//                            if (!ProcessClientMessage(s)) {
                            if (!app.ProcessClientMessage(s)) {
                                System.err.println("Recieved unparseable message!  Buffer is:\n"
                                +CommElement.displayByteBuffer(s.getCommBuff()));
                                System.err.flush();
                                s.clearCommBuff();
                                s.put(CommTrans.CommTagTransNakConn,"Recieved unparseable message!").send();
                                s.close();
                                s=null;
                            } else {
                                s.clearCommBuff();
                            }
                        }
                    }
                }
            }
            s=null;
        }
        
    }
    
//    public Server(Object t) {
    public Server(ServerApp a) {
        app=a;
        System.err.println("Initializing constants");
//        new DefaultCommConstants(t);
        new DefaultCommConstants(app.getClass());
        
        //        clientSockets
        //        = new com.InfoMontage.helper.clientServer.Server.ClientSocketHashtable(CommSock.InitialClientSocketCapacity);
        clientSockets
        = new com.InfoMontage.helper.clientServer.Server.ClientSocketHashtable(CommSock.InitialClientSocketCapacity);
        //        clientSocketsToAdd
        //        = new com.InfoMontage.helper.clientServer.Server.ClientSocketHashtable(CommSock.InitialClientSocketsWaitingCapacity);
        clientSocketsToAdd
        = new com.InfoMontage.helper.clientServer.Server.ClientSocketHashtable(CommSock.InitialClientSocketsWaitingCapacity);
        
        int pbi=0;
        //        com.InfoMontage.helper.clientServer.Server.ClientConnector tcct;
        com.InfoMontage.helper.clientServer.Server.ClientConnector tcct;
        clientConnectionThreadGroup
        =new ThreadGroup("Client connection processor threads");
        activeClientConnectionThreadsList=java.util.Collections
        .synchronizedList(new java.util.LinkedList());
        inactiveClientConnectionThreadsList=java.util.Collections
        .synchronizedList(new java.util.LinkedList());
        System.err.println("Creating connection processor threads");
        for (int i=0;i<CommSock.MaxClientConnectionProcessorThreads;i++) {
            //            tcct=new com.InfoMontage.helper.clientServer.Server.ClientConnector();
            tcct=new com.InfoMontage.helper.clientServer.Server.ClientConnector(clientConnectionThreadGroup);
            if (tcct==null)
                throw new RuntimeException("Ran out of memory allocating connection processor threads!");
            tcct.setPriority(clientChannelConnectionThreadPriority);
            tcct.setName("ClientConnector "+(i+1));
            inactiveClientConnectionThreadsList.add(tcct);
        }
        
        //        com.InfoMontage.helper.clientServer.Server.ClientMessageProcessor tmpt;
        com.InfoMontage.helper.clientServer.Server.ClientMessageProcessor tmpt;
        messageProcessorThreadGroup
        =new ThreadGroup("Client message processor threads");
        activeMessageProcessorThreadsList=java.util.Collections
        .synchronizedList(new java.util.LinkedList());
        inactiveMessageProcessorThreadsList=java.util.Collections
        .synchronizedList(new java.util.LinkedList());
        System.err.println("Creating message processor threads");
        for (int i=0;i<CommSock.MaxClientMessageProcessorThreads;i++) {
            //            tmpt=new com.InfoMontage.helper.clientServer.Server.ClientMessageProcessor();
            tmpt=new com.InfoMontage.helper.clientServer.Server.ClientMessageProcessor(messageProcessorThreadGroup);
            if (tmpt==null)
                throw new RuntimeException("Ran out of memory allocating connection processor threads!");
            tmpt.setPriority(clientMessageProcessorThreadPriority);
            tmpt.setName("ClientMessageProcessor "+(i+1));
            inactiveMessageProcessorThreadsList.add(tmpt);
            //            initializationProgressBar.setValue(pbi++);
            //            initializationProgressBar.paintImmediately(initializationProgressBar.getBounds());
        }
        
        //        initializationProgressFrame.setVisible(false);
/*
        System.err.println("Created "+pbi+" threads!\nMP="
        +inactiveMessageProcessorThreadsList.size()+"  CP="
        +inactiveClientConnectionThreadsList.size());
 */
        
        System.err.println("Creating client listener thread");
        try {
            clientSocketListener
            //            = new com.InfoMontage.helper.clientServer.Server.ClientListener(CommSock.ClientConnectionListenerPort);
            = new com.InfoMontage.helper.clientServer.Server.ClientListener(CommSock.ClientConnectionListenerPort);
        } catch( IOException e ) {
            System.err.println("Error while instantiating client listener!");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        clientSocketListener.setDaemon(true);
        clientSocketListener.setPriority(clientSocketListenerThreadPriority);
        //        clientChannelProcessor = new com.InfoMontage.helper.clientServer.Server.ClientChannelProcessor();
        clientChannelProcessor = new com.InfoMontage.helper.clientServer.Server.ClientChannelProcessor();
        clientChannelProcessor.setDaemon(true);
        clientChannelProcessor.setPriority(clientChannelProcessorThreadPriority);
    }
    
    public void start() {
        // start the client processing thread
        System.err.println("Starting channel processor thread");
        clientChannelProcessor.start();
        // now start listening for clients
        System.err.println("Starting client listener thread");
        clientSocketListener.start();
    }
}
