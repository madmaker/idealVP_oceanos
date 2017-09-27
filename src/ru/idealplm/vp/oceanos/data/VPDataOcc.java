package ru.idealplm.vp.oceanos.data;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponentBOMLine;

public class VPDataOcc
{
	public String position = "";
	public String name = "";
	public String idDocForDelivery = "";
	public String upperItem = "";
	public String allWhereUsedInOneLineAssy = "";
	public String allWhereUsedInOneLineKit = "";
	public String allQtyInOneLineAssy = "";
	public String allQtyInOneLineKit = "";
	public String id = "";
	public boolean isVpSection = false;
	public Double qtyAssy = 0.0;
	public Double qtyKit = 0.0;
	public Double reserveFactor = 0.1;
	public String demension = "";
	public String remark = "";
	public HashMap<String,Double> mapWhereUsedAssy;
	public HashMap<String,Double> mapWhereUsedKit;
	public ArrayList<String> arrayListWhereUsedAssy;
	public ArrayList<String> arrayListWhereUsedKit;
	
	public static VPDataOcc copyVpOcc(VPDataOcc inVpDataLine)
	{
		VPDataOcc out = new VPDataOcc();
		out.position = inVpDataLine.position;
		out.name = inVpDataLine.name;
		out.idDocForDelivery = inVpDataLine.idDocForDelivery;
		out.upperItem = inVpDataLine.upperItem;
		out.qtyAssy = inVpDataLine.qtyAssy;
		out.qtyKit = inVpDataLine.qtyKit;
		out.reserveFactor = inVpDataLine.reserveFactor;
		out.demension = inVpDataLine.demension;
		out.remark = inVpDataLine.remark;
		out.mapWhereUsedAssy = new HashMap<String, Double>();
		out.mapWhereUsedKit = new HashMap<String, Double>();
		out.isVpSection = inVpDataLine.isVpSection;
		return out;
	}
	
	public void makeOneStringFromWhereUsedMapAndQty()
	{
		for (String currWhereUsedAssy : arrayListWhereUsedAssy)
		{
			allWhereUsedInOneLineAssy += (currWhereUsedAssy + "\n");
			allQtyInOneLineAssy += (mapWhereUsedAssy.get(currWhereUsedAssy) + "\n");
		}
		for (String currWhereUsedKit : arrayListWhereUsedKit)
		{
			allWhereUsedInOneLineKit += (currWhereUsedKit + "\n");
			allQtyInOneLineKit += (mapWhereUsedKit.get(currWhereUsedKit) + "\n");
		}
		
		allWhereUsedInOneLineAssy = allWhereUsedInOneLineAssy.substring(0, allWhereUsedInOneLineAssy.length()-1);
		allQtyInOneLineAssy = allQtyInOneLineAssy.substring(0, allQtyInOneLineAssy.length()-1);
		allWhereUsedInOneLineKit = allWhereUsedInOneLineKit.substring(0, allWhereUsedInOneLineKit.length()-1);
		allQtyInOneLineKit = allQtyInOneLineKit.substring(0, allQtyInOneLineKit.length()-1);
	}
}
