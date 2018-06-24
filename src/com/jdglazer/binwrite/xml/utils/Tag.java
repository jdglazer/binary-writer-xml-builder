package com.jdglazer.binwrite.xml.utils;

import org.xml.sax.Attributes;

public class Tag {
	private TagModel   model;
	private Attributes attributes;
	private String     value;
	private boolean    valid;
	
	public Tag(TagModel model, Attributes attributes, String value) {
		this.model      = model;
		this.attributes = attributes;
		this.value      = value;
		this.valid      = true;
	}
	
	public String getTagName() {
		return model.getName();
	}
	
	public TagModel getTagModel() {
		return model;
	}
	
	public Attributes getAttributes() {
		return attributes;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValid( boolean valid ) {
		this.valid = valid;
	}
	
	public boolean isValid() {
		return valid;
	}
}
