/*
 * CommSequence.java
 *
 * Created on February 7, 2003, 11:07 PM
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

import com.InfoMontage.helper.clientServer.CommElement.CommTag;
import java.nio.ByteBuffer;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public class CommSequence extends java.util.ArrayList {
    
    private static final int defaultSequenceLength=3;
    
    /** Creates a new instance of CommSequence */
    public CommSequence() {
        super(defaultSequenceLength);
    }
    
    public CommSequence(CommTag[] cta) {
        super(java.util.Arrays.asList(cta));
    }
    
    synchronized public void add(int index, CommTag element) {
        super.add(index,element);
    }
    
    synchronized public void add(int index, Object element) {
        throw new IllegalArgumentException("Attempt to add an object of type "
        +element.getClass().getName()+" instead of a CommTag!");
    }
    
    synchronized public boolean add(CommTag o) {
        return super.add(o);
    }
    
    synchronized public boolean add(Object o) {
        throw new IllegalArgumentException("Attempt to add an object of type "
        +o.getClass().getName()+" instead of a CommTag!");
    }
    
    synchronized public boolean addAll(int index, java.util.Collection c) {
        throw new UnsupportedOperationException();
    }
    
    synchronized public boolean addAll(java.util.Collection c) {
        throw new UnsupportedOperationException();
    }
    
    synchronized public Object set(int index, CommTag element) {
        return super.set(index, element);
    }
    
    synchronized public Object set(int index, Object element) {
        throw new IllegalArgumentException("Attempt to replace a CommTag with an object of type "
        +element.getClass().getName()+" instead of a CommTag!");
    }
    
    synchronized public CommElement[] parseBuffer(ByteBuffer b) {
        CommElement[] rcea=null;
        if (b!=null) {
            synchronized (b) {
                rcea=new CommElement[size()];
                boolean validParse=true;
                int i;
                CommElement ne;
                b.mark();
                for (i=0;validParse && (i<size());i++) {
                    ne=CommElement.nextElement(b);
                    if (((CommTag)get(i)).equals(ne.tag))
                        rcea[i] = ne;
                    else
                        validParse = false;
                }
                if (!validParse) {
                    b.reset();
                    rcea=null;
                }
            }
        }
        return rcea;
    }
    
    synchronized public ByteBuffer buildBuffer(Object[] p) {
        ByteBuffer b=ByteBuffer.allocate(com.InfoMontage.helper.clientServer.DefaultCommConstants.CommSock.ClientChannelBufferCapacity);
        boolean validParse=true;
        int i;
        int pidx=0;
        Object cp;
        Class cpc;
        CommTag ct;
        for (i=0;validParse && (i<size());i++) {
            ct=(CommTag)get(i);
            if (ct.pLen!=0) {
                if (p!=null) {
                    synchronized (p) {
                        if (!(pidx<p.length))
                            validParse=false;
                        else {
                            cp=p[pidx];
                            cpc=cp.getClass();
                            if (ct.pLen==1)
                                if (cpc.getName()=="java.lang.Byte") {
                                    b=ct.encode(b, (Byte)cp);
                                }
                                else
                                    validParse=false;
                            else
                                if (ct.pLen>0)
                                    if (cpc.getName()=="[B")
                                        b=ct.encode(b, (byte[])cp);
                                    else
                                        //                                if (cpc.getName()=="[Ljava.lang.Byte")
                                        //                                    b=ct.encode(b, ((Byte[])cp).);
                                        //                                else
                                        validParse=false;
                                else
                                    if (ct.pLen==-1)
                                        if (cpc.getName()=="java.lang.String")
                                            b=ct.encode(b, (String)cp);
                                        else
                                            validParse=false;
                                    else // should never happen!
                                        validParse=false;
                            if (validParse)
                                pidx++;
                        }
                    }
                }
            }
            else
                b=ct.encode(b);
        }
        if (!validParse) {
            System.err.println("Invalid parsing construct or parameters!  Last parameter attempted was index "+pidx+".  Partial parse is:\n"+CommElement.displayByteBuffer((ByteBuffer)b.asReadOnlyBuffer().flip()));
            b=null;
        }
        b.flip();
        System.err.println("buildBuffer: Parsed parameters gave:\n"+CommElement.displayByteBuffer(b));
        return b;
    }
    
}
