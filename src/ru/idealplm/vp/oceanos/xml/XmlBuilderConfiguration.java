package ru.idealplm.vp.oceanos.xml;

import java.util.HashMap;

import ru.idealplm.vp.oceanos.core.Report.FormField;

public class XmlBuilderConfiguration
{
	public static HashMap<FormField, Double> columnLengths;
	public static int	MaxLinesOnFirstPage;
	public static int	MaxLinesOnOtherPage;
	public static int	MaxWidthGlobalRemark;

	public XmlBuilderConfiguration()
	{
		MaxLinesOnFirstPage = 1;
		MaxLinesOnOtherPage = 1;
		MaxWidthGlobalRemark = 1;
		initColumnLengths();
	}

	public XmlBuilderConfiguration(int MaxLinesOnFirstPage, int MaxLinesOnOtherPage)
	{
		this();
		this.MaxLinesOnFirstPage = MaxLinesOnFirstPage;
		this.MaxLinesOnOtherPage = MaxLinesOnOtherPage;
	}
	
	public void initColumnLengths()
	{
		columnLengths = new HashMap<FormField, Double>();
		columnLengths.put(FormField.ID, 3d);
		columnLengths.put(FormField.NAME, 400.0d);
		columnLengths.put(FormField.PARENTID, 3d);
		columnLengths.put(FormField.QUANTITY, 3d);
		columnLengths.put(FormField.TOTALQUANTITY, 3d);
		columnLengths.put(FormField.REMARK, 270d);
	}
	
	public void setColumnLengths(HashMap<FormField, Double> columnLengths)
	{
		XmlBuilderConfiguration.columnLengths = columnLengths;
	}
}
