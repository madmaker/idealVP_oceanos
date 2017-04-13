package ru.idealplm.pm.vp2g;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

public class InformMessageBox {
	public InformMessageBox(final String message, final int style) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = new Shell(Display.getDefault(), SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);
				shell.setText(message);

			    MessageBox messageBox = new MessageBox(shell, style);
			    messageBox.setMessage(message);
			    shell.setActive();
			    int rc = messageBox.open();
			}
		});
	}
}