/*
 * ExampleServer.java
 *
 * Created on August 7, 2002, 11:57 PM
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

package com.InfoMontage.example.ClientServer;

import com.InfoMontage.helper.clientServer.*;
import com.InfoMontage.helper.clientServer.DefaultCommConstants.CommTrans;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */

public class ExampleServer extends JFrame implements ServerApp {
//public class ServerApp extends javax.swing.JFrame implements com.InfoMontage.helper.clientServer.ServerApp {
    
    Server myServer=null;
    static long nextUniqueUserID=1;
    
//    public class MyServer extends com.InfoMontage.helper.clientServer.Server {
//  public class MyServer implements com.InfoMontage.helper.clientServer.ServerApp {
//        ServerApp myApp;
        
//        public MyServer(ServerApp t) {
//            super(t);
//            myApp=t;
//        }
        
        public synchronized void updateClientsConnecting(int n) {
//            myApp.clientsConnectingDisplay.setText(Integer.toString(n));
clientsConnectingDisplay.setText(Integer.toString(n));
        }
        
        public synchronized void updateClientsConnected(int n) {
//            myApp.clientsConnectedDisplay.setText(Integer.toString(n));
clientsConnectedDisplay.setText(Integer.toString(n));
        }
        
        public synchronized void updateClientsLoggingIn(int n) {
//            myApp.clientsLoggingInDisplay.setText(Integer.toString(n));
clientsLoggingInDisplay.setText(Integer.toString(n));
        }
        
        public synchronized void updateClientsLoggedIn(int n) {
//            myApp.clientsLoggedInDisplay.setText(Integer.toString(n));
clientsLoggedInDisplay.setText(Integer.toString(n));
        }
        
        public synchronized void updateClientsForceDisconnected(int n) {
//            myApp.clientsForceDisconnectedDisplay.setText(Integer.toString(n));
clientsForceDisconnectedDisplay.setText(Integer.toString(n));
        }
        
        public synchronized void updateHbsPending(int n) {
//            myApp.clientHbsPendingDisplay.setText(Integer.toString(n));
clientHbsPendingDisplay.setText(Integer.toString(n));
        }
        
        public synchronized void updateHbsFailedCurrent(int n) {
//            myApp.clientHbsFailedCurrentDisplay.setText(Integer.toString(n));
clientHbsFailedCurrentDisplay.setText(Integer.toString(n));
        }
        
        public synchronized void updateHbsFailedTotal(int n) {
//            myApp.clientHbsFailedTotalDisplay.setText(Integer.toString(n));
clientHbsFailedTotalDisplay.setText(Integer.toString(n));
        }
        
//        protected String HelloMessage(java.nio.channels.SocketChannel sc) {
        public String HelloMessage(Socket s) {
            return "Hello, "
//            +((java.net.InetSocketAddress)(sc.socket().getRemoteSocketAddress()))
            +((InetSocketAddress)(s.getRemoteSocketAddress()))
            .getAddress().getHostName()
            +", and welcome to the Client/Server code test server, using Server code version "
//            +com.InfoMontage.helper.clientServer.Server.ServerCodeVersion+"!";
            +Server.ServerCodeVersion+"!";
        }
        
        public byte LoginReqs() {
            return CommTrans.LoginRequiresNoAuthentication;
        }
        
        public String WelcomeMessage(UserData u) {
            return "Thank you for successfully logging in!  Your user ID is '"
            +u.getID()+"'";
        }
        
        public class MyUserData implements UserData {
            
            public class MyUniqueUserID extends UniqueUserID {
                
                long myID;
                
                MyUniqueUserID() {
                    myID=nextUniqueUserID;
                    nextUniqueUserID++;
                }
                
                public boolean equals(Object obj) {
                    return ((obj.getClass()==this.getClass())
                    &&(((MyUniqueUserID)obj).myID==myID));
                }
                
                public String toString() {
                    return Long.toString(myID);
                }
            }
            
            MyUniqueUserID myID;
            
            public UniqueUserID getID() {
                return myID;
            }
            
            MyUserData() {
                myID=new MyUniqueUserID();
            }
            
        }
        
//        protected com.InfoMontage.helper.clientServer.UserData ProcessLogin(com.InfoMontage.helper.clientServer.ClientServerSocket s) {
        public UserData ProcessLogin(ClientServerSocket s) {
            MyUserData u=null;
            CommElement e=CommElement.nextElement(s.commBuff);
            if (e != null && e.tag==CommTrans.CommTagLoginNone) {
                System.err.println("Proper login tag '"+e.tag+"'");
                u=new MyUserData();
                System.err.println("UserID is '"+u.myID+"'");
            }
            return u;
        }
        
        public boolean ProcessClientMessage(ClientServerSocket css) {
        return false; // No test messages as yet
        }
        
//    }
    
    private long toKB(long b) {
        return b/1024;
    }
    
    private void updateMemoryUse() {
        freeMemoryDisplay.setText(Long.toString(toKB(Runtime.getRuntime().freeMemory()))+"K");
        totalMemoryDisplay.setText(Long.toString(toKB(Runtime.getRuntime().totalMemory()))+"K");
        maxMemoryDisplay.setText(Long.toString(toKB(Runtime.getRuntime().maxMemory()))+"K");
    }
    
    /** Creates new form ExampleServer */
    public ExampleServer() {
        initComponents();
//        new com.InfoMontage.helper.clientServer.DefaultCommConstants(this);
        
//        serverVersionDisplay.setText(com.InfoMontage.helper.clientServer.Server.ServerCodeVersion);
        serverVersionDisplay.setText(Server.ServerCodeVersion);
        
        //        frame.show();
        
        updateMemoryUse();
        Timer t=new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateMemoryUse();
            }
        });
        
//        myServer=new ClientServer.ExampleServer.MyServer();
        statusLabel.setText("Initializing...");
//        myServer=new ExampleServer.MyServer(this);
//        myServer=new com.InfoMontage.helper.clientServer.Server(this);
        t.start();
        t=null;
//        myServer.start();
//        statusLabel.setText("Listening...");
    }
    
    public void start() {
//        ExampleServer.MyServer m=new ExampleServer.MyServer();
//        myServer=new com.InfoMontage.helper.clientServer.Server(m);
        myServer=new Server(this);
        myServer.start();
        statusLabel.setText("Listening...");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        ServerCodeVersionPanel = new javax.swing.JPanel();
        serverVersionLabel = new javax.swing.JLabel();
        serverVersionDisplay = new javax.swing.JLabel();
        formNameLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        globalStatsPanel = new javax.swing.JPanel();
        memoryInfoPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        maxMemoryLabel = new javax.swing.JLabel();
        maxMemoryDisplay = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        totalMemoryLabel = new javax.swing.JLabel();
        totalMemoryDisplay = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        freeMemoryLabel = new javax.swing.JLabel();
        freeMemoryDisplay = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        clientConnectionsPanel = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        clientsConnectingLabel = new javax.swing.JLabel();
        clientsConnectingDisplay = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        clientsConnectedLabel = new javax.swing.JLabel();
        clientsConnectedDisplay = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        clientsForceDisconnectedLabel = new javax.swing.JLabel();
        clientsForceDisconnectedDisplay = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        clientsLoggingInLabel = new javax.swing.JLabel();
        clientsLoggingInDisplay = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        clientsLoggedInLabel = new javax.swing.JLabel();
        clientsLoggedInDisplay = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        clientHbsPendingLabel = new javax.swing.JLabel();
        clientHbsPendingDisplay = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        clientHbsFailedCurrentLabel = new javax.swing.JLabel();
        clientHbsFailedCurrentDisplay = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        clientHbsFailedTotalLabel = new javax.swing.JLabel();
        clientHbsFailedTotalDisplay = new javax.swing.JLabel();

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        ServerCodeVersionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 1));

        ServerCodeVersionPanel.setMaximumSize(new java.awt.Dimension(32767, 16));
        ServerCodeVersionPanel.setMinimumSize(new java.awt.Dimension(158, 16));
        ServerCodeVersionPanel.setPreferredSize(new java.awt.Dimension(158, 16));
        serverVersionLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        serverVersionLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        serverVersionLabel.setLabelFor(serverVersionDisplay);
        serverVersionLabel.setText("using Server code version:");
        serverVersionLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        serverVersionLabel.setAlignmentX(0.5F);
        serverVersionLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ServerCodeVersionPanel.add(serverVersionLabel);

        serverVersionDisplay.setFont(new java.awt.Font("Dialog", 0, 10));
        serverVersionDisplay.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        serverVersionDisplay.setText("0.0.0.0");
        serverVersionDisplay.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        serverVersionDisplay.setAlignmentX(0.5F);
        serverVersionDisplay.setMaximumSize(new java.awt.Dimension(36, 14));
        serverVersionDisplay.setMinimumSize(new java.awt.Dimension(24, 14));
        serverVersionDisplay.setPreferredSize(new java.awt.Dimension(34, 14));
        serverVersionDisplay.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        ServerCodeVersionPanel.add(serverVersionDisplay);

        getContentPane().add(ServerCodeVersionPanel);

        formNameLabel.setFont(new java.awt.Font("Arial Narrow", 2, 24));
        formNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        formNameLabel.setLabelFor(this);
        formNameLabel.setText("Example Server Application");
        formNameLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        formNameLabel.setAlignmentX(0.5F);
        formNameLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        getContentPane().add(formNameLabel);

        statusLabel.setFont(new java.awt.Font("Dialog", 2, 12));
        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        statusLabel.setText("Starting up...");
        statusLabel.setAlignmentX(0.5F);
        statusLabel.setBorder(new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        statusLabel.setMaximumSize(new java.awt.Dimension(160, 20));
        statusLabel.setPreferredSize(new java.awt.Dimension(120, 20));
        statusLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        statusLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        getContentPane().add(statusLabel);

        globalStatsPanel.setLayout(new javax.swing.BoxLayout(globalStatsPanel, javax.swing.BoxLayout.Y_AXIS));

        globalStatsPanel.setBorder(new javax.swing.border.TitledBorder(null, "Server Global Statistics", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        globalStatsPanel.setMaximumSize(new java.awt.Dimension(460, 274));
        globalStatsPanel.setMinimumSize(new java.awt.Dimension(200, 143));
        globalStatsPanel.setPreferredSize(new java.awt.Dimension(386, 159));
        memoryInfoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 1));

        memoryInfoPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        memoryInfoPanel.setMaximumSize(new java.awt.Dimension(458, 55));
        memoryInfoPanel.setMinimumSize(new java.awt.Dimension(144, 21));
        memoryInfoPanel.setPreferredSize(new java.awt.Dimension(279, 37));
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 1, 1));

        jPanel1.setMaximumSize(new java.awt.Dimension(158, 16));
        jPanel1.setMinimumSize(new java.awt.Dimension(142, 16));
        jPanel1.setPreferredSize(new java.awt.Dimension(142, 16));
        maxMemoryLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        maxMemoryLabel.setText("Maximum memory:");
        maxMemoryLabel.setAlignmentY(0.0F);
        jPanel1.add(maxMemoryLabel);

        maxMemoryDisplay.setFont(new java.awt.Font("Dialog", 1, 10));
        maxMemoryDisplay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        maxMemoryDisplay.setText("0K");
        maxMemoryDisplay.setMaximumSize(new java.awt.Dimension(64, 14));
        maxMemoryDisplay.setMinimumSize(new java.awt.Dimension(48, 14));
        maxMemoryDisplay.setPreferredSize(new java.awt.Dimension(48, 14));
        jPanel1.add(maxMemoryDisplay);

        memoryInfoPanel.add(jPanel1);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 1, 1));

        jPanel2.setMaximumSize(new java.awt.Dimension(136, 16));
        totalMemoryLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        totalMemoryLabel.setText("Total memory:");
        totalMemoryLabel.setAlignmentY(0.0F);
        jPanel2.add(totalMemoryLabel);

        totalMemoryDisplay.setFont(new java.awt.Font("Dialog", 1, 10));
        totalMemoryDisplay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalMemoryDisplay.setText("0K");
        totalMemoryDisplay.setMaximumSize(new java.awt.Dimension(64, 14));
        totalMemoryDisplay.setMinimumSize(new java.awt.Dimension(48, 14));
        totalMemoryDisplay.setPreferredSize(new java.awt.Dimension(48, 14));
        jPanel2.add(totalMemoryDisplay);

        memoryInfoPanel.add(jPanel2);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 1, 1));

        jPanel3.setMaximumSize(new java.awt.Dimension(132, 16));
        jPanel3.setMinimumSize(new java.awt.Dimension(116, 16));
        jPanel3.setPreferredSize(new java.awt.Dimension(116, 16));
        freeMemoryLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        freeMemoryLabel.setText("Free memory:");
        freeMemoryLabel.setAlignmentY(0.0F);
        jPanel3.add(freeMemoryLabel);

        freeMemoryDisplay.setFont(new java.awt.Font("Dialog", 1, 10));
        freeMemoryDisplay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        freeMemoryDisplay.setText("0K");
        freeMemoryDisplay.setMaximumSize(new java.awt.Dimension(64, 14));
        freeMemoryDisplay.setMinimumSize(new java.awt.Dimension(48, 14));
        freeMemoryDisplay.setPreferredSize(new java.awt.Dimension(48, 14));
        jPanel3.add(freeMemoryDisplay);

        memoryInfoPanel.add(jPanel3);

        globalStatsPanel.add(memoryInfoPanel);

        jPanel17.setMaximumSize(new java.awt.Dimension(10, 10));
        globalStatsPanel.add(jPanel17);

        clientConnectionsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 10));

        clientConnectionsPanel.setBorder(new javax.swing.border.EtchedBorder());
        clientConnectionsPanel.setMaximumSize(new java.awt.Dimension(405, 178));
        clientConnectionsPanel.setMinimumSize(new java.awt.Dimension(199, 118));
        clientConnectionsPanel.setPreferredSize(new java.awt.Dimension(405, 118));
        jPanel15.setLayout(new javax.swing.BoxLayout(jPanel15, javax.swing.BoxLayout.Y_AXIS));

        jPanel15.setMinimumSize(new java.awt.Dimension(179, 94));
        jPanel15.setPreferredSize(new java.awt.Dimension(179, 94));
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        jPanel4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        jPanel4.setMinimumSize(new java.awt.Dimension(177, 50));
        jPanel4.setPreferredSize(new java.awt.Dimension(177, 50));
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 1, 1));

        clientsConnectingLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        clientsConnectingLabel.setText("Clients connecting:");
        clientsConnectingLabel.setAlignmentX(0.5F);
        jPanel6.add(clientsConnectingLabel);

        clientsConnectingDisplay.setFont(new java.awt.Font("Dialog", 1, 10));
        clientsConnectingDisplay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clientsConnectingDisplay.setText("0");
        clientsConnectingDisplay.setAlignmentX(0.5F);
        clientsConnectingDisplay.setPreferredSize(new java.awt.Dimension(32, 14));
        jPanel6.add(clientsConnectingDisplay);

        jPanel4.add(jPanel6);

        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 1, 1));

        clientsConnectedLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        clientsConnectedLabel.setText("Clients connected:");
        clientsConnectedLabel.setAlignmentX(0.5F);
        jPanel7.add(clientsConnectedLabel);

        clientsConnectedDisplay.setFont(new java.awt.Font("Dialog", 1, 10));
        clientsConnectedDisplay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clientsConnectedDisplay.setText("0");
        clientsConnectedDisplay.setAlignmentX(0.5F);
        clientsConnectedDisplay.setMaximumSize(new java.awt.Dimension(48, 14));
        clientsConnectedDisplay.setMinimumSize(new java.awt.Dimension(32, 14));
        clientsConnectedDisplay.setPreferredSize(new java.awt.Dimension(32, 14));
        jPanel7.add(clientsConnectedDisplay);

        jPanel4.add(jPanel7);

        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 1, 1));

        jPanel8.setMinimumSize(new java.awt.Dimension(175, 16));
        jPanel8.setPreferredSize(new java.awt.Dimension(175, 16));
        clientsForceDisconnectedLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        clientsForceDisconnectedLabel.setText("Clients forcibly disconnected:");
        clientsForceDisconnectedLabel.setAlignmentX(0.5F);
        jPanel8.add(clientsForceDisconnectedLabel);

        clientsForceDisconnectedDisplay.setFont(new java.awt.Font("Dialog", 1, 10));
        clientsForceDisconnectedDisplay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clientsForceDisconnectedDisplay.setText("0");
        clientsForceDisconnectedDisplay.setAlignmentX(0.5F);
        clientsForceDisconnectedDisplay.setMaximumSize(new java.awt.Dimension(48, 14));
        clientsForceDisconnectedDisplay.setMinimumSize(new java.awt.Dimension(32, 14));
        clientsForceDisconnectedDisplay.setPreferredSize(new java.awt.Dimension(32, 14));
        jPanel8.add(clientsForceDisconnectedDisplay);

        jPanel4.add(jPanel8);

        jPanel15.add(jPanel4);

        jPanel16.setMaximumSize(new java.awt.Dimension(10, 10));
        jPanel15.add(jPanel16);

        jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.Y_AXIS));

        jPanel12.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        jPanel12.setMaximumSize(new java.awt.Dimension(32767, 34));
        jPanel12.setMinimumSize(new java.awt.Dimension(95, 34));
        jPanel12.setPreferredSize(new java.awt.Dimension(121, 34));
        jPanel13.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 1, 1));

        clientsLoggingInLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        clientsLoggingInLabel.setText("Clients logging in:");
        clientsLoggingInLabel.setAlignmentX(0.5F);
        jPanel13.add(clientsLoggingInLabel);

        clientsLoggingInDisplay.setFont(new java.awt.Font("Dialog", 1, 10));
        clientsLoggingInDisplay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clientsLoggingInDisplay.setText("0");
        clientsLoggingInDisplay.setAlignmentX(0.5F);
        clientsLoggingInDisplay.setMaximumSize(new java.awt.Dimension(48, 14));
        clientsLoggingInDisplay.setMinimumSize(new java.awt.Dimension(32, 14));
        clientsLoggingInDisplay.setPreferredSize(new java.awt.Dimension(32, 14));
        jPanel13.add(clientsLoggingInDisplay);

        jPanel12.add(jPanel13);

        jPanel14.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 1, 1));

        clientsLoggedInLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        clientsLoggedInLabel.setText("Clients logged in:");
        clientsLoggedInLabel.setAlignmentX(0.5F);
        jPanel14.add(clientsLoggedInLabel);

        clientsLoggedInDisplay.setFont(new java.awt.Font("Dialog", 1, 10));
        clientsLoggedInDisplay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clientsLoggedInDisplay.setText("0");
        clientsLoggedInDisplay.setAlignmentX(0.5F);
        clientsLoggedInDisplay.setMaximumSize(new java.awt.Dimension(48, 14));
        clientsLoggedInDisplay.setMinimumSize(new java.awt.Dimension(32, 14));
        clientsLoggedInDisplay.setPreferredSize(new java.awt.Dimension(32, 14));
        jPanel14.add(clientsLoggedInDisplay);

        jPanel12.add(jPanel14);

        jPanel15.add(jPanel12);

        clientConnectionsPanel.add(jPanel15);

        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.Y_AXIS));

        jPanel5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        jPanel5.setMinimumSize(new java.awt.Dimension(196, 50));
        jPanel5.setPreferredSize(new java.awt.Dimension(196, 50));
        jPanel9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 1, 1));

        jPanel9.setMinimumSize(new java.awt.Dimension(160, 16));
        clientHbsPendingLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        clientHbsPendingLabel.setText("Client heartbeats pending:");
        clientHbsPendingLabel.setAlignmentX(0.5F);
        jPanel9.add(clientHbsPendingLabel);

        clientHbsPendingDisplay.setFont(new java.awt.Font("Dialog", 1, 10));
        clientHbsPendingDisplay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clientHbsPendingDisplay.setText("0");
        clientHbsPendingDisplay.setAlignmentX(0.5F);
        clientHbsPendingDisplay.setMaximumSize(new java.awt.Dimension(48, 14));
        clientHbsPendingDisplay.setMinimumSize(new java.awt.Dimension(48, 14));
        clientHbsPendingDisplay.setPreferredSize(new java.awt.Dimension(32, 14));
        jPanel9.add(clientHbsPendingDisplay);

        jPanel5.add(jPanel9);

        jPanel10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 1, 1));

        clientHbsFailedCurrentLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        clientHbsFailedCurrentLabel.setText("# failed current client heartbeats:");
        clientHbsFailedCurrentLabel.setAlignmentX(0.5F);
        jPanel10.add(clientHbsFailedCurrentLabel);

        clientHbsFailedCurrentDisplay.setFont(new java.awt.Font("Dialog", 1, 10));
        clientHbsFailedCurrentDisplay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clientHbsFailedCurrentDisplay.setText("0");
        clientHbsFailedCurrentDisplay.setAlignmentX(0.5F);
        clientHbsFailedCurrentDisplay.setMaximumSize(new java.awt.Dimension(48, 14));
        clientHbsFailedCurrentDisplay.setMinimumSize(new java.awt.Dimension(32, 14));
        clientHbsFailedCurrentDisplay.setPreferredSize(new java.awt.Dimension(32, 14));
        jPanel10.add(clientHbsFailedCurrentDisplay);

        jPanel5.add(jPanel10);

        jPanel11.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 1, 1));

        clientHbsFailedTotalLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        clientHbsFailedTotalLabel.setText("Total # failed client heartbeats:");
        clientHbsFailedTotalLabel.setAlignmentX(0.5F);
        jPanel11.add(clientHbsFailedTotalLabel);

        clientHbsFailedTotalDisplay.setFont(new java.awt.Font("Dialog", 1, 10));
        clientHbsFailedTotalDisplay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clientHbsFailedTotalDisplay.setText("0");
        clientHbsFailedTotalDisplay.setAlignmentX(0.5F);
        clientHbsFailedTotalDisplay.setPreferredSize(new java.awt.Dimension(32, 14));
        jPanel11.add(clientHbsFailedTotalDisplay);

        jPanel5.add(jPanel11);

        clientConnectionsPanel.add(jPanel5);

        globalStatsPanel.add(clientConnectionsPanel);

        getContentPane().add(globalStatsPanel);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-302)/2, (screenSize.height-365)/2, 302, 365);
    }//GEN-END:initComponents
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
   private static class FrameShower implements Runnable {
     final ExampleServer frame;
     public FrameShower(ExampleServer frame) {
       this.frame = frame;
     }
     public void run() {
       frame.setVisible(true); // This also needs to be replaced
       frame.start();
     }
   }
   
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
//        new ExampleServer().show();
        ExampleServer myApp=new ExampleServer();
        Runnable runner = new FrameShower(myApp);
        EventQueue.invokeLater(runner);
//        myApp.start();
//        new ExampleServer().start();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ServerCodeVersionPanel;
    private javax.swing.JPanel clientConnectionsPanel;
    private javax.swing.JLabel clientHbsFailedCurrentDisplay;
    private javax.swing.JLabel clientHbsFailedCurrentLabel;
    private javax.swing.JLabel clientHbsFailedTotalDisplay;
    private javax.swing.JLabel clientHbsFailedTotalLabel;
    private javax.swing.JLabel clientHbsPendingDisplay;
    private javax.swing.JLabel clientHbsPendingLabel;
    private javax.swing.JLabel clientsConnectedDisplay;
    private javax.swing.JLabel clientsConnectedLabel;
    private javax.swing.JLabel clientsConnectingDisplay;
    private javax.swing.JLabel clientsConnectingLabel;
    private javax.swing.JLabel clientsForceDisconnectedDisplay;
    private javax.swing.JLabel clientsForceDisconnectedLabel;
    private javax.swing.JLabel clientsLoggedInDisplay;
    private javax.swing.JLabel clientsLoggedInLabel;
    private javax.swing.JLabel clientsLoggingInDisplay;
    private javax.swing.JLabel clientsLoggingInLabel;
    private javax.swing.JLabel formNameLabel;
    private javax.swing.JLabel freeMemoryDisplay;
    private javax.swing.JLabel freeMemoryLabel;
    private javax.swing.JPanel globalStatsPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel maxMemoryDisplay;
    private javax.swing.JLabel maxMemoryLabel;
    private javax.swing.JPanel memoryInfoPanel;
    private javax.swing.JLabel serverVersionDisplay;
    private javax.swing.JLabel serverVersionLabel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel totalMemoryDisplay;
    private javax.swing.JLabel totalMemoryLabel;
    // End of variables declaration//GEN-END:variables
    
}
