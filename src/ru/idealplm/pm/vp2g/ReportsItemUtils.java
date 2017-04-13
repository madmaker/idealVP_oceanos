package ru.idealplm.pm.vp2g;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




import ru.idealplm.pm.vp2g.EngineVP2G.Month;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
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

public class ReportsItemUtils {
	
	private static Logger log = Logger.getLogger(Thread.currentThread()
			.getStackTrace()[0].getClassName());
	
	public static final String DOC_TYPE = "Pm8_KDRevision";
	public static final String COMPLEX_TYPE = "Complex";
	public static final String ASSEMBLY_TYPE = "Assembly";
	public static final String DETAIL_TYPE = "Part";
	public static final String MATERIAL_TYPE = "Pm8_Material";
	public static final String KIT_TYPE = "Set";
	public static final String GEOM_TYPE = "GeomOfMaterial";
	
	protected ReportsItemUtils(){
		
	}
	
	// Упаковывает все линии, входящие в состав переданной как аргумент
	public static void packLines(TCComponentBOMLine bomLineToPack) throws TCException {
		AIFComponentContext[] childBOMLines = bomLineToPack.getChildren();
		for (AIFComponentContext currBOMLineContext : childBOMLines) {
			TCComponentBOMLine bomLine = (TCComponentBOMLine) currBOMLineContext.getComponent();
			bomLine.pack();
		}
		bomLineToPack.refresh();
	}
	
	// Распаковывает все линии, входящие в состав переданной как аргумент
	public static void unpackLines(TCComponentBOMLine bomLineToUnPack) throws TCException {
		AIFComponentContext[] childBOMLines = bomLineToUnPack.getChildren();
		for (AIFComponentContext currBOMLineContext : childBOMLines) {
			TCComponentBOMLine bomLine = (TCComponentBOMLine) currBOMLineContext
					.getComponent();
			bomLine.unpack();
		}
		bomLineToUnPack.refresh();
	}
	
	// Возвращает Map из имён атрибутов штампа и их значений
	public static Map<String, String> getFormerBlockAttr(TCComponentItemRevision compRev, TCComponentItemRevision docRev, String page, String factory) throws Exception {
		Map<String, String> ret = new TreeMap<String, String>();
		TCComponent signForm = null;
		
		if (docRev != null) {
			signForm = docRev.getRelatedComponent("Oc9_SignRel");
		}

		String pagesQty = String.valueOf(page);
		ret.put("PAGEQTY", pagesQty);
		ret.put("ZAVOD", factory);
		
		String razr = " ";
		String prov = " ";
		String norm = " ";
		String utv = " ";
		String litera = " ";
		String addChecker = " ";
		String addCheckerDate = " ";
		//String pervPrim = " ";
		String spCode = " ";
		String crtDate = " ";
		String chkDate = " ";
		String ctrlDate = " ";
		String aprDate = " ";
		String obozn = " ";
		String prjName =" ";
		String naimen = " ";
		
		obozn = compRev.getProperty("pm8_Designation");
		naimen = compRev.getProperty("object_name");
		if (compRev.getItem().getRelated("Pm8_SAPRKTI").length > 0) {
			System.out.println("got > 0");
			spCode = (compRev.getItem().getRelated("Pm8_SAPRKTI")[0]).getComponent().getProperty("pm8_SPCode");
			prjName = (compRev.getItem().getRelated("Pm8_SAPRKTI")[0]).getComponent().getProperty("pm8_FstPrj");
		}
		
		if (signForm != null) {
			razr = signForm.getProperty("pm8_Designer");
			prov = signForm.getProperty("pm8_Checker");   
			norm = signForm.getProperty("pm8_NChecker");  
			utv = signForm.getProperty("pm8_Approver");   
			litera = signForm.getProperty("pm8_StageLiter");
			addChecker = signForm.getProperty("pm8_AddChecker");
			//pervPrim = signForm.getProperty("pm8_1stProject");
//			spCode = signForm.getProperty("pm8_SPCode");  
//			prjName = signForm.getProperty("pm8_1stProject"); 
			crtDate = DateUtil.parseDateFromTC(signForm.getProperty("pm8_DesignDate"));   
			chkDate = DateUtil.parseDateFromTC(signForm.getProperty("pm8_CheckDate"));   
			ctrlDate = DateUtil.parseDateFromTC(signForm.getProperty("pm8_NCheckDate")); 
			aprDate = DateUtil.parseDateFromTC(signForm.getProperty("pm8_ApproveDate")); 
			addCheckerDate = DateUtil.parseDateFromTC(signForm.getProperty("pm8_AddCheckerDate")); 
		}
		
		ret.put("RAZR", razr.equals("")? " " : razr);
		ret.put("PROV", prov.equals("")? " " : prov); 
		ret.put("NORM", norm.equals("")? " " : norm);
		ret.put("UTV", utv.equals("")? " " : utv);
		ret.put("LITERA", litera.equals("")? " " : litera);
		ret.put("ADDCHECKER", addChecker.equals("")? " " : addChecker);
		ret.put("ADDCHECKERDATE", addCheckerDate.equals("")? " " : addCheckerDate);
		//ret.put("PERVPRIM", pervPrim.equals("")? " " : pervPrim);
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
	
	// Возвращает Ревизию изделия ВП для аргумента - строки состава изделия
	/*static TCComponentItemRevision getVpRev(TCComponentBOMLine topBOMLine) throws Exception {
		AIFComponentContext[] compContext = topBOMLine.getItemRevision().getRelated("Pm8_DocRel");
		for (AIFComponentContext currContext : compContext) {
			String type = currContext.getComponent().getType();
			if (type.equals("Pm8_KDRevision")) {
				if (currContext.getComponent().getProperty("item_id")
					.equals(topBOMLine.getItemRevision().getProperty("pm8_Designation") + " ВП")) {
					return (TCComponentItemRevision) currContext.getComponent();
				}
			}
		}
		return null;
	}*/
	static TCComponentItemRevision getVpRev(TCComponentBOMLine topBOMLine) throws TCException {
		TCComponentItem result = null;
		TCComponentItemType itemType = (TCComponentItemType) BuildVP2G.session.getTypeComponent("Oc9_KD");
		String criteria = topBOMLine.getItemRevision().getProperty("item_id") + " ВП";
		TCComponentItem[] items = itemType.findItems(criteria);
		if (items != null && items.length > 0) {
			for(TCComponentItem item : items){
				System.out.println("Found item " + item.getProperty("item_id") + " of type " + item.getType());
				if(item.getType().equals("Oc9_KD")){
					result = item;
					break;
				}
			}
		}
		
		return result.getLatestItemRevision();
	}
	
	// Создаёт форму и связывает её указанным отношение с указанным объектом
	public static void createAndAddFormTo(TCComponentItemRevision parentItemRev, String formType, String relationType, String formName, String formDesc) {
		
		if (parentItemRev == null)
			System.out.println("FUCK PARENT ITEM REV FOR FORM IS NULL!!");
		
		FormInfo formInfo = new FormInfo();
		formInfo.formObject = null;
		formInfo.formType = formType;
		formInfo.relationName = relationType;
		formInfo.name = formName;
		formInfo.description = formDesc;
		formInfo.parentObject = parentItemRev;
		formInfo.saveDB = true;
		
		CreateOrUpdateFormsResponse response = BuildVP2G.dmService.createOrUpdateForms(new FormInfo[] {formInfo});
		
	}
	
	// Создаёт набор данных PDF и прикрепляет к нему файл
	static TCComponentDataset createPDFDatasetAndAddFile(String filePath, String datasetName, String datasetDesc)
			throws TCException {
		TCComponentDataset ret = null;
		String dataset_tool = null;
		String dataset_type = null;
		dataset_tool = "PDF_Reference";
		dataset_type = "PDF";
		TCComponentDatasetType dst = (TCComponentDatasetType) getSession().getTypeComponent("Dataset");
		ret = dst.create(datasetName, datasetDesc, dataset_type);
		ret.setFiles(new String[] { filePath }, new String[] { dataset_tool });
		ret.lock();
		ret.save();
		ret.unlock();
		
		return ret;
	}
	
	// Генерирует имя для набора данных со спецификацией
	private static String gen_sp_dataset_name(TCComponentItemRevision parent) throws TCException {
		String ret = null;
		if (parent != null)
			ret = "Спецификация - "
					+ parent.getTCProperty("object_name").getStringValue();
		return ret;
	}
	
	// Проверяет не заблокирован ли набор данных ВП
	static boolean isVpDatasetBlocked(TCComponentItemRevision itemRev) throws TCException, Exception {
		for (AIFComponentContext compContext : itemRev.getChildren()) {

			if ((compContext.getComponent() instanceof TCComponentDataset)
					&& compContext.getComponent().getProperty("object_desc").equals("Ведомость покупных")
					&& compContext.getComponent().getProperty("expl_checkout").equals("Y")
					) {
				log.warning("VP dataset is blocked");
				return true;
			}
		}
		return false;
	}
	
	// Возвращает массив значений LOV
	static String[] getLOV(String property) {
		String[] foundElements = null;
		try {
			TCComponent[] foundComponents = getSession().getClassService().findByClass("ListOfValues", "lov_name", property);
			if ((foundComponents == null) || (foundComponents.length == 0)) {
				return null;
			}
			ListOfValuesInfo lovInfo = ((TCComponentListOfValues) foundComponents[0]).getListOfValues();
			if (lovInfo != null)
				foundElements = lovInfo.getStringListOfValues();
		} catch (TCException e) {
			e.printStackTrace();
		}
		return foundElements;
	}
	
	// Возвращает текущую сессию
	static TCSession getSession(){
		return (TCSession) AIFUtility.getCurrentApplication().getSession();
	}
	
	// Возвращает текущий DataManagementService
	static DataManagementService getDMService(){
		return DataManagementService.getService(getSession());
	}
	
	// Создаёт новую ревизию на основе старой
	private static TCComponentItemRevision createNextRevisionBasedOn(TCComponentItemRevision itemRev) {
		TCComponentItemRevision out = null;
		System.out.println("DMService: " + getDMService());
		
		ReviseProperties revProp = new ReviseProperties();
		ReviseInfo revInfo = new ReviseInfo();
		revInfo.baseItemRevision = itemRev;
		ReviseResponse2 response = getDMService().revise2(new ReviseInfo[] {revInfo});
		
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
	
	private static CreateItemsOutput[] createItems(final ItemIdsAndInitialRevisionIds[] itemIds, final String itemType, final String itemName, final String itemDesc)
			throws TCException {
		final ItemProperties[] itemProps = new ItemProperties[itemIds.length];
		for (int i = 0; i < itemIds.length; i++) {
			final ItemProperties itemProperty = new ItemProperties();
			itemProperty.itemId = itemIds[i].newItemId;
			itemProperty.revId = itemIds[i].newRevId;
			itemProperty.name = itemName;
			itemProperty.type = itemType;
			itemProperty.description = itemDesc;
			itemProperty.uom = "";
			itemProps[i] = itemProperty;
		}

		final CreateItemsResponse response = getDMService().createItems(
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
	
	private static ItemIdsAndInitialRevisionIds[] generateItemIds(final int numberOfIds, final String type) throws TCException {
		final GenerateItemIdsAndInitialRevisionIdsProperties property = new GenerateItemIdsAndInitialRevisionIdsProperties();
		property.count = numberOfIds;
		property.itemType = type;
		property.item = null; // Not used
		final GenerateItemIdsAndInitialRevisionIdsResponse response = getDMService()
				.generateItemIdsAndInitialRevisionIds(new GenerateItemIdsAndInitialRevisionIdsProperties[] { property });
		final BigInteger bIkey = new BigInteger("0");
		final Map<BigInteger, ItemIdsAndInitialRevisionIds[]> allNewIds = response.outputItemIdsAndInitialRevisionIds;
		final ItemIdsAndInitialRevisionIds[] myNewIds = allNewIds.get(bIkey);
		return myNewIds;
	}
	
	// Удаляет имеющуюся форму с подписями у изделия - аргумента
	private static void deletePrevSignForm(TCComponentItemRevision rev) throws Exception {
		for (AIFComponentContext compContext : rev.getChildren()) {
			if ((compContext.getComponent() instanceof TCComponentForm) 
					&& compContext.getComponent().getProperty("object_name").equals("Подписи")) {
				rev.remove("Pm8_SignRel", (TCComponentForm)compContext.getComponent());
				rev.lock();
				rev.save();
				rev.unlock();
			}
		}
	}
	
	// Проверяет является ли изделие стандартным или покупным
	private static boolean isStdNotRst(String namePattern, TCComponentItem inItem) throws TCException {
		boolean answer = false;
	
		String itemID = inItem.getProperty("current_id");
		System.out.println("Commercial Part with itemID: " + itemID);
		
		
		AIFComponentContext[] primaries = inItem.getPrimary();
//		AIFComponentContext[] contextArray = inItemRev.getRelated("Pm8_DocRel");
		
		for (AIFComponentContext currContext : primaries) {
			String itemName = ((TCComponentItem) currContext.getComponent()).getProperty("object_name");
			System.out.println("### --- > " + itemName);
			AIFComponentContext[] childsContext = ((TCComponentItem) currContext.getComponent()).getRelated("Pm8_Instances");
			System.out.println("childsContext.length = " + childsContext.length);
			if (childsContext.length > 0) {
				for (AIFComponentContext context : childsContext) {
					System.out.println("child itemID: " + ((TCComponentItem) context.getComponent()).getProperty("current_id"));
					if (((TCComponentItem) context.getComponent()).getProperty("current_id").equals(itemID)) {
				        System.out.println("Inside REGEXP");
						Pattern p = Pattern.compile(".*("+namePattern+").*");  
				        Matcher m = p.matcher(itemName.toUpperCase());
				        System.out.println("m.matches : " + m.matches());
						if (m.matches()) {
							answer = true;
							break;
						}
					}
				}
				if (answer == true)
					break;
			}
		}
		System.out.println("is StdNotRst ANSWER: " + answer);
		return answer;
	}
	
	// Проверяет выпущено ли изделие
	static boolean isComponentHasReleasedStatus(TCComponent comp) throws TCException
	{
		boolean out = false;
		TCComponent[] statuses = null;
		TCProperty statusProp = comp.getTCProperty("release_status_list");
		if (statusProp != null) {
			statuses = statusProp.getReferenceValueArray();
			for (TCComponent currStatus : statuses) {
				if (currStatus.getProperty("object_name").equals("Released")) {
					out = true;
					break;
				}
			}
		}
		return out;
	}
	
	// Возвращает сырьё для иделия - аргумента
	static TCComponent getSourceForGeometry(TCComponentItemRevision itemRev) throws TCException {
		TCComponent sourceComponent = null;
		String type = itemRev.getItem().getType();
		if (type.equals("Pm8_CompanyPart")) {
			String typeOfPart = itemRev.getItem().getTCProperty("pm8_TypeOfPart").getStringValue();
			if (typeOfPart.equals(GEOM_TYPE) || typeOfPart.equals(DETAIL_TYPE)) {
				TCComponent sourceComp = itemRev.getReferenceProperty("pm8_Source");
				if (sourceComp != null) {
//					if (sourceComp.getType().equals(MATERIAL_TYPE)) {
						return sourceComp;
//					}
				}
			}
		}
		return sourceComponent;
	}
	
	// Возвращает массив наборов данных, прикрепленных к ревизии изделия - аргументу
	public static TCComponentDataset[] getIrDatasets(TCComponentItemRevision item_rev)
			throws TCException {
		ArrayList ret = new ArrayList();
		AIFComponentContext[] ctxt = item_rev.getChildren();
		for (AIFComponentContext aifComponentContext : ctxt) {
			InterfaceAIFComponent c = aifComponentContext.getComponent();
			if ((c instanceof TCComponentDataset)) {
				ret.add(c);
			}
		}
		return (TCComponentDataset[]) ret.toArray(new TCComponentDataset[ret.size()]);
	}

}
