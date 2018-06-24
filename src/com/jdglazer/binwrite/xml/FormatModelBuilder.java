package com.jdglazer.binwrite.xml;

import java.util.HashMap;
import java.util.Properties;
import java.util.Stack;

import org.apache.commons.lang.RandomStringUtils;

import com.jdglazer.binwrite.dataaccess.DataElementPrototype;
import com.jdglazer.binwrite.dataaccess.DataType;
import com.jdglazer.binwrite.dataaccess.types.complex.ArrayPrototype;
import com.jdglazer.binwrite.dataaccess.types.complex.ObjectPrototype;
import com.jdglazer.binwrite.dataaccess.types.complex.StringPrototype;
import com.jdglazer.binwrite.dataaccess.types.primitive.BitPrototype;
import com.jdglazer.binwrite.dataaccess.types.primitive.BooleanPrototype;
import com.jdglazer.binwrite.dataaccess.types.primitive.BytePrototype;
import com.jdglazer.binwrite.dataaccess.types.primitive.CharPrototype;
import com.jdglazer.binwrite.dataaccess.types.primitive.FloatPrototype;
import com.jdglazer.binwrite.dataaccess.types.primitive.IntegerPrototype;
import com.jdglazer.binwrite.dataaccess.types.primitive.PrimitivePrototype;
import com.jdglazer.binwrite.xml.utils.Tag;
import com.jdglazer.binwrite.xml.utils.TagUtils;

public class FormatModelBuilder {
	  
	  private boolean parseComplete;
	  
	  private Stack<Tag> stack;
	  
	  private boolean stopParsing;
	  
	  Properties properties;
	  
	  private String currentFormatName;
	  
	  private HashMap<String,ObjectPrototype> formats;
	  
	  private HashMap<String,DataElementPrototype> dataElements;
	  
/**
 * constructor	  
 */
	  public FormatModelBuilder() {
		  parseComplete = false;
	  }

/**
 * 
 * When we start the document we need to perform some basic initialization functions
 * 
 */
	  public void startDocument() {
		  
		  formats = new HashMap<String,ObjectPrototype>();
		  
		  dataElements = new HashMap<String,DataElementPrototype>();
		  
		  stack = new Stack<Tag>();
		  
		  properties = new Properties();
		  
		  stopParsing = false;
	  }

/**
 * 
 * turns a start tag into a data type prototype
 * @param tag
 * 
 */
	  public void startTag( Tag tag ) {
		  // let' grab the parent tag
		  Tag parentTag = stack.isEmpty() ? null : stack.peek();
		  
		  // Do we need to stop parsing because of a schema violation?
		  if( !tag.isValid() ) {
			  
			  stopParsing = true;
			  
		  } else if( !stack.isEmpty() && tag.isValid() ) {
			  
			  if( !stack.peek().isValid() ) {
				  
				  tag.setValid(false);
				  
				  stopParsing = true;
			  }
		  }

		  if( !stopParsing ) {
			  String tagName = tag.getTagName();
			  
			  if( tagName.equals( SchemaDefinition.BINARYFORMATS_TAG ) ) {
				  
				  handleBinaryFormats( tag, parentTag );
				  
			  } else if( tagName.equals( SchemaDefinition.PROPERTY_TAG ) ) {
				  
				  handleProperty( tag, parentTag );
				  
			  } else if( tagName.equals( SchemaDefinition.FORMAT_TAG ) ) {
				  
				  handleFormat( tag, parentTag );
				  
			  } 
			  
			  // Don't allow parsing elements without being inside a format object
			  if( currentFormatName != null ) {
				  
				  if ( tagName.equals( SchemaDefinition.OBJECT_TAG ) ) {
					      
					  handleObject( tag, parentTag );
	
				  } else if( tagName.equals( SchemaDefinition.STRING_TAG ) ) {
	
					  handleString( tag, parentTag );
					  
				  } else if( tagName.equals( SchemaDefinition.ARRAY_TAG ) ){
					  
					  handleArray( tag, parentTag );
					  
				  } else if( tagName.equals( SchemaDefinition.INT_TAG ) || tagName.equals( SchemaDefinition.FLOAT_TAG ) 
						  || tagName.equals( SchemaDefinition.BOOL_TAG ) || tagName.equals( SchemaDefinition.BITS_TAG ) 
						  || tagName.equals( SchemaDefinition.CHAR_TAG ) || tagName.equals( SchemaDefinition.BYTE_TAG ) ) {
					  
					  handlePrimitive( tag, parentTag );
				  }
			  }
		  }
		  
		  stack.push( tag );
	  }

/**
 * 
 * The end tag element is mainly concerned with xml stack management
 * 	  
 */
	  public void endTag() {
		  if( stack.isEmpty() )
			  return;
		  
		  Tag tag = stack.pop();

		  if( tag.getTagName().equals("Format") ) {
			  currentFormatName = null;
		  }
		  
		  stopParsing = !stack.isEmpty() ? !stack.peek().isValid() : false;
	  }

/**
 * 
 * Any final closing tasks when parsing is done
 * 	  
 */
	  public void endDocument() {
		  
		  /** We need to go through the list and point all objects 
		   *  that reference the definition of another to the referenced 
		   *  object in the mapping
		   */
		  
		  for( String name : dataElements.keySet() ) {
			  
			  DataElementPrototype p = dataElements.get(name);
			  
			  if( p != null && p.getDataType().equals( DataType.OBJECT ) ) {
				  
				  ObjectPrototype o = (ObjectPrototype) p;
				  
				  String ref = o.getElementReference();
				  
				  if( ref != null && dataElements.get(ref) != null ) {
					  
					  dataElements.put(p.getName(), dataElements.get(ref) );
				  }
				  
			  }
		  }
		  
		  setComplete( true );
	  }

/**
 * 
 * Handles the case where there are characters enclosed within a tag	  
 * @param value The string value contained within a tag
 * 
 */
	  public void characters( String value ) {
		  
		  if( !stack.isEmpty() && value != null ) {
			  
			  Tag lastTag = stack.peek();
			  
			  if( lastTag.getTagName().equals("Property") ) {
				  String name = TagUtils.getAttribute( lastTag,"name",properties );
				  properties.setProperty( name, value.trim() );
			  }
		  }
	  }

/**
 * 
 * Sets a variable when parsing is complete to allow the
 * dispensing of format prototypes
 * @param complete
 * 
 */
      void setComplete( boolean complete ) {
    	  parseComplete = complete;
      }
 
/**
 * 
 * Gets the completeness of the parse operation
 * @return true if parsing is complete, false if in progress or hasn't started
 * 
 */
      public boolean getComplete(){
    	  return parseComplete;
      }   
      
/**
 * 
 * A utility function to help us extract basic, universal attributes from tags and store
 * in prototypes ( name, description, id )
 * @param tag The tag we are extracting attributes from
 * @param prototype The prototype we are storing data in
 * @return true if the operation succeeded, false if we have no registered name value
 * 
 */
      private boolean recordBasicData( Tag tag, DataElementPrototype prototype ) {
    	  
          String name = TagUtils.getAttribute( tag, "name", properties );
    	  
    	  if( name == null || name.isEmpty() ) {
    		  System.out.println("ERROR: no name provided for "+tag.getTagName()+" element");
    		  return false;
    	  }
    	  
    	  prototype.setName(name.trim());
    	  
    	  prototype.setDescription( TagUtils.getAttribute( tag, "description",properties) );
    	  
    	  return true;
      }

/**
 * 
 * A utility function that registers a tag and it's associated data
 * prototype to it's parent prototype
 * @param parent The parent tag
 * @param prototype The tag's associated prototype
 * @return True if the operation succeeded, false if there was an error
 * 
 */
      private boolean registerToParentObjectOrArray( Tag parent, DataElementPrototype prototype ) {
    	  
    	  String tagName = parent.getTagName();
    	  
    	  if( "Object".equals(tagName) || "Array".equals(tagName) || "Format".equals(tagName) ) {
    		  
    		  String name = TagUtils.getAttribute( parent, "name", properties );

    		  if ( name == null ) {
    			  prototype = null;
    			  System.out.println("ERROR: object could not retrieve parent name");
    			  return false;
    		  }

    		  DataElementPrototype parentProto = dataElements.get(name);
    		  
    		  if( parentProto == null ) {
    			  System.out.println("ERROR: Parent prototype not found for prototype "+prototype.getName());
    			  return false;
    		  }
    		  
    		  switch(parentProto.getDataType()) {
    		  
    		  case OBJECT:
    			  ObjectPrototype protoObj = (ObjectPrototype) parentProto;
        		  protoObj.appendDataPrototype(prototype);
        		  break;
        		  
    		  case ARRAY:
    			  ArrayPrototype protoArr = (ArrayPrototype) parentProto;
    			  protoArr.setDataElementPrototype( prototype );
    			  break;
    			  
    		  default:
    			  System.out.println("ERROR: parent prototype is not Object nor Array type");
    			  return false;
    		  }
    	  } else { 
    		  
    		  return false;
    	  }
    	  
    	  return true;
      }

/*********** This section contains our handler functions for different tags *************/

/**
 * 
 * @param binaryFormats
 * @param parent
 */
      private void handleBinaryFormats( Tag binaryFormats, Tag parent ) {
    	  
      }

 /**
  * 
  * 
  * @param format
  * @param parent
  * 
  */
      private void handleFormat( Tag format, Tag parent ) {
    	  
    	  ObjectPrototype prototype = new ObjectPrototype("h");
    	  
    	  if( !recordBasicData( format, prototype ) ) {
    		  prototype = null;
    		  System.out.println("ERROR: error recording basic data for format");
    		  return;
    	  }
    	  
    	  if( formats.containsKey( prototype.getName() ) ) {
    		  System.out.println("ERROR: duplicate format found: "+prototype.getName() );
    		  return;
    	  }
    	  
    	  currentFormatName = prototype.getName();
    	  
    	  formats.put( currentFormatName, prototype ) ;
    	  
    	  dataElements.put( currentFormatName, prototype ) ;
      }
 
/**
 * 
 * handles the occurrence of a property tag
 * @param property The Property tag
 * @param parent the parent tag
 * 
 */
      private void handleProperty( Tag property, Tag parent ) {
    	  //Nothing to do until we receive string value
      }

/**
 * 
 * 
 * @param object
 * @param parent
 * 
 */
      private void handleObject( Tag object, Tag parent ) {
    	  
          ObjectPrototype prototype = new ObjectPrototype("h");
          
          if( !recordBasicData( object, prototype ) ) {
    		  System.out.println("ERROR: error recording basic data for object");
    		  return;
    	  }
          
          String elementReference = TagUtils.getAttribute(object, "element-reference", properties);
          
          if( elementReference != null ) {
        	  
        	  prototype.setElementReference(elementReference);
          }
          
    	  if( !registerToParentObjectOrArray( parent, prototype) ) {
    		  System.out.println("ERROR: unable to register object under parent prototype");
    		  return;
    	  }
    	  
    	  dataElements.put(prototype.getName(), prototype);
      }

/**
 * 
 * 
 * @param string
 * @param parent
 * 
 */
      private void handleString( Tag string, Tag parent ) {
    	  
          StringPrototype prototype = new StringPrototype("h");
          
          if( !recordBasicData( string, prototype ) ) {
    		  System.out.println("ERROR: error recording basic data for object");
    		  return;
    	  }
          
          // Grab the values of all possible attributes
          Boolean fixed              = TagUtils.getAttributeAsBoolean( string, "fixed", properties),
                  fixedLength        = TagUtils.getAttributeAsBoolean( string, "fixed-length", properties);
          String defaultValue        = TagUtils.getAttribute( string, "value", properties),
                 length_element_name = TagUtils.getAttribute( string, "length-element-name", properties);
          Integer length             = TagUtils.getAttributeAsInteger( string, "length", properties, null);
          
          
          //set attributes values
          prototype.setFixedLength(fixedLength);
          prototype.setLengthElementName(length_element_name);
          
          if( fixedLength ) {
              prototype.setLength(length);
          }
          
          if( fixedLength && length != null ) {
	         prototype.setLength(length);
          }
          
          if( fixedLength && defaultValue != null && defaultValue.length() == length || !fixedLength ) {
        	  prototype.setValue(defaultValue);
          }
          
    	  if( !registerToParentObjectOrArray( parent, prototype) ) {
    		  System.out.println("ERROR: unable to register string under parent prototype");
    		  return;
    	  }  	
    	  
    	  dataElements.put(prototype.getName(), prototype);
      }

/**
 * 
 * @param array
 * @param parent
 * 
 */
      private void handleArray( Tag array, Tag parent ) {
    	  
    	  ArrayPrototype prototype = new ArrayPrototype(null,"array");
    	  
          if( !recordBasicData( array, prototype ) ) {
    		  System.out.println("ERROR: error recording basic data for array");
    		  return;
    	  }
          
          String randomName = RandomStringUtils.random(8);
          
          HashMap<String,DataElementPrototype> validTypes = new HashMap<String,DataElementPrototype>() {{
        		 put("OBJECT", new ObjectPrototype(randomName)); 
        		 put("STRING", new StringPrototype(randomName));
        		 put("ARRAY", new ArrayPrototype(null,randomName)); 
        		 put("CHAR",new CharPrototype(randomName));
        		 put("BYTE",new BytePrototype(randomName));
        		 put("BITS",new BitPrototype(randomName));
        		 put("FLOAT",new FloatPrototype(randomName));
        		 put("INT",new IntegerPrototype(randomName));
        		 put("BOOLEAN",new BooleanPrototype(randomName));
        		 
          }};
          
          
          Boolean fixed_length          = TagUtils.getAttributeAsBoolean( array, "fixed-length", properties );
          String elementType            = TagUtils.getAttributeAsString( array, "element-type", properties, validTypes.keySet() ),
        		 length_element_name    = TagUtils.getAttribute( array, "length-element-name", properties ),
                 elementReference      = TagUtils.getAttribute( array, "element-reference", properties );
          Integer length                = TagUtils.getAttributeAsInteger( array, "length", properties, null );
          
          
          DataElementPrototype p = null;
          
          if( elementReference != null) {
        	  prototype.setDataElementNameReference(elementReference);
          } else if( elementType != null ) {
              p = validTypes.get(elementType);
          } else {
        	  System.out.println("DEBUG: no valid value for data element type nor reference found found in array attributes");
          }
          
          prototype.setFixedLength(fixed_length);
          if( fixed_length ) {
        	  prototype.setLength(length);
          }
          
          prototype.setDataElementPrototype(p);
          prototype.setLengthElementName(length_element_name);
          
          if( !registerToParentObjectOrArray( parent, prototype) ) {
    		  System.out.println("ERROR: unable to register array under parent prototype");
    		  return;
    	  }
          
          dataElements.put(prototype.getName(), prototype);
      }
      

 /**
  * 
  * Handles registering data for all primitives     
  * @param primitive
  * @param parent
  * 
  */
      private void handlePrimitive( Tag primitive, Tag parent ) {
    	  
    	  PrimitivePrototype tmpProto = new IntegerPrototype( null);
    	  PrimitivePrototype finalProto = null;
    	  
    	  if ( !recordBasicData( primitive, tmpProto ) ) {
    		  System.out.println("ERROR: error recording basic data for primitive type");
    		  return;
    	  }
    	  
    	  switch( primitive.getTagName() ) {
    	  
    	  case SchemaDefinition.BITS_TAG:
    		  finalProto = new BitPrototype(tmpProto.getName());
    		  break;
    		  
    	  case SchemaDefinition.BYTE_TAG:
    		  finalProto = new BytePrototype(tmpProto.getName());
    		  break;
    		  
    	  case SchemaDefinition.CHAR_TAG:
    		  finalProto = new CharPrototype(tmpProto.getName());
    		  break;
    		  
    	  case SchemaDefinition.BOOL_TAG:
    		  finalProto = new BooleanPrototype(tmpProto.getName());
    		  break;
    		  
    	  case SchemaDefinition.INT_TAG:
    		  finalProto = new IntegerPrototype(tmpProto.getName());
    		  break;
    		  
    	  case SchemaDefinition.FLOAT_TAG:
    		  finalProto = new FloatPrototype(tmpProto.getName()); 
    		  break;
    		  
    	  default:
    		  System.out.println( "ERROR: Found an unrecognized primitive tag: "+primitive.getTagName() );
    		  return;
    	  }
    	  
          String defaultValue = TagUtils.getAttribute( primitive, "value", properties );
    	  finalProto.setValue(defaultValue);
    	  
    	  Boolean fixed = TagUtils.getAttributeAsBoolean( primitive, "fixed", properties );
    	  finalProto.setFixed(fixed);
    	  
    	  Integer finalLength = TagUtils.getAttributeAsInteger( primitive, "length", properties, null );
    	  
    	  if( finalLength != null ){
    	      finalProto.setLength( finalLength );
    	  }
    	  
    	  finalProto.setDescription( tmpProto.getDescription() );
    	  
          if( !registerToParentObjectOrArray( parent, finalProto) ) {
    		  System.out.println("ERROR: unable to register primitive "+primitive.getTagName()+" under parent prototype");
    		  return;
    	  }
          
          dataElements.put(finalProto.getName(), finalProto);
    	  
      }
      
      public ObjectPrototype getFormat( String name ) {
    	  return formats.get(name);
      }
}
