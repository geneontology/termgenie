package org.bbop.termgenie.client.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.shared.GWTTermTemplate.GWTCardinality;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public interface DataInputField
{
	public enum Kind {
		Simple,
		List,
		Prefix
	}
	
	public Collection<Kind> getKind();
	
	public String getSimpleValue();
	
	public List<String> getListSimpleValue();
	
	public List<String> getPrefixValues();
	
	public static class TextFieldInput extends TextBox implements DataInputField {

		@Override
		public Collection<Kind> getKind() {
			return Collections.singleton(Kind.Simple);
		}

		@Override
		public String getSimpleValue() {
			return getText();
		}

		@Override
		public List<String> getListSimpleValue() {
			return null;
		}

		@Override
		public List<String> getPrefixValues() {
			return null;
		}
	}
	
	public static class AutoCompleteInputField extends SuggestBox implements DataInputField {
		
		public AutoCompleteInputField(SuggestOracle oracle) {
			super(oracle);
		}

		@Override
		public Collection<Kind> getKind() {
			return Collections.singleton(Kind.Simple);
		}

		@Override
		public String getSimpleValue() {
			return getText();
		}

		@Override
		public List<String> getListSimpleValue() {
			return null;
		}

		@Override
		public List<String> getPrefixValues() {
			return null;
		}
	}
	
	public static class PrefixAutoCompleteInputField extends VerticalPanel implements DataInputField {

		private final AutoCompleteInputField field;
		private final List<CheckBox> prefixes;
		
		public PrefixAutoCompleteInputField(SuggestOracle oracle, String[] functionalPrefixes) {
			super();
			setSize("100%", "100%");
			
			field = new AutoCompleteInputField(oracle);
			add(field);
			this.prefixes = new ArrayList<CheckBox>(functionalPrefixes.length);
			for (String prefix : functionalPrefixes) {
				CheckBox checkBox = new CheckBox(prefix);
				this.prefixes.add(checkBox);
				add(checkBox);
			}
		}
		
		@Override
		public Collection<Kind> getKind() {
			return Arrays.asList(Kind.Simple, Kind.Prefix);
		}

		@Override
		public String getSimpleValue() {
			return field.getSimpleValue();
		}

		@Override
		public List<String> getListSimpleValue() {
			return null;
		}

		@Override
		public List<String> getPrefixValues() {
			List<String> selectedPrefixes = new ArrayList<String>(prefixes.size());
			for (CheckBox box : prefixes) {
				if (box.getValue()) {
					selectedPrefixes.add(box.getText());
				}
			}
			return selectedPrefixes;
		}
	}
	
	public static class ListAutoCompleteInputField extends VerticalPanel implements DataInputField {

		private final List<RemovableWidget<AutoCompleteInputField>> fields;
		private final Button moreButton;
		
		public ListAutoCompleteInputField(final SuggestOracle oracle, final GWTCardinality cardinality) {
			super();
			this.fields = new ArrayList<RemovableWidget<AutoCompleteInputField>>();
			this.moreButton = new Button("add");
			moreButton.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					synchronized (fields) {
						if (fields.size() < cardinality.getMax()) {
							RemovableWidget<AutoCompleteInputField> widget = createWidget(oracle, cardinality);
							ListAutoCompleteInputField.this.remove(widget);
							ListAutoCompleteInputField.this.add(widget);
							if (fields.size() < cardinality.getMax()) {
								ListAutoCompleteInputField.this.add(moreButton);
							}
						}
					}
				}
			});
			
			int startCount = Math.min(cardinality.getMax(), Math.max(1, cardinality.getMin()));
			for (int i = 0; i < startCount; i++) {
				RemovableWidget<AutoCompleteInputField> widget = createWidget(oracle, cardinality);
				add(widget);
			}
		}

		private RemovableWidget<AutoCompleteInputField> createWidget(final SuggestOracle oracle, final GWTCardinality cardinality) {
			final RemovableWidget<AutoCompleteInputField> widget = new RemovableWidget<AutoCompleteInputField>(new AutoCompleteInputField(oracle));
			fields.add(widget);
			widget.addRemoveHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					synchronized (fields) {
						int size = fields.size();
						if (size > cardinality.getMin()) {
							fields.remove(widget);
							widget.removeFromParent();
						}
					}
				}
			});
			return widget;
		}
		
		@Override
		public Collection<Kind> getKind() {
			return Collections.singleton(Kind.List);
		}

		@Override
		public String getSimpleValue() {
			return null;
		}

		@Override
		public List<String> getListSimpleValue() {
			List<String> values = new ArrayList<String>(fields.size());
			for (RemovableWidget<AutoCompleteInputField> field : fields) {
				values.add(field.getChildWidget().getSimpleValue());
			}
			return values;
		}

		@Override
		public List<String> getPrefixValues() {
			return null;
		}
		
	}
}