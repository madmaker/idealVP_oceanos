package ru.idealplm.pm.vp2g;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerException;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.xml.sax.SAXException;




import ru.idealplm.util.pbwnd.ProgressBarWindow;

import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class VPRunnable implements Runnable
{	
	static boolean isChangable = true;
	static boolean hasWrongZones;
	private static String warnMessage = "\n";
	static IWorkbenchWindow window = null;
	Display display;
	ProgressBarWindow spProgressBar;
	
	public VPRunnable(Display d){
		display = d;
	}

	public void setPB(ProgressBarWindow pb){
		spProgressBar = pb;
		display.syncExec(new Runnable(){
			@Override
			public void run(){
				spProgressBar.setMaxValue(5);
			}
		});
	}
	
	public void setProgressBarStage(final String text, final int value){
		display.syncExec(new Runnable(){
			@Override
			public void run(){
				spProgressBar.getShell().setActive();
				spProgressBar.setText(text);
				spProgressBar.setValue(value);
			}
		});
	}
	
	@Override
	public void run() {
		//final ProgressBarWindow spProgressBar = new ProgressBarWindow(Thread.currentThread());
		
		String outMessage = "";
		long startTime = System.currentTimeMillis();
		try {
			warnMessage = "";
			
			setProgressBarStage("Анализ структуры",1);

			EngineVP2G.getListOfBoughts(BuildVP2G.topBomLine, 1, 0);
			if (EngineVP2G.arrListErrorElements.size() == 0) {
				EngineVP2G.makeAndSortBlocks();
				int result = EngineVP2G.exportXML();
				System.out.println("XML exporting answer ->" + result);
				System.out.println("Export to XML!");
				setProgressBarStage("Добавление в Teamcenter", 5);
				EngineVP2G.addToTeamcenter();
				System.out.println("Added to Teamcenter!");
//				outMessage = "Ведомость покупных добавлена\n";
//				outMessage += warnMessage;
				long endTime = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				System.out.println("------------------------ Done("
						+ String.format("%.2f", (double) totalTime / 1000)
						+ "sec) ------------------------");
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("In EXCEPTION....");
//			new InformMessageBox(
//					"При экспортировании произошли непредвиденные ошибки.\nОбратитесь к разработчику", SWT.ICON_ERROR);
			e.printStackTrace();
		} finally {
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					spProgressBar.close();
				}
			});
		}
		

		//
		// outMessage += ("\nВремя генерации СП: " + String.format("%.2f",
		// (double)totalTime/1000) + " c");
		// int style = warnMessage.length() > 0 ? SWT.ICON_WARNING :
		// SWT.ICON_INFORMATION;
		// globalDone = true;

		// new SPMessageBox(outMessage, style);

		System.out.println("ERROR size: " + EngineVP2G.arrListErrorElements.size());
		
		String out = "\n";
		if (EngineVP2G.arrListErrorElements.size() > 0) {
			for (String currString: EngineVP2G.arrListErrorElements) {
				out += (currString + "\n");
			}
			new InformMessageBox("Формирование ВП невозможно так как для следующих компонентов указана различная размерность:" + out, SWT.ICON_ERROR);
		}
		
		
	}

}
