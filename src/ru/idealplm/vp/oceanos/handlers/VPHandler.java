package ru.idealplm.vp.oceanos.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;

import ru.idealplm.vp.oceanos.core.VP;
import ru.idealplm.vp.oceanos.core.VPSettings;
import ru.idealplm.vp.oceanos.gui.VPDialog;

public class VPHandler extends AbstractHandler
{	
	public static TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	VP vp;
	ProgressMonitorDialog pd;

	public VPHandler()
	{
	}
	
	@SuppressWarnings("restriction")
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		pd = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event).getShell());
		
		vp = new VP();
		vp.init();
		vp.readExistingData();

		VPDialog mainDialog = new VPDialog(HandlerUtil.getActiveShell(event).getShell(), SWT.CLOSE, vp);
		mainDialog.open();
		
		if (!VPSettings.isOKPressed) { return null; }
		
		try
		{
			pd.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					monitor.beginTask("Чтение данных", 100);
					monitor.worked(20);
					vp.readData();
					monitor.beginTask("Обработка данных", 100);
					monitor.worked(40);
					vp.prepareData();
					monitor.beginTask("Построение отчета", 100);
					monitor.worked(60);
					vp.buildReportFile();
					monitor.beginTask("Добавление в Teamcenter", 100);
					monitor.worked(80);
					vp.uploadReportFile();
					monitor.done();
				}
			});
		}
		catch (InvocationTargetException | InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (CancellationException ex)
		{
			VPSettings.isCancelled = true;
			System.out.println(ex.getMessage());
		}
		vp.openReportFile();
		
		return null;
	}
}
