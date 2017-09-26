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
			 * .add("� �������, ��� �������� �������� ��, �� ������ �����������."
			 * ); }
			 */
			if (topBOMLine == null) {
				errorList.add("����������� ������ ��� ���������� ������������");
			}
			TCComponentItemRevision kdRev = ReportsItemUtils.getVpRev(topBOMLine);
			if ((kdRev != null) && ReportsItemUtils.isComponentHasReleasedStatus(kdRev))
			{
				errorList.add("� ������ ������� ��� ���� ����������� ��");
			}
			if ((kdRev != null) && ReportsItemUtils.isVpDatasetBlocked(kdRev))
			{
				errorList.add("������� ����� ������ ������������.\n�������� PDF ���� � ��������� �������.");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return errorList;
	}
}
