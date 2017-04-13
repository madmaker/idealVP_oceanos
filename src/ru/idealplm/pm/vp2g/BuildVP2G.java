package ru.idealplm.pm.vp2g;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;

import ru.idealplm.pm.vp2g.ErrorListDialog;
import ru.idealplm.pm.vp2g.InformMessageBox;
//import ru.idealplm.pm.vp2g.ReportsProperties;
import ru.idealplm.pm.vp2g.VPDialog;
import ru.idealplm.pm.vp2g.VPValidator;

import com.teamcenter.rac.kernel.TCComponentBOMEdit;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.services.IPSEService;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.services.rac.core.DataManagementService;

public class BuildVP2G extends AbstractHandler {
	
//+++++++++++++++++++ DEBUG MODE +++++++++++++++++++++++++	
	static boolean debugMode = true;
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	
	static TCComponentBOMLine topBomLine;
	static TCComponentItemRevision vpRev;
	
	public static TCSession session;
	public static DataManagementService dmService;
	
	static Map<Integer, VPBlock> mapSection = null;

	public BuildVP2G() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		topBomLine = null;
		vpRev = null;
		
		EngineVP2G.mapGlobalBoughts = null;
		EngineVP2G.mapItemsCached = null;
		EngineVP2G.multiWhereUsed = false;
		EngineVP2G.mapUidToMeasurement = null;
		EngineVP2G.arrListErrorElements = new ArrayList<String>();
		session = getPSEService().getTopBOMLine().getSession();
		dmService = DataManagementService.getService(session);
		
//		EngineVP2G.globalKey = 0;
		
		topBomLine = getPSEService().getTopBOMLine();

		
		if((topBomLine) != null) {
//			ReportsProperties.getReportsProperties("/ru/idealplm/pm/vp2G/sp2G.prop");
			VPValidator vpValidator = new VPValidator(topBomLine);
			ArrayList<String> result = vpValidator.validate();
			if(result.size()>0){
				ErrorListDialog eld = new ErrorListDialog(new Shell(Display.getCurrent()), result);
			} else {
				mapSection = new HashMap<Integer, VPBlock>();
				try {
					EngineVP2G.initialize();
					VPDialog dialog = new VPDialog(HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell(), 1);
					dialog.open();			
				} catch (TCException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			new InformMessageBox("Выберите объект, для которого необходимо построить ВП!", SWT.ICON_WARNING);
		}
		
		
//		if (topBomLine != null) {
//			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
//			try {
//				vpRev = EngineVP2G.getVpRev(topBomLine);
//				if ((vpRev != null) && EngineVP2G.isComponentHasReleasedStatus(vpRev)) 
//					new InformMessageBox("У данной ревизии уже есть утверждённая ВП", SWT.ICON_ERROR);
//				else {
//					if(topBomLine.getItemRevision().getProperty("pm8_Designation").trim().equals("")){
//						new InformMessageBox("У изделия, для которого строится ведомость ВП, не задано обозначение.", SWT.ICON_ERROR);
//						return null;
//					}
//					mapSection = new HashMap<Integer, VPBlock>();
//					EngineVP2G.initialize();
//					VPDialog dialog = new VPDialog(HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell(), 1);
//					dialog.open();
//				}
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}
//		}
		return null;	
	}
	
	public static IPSEService getPSEService() {
		@SuppressWarnings("restriction")
		IPSEService service = (IPSEService) OSGIUtil.getService(InternalPlatform.getDefault().getBundleContext(),
				IPSEService.class.getName());
		return service;
	}
	
}
