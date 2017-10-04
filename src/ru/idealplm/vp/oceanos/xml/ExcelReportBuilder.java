package ru.idealplm.vp.oceanos.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

import ru.idealplm.vp.oceanos.core.Report;
import ru.idealplm.vp.oceanos.data.ReportLine;
import ru.idealplm.vp.oceanos.data.ReportLine.ReportLineType;
import ru.idealplm.vp.oceanos.data.ReportLineOccurence;

public class ExcelReportBuilder
{
	private Report report;
	private HSSFWorkbook workbook;
	private HSSFSheet sheet;
	private Row row;
	private Cell cellName;
	private Cell cellCode;
	private Cell cellShippingDocument;
	private Cell cellProvider;
	private Cell cellOccurence;
	private Cell cellQuantityAssy;
	private Cell cellQuantityKit;
	private Cell cellReserveFactor;
	private Cell cellQuantityTotal;
	private Cell cellRemark;
	private Cell cellPrice;
	private CellStyle cellStyleRemarkRemark;
	private CellStyle cellStyleHeader;
	private int rowCount = 0;
	
	public ExcelReportBuilder(Report report)
	{
		this.report = report;
		workbook = new HSSFWorkbook();
		sheet = workbook.createSheet();
		sheet.createFreezePane(0, 1);
		
		sheet.setColumnWidth(0, 10000);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 10000);
		sheet.setColumnWidth(3, 5000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 4000);
		sheet.setColumnWidth(6, 4000);
		sheet.setColumnWidth(7, 4000);
		sheet.setColumnWidth(8, 4000);
		sheet.setColumnWidth(9, 5000);
		sheet.setColumnWidth(10, 5000);
		
		cellStyleHeader = workbook.createCellStyle();
	    Font font = sheet.getWorkbook().createFont();
	    font.setBold(true);
	    font.setFontHeightInPoints((short) 10);
	    cellStyleHeader.setFont(font);
	    
	    cellStyleRemarkRemark = workbook.createCellStyle();
	    cellStyleRemarkRemark.setWrapText(true);
	}
	
	public void buildReport()
	{
		addHeader();
		processData();
		report.report = writeToFile();
	}
	
	private void processData()
	{
		System.out.println("EXCEL: processData");
		ReportLineType previousLineType = ReportLineType.NONE;
		for(ReportLine line : report.linesList.getSortedList())
		{
			System.out.println("XML: processing line..." + line.fullName);
			if(!line.isReportable) continue;
			System.out.println("XML: processing reportable line...");
			if(previousLineType!=ReportLineType.NONE && previousLineType!=line.type) 
				addEmptyLines(1);
			addLine(line);
			previousLineType = line.type;
		}
	}
	
	private void addLine(ReportLine line)
	{
		row = sheet.createRow(rowCount++);
		cellName = row.createCell(0);
		cellName.setCellValue(line.fullName);
		cellName.setCellStyle(cellStyleRemarkRemark);
		cellCode = row.createCell(1);
		cellCode.setCellValue(line.id);
		cellShippingDocument = row.createCell(2);
		cellShippingDocument.setCellValue(line.shippingDocument);
		cellProvider = row.createCell(3);
		cellProvider.setCellValue(line.provider);
		cellPrice = row.createCell(10);
		cellPrice.setCellValue(line.price);
		for(ReportLineOccurence occurence : line.occurences())
		{
			cellOccurence = row.createCell(4);
			cellOccurence.setCellValue(occurence.getParentItemId());
			cellQuantityAssy = row.createCell(5);
			cellQuantityAssy.setCellValue(occurence.quantityAssy);
			cellQuantityKit = row.createCell(6);
			cellQuantityKit.setCellValue(occurence.quantityKit);
			cellReserveFactor = row.createCell(7);
			cellReserveFactor.setCellValue(occurence.reserveFactor);
			cellQuantityTotal = row.createCell(8);
			cellQuantityTotal.setCellValue(occurence.getTotalQuantityWithReserve());
			cellRemark = row.createCell(9);
			cellRemark.setCellValue(occurence.remark);
			cellRemark.setCellStyle(cellStyleRemarkRemark);
			if(line.occurences().size()>1)
				row = sheet.createRow(rowCount++);
		}
		
		if(line.occurences().size()>1)
			addTotalQuantityLine(line);
	}
	
	private void addDocumentLine(ReportLine line)
	{
		row = sheet.createRow(rowCount++);
		cellName = row.createCell(0);
		cellName.setCellValue(line.fullName);
		cellName.setCellStyle(cellStyleRemarkRemark);
	}
	
	private void addHeader()
	{
        row = sheet.createRow(rowCount++); 
        cellName = row.createCell(0);
        cellName.setCellStyle(cellStyleHeader);
        cellName.setCellValue("Наименование");
        cellCode = row.createCell(1);
        cellCode.setCellStyle(cellStyleHeader);
        cellCode.setCellValue("Код продукции");
        cellShippingDocument = row.createCell(2);
        cellShippingDocument.setCellStyle(cellStyleHeader);
        cellShippingDocument.setCellValue("Обозначение документа на поставку");
        cellProvider = row.createCell(3);
        cellProvider.setCellStyle(cellStyleHeader);
        cellProvider.setCellValue("Поставщик");
        cellOccurence = row.createCell(4);
        cellOccurence.setCellStyle(cellStyleHeader);
        cellOccurence.setCellValue("Куда входит");
        cellQuantityAssy = row.createCell(5);
        cellQuantityAssy.setCellStyle(cellStyleHeader);
        cellQuantityAssy.setCellValue("на изделие");
        cellQuantityKit = row.createCell(6);
        cellQuantityKit.setCellStyle(cellStyleHeader);
        cellQuantityKit.setCellValue("в комплекты");
        cellReserveFactor = row.createCell(7);
        cellReserveFactor.setCellStyle(cellStyleHeader);
        cellReserveFactor.setCellValue("на регулир.");
        cellQuantityTotal = row.createCell(8);
        cellQuantityTotal.setCellStyle(cellStyleHeader);
        cellQuantityTotal.setCellValue("Всего");
        cellRemark = row.createCell(9);
        cellRemark.setCellStyle(cellStyleHeader);
        cellRemark.setCellValue("Примечание");
        cellPrice = row.createCell(10);
        cellPrice.setCellStyle(cellStyleHeader);
        cellPrice.setCellValue("Цена");
	}
	
	private void addTotalQuantityLine(ReportLine line)
	{
		cellQuantityTotal = row.createCell(8);
		cellQuantityTotal.setCellValue(line.getTotalQuantity());
	}
	
	private void addEmptyLines(int num)
	{
		for(int i = 0; i < num; i++)
		{
			row = sheet.createRow(rowCount++); 
		}	
	}
	
	private File writeToFile()
	{
		File xlsxFile = null;
		try
		{
			xlsxFile = File.createTempFile(report.stampData.id+"_", ".xls");
			workbook.write(new FileOutputStream(xlsxFile));
			workbook.close();
		} 
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return xlsxFile;
	}
}
