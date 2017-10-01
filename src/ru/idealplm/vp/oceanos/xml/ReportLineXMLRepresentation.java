package ru.idealplm.vp.oceanos.xml;

import java.util.ArrayList;

import ru.idealplm.vp.oceanos.core.Report.FormField;
import ru.idealplm.vp.oceanos.data.ReportLine;
import ru.idealplm.vp.oceanos.data.ReportLineOccurence;
import ru.idealplm.vp.oceanos.util.LineUtil;

public class ReportLineXMLRepresentation
{
	public ReportLine reportLine;
	public ArrayList<String> nameLines;
	public ArrayList<ReportLineOccurenceXmlRepresentation> occurences;
	private int lineHeight = 1;
	private int totalQuantity = 0;
	
	public ReportLineXMLRepresentation(ReportLine reportLine)
	{
		this.reportLine = reportLine;
		this.occurences = new ArrayList<ReportLineOccurenceXmlRepresentation>();
		for(ReportLineOccurence occurence : reportLine.occurences())
		{
			occurences.add(new ReportLineOccurenceXmlRepresentation(occurence));
		}
		calcLineHeight();
	}
	
	/*public void calcTotalQuantity()
	{
		for(ReportLineOccurenceXmlRepresentation occurence : occurences)
		{
			totalQuantity += occurence.occurence.getTotalQuantity();
		}
	}
	
	public int getTotalQuantity()
	{
		return totalQuantity;
	}*/
	
	public int getLineHeight()
	{
		return lineHeight;
	}
	
	private int calcLineHeight()
	{
		nameLines = new ArrayList<String>(1);
		nameLines = LineUtil.getFittedLines(reportLine.name, XmlBuilderConfiguration.columnLengths.get(FormField.NAME));
		lineHeight = nameLines.size();
		return lineHeight;
	}
}
