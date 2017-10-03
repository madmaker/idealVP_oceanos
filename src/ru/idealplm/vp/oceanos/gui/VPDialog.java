package ru.idealplm.vp.oceanos.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.util.DateButton;

import ru.idealplm.vp.oceanos.core.Report.ReportType;
import ru.idealplm.vp.oceanos.core.VP;
import ru.idealplm.vp.oceanos.core.VPSettings;
import ru.idealplm.vp.oceanos.util.DateUtil;

public class VPDialog extends Dialog
{
	protected Object result;
	protected Shell shell;
	private TabFolder tabFolder;
	private TabItem tabMain;
	private TabItem tabSignatures;
	private Composite compositeMain;
	private Composite compositeSignatures;
	
	private Text text_PrimaryApp;
	private Text text_Litera1;
	private Text text_Litera2;
	private Text text_Litera3;
	
	private Text textDesigner;
	private Text textCheck;
	private Text textTCheck;
	private Text textNCheck;
	private Text textApprover;
	
	private DateButton dateDesigner;
	private DateButton dateCheck;
	private DateButton dateTCheck;
	private DateButton dateNCheck;
	private DateButton dateApprover;
	private VP vp;
	
	public VPDialog(Shell parent, int style, VP vsp) {
		super(parent, style);
		this.vp = vsp;
	}
	
	public Object open()
	{
		createContents();
		fillContents();
		
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		return result;
	}
	
	private void createContents()
	{
		shell = new Shell();
		shell.setMinimumSize(new Point(50, 27));
		shell.setSize(424, 293);
		shell.setText("\u0412\u0435\u0434\u043E\u043C\u043E\u0441\u0442\u044C \u043F\u043E\u043A\u0443\u043F\u043D\u044B\u0445");
		shell.setLayout(null);
		tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setBounds(0, 0, 414, 215);
		tabMain = new TabItem(tabFolder, SWT.BORDER);
		tabSignatures = new TabItem(tabFolder, SWT.BORDER);
		compositeMain = new Composite(tabFolder, SWT.NONE);
		compositeSignatures = new Composite(tabFolder, SWT.NONE);
		compositeMain.setLayout(null);
		compositeSignatures.setLayout(null);
		tabMain.setControl(compositeMain);
		tabSignatures.setControl(compositeSignatures);
		
		Label labelDesigner = new Label(compositeSignatures, SWT.NONE);
		labelDesigner.setText("\u0420\u0430\u0437\u0440\u0430\u0431\u043E\u0442\u0430\u043B");
		labelDesigner.setBounds(10, 10, 90, 23);
		
		textDesigner = new Text(compositeSignatures, SWT.BORDER);
		textDesigner.setBounds(106, 7, 110, 23);
		
		Label labelCheck = new Label(compositeSignatures, SWT.NONE);
		labelCheck.setBounds(10, 47, 90, 23);
		labelCheck.setText("\u041F\u0440\u043E\u0432\u0435\u0440\u0438\u043B");
		
		textCheck = new Text(compositeSignatures, SWT.BORDER);
		textCheck.setBounds(106, 44, 110, 23);
		
		/*Label labelAddCheck = new Label(compositeSignatures, SWT.NONE);
		labelAddCheck.setBounds(173, 20, 90, 23);
		labelAddCheck.setText("\u0424\u0430\u043C\u0438\u043B\u0438\u044F");*/
		
		textTCheck = new Text(compositeSignatures, SWT.BORDER);
		textTCheck.setBounds(106, 80, 110, 23);
		
		Label labelNCheck = new Label(compositeSignatures, SWT.NONE);
		labelNCheck.setBounds(10, 120, 90, 23);
		labelNCheck.setText("\u041D.\u043A\u043E\u043D\u0442\u0440\u043E\u043B\u044C");
		
		Label labelTCheck = new Label(compositeSignatures, SWT.NONE);
		labelTCheck.setBounds(10, 83, 90, 23);
		labelTCheck.setText("�.�����");
		
		Label labelApprover = new Label(compositeSignatures, SWT.NONE);
		labelApprover.setBounds(10, 160, 90, 23);
		labelApprover.setText("\u0423\u0442\u0432\u0435\u0440\u0434\u0438\u043B");
		
		textNCheck = new Text(compositeSignatures, SWT.BORDER);
		textNCheck.setBounds(106, 117, 110, 23);
		
		textApprover = new Text(compositeSignatures, SWT.BORDER);
		textApprover.setBounds(106, 157, 110, 23);
		
		//TODO okeanos
		Composite compositeDesigner = new Composite(compositeSignatures, SWT.EMBEDDED);
		compositeDesigner.setBounds(239, 7, 150, 23);
		java.awt.Frame frameDesigner = SWT_AWT.new_Frame(compositeDesigner);
		java.awt.Panel panelDesigner = new java.awt.Panel(new java.awt.BorderLayout());
	    frameDesigner.add(panelDesigner);
		dateDesigner = new DateButton();
		dateDesigner.setDoubleBuffered(true);
		panelDesigner.add(dateDesigner);
		
		Composite compositeCheck = new Composite(compositeSignatures, SWT.EMBEDDED);
		compositeCheck.setBounds(239, 44, 150, 23);
		java.awt.Frame frameCheck = SWT_AWT.new_Frame(compositeCheck);
	    java.awt.Panel panelCheck = new java.awt.Panel(new java.awt.BorderLayout());
	    frameCheck.add(panelCheck);
		dateCheck = new DateButton();
		dateCheck.setDoubleBuffered(true);
		panelCheck.add(dateCheck);
		
		Composite compositeTCheck = new Composite(compositeSignatures, SWT.EMBEDDED);
		compositeTCheck.setBounds(239, 80, 150, 23);
		java.awt.Frame frameTCheck = SWT_AWT.new_Frame(compositeTCheck);
	    java.awt.Panel panelTCheck = new java.awt.Panel(new java.awt.BorderLayout());
	    frameTCheck.add(panelTCheck);
		dateTCheck = new DateButton();
		dateTCheck.setDoubleBuffered(true);
		panelTCheck.add(dateTCheck);
		
		Composite compositeNCheck = new Composite(compositeSignatures, SWT.EMBEDDED);
		compositeNCheck.setBounds(239, 117, 150, 23);
		java.awt.Frame frameNCheck = SWT_AWT.new_Frame(compositeNCheck);
		java.awt.Panel panelNCheck = new java.awt.Panel(new java.awt.BorderLayout());
		frameNCheck.add(panelNCheck);
		dateNCheck = new DateButton();
		dateNCheck.setDoubleBuffered(true);
		panelNCheck.add(dateNCheck);
		
		Composite compositeApprover = new Composite(compositeSignatures, SWT.EMBEDDED);
		compositeApprover.setBounds(239, 157, 150, 23);
		java.awt.Frame frameApprover = SWT_AWT.new_Frame(compositeApprover);
	    java.awt.Panel panelApprover = new java.awt.Panel(new java.awt.BorderLayout());
	    frameApprover.add(panelApprover);
		dateApprover = new DateButton();
		dateApprover.setDoubleBuffered(true);
		panelApprover.add(dateApprover);
		
	    tabMain.setText("���������");
	    tabSignatures.setText("\u041F\u043E\u0434\u043F\u0438\u0441\u0430\u043D\u0442\u044B");

	    final Button button_ShowAdditionalForm = new Button(compositeMain, SWT.CHECK);
		button_ShowAdditionalForm.setBounds(10, 163, 225, 16);
		button_ShowAdditionalForm.setText("�������� �������������� �����");
		
		text_PrimaryApp = new Text(compositeMain, SWT.BORDER);
		text_PrimaryApp.setBounds(10, 29, 154, 19);
		
		Label label_Litera1 = new Label(compositeMain, SWT.NONE);
		label_Litera1.setText("\u041B\u0438\u0442\u0435\u0440\u0430 1");
		label_Litera1.setBounds(10, 60, 76, 13);
		
		text_Litera1 = new Text(compositeMain, SWT.BORDER);
		text_Litera1.setBounds(10, 79, 76, 19);
		
		Label label_Litera2 = new Label(compositeMain, SWT.NONE);
		label_Litera2.setText("\u041B\u0438\u0442\u0435\u0440\u0430 2");
		label_Litera2.setBounds(92, 60, 76, 13);
		
		text_Litera2 = new Text(compositeMain, SWT.BORDER);
		text_Litera2.setBounds(92, 79, 76, 19);
		
		Label label_Litera3 = new Label(compositeMain, SWT.NONE);
		label_Litera3.setText("\u041B\u0438\u0442\u0435\u0440\u0430 3");
		label_Litera3.setBounds(174, 60, 76, 13);
		
		text_Litera3 = new Text(compositeMain, SWT.BORDER);
		text_Litera3.setBounds(174, 79, 76, 19);
		
		Label label = new Label(compositeMain, SWT.NONE);
		label.setBounds(10, 10, 154, 13);
		label.setText("\u041F\u0435\u0440\u0432\u0438\u0447\u043D\u0430\u044F \u043F\u0440\u0438\u043C\u0435\u043D\u044F\u0435\u043C\u043E\u0441\u0442\u044C");
		
		Label label_1 = new Label(compositeMain, SWT.NONE);
		label_1.setBounds(10, 114, 76, 13);
		label_1.setText("\u0422\u0438\u043F \u043E\u0442\u0447\u0435\u0442\u0430");
		
		final Button btnPdf = new Button(compositeMain, SWT.RADIO);
		btnPdf.setSelection(true);
		btnPdf.setBounds(10, 133, 54, 16);
		btnPdf.setText("PDF");
		
		final Button btnExcel = new Button(compositeMain, SWT.RADIO);
		btnExcel.setBounds(81, 133, 83, 16);
		btnExcel.setText("EXCEL");
		
		btnPdf.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				button_ShowAdditionalForm.setEnabled(true);
			}
		});
		
		btnExcel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				button_ShowAdditionalForm.setSelection(false);
				button_ShowAdditionalForm.setEnabled(false);
			}
		});
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBounds(0, 221, 414, 44);
		
		Button btnOk = new Button(composite, SWT.NONE);
		btnOk.setLocation(131, 10);
		btnOk.setSize(70, 25);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				VPSettings.isOKPressed = true;
				VPSettings.doShowAdditionalForm = button_ShowAdditionalForm.getSelection();
				
				vp.report.stampData.litera1 = text_Litera1.getText();
				vp.report.stampData.litera2 = text_Litera2.getText();
				vp.report.stampData.litera3 = text_Litera3.getText();
				vp.report.stampData.pervPrim = text_PrimaryApp.getText();
				
				vp.report.stampData.design = textDesigner.getText();
				vp.report.stampData.check = textCheck.getText();
				vp.report.stampData.techCheck = textTCheck.getText();
				vp.report.stampData.normCheck = textNCheck.getText();
				vp.report.stampData.approve = textApprover.getText();
				
				vp.report.stampData.designDate = dateDesigner.getText().equals("���� �� �����������.")?"":fixData(dateDesigner.getText());
				vp.report.stampData.checkDate = dateCheck.getText().equals("���� �� �����������.")?"":fixData(dateCheck.getText());
				vp.report.stampData.techCheckDate = dateTCheck.getText().equals("���� �� �����������.")?"":fixData(dateTCheck.getText());
				vp.report.stampData.normCheckDate = dateNCheck.getText().equals("���� �� �����������.")?"":fixData(dateNCheck.getText());
				vp.report.stampData.approveDate = dateApprover.getText().equals("���� �� �����������.")?"":fixData(dateApprover.getText());
				
				if(btnPdf.getSelection())
				{
					vp.report.type = ReportType.PDF;
				} else 
				{
					vp.report.type = ReportType.XLS;
				}
				
				shell.dispose();
				System.out.println("OK!");
			}
		});
		btnOk.setText("OK");
		
		Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.setLocation(224, 10);
		btnCancel.setSize(70, 25);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		btnCancel.setText("Cancel");
	}
	
	private void fillContents()
	{
		text_Litera1.setText(vp.report.stampData.litera1);
		text_Litera2.setText(vp.report.stampData.litera2);
		text_Litera3.setText(vp.report.stampData.litera3);
		text_PrimaryApp.setText(vp.report.stampData.pervPrim);
		
		textDesigner.setText(vp.report.stampData.design);
		textCheck.setText(vp.report.stampData.check);
		textTCheck.setText(vp.report.stampData.techCheck);
		textNCheck.setText(vp.report.stampData.normCheck);
		textApprover.setText(vp.report.stampData.approve);

		//TODO okeanos
		String s_DesignDate = vp.report.stampData.designDate;
		String s_CheckDate = vp.report.stampData.checkDate;
		String s_TCheckDate = vp.report.stampData.techCheckDate;
		String s_NCheckDate = vp.report.stampData.normCheckDate;
		String s_ApproveDate = vp.report.stampData.approveDate;
		System.out.println("::DATE::"+s_DesignDate);
		if(!s_DesignDate.isEmpty()) { dateDesigner.setDate(DateUtil.getDateFormSimpleString(s_DesignDate)); }else{ dateDesigner.setDate(""); }
		if(!s_CheckDate.isEmpty()) { dateCheck.setDate(DateUtil.getDateFormSimpleString(s_CheckDate)); }else{ dateCheck.setDate(""); }
		if(!s_TCheckDate.isEmpty()) { dateTCheck.setDate(DateUtil.getDateFormSimpleString(s_TCheckDate)); }else{ dateTCheck.setDate(""); }
		if(!s_NCheckDate.isEmpty()) { dateNCheck.setDate(DateUtil.getDateFormSimpleString(s_NCheckDate)); }else{ dateNCheck.setDate(""); }
		if(!s_ApproveDate.isEmpty()) { dateApprover.setDate(DateUtil.getDateFormSimpleString(s_ApproveDate)); }else{ dateApprover.setDate(""); }
		
	}
	
	private String fixData(String input){
		System.out.println("DATE" + "{" + input + "}");
		String output = input;
		if(input.contains("-")){
			if(input.substring(0, input.indexOf("-")).length()<2){
				output = "0"+output;
			}
		}
		return output;
	}
}
