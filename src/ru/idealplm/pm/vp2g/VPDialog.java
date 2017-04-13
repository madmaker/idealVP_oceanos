package ru.idealplm.pm.vp2g;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ru.idealplm.pm.vp2g.VPRunnable;
import ru.idealplm.util.pbwnd.ProgressBarWindow;

import com.teamcenter.rac.kernel.TCException;

public class VPDialog extends Dialog {

	public boolean isOK = false;
	public static Display display;

	public boolean addSpareLine = false;
	
	protected Object result;
	protected static Shell shell;
	private Table table;
	private Text text;
	private Composite blockArea;


	public static Display getDisplay() {
		return display;
	}
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public VPDialog(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 * @throws TCException 
	 */
	public Object open() throws TCException {
		createContents();
		shell.open();
		shell.layout();
		display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 * @throws TCException 
	 */
	private void createContents() throws TCException {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM);
		shell.setSize(289, 342);
		shell.setText("Ведомость покупных изделий");
		
		
		/*final Combo combo = new Combo(shell, SWT.NONE | SWT.READ_ONLY);
		combo.setBounds(7, 260, 180, 21);
		String[] items = EngineVP2G.factoryArray;
		combo.setItems(items);
		combo.select(0);*/

		
		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.setBounds(76, 287, 68, 23);
		btnOk.setText("OK");
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				System.out.println("OK!");
				
				//EngineVP2G.factory = EngineVP2G.factoryArrayForStamp[combo.getSelectionIndex()];
				EngineVP2G.factory = "Океанос!";
				
				shell.close();
				
				VPRunnable runnable = new VPRunnable(display);
				Thread mainBuild = new Thread(runnable); 
				ProgressBarWindow spProgressBar = new ProgressBarWindow(mainBuild, "Построение ВП...");
				spProgressBar.placeAtCenter();
				spProgressBar.getShell().setActive();
				runnable.setPB(spProgressBar);
				mainBuild.start();
			}
		});
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.setBounds(150, 287, 68, 23);
		btnCancel.setText("Cancel");
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("----------------- Cancel! -----------------");
				shell.close();
			}
		});
		
		Label label = new Label(shell, SWT.NONE);
		label.setBounds(7, 241, 83, 13);
		label.setText("\u041E\u0440\u0433\u0430\u043D\u0438\u0437\u0430\u0446\u0438\u044F");
	}
}
