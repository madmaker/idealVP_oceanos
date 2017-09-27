package ru.idealplm.vp.oceanos.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

import ru.idealplm.vp.oceanos.comparator.ComparingID;
import ru.idealplm.vp.oceanos.comparator.ComparingLines;
import ru.idealplm.vp.oceanos.data.VPBlock;
import ru.idealplm.vp.oceanos.data.VPDataOcc;
import ru.idealplm.vp.oceanos.data.VPTable;
import ru.idealplm.vp.oceanos.util.DateUtil;
import ru.idealplm.vp.oceanos.util.ReportsItemUtils;

public class DataReader
{
	public static final String DOC_TYPE = "Oc9_KDRevision";
	public static final String COMPLEX_TYPE = "Complex";
	public static final String ASSEMBLY_TYPE = "Сборочная единица"; // "Assembly"
	public static final String DETAIL_TYPE = "Part";
	public static final String MATERIAL_TYPE = "Oc9_Material";
	public static final String KIT_TYPE = "Комплект"; // "Set"
	public static final String GEOM_TYPE = "GeomOfMaterial";
	public static boolean multiWhereUsed;
	
	private VP vp;
	private StampData stampData;
	
	private ArrayList<String> arrListErrorElements;
	
	private Map<String, HashMap<String, VPDataOcc>> mapItemsCached;
	private Map<String, VPDataOcc> mapGlobalBoughts;
	private Map<String, String> mapUidToMeasurement;
	
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
		this.mapGlobalBoughts = new HashMap<String, VPDataOcc>();
		this.mapUidToMeasurement = new HashMap<String, String>();
		this.arrListErrorElements = new ArrayList<String>();
		this.multiWhereUsed = false;
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
			getListOfBoughts(VP.topBOMLine, 1, 0);
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void prepareData()
	{
		try
		{
			makeAndSortBlocks();
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private HashMap<String, VPDataOcc> getListOfBoughts(TCComponentBOMLine currentTopBomLine, Integer accumQty, int level) throws Exception
	{
		level++;
		HashMap<String, VPDataOcc> mapCurrLevelBoughts = null;
		String topId = currentTopBomLine.getItemRevision().getProperty("item_id");
		
		String parentType = currentTopBomLine.getItemRevision().getType();
		String parentTypeOfPart = "";
		
		if (parentType.equals("Oc9_CompanyPartRevision"))
			parentTypeOfPart = currentTopBomLine.getItem().getTCProperty("oc9_TypeOfPart").getStringValue();
		
		if (!ReportsItemUtils.hasVP(currentTopBomLine) || (level == 1))
		{
			System.out.println("--- Нету ВП!!!!");
			AIFComponentContext[] childBOMLines =  unpackBomList(currentTopBomLine);
			for (int i = 0; i < childBOMLines.length; i++)
			{
				TCComponentBOMLine currBOMLine = (TCComponentBOMLine)childBOMLines[i].getComponent();
				TCComponentItemRevision itemRev = currBOMLine.getItemRevision();
				String type = itemRev.getType();
				String typeOfPart = "";
				
				if (type.equals("Oc9_CompanyPartRevision"))
					typeOfPart = currBOMLine.getItem().getTCProperty("oc9_TypeOfPart").getStringValue();

				boolean isSource = false;
				TCComponent sourceComp = null;
				
				if(type.equals("Oc9_GeomOfMat"))
				{
					sourceComp = getSourceForGeometry(itemRev);
					isSource = (sourceComp != null) ? true : false;
				}
				
				String id = "";
				if (isSource)
					id = sourceComp.getProperty("item_id");
				else
					id = itemRev.getProperty("item_id");

				if (!arrListErrorElements.contains(id)) {
					String qtyStr = currBOMLine.getProperty("bl_quantity");
					Double qty =  qtyStr.equals("")? 1.0 : Double.valueOf(qtyStr);
					String upperItemName = currentTopBomLine.getItem().getProperty("item_id");
					
					if (currBOMLine.getChildren().length > 0 && !ReportsItemUtils.hasVP(currBOMLine)) {
						if (mapItemsCached == null || !mapItemsCached.containsKey(id)) {
							HashMap<String, VPDataOcc> mapBoughts = getListOfBoughts(currBOMLine, accumQty * qty.intValue(), level);
							if (mapBoughts != null) {
								cacheItem(topId, mapBoughts, qty.intValue());
							}
						} else {
							copyCached2GlobalMap(mapItemsCached.get(id), qty.intValue());
						}
					} else {
						boolean isPurchased = itemRev.getProperty("source").equals("2");
						
						if ((isPurchased || type.equals("Oc9_Material") || isSource) || ReportsItemUtils.hasVP(currBOMLine)) {
							String currMeasure = mapUom.get(currBOMLine.getProperty("bl_uom"));
							if (currMeasure == null)
								currMeasure = "*";
							
							if (mapUidToMeasurement == null)
								mapUidToMeasurement = new TreeMap<String, String>();
							
							if (mapUidToMeasurement.containsKey(id)) {
								String usedMeasurement = mapUidToMeasurement.get(id);
								System.out.println("COMPARING:\n\t" + usedMeasurement + "\n\t" + currMeasure);
								if (!usedMeasurement.equals(currMeasure)) {
									arrListErrorElements.add(id);
									if (mapGlobalBoughts.containsKey(id))
										mapGlobalBoughts.remove(id);
									if (mapCurrLevelBoughts.containsKey(id))
										mapCurrLevelBoughts.remove(id);
									continue;
								}
							} else
								mapUidToMeasurement.put(id, currMeasure);
							
							
							if (mapCurrLevelBoughts == null)
								mapCurrLevelBoughts = new HashMap<String, VPDataOcc>();
								
							System.out.println("!!!!!" + parentType + "!!!!!" + parentTypeOfPart);
							if (mapCurrLevelBoughts.containsKey(id)) {
								VPDataOcc updatedVpLine = mapCurrLevelBoughts.get(id);
								if(parentTypeOfPart.equals(ASSEMBLY_TYPE)){
									updatedVpLine.qtyAssy += qty;
								} else if (parentTypeOfPart.equals(KIT_TYPE)) {
									updatedVpLine.qtyKit += qty;
								}
								mapCurrLevelBoughts.put(id, updatedVpLine);
								VPDataOcc updatedGlobalLine = mapGlobalBoughts.get(id);
								if(parentTypeOfPart.equals(ASSEMBLY_TYPE)){
									updatedGlobalLine.mapWhereUsedAssy.put(upperItemName, updatedGlobalLine.mapWhereUsedAssy.get(upperItemName) + qty * accumQty);
								} else if (parentTypeOfPart.equals(KIT_TYPE)) {
									updatedGlobalLine.mapWhereUsedKit.put(upperItemName, updatedGlobalLine.mapWhereUsedKit.get(upperItemName) + qty * accumQty);
								}
							} else {
								VPDataOcc addingLine = new VPDataOcc();
								if (isSource) {
									addingLine.name = sourceComp.getProperty("object_name");
									addingLine.id = sourceComp.getProperty("item_id");
								} else {
									addingLine.name = itemRev.getProperty("object_name");
									addingLine.id = itemRev.getProperty("item_id");
									
								}
								if (ReportsItemUtils.hasVP(currBOMLine))
									addingLine.idDocForDelivery = itemRev.getProperty("item_id") + " ВП";
								else {
									AIFComponentContext[] context = currBOMLine.getItem().whereReferencedByTypeRelation(new String[]{"Oc9_NormDocument"}, new String[] {"Oc9_Instances"});
									if (context != null) {
										if (context.length > 0) {
											addingLine.idDocForDelivery = context[0].getComponent().getProperty("item_id");
										}
									}
								}
								if (ReportsItemUtils.hasVP(currBOMLine))
									addingLine.isVpSection = true;
								addingLine.upperItem = upperItemName;
								if(parentTypeOfPart.equals(ASSEMBLY_TYPE)){
									addingLine.qtyAssy = qty;
								} else if (parentTypeOfPart.equals(KIT_TYPE)) {
									addingLine.qtyKit = qty;
								}
								addingLine.demension = mapUom.get(currBOMLine.getProperty("bl_uom"));

								System.out.println(">>>>>> " + currBOMLine.getProperty("Oc9_Note"));
								System.out.println("++++++ " + currMeasure);
								
								
								addingLine.remark = currMeasure.equals("*") ?  "" : currMeasure 
										+ (currBOMLine.getProperty("Oc9_Note").equals("") ? " " : "\n" + currBOMLine.getProperty("Oc9_Note"));
								mapCurrLevelBoughts.put(id, VPDataOcc.copyVpOcc(addingLine));
									
								System.out.println("Adding line: " + addingLine.name);
								
								if(parentTypeOfPart.equals(ASSEMBLY_TYPE)){
									addingLine.qtyAssy = qty * accumQty;
								} else if (parentTypeOfPart.equals(KIT_TYPE)) {
									addingLine.qtyKit = qty * accumQty;
								}
								addVpDataOccToGlobalPool(addingLine);
									
								if (!multiWhereUsed && level > 1)
									multiWhereUsed = true;
							}
						}
					}
				}
			}
			
			for (int i = 0; i < childBOMLines.length; i++)	{
				TCComponentBOMLine currBOMLine = (TCComponentBOMLine)childBOMLines[i].getComponent();
				currBOMLine.pack();
			}
			
			if (mapCurrLevelBoughts != null) {
				System.out.println("Trying to caching " + topId + " with Name: " + currentTopBomLine.getItemRevision().getProperty("object_name") + " mapSize: " + mapCurrLevelBoughts.size());
				cacheItem(topId, mapCurrLevelBoughts, 1);
				System.out.println("\nCACHING DONE...copy to global to glabal map with QTY: " + accumQty);
			}
		}
		return mapCurrLevelBoughts;
	}
	
	private void addVpDataOccToGlobalPool(VPDataOcc addingLine)
	{
		if (mapGlobalBoughts == null)
			mapGlobalBoughts = new HashMap<String, VPDataOcc>();
		if (mapGlobalBoughts.containsKey(addingLine.id))
		{
			VPDataOcc updatedLine = mapGlobalBoughts.get(addingLine.id);
			if (updatedLine.mapWhereUsedAssy == null) {
				updatedLine.mapWhereUsedAssy = new HashMap<String, Double>();
				updatedLine.mapWhereUsedAssy.put(addingLine.upperItem, addingLine.qtyAssy);
			} else {
				if (updatedLine.mapWhereUsedAssy.containsKey(addingLine.upperItem)) {
					updatedLine.mapWhereUsedAssy.put(addingLine.upperItem, updatedLine.mapWhereUsedAssy.get(addingLine.upperItem + addingLine.qtyAssy));
				} else {
					updatedLine.mapWhereUsedAssy.put(addingLine.upperItem, addingLine.qtyAssy);
				}
			}
			if (updatedLine.mapWhereUsedKit == null) {
				updatedLine.mapWhereUsedKit = new HashMap<String, Double>();
				updatedLine.mapWhereUsedKit.put(addingLine.upperItem, addingLine.qtyKit);
			} else {
				if (updatedLine.mapWhereUsedKit.containsKey(addingLine.upperItem)) {
					updatedLine.mapWhereUsedKit.put(addingLine.upperItem, updatedLine.mapWhereUsedKit.get(addingLine.upperItem + addingLine.qtyKit));
				} else {
					updatedLine.mapWhereUsedKit.put(addingLine.upperItem, addingLine.qtyKit);
				}
			}
		}
		else
		{
			HashMap<String,Double> mapWhereUsedAssy = new HashMap<String, Double>();
			HashMap<String,Double> mapWhereUsedKit = new HashMap<String, Double>();
			mapWhereUsedAssy.put(addingLine.upperItem, addingLine.qtyAssy);
			mapWhereUsedKit.put(addingLine.upperItem, addingLine.qtyKit);
			addingLine.mapWhereUsedAssy = mapWhereUsedAssy;
			addingLine.mapWhereUsedKit = mapWhereUsedKit;
			mapGlobalBoughts.put(addingLine.id, addingLine);
		}
	}

	private void copyCached2GlobalMap(HashMap<String, VPDataOcc> inMap, int qty)
	{
		Iterator<Entry<String, VPDataOcc>> it = inMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, VPDataOcc> pairs = (Map.Entry<String, VPDataOcc>)it.next();
			VPDataOcc updatingLine = mapGlobalBoughts.get(pairs.getKey());
			HashMap<String, Double> updatingMapAssy = updatingLine.mapWhereUsedAssy;
			HashMap<String, Double> updatingMapKit = updatingLine.mapWhereUsedKit;
			
			/*System.out.println("\n1 " + pairs.getValue().upperItem  
					 + "\n2 " + updatingMap.get(pairs.getValue().upperItem)
					 + "\n3 " + pairs.getValue().qty * qty
					);*/
			updatingMapAssy.put(pairs.getValue().upperItem, updatingMapAssy.get(pairs.getValue().upperItem) + pairs.getValue().qtyAssy * qty);
			updatingMapKit.put(pairs.getValue().upperItem, updatingMapKit.get(pairs.getValue().upperItem) + pairs.getValue().qtyKit * qty);
		}
	}
	
	private void cacheItem(String uid, HashMap<String, VPDataOcc> boughts, int qty) {
		if (mapItemsCached == null) 
			mapItemsCached = new HashMap<String, HashMap<String,VPDataOcc>>();

		if (mapItemsCached.containsKey(uid)) {
			System.out.println("...ALREADY HAS...");
			HashMap<String, VPDataOcc> cachedItem = mapItemsCached.get(uid);
			Iterator<Entry<String, VPDataOcc>> it = boughts.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, VPDataOcc> pairs = (Map.Entry<String, VPDataOcc>)it.next();
				if(cachedItem.containsKey(pairs.getKey())) {
					VPDataOcc updatedLine = cachedItem.get(pairs.getKey());
					updatedLine.qtyAssy += pairs.getValue().qtyAssy * qty;
					updatedLine.qtyKit += pairs.getValue().qtyKit * qty;
					cachedItem.put(pairs.getKey(), updatedLine);
				} else {
					VPDataOcc addingLine = pairs.getValue();
					addingLine.qtyAssy *= qty;
					addingLine.qtyKit *= qty;		
					cachedItem.put(pairs.getKey(), addingLine);
				}
			}
			mapItemsCached.put(uid, cachedItem);
		} else {
			System.out.println("...HAS NOT...");
			mapItemsCached.put(uid, mulQtyEachVpLine(boughts, qty));
			System.out.println("++++ CACHED: " + uid + " with Map sized: " + mapItemsCached.get(uid).size());
		}
	}

	private HashMap<String, VPDataOcc> mulQtyEachVpLine(HashMap<String, VPDataOcc> inMap, int qty) {
		HashMap<String, VPDataOcc> outMap = null;
		if (inMap != null) {
			outMap = new HashMap<String, VPDataOcc>(inMap);
			if (qty > 1) {
				Iterator<Entry<String, VPDataOcc>> it = outMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, VPDataOcc> pairs = (Map.Entry<String, VPDataOcc>) it.next();
					VPDataOcc updatedLine = VPDataOcc.copyVpOcc(pairs.getValue());
					System.out.println("BEFORE dataLine: " + updatedLine.name + " with qty: " + updatedLine.qtyAssy);
					updatedLine.qtyAssy *= qty;
					updatedLine.qtyKit *= qty;
					System.out.println("AFTER dataLine: " + updatedLine.name + " with qty: " + updatedLine.qtyAssy);
					outMap.put(pairs.getKey(), updatedLine);
				}
			}
		}
		return outMap;
	}
	
	private AIFComponentContext[] unpackBomList(TCComponentBOMLine topBomLine) throws TCException {
		AIFComponentContext[] childArray = topBomLine.getChildren();
		for (int i=0; i < childArray.length; i++)
			if (((TCComponentBOMLine)childArray[i].getComponent()).isPacked()) {
				((TCComponentBOMLine)childArray[i].getComponent()).unpack();
			}
		
		topBomLine.refresh();
		childArray = topBomLine.getChildren(); 
		ArrayList<AIFComponentContext> arrayListContext = null;
		
		if (childArray.length > 0)
			arrayListContext = new ArrayList<AIFComponentContext>();


		for (int i=0; i < childArray.length; i++)
			arrayListContext.add(childArray[i]);
		
		return arrayListContext.toArray(new AIFComponentContext[arrayListContext.size()]);
	}
	
	private TCComponent getSourceForGeometry(TCComponentItemRevision itemRev) throws TCException {
		System.out.println("Inside getProp for geometry...");
		TCComponent sourceComp = null;
		String type = itemRev.getItem().getType();
		if (type.equals("Oc9_CompanyPart")) {
			String typeOfPart = itemRev.getItem().getTCProperty("oc9_TypeOfPart").getStringValue();
			if (typeOfPart.equals(GEOM_TYPE) || typeOfPart.equals(DETAIL_TYPE)) {
				sourceComp = itemRev.getRelatedComponent("Oc9_SourceRel");
				if (sourceComp != null) {
					System.out.println("Source for " + itemRev.getProperty("item_id") + " not null");
//					if (sourceComp.getType().equals(MATERIAL_TYPE)) {
						return sourceComp;
//					}
				}
			}
		}
		System.out.println("SOURCE IS NULL");
		return sourceComp;
	}
	
	public void makeAndSortBlocks() throws TCException
	{
		VPTable vpTable = vp.report.vpTable;
		Iterator<Entry<String, VPDataOcc>> it = mapGlobalBoughts.entrySet().iterator();
		VPBlock listOfOcc = null;
		VPBlock listOfInnerVP = null;
		
		while (it.hasNext()) {
			Map.Entry<String, VPDataOcc> pair = (Entry<String, VPDataOcc>)it.next();
			VPDataOcc currVpLine = pair.getValue();
			if (currVpLine.isVpSection) {
				if (listOfInnerVP == null) {
					listOfInnerVP = new VPBlock(9);
					listOfInnerVP.title = "ВП составных частей";
				}
				
				listOfInnerVP.add(currVpLine);
			}
			else {
				if (listOfOcc == null)
					listOfOcc = new VPBlock(1);
				listOfOcc.add(currVpLine);
			}
			
			Iterator<Entry<String, Double>> it2 = currVpLine.mapWhereUsedAssy.entrySet().iterator();
			while (it2.hasNext()) {
				Map.Entry<String, Double> pair2 = (Entry<String, Double>)it2.next();
				if (currVpLine.arrayListWhereUsedAssy == null)
					currVpLine.arrayListWhereUsedAssy = new ArrayList<String>();
				currVpLine.arrayListWhereUsedAssy.add(pair2.getKey());
			}
			Iterator<Entry<String, Double>> it3 = currVpLine.mapWhereUsedKit.entrySet().iterator();
			while (it3.hasNext()) {
				Map.Entry<String, Double> pair2 = (Entry<String, Double>)it3.next();
				if (currVpLine.arrayListWhereUsedKit == null)
					currVpLine.arrayListWhereUsedKit = new ArrayList<String>();
				currVpLine.arrayListWhereUsedKit.add(pair2.getKey());
			}
			Collections.sort(currVpLine.arrayListWhereUsedAssy, new ComparingID());
			Collections.sort(currVpLine.arrayListWhereUsedKit, new ComparingID());
			currVpLine.makeOneStringFromWhereUsedMapAndQty();
		}
		if (listOfOcc != null)
			Collections.sort(listOfOcc, new ComparingLines());
		if (listOfInnerVP != null) {
			Collections.sort(listOfInnerVP, new ComparingLines());
			System.out.println("size of list of inner VP: " + listOfInnerVP.size());
		}
		
		if (listOfOcc != null)
			vpTable.fillGridVp(listOfOcc);
		if (listOfInnerVP != null)
			vpTable.fillGridVp(listOfInnerVP);
		System.out.println("VPTABLESIZE" + vpTable.getRowCount());
	}
}
