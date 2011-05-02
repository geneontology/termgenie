package org.bbop.termgenie.client;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

public class InternalErrorPanel extends FlowPanel {

	public InternalErrorPanel(Throwable caught) {
		super();
		Label label = new Label("An internal error occured.");
		add(label);
		DisclosurePanel panel = new DisclosurePanel("See Error Details");
		TextArea textArea = new TextArea();
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		caught.printStackTrace(printWriter);
		printWriter.close();
		textArea.setText(stringWriter.getBuffer().toString());
		textArea.setReadOnly(true);
		panel.add(textArea);
	}
}
