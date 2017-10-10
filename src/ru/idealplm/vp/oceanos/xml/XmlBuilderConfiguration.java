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
		columnLengths.put(FormField.NAME, 190.0d);
		columnLengths.put(FormField.PRODUCTCODE, 140.0d);
		columnLengths.put(FormField.SHIPPINGDOC, 200.0d);
		columnLengths.put(FormField.PROVIDER, 150.0d);
		columnLengths.put(FormField.PARENTID, 200.0d);
		columnLengths.put(FormField.QUANTITYASSY, 50d);
		columnLengths.put(FormField.QUANTITYKIT, 50d);
		columnLengths.put(FormField.ADJUSTFACTOR, 50d);
		columnLengths.put(FormField.TOTALQUANTITY, 50d);
		columnLengths.put(FormField.REMARK, 70.0d);
	}
	
	public void setColumnLengths(HashMap<FormField, Double> columnLengths)
	{
		XmlBuilderConfiguration.columnLengths = columnLengths;
	}
}
