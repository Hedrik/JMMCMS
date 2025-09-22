/*
 * ServerApp.java
 *
 * Created on May 13, 2003, 11:03 PM
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
import java.net.Socket;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public interface ServerApp {
     void updateClientsConnecting(int n);
     void updateClientsConnected(int n);
     void updateClientsLoggingIn(int n);
     void updateClientsLoggedIn(int n);
     void updateClientsForceDisconnected(int n);
     void updateHbsPending(int n);
     void updateHbsFailedCurrent(int n);
     void updateHbsFailedTotal(int n);
     byte LoginReqs();
     String HelloMessage(Socket s);
     UserData ProcessLogin(ClientServerSocket s);
     String WelcomeMessage(UserData u);
     boolean ProcessClientMessage(ClientServerSocket css);
}
