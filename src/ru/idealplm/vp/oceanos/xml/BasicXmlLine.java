package ru.idealplm.vp.oceanos.xml;

import java.util.HashMap;

import ru.idealplm.vp.oceanos.core.Report.FormField;

public class BasicXmlLine
{
	public static final BasicXmlLine INSTANCE = new BasicXmlLine();
	private HashMap<FormField, String> data;
	
	private BasicXmlLine()
	{
		data = new HashMap<FormField, String>();
	}
	
	public void setAttribute(FormField attribute, String value)
	{
		data.put(attribute, value);
	}
	
	public String getAttribute(FormField attribute)
	{
		return data.get(attribute);
	}
}
