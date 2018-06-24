package com.jdglazer.binwrite.xml;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.jdglazer.binwrite.dataaccess.types.complex.ObjectPrototype;
import com.jdglazer.binwrite.dataaccess.utils.XMLBuilder;
import com.jdglazer.binwrite.xml.SchemaDefinition;
import com.jdglazer.binwrite.xml.XMLParser;
import com.jdglazer.binwrite.xml.utils.SchemaDefinitionCompiler;

import junit.framework.TestCase;

public class XMLParserTest extends TestCase {

	@Test
	public void test() throws IllegalArgumentException, IllegalAccessException, SAXException, ParserConfigurationException, IOException, TransformerFactoryConfigurationError, TransformerException {
		SchemaDefinitionCompiler validator = new SchemaDefinitionCompiler( new SchemaDefinition() );
		XMLParser parser = new XMLParser(validator);
		parser.parse("/Users/jglazer/Documents/BinaryFormatDesign.xml");
		
		ObjectPrototype object = parser.getFileModel("MyFile");
		
		if( object != null ) {
			XMLBuilder.formatXML(1, XMLBuilder.buildElement(object.getTagName(), object.getAttributes(), object.getEncasedContent() ) );
		}
	}
    
}
