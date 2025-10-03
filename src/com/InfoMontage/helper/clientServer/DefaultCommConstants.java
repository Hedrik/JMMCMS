/*
 * DefaultCommConstants.java
 *
 * Created on July 1, 2001, 10:03 PM
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

import com.InfoMontage.helper.clientServer.CommElement.*;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public final class DefaultCommConstants {
    
    public static final java.nio.charset.Charset chrset
    =java.nio.charset.Charset.forName(new java.io.InputStreamReader(System.in)
    .getEncoding());
    
    private static Class ac = null;
    
    public DefaultCommConstants() {
        new CommSock();
        new CommTrans();
    }
    
//    public DefaultCommConstants(Object app) {
    public DefaultCommConstants(Class appClass) {
        ac = appClass;
        new CommSock();
        new CommTrans();
    }
    
    // Communications
    //    public static final class CommTrans {
    public static final class CommTrans {
        
        public static final ClientServerProperty testProp1;
        public static final ClientServerProperty testProp2;
        public static final ClientServerProperty testProp3;
        public static final ClientServerProperty testProp4;
        
        static {
            testProp1 =
            new ClientServerProperty(ac,"testByteProperty",Byte.valueOf((byte)33));
            testProp2 =
            new ClientServerProperty(ac,"testStringProperty","String","Test");
            testProp3 =
            new ClientServerProperty(ac,"testIntegerProperty2",Integer.valueOf(223));
            testProp4 =
            new ClientServerProperty(ac,"testLongProperty",Long.valueOf(22334455));
        }
        
        /* transmission and connection protocol constants */
        
        public static final byte CommTransVersion = (byte)1;
        public static final int CommTransIdLen = 2;
        public static final int HeartbeatTimeoutMs = 3000;
        public static final int MaxFailedHeartbeats = 5;
        public static final int ClientAcknowledgementTimeout = HeartbeatTimeoutMs * 4;
        public static final byte LoginRequiresNoAuthentication = (byte)0;
        public static final byte LoginRequiresNameOnly = (byte)1;
        public static final byte LoginRequiresPasswordOnly = (byte)2;
        public static final byte LoginRequiresNameAndPassword = (byte)3;
        public static final byte CommDelimiterByte = (byte)255;
        public static final CommTag CommTagTransAck = new CommTag("TAck");
        public static final CommTag CommTagTransNakConn = new CommTag("TNak","kaNT");
        public static final CommTag CommTagTransHB = new CommTag("TrHb");
        public static final CommTag CommTagTransHBAck = new CommTag("TrHA");
        public static final CommTag CommTagTransVers = new CommTag("TrVr",1);
        public static final CommTag CommTagTransId = new CommTag("TrId",CommTransIdLen);
        public static final CommTag CommTagTransLoginReq = new CommTag("TLog",1);
        public static final CommTag CommTagLoginAttempt = new CommTag("Lgin",1);
        public static final CommTag CommTagLoginNone = new CommTag("LNon");
        public static final CommTag CommTagLoginName = new CommTag("LNam","maNL");
        public static final CommTag CommTagLoginPasswd = new CommTag("LPas","saPL");
        public static final CommTag CommTagHello = new CommTag("Helo","eloH");
        public static final CommTag CommTagWelcomeMsg = new CommTag("Welc","cleW");
        public static final CommTag CommTagChatMsg = new CommTag("Chat","tahC");
        
        public static final CommSequence CommSeqBegin=new CommSequence(new CommTag[] {
            CommTagTransAck,
            CommTagTransVers,
            CommTagHello,
            CommTagTransLoginReq } );
            
        public static final CommSequence CommSeqMaxConnections=new CommSequence(new CommTag[] {
            CommTagTransAck,
            CommTagTransVers,
            CommTagTransNakConn } );
            
        public static final CommSequence CommSeqLoginSuccess=new CommSequence(new CommTag[] {
            CommTagTransAck,
            CommTagWelcomeMsg } );

        public static final CommSequence CommSeqLoginFailure=new CommSequence(new CommTag[] {
            CommTagTransNakConn } );

        public static final CommSequence CommSeqChat=new CommSequence(new CommTag[] {
            CommTagChatMsg } );

    }
    
    public static final class CommSock {
        /* socket connection and I/O buffer constants */
        public static final boolean SecureConnection = false;
        public static final boolean AuthenticateClient = false;
        public static final boolean UseDefaultSocketFactory = true;
        public static final int InitialClientSocketCapacity = 100;
        public static final int InitialClientSocketsWaitingCapacity = 10;
        public static final int MaxClientConnectionProcessorThreads = 10;
        public static final int MaxClientMessageProcessorThreads = 100;
        public static final int MaxClientSocketCapacity = 1000;
        public static final int SocketTimoutMs = 2000;
        public static final long PollSocketTimoutMs = 5;
        public static final long ReadSocketTimoutMs = 50;
        public static final long ClientSocketReadPauseMs = 10;
        public static final long ClientSocketLoopActivePauseMs = 10;
        public static final long ClientSocketLoopInactivePauseMs = 500;
//        public static final String DefaultServerHostName = "localhost";
        public static final String DefaultServerHostName = "127.0.0.1";
        public static final int ClientConnectionListenerPort = 8453;
        public static final int StdErrChannelBufferCapacity = 250;
        public static final int ClientChannelBufferCapacity = 4096;
    }
    
}
