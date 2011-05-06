package org.bbop.termgenie.client;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

public class UserPanel extends FlowPanel {

	private final TextBox usernameTextBox;
	private final PasswordTextBox passwordTextBox;
	private final CheckBox commitcheckbox;
	private final Button globalSubmitButton;

	public UserPanel() {
		super();
		setSize("100%", "100px");

		// create internal widgets
		final Label lblUsername = new Label("Username");
		usernameTextBox = new TextBox();
		final Label lblPassword = new Label("Password");
		passwordTextBox = new PasswordTextBox();

		commitcheckbox = new CheckBox("commit");
		globalSubmitButton = new Button("GlobalSubmitButton");

		// config internal widgets
		usernameTextBox.setMaxLength(255);
		passwordTextBox.setMaxLength(255);

		globalSubmitButton.setText("Submit Request");
		globalSubmitButton.setSize("160px", "40px");
		
		commitcheckbox.setValue(false);
		usernameTextBox.setEnabled(false);
		passwordTextBox.setEnabled(false);
		
		commitcheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				usernameTextBox.setEnabled(event.getValue());
				passwordTextBox.setEnabled(event.getValue());
			}
		});
		
		// layout internal widgets
		// username, password
		final Grid userCredentialGrid = new Grid();
		userCredentialGrid.resize(2, 2);
		userCredentialGrid.setCellSpacing(3);
		userCredentialGrid.setCellPadding(3);
		userCredentialGrid.setWidget(0, 0, lblUsername);
		userCredentialGrid.setWidget(0, 1, usernameTextBox);
		userCredentialGrid.getCellFormatter().setHorizontalAlignment(0, 1,
				HasHorizontalAlignment.ALIGN_RIGHT);
		userCredentialGrid.setWidget(1, 0, lblPassword);
		userCredentialGrid.setWidget(1, 1, passwordTextBox);

		// commit button and checkbox
		final HorizontalPanel commitPanel = new HorizontalPanel();
		
		commitPanel.setSpacing(10);
		
		commitPanel.add(globalSubmitButton);
		commitPanel.setCellVerticalAlignment(globalSubmitButton, HasVerticalAlignment.ALIGN_MIDDLE);
		commitPanel.setCellHorizontalAlignment(globalSubmitButton,
				HasHorizontalAlignment.ALIGN_CENTER);

		commitPanel.add(commitcheckbox);
		commitPanel.setCellVerticalAlignment(commitcheckbox, HasVerticalAlignment.ALIGN_MIDDLE);
		commitPanel.setCellHorizontalAlignment(commitcheckbox, HasHorizontalAlignment.ALIGN_CENTER);
		
		add(commitPanel);
		add(userCredentialGrid);
	}
	
	public String getUserName() {
		return usernameTextBox.getText();
	}
	
	public String getPassword() {
		return passwordTextBox.getText();
	}
	
	public boolean doCommit() {
		return commitcheckbox.getValue();
	}
	
	public void addGobalSubmitButtonHandler(ClickHandler handler) {
		globalSubmitButton.addClickHandler(handler);
	}
}
