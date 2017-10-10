package ru.idealplm.vp.oceanos.core;

import java.io.File;

import ru.idealplm.vp.oceanos.data.ReportLineList;

public class Report
{
	public enum ReportType {
		PDF, XLS
	};
	
	public static enum FormField {
		NAME, PRODUCTCODE, SHIPPINGDOC, PROVIDER, PARENTID, QUANTITYASSY, QUANTITYKIT, ADJUSTFACTOR, TOTALQUANTITY, REMARK
	};
	
	public ReportType type;
	public ReportConfiguration configuration;
	public ReportLineList linesList;
	public File data;
	public File report;
	public StampData stampData;
	public String targetId;
	
	public Report()
	{
		stampData = new StampData();
		linesList = new ReportLineList();
	}
	
	public void isDataValid()
	{
		if(data == null)
			throw new RuntimeException("Report data is null");

	}
}
