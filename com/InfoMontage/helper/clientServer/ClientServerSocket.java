/*
 * ClientServerSocket.java
 *
 * Created on July 1, 2001, 12:23 PM
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

import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
import java.util.*;
import java.util.*; 
import com.InfoMontage.helper.clientServer.DefaultCommConstants.*;
import com.InfoMontage.helper.clientServer.CommElement.*;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public class ClientServerSocket {
    //    public SocketChannel s=null;
    public volatile Socket sock=null;
    public volatile ByteBuffer commBuff=null;
    private InputStream inStream=null;
    private OutputStream outStream=null;
    public ByteBuffer inBuff=null;
    public ByteBuffer outBuff=null;
    public Set buffPool=null;
    public volatile boolean loggedIn=false;
    public volatile int numHbAckNeeded=0;
    public volatile long nextHbTime=0;
    public volatile int numFailedHbs=0;
    public volatile boolean recvComplete=false;
    
    //    public ClientServerSocket_New(SocketChannel sockCh) {
    public ClientServerSocket(Socket s) {
        //        if (sockCh==null)
        //        if (s==null)
        //            throw new RuntimeException("Attempt to create a socket with a null channel!");
        //        else {
        ByteBuffer bb=ByteBuffer.allocate(CommSock.ClientChannelBufferCapacity);
        //            if (bb==null)
        //                throw new RuntimeException("Could not allocate buffer for channel!");
        //            else
        //                initClientServerSocket(sockCh,bb);
        initClientServerSocket(s,bb);
        //        }
    }
    
    //    public ClientServerSocket_New(SocketChannel sockCh, ByteBuffer bb) {
    public ClientServerSocket(Socket s, ByteBuffer bb) {
        //        if (sockCh==null)
        //        if (s==null)
        //            throw new RuntimeException("Attempt to create a socket with a null channel!");
        //        else {
        //            if (bb==null)
        //                throw new RuntimeException("Attempt to create a socket with a null buffer for channel!");
        //            else
        //                initClientServerSocket(sockCh,bb);
        initClientServerSocket(s,bb);
        //        }
    }
    
    //    public ClientServerSocket_New(SocketChannel sockCh, Set bs) {
    public ClientServerSocket(Socket s, Set bs) {
        //        if (sockCh==null)
        //        if (s==null)
        //            throw new RuntimeException("Attempt to create a socket with a null channel!");
        //        else {
        if (bs==null)
            throw new RuntimeException("Attempt to create a socket with a null buffer for channel!");
        else {
            synchronized(bs) {
                if (bs.isEmpty())
                    throw new RuntimeException("Attempt to create a socket with a null buffer for channel!");
                else {
                    ByteBuffer bb = null;
                    Iterator i=bs.iterator();
                    bb=(ByteBuffer)i.next();
                    i.remove();
                    buffPool=bs;
                    i=null;
                    //                    initClientServerSocket(sockCh,bb);
                    initClientServerSocket(s,bb);
                }
            }
        }
        //        }
    }
    
    //    public void initClientServerSocket( SocketChannel sockCh, ByteBuffer bb ) {
    private void initClientServerSocket( Socket s, ByteBuffer bb ) {
        //        if (sockCh==null)
        if (s==null)
            throw new RuntimeException("Attempt to initialize a socket with a null channel!");
        else {
            synchronized (s) {
                //            sc=sockCh;
                sock=s;
                if (bb==null)
                    throw new RuntimeException("Attempt to initialize a socket with a null buffer for channel!");
                else
                    synchronized (bb) {
                        if (!bb.hasArray())
                            throw new RuntimeException("Attempt to initialize a socket with a buffer that has no backing array!");
                        else
                            commBuff=bb;
                        //            if (sc.isConnectionPending())
                        if (!sock.isConnected())
                            //                try {
                            //                    long st=System.currentTimeMillis();
                            //                    sc.finishConnect(); // time expensive?
                            //                    st-=System.currentTimeMillis();
                            //                    System.err.println("Connection finish took "+(-st)+" ms!");
                            //                    System.err.flush();
                            //                } catch (IOException e) {
                            //                    System.err.println("Error while finishing channel connection!");
                            //                    System.err.println(e.getMessage());
                            //                    e.printStackTrace();
                            //                    close();
                            //                }
                            //            else
                            //              if (!sc.isConnected()) {
                        {
                            close();
                            // throw new RuntimeException("Attempt to initialize a socket with an unconnected channel!");
                            throw new NotYetConnectedException();
                        }
                        else {
                            try {
                                inStream=sock.getInputStream();
                                outStream=sock.getOutputStream();
                            } catch (IOException e ) {
                                close();
                                // throw new RuntimeException("I/O error while getting streams from socket!");
                                throw new NotYetConnectedException();
                            }
                        }
                    }
            }
        }
    }
    
    synchronized public boolean isConnected() {
        return ((sock != null) && (!sock.isClosed()) && (sock.isBound())
        && (!sock.isInputShutdown()) && (!sock.isOutputShutdown())
        && (sock.isConnected()) && (commBuff != null));
    }
    
    synchronized public void close() {
        //        if (sc!=null)
        try {
            if (inStream!=null) {
                inStream.close();
                inStream=null;
            }
            if (outStream!=null) {
                outStream.close();
                outStream=null;
            }
            if (sock!=null)
                synchronized (sock) {
                    //                sc.close();
                    sock.close();
                    //        sc=null;
                    sock=null;
                }
        } catch ( IOException e ) {
            // We need to do anything about this, just report it
            System.err.println("Error while closing channel!");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        if (commBuff!=null) {
            synchronized (commBuff) {
                if (buffPool!=null) {
                    synchronized (buffPool) {
                        if (buffPool.add(commBuff))
                            // we should never get here!
                            throw new IllegalStateException("Channel buffer already returned to pool before socket close!!");
                        buffPool=null;
                    }
                }
                commBuff=null;
            }
        }
        recvComplete=false;
    }
    
    synchronized public boolean send() {
        boolean sendOk=true;
        recvComplete=false;
        //        if ((sc==null)||(commBuff==null)) {
        if ((sock==null)||(commBuff==null)) {
            close();
            sendOk=false;
            throw new NullPointerException("Attempt to send to null channel or out of null buffer!");
        } else
            //            if (!sc.isConnected()) {
            if (!sock.isConnected()) {
                close();
                sendOk=false;
            }
            else
                // System.err.println("Requested to send "+CommElement.displayByteBuffer((ByteBuffer)commBuff.asReadOnlyBuffer().flip()));
                synchronized (commBuff) {
                    if (commBuff.position()>0) { // something to send...
                        long st=System.currentTimeMillis();
                        if (commBuff.get(commBuff.position()-1)!=CommTrans.CommDelimiterByte)
                            if (commBuff.hasRemaining())
                                commBuff.put(CommTrans.CommDelimiterByte);
                            else
                                // throw new RuntimeException("Buffer overflow putting delimiter byte!");
                                throw new BufferOverflowException();
                            commBuff.flip(); // makes buff ready to be written out
                        // System.err.println("Sending "+CommElement.displayByteBuffer(commBuff));
                        int sent=0;
                        try { // keep writing until done or error
                            //                        while (commBuff.hasRemaining()) {
                            //                            sc.write(commBuff);
                            sock.getOutputStream()
                            .write(commBuff.array(),commBuff.arrayOffset(),commBuff.remaining());
                            //                            Thread.yield(); // sleep instead?  lock on commBuff?
                            //                        }
                            sent=commBuff.remaining();
                            commBuff.clear(); // makes buff ready to be put into again
                            // oughta maybe be compact()?
                        } catch ( IOException e ) {
                            close();
                            sendOk=false;
                            System.err.println("Error while writing to channel!");
                            System.err.println(e.getMessage());
                            e.printStackTrace();
                        }
                        st-=System.currentTimeMillis();
                        System.err.println("ClientServerSocket: Send ("+Boolean.toString(sendOk)+") of "+sent+" bytes took "+(-st)+" ms");
                        System.err.flush();
                    }
                }
                return sendOk;
    }
    
    synchronized public boolean recv(long t) {
        boolean recvOk=true;
        recvComplete=false;
        long tot=System.currentTimeMillis()+t;
        int cnb=0, nb=0;
        //        if ((sc==null)||(commBuff==null)) {
        if (sock==null) {
            close();
            recvOk=false;
            throw new NullPointerException("Attempt to recieve from null channel!");
        }
        else {
            //            if (!sc.isConnected()) {
            synchronized (sock) {
                if (commBuff==null) {
                    close();
                    recvOk=false;
                    throw new NullPointerException("Attempt to recieve into null buffer!");
                }
                else {
                    synchronized (commBuff) {
                        if (!sock.isConnected()) {
                            close();
                            recvOk=false;
                        }
                        else {
                            long st=System.currentTimeMillis();
                            try {
                                while (((t>0)&&(System.currentTimeMillis()<tot)&&(cnb>-1))
                                || ((t==0)&&(cnb>-1))) {
                                    //                        cnb=sc.read(commBuff);
                                    InputStream is=sock.getInputStream();
                                    //                        System.err.println("InputStream ("+is+") has "+is.available()+" bytes available.");
                                    cnb=0;
                                    try {
                                        cnb=is.read(commBuff.array()
                                        ,commBuff.arrayOffset()+commBuff.position()
                                        ,is.available());
                                    } catch ( SocketTimeoutException e ) {
                                        //                            System.err.println("Timeout while reading channel!");
                                    }
                                    if (cnb>0) {
                                        nb+=cnb;
                                        System.err.println("recieved "+cnb+" bytes.  Subtotal="+nb);
                                        commBuff.position(commBuff.position()+cnb);
                                        if (!commBuff.hasRemaining()) {
                                            if (t==0) {
                                                // ran out of buffer space!
                                                recvOk=false;
                                                // throw new RuntimeException("End-of-Buffer while reading channel!");
                                                throw new BufferUnderflowException();
                                                // we really should do something here... handle this better
                                            }
                                        }
                                        //                            else
                                        //                                Thread.yield(); // sleep instead?
                                    }
                                    else {
                                        if ((cnb==0) && (commBuff.position()>0)
                                        && (commBuff.get(commBuff.position()-1)
                                        ==CommTrans.CommDelimiterByte))
                                            cnb=-1;
                                        // next should not be necessary unless read() spins
                                        // instead of sleeping
                                        //                            else
                                        //                                Thread.yield(); // sleep instead?
                                    }
                                    // next should not be necessary unless read() spins
                                    // instead of sleeping
                                    //                        Thread.yield(); // sleep instead?
                                }
                                //                    if (nb>0) {
                                //                        commBuff.flip(); // makes buff ready to be gotten from
                                //                        System.err.println("Recieved "+CommElement.displayByteBuffer(commBuff));
                                //                    }
                            } catch ( IOException e ) {
                                close();
                                recvOk=false;
                                System.err.println("Error while reading channel!");
                                System.err.println(e.getMessage());
                                e.printStackTrace();
                            } finally {
                                st-=System.currentTimeMillis();
                                System.err.println("ClientServerSocket: Recv ("+Boolean.toString(recvOk)+") of "+nb+" bytes took "+(-st)+" ms");
                                System.err.flush();
                            }
                        }
                        if ((recvOk)&&(((t>0)&&(nb==0))||((t==0)&&(cnb!=-1)))) {
                            //            System.err.println("ClientServerSocket: Recv failure (t="+t+", nb="+nb+", cnb="+cnb+")");
                            //            System.err.flush();
                            // don't close() - (t>0 && nb==0) is valid to keep it open
                            recvOk=false;
                        }
                        if ((recvOk)
                        &&(commBuff.position()>0)
                        && (commBuff.get(commBuff.position()-1)
                        ==CommTrans.CommDelimiterByte)) {
                            commBuff.flip(); // makes buff ready to be gotten from
                            System.err.println("Recieved "+CommElement.displayByteBuffer(commBuff));
                            recvComplete=true;
                        } else {
                            if ((recvOk)&&(t==0)
                            &&(commBuff.get(commBuff.limit()-1)!=CommTrans.CommDelimiterByte)) {
                                System.err.println("ClientServerSocket: Recv failure (t="+t+")");
                                System.err.println("ClientServerSocket: Recv failure (buff="+commBuff.toString()+")");
                                //            System.err.println("ClientServerSocket: Recv failure (limit="+commBuff.limit()+")");
                                System.err.println("ClientServerSocket: Recv failure (last byte="+Byte.toString(commBuff.get(commBuff.limit()-1))+")");
                                System.err.flush();
                                close(); // TBD: retry or other notification of bad data stream
                                recvOk=false;
                            }
                        }
                    }
                }
            }
        }
        return recvOk;
    }
    
    public String displayByteBuffer() {
        return com.InfoMontage.util.Buffer.toString(commBuff
        ,DefaultCommConstants.chrset);
        //        if (commBuff==null)
        //            return "[null]";
        //        else
        //            synchronized (commBuff) {
        //                return "'"+(((commBuff.position())==0 && (commBuff.limit()==commBuff.capacity()))?""
        //                :DefaultCommConstants.chrset.decode(commBuff.asReadOnlyBuffer()).toString())
        //                +"'="+commBuff.position()+","+commBuff.limit()+","+commBuff.capacity();
        //            }
    }
    
    synchronized public ClientServerSocket put(CommTag t) {
        t.encode(commBuff);
        return this;
    }
    
    synchronized public ClientServerSocket put(CommTag t, byte b) {
        t.encode(commBuff,b);
        return this;
    }
    
    synchronized public ClientServerSocket put(CommTag t, byte[] b) {
        t.encode(commBuff,b);
        return this;
    }
    
    synchronized public ClientServerSocket put(CommTag t, String s) {
        t.encode(commBuff,s);
        return this;
    }
    
    synchronized public ClientServerSocket put(ByteBuffer b) {
        // System.err.println("Putting "+CommElement.displayByteBuffer(b));
        commBuff.put(b);
        return this;
    }
    
}