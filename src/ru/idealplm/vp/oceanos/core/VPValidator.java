package ru.idealplm.vp.oceanos.core;

import java.util.ArrayList;

import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

import ru.idealplm.vp.oceanos.util.ReportsItemUtils;

public class VPValidator
{
	private ArrayList<String> errorList;
	private TCComponentBOMLine topBOMLine;
	private TCComponentItemRevision topIR;

	public VPValidator(TCComponentBOMLine topBomLine)
	{
		this.topBOMLine = topBomLine;
	}

	public ArrayList<String> validate()
	{
		errorList = new ArrayList<String>();
		try {
			topIR = topBOMLine.getItemRevision();
			/*
			 * if (topIR.getProperty("pm8_Designation").trim().equals("")) {
			 * errorList
			 * .add("У изделия, для которого строится ВП, не задано обозначение."
			 * ); }
			 */
			if (topBOMLine == null) {
				errorList.add("Отсутствует состав для построения спецификации");
			}
			TCComponentItemRevision kdRev = ReportsItemUtils.getVpRev(topBOMLine);
			if ((kdRev != null) && ReportsItemUtils.isComponentHasReleasedStatus(kdRev))
			{
				errorList.add("У данной ревизии уже есть утверждённая ВП");
			}
			if ((kdRev != null) && ReportsItemUtils.isVpDatasetBlocked(kdRev))
			{
				errorList.add("Текущий набор данных заблокирован.\nЗакройте PDF файл и повторите попытку.");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return errorList;
	}
}
