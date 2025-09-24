/*
 * CommElement.java
 *
 * Created on July 3, 2001, 9:43 PM
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
public final class CommElement {
    // static final java.nio.charset.Charset chrset=java.nio.charset.Charset.forName("US-ASCII");
    
    public CommTag tag=null;
    public ByteBuffer pByte=null; // byte array payload
    public String pString=null; // String payload
    
    static String displayByteBuffer(final ByteBuffer b) {
        return com.InfoMontage.util.Buffer.toString(b,DefaultCommConstants.chrset);
//        if (b==null)
//            return "[null]";
//        else
//            synchronized (b) {
//                return "'"+(((b.position())==0 && (b.limit()==b.capacity()))?""
//                :DefaultCommConstants.chrset.decode(b.asReadOnlyBuffer()).toString())
//                +"'="+b.position()+","+b.limit()+","+b.capacity();
//            }
    }
    
    synchronized public String toString() {
        StringBuffer rs=new StringBuffer();
        if (tag==null)
            return "[null]";
        else {
            synchronized (tag) {
                rs.append(tag.sTag);
                if (pByte==null)
                    if (pString!=null)
                        synchronized (pString) {
                            rs.append("{"+pString+"}");
                        }
                    else {}
                else {
                    synchronized (pByte) {
                        rs.append("["+DefaultCommConstants.chrset.decode(pByte).toString()+"]");
                    }
                }
                rs.append((tag.pLen<0)?"/"+tag.eTag:"");
            }
        }
        return rs.toString();
    }
    
    public static CommElement nextElement(ByteBuffer bb) {
        synchronized (bb) {
            CommElement e=null;
            CommTag t=CommTag.nextTag(bb);
            if (t!=null) {
                e=new CommElement(); // shouldn't need to synchronize since it's new
                e.tag=t;
                if (t.pLen>0) {
                    byte[] tba=new byte[t.pLen];
                    bb.mark();
                    try {
                        bb.get(tba);
                    } catch (java.nio.BufferUnderflowException ex) {
                        bb.reset();
                        e.pByte=null;
                        e=null;
                        System.err.println("Comm error: expected "+t.pLen+" bytes, had "
                        +bb.remaining());
                        System.err.println(ex.getMessage());
                        ex.printStackTrace();
                    }
                    e.pByte=ByteBuffer.wrap(tba);
                } else {
                    if (t.pLen==-1) {
                        bb.mark();
                        //                        StringBuffer sb=new StringBuffer();
                        ByteBuffer sb=ByteBuffer.allocate(bb.remaining()+1);
                        boolean looking=true;
                        byte cb;
                        while (looking && bb.hasRemaining()) {
                            cb=bb.get();
                            if (t.eTagAsBB.get(0)!=cb) {
                                //                                sb.append(cb);
                                sb.put(cb);
                            } else {
                                int i=0;
                                while (((i+1)<t.eTagAsBB.remaining())
                                &&(i<bb.remaining())
                                &&(bb.get(bb.position()+i)==t.eTagAsBB.get(i+1))) {
                                    i++;
                                }
                                if (i==(t.eTagAsBB.remaining()-1)) {
                                    looking=false;
                                    bb.position(bb.position()+i);
                                } else {
                                    //                                    sb.append(cb);
                                    sb.put(cb);
                                }
                            }
                        }
                        if (looking) {
                            bb.reset();
                            sb=null;
                            e=null;
                            System.err.println("Comm error: expected end tag '"+t.eTag+"'");
                            System.err.flush();
                        } else {
                            sb.rewind();
                            e.pString=DefaultCommConstants.chrset.decode(sb).toString();
                            System.err.println("Decoded '"+e.pString+"'");
                            System.err.flush();
                            sb=null;
                        }
                    }
                }
            }
            return e;
        }
    }
    
    public static final class CommTag {
        public String sTag; // tag as String
        public String eTag; // ending tag as String
        public int pLen; // payload length or -1
        public ByteBuffer sTagAsBB; // tag as ByteBuffer
        public ByteBuffer eTagAsBB; // ending tag as ByteBuffer
        
        // Note: hashtables are synchronized
        static final java.util.Hashtable tags=new java.util.Hashtable(50);
        static int minLen=Integer.MAX_VALUE;
        static int maxLen=0;
        
        public CommTag(String t) {
            initCommTag(t,null,0);
        }
        
        public CommTag(String t, int p) {
            initCommTag(t,null,p);
        }
        
        public CommTag(String t, String e) {
            initCommTag(t,e,-1);
        }
        
        private void initCommTag(String t, String e, int l) {
            if (t==null)
                throw new RuntimeException("Creation of null CommTag!");
            Object oldVal=null;
            sTag=t;
            eTag=e;
            pLen=l;
            oldVal=tags.put(t,this);
            //        System.err.println("Add '"+t+"'='"+this.toString()+"' giving total of "+tags.size());
            if (oldVal!=null)
                throw new RuntimeException("Creation of duplicate CommTag!\n"
                +"oldVal="+oldVal+"\nnewVal="+this);
            if (t.length()<minLen)
                minLen=t.length();
            if (t.length()>maxLen)
                maxLen=t.length();
            sTagAsBB=DefaultCommConstants.chrset.encode(sTag);
            sTagAsBB.rewind(); // needed?
            if (e!=null) {
                eTagAsBB=DefaultCommConstants.chrset.encode(eTag);
                eTagAsBB.rewind(); // needed?
            }
            else
                eTagAsBB=ByteBuffer.allocate(0);
        }
        
        public ByteBuffer encode(ByteBuffer b, String s) {
            if (b==null)
                throw new Error("Error: Attempt to encode to null buffer!");
            synchronized (b) {
                if (s==null || s.length()==0)
                    if (pLen==0)
                        b=encode(b);
                    else
                        throw new Error("Error: Attempt to encode invalid data for tag '"
                        +toString()+"'\ndata was 'null' string (expected "+pLen+" bytes)");
                else
                    synchronized (sTagAsBB) {
                        synchronized (eTagAsBB) {
                            System.err.println("Adding '"+sTag+"' ("+displayByteBuffer(sTagAsBB)+") of {"+s+"} to "+displayByteBuffer(b));
                            if ((pLen==-1 && eTag!=null) || (s.getBytes().length==pLen)) {
                                b.put(sTagAsBB).put(DefaultCommConstants.chrset.encode(s))
                                .put(eTagAsBB);
                                sTagAsBB.rewind();
                                eTagAsBB.rewind();
                            }
                            else
                                throw new Error("Error: Attempt to encode invalid data for tag '"
                                +toString()+"'\ndata was \""+s+"\"  (len="
                                +DefaultCommConstants.chrset.encode(s).remaining()
                                +", expected "+pLen+")");
                        }
                    }
            }
            return b;
        }
        
        public ByteBuffer encode(ByteBuffer b, final byte d) {
            if (b==null)
                throw new Error("Error: Attempt to encode to null buffer!");
            synchronized (b) {
                if (pLen==1) {
                    synchronized (sTagAsBB) {
                        synchronized (eTagAsBB) {
                            System.err.println("Adding '"+sTag+"' ("+displayByteBuffer(sTagAsBB)+") of {"+String.valueOf(d)+"} to "+displayByteBuffer(b));
                            b.put(sTagAsBB).put(d);
                            sTagAsBB.rewind();
                        }
                    }
                }
                else
                    throw new Error("Error: Attempt to encode invalid data for tag '"
                    +toString()+"'\ndata was byte '"+d+"' (expected "+pLen+" bytes)");
            }
            return b;
        }
        
        public ByteBuffer encode(ByteBuffer b, final Byte d) {
            if (b==null)
                throw new Error("Error: Attempt to encode to null buffer!");
            synchronized (b) {
                if (d==null)
                    throw new Error("Error: Attempt to encode to null Byte!");
                b=encode(b,d.byteValue());
            }
            return b;
        }
        
        public ByteBuffer encode(ByteBuffer b, final byte[] d) {
            if (b==null)
                throw new Error("Error: Attempt to encode to null buffer!");
            synchronized (b) {
                if (d==null)
                    if (pLen==0)
                        b=encode(b);
                    else
                        throw new Error("Error: Attempt to encode invalid data for tag '"
                        +toString()+"'\ndata was 'null' (expected "+pLen+" bytes)");
                else
                    if (d==null)
                        throw new Error("Error: Attempt to encode to null byte array!");
                synchronized (d) {
                    if (pLen==d.length) {
                        synchronized (sTagAsBB) {
                            b.put(sTagAsBB).put(d);
                            sTagAsBB.rewind();
                        }
                    }
                    else
                        throw new Error("Error: Attempt to encode invalid data for tag '"
                        +toString()+"'\ndata was byte[] '"+d+"' (len="+d.length
                        +", expected "+pLen+")");
                }
            }
            return b;
        }
        
        public ByteBuffer encode(ByteBuffer b) {
            if (b==null)
                throw new Error("Error: Attempt to encode to null buffer!");
            synchronized (b) {
                if (pLen==0) {
                    synchronized (sTagAsBB) {
                        System.err.println("Adding '"+sTag+"' ("+displayByteBuffer(sTagAsBB)+") to "+displayByteBuffer(b));
                        b.put(sTagAsBB);
                        sTagAsBB.rewind();
                    }
                }
                else
                    throw new Error("Error: Attempt to encode invalid data for tag '"
                    +toString()+"'\nno data provided (expected "+pLen+" bytes)");
            }
            return b;
        }
        
        public static CommTag nextTag(ByteBuffer b) {
            //        ByteBuffer bb=b.slice(); // slice doesn't work right?
            CommTag rv=null;
            if (b!=null) {
                synchronized (b) {
                    // System.err.println("Getting next tag from "+displayByteBuffer(b));
                    int obp=b.position();
                    // b.mark();
                    java.nio.CharBuffer cb=DefaultCommConstants.chrset.decode(b.asReadOnlyBuffer());
                    // b.reset();
                    int i=minLen;
                    String cbs=null;
                    boolean found=false;
                    //        int m=tags.size();
                    //        System.err.println("numTags="+m+"\n'"+cb.toString()+"'");
                    //            long st=System.currentTimeMillis();
                    while ((i<cb.remaining())&&(i<=maxLen)&&(!found)) {
                        cbs=cb.subSequence(0,i).toString();
                        // System.err.println("Checking if '"+cbs+"' is a tag...");
                        if(tags.containsKey(cbs))
                            found=true;
                        else
                            i++;
                    }
                    // if ((i<cb.remaining())&&(i<=maxLen)) {
                    if (found) {
                        //CharSequence nb=cb.subSequence(0,i+1);
                        //rv=(CommTag)tags.get(nb.toString());
                        rv=(CommTag)tags.get(cbs);
                        // synchronize this next on rv?
                        System.err.println("Found tag '"+rv.toString()+"'");
                        //            st-=System.currentTimeMillis();
                        //            System.err.println("Finding tag took "+(-st)+" ms!");
                    }
                    if (rv!=null) {
                        synchronized (rv) {
                            //System.err.println("Incrementing position by '"+rv.sTagAsBB.limit()+"'");
                            synchronized (rv.sTagAsBB) {
                                b.position(obp+rv.sTagAsBB.limit());
                            }
                        }
                    }
                }
            }
            return rv;
        }
        
        public String toString() {
            return sTag+((pLen>0)?"("+String.valueOf(pLen)+")"
            :((pLen<0)?"/"+eTag:""));
        }
    }
}
