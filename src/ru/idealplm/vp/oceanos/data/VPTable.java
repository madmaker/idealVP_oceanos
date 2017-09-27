package ru.idealplm.vp.oceanos.data;

import java.util.ArrayList;
import java.util.Iterator;

import com.teamcenter.rac.kernel.TCException;

import ru.idealplm.vp.oceanos.core.DataReader;
import ru.idealplm.vp.oceanos.util.LineUtil;
import ru.idealplm.vp.oceanos.xml.XmlBuilder;
import ru.idealplm.vp.oceanos.xml.XmlBuilderConfiguration;

public class VPTable
{
	private long lineNumber = 1;
	private static int rowCount = 0;
	private boolean needQty = true;
	static int linesBeforeSection = 1;
	
	final private double maxWidthName = 194.0;
	final private double maxWidthRemark = 130.0;
	final private double maxWidthGlobalRemark = 500.0;

	public class VPLine {
		public boolean isTitle = false;
		public boolean isStartOfBlock = false;
		public int section = 1;
		public String[] textLine = new String[11];
	}
	
	private static ArrayList<VPLine> gridVP = new ArrayList<VPLine>();
	private ArrayList<VPLine> bufferLine = new ArrayList<VPLine>();
	
	private int rowTillEndOfPage(int numLine) {
		if (numLine <= XmlBuilderConfiguration.MaxLinesOnFirstPage)
			return (XmlBuilderConfiguration.MaxLinesOnFirstPage - numLine+1);
		else
			return (XmlBuilderConfiguration.MaxLinesOnOtherPage - ((numLine - XmlBuilderConfiguration.MaxLinesOnFirstPage+1) % XmlBuilderConfiguration.MaxLinesOnOtherPage));
	} 
	
	public int getRowCount(){
		return gridVP.size();
	}
	
	public String getValueAt(int row, int col){
		String value = null;
		value = (gridVP.get(row).textLine)[col-1];
		return value;
	}
	
	public void fillGridVp(VPBlock section) throws TCException
	{
		System.out.println("Inside fillGridSpec <<<" + "\nsection size: " + section.size());	

		int startLine = XmlBuilder.line;
		
		if (!section.title.equals("")) {
			addEmptyRowBuff(linesBeforeSection);
			addTitle(section.title);
			addEmptyRowBuff(1);
		}
		
		Iterator itr = section.iterator();
		
		while (itr.hasNext()) {
			VPDataOcc currLine = (VPDataOcc) itr.next();
			parse(currLine);

			if (bufferLine.size() > this.rowTillEndOfPage(startLine)+1) {
				System.out.println("THAT's IT and adding " + this.rowTillEndOfPage(startLine)+1 + " empty lines!!!");
				addEmptyRow(this.rowTillEndOfPage(startLine) + 1);
			}
			gridVP.addAll(bufferLine);
			System.out.println("BufferLine size: " + bufferLine.size());
			bufferLine.clear();
			startLine = XmlBuilder.line;
		}

		System.out.println("GRID SIZE: " + gridVP.size());
	}
		
	private void addEmptyRow(int n)
	{
		for(int i=0; i<n; i++)
		{
			VPLine line = new VPLine();
			gridVP.add(line);
		}
		XmlBuilder.line += n;
		
	}

	private void addEmptyRowBuff(int n)
	{
		for(int i=0; i<n; i++)
		{
			VPLine line = new VPLine();
			bufferLine.add(line);
		}
		XmlBuilder.line += n;
	}

	private void addTitle(String title)
	{
		VPLine line2add = new VPLine();
		line2add.isTitle = true;
		line2add.textLine[1] = title;
		bufferLine.add(line2add);
		XmlBuilder.line++;
		lineNumber++;
	}
	
	public boolean isTitle(int nLine)
	{
		return gridVP.get(nLine).isTitle;
	}
	
	public void clear()
	{
		gridVP.clear();
	}
	
	private void parse(VPDataOcc parseLine) throws TCException
	{
		boolean contFlag = false;
		Double quantityAssy = 0.0;
		Double quantityKit = 0.0;

		VPLine line2add = new VPLine();
		
		/********************************************	
		*					Str No.
		********************************************/
		line2add.textLine[0] = String.valueOf(lineNumber);

		/********************************************	
		*					Name
		********************************************/
		int posNewLine;
		if ((posNewLine = LineUtil.getEndPositionForFittedLine(parseLine.name, maxWidthName)) < parseLine.name.length()) {	
			contFlag = true;
			line2add.textLine[1] = parseLine.name.substring(0, posNewLine);
			
			parseLine.name = parseLine.name.replaceAll("^(\\s*|\\n*)", "");
			parseLine.name = parseLine.name.substring(posNewLine, parseLine.name.length());
			parseLine.name = parseLine.name.replaceAll("^(\\s*|\\n*)", "");
		}

		else 
		{
			line2add.textLine[1] = parseLine.name;
			parseLine.name = "";
		}

		
		
		/********************************************	
		*				Product Code
		********************************************/
		line2add.textLine[2] = parseLine.id;
		parseLine.id = "";

		/********************************************	
		*				id DOC on delivery
		********************************************/

		line2add.textLine[3] = parseLine.idDocForDelivery;
		parseLine.idDocForDelivery = "";

		/********************************************	
		*				  Supplier
		********************************************/
		line2add.textLine[4] = parseLine.name;
		
		/********************************************	
		*				Where used Assy
		********************************************/
		if (DataReader.multiWhereUsed) {
			if (parseLine.allWhereUsedInOneLineAssy.indexOf("\n") >= 0) {
				String occWhereUsed = parseLine.allWhereUsedInOneLineAssy.substring(0, parseLine.allWhereUsedInOneLineAssy.indexOf("\n"));
				line2add.textLine[5] = occWhereUsed;
				parseLine.allWhereUsedInOneLineAssy = parseLine.allWhereUsedInOneLineAssy.substring(parseLine.allWhereUsedInOneLineAssy.indexOf("\n")+1);
				contFlag = true;
			} else {
				line2add.textLine[5] = parseLine.allWhereUsedInOneLineAssy;
				parseLine.allWhereUsedInOneLineAssy = "";
			}
		}
		
		/********************************************	
		*				Where used Kit
		********************************************/
		if (DataReader.multiWhereUsed) {
			if (parseLine.allWhereUsedInOneLineKit.indexOf("\n") >= 0) {
				String occWhereUsed = parseLine.allWhereUsedInOneLineKit.substring(0, parseLine.allWhereUsedInOneLineKit.indexOf("\n"));
				line2add.textLine[5] = occWhereUsed;
				parseLine.allWhereUsedInOneLineKit = parseLine.allWhereUsedInOneLineKit.substring(parseLine.allWhereUsedInOneLineKit.indexOf("\n")+1);
				contFlag = true;
			} else {
				line2add.textLine[5] = parseLine.allWhereUsedInOneLineKit;
				parseLine.allWhereUsedInOneLineKit = "";
			}
		}
		
		
		/********************************************	
		*			Qty Assy + Total
		********************************************/
		if (parseLine.allQtyInOneLineAssy.indexOf("\n") >= 0) {
			String occWhereUsed = parseLine.allQtyInOneLineAssy.substring(0, parseLine.allQtyInOneLineAssy.indexOf("\n"));
			line2add.textLine[6] = removeTrailingZeros(occWhereUsed);
			quantityAssy = Double.parseDouble(line2add.textLine[6]);
			/*// Total quantity
			System.out.println("====" + occWhereUsed);
			Double quantity = Double.parseDouble(occWhereUsed);
			System.out.println("====" + quantity);
			quantity = Math.ceil(quantity + quantity * parseLine.reserveFactor);
			line2add.textLine[9] = removeTrailingZeros(String.valueOf(quantity));
			System.out.println("====" + quantity);*/
			parseLine.allQtyInOneLineAssy = parseLine.allQtyInOneLineAssy.substring(parseLine.allQtyInOneLineAssy.indexOf("\n")+1);
			contFlag = true;
		} else {
			line2add.textLine[6] = removeTrailingZeros(parseLine.allQtyInOneLineAssy);
			quantityAssy = Double.parseDouble(parseLine.allQtyInOneLineAssy);
			/*// Total quantity
			System.out.println("===" + parseLine.allQtyInOneLineAssy);
			Double quantity = Double.parseDouble(parseLine.allQtyInOneLineAssy);
			System.out.println("===" + quantity);
			quantity = Math.ceil(quantity + quantity * parseLine.reserveFactor);
			line2add.textLine[9] = removeTrailingZeros(String.valueOf(quantity));
			System.out.println("===" + quantity);*/
			parseLine.allQtyInOneLineAssy = "";
		}
		
		/********************************************
		*			Qty Kit + Total
		********************************************/
		if (parseLine.allQtyInOneLineKit.indexOf("\n") >= 0) {
			String occWhereUsed = parseLine.allQtyInOneLineKit.substring(0, parseLine.allQtyInOneLineKit.indexOf("\n"));
			line2add.textLine[7] = removeTrailingZeros(occWhereUsed);
			quantityKit = Double.parseDouble(line2add.textLine[7]);
			/*// Total quantity
			Double quantity = Double.parseDouble(occWhereUsed);
			quantity = Math.ceil(quantity + quantity * parseLine.reserveFactor);
			line2add.textLine[9] = removeTrailingZeros(String.valueOf(quantity));*/
			parseLine.allQtyInOneLineKit = parseLine.allQtyInOneLineKit.substring(parseLine.allQtyInOneLineKit.indexOf("\n")+1);
			contFlag = true;
		} else {
			line2add.textLine[7] = removeTrailingZeros(parseLine.allQtyInOneLineKit);
			quantityKit = Double.parseDouble(parseLine.allQtyInOneLineKit);
			/*// Total quantity
			System.out.println("===" + parseLine.allQtyInOneLineKit);
			Double quantity = Double.parseDouble(parseLine.allQtyInOneLineKit);
			System.out.println("===" + quantity);
			quantity = Math.ceil(quantity + quantity * parseLine.reserveFactor);
			line2add.textLine[9] = removeTrailingZeros(String.valueOf(quantity));
			System.out.println("===" + quantity);*/
			parseLine.allQtyInOneLineKit = "";
		}
		
		/********************************************	
		*			Reserve Factor
		********************************************/
		line2add.textLine[8] = removeTrailingZeros(String.valueOf(parseLine.reserveFactor));
		
		/********************************************	
		*			Qty Total
		********************************************/
		Double result = Math.ceil(quantityAssy + quantityKit + quantityAssy * parseLine.reserveFactor);
		line2add.textLine[9] = removeTrailingZeros(String.valueOf(result));
		
		/********************************************	
		*					Remark
		********************************************/
		if ((posNewLine = LineUtil.getEndPositionForFittedLine(parseLine.remark, maxWidthRemark)) < parseLine.remark.length()) {
				contFlag = true;
			if (((parseLine.remark.indexOf("\n") > 0)&& (parseLine.remark.indexOf("\n") < posNewLine)))
				posNewLine = parseLine.remark.indexOf("\n");
				
			line2add.textLine[10] = parseLine.remark.substring(0, posNewLine);

			parseLine.remark = parseLine.remark.replaceAll("^(\\s*|\\n*)", "");
			parseLine.remark = parseLine.remark.substring(posNewLine, parseLine.remark.length());
			parseLine.remark = parseLine.remark.replaceAll("^(\\s*|\\n*)", "");
		} 
		else {
			line2add.textLine[10] = parseLine.remark;
			parseLine.remark = "";
		}
		
		/********************************************	
		*					ADDING LINE
		********************************************/
		bufferLine.add(line2add);
		XmlBuilder.line++;
		if (contFlag == true) {
			parse(parseLine);
		}
	}
	
	private static String removeTrailingZeros(String inStr) {
		return inStr.replaceAll(".0+$", "");
	}
}
