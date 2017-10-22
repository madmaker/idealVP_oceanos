package ru.idealplm.vp.oceanos.data;

import java.util.ArrayList;

import com.teamcenter.rac.kernel.TCComponentBOMLine;

import ru.idealplm.vp.oceanos.data.ReportLine.ReportLineType;

public class ReportLineOccurence
{
	public ReportLine reportLine;
	public TCComponentBOMLine bomLine;
	public int quantityAssy = 0;
	public int quantityKit = 0;
	public double adjustFactor = 0.0;
	public String remark = "";
	
	private ReportLineOccurence parent;
	private ArrayList<ReportLineOccurence> children;
	
	public ReportLineOccurence(ReportLine reportLine, ReportLineOccurence parentOccurence)
	{
		this.reportLine = reportLine;
		this.parent = parentOccurence;
		this.children = new ArrayList<ReportLineOccurence>(1);
	}
	
	public void addChild(ReportLineOccurence child)
	{
		children.add(child);
	}
	
	public int getChildrenCount()
	{
		return children.size();
	}
	
	public ReportLineOccurence getChild(int index)
	{
		return children.get(index);
	}
	
	public ArrayList<ReportLineOccurence> getChildren()
	{
		return children;
	}
	
	public void setQuantity(int quantity)
	{
		if(parent.reportLine.type == ReportLineType.ASSEMBLY)
		{
			setQuantityAssy(quantity);
		} else if (parent.reportLine.type == ReportLineType.KIT)
		{
			setQuantityKit(quantity);
		} else if(parent.reportLine.type == ReportLineType.NONE)
		{
			this.quantityAssy = 1;
		}
	}
	
	public void setQuantityAssy(int quantity)
	{
		this.quantityAssy = parent.getTotalQuantity() * quantity;
	}
	
	public void setQuantityKit(int quantity)
	{
		this.quantityKit = parent.getTotalQuantity() * quantity;
	}
	
	public double getTotalQuantityWithReserve()
	{
		return quantityAssy + Math.ceil(quantityAssy*adjustFactor) + quantityKit;
	}
	
	private int getTotalQuantity()
	{
		return quantityAssy + quantityKit;
	}
	
	public String getParentItemId()
	{
		if(parent!=null && parent.reportLine!=null)
			return parent.reportLine.id;
		return "";
	}
	
	public String getParentItemUID()
	{
		if(parent!=null && parent.reportLine!=null)
			return parent.reportLine.uid;
		return "";
	}
	
	public ReportLineType getParentType()
	{
		if(parent!=null && parent.reportLine!=null)
			return parent.reportLine.type;
		return ReportLineType.NONE;
	}
	
	@Override
	public boolean equals(Object v)
	{
		boolean retVal = false;

	    if (v instanceof ReportLineOccurence){
	    	ReportLineOccurence ptr = (ReportLineOccurence) v;
	        retVal = ptr.reportLine.uid.equals(this.reportLine.uid);
	    }
	    
	    return retVal;
	}
	
	@Override
    public int hashCode()
	{
        int hash = 7;
        hash = 17 * hash + (this.reportLine.uid != null ? this.reportLine.uid.hashCode() : 0);
        return hash;
    }
}
