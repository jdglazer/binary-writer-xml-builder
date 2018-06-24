package com.jdglazer.binwrite.xml.utils;

import java.util.HashMap;

import org.apache.commons.lang.RandomStringUtils;

public class TagModel {
	
	 private static final String OPTIONAL_STRING = "optional";
	 private static final String REQUIRED_STRING = "required";
	 
     private final String name;
     private HashMap<String,TagModel> optionalChildren;
     private HashMap<String,TagModel> requiredChildren;
     private HashMap<String,String> allowedAttributes;
     
     public TagModel(String name) {
    	 if( name == null ) {
    		 name = RandomStringUtils.random(10);
    	 }
    	 this.name = name;
         optionalChildren = new HashMap<String,TagModel>();
         requiredChildren = new HashMap<String,TagModel>();
         allowedAttributes = new HashMap<String,String>();
     }
     
     public void addOptionalChild(TagModel tag) {
    	 optionalChildren.put(tag.getName(), tag);
     }
     
     public void addRequiredChild(TagModel tag) {
    	 requiredChildren.put(tag.getName(), tag);
     }
     
     public void addOptionalAttribute(String attr) {
    	 allowedAttributes.put(attr, OPTIONAL_STRING);
     }
  
     public void addRequiredAttribute(String attr ) {
    	 allowedAttributes.put(attr, REQUIRED_STRING);
     }
     
     public String getName() {
    	 return this.name;
     }
     
     public boolean isAttributeAllowed( String name ) {
    	 return allowedAttributes.get(name) != null;
     }
     
     public boolean isChildAllowed( String name ) {
    	 return optionalChildren.get(name) != null || isChildRequired(name);
     }
     
     public boolean isChildRequired( String name ) {
    	 return requiredChildren.get( name ) != null;
     }
     
     public boolean isChildOptional( String name ) {
    	 return optionalChildren.get( name ) != null;
     }
     
     public boolean isAttributeOptional(String name) {
    	 String optreq = allowedAttributes.get(name);
    	 return optreq == null ? false : optreq.equals(OPTIONAL_STRING);
     }
     
     public boolean isAttributeRequired(String name) {
    	 String optreq = allowedAttributes.get(name);
    	 return optreq == null ? false : optreq.equals(REQUIRED_STRING);
     }
}
