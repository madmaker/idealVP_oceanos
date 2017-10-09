package ru.idealplm.vp.oceanos.xml;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.RetentionPolicy;
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

import ru.idealplm.vp.oceanos.data.ReportLine;
import ru.idealplm.vp.oceanos.data.ReportLine.ReportLineType;
import ru.idealplm.vp.oceanos.core.VPSettings;
import ru.idealplm.vp.oceanos.core.Report;
import ru.idealplm.vp.oceanos.core.Report.FormField;
import ru.idealplm.vp.oceanos.util.DateUtil;

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
	
	private Report report;

	public XmlBuilder(XmlBuilderConfiguration configuration, Report report)
	{
		this.configuration = configuration;
		this.report = report;
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
			processData();
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
		node.setAttribute("FileName", "Файл ведомости покупных изделий: " + report.stampData.id+".pdf/" + report.stampData.reportRevNo);
		node_root.appendChild(node);
	}
	
	private void processData()
	{
		System.out.println("XML: processData");
		if (node_block == null)
		{
			System.out.println("XML: first Block");
			node_block = document.createElement("Block");
			node_root.appendChild(node_block);
		}
		if(getFreeLinesNum() < 1)
		{
			System.out.println("XML: freeLinesNum < 1 -> newPage()");
			newPage();
		}
		ReportLineType previousLineType = ReportLineType.NONE;
		
		System.out.println("XML: SIZE" + report.linesList.getSortedList().size());
		for(ReportLine line : report.linesList.getSortedList())
		{
			System.out.println("XML: processing line..." + line.fullName);
			if(!line.isReportable) continue;
			System.out.println("XML: processing reportable line...");
			ReportLineXMLRepresentation reportLineXMLRepresentation = new ReportLineXMLRepresentation(line);
			int lineHeight = reportLineXMLRepresentation.getLineHeight();
			System.out.println("XML: processing line with height... " + lineHeight);
			if(getFreeLinesNum() < 1 + lineHeight) newPage();
			ReportLineType currentLineType = line.type;
			
			// Adding empty line before Document lines
			if(previousLineType!=ReportLineType.NONE && previousLineType!=ReportLineType.DOCUMENT && currentLineType==ReportLineType.DOCUMENT)
			{
				addEmptyLines(1);
			}
			previousLineType = currentLineType;
			
			System.out.println("XML: adding line...");
			addLine(reportLineXMLRepresentation);
		}
		
		if(getFreeLinesNum()>0) addEmptyLines(getFreeLinesNum());
		node_root.appendChild(node_block);
	}
	
	public void addLine(ReportLineXMLRepresentation line)
	{
		if(line.reportLine.type==ReportLineType.COMMERCIAL)
		{
			addCommercialLine(line);
		} 
		else if (line.reportLine.type==ReportLineType.DOCUMENT)
		{
			addDocumentLine(line);
		}
	}
	
	public void addCommercialLine(ReportLineXMLRepresentation line)
	{
		ReportLineOccurenceXmlRepresentation currentOccurence;
		int lineHeight = line.getLineHeight();
		int occurencesHeight = 0;
		for(ReportLineOccurenceXmlRepresentation occurence:line.occurences) {
			System.out.println("incrementing with " + occurence.getLineHeight());
			occurencesHeight += occurence.getLineHeight();
		}
		int totalHeight = lineHeight>occurencesHeight?lineHeight:occurencesHeight;
		int currentOccurenceNumber = 0;
		int currentOccurenceRemarkLine = 0;
		
		for(int i = 0; i < totalHeight; i++)
		{
			node_occ = document.createElement("Occurrence");
			System.out.println(line.reportLine.shortName + " " + currentOccurenceNumber + " lh" + lineHeight + " os" + line.occurences.size() + " th" + totalHeight + " oh" + occurencesHeight);
			// If it is the first line, we print first line info and increment currentLine number
			if(i==0)
			{
				// Line number
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				// Name
				node = document.createElement("Col_" + 2);
				node.setAttribute("align", "left");
				node.setTextContent(line.nameLines.get(i));
				node_occ.appendChild(node);
				// Product code
				node = document.createElement("Col_" + 3);
				node.setAttribute("align", "left");
				node.setTextContent(line.reportLine.productCode);
				node_occ.appendChild(node);
				// Shipping document id
				node = document.createElement("Col_" + 4);
				node.setAttribute("align", "left");
				node.setTextContent(line.reportLine.shippingDocument);
				node_occ.appendChild(node);
				// Supplier
				//node = document.createElement("Col_" + 5);
				//node.setAttribute("align", "left");
				//node.setTextContent(line.reportLine.provider);
				//node_occ.appendChild(node);
			} else if (lineHeight>i && lineHeight <= totalHeight) {
				// Line number
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				System.out.println("new line for name");
				// If line name takes multiple lines, we print it
				node = document.createElement("Col_" + 2);
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
					node = document.createElement("Col_" + 6);
					node.setAttribute("align", "left");
					node.setTextContent(currentOccurence.occurence.getParentItemId());
					node_occ.appendChild(node);
					// Quantity Assy
					node = document.createElement("Col_" + 7);
					node.setAttribute("align", "center");
					node.setTextContent(String.valueOf(currentOccurence.occurence.quantityAssy));
					node_occ.appendChild(node);
					// Quantity Kit
					node = document.createElement("Col_" + 8);
					node.setAttribute("align", "center");
					node.setTextContent(String.valueOf(currentOccurence.occurence.quantityKit));
					node_occ.appendChild(node);
					// Reserve Factor
					node = document.createElement("Col_" + 9);
					node.setAttribute("align", "center");
					node.setTextContent(String.valueOf(currentOccurence.occurence.reserveFactor));
					node_occ.appendChild(node);
					// Total quantity
					node = document.createElement("Col_" + 10);
					node.setAttribute("align", "center");
					node.setTextContent(removeTrailingZeros(String.valueOf(currentOccurence.occurence.getTotalQuantityWithReserve())));
					node_occ.appendChild(node);
				}
				// Line number
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				// Remark
				node = document.createElement("Col_" + 11);
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
				node = document.createElement("Col_" + 10);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(line.reportLine.getTotalQuantity()));
				node_occ.appendChild(node);
				node_block.appendChild(node_occ);
			}
			currentLineNum++;
		}
	}
	
	public void addDocumentLine(ReportLineXMLRepresentation line)
	{
		int lineHeight = line.getLineHeight();
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
				node = document.createElement("Col_" + 2);
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
				node = document.createElement("Col_" + 2);
				node.setAttribute("align", "left");
				node.setTextContent(line.nameLines.get(i));
				node_occ.appendChild(node);
			}
			node_block.appendChild(node_occ);
			currentLineNum++;
		}
	}
	
	public void newPage()
	{
		addEmptyLines(getFreeLinesNum());
		node_block = document.createElement("Block");
		node_root.appendChild(node_block);
		currentLineNum = 1;
		currentPageNum += 1;
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
	
	public static int getFreeLinesNum(int currentPageNum, int currentLineNum)
	{
		if(currentPageNum==1) return (XmlBuilderConfiguration.MaxLinesOnFirstPage - currentLineNum + 1);
		return (XmlBuilderConfiguration.MaxLinesOnOtherPage - currentLineNum + 1);
	}
	
	int getCurrentPageMaxLinesNum()
	{
		if(currentPageNum==1) return configuration.MaxLinesOnFirstPage;
		return configuration.MaxLinesOnOtherPage;
	}
	
	private String removeTrailingZeros(String inStr) {
		return inStr.replaceAll(".0+$", "");
	}
}
