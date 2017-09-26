package ru.idealplm.vp.oceanos.core;

import java.io.File;

import ru.idealplm.vp.oceanos.data.VPTable;

public class Report
{
	public enum ReportType {
		PDF, XLS
	};
	
	public static enum FormField {
		ID, NAME, PARENTID, QUANTITY, TOTALQUANTITY, REMARK
	};
	
	public ReportType type;
	public ReportConfiguration configuration;
	public VPTable vpTable;
	public File data;
	public File report;
	public StampData stampData;
	public String targetId;
	
	public Report()
	{
		stampData = new StampData();
		vpTable = new VPTable();
	}
	
	public void isDataValid()
	{
		if(data == null)
			throw new RuntimeException("Report data is null");

	}
}