package ru.idealplm.vp.oceanos.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelResponse;

import ru.idealplm.vp.oceanos.data.ReportLine;
import ru.idealplm.vp.oceanos.data.ReportLine.ReportLineType;
import ru.idealplm.vp.oceanos.data.ReportLineList;
import ru.idealplm.vp.oceanos.data.ReportLineOccurence;
import ru.idealplm.vp.oceanos.handlers.VPHandler;
import ru.idealplm.vp.oceanos.util.DateUtil;

public class DataReader
{
	public static final String DOC_TYPE = "Oc9_KDRevision";
	public static final String COMPLEX_TYPE = "Complex";
	public static final String ASSEMBLY_TYPE = "Сборочная единица"; // "Assembly"
	public static final String DETAIL_TYPE = "Деталь"; // "Part" ?
	public static final String MATERIAL_TYPE = "Oc9_Material";
	public static final String KIT_TYPE = "Комплект"; // "Set"
	public static final String GEOM_TYPE = "GeomOfMaterial";
	
	public static final String[] companyPartTypes = {ASSEMBLY_TYPE, KIT_TYPE};
	
	private VP vp;
	private StampData stampData;
	private ReportLineList lineList;
	private StructureManagementService smsService = StructureManagementService.getService(VPHandler.session);
	private ProgressMonitorDialog pd;
	private String blPropertyNames[] = {"bl_item_object_type", "bl_Part_oc9_TypeOfPart", "bl_quantity", "bl_item_item_id", "Oc9_Note", "bl_item_object_name", "Oc9_AdjustFactor"};
	private String blPropertyValues[];
	private ReportLine emptyLine;
	private ReportLineOccurence emptyOccurence;
	private ReportLineOccurence topOccurence;
	
	static HashMap<String, String> mapUom = new HashMap<String, String>();
	static {
		mapUom.put("kg", "кг");
		mapUom.put("l", "л");
		mapUom.put("m", "м");
		mapUom.put("m2", "м^2");
		mapUom.put("кг", "кг");
		mapUom.put("л", "л");
		mapUom.put("м", "м");
		mapUom.put("м^2", "м^2");
	}
	
	public DataReader(VP vp)
	{
		this.vp = vp;
		this.stampData = vp.report.stampData;
		this.lineList = vp.report.linesList;
		this.pd = vp.progressMonitor;
		emptyLine = new ReportLine(ReportLineType.NONE);
		emptyOccurence = new ReportLineOccurence(emptyLine, null);
		emptyLine.addOccurence(emptyOccurence);
		topOccurence = readBomLineData(VP.topBOMLine, emptyOccurence);
	}
	
	public void readExistingData()
	{
		findExistingVP();
		readGeneralNoteForm();
		readExistingVPData();
		readSpecifiedItemData();
	}
	
	public void findExistingVP()
	{
		try{
			System.out.println("Looking for vsp!");
			TCComponent[] documents = VP.topBOMLineIR.getRelatedComponents("Oc9_DocRel");
			String IRid = VP.topBOMLineI.getProperty("item_id");
			String id;
			for(TCComponent document : documents){
				id = document.getProperty("item_id");
				System.out.println("Comparing " + id + " and " + IRid);
				if(id.equals(IRid + " ВП")){
					System.out.println("Found one");
					VP.vpIR = ((TCComponentItem)document).getLatestItemRevision();
					VP.generalNoteForm = VP.vpIR.getRelatedComponent("Oc9_SignRel");
					return;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void readExistingVPData()
	{
		try {
			if(VP.vpIR != null){
				stampData.litera1 = VP.vpIR.getProperty("oc9_Litera1");
				stampData.litera2 = VP.vpIR.getProperty("oc9_Litera2");
				stampData.litera3 = VP.vpIR.getProperty("oc9_Litera3");
				stampData.pervPrim = VP.vpIR.getItem().getProperty("oc9_PrimaryApp");
				stampData.invNo = VP.vpIR.getItem().getProperty("oc9_InvNo");
				stampData.reportRevNo = VP.vpIR.getProperty("item_revision_id");
			}
		} catch (TCException ex) {
			ex.printStackTrace();
		}
	}
	
	public void readGeneralNoteForm()
	{
		try {
			if(VP.generalNoteForm != null)
			{
				stampData.design =  VP.generalNoteForm.getProperty("oc9_Designer");
				stampData.check = VP.generalNoteForm.getProperty("oc9_Check");
				stampData.techCheck = VP.generalNoteForm.getProperty("oc9_TCheck");
				stampData.normCheck = VP.generalNoteForm.getProperty("oc9_NCheck");
				stampData.approve = VP.generalNoteForm.getProperty("oc9_Approver");
				stampData.designDate = VP.generalNoteForm.getProperty("oc9_DesignDate").equals("")?"":DateUtil.parseDateFromTC(VP.generalNoteForm.getProperty("oc9_DesignDate"));
				stampData.checkDate = VP.generalNoteForm.getProperty("oc9_CheckDate").equals("")?"":DateUtil.parseDateFromTC(VP.generalNoteForm.getProperty("oc9_CheckDate"));
				stampData.techCheckDate = VP.generalNoteForm.getProperty("oc9_TCheckDate").equals("")?"":DateUtil.parseDateFromTC(VP.generalNoteForm.getProperty("oc9_TCheckDate"));
				stampData.normCheckDate = VP.generalNoteForm.getProperty("oc9_NCheckDate").equals("")?"":DateUtil.parseDateFromTC(VP.generalNoteForm.getProperty("oc9_NCheckDate"));
				stampData.approveDate = VP.generalNoteForm.getProperty("oc9_ApproveDate").equals("")?"":DateUtil.parseDateFromTC(VP.generalNoteForm.getProperty("oc9_ApproveDate"));
			}
		} catch (TCException ex) {
			ex.printStackTrace();
		}
	}
	
	private void readSpecifiedItemData(){
		try {
			stampData.id = VP.topBOMLineI.getProperty("item_id") + " ВП";
			stampData.name = VP.topBOMLineIR.getProperty("object_name");
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void readData()
	{
		try
		{
			pd.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					monitor.beginTask("Чтение данных", 100);
					readBomData(VP.topBOMLine, topOccurence, emptyOccurence, monitor);
					monitor.done();
				}
			});
		}
		catch (InvocationTargetException | InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (CancellationException ex)
		{
			VPSettings.isCancelled = true;
			System.out.println(ex.getMessage());
		}
	}

	private void readBomData(TCComponentBOMLine parentBomLine, ReportLineOccurence currentOccurence, ReportLineOccurence parentOccurence, IProgressMonitor monitor)
	{
		ReportLineOccurence tempOccurence;
		if(currentOccurence==null) return;
		if(currentOccurence.reportLine.type==ReportLineType.DOCUMENT) return;

		for (TCComponentBOMLine bomLine : getChildBOMLines(parentBomLine))
		{
			tempOccurence = readBomLineData(bomLine, currentOccurence);
			if(tempOccurence!=null)
				currentOccurence.addChild(tempOccurence);
			checkIfMonitorIsCancelled(monitor);
		}
		for(ReportLineOccurence child : currentOccurence.getChildren())
		{
			readBomData(child.bomLine, child, currentOccurence, monitor);
			checkIfMonitorIsCancelled(monitor);
		}
	}
	
	public ReportLineOccurence readBomLineData(TCComponentBOMLine bomLine, ReportLineOccurence parentOccurence)
	{
		ReportLineOccurence resultOccurence = null;
		try
		{
			blPropertyValues = bomLine.getProperties(blPropertyNames);
			boolean hasValidType = hasValidType(blPropertyValues[0], blPropertyValues[1]);
			if(hasValidType)
			{
				resultOccurence = processLine(bomLine, parentOccurence);
			}
		} catch (TCException ex)
		{
			ex.printStackTrace();
		}
		return resultOccurence;
	}
	
	public ReportLineOccurence processLine(TCComponentBOMLine bomLine, ReportLineOccurence parentOccurence)
	{
		ReportLineOccurence resultOccurence = null;
		try{
			if(lineList.containsLineWithUid(bomLine.getItem().getUid())){
				resultOccurence = updateExistingLine(bomLine, parentOccurence);
			} else {
				resultOccurence = addNewLine(bomLine, parentOccurence);
			}
		} catch (TCException ex) {
			ex.printStackTrace();
		}
		return resultOccurence;
	}
	
	public ReportLineOccurence addNewLine(TCComponentBOMLine bomLine, ReportLineOccurence parentOccurence)
	{
		ReportLineOccurence resultOccurence = emptyOccurence;
		try{
			int quantity = blPropertyValues[2].trim().isEmpty()?1:Integer.parseInt(blPropertyValues[2]);
			ReportLine line = new ReportLine(getTypeOfLine(bomLine));
			System.out.println("READER: new line for " + blPropertyValues[3]);
			line.uid = bomLine.getItem().getUid();
			line.id = blPropertyValues[3];
			line.fullName = blPropertyValues[5];
			line.shortName = "Empty";
			TCComponentForm form1C = get1CForm(bomLine.getItem());
			if(form1C!=null)
			{
				System.out.println("READER: form 1c exists for " + blPropertyValues[3]);
				line.fullName = form1C.getProperty("oc9_RightName");
				line.productCode = form1C.getProperty("oc9_ProductCode");
				line.provider = form1C.getProperty("oc9_Provider");
				line.shippingDocument = form1C.getProperty("oc9_ShippingDocument");
				line.price = form1C.getProperty("oc9_Price");
			} else {
				System.out.println("READER: form 1c is null for " + blPropertyValues[3]);
			}
			if(line.type==ReportLineType.DOCUMENT) {
				line.fullName = "Ведомость покупных\n"+line.fullName;
			}
			line.shortName = getShortName(line.fullName, line.productCode, line.provider);
			resultOccurence = new ReportLineOccurence(line, parentOccurence);
			resultOccurence.setQuantity(quantity);
			resultOccurence.bomLine = bomLine;
			resultOccurence.remark = blPropertyValues[4];
			resultOccurence.reserveFactor = parseAdjustValue(blPropertyValues[6]);
			line.addOccurence(resultOccurence);
			lineList.addLine(line);
		} catch (Exception ex) 
		{
			ex.printStackTrace();
		}
		return resultOccurence;
	}
	
	public ReportLineOccurence updateExistingLine(TCComponentBOMLine bomLine, ReportLineOccurence parentOccurence)
	{
		ReportLineOccurence resultOccurence = emptyOccurence;
		try{
			int quantity = blPropertyValues[2].trim().isEmpty()?1:Integer.parseInt(blPropertyValues[2]);
			ReportLine line = lineList.getLine(bomLine.getItem().getUid());
			System.out.println("READER: upd line for " + blPropertyValues[3] + " with quantity " + quantity);
			if(line.type==ReportLineType.DOCUMENT) 
				return null;
			resultOccurence = new ReportLineOccurence(line, parentOccurence);
			resultOccurence.setQuantity(quantity);
			resultOccurence.bomLine = bomLine;
			resultOccurence.remark = blPropertyValues[4];
			line.updateOccurence(resultOccurence);
		} catch (Exception ex) 
		{
			ex.printStackTrace();
		}
		return resultOccurence;
	}
	
	public TCComponentForm get1CForm(TCComponentItem item)
	{
		TCComponentForm form1C = null;
		try{
			form1C = (TCComponentForm) item.getRelatedComponent("Oc9_Cod1CRel");
		} catch (Exception ex){
			ex.printStackTrace();
		}
		return form1C;
	}
	
	public String getShortName(String fullName, String code, String provider)
	{
		String shortName = fullName.replace(code, "");
		shortName = shortName.replace(provider, "");
		shortName = shortName.replaceAll("[\\s]{2,}", " ").trim();
		return shortName;
	}
	
	public boolean hasValidType(String itemType, String partType)
	{
		boolean isCommercial = itemType.equals("Коммерческое изделие");
		boolean isCompanyPart = itemType.equals("Изделие предприятия");
		if(isCommercial) return true;
		if(isCompanyPart && Arrays.asList(companyPartTypes).contains(partType))
			return true;
		return false;
	}
	
	public ReportLineType getTypeOfLine(TCComponentBOMLine bomLine)
	{
		ReportLineType type = ReportLineType.NONE;
		try
		{
			if(blPropertyValues[0].equals("Коммерческое изделие"))
			{
				type = ReportLineType.COMMERCIAL;
			}
			else if (blPropertyValues[1].equals("Сборочная единица"))
			{
				type = ReportLineType.ASSEMBLY;
			}
			else if (blPropertyValues[1].equals("Комплект"))
			{
				type = ReportLineType.KIT;
			}
			if(hasExistingVP(bomLine)) 
				type = ReportLineType.DOCUMENT;
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
		return type;
	}
	
	public boolean hasExistingVP(TCComponentBOMLine bomLine)
	{
		try
		{
			TCComponent[] documents = bomLine.getItemRevision().getRelatedComponents("Oc9_DocRel");
			String IRid = bomLine.getItem().getProperty("item_id");
			if(IRid.equals(VP.topBOMLineI.getProperty("item_id"))) return false; //TODO исправить эту жесть!
			String id;
			for(TCComponent document : documents){
				id = document.getProperty("item_id");
				if(id.equals(IRid + " ВП")){
					return true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	private TCComponentBOMLine[] getChildBOMLines(TCComponentBOMLine parent)
	{
		TCComponentBOMLine[] childLines = null;
		
		ExpandPSOneLevelInfo levelInfo = new ExpandPSOneLevelInfo();
		ExpandPSOneLevelPref levelPref = new ExpandPSOneLevelPref();

		levelInfo.parentBomLines = new TCComponentBOMLine[] { parent };
		levelInfo.excludeFilter = "None";
		levelPref.expItemRev = true;

		ExpandPSOneLevelResponse levelResp = smsService.expandPSOneLevel(levelInfo, levelPref);

		if (levelResp.output.length > 0)
		{
			for (ExpandPSOneLevelOutput levelOut : levelResp.output)
			{
				childLines = new TCComponentBOMLine[levelOut.children.length];
				for (int i=0; i<levelOut.children.length; i++)
				{
					childLines[i] = levelOut.children[i].bomLine;
				}
			}
		}
		
		if(childLines==null) childLines = new TCComponentBOMLine[0];
		
		return childLines;
	}
	
	private void checkIfMonitorIsCancelled(IProgressMonitor monitor)
	{
		if (monitor.isCanceled())
		{
			throw new CancellationException("Чтение данных отменено!");
		}
	}
	
	private double parseAdjustValue(String adjustValue)
	{
		adjustValue = adjustValue.trim();
		if(adjustValue.isEmpty()) return 0;
		
		double result = 0.0;
		
		try
		{
			result = Double.parseDouble(adjustValue);
		} 
		catch (NumberFormatException ex1)
		{
			try
			{
				System.out.println("Failed to parseadjust value, another attempt...");
				adjustValue = adjustValue.replaceAll(",", "\\.");
				result = Double.parseDouble(adjustValue);
			} 
			catch (NumberFormatException ex2)
			{
				System.out.println("Failed to parse adjust value");
				ex2.printStackTrace();
			}
		}
		return result;
	}
	
	private void printData()
	{
		for(ReportLine line : lineList.getSortedList())
		{
			System.out.println("line " + line.fullName);
			for(ReportLineOccurence occurence : line.occurences())
			{
				System.out.println("occurence " + occurence.getParentItemId());
			}
		}
	}
}
