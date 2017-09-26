package ru.idealplm.vp.oceanos.xml;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.teamcenter.rac.util.MessageBox;

import ru.idealplm.vp.oceanos.core.Report;
import ru.idealplm.vp.oceanos.core.Report.FormField;
import ru.idealplm.vp.oceanos.core.VPSettings;
import ru.idealplm.vp.oceanos.util.DateUtil;
import ru.idealplm.vp.oceanos.data.VPTable;

public class XmlBuilder
{
	private XmlBuilderConfiguration configuration;
	
	private DocumentBuilderFactory documentBuilderFactory;
	private DocumentBuilder builder;
	private Document document;
	private Element node_root;
	private Element node;
	private Element node_block = null;
	private Element node_occ;
	
	private int currentLineNum = 1;
	private int currentPageNum = 1;
	private int page = 0;
	public static int line = 0;
	private Map<String, String> form_block;
	
	private Report report;
	private ReportLineXMLRepresentation reportLineXMLRepresentation;
	private VPTable vpTable;

	public XmlBuilder(XmlBuilderConfiguration configuration, Report report)
	{
		this.configuration = configuration;
		this.report = report;
		this.vpTable = report.vpTable;
		form_block = new HashMap<String, String>();
	}

	public void setConfiguration(XmlBuilderConfiguration configuration)
	{
		this.configuration = configuration;
	}
	
	public File buildXml()
	{
		try{			
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			builder = documentBuilderFactory.newDocumentBuilder();
			document = builder.newDocument();
			node_root = document.createElement("root");
			document.appendChild(node_root);
			
			node = document.createElement("Settings");
			node.setAttribute("ShowAdditionalForm", VPSettings.doShowAdditionalForm==true?"true":"false");
			node_root.appendChild(node);
			
			addStampData();
			addExtraData();
			processDataLegacy();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(document);
			File xmlFile = File.createTempFile(report.stampData.id+"_", ".xml");
			StreamResult result = new StreamResult(xmlFile);
			transformer.transform(source, result);
			return xmlFile;
		} catch (TransformerConfigurationException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void addStampData()
	{
		node = document.createElement("Izdelie_osnovnai_nadpis");
		node.setAttribute("NAIMEN", report.stampData.name);
		node.setAttribute("OBOZNACH", report.stampData.id);
		node.setAttribute("PERVPRIM", report.stampData.pervPrim);
		node.setAttribute("LITERA1", report.stampData.litera1);
		node.setAttribute("LITERA2", report.stampData.litera2);
		node.setAttribute("LITERA3", report.stampData.litera3);
		node.setAttribute("INVNO", report.stampData.invNo);
		
		node.setAttribute("RAZR", report.stampData.design);
		node.setAttribute("PROV", report.stampData.check);
		node.setAttribute("ADDCHECKER", report.stampData.techCheck);
		node.setAttribute("NORM", report.stampData.normCheck);
		node.setAttribute("UTV", report.stampData.approve);
		node.setAttribute("CRTDATE", report.stampData.designDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.designDate));
		node.setAttribute("CHKDATE", report.stampData.checkDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.checkDate));
		node.setAttribute("TCHKDATE", report.stampData.techCheckDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.techCheckDate));
		node.setAttribute("CTRLDATE", report.stampData.normCheckDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.normCheck));
		node.setAttribute("APRDATE", report.stampData.approveDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.approveDate));
		
		node_root.appendChild(node);
	}
	
	private void addExtraData()
	{
		node = document.createElement("FileData");
		node.setAttribute("FileName", "Файл ведомости спецификаций: " + report.stampData.id+".pdf/" + report.stampData.reportRevNo);
		node_root.appendChild(node);
	}
	
	public void processDataLegacy()
	{
		try {
			if (vpTable.getRowCount() == 0) {
				System.out.println("SPTABLE IS EMPTY");
				throw new Exception("SPTABLE IS EMPTY");
			}

			DecimalFormat fmt = new DecimalFormat();
			fmt.setMinimumIntegerDigits(2);

			//document.appendChild(node_root);

			Element node;

			String val_cell;
			Element node_block = null;

			for (int i = 0; i < vpTable.getRowCount(); i++) {
				Element node_occ = document.createElement("Occurrence");
				if (vpTable.isTitle(i))
					node_occ.setAttribute("font", "underline,bold");

				
				for (int j = 1; j <= 8; j++) {
					val_cell = (String) vpTable.getValueAt(i, j);
					if ((val_cell == null) || (val_cell.length() <= 0))
						continue;
					node = document.createElement("Col_" + j);
					if (vpTable.isTitle(i))
						node.setAttribute("align", "center");
//					if (vpTable.isTitle(i)) {
//						node.setAttribute("align", "center");
//					}
//					if (j == 5 || j == 7 || j == 8) {
//						node.setAttribute("align", "center");
//					}
//					if (j == 2 || j == 3) {
//						node.setAttribute("align", "left");
//					}
//					if ((!val_cell.equals("")) && ((j == 6)))
//						node.setAttribute("align", "right");
//					if ((!val_cell.equals("")) && (j == 7))
//						node.setAttribute("align", "left");
					node.setTextContent(val_cell);
					node_occ.appendChild(node);
				}

				if ((node_block == null) || (((i + 1) % (configuration.MaxLinesOnFirstPage + 1) == 0) && (page == 1))
						|| (((i + 1) - (configuration.MaxLinesOnFirstPage + 1)) % (configuration.MaxLinesOnOtherPage) == 0)) {
					System.out.println("new block creating");
					node_block = document.createElement("Block");
//					if (vpTable.isTitle(i))
//						node_block.setAttribute("end_page", "false");
					page++;
				}
				node_block.appendChild(node_occ);
				node_root.appendChild(node_block);
			}

			node = document.createElement("Izdelie_osnovnai_nadpis");
			
			//form_block = getFormerBlockAttr(topIR);
			Set<String> keys = form_block.keySet();
			for (String idx_form_block : keys)
				if (form_block.get(idx_form_block) != null)
					node.setAttribute(idx_form_block, form_block.get(idx_form_block));
			
			node_root.appendChild(node);
			
			vpTable.clear();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			MessageBox.post(e);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/*private void processData()
	{
		if (node_block == null)
		{
			node_block = document.createElement("Block");
			node_root.appendChild(node_block);
		}
		if(getFreeLinesNum() < 1)
		{
			newPage();
		}
		ReportLineType previousLineType = ReportLineType.NONE;
		
		for(ReportLine line : report.linesList.getSortedList())
		{
			System.out.println("-line!");
			ReportLineXMLRepresentation reportLineXMLRepresentation = new ReportLineXMLRepresentation(line);
			int lineHeight = reportLineXMLRepresentation.getLineHeight();
			if(getFreeLinesNum() < 1 + lineHeight) newPage();
			ReportLineType currentLineType = line.type;
			
			// Adding empty line before Document lines
			if(previousLineType!=ReportLineType.NONE && previousLineType!=ReportLineType.DOCUMENT && currentLineType==ReportLineType.DOCUMENT)
			{
				addEmptyLines(1);
			}
			previousLineType = currentLineType;
			
			if(currentLineType==ReportLineType.ASSEMBLY || currentLineType==ReportLineType.KIT)
			{
				addAssyOrKitLine2(reportLineXMLRepresentation);
			} else 
			{
				addDocumentLine(reportLineXMLRepresentation);
			}
		}
		
		node_root.appendChild(node_block);
		
		if(getFreeLinesNum()>0) addEmptyLines(getFreeLinesNum());
	}*/
	
	/*public void addAssyOrKitLine2(ReportLineXMLRepresentation line)
	{
		ReportLineOccurenceXmlRepresentation currentOccurence;
		int lineHeight = line.getLineHeight();
		int totalQuantity = 0;
		int occurencesHeight = 0;
		for(ReportLineOccurenceXmlRepresentation occurence:line.occurences) {
			System.out.println("incrementing with " + occurence.getLineHeight());
			occurencesHeight += occurence.getLineHeight();
			totalQuantity += occurence.occurence.totalQuantity;
		}
		int totalHeight = lineHeight>occurencesHeight?lineHeight:occurencesHeight;
		int currentOccurenceNumber = 0;
		int currentOccurenceRemarkLine = 0;
		
		for(int i = 0; i < totalHeight; i++)
		{
			node_occ = document.createElement("Occurrence");
			System.out.println(line.reportLine.name + " " + currentOccurenceNumber + " lh" + lineHeight + " os" + line.occurences.size() + " th" + totalHeight + " oh" + occurencesHeight);
			// If it is the first line, we print first line info and increment currentLine number
			if(i==0)
			{
				// Line number
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				// Id of the line
				node = document.createElement("Col_" + 2);
				node.setAttribute("align", "left");
				node.setTextContent(line.reportLine.id);
				node_occ.appendChild(node);
				// Name
				node = document.createElement("Col_" + 3);
				node.setAttribute("align", "left");
				node.setTextContent(line.nameLines.get(i));
				node_occ.appendChild(node);
			} else if (lineHeight>i && lineHeight <= totalHeight) {
				// Line number
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				System.out.println("new line for name");
				// If line name takes multiple lines, we print it
				node = document.createElement("Col_" + 3);
				node.setAttribute("align", "left");
				node.setTextContent(line.nameLines.get(i));
				node_occ.appendChild(node);
			}
			// If there are still occurences left
			if(currentOccurenceNumber < line.occurences.size()){
				currentOccurence = line.occurences.get(currentOccurenceNumber);
				//Occurence first line info
				if(currentOccurenceRemarkLine==0)
				{
					// Line number
					node = document.createElement("Col_" + 1);
					node.setAttribute("align", "center");
					node.setTextContent(String.valueOf(currentLineNum));
					node_occ.appendChild(node);
					//Parent id
					node = document.createElement("Col_" + 4);
					node.setAttribute("align", "left");
					node.setTextContent(currentOccurence.occurence.getParentItemId());
					node_occ.appendChild(node);
					// Quantity
					node = document.createElement("Col_" + 5);
					node.setAttribute("align", "center");
					node.setTextContent(String.valueOf(currentOccurence.occurence.quantity));
					node_occ.appendChild(node);
					// Total quantity
					node = document.createElement("Col_" + 6);
					node.setAttribute("align", "center");
					node.setTextContent(String.valueOf(currentOccurence.occurence.totalQuantity));
					node_occ.appendChild(node);
				}
				// Line number
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				// Remark
				node = document.createElement("Col_" + 7);
				node.setAttribute("align", "left");
				node.setTextContent(currentOccurence.remarkLines.size()>0?currentOccurence.remarkLines.get(currentOccurenceRemarkLine):"");
				node_occ.appendChild(node);
				
				currentOccurenceRemarkLine++;
				if(currentOccurenceRemarkLine >= currentOccurence.getLineHeight())
				{
					currentOccurenceRemarkLine = 0;
					currentOccurenceNumber++;
				}
			}
			
			node_block.appendChild(node_occ);
			
			//If it is the last line
			if(i==(totalHeight-1) && line.occurences.size()>1)
			{
				currentLineNum++;
				// Line number
				node_occ = document.createElement("Occurrence");
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				// Total quantity
				node = document.createElement("Col_" + 6);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(totalQuantity));
				node_occ.appendChild(node);
				node_block.appendChild(node_occ);
			}
			currentLineNum++;
		}
	}*/
	
	/*public void addDocumentLine(ReportLineXMLRepresentation line)
	{
		int lineHeight = line.getLineHeight();
		//node_occ = document.createElement("Occurrence");
		for(int i = 0; i < lineHeight; i++)
		{
			node_occ = document.createElement("Occurrence");
			// If it is the first line, we print first line info and increment currentLine number
			if(i==0)
			{
				// Line number
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				// Name
				node = document.createElement("Col_" + 3);
				node.setAttribute("align", "left");
				node.setTextContent(line.nameLines.get(i));
				node_occ.appendChild(node);
			} else {
				// Line number
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				// If line name takes multiple lines, we print it
				node = document.createElement("Col_" + 3);
				node.setAttribute("align", "left");
				node.setTextContent(line.nameLines.get(i));
				node_occ.appendChild(node);
			}
			node_block.appendChild(node_occ);
			currentLineNum++;
		}
	}*/
	
	public void newPage()
	{
		addEmptyLines(getFreeLinesNum());
		node_block = document.createElement("Block");
		node_root.appendChild(node_block);
		currentLineNum = 1;
		currentPageNum += 1;
		addEmptyLines(1);
	}
	
	/*public void addBasicLine(BasicXmlLine line)
	{
		// Line number
		node = document.createElement("Col_" + 1);
		node.setAttribute("align", "center");
		node.setTextContent(String.valueOf(currentLineNum));
		node_occ.appendChild(node);
		// Id of the line
		node = document.createElement("Col_" + 2);
		node.setAttribute("align", "left");
		node.setTextContent(line.getAttribute(FormField.ID));
		node_occ.appendChild(node);
		// Name
		node = document.createElement("Col_" + 3);
		node.setAttribute("align", "left");
		node.setTextContent(line.getAttribute(FormField.NAME));
		node_occ.appendChild(node);
		// Parent Id
		node = document.createElement("Col_" + 4);
		node.setAttribute("align", "center");
		node.setTextContent(line.getAttribute(FormField.PARENTID));
		node_occ.appendChild(node);
		// Quantity
		node = document.createElement("Col_" + 5);
		node.setAttribute("align", "left");
		node.setTextContent(line.getAttribute(FormField.QUANTITY));
		node_occ.appendChild(node);
		// TotalQuantity
		node = document.createElement("Col_" + 6);
		node.setAttribute("align", "left");
		node.setTextContent(line.getAttribute(FormField.TOTALQUANTITY));
		node_occ.appendChild(node);
		// Remark
		node = document.createElement("Col_" + 7);
		node.setAttribute("align", "left");
		node.setTextContent(line.getAttribute(FormField.REMARK));
		node_occ.appendChild(node);
		currentLineNum++;
	}*/
	
	public void addEmptyLines(int num)
	{
		for(int i = 0; i < num; i++){
			if(getFreeLinesNum() <= 0){
				newPage();
			}
			node_occ = document.createElement("Occurrence");
			// Line number
			node = document.createElement("Col_" + 1);
			node.setAttribute("align", "center");
			node.setTextContent(String.valueOf(currentLineNum));
			node_occ.appendChild(node);
			node_block.appendChild(node_occ);
			currentLineNum++;
		}
	}
	
	int getFreeLinesNum()
	{
		if(currentPageNum==1) return (configuration.MaxLinesOnFirstPage - currentLineNum + 1);
		return (configuration.MaxLinesOnOtherPage - currentLineNum + 1);
	}
}
