package com.jdglazer.binwrite.xml;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.jdglazer.binwrite.dataaccess.types.complex.ObjectPrototype;
import com.jdglazer.binwrite.xml.utils.SchemaDefinitionCompiler;
import com.jdglazer.binwrite.xml.utils.Tag;
import com.jdglazer.binwrite.xml.utils.TagModel;

public class XMLParser extends DefaultHandler implements ErrorHandler {
	
	private FormatModelBuilder formatBuilder;
	private SchemaDefinitionCompiler validator;
	private XMLReader reader;
	
	public XMLParser(SchemaDefinitionCompiler validator) throws SAXException, ParserConfigurationException {
		
		this.validator = validator;
		
		reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		
		reader.setContentHandler(this);
	}
    
	public boolean parse( String filepath ) {
		try {
			if( validator == null ) {
				System.out.println("ERROR: Non-null validator required to parse xml");
				return false;
			}
			reader.parse(filepath);
		} catch (IOException | SAXException e) {
		    //log error
			return false;
		}
		
		return formatBuilder.getComplete();
	}
	
	@Override
	public void startDocument() {
		formatBuilder = new FormatModelBuilder();
		formatBuilder.startDocument();
	}
	
	@Override
	public void endDocument() {
		formatBuilder.endDocument();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		
		TagModel model = validator.getTagModels().get(qName);
		
		if( model == null ) {
			System.out.println("DEBUG: No allowed tag found for start tag qualified name: "+qName);
			return;
		}
		
		// We need to copy attributes to persist them when building tag map
		Attributes attr = new AttributesImpl(attributes);
		Tag tag = new Tag(model,attr,qName);
		
		formatBuilder.startTag(tag);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) {
		
		if( validator.getTagModels().get(qName) == null ) {
			System.out.println("DEBUG: No allowed tag found for end tag qualified name: "+qName);
			return;
		}
		
		formatBuilder.endTag();
	}
	
	@Override
	public void characters(char[] ch, int start, int length) {
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(ch, start, length);
		
		formatBuilder.characters( builder.toString() );
	}
	
	public void setValidator(SchemaDefinitionCompiler validator) {
		this.validator = validator;
	}
	
	public SchemaDefinitionCompiler getValidator() {
		return this.validator;
	}
	
	public ObjectPrototype getFileModel( String name ) {
		return formatBuilder.getFormat(name);
	}
}
