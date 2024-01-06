/*
 * UniqueUserID.java
 *
 * Created on August 5, 2002, 8:56 PM
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

/** A class to be over-ridden that provides for a unique ID value linked to a
 * user of a server.
 * @author Richard A. Mead <BR> Information Montage
 * @version 1.0.0
 */

public abstract class UniqueUserID extends Object {
    
    /** Since this class is intended to provide unique IDs, cloning is not
     * supported, and this method always throws a CloneNotSupportedException.
     * @throws CloneNotSupportedException Cloning of a unique ID is not supported.
     * @return Would return null - except that cloning is not supported, and an
     * exception is thrown if this method is called.
     */    
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    /** The equals() method of the base Object class is used, and may not be
     * over-ridden.  TBD: allow over-riding
     * @param obj The Object to compare with.
     * @return true only if the Object obj is the same object as this instance.
     *  (this == obj)
     */    
//    public final boolean equals(Object obj) {
//        return super.equals(obj);
//    }
    
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    /** The hashcode() method of the base Object class is used, and may not be
     * over-ridden.  TBD: allow over-riding
     * @return int - the hashcode of this Object.
     */    
//    public final int hashcode() {
//        return super.hashCode();
//    }
    
    public int hashcode() {
        return super.hashCode();
    }
    
    /** The toString() method must be provided by the over-riding class.
     * @return String Must be over-ridden to return a String representation of the
     * unique ID.
     */    
    public abstract String toString();
    
}