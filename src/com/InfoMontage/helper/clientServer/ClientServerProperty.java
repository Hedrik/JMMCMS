/*
 * ClientServerProperty.java
 *
 * Created on December 23, 2002, 11:18 PM
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

import java.util.prefs.*;
import java.util.*;
import java.beans.*;
import java.lang.reflect.*;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public class ClientServerProperty {
    
    public static final Class[] validClasses = {
        String.class,
        Byte.class,
        Character.class,
        Integer.class,
        Long.class,
        Short.class,
        Double.class,
        Float.class,
        Boolean.class
    };
    
    public static final String[] validClassStrings = new String[validClasses.length];
    
    static {
        for (int i=0;i<validClasses.length;i++)
            validClassStrings[i] = validClasses[i].getName();
    }
    
    public static final Class[][] validClassArglists = new Class[validClasses.length][1];
    
    static {
        for (int i=0;i<validClasses.length;i++)
            validClassArglists[i] = new Class[] {validClasses[i]};
    }
    
    public static final Class[] nullClassArray = new Class[] {};
    public static final Class[] stringClassArray = new Class[] {String.class};
    
    private static final String NO_DEFAULT = "***{[No Default]}***";
    
    public final String PROPERTY_ID;
    
    private Object theProperty;
    
    private PropertyChangeSupport propertySupport;
    
    private final PropertyDescriptor[] propertyDescriptorArray = new PropertyDescriptor[1];
    
    private Preferences pref;
    
    /** Creates new ClientServerProperty */
    protected ClientServerProperty(Class AppClass, String PID, String PClass) {
        PROPERTY_ID = PID;
        initProperty(AppClass, PID, PClass, null);
    }
    
    protected ClientServerProperty(Class AppClass, String PID, Object PDefault) {
        PROPERTY_ID = PID;
        initProperty(AppClass, PID, null, PDefault);
    }
    
    protected ClientServerProperty(Class AppClass, String PID, String PClass, Object PDefault) {
        PROPERTY_ID = PID;
        initProperty(AppClass, PID, PClass, PDefault);
    }
    
    private void initProperty(Class AppClass, String PID, String PClass, Object PDefault) {
        //        if (!(Class.forName("com.InfoMontage.helper.clientServer.Server")
        //        .isAssignableFrom(AppClass) || Class
        //        .forName("com.InfoMontage.helper.clientServer.Client")
        //        .isAssignableFrom(AppClass))) {
        //            throw new InstantiationException();
        //        }
        if ((PDefault == null) && ((PClass == null) || (PClass == "")))
            PClass = "String"; // default property class
        if ((PDefault != null) && (PClass == null))
            PClass = PDefault.getClass().getName();
        if (!PClass.startsWith("java.lang."))
            PClass = "java.lang."+PClass;
        if ((PDefault != null) && (!PDefault.getClass().getName().equals(PClass)))
            // This should never happen!!
            throw new IllegalArgumentException(
            "Property class name ('"+PClass+"') and default (class '"
            +PDefault.getClass().getName()+"' value '"+PDefault.toString()
            +"') both provided, but default is not of class "+PClass+"!");
        int classListIndex=Arrays.asList(validClassStrings).indexOf(PClass);
        if (classListIndex<0)
            throw new IllegalArgumentException("Invalid class used for property");
        Class theClass = (Class)Arrays.asList(validClasses).get(classListIndex);
        String defValue = NO_DEFAULT;
        String defDefValue = NO_DEFAULT;
        if (AppClass!=null) {
//            System.out.println("Attempting to get preferences");
            Preferences sysPref = Preferences.systemNodeForPackage(AppClass);
//            System.out.println("Got system preferences at "+sysPref.absolutePath());
            if (PDefault != null) { // sysPref overrides provided default
                defDefValue = sysPref.get(PROPERTY_ID, NO_DEFAULT);
                if (defDefValue == NO_DEFAULT) {
                    // but if there's no sysPref,
                    // create it with provided default value
                    sysPref.put(PROPERTY_ID, PDefault.toString());
                    try {
                        sysPref.sync();
                    } catch(BackingStoreException e) {
                        System.out.println("Problem storing system preferences at "+sysPref.absolutePath());
                        System.out.println(e.getMessage());
                    }
                }
                // get it again and use provided default in case backing store
                // is unavailable
                defDefValue = sysPref.get(PROPERTY_ID, PDefault.toString());
            }
            else // no default provided, try getting sysPref value
                defDefValue = sysPref.get(PROPERTY_ID, NO_DEFAULT);
            pref = Preferences.userNodeForPackage(AppClass);
//            System.out.println("Got user preferences at "+pref.absolutePath());
            // userPrefs override sysPref and provided default
            defValue = pref.get(PROPERTY_ID, defDefValue);
        } else
            System.out.println("No application class passed - cannot use preferences!");
        // if we've still failed to get a default initial value, use "" or zero
        if (defValue == NO_DEFAULT) {
            if (PDefault == null)
                if (theClass == String.class)
                    defValue = "";
                else
                    defValue = "0";
            else
                defValue=PDefault.toString();
        }
        try {
            //            System.out.println("Creating property of class "+theClass);
            theProperty = theClass.getDeclaredConstructor(stringClassArray)
            .newInstance(new Object[] {defValue});
        } catch(Exception e) {
            System.out.println("Exception creating property of class "+theClass);
            System.out.println(e.getMessage());
            e.printStackTrace();
            // Never happens
            // TBD: do something just in case it does happen
        }
        //        System.out.println("Created property "+theProperty);
        propertySupport = new PropertyChangeSupport( this );
        try {
            propertyDescriptorArray[0] = new PropertyDescriptor(PROPERTY_ID
            ,theProperty.getClass(),"getProperty","setProperty");
        } catch(IntrospectionException e) {
            // Never happens
            // TBD: do something just in case it does happen
        }
        if (pref != null) {
//            System.out.println("Setting preference for property '"+PROPERTY_ID
//            +"' with value of '"+theProperty.toString()+"'");
            pref.put(PROPERTY_ID, theProperty.toString());
            try {
                pref.sync();
            } catch(BackingStoreException e) {
                System.out.println("Problem storing user preferences at "+pref.absolutePath());
                System.out.println(e.getMessage());
            }
        }
    }
    
    private void setValue(String newValue) {
        Method m = null;
        try {
            m = theProperty.getClass().getMethod("valueOf", stringClassArray);
        } catch(NoSuchMethodException e) {
            // Never happens
            // TBD: do something just in case
        }
        try {
            theProperty = m.invoke(theProperty, new Object[] {newValue});
        } catch(InvocationTargetException e) {
            // Never happens
            // TBD: do something just in case
        } catch(IllegalAccessException e) {
            // Never happens
            // TBD: do something just in case
        }
    }
    
    protected void setProperty(Object value) {
        if (theProperty.getClass().isInstance(value)) {
            Object oldValue = theProperty;
            setValue(value.toString());
            if (pref != null) {
                pref.put(PROPERTY_ID, theProperty.toString());
                try {
                    pref.sync();
                } catch(BackingStoreException e) {
                    System.out.println("Problem storing user preferences at "+pref.absolutePath());
                    System.out.println(e.getMessage());
                }
            }
            propertySupport.firePropertyChange(PROPERTY_ID, oldValue, theProperty);
        }
    }
    
    public Object getProperty() {
        return theProperty;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        return propertyDescriptorArray;
    }
    
    protected void finalize() {
        pref=null;
        propertyDescriptorArray[0]=null;
        Iterator i = Arrays.asList(propertySupport
        .getPropertyChangeListeners()).iterator();
        while (i.hasNext()) {
            propertySupport.removePropertyChangeListener(
            (PropertyChangeListener)i.next());
        }
        i=null;
        propertySupport=null;
        theProperty=null;
    }
    
}
