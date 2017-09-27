package ru.idealplm.vp.oceanos.data;

import java.util.ArrayList;
import java.util.Iterator;

public class VPBlock extends ArrayList<VPDataOcc>
{
	private static final long serialVersionUID = 1L;
	
	int id = 0;
	public int sparePosition = 0;
	public int spareLines = 0;
	public boolean isNumbering = true;
	public String title = "";
	
	public VPBlock(int idSection) {
		setID(idSection);
	}
	
	void setID(int idSection) {
		id = idSection;
	}
	
	int getID() {
		return (id);
	}
}