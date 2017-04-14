package ru.idealplm.pm.vp2g;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.JarURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.jar.Pack200.Unpacker;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.FileLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;










//import ru.idealplm.pm.sp2G.SPTable;
//import ru.idealplm.pm.sp2G.SpecificationBlock;
import ru.idealplm.pm.vp2g.BuildVP2G;
import ru.idealplm.pm.vp2g.EngineVP2G;
import ru.idealplm.pm.vp2g.VPDataOcc;
import ru.idealplm.pm.vp2g.VPTable;
import ru.idealplm.pm.vp2g.VPBlock;
import ru.idealplm.xml2pdf2.handlers.PDFBuilder;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsOutput;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsProperties;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemIdsAndInitialRevisionIds;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ReviseProperties;
import com.teamcenter.services.rac.core._2007_01.DataManagement.CreateOrUpdateFormsResponse;
import com.teamcenter.services.rac.core._2007_01.DataManagement.FormInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseOutput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseResponse2;

public class EngineVP2G
{
	private static Logger log = Logger.getLogger(Thread.currentThread()
			.getStackTrace()[0].getClassName());

	static TCSession session;

	public static int numOfCurrLine;

	public enum Month {
		JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC, ЯНВ, ФЕВ, МАР, АПР, МАЙ, ИЮН, ИЮЛ, АВГ, СЕН, ОКТ, НОЯ, ДЕК
	}
	
	public enum Section {DOCS, COMPLEXES, ASSEMBLIES, DETAILS, STANDARTS, RESTS, MATERIALS, KITS}
	
	public enum Statuses {RELEASED}
	
	enum Field {NUMBER, FORMAT, ZONE, ID, NAME, QTY, REMARK}

	public static File xmlFile = null;
	public static File pdfFile = null;

	static VPTable vpTable;
	static int page;

	static ArrayList<VPBlock> blocks = new ArrayList<VPBlock>();

	static TCComponentItemRevision topIR;
	
	final static int firstPageColumnQty = 26;
	final static int notFirstPageColumnQty = 29;
	

	static String factory;
	static String nonbreakable;

	static String[] matType;
	static String[] factoryArray;
	static String[] factoryArrayForStamp;
	static String[] nonbreakableArray;
	static String[] nonbreakablePlaneArray;

	static String[] prefetchedBlockTitles;

	static ArrayList<String> suspectMaterials = new ArrayList<String>();
	public static ArrayList<Integer> max_cols_sise_a1 = null;

	public static final String DOC_TYPE = "Oc9_KDRevision";
	public static final String COMPLEX_TYPE = "Complex";
	public static final String ASSEMBLY_TYPE = "Assembly";
	public static final String DETAIL_TYPE = "Part";
	public static final String MATERIAL_TYPE = "Oc9_Material";
	public static final String KIT_TYPE = "Set";
	public static final String GEOM_TYPE = "GeomOfMaterial";

	static ArrayList<String> docCodesArrayList;
	static ArrayList<String> kitCodesArrayList;
	static ArrayList<String> arrListErrorElements;

	static Map<String, String> form_block;
	static Map<String, HashMap<String, VPDataOcc>> mapItemsCached;
	static Map<String, VPDataOcc> mapGlobalBoughts;
	static Map<String, String> mapUidToMeasurement;
	static boolean multiWhereUsed;
	
	private static int totalLines;
	private static int globalPosition = 0;
	static boolean hasWrongZones = false;
	static long startTime;

	static TCComponentItemRevision vpIR;

	static boolean doRenumerize = false;
	
	private static final String CLIENT_ID = UUID.randomUUID().toString();

	static boolean isSplittedByBlocks = false;
	static boolean isAddEmptyAfterEach = false;
	
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
	
	static void initialize() throws TCException, IOException {
		log.info("Initializing...");
		
		globalPosition = 0;
		topIR = BuildVP2G.topBomLine.getItemRevision();
		session = BuildVP2G.topBomLine.getSession();
		vpIR = null;
		
		form_block = new HashMap<String, String>();
		page = 0;

		nonbreakable = "ГОСТ>,ОСТ>,ТУ>,СТП>,Ц15.>,Ц12.>,фос.>";

		nonbreakableArray = nonbreakable.split(",\\s*");

		nonbreakablePlaneArray = nonbreakableArray.clone();
		for (int i = 0; i < nonbreakablePlaneArray.length; i++) {
			nonbreakablePlaneArray[i] = nonbreakablePlaneArray[i].replaceAll(
					"[<>]", "");
		}

		numOfCurrLine = 1;
	}

	static HashMap<String, VPDataOcc> getListOfBoughts(TCComponentBOMLine currentTopBomLine, Integer accumQty, int level) throws Exception
	{
		level++;
		HashMap<String, VPDataOcc> mapCurrLevelBoughts = null;
		String topId = currentTopBomLine.getItemRevision().getProperty("item_id");
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
	
	private static void addVpDataOccToGlobalPool(VPDataOcc addingLine)
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

	private static void copyCached2GlobalMap(HashMap<String, VPDataOcc> inMap, int qty)
	{
		Iterator it = inMap.entrySet().iterator();
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
	
	private static void cacheItem(String uid, HashMap<String, VPDataOcc> boughts, int qty) {
		if (mapItemsCached == null) 
			mapItemsCached = new HashMap<String, HashMap<String,VPDataOcc>>();

		if (mapItemsCached.containsKey(uid)) {
			System.out.println("...ALREADY HAS...");
			HashMap<String, VPDataOcc> cachedItem = mapItemsCached.get(uid);
			Iterator it = boughts.entrySet().iterator();
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

	private static HashMap<String, VPDataOcc> mulQtyEachVpLine(HashMap<String, VPDataOcc> inMap, int qty) {
		HashMap<String, VPDataOcc> outMap = null;
		if (inMap != null) {
			outMap = new HashMap<String, VPDataOcc>(inMap);
			if (qty > 1) {
				Iterator it = outMap.entrySet().iterator();
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
	
	public static boolean addToTeamcenter() throws Exception {
		TCComponentItemRevision vpRev = ReportsItemUtils.getVpRev(BuildVP2G.topBomLine);
		if((vpRev != null) && !ReportsItemUtils.isComponentHasReleasedStatus(vpRev)) {
			System.out.println("Deleting previous dataset...");
			deletePrevVpDataSetOfItemPart(BuildVP2G.topBomLine.getItemRevision());
			vpIR = vpRev;
		}
		else if (vpRev == null) {
			TCComponentItem kdDoc = findKDDocItem();
			if (kdDoc == null) {
				System.out.println("CREATING KD ITEM WITH FIRST ITEMREVISION + SignForm!");
				TCComponentItemRevision newItemRev = (TCComponentItemRevision)createItem("Oc9_KD", BuildVP2G.topBomLine.getItemRevision().getProperty("item_id") + " ВП",
						topIR.getProperty("object_name"),
						"Создано утилитой по генерации документа \"Ведомость покупных\"")[1];
				vpIR = newItemRev;
				//createAndAddFormTo(vpIR , "Pm8_GeneralNoteForm", "Pm8_SignRel");
				
			}
			else {
				System.out.println("REVISE AND REMOVE VP + SignForm...");
				vpIR = createNextRevisionBasedOn(kdDoc.getLatestItemRevision());

				if (vpIR != null) {
					System.out.println("vpIR not NULL!");
					
					deletePrevVpDatasetOfKd(vpIR);
					deletePrevSignForm(vpIR);
					//createAndAddFormTo(vpIR, "Pm8_GeneralNoteForm",	"Pm8_SignRel");
				}
			}
			
			vpIR.setProperty("oc9_Format", "A3");
					
			topIR.add("Oc9_DocRel", vpIR.getItem());
			topIR.lock();
			topIR.save();
			topIR.unlock();
		}

		InputStream template = EngineVP2G.class.getResourceAsStream("/template/VP.xsl");
		InputStream fontConfig = getResourceInputStream("/template/userconfig.xml");

		if (xmlFile == null)
			System.out.println("XML FILE == NULL!!!");
		
		pdfFile = PDFBuilder.xml2pdf(xmlFile, template, fontConfig);
		TCComponentDataset ds_new = createDatasetAndAddFile(pdfFile.getAbsolutePath());
		if (ds_new != null) {
			System.out.println("Adding to item_id: " + vpIR.getProperty("item_id"));
			vpIR.add("IMAN_specification", ds_new);
			vpIR.lock();
			vpIR.save();
			vpIR.unlock();
			
			Desktop.getDesktop().open(ds_new.getFiles("")[0]);
			
			return true;
		}
		return false;
	}

	
	static TCComponentDataset createDatasetAndAddFile(String file_path)
			throws TCException {
		TCComponentDataset ret = null;
		String dataset_tool = null;
		String dataset_type = null;
		dataset_tool = "PDF_Reference";
		dataset_type = "PDF";
		TCComponentDatasetType dst = (TCComponentDatasetType) topIR.getSession().getTypeComponent("Dataset");
		ret = dst.create(gen_dataset_name(), "Ведомость покупных", dataset_type);
		ret.setFiles(new String[] { pdfFile.getAbsolutePath() }, new String[] { dataset_tool });
		ret.lock();
		ret.save();
		ret.unlock();

		return ret;
	}
	
	
	private static String gen_dataset_name() throws TCException {
		String ret = null;
		if (topIR != null)
			ret = "ВП - "
					+ topIR.getTCProperty("object_name").getStringValue();
		return ret;
	}
	
	
	static TCComponentItem findKDDocItem() throws TCException {
		TCComponentItem result = null;
		TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent("Item");
		String criteria = topIR.getProperty("item_id") + " ВП";
		TCComponentItem[] items = itemType.findItems(criteria);
		if (items != null && items.length > 0) {
			System.out.println("HAS FOUND KDDOC ITEM!");
			result = items[0];
		}
		return result;
	}
	
	private static InputStream getResourceInputStream(String resourcename) {
		InputStream result = null;
		if ((resourcename != null) && (resourcename.length() > 0)) {
			try {
				URL url = EngineVP2G.class.getResource(resourcename);
				if (url != null) {
					if (url.getProtocol().equalsIgnoreCase("bundleresource")) {
						url = FileLocator.resolve(url);
					}
					if (url.getProtocol().equalsIgnoreCase("file")) {
						File file = new File(url.getPath());
						result = new FileInputStream(file);
					} else if (url.getProtocol().equalsIgnoreCase("jar")) {
						JarURLConnection connection = (JarURLConnection) url
								.openConnection();
						JarFile jarfile = connection.getJarFile();
						ZipEntry entry = jarfile.getEntry(resourcename
								.startsWith("/") ? resourcename.substring(1)
								: resourcename);
						result = jarfile.getInputStream(entry);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				MessageBox.post(ex);
			}
		}
		return result;
	}
	
	private static void deletePrevVpDataSetOfItemPart(TCComponentItemRevision rev) throws Exception {
		AIFComponentContext[] contextArray = rev.getRelated("Oc9_DocRel");
		System.out.println("num of children: " + contextArray.length);
		System.out.println("}}}}}}}}} " + rev.getProperty("pm8_Designation"));
		for (AIFComponentContext currContext : contextArray) {
			if (currContext.getComponent().getProperty("item_id").equals(rev.getProperty("item_id") + " ВП")) {
				System.out.println(" -> Deleting previous Dataset: " + currContext.getComponent().getProperty("item_id"));
				deletePrevVpDatasetOfKd((TCComponentItemRevision)currContext.getComponent());
			}
		}
	}
	
	private static void deletePrevVpDatasetOfKd(TCComponentItemRevision rev) throws Exception {
		
		for (AIFComponentContext compContext : rev.getChildren()) {
			System.out.println(">>> TYPE: " + compContext.getComponent().getProperty("object_type") 
					+ "\nDesc: " + compContext.getComponent().getProperty("object_desc"));
			if ((compContext.getComponent() instanceof TCComponentDataset) 
//					&& compContext.getComponent().getProperty("object_type").equals("Pm8_SpecDataSet")) { 
					&& compContext.getComponent().getProperty("object_desc").equals("Ведомость покупных")) {
				System.out.println("Deleting Spec Dataset in KD");
				((TCComponent) compContext.getComponent()).removeAndDestroy("IMAN_specification", rev);
				System.out.println("after destroying");
				rev.lock();
				rev.save();
				rev.unlock();
			}
		}
	}
	
	private static void createAndAddFormTo(TCComponentItemRevision itemRev, String formType, String relationType) {
		log.info("createAndAddFormTo");
		if (itemRev != null) {
			System.out.println("FormInfo setting...");
			FormInfo formInfo = new FormInfo();
			formInfo.clientId = CLIENT_ID;
			formInfo.formObject = null;
			formInfo.formType = formType;
			formInfo.relationName = relationType;
			formInfo.name = "Подписи";
			formInfo.description = "Подписи для Спецификации";
			formInfo.parentObject = itemRev;
			formInfo.saveDB = true;
			CreateOrUpdateFormsResponse response = BuildVP2G.dmService
					.createOrUpdateForms(new FormInfo[] { formInfo });
			System.out.println("FormInfo setting... DONE!");
		}
	}

	private static void deletePrevSignForm(TCComponentItemRevision rev) throws Exception {
		log.info("deletePrevSignForm");
		for (AIFComponentContext compContext : rev.getRelated("Oc9_SignRel")) {
			if ((compContext.getComponent() instanceof TCComponentForm) 
					&& compContext.getComponent().getProperty("object_name").equals("Подписи")) {
				System.out.println("Deleting PDF Dataset in KD");
				rev.remove("Oc9_SignRel", (TCComponentForm)compContext.getComponent());
				rev.lock();
				rev.save();
				rev.unlock();
			}
		}
	}
	
	private static TCComponentItemRevision createNextRevisionBasedOn(TCComponentItemRevision itemRev) {
		TCComponentItemRevision out = null;
		
		ReviseProperties revProp = new ReviseProperties();
		ReviseInfo revInfo = new ReviseInfo();
		revInfo.baseItemRevision = itemRev;
		ReviseResponse2 response = BuildVP2G.dmService.revise2(new ReviseInfo[] {revInfo});
		
		System.out.println("MAP SIZE = " + response.reviseOutputMap.size());
		Iterator it = response.reviseOutputMap.entrySet().iterator();
		if (it.hasNext()) {
			System.out.println("trying to return itemRev...");
			Map.Entry entry = (Entry) it.next();
			System.out.println("Class NAME VALUE: " + entry.getValue().getClass().getName() + " = " + entry.getKey()
					+ "\nClass NAME KEY: " + entry.getKey().getClass().getName()
//					+ "\nNew REV ID: " + ((RevisionIds)entry.getValue()).
					);
			out = ((ReviseOutput)entry.getValue()).newItemRev;
		}
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private static CreateItemsOutput[] createItems(final ItemIdsAndInitialRevisionIds[] itemIds, final String itemType, final String itemName, final String itemDesc)
			throws TCException {
//		final GetItemCreationRelatedInfoResponse relatedResponse = BuildSpec2G.dmService.getItemCreationRelatedInfo(itemType, null);
		final ItemProperties[] itemProps = new ItemProperties[itemIds.length];
		for (int i = 0; i < itemIds.length; i++) {
			final ItemProperties itemProperty = new ItemProperties();
			itemProperty.clientId = CLIENT_ID;
			itemProperty.itemId = itemIds[i].newItemId;
			itemProperty.revId = itemIds[i].newRevId;
			itemProperty.name = itemName;
			itemProperty.type = itemType;
			itemProperty.description = itemDesc;
			itemProperty.uom = "";
			itemProps[i] = itemProperty;
		}

		final CreateItemsResponse response = BuildVP2G.dmService.createItems(
				itemProps, null, null);
		return response.output;
	}

	public static TCComponent[] createItem(final String type, final String id,
			final String name, final String desc) throws TCException {
		
		final ItemIdsAndInitialRevisionIds[] itemIds = generateItemIds(1, type);
		final CreateItemsOutput[] newItems = createItems(itemIds, type, name, desc);
		
		newItems[0].item.setProperty("item_id", id);

		return new TCComponent[] { newItems[0].item, newItems[0].itemRev };
	}

	@SuppressWarnings("unchecked")
	private static ItemIdsAndInitialRevisionIds[] generateItemIds(final int numberOfIds, final String type) throws TCException {
		final GenerateItemIdsAndInitialRevisionIdsProperties property = new GenerateItemIdsAndInitialRevisionIdsProperties();
		property.count = numberOfIds;
		property.itemType = type;
		property.item = null; // Not used
		final GenerateItemIdsAndInitialRevisionIdsResponse response = BuildVP2G.dmService
				.generateItemIdsAndInitialRevisionIds(new GenerateItemIdsAndInitialRevisionIdsProperties[] { property });
		final BigInteger bIkey = new BigInteger("0");
		final Map<BigInteger, ItemIdsAndInitialRevisionIds[]> allNewIds = response.outputItemIdsAndInitialRevisionIds;
		final ItemIdsAndInitialRevisionIds[] myNewIds = allNewIds.get(bIkey);
		return myNewIds;
	}

	public static void makeAndSortBlocks() throws TCException {
		vpTable = new VPTable();
		log.info("VPTable created...");
		Iterator it = mapGlobalBoughts.entrySet().iterator();
		String outLine = "";
		VPBlock listOfOcc = null;
		VPBlock listOfInnerVP = null;
		
		while (it.hasNext()) {
			Map.Entry<String, VPDataOcc> pair = (Entry)it.next();
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
			
			outLine += currVpLine.name + " with map size: " + currVpLine.mapWhereUsed.size() + "\n";
			
			Iterator it2 = currVpLine.mapWhereUsed.entrySet().iterator();
			while (it2.hasNext()) {
				Map.Entry<String, Double> pair2 = (Entry)it2.next();
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

	
	
	public static int exportXML() {
		try {
			if (vpTable.getRowCount() == 0) {
				System.out.println("SPTABLE IS EMPTY");
				return 2; // 2 - sp_table is empty
			}

			DecimalFormat fmt = new DecimalFormat();
			fmt.setMinimumIntegerDigits(2);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element node_root = document.createElement("root");
			document.appendChild(node_root);

			Element node;

//			node = document.createElement("Max_Cols_Size");
//			for (Integer ii = Integer.valueOf(0); ii.intValue() < max_cols_sise_a1.size(); ii = Integer.valueOf(ii
//					.intValue() + 1))
//				node.setAttribute("Col_" + Integer.toString(ii.intValue() + 1),
//						Integer.toString(((Integer) max_cols_sise_a1.get(ii.intValue())).intValue()));

//			node_root.appendChild(node);

			String val_cell;
			Element node_block = null;

			for (int i = 0; i < vpTable.getRowCount(); i++) {
				Element node_occ = document.createElement("Occurrence");
//				if (vpTable.isTitle(i))
//					node_occ.setAttribute("font", "underline,bold");
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

				if ((node_block == null) || (((i + 1) % (firstPageColumnQty + 1) == 0) && (page == 1))
						|| (((i + 1) - (firstPageColumnQty + 1)) % (notFirstPageColumnQty) == 0)) {
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
			
			form_block = getFormerBlockAttr(topIR);
			Set<String> keys = form_block.keySet();
			for (String idx_form_block : keys)
				if (form_block.get(idx_form_block) != null)
					node.setAttribute(idx_form_block, form_block.get(idx_form_block));
			
			node_root.appendChild(node);
			
			vpTable.clear();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();

			DOMSource source = new DOMSource(document);
			xmlFile = File.createTempFile("vp_export", ".xml");

			// xmlFile = new File("spec_export.xml");
			// FileOutputStream xmlStream = new FileOutputStream(xmlFile);
			// FileChannel xmlChannel = xmlStream.getChannel();
			// xmlChannel.map(FileChannel.MapMode.READ_WRITE, 0, document.)

			StreamResult result = new StreamResult(xmlFile);

			// transformer.transform(source, new FileOutputStream(new File ));
			transformer.transform(source, result);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			MessageBox.post(e);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		return 0;
	}

	
	private static Map<String, String> getFormerBlockAttr(TCComponentItemRevision compRev) throws Exception {
		Map<String, String> ret = new TreeMap<String, String>();
		TCComponent signForm = null;
		
		TCComponentItemRevision vpRev = ReportsItemUtils.getVpRev(BuildVP2G.topBomLine);
		
		if (vpRev != null) {
			signForm = vpRev.getRelatedComponent("Oc9_SignRel");
		}

		String pagesQty = String.valueOf(page);
		ret.put("PAGEQTY", pagesQty);
		ret.put("ZAVOD", factory);
		
		String razr = " ";
		String prov = " ";
		String norm = " ";
		String utv = " ";
		String litera = " ";
		String pervPrim = " ";
		String spCode = " ";
		String crtDate = " ";
		String chkDate = " ";
		String ctrlDate = " ";
		String aprDate = " ";
		String obozn = " ";
		String prjName =" ";
		String naimen = " ";
		
		obozn = compRev.getProperty("pm8_Designation") + " ВП";
		naimen = compRev.getProperty("object_name");
		
		System.out.println(">>>>>>>>>>>" + naimen + "<<<<<");
		
		/*if (topIR.getItem().getRelated("Pm8_SAPRKTI").length > 0) {
			System.out.println("got > 0");
			spCode = (topIR.getItem().getRelated("Pm8_SAPRKTI")[0]).getComponent().getProperty("pm8_SPCode");
			prjName = (topIR.getItem().getRelated("Pm8_SAPRKTI")[0]).getComponent().getProperty("pm8_FstPrj");
		}*/
		
		if (signForm != null) {
			razr = signForm.getProperty("pm8_Designer");
			prov = signForm.getProperty("pm8_Cheker");   
			norm = signForm.getProperty("pm8_NChecker");  
			utv = signForm.getProperty("pm8_Approver");   
			litera = signForm.getProperty("pm8_StageLiter");
			pervPrim = signForm.getProperty("pm8_1stProject");
			crtDate = DateUtil.parseDateFromTC(signForm.getProperty("pm8_DesignDate"));   
			chkDate = DateUtil.parseDateFromTC(signForm.getProperty("pm8_ChekDate"));   
			ctrlDate = DateUtil.parseDateFromTC(signForm.getProperty("pm8_NCheckDate")); 
			aprDate = DateUtil.parseDateFromTC(signForm.getProperty("pm8_ApproveDate")); 
		}
		
		ret.put("RAZR", razr.equals("")? " " : razr);
		ret.put("PROV", prov.equals("")? " " : prov); 
		ret.put("NORM", norm.equals("")? " " : norm);
		ret.put("UTV", utv.equals("")? " " : utv);
		ret.put("LITERA", litera.equals("")? " " : litera);
		ret.put("PERVPRIM", pervPrim.equals("")? " " : pervPrim);
		ret.put("SPCODE", spCode.equals("")? " " : spCode);
		ret.put("CRTDATE", crtDate.equals("")? " " : crtDate); 
		ret.put("CHKDATE", chkDate.equals("")? " " : chkDate); 
		ret.put("CTRLDATE", ctrlDate.equals("")? " " : ctrlDate);
		ret.put("APRDATE", aprDate.equals("")? " " : aprDate); 
		ret.put("OBOZNACH", obozn.equals("")? " " : obozn); 
		ret.put("PROJECTNAME", prjName.equals("")? " " : prjName);
		ret.put("NAIMEN", naimen.equals("")? " " : naimen);
		return ret;
	}

	static AIFComponentContext[] unpackBomList(TCComponentBOMLine topBomLine) throws TCException {
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
	
	static TCComponent getSourceForGeometry(TCComponentItemRevision itemRev) throws TCException {
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
}
