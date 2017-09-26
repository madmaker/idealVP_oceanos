package ru.idealplm.vp.oceanos.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.plugin.Activator;
import com.teamcenter.services.rac.core.DataManagementService;

import ru.idealplm.pm.vp2g.ErrorListDialog;
import ru.idealplm.vp.oceanos.core.VP;
import ru.idealplm.vp.oceanos.core.VPSettings;
import ru.idealplm.vp.oceanos.core.VPValidator;
import ru.idealplm.vp.oceanos.data.VPBlock;
import ru.idealplm.vp.oceanos.gui.InformMessageBox;
import ru.idealplm.vp.oceanos.gui.VPDialog;

public class VPHandler extends AbstractHandler
{	
	public static TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	VP vp;

	public VPHandler()
	{
	}
	
	@SuppressWarnings("restriction")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ProgressMonitorDialog pd = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event).getShell());
		
		vp = new VP();
		vp.progressMonitor = pd;
		vp.init();
		vp.readExistingData();

		VPDialog mainDialog = new VPDialog(HandlerUtil.getActiveShell(event).getShell(), SWT.CLOSE, vp);
		mainDialog.open();
		
		if (!VPSettings.isOKPressed) { return null; }
		
		vp.readData();
		vp.prepareData();
		vp.buildReportFile();
		vp.uploadReportFile();
		
		return null;
	}
}
