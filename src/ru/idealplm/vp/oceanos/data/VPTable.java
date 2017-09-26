package ru.idealplm.vp.oceanos.data;

import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.teamcenter.rac.kernel.TCException;

import ru.idealplm.vp.oceanos.core.DataReader;
import ru.idealplm.vp.oceanos.core.VPSettings;
import ru.idealplm.vp.oceanos.util.LineUtil;
import ru.idealplm.vp.oceanos.xml.XmlBuilder;
import ru.idealplm.vp.oceanos.xml.XmlBuilderConfiguration;

import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;


public class VPTable
{
	private long lineNumber = 1;
	private static int rowCount = 0;
	private boolean needQty = true;
	static int linesBeforeSection = 1;
	static Font font = new Font("Arial", Font.PLAIN, 14);
	
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
	
//	private int rowTillEndOfPage(int numLine) {
//		if (numLine <= 24)
//			return (24 - numLine);
//		else
//			return (29 - ((numLine - 24) % 29));
//	}
	
	
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

//  --------------		
//		String secTitle = "";
//		if (EngineVP2G.isSplittedByBlocks)
//			EngineVP2G.getSectionByID(section.getID());

		int startLine = XmlBuilder.line;

//		if (EngineVP2G.isSplittedByBlocks) {
//			addEmptyRowBuff(linesBeforeSection);
//			addTitle(secTitle);
//			addEmptyRowBuff(1);
//		}
		
		if (!section.title.equals("")) {
			addEmptyRowBuff(linesBeforeSection);
			addTitle(section.title);
			addEmptyRowBuff(1);
		}
		
		Iterator itr = section.iterator();
		
		while (itr.hasNext()) {
			VPDataOcc currLine = (VPDataOcc) itr.next();
			parse(currLine);

//			System.out.println ("AfterParse: " + tempID4out
//					+ "\nstartLine -> " + startLine
//					+ "\nnumOfCurrLine -> " + BuildSpec.numOfCurrLine 
//					+ "\n--\nbufferLine.size() -> " + bufferLine.size()
//					+ "\nthis.rowTillEndOfPage(" + startLine + ") -> " + this.rowTillEndOfPage(startLine)
//					);

			if (bufferLine.size() > this.rowTillEndOfPage(startLine)+1) {
				System.out.println("THAT's IT and adding " + this.rowTillEndOfPage(startLine)+1 + " empty lines!!!");
				addEmptyRow(this.rowTillEndOfPage(startLine) + 1);
			}
			gridVP.addAll(bufferLine);
			System.out.println("BufferLine size: " + bufferLine.size());
			bufferLine.clear();
//			if (EngineVP2G.isAddEmptyAfterEach)
//				addEmptyRow(1);
			
			startLine = XmlBuilder.line;

		}

		System.out.println("GRID SIZE: " + gridVP.size());
		
		
//		System.out.println("SPARE LINES FOR " + EngineVP.getSectionByID(section.getID()) 
//				+ " : " + section.spareLines
//				+ " AND Global Remark: " + VPDialog.globalRemark.equals("")
//				);
		
			
	}
		
//	public void fillGridMat(TreeMap<String, TCComponentItemRevision> mapObj,
//		    TreeMap<String, Double> mapQty)
//	{
//		if (gridSP == null) addEmptyRow();
//		addTitle();
//	}

//	private void addEmptyRowBuff()
//	{
//		SpecLine line2add = new SpecLine();
////		gridSP.add(line2add);
//		bufferLine.add(line2add);
//		BuildSpec.numOfCurrLine++;
//
//	}
	private void addEmptyRow(int n)
	{
		for(int i=0; i<n; i++)
		{
			VPLine line = new VPLine();
			gridVP.add(line);
//			bufferLine.add(line);
		}
		XmlBuilder.line += n;
		
	}

	private void addEmptyRowBuff(int n)
	{
		for(int i=0; i<n; i++)
		{
			VPLine line = new VPLine();
//			gridSP.add(line);
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
	
//	private String[] getLine(int position)
//	{
//		return gridSP.get(position).textLine;
//	}

	public boolean isTitle(int nLine)
	{
		return gridVP.get(nLine).isTitle;
	}
//	
//	public boolean isNewBlock(int nLine)
//	{
//		return gridVP.get(nLine).isStartOfBlock;
//	}
	
	public void clear()
	{
		gridVP.clear();
	}
	
	private void parse(VPDataOcc parseLine) throws TCException
	{
		boolean contFlag = false;

		VPLine line2add = new VPLine();
		
		/********************************************	
		*					Str No.
		********************************************/
//		line2add.textLine[0] = String.valueOf(lineNumber);

		/********************************************	
		*					Name
		********************************************/
//		line2add.textLine[1] = parseLine.name;
		
		int posNewLine;
		if ((posNewLine = getEndPositionForFittedLine(parseLine.name, maxWidthName)) < parseLine.name.length()) {	
			contFlag = true;
			line2add.textLine[1] = parseLine.name.substring(0, posNewLine);
			
//			System.out.println("BEFORE SUBSTRING :>" + parseLine.name + "<" + "and newPos is " + posNewLine);
			parseLine.name = parseLine.name.replaceAll("^(\\s*|\\n*)", "");
			parseLine.name = parseLine.name.substring(posNewLine, parseLine.name.length());
			parseLine.name = parseLine.name.replaceAll("^(\\s*|\\n*)", "");
//			System.out.println("REST STRING :>" + parseLine.name + "<");
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

		
//		if (!parseLine.strId.isEmpty()) {
////			line2add.textLine[3] = id2Str(parseLine.id) + parseLine.docType;
//			line2add.textLine[3] = parseLine.strId;
//	
//			parseLine.strId = "";
////			parseLine.id = null;
//		}

		/********************************************	
		*				  Supplier
		********************************************/

//		line2add.textLine[4] = parseLine.name;
		
		/********************************************	
		*				Where used
		********************************************/
		
		if (DataReader.multiWhereUsed) {
//			System.out.println("BEFORE where used: >" +  parseLine.allWhereUsedInOneLine + "<");
			if (parseLine.allWhereUsedInOneLine.indexOf("\n") >= 0) {
				String occWhereUsed = parseLine.allWhereUsedInOneLine.substring(0, parseLine.allWhereUsedInOneLine.indexOf("\n"));
				line2add.textLine[5] = occWhereUsed;
				parseLine.allWhereUsedInOneLine = parseLine.allWhereUsedInOneLine.substring(parseLine.allWhereUsedInOneLine.indexOf("\n")+1);
				contFlag = true;
			} else {
				line2add.textLine[5] = parseLine.allWhereUsedInOneLine;
				parseLine.allWhereUsedInOneLine = "";
			}
			
//			System.out.println("AFTER where used: >" +  parseLine.allWhereUsedInOneLine + "<");
		}
		
		
		/********************************************	
		*			Qty 
		********************************************/

		
		if (parseLine.allQtyInOneLine.indexOf("\n") >= 0) {
			String occWhereUsed = parseLine.allQtyInOneLine.substring(0, parseLine.allQtyInOneLine.indexOf("\n"));
			line2add.textLine[6] = removeTrailingZeros(occWhereUsed);
			parseLine.allQtyInOneLine = parseLine.allQtyInOneLine.substring(parseLine.allQtyInOneLine.indexOf("\n")+1);
			contFlag = true;
		} else {
			line2add.textLine[6] = removeTrailingZeros(parseLine.allQtyInOneLine);
			parseLine.allQtyInOneLine = "";
		}
		
		/********************************************	
		*					Remark
		********************************************/

		if ((posNewLine = getEndPositionForFittedLine(parseLine.remark, maxWidthRemark)) < parseLine.remark.length()) {
				contFlag = true;
			if (((parseLine.remark.indexOf("\n") > 0)&& (parseLine.remark.indexOf("\n") < posNewLine)))
				posNewLine = parseLine.remark.indexOf("\n");
				
			line2add.textLine[7] = parseLine.remark.substring(0, posNewLine);

//			System.out.println("BEFORE SUBSTRING :>" + parseLine.remark	+ "<" + "and newPos is " + posNewLine);
			parseLine.remark = parseLine.remark.replaceAll("^(\\s*|\\n*)", "");
			parseLine.remark = parseLine.remark.substring(posNewLine, parseLine.remark.length());
			parseLine.remark = parseLine.remark.replaceAll("^(\\s*|\\n*)", "");
//			System.out.println("REST STRING :>" + parseLine.remark + "<");
		} 
		else {
			line2add.textLine[7] = parseLine.remark;
			parseLine.remark = "";
		}
		
		/********************************************	
		*					ADDING LINE
		********************************************/
		
		bufferLine.add(line2add);
		XmlBuilder.line++;
		if (contFlag == true) {
//			recurseCounter++;
			parse(parseLine);
		}


//		recurseCounter = 0;
		
		
		
//		bufferLine.add(line2add);
//		
//		
////		EngineVP.numOfCurrLine++;
//		
//		lineNumber++;
//		if (contFlag == true)
//			parse(parseLine);
	}

	static int getEndPositionForFittedLine(String inLine, double maxWidth) {
		int position = 0;
		if (((position = inLine.indexOf("\n")) > 0) && (getWidthOfLine(inLine.substring(0, position)) < maxWidth)) {
			return position;
		}
		if (getWidthOfLine(inLine)	< maxWidth) {
			return inLine.length();
		}
		else {
			String stringForOut = "";
			int i = 0;
			String[] wordsInLine = inLine.split(" ");
			String[] connectedWords = connectNonBreakableWords(wordsInLine);
			
			while (getWidthOfLine(stringForOut) < maxWidth) {
				stringForOut = stringForOut  + " " + connectedWords[i];
				i++;
			}
			stringForOut = "";
			if (i == 1) {
				stringForOut = connectedWords[0];
				while (getWidthOfLine(stringForOut) > maxWidth) {
					stringForOut = stringForOut.substring(0, stringForOut.length()-1);
				}
			}
			else {
				for (int j = 0; j < i-1; j++) {
					if (j > 0)
						stringForOut = stringForOut + " " + connectedWords[j];
					else
						stringForOut = connectedWords[j];
				}
			}
			return stringForOut.length();
		}
	}
	
	static int getWidthOfLine(String measuredLine) {
		Rectangle2D areaName = font.getStringBounds(measuredLine, new FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT));
		return (((Double)areaName.getWidth()).intValue());
	}
	
	static String[] connectNonBreakableWords(String[] inStrArray) {
		ArrayList<String> list4Out = new ArrayList<String>();
		String gotSymbols = "";
		String gotPreviousSymbols = "";

		for (String inStr : inStrArray) {

			gotSymbols = getNonBreakableSymbols(inStr);
			if (gotSymbols.equals("<") || gotPreviousSymbols.equals(">") 
					|| gotSymbols.equals("<>") || gotSymbols.equals("><") 
					|| gotPreviousSymbols.equals("<>") || gotPreviousSymbols.equals("><")) {

//				System.out.println("GOT < OR >!!!!");
				if (!list4Out.isEmpty()) {
					list4Out.set(list4Out.size() - 1,
							list4Out.get(list4Out.size() - 1) + " " + inStr);
				} else {
					list4Out.add(inStr);
				}
				
			} else {
				list4Out.add(inStr);
			}
			gotPreviousSymbols = gotSymbols;
		}
		return list4Out.toArray(new String[list4Out.size()]);
	}
	
	static String getNonBreakableSymbols(String inStr) {
		int index = -1;
		if ((index = (Arrays.asList(LineUtil.nonbreakablePlaneArray)).indexOf(inStr)) >= 0) {
			return LineUtil.nonbreakableWords[index].replaceAll("[^<>]", ""); 
		}
		else return "";
	}
	
	static String breakLine(String inLine, double maxWidth ) {
		boolean isEnd = false;
		String newLine = "";
		
		System.out.println("inside BREAKLINES... with line: >" + inLine + "<");
		int i = 0;
		
		while (!isEnd) {
			if (i++ > 5) {
				System.out.println("has reached LIMIT!!!");
				break;
			}
			System.out.println("WidthOfLine = " + getWidthOfLine(inLine) + " with LIMIT = " + maxWidth);
			if (getWidthOfLine(inLine) < maxWidth) {
				newLine += inLine;
				isEnd = true;
			}
			else {
				newLine += (inLine.substring(0, getEndPositionForFittedLine(inLine, maxWidth)) + "\n");
				System.out.println("is char at " + getEndPositionForFittedLine(inLine, maxWidth) + 1 + " is \\n? " + (inLine.charAt(getEndPositionForFittedLine(inLine, maxWidth)) == '\n') );
				int shiftInt = (inLine.charAt(getEndPositionForFittedLine(inLine, maxWidth) + 1) == '\n')? 2 : 1; 
				inLine = inLine.substring(getEndPositionForFittedLine(inLine, maxWidth) + shiftInt);
				System.out.println("newLine: >" + newLine + "<\nrest line: >" + inLine + "<");
			}
		}
		return newLine.trim();
	}
	
	private static String removeTrailingZeros(String inStr) {
		return inStr.replaceAll(".0+$", "");
	}	
	
	
	
	
	
	
	
	
	
	
	
}
