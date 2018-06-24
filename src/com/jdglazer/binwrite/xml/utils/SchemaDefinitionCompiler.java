package com.jdglazer.binwrite.xml.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchemaDefinitionCompiler {
	
    private final String TAG_LIST_DELIMETER           = ",";
	
	private final String TAG_NAME_FIELD_REGEX         = "^(.*)_TAG$";
	
	private final String TAG_OPTIONAL_CHILDREN_FIELD  = "%s_TAG_CHILDREN_OPTIONAL";
	
	private final String TAG_REQUIRED_CHILDREN_FIELD  = "%s_TAG_CHILDREN_REQUIRED";
	
	private final String TAG_REQUIRED_ATTRIBUTE_FIELD = "%s_TAG_ATTRIBUTES_REQUIRED";
	
	private final String TAG_OPTIONAL_ATTRIBUTE_FIELD = "%s_TAG_ATTRIBUTES_OPTIONAL";
    
    private HashMap<String,TagModel> tagMap;
    
    private Object schemaProvider = null;
    
    public SchemaDefinitionCompiler(Object schemaProvider) throws IllegalArgumentException, IllegalAccessException {
    	if( schemaProvider == null )
    		return;
    	this.schemaProvider = schemaProvider;
    	tagMap = new HashMap<String,TagModel>();
    	compileTags();
    }
    
    private void compileTags() throws IllegalArgumentException, IllegalAccessException {
    	
    	Field [] fields = schemaProvider.getClass().getDeclaredFields();
    	
    	// First we create minimally configured tags in our listing
    	for( Field f : fields ) {
    		if( f.getType().getSimpleName().equals("String") ) {
    			Matcher matcher = Pattern.compile(TAG_NAME_FIELD_REGEX).matcher(f.getName());
    		    if( matcher.matches()) {
	    		    String tagName = (String) f.get(this);
	    		    TagModel tag = new TagModel(tagName);
	    		    tagMap.put(tagName, tag);
    		    }
    		}
    	}
    	compileAllowedValues();
    }
    
    private void compileAllowedValues() {
    	for( String tagName : tagMap.keySet() ) {
    		TagModel tag = tagMap.get(tagName);
    		parseOptionalChildren(tag);
    		parseRequiredChildren(tag);
    		parseOptionalAttribute(tag);
    		parseRequiredAttribute(tag);
    	}
    }
    
    private void parseOptionalChildren(TagModel tag) {
    	List<TagModel> tags = parseTagList(TAG_OPTIONAL_CHILDREN_FIELD, tag.getName().toUpperCase());
    	for( TagModel t : tags ) {
    		tag.addOptionalChild(t);
    	}
    }
    
    private void parseRequiredChildren(TagModel tag) {
    	List<TagModel> tags = parseTagList(TAG_REQUIRED_CHILDREN_FIELD, tag.getName().toUpperCase());
    	for( TagModel t : tags ) {
    		tag.addRequiredChild(t);
    	}
    }
    
    private void parseOptionalAttribute(TagModel tag) {
    	List<String> attrs = parseAttrList(TAG_OPTIONAL_ATTRIBUTE_FIELD, tag.getName().toUpperCase());
    	for( String attr : attrs ) {
    		tag.addOptionalAttribute(attr);
    	}
    }    
    
    private void parseRequiredAttribute(TagModel tag) {
    	List<String> attrs = parseAttrList(TAG_REQUIRED_ATTRIBUTE_FIELD, tag.getName().toUpperCase());
    	for( String attr : attrs ) {
    		tag.addRequiredAttribute(attr);
    	}
    }  
    
    private List<TagModel> parseTagList(String format, String...replacements) {
    	ArrayList<TagModel> tags = new ArrayList<TagModel>();
		try {
    		String expectedFieldName = String.format(format, replacements);
    		Field field = schemaProvider.getClass().getField(expectedFieldName);
    		String config = (String) field.get(this);
    		String [] values = config.split(TAG_LIST_DELIMETER);
    		for( String value : values ) {
    		    TagModel tag = tagMap.get(value);
    		    if( tag != null ) {
    		    	tags.add(tag);
    		    } else{
    		    	// log warning about tag not being configured in schema definition class
    		    }
    		}
    		
    		
		} catch( NoSuchFieldException nsfe ) { }
		catch( Exception e){}
    	return tags;
    }
 
    private List<String> parseAttrList(String format, String...replacements) {
    	ArrayList<String> attrs = new ArrayList<String>();
		try {
    		String expectedFieldName = String.format(format, replacements);
    		Field field = schemaProvider.getClass().getField(expectedFieldName);
    		String config = (String) field.get(this);
    		String [] values = config.split(TAG_LIST_DELIMETER);
    		for( String value : values ) {
    		   attrs.add(value);
    		}
    		
    		
		} catch( NoSuchFieldException nsfe ) {}
		catch( Exception e){}
    	return attrs;
    }
    
    public boolean isOptionalChild( String tag, String child ) {
    	TagModel t = tagMap.get(tag);
    	if ( t == null ) {
    		return false;
    	} else {
    		return t.isChildOptional(child);
    	}
    }
    
    public boolean isRequiredChild( String tag, String child ) {
    	TagModel t = tagMap.get(tag);
    	if ( t == null ) {
    		return false;
    	} else {
    		return t.isChildRequired(child);
    	}
    }
    
    public boolean isAllowedChild( String tag, String child ) {
    	return isRequiredChild(tag,child) || isOptionalChild(tag,child);
    }
    
    public boolean isOptionalAttr( String tag, String attr ) {
    	TagModel t = tagMap.get(tag);
    	if ( t == null ) {
    		return false;
    	} else {
    		return t.isAttributeOptional(attr);
    	}
    }
    
    public boolean isRequiredAttr( String tag, String attr ) {
    	TagModel t = tagMap.get(tag);
    	if ( t == null ) {
    		return false;
    	} else {
    		return t.isAttributeRequired(attr);
    	}
    }
    
    public boolean isAllowedAttr( String tag, String attr ) {
    	return isRequiredAttr(tag,attr) || isOptionalAttr(tag,attr);
    }
    
    public HashMap<String,TagModel> getTagModels() {
    	return this.tagMap;
    }
}
