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
	public static final String ASSEMBLY_TYPE = "Assembly";
	public static final String DETAIL_TYPE = "Part";
	public static final String MATERIAL_TYPE = "Oc9_Material";
	public static final String KIT_TYPE = "Set";
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
		mapUom.put("kg", "êã");
		mapUom.put("l", "ë");
		mapUom.put("m", "ì");
		mapUom.put("m2", "ì^2");
		mapUom.put("êã", "êã");
		mapUom.put("ë", "ë");
		mapUom.put("ì", "ì");
		mapUom.put("ì^2", "ì^2");
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
				if(id.equals(IRid + " ÂÏ")){
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
			stampData.id = VP.topBOMLineI.getProperty("item_id") + " ÂÏ";
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
		if (!ReportsItemUtils.hasVP(currentTopBomLine) || (level == 1))
		{
			System.out.println("--- Íåòó ÂÏ!!!!");
			AIFComponentContext[] childBOMLines =  unpackBomList(currentTopBomLine);
			for (int i = 0; i < childBOMLines.length; i++)
			{
				TCComponentBOMLine currBOMLine = (TCComponentBOMLine)childBOMLines[i].getComponent();
				TCComponentItemRevision itemRev = currBOMLine.getItemRevision();
				String type = itemRev.getType();
				String typeOfPart = "";
				
				if (type.equals("Oc9_CompanyPart"))
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
								
							if (mapCurrLevelBoughts.containsKey(id)) {
								VPDataOcc updatedVpLine = mapCurrLevelBoughts.get(id);
								updatedVpLine.qty += qty;
								mapCurrLevelBoughts.put(id, updatedVpLine);
								VPDataOcc updatedGlobalLine = mapGlobalBoughts.get(id);
								updatedGlobalLine.mapWhereUsed.put(upperItemName, updatedGlobalLine.mapWhereUsed.get(upperItemName) + qty * accumQty);
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
									addingLine.idDocForDelivery = itemRev.getProperty("item_id") + " ÂÏ";
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
								addingLine.qty = qty;
								addingLine.demension = mapUom.get(currBOMLine.getProperty("bl_uom"));

								System.out.println(">>>>>> " + currBOMLine.getProperty("Oc9_Note"));
								System.out.println("++++++ " + currMeasure);
								
								
								addingLine.remark = currMeasure.equals("*") ?  "" : currMeasure 
										+ (currBOMLine.getProperty("Oc9_Note").equals("") ? " " : "\n" + currBOMLine.getProperty("Oc9_Note"));
								mapCurrLevelBoughts.put(id, VPDataOcc.copyVpOcc(addingLine));
									
								System.out.println("Adding line: " + addingLine.name);
								
								addingLine.qty = qty * accumQty;
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
			if (updatedLine.mapWhereUsed == null) {
				updatedLine.mapWhereUsed = new HashMap<String, Double>();
				updatedLine.mapWhereUsed.put(addingLine.upperItem, addingLine.qty);
			} else {
				if (updatedLine.mapWhereUsed.containsKey(addingLine.upperItem)) {
					updatedLine.mapWhereUsed.put(addingLine.upperItem, updatedLine.mapWhereUsed.get(addingLine.upperItem + addingLine.qty));
				} else {
					updatedLine.mapWhereUsed.put(addingLine.upperItem, addingLine.qty);
				}
			}
		}
		else
		{
			HashMap<String,Double> mapWhereUsed = new HashMap<String, Double>();
			mapWhereUsed.put(addingLine.upperItem, addingLine.qty);
			addingLine.mapWhereUsed = mapWhereUsed;
			mapGlobalBoughts.put(addingLine.id, addingLine);
		}
	}

	private void copyCached2GlobalMap(HashMap<String, VPDataOcc> inMap, int qty)
	{
		Iterator<Entry<String, VPDataOcc>> it = inMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, VPDataOcc> pairs = (Map.Entry<String, VPDataOcc>)it.next();
			VPDataOcc updatingLine = mapGlobalBoughts.get(pairs.getKey());
			HashMap<String, Double> updatingMap = updatingLine.mapWhereUsed;
			
			System.out.println("\n1 " + pairs.getValue().upperItem  
					 + "\n2 " + updatingMap.get(pairs.getValue().upperItem)
					 + "\n3 " + pairs.getValue().qty * qty
					);
			
			updatingMap.put(pairs.getValue().upperItem, updatingMap.get(pairs.getValue().upperItem) + pairs.getValue().qty * qty);
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
					updatedLine.qty += pairs.getValue().qty * qty;
					cachedItem.put(pairs.getKey(), updatedLine);
				} else {
					VPDataOcc addingLine = pairs.getValue();
					addingLine.qty *= qty;					
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
					System.out.println("BEFORE dataLine: " + updatedLine.name + " with qty: " + updatedLine.qty);
					updatedLine.qty *= qty;
					System.out.println("AFTER dataLine: " + updatedLine.name + " with qty: " + updatedLine.qty);
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
		String outLine = "";
		VPBlock listOfOcc = null;
		VPBlock listOfInnerVP = null;
		
		while (it.hasNext()) {
			Map.Entry<String, VPDataOcc> pair = (Entry<String, VPDataOcc>)it.next();
			VPDataOcc currVpLine = pair.getValue();
			if (currVpLine.isVpSection) {
				if (listOfInnerVP == null) {
					listOfInnerVP = new VPBlock(9);
					listOfInnerVP.title = "ÂÏ ñîñòàâíûõ ÷àñòåé";
				}
				
				listOfInnerVP.add(currVpLine);
			}
			else {
				if (listOfOcc == null)
					listOfOcc = new VPBlock(1);
				listOfOcc.add(currVpLine);
			}
			
			outLine += currVpLine.name + " with map size: " + currVpLine.mapWhereUsed.size() + "\n";
			
			Iterator<Entry<String, Double>> it2 = currVpLine.mapWhereUsed.entrySet().iterator();
			while (it2.hasNext()) {
				Map.Entry<String, Double> pair2 = (Entry<String, Double>)it2.next();
				if (currVpLine.arrayListWhereUsed == null)
					currVpLine.arrayListWhereUsed = new ArrayList<String>();
				currVpLine.arrayListWhereUsed.add(pair2.getKey());
				
				outLine += "\t" + pair2.getKey() + "\t" + pair2.getValue() + "\n";
			}
			Collections.sort(currVpLine.arrayListWhereUsed, new ComparingID());
//			Collections.sort(currVpLine.arrayListWhereUsed, new StrSort());

			currVpLine.makeOneStringFromWhereUsedMapAndQty();
		}
//		Collections.sort(listOfOcc, new VPLinesSort(Field.NAME));
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
	}
}
