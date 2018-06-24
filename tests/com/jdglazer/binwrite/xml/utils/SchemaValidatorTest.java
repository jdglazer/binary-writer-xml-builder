package com.jdglazer.binwrite.xml.utils;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import com.jdglazer.binwrite.xml.SchemaDefinition;

import junit.framework.TestCase;

public class SchemaValidatorTest extends TestCase {
	
	private final String TEST_TAG_NAME = "Object";
	
	private final String [] REQUIRED_ATTRIBUTES = new String[]{"name"};
	
	private final String [] OPTIONAL_ATTRIBUTES = new String[]{"id","description"};
	
	private final String [] REQUIRED_CHILDREN = new String[]{};
	
	private final String [] OPTIONAL_CHILDREN = new String[]{"Object","String","Array","Int","Bool","Bits","Float","Char","Byte"};
	
	private SchemaDefinition schema = new SchemaDefinition();
	
    private SchemaDefinitionCompiler validator;

    public SchemaValidatorTest() throws IllegalArgumentException, IllegalAccessException {
    	validator = new SchemaDefinitionCompiler(schema);
    }
    
	@Test
	public void testValidRequiredAttributes() {
		for( String attr : REQUIRED_ATTRIBUTES ) {
			assertTrue( "Required attributes validated", validator.isRequiredAttr(TEST_TAG_NAME, attr));
		}
	}
	
	@Test
	public void testInvalidRequiredAttributes() {
		for( String attr : OPTIONAL_ATTRIBUTES ) {
			assertFalse("Optional attributes not parsed as valid required", validator.isRequiredAttr(TEST_TAG_NAME, attr));
		}
	}
	
	@Test
	public void testValidOptionalAttributes() {
		for( String attr : OPTIONAL_ATTRIBUTES ) {
			assertTrue( "Optional Attributes validated", validator.isOptionalAttr(TEST_TAG_NAME, attr));
		}
	}
	
	@Test
	public void testInvalidOptionalAttributes() {
		for( String attr : REQUIRED_ATTRIBUTES ) {
			assertFalse("Required attributes not parsed as valid optional", validator.isOptionalAttr(TEST_TAG_NAME, attr));
		}
	}
	
	@Test
	public void testValidAllowedAttributes() {
		for( Object attr : ArrayUtils.addAll(OPTIONAL_ATTRIBUTES, REQUIRED_ATTRIBUTES) ) {
			assertTrue( "Allowed Attributes validated", validator.isAllowedAttr(TEST_TAG_NAME, (String) attr));
		}
	}
	
	@Test
	public void testInvalidAllowedAttributes() {
		assertFalse( "Not allowed attributes not parsed as allowed", validator.isAllowedAttr(TEST_TAG_NAME, "altitude") );
	}
	
	@Test
	public void testValidRequiredChildren() {
		for( String attr : REQUIRED_CHILDREN ) {
			assertTrue( "Required children validated", validator.isRequiredChild(TEST_TAG_NAME, attr));
		}
	}
	
	@Test
	public void testInvalidRequiredChildren() {
		for( String attr : OPTIONAL_CHILDREN ) {
			assertFalse("Optional children not parsed as valid required", validator.isRequiredChild(TEST_TAG_NAME, attr));
		}
	}
	
	@Test
	public void testValidOptionalChildren() {
		for( String attr : OPTIONAL_CHILDREN ) {
			assertTrue( "Optional children validated", validator.isOptionalChild(TEST_TAG_NAME, attr));
		}
	}
	
	@Test
	public void testInvalidOptionalChildren() {
		for( String attr : REQUIRED_CHILDREN ) {
			assertFalse("Required children not parsed as valid optional", validator.isOptionalChild(TEST_TAG_NAME, attr));
		}
	}
	
	@Test
	public void testValidAllowedChildren() {
		for( Object attr : ArrayUtils.addAll(OPTIONAL_CHILDREN, REQUIRED_CHILDREN) ) {
			assertTrue( "Allowed children validated", validator.isAllowedChild(TEST_TAG_NAME, (String) attr));
		}
	}
	
	@Test
	public void testInvalidAllowedChildren() {
		assertFalse( "Not allowed children not parsed as allowed", validator.isAllowedAttr(TEST_TAG_NAME, "altitude") );
	}
}
