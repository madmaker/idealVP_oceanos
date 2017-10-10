package ru.idealplm.vp.oceanos.xml;

import java.util.ArrayList;

import ru.idealplm.vp.oceanos.core.Report.FormField;
import ru.idealplm.vp.oceanos.data.ReportLineOccurence;
import ru.idealplm.vp.oceanos.util.LineUtil;

public class ReportLineOccurenceXmlRepresentation
{
	public ReportLineOccurence occurence;
	public ArrayList<String> remarkLines;
	private int lineHeight = 1;
	
	public ReportLineOccurenceXmlRepresentation(ReportLineOccurence occurence)
	{
		this.occurence = occurence;
		calcLineHeight();
	}
	
	private int calcLineHeight()
	{
		remarkLines = new ArrayList<String>(1);
		remarkLines = LineUtil.getFittedLines(occurence.remark, XmlBuilderConfiguration.columnLengths.get(FormField.REMARK));
		lineHeight = remarkLines.size()==0 ? 1 : remarkLines.size();
		return lineHeight;
	}
	
	public int getLineHeight()
	{
		return lineHeight;
	}
}
