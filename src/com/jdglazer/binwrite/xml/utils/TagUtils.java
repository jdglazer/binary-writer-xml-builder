package com.jdglazer.binwrite.xml.utils;

import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

public abstract class TagUtils {
	
    public static String getAttribute( Tag tag, String attr, Properties propertyReplacements ) {
  	  
  	  if ( tag == null || attr == null ) {
  		  System.out.println("ERROR: NULL arguments to getAttribute");
  		  return null;
  	  }
  	  
  	  String value = tag.getAttributes().getValue(attr);
  	  
  	  if( value != null ) {
  		  
  		  value = value.trim();
  		  
  		  if( propertyReplacements != null ) {
  			  
  		      String propertyMapping = propertyReplacements.getProperty(value);
  		  
  		      return propertyMapping != null ? propertyMapping : value;
  		  }
  		  
  		  return value;
  	  }
  	  
  	  return null;
    }  
	
    public static boolean getAttributeAsBoolean( Tag tag, String attr, Properties propertyReplacements ) {
  	  
  	  String value = getAttribute(tag,attr,propertyReplacements);
  	  
  	  if( value == null ) {
  		  return false;
  	  }
  	  
  	  return Boolean.parseBoolean(value);
    }
    
    public static String getAttributeAsString( Tag tag, String attr, Properties propertyReplacements, String [] allowedValues ) {
  	  
  	  String value = getAttribute(tag,attr,propertyReplacements);
  	  
  	  if( allowedValues != null && value != null) {
		      if( ArrayUtils.contains( allowedValues, value) ) {
		    	  return value;
		      }
  	  } else {
  		  return value;
  	  }
  	  
  	  return null;
    }
    
    public static String getAttributeAsString( Tag tag, String attr, Properties propertyReplacements, Set<String> allowedValues ) {
    	  
    	  String value = getAttribute(tag,attr,propertyReplacements);
    	  
    	  if( allowedValues != null && value != null) {
  		      if( allowedValues.contains(value) ) {
  		    	  return value;
  		      }
    	  } else {
    		  return value;
    	  }
    	  
    	  return null;
      }
    
    public static Integer getAttributeAsInteger(Tag tag, String attr, Properties propertyReplacements, Integer [] allowedValues) {
  	  
  	  String value = getAttribute(tag,attr,propertyReplacements);
  	  
  	  Integer i;
  	  
  	  try { 
  	      i = new Integer( Integer.parseInt(value) );
  	  } catch( Exception e ) {
  		  System.out.println("WARNING: Invalid integer format provided");
  		  return null; 
  	  }
  	  
  	  if( allowedValues != null ) {
		      if( ArrayUtils.contains( allowedValues, i) ) {
		    	  return i;
		      }
  	  } else {
  		  return i;
  	  }
  	  
  	  return null;
    }
    
    private Double getAttributeAsDouble(Tag tag, String attr, Properties propertyReplacements, Double [] allowedValues) {
  	  
  	  String value = getAttribute(tag,attr,propertyReplacements);
  	  
  	  Double d;
  	  
  	  try { 
  	      d = new Double( Double.parseDouble(value) );
  	  } catch( Exception e ) {
  		  System.out.println("WARNING: Invalid integer format provided");
  		  return null; 
  	  }
  	  
  	  if( allowedValues != null ) {
		      if( ArrayUtils.contains( allowedValues, d) ) {
		    	  return d;
		      }
  	  } else {
  		  return d;
  	  }
  	  
  	  return null;
    }
}
