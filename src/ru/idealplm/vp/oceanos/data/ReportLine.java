package ru.idealplm.vp.oceanos.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import ru.idealplm.vp.oceanos.comparator.ReportLineOccurencesComparator;

public class ReportLine
{
	private static ReportLineOccurencesComparator reportLineOccurencessComparator = new ReportLineOccurencesComparator();
	
	public boolean isReportable = false;
	public ReportLineType type;
	public String uid;
	public String fullName;
	public String shortName;
	public String id;
	public String shippingDocument;
	public String provider;
	public String price;
	private int totalQuantity = 0;
	
	public enum ReportLineType {
		NONE, COMMERCIAL, ASSEMBLY, KIT, DOCUMENT
	};
	
	private HashMap<String, ReportLineOccurence> occurences;
	
	public ReportLine(ReportLineType type)
	{
		this.type = type;
		this.occurences = new HashMap<String, ReportLineOccurence>(1);
		if(type == ReportLineType.COMMERCIAL) isReportable = true;
	}
	
	public final ArrayList<ReportLineOccurence> occurences()
	{
		ArrayList<ReportLineOccurence> lines = new ArrayList<ReportLineOccurence>(occurences.values());
		Collections.sort(lines, reportLineOccurencessComparator);
		return lines;
	}
	
	public void addOccurence(ReportLineOccurence occurence)
	{
		occurence.reportLine = this;
		occurences.put(occurence.getParentItemUID(), occurence);
	}
	
	public void updateOccurence(ReportLineOccurence occurence)
	{
		if(occurences.containsKey(occurence.getParentItemUID()))
		{
			ReportLineOccurence existingOccurence = occurences.get(occurence.getParentItemUID());
			existingOccurence.quantityAssy += occurence.quantityAssy;
			existingOccurence.quantityKit += occurence.quantityKit;
		} 
		else {
			addOccurence(occurence);
		}
	}
	
	public int getTotalQuantity()
	{
		calcTotalQuantity(); 
		return totalQuantity;
	}
	
	private void calcTotalQuantity()
	{
		for(ReportLineOccurence occurence : occurences.values())
		{
			totalQuantity += occurence.getTotalQuantityWithReserve();
		}
	}
}
