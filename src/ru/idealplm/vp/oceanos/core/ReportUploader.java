package ru.idealplm.vp.oceanos.core;

import java.awt.Desktop;
import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsOutput;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsProperties;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemIdsAndInitialRevisionIds;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ReviseProperties;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseOutput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseResponse2;

import ru.idealplm.vp.oceanos.handlers.VPHandler;

public class ReportUploader
{
	private VP vp;
	private File renamedReportFile = null;
	public DataManagementService dmService;
	
	public ReportUploader(VP vp)
	{
		this.vp = vp;
	}
	
	public void addToTeamcenter()
	{
		try{
			TCComponentDataset currentVPDataset = null;
			this.dmService = DataManagementService.getService(VPHandler.session);
			
			if(vp.report.report!=null){
				try{
					renamedReportFile = new File(vp.report.data.getAbsolutePath().substring(0, vp.report.data.getAbsolutePath().lastIndexOf("_"))+".pdf");
					Files.deleteIfExists(renamedReportFile.toPath());
					vp.report.report.renameTo(renamedReportFile);
					System.out.println(vp.report.report.getAbsolutePath());
					System.out.println(renamedReportFile.getAbsolutePath());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if(VP.vpIR != null) {
				System.out.println("+++++++++++  SPREV!=NULL");
				currentVPDataset = deletePrevSpecDatasetOfKd();
			} else if (VP.vpIR == null) {
				System.out.println("+++++++++++  SPREV==NULL");
				TCComponentItem kdDoc = findKDDocItem();
				if (kdDoc == null) {
					System.out.println("CREATING KD ITEM WITH FIRST ITEMREVISION + SignForm!");
					TCComponentItemRevision newItemRev = (TCComponentItemRevision)createItem("Oc9_KD", VP.topBOMLineIR.getProperty("item_id") + " ВП",
							VP.topBOMLineIR.getProperty("object_name"),
							"Создано утилитой по генерации документа \"Ведомость покупных\"")[1];
					VP.vpIR = newItemRev;
				} else {
					System.out.println("+++++++++++  KD!=NULL");
					System.out.println(kdDoc.getProperty("item_id"));
					if (isKdLastRevHasAssemblyRev(kdDoc)) {
						System.out.println("REVISE AND REMOVE SP + SignForm...");
						VP.vpIR = createNextRevisionBasedOn(getLastRevOfItem(kdDoc));
						
						if (VP.vpIR != null) {
							deleteRelationsToCompanyPart(VP.vpIR);
							currentVPDataset = deletePrevSpecDatasetOfKd();
						}
					} else {
						System.out.println("REPLACING LAST REVISION!");
						VP.vpIR = kdDoc.getLatestItemRevision();
						currentVPDataset = deletePrevSpecDatasetOfKd();
					}
				}
				
				if(VP.vpIR!=null){
					VP.vpIR.setProperty("oc9_Format", "A3");
					VP.vpIR.lock();
					VP.vpIR.save();
					VP.vpIR.unlock();
					VP.topBOMLine.getItemRevision().add("Oc9_DocRel", new TCComponent[]{VP.vpIR.getItem()});
				}
				//spRev.getItem().("Oc9_DocRel", topBOMLine.getItemRevision());
				//spRev.setProperty("pm8_Format", finalFormat(page));
			}
	
			if(currentVPDataset==null){
				TCComponentDataset ds_new = createDatasetAndAddFile(vp.report.report.getAbsolutePath());
				if (ds_new != null) {
					System.out.println("Adding to item_id: " + VP.vpIR.getProperty("item_id"));
					VP.vpIR.add("IMAN_specification", ds_new);
					saveGeneralNoteFormInfo();
					
					ds_new.getFiles("")[0].setReadOnly();
					Desktop.getDesktop().open(ds_new.getFiles("")[0]);
				}
			} else {
				System.out.println("SPEC DATASET IS NOT NULL");
				String dataset_tool = "PDF_Reference";
				currentVPDataset.setFiles(new String[] { renamedReportFile!=null?renamedReportFile.getAbsolutePath():vp.report.report.getAbsolutePath() }, new String[] { dataset_tool });
				saveGeneralNoteFormInfo();
				vp.report.report=renamedReportFile!=null?renamedReportFile:vp.report.report;
				
				//currentVPDataset.getFiles("")[0].setReadOnly();
				//Desktop.getDesktop().open(currentVPDataset.getFiles("")[0]);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void saveGeneralNoteFormInfo()
	{
		try{
			TCComponent tempComp;
			if((tempComp = VP.vpIR.getRelatedComponent("Oc9_SignRel"))!=null)
			{
				System.out.println("+++++FOUND SIGN FORM!!!!");
				tempComp.setProperty("oc9_Designer", vp.report.stampData.design);
				tempComp.setProperty("oc9_Check", vp.report.stampData.check);
				tempComp.setProperty("oc9_TCheck", vp.report.stampData.techCheck);
				tempComp.setProperty("oc9_NCheck", vp.report.stampData.normCheck);
				tempComp.setProperty("oc9_Approver", vp.report.stampData.approve);
				
				tempComp.setProperty("oc9_DesignDate", vp.report.stampData.designDate);
				tempComp.setProperty("oc9_CheckDate", vp.report.stampData.checkDate);
				tempComp.setProperty("oc9_TCheckDate", vp.report.stampData.techCheckDate);
				tempComp.setProperty("oc9_NCheckDate", vp.report.stampData.normCheckDate);
				tempComp.setProperty("oc9_ApproveDate", vp.report.stampData.approveDate);
			}
			/*if(VSP.vspIR.getRelatedComponent("IMAN_master_form_rev")!=null){
				specIR.getRelatedComponent("IMAN_master_form_rev").setProperty("object_desc", Specification.settings.getStringProperty("blockSettings"));
			}*/
			
			VP.vpIR.lock();
			//topBOMLine.getItemRevision().setProperty("oc9_AddNote", Specification.settings.getStringProperty("AddedText"));
			VP.vpIR.setProperty("oc9_Litera1", vp.report.stampData.litera1);
			VP.vpIR.setProperty("oc9_Litera2", vp.report.stampData.litera2);
			VP.vpIR.setProperty("oc9_Litera3", vp.report.stampData.litera3);
			VP.vpIR.save();
			VP.vpIR.unlock();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private TCComponentItemRevision createNextRevisionBasedOn(TCComponentItemRevision itemRev) {
		TCComponentItemRevision out = null;
		
		ReviseProperties revProp = new ReviseProperties();
		ReviseInfo revInfo = new ReviseInfo();
		revInfo.baseItemRevision = itemRev;
		ReviseResponse2 response = dmService.revise2(new ReviseInfo[] {revInfo});
		
		System.out.println("MAP SIZE = " + response.reviseOutputMap.size());
		Iterator it = response.reviseOutputMap.entrySet().iterator();
		if (it.hasNext()) {
			System.out.println("trying to return itemRev...");
			Map.Entry entry = (Entry) it.next();
			System.out.println("Class NAME VALUE: " + entry.getValue().getClass().getName() + " = " + entry.getKey()
					+ "\nClass NAME KEY: " + entry.getKey().getClass().getName()
					);
			out = ((ReviseOutput)entry.getValue()).newItemRev;
		}
		
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private CreateItemsOutput[] createItems(final ItemIdsAndInitialRevisionIds[] itemIds, final String itemType, final String itemName, final String itemDesc)
			throws TCException {
//		final GetItemCreationRelatedInfoResponse relatedResponse = BuildSpec2G.dmService.getItemCreationRelatedInfo(itemType, null);
		final ItemProperties[] itemProps = new ItemProperties[itemIds.length];
		for (int i = 0; i < itemIds.length; i++) {
			final ItemProperties itemProperty = new ItemProperties();
			itemProperty.clientId = VP.CLIENT_ID;
			itemProperty.itemId = itemIds[i].newItemId;
			itemProperty.revId = itemIds[i].newRevId;
			itemProperty.name = itemName;
			itemProperty.type = itemType;
			itemProperty.description = itemDesc;
			itemProperty.uom = "";
			itemProps[i] = itemProperty;
		}

		final CreateItemsResponse response = dmService.createItems(
				itemProps, null, null);
		return response.output;
	}
	
	public TCComponent[] createItem(final String type, final String id,
			final String name, final String desc) throws TCException {
		
		final ItemIdsAndInitialRevisionIds[] itemIds = generateItemIds(1, type);
		final CreateItemsOutput[] newItems = createItems(itemIds, type, name, desc);
		
		newItems[0].item.setProperty("item_id", id);

		return new TCComponent[] { newItems[0].item, newItems[0].itemRev };
	}
	
	private boolean isKdLastRevHasAssemblyRev(TCComponentItem kdDoc) throws TCException {
		boolean out = false;
		TCComponentItemRevision lastRev = getLastRevOfItem(kdDoc);
		if (lastRev != null) {
			AIFComponentContext[] relatedComp = lastRev.getRelated("TC_DrawingOf");
			System.out.println("got " + relatedComp.length + " Specs from LAST REVISIONS");
			
			for (AIFComponentContext currConetext : relatedComp) {
				System.out.println("TYPE: " + currConetext.getComponent().getType());
				if (currConetext.getComponent().getType().equals("Oc9_CompanyPartRevision")) {
					TCComponentItemRevision currItemRev = (TCComponentItemRevision) currConetext.getComponent(); 
					if (currItemRev.getProperty("item_id").equals(lastRev.getProperty("item_id")))
						out = true;
				}
			}
		}
		System.out.println("IS KD LAST REV HAS ASSEMBLY? >> " + out);
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private ItemIdsAndInitialRevisionIds[] generateItemIds(final int numberOfIds, final String type) throws TCException {
		final GenerateItemIdsAndInitialRevisionIdsProperties property = new GenerateItemIdsAndInitialRevisionIdsProperties();
		property.count = numberOfIds;
		property.itemType = type;
		property.item = null; // Not used
		final GenerateItemIdsAndInitialRevisionIdsResponse response = dmService
				.generateItemIdsAndInitialRevisionIds(new GenerateItemIdsAndInitialRevisionIdsProperties[] { property });
		final BigInteger bIkey = new BigInteger("0");
		final Map<BigInteger, ItemIdsAndInitialRevisionIds[]> allNewIds = response.outputItemIdsAndInitialRevisionIds;
		final ItemIdsAndInitialRevisionIds[] myNewIds = allNewIds.get(bIkey);
		return myNewIds;
	}
	
	private TCComponentDataset createDatasetAndAddFile(String file_path)
			throws TCException {
		TCComponentDataset ret = null;
		String dataset_tool = null;
		String dataset_type = null;
		dataset_tool = "PDF_Reference";
		dataset_type = "PDF";
		TCComponentDatasetType dst = (TCComponentDatasetType) VP.topBOMLineIR.getSession().getTypeComponent("Dataset");
		ret = dst.create(gen_dataset_name(), "Ведомость покупных", dataset_type);
		ret.setFiles(new String[] { renamedReportFile!=null?renamedReportFile.getAbsolutePath():vp.report.report.getAbsolutePath() }, new String[] { dataset_tool });
		ret.lock();
		ret.save();
		ret.unlock();

		return ret;
	}

	private String gen_dataset_name() throws TCException {
		String ret = null;
		if (VP.topBOMLineIR != null)
			ret = "Ведомость покупных - "
					+ VP.topBOMLineIR.getTCProperty("object_name").getStringValue();
		return ret;
	}
	
	private TCComponentDataset deletePrevSpecDatasetOfKd() throws Exception
	{
		TCComponentDataset dataset = null;
		for (AIFComponentContext compContext : VP.vpIR.getChildren())
		{
			System.out.println(">>> TYPE: " + compContext.getComponent().getProperty("object_type"));
			if ((compContext.getComponent() instanceof TCComponentDataset) 
					&& compContext.getComponent().getProperty("object_desc").equals("Ведомость покупных")) {
				dataset = (TCComponentDataset)compContext.getComponent();
				System.out.println("Deleting Spec Dataset Named Ref in KD");
				dataset.removeFiles("ImanFile");
				System.out.println("after destroying");
			}
		}
		
		return dataset;
	}
	
	private TCComponentItemRevision getLastRevOfItem(TCComponentItem item) throws TCException {
		TCComponentItemRevision out = null;
		Map<Integer, TCComponentItemRevision> mapItemRevByRev = new HashMap<Integer, TCComponentItemRevision>();
		ArrayList<Integer> revisions = new ArrayList<Integer>();
		AIFComponentContext[] contextArray = item.getChildren();
		System.out.println("Children of ITEM: " + contextArray.length);
		for (int i=0; i<contextArray.length; i++) {
			System.out.println("~~~~ TYPE: " + contextArray[i].getComponent().getType());
			if (contextArray[i].getComponent().getType().equals("Oc9_KDRevision")) {
				TCComponentItemRevision currItemRev = (TCComponentItemRevision)contextArray[i].getComponent();
				if(currItemRev.getProperty("item_id").equals(item.getProperty("item_id"))) {
					System.out.println("ADDING TO MAP!");
					Integer rev = Integer.valueOf(currItemRev.getProperty("current_revision_id"));
					mapItemRevByRev.put(rev, currItemRev);
					revisions.add(rev);
				}
			}
		}
		Collections.sort(revisions);
		if (revisions.size() > 0) 
			out = mapItemRevByRev.get(revisions.get(revisions.size()-1)); 
		
		System.out.println("returning: " + out.getProperty("item_id"));
		return out;
	}
	
	private static void deleteRelationsToCompanyPart(TCComponentItemRevision rev) throws Exception {
		ArrayList<TCComponentItemRevision> list4Removing = new ArrayList<TCComponentItemRevision>();
		AIFComponentContext[] itemRev4Delete = rev.getItem().getRelated("Oc9_DocRel");
		for (AIFComponentContext currContext : itemRev4Delete) {
			if (((TCComponentItemRevision)currContext.getComponent()).getProperty("item_id")
					.equals(rev.getItem().getProperty("item_id") + " ВП")) {
				System.out.println("~~~ Added to delete");
				list4Removing.add((TCComponentItemRevision)currContext.getComponent());
			}
		}
		rev.remove("Oc9_DocRel", list4Removing);
	}
	
	private TCComponentItem findKDDocItem() throws TCException {
		TCComponentItem result = null;
		TCComponentItemType itemType = (TCComponentItemType) VPHandler.session.getTypeComponent("Oc9_KD");
		String criteria = VP.topBOMLineIR.getProperty("item_id") + " ВП";
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
		
		return result;
	}
}
