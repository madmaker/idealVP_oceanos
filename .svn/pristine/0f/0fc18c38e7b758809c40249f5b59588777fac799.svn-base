package ru.idealplm.pm.vp2g;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponentBOMLine;

public class VPDataOcc {
//	int key;
	
//	public TCComponentBOMLine originalBOMLine;

//	String uid = "";
	String position = "";
	String name = "";
	String idDocForDelivery = "";
//	String supplier = " ";
	String upperItem = "";
	String allWhereUsedInOneLine = "";
	String allQtyInOneLine = "";
	String id = "";
	boolean isVpSection = false;
//	String codeOKP = "";
	Double qty = 0.0;
	String demension = "";
//	String qtyPerAssembly = "";
//	String qtyForKits = "";
//	String qtyForRegulation = "";
//	String qtyTotal = "";
	String remark = "";
	HashMap<String,Double> mapWhereUsed;
	ArrayList<String> arrayListWhereUsed;
	
	
	public static VPDataOcc copyVpOcc(VPDataOcc inVpDataLine) {
		VPDataOcc out = new VPDataOcc();
//		out.uid = inVpDataLine.uid;
		out.position = inVpDataLine.position;
		out.name = inVpDataLine.name;
		out.idDocForDelivery = inVpDataLine.idDocForDelivery;
		out.upperItem = inVpDataLine.upperItem;
//		out.supplier = inVpDataLine.supplier;
//		out.whereUsed = inVpDataLine.whereUsed;
		out.qty = inVpDataLine.qty;
		out.demension = inVpDataLine.demension;
		out.remark = inVpDataLine.remark;
		out.mapWhereUsed = new HashMap<String, Double>();
		out.isVpSection = inVpDataLine.isVpSection;
		
		return out;
	}
	
	public void makeOneStringFromWhereUsedMapAndQty() {
		for (String currWhereUsed : arrayListWhereUsed) {
			allWhereUsedInOneLine += (currWhereUsed + "\n");
			allQtyInOneLine += (mapWhereUsed.get(currWhereUsed) + "\n");
//			System.out.println("where used: " + currWhereUsed + "\nqty: " + mapWhereUsed.get(currWhereUsed));
		}
		
//		if (allWhereUsedInOneLine.charAt(allWhereUsedInOneLine.length()-1) == '\n') {
			allWhereUsedInOneLine = allWhereUsedInOneLine.substring(0, allWhereUsedInOneLine.length()-1);
			allQtyInOneLine = allQtyInOneLine.substring(0, allQtyInOneLine.length()-1);
//		}
		
//		String temp = allWhereUsedInOneLine;
//		allWhereUsedInOneLine = allWhereUsedInOneLine.trim();
//		allQtyInOneLine = allQtyInOneLine.trim();
		
//		System.out.println("\nTRIMMM\nBEFORE:\n>" + temp + "<\nAFTER:\n>" + allWhereUsedInOneLine + "<");

		
		
	}
	
}
