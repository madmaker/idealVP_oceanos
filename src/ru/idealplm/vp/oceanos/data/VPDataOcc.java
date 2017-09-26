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
	public String allWhereUsedInOneLine = "";
	public String allQtyInOneLine = "";
	public String id = "";
	public boolean isVpSection = false;
	public Double qty = 0.0;
	public String demension = "";
	public String remark = "";
	public HashMap<String,Double> mapWhereUsed;
	public ArrayList<String> arrayListWhereUsed;
	
	public static VPDataOcc copyVpOcc(VPDataOcc inVpDataLine)
	{
		VPDataOcc out = new VPDataOcc();
		out.position = inVpDataLine.position;
		out.name = inVpDataLine.name;
		out.idDocForDelivery = inVpDataLine.idDocForDelivery;
		out.upperItem = inVpDataLine.upperItem;
		out.qty = inVpDataLine.qty;
		out.demension = inVpDataLine.demension;
		out.remark = inVpDataLine.remark;
		out.mapWhereUsed = new HashMap<String, Double>();
		out.isVpSection = inVpDataLine.isVpSection;
		
		return out;
	}
	
	public void makeOneStringFromWhereUsedMapAndQty()
	{
		for (String currWhereUsed : arrayListWhereUsed)
		{
			allWhereUsedInOneLine += (currWhereUsed + "\n");
			allQtyInOneLine += (mapWhereUsed.get(currWhereUsed) + "\n");
		}
		
		allWhereUsedInOneLine = allWhereUsedInOneLine.substring(0, allWhereUsedInOneLine.length()-1);
		allQtyInOneLine = allQtyInOneLine.substring(0, allQtyInOneLine.length()-1);
	}
}
