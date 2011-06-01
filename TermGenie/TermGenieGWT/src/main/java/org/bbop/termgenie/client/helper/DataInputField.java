package org.bbop.termgenie.client.helper;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.services.TermSuggestion;
import org.bbop.termgenie.shared.GWTTermGenerationParameter;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTCardinality;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTTemplateField;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.GenericSuggestBox;
import com.google.gwt.user.client.ui.GenericSuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Wrapper for keeping the different options of the data input fields. Hide the
 * actual implementation from the user. Use only the {@link #extractParameter}
 * method to retrieve the input data.
 */
public interface DataInputField {

	/**
	 * Extract the field values into the parameter data structure for the given
	 * template field.
	 * 
	 * @param parameter
	 * @param field
	 * @return true, if the extraction was successful
	 */
	public boolean extractParameter(GWTTermGenerationParameter parameter, GWTTemplateField field);

	/**
	 * Retrieve the widget to be displayed for this input field.
	 * 
	 * @return display widget
	 */
	public Widget getWidget();
	
	/**
	 * A css style name for marking input fields to have an error.
	 */
	public static final String CSS_ERROR_FIELD_STYLE = "input-field-error-markup";
	
	/**
	 * A simple input field for text.
	 */
	public static class TextFieldInput extends TextBox implements DataInputField {

		@Override
		public boolean extractParameter(GWTTermGenerationParameter parameter, GWTTemplateField field) {
			resetErrorMark();
			String text = getText().trim();
			if (text != null && !text.isEmpty()) {
				parameter.getStrings().addValue(text, field, 0);
				return true;
			}
			// only report this field as error if it is required.
			boolean success = field.isRequired() == false;
			if (!success) {
				setErrorMark();
			}
			return success;
		}

		@Override
		public Widget getWidget() {
			return this;
		}

		void setErrorMark() {
			setStyleName(CSS_ERROR_FIELD_STYLE, true);
		}
		
		void resetErrorMark() {
			setStyleName(CSS_ERROR_FIELD_STYLE, false);
		}
	}

	/**
	 * A input field for terms, requires a {@link GenericSuggestBox} to provide
	 * auto-complete of ontology terms.
	 */
	public static class AutoCompleteInputField extends GenericSuggestBox<TermSuggestion> implements
			DataInputField {
		
		public AutoCompleteInputField(GenericSuggestOracle<TermSuggestion> oracle) {
			super(oracle);
		}

		@Override
		public Widget getWidget() {
			return this;
		}
		
		void setErrorMark() {
			getTextBox().setStyleName(CSS_ERROR_FIELD_STYLE, true);
		}
		
		void resetErrorMark() {
			getTextBox().setStyleName(CSS_ERROR_FIELD_STYLE, false);
		}

		@Override
		public boolean extractParameter(GWTTermGenerationParameter parameter, GWTTemplateField field) {
			resetErrorMark();
			TermSuggestion suggestion = getTermSuggestion();
			if (suggestion != null) {
				parameter.getTerms().addValue(suggestion.getIdentifier(), field, 0);
				return true;
			}
			setErrorMark();
			return false;
		}

		/**
		 * Try to get for the current text the corresponding term suggestion.
		 * 
		 * @return term suggestion
		 */
		TermSuggestion getTermSuggestion() {
			TermSuggestion suggestion = getCurrentItem();
			String text = getText();
			if (suggestion != null) {
				// always check if the current text and the term correspond!
				if (text != null && !text.isEmpty()) {
					if (text.equals(suggestion.getLabel())) {
						return suggestion;
					}
				}
			}
			return null;
		}
	}

	/**
	 * An input field with auto-completion for ontology terms and a list of
	 * prefixes to be selected.
	 */
	public static class PrefixAutoCompleteInputField extends VerticalPanel implements
			DataInputField {

		private final AutoCompleteInputField field;
		private final List<CheckBox> prefixes;

		public PrefixAutoCompleteInputField(GenericSuggestOracle<TermSuggestion> oracle,
				String[] functionalPrefixes) {
			super();
			setSize("100%", "100%");

			field = new AutoCompleteInputField(oracle);
			add(field);
			this.prefixes = new ArrayList<CheckBox>(functionalPrefixes.length);
			for (String prefix : functionalPrefixes) {
				CheckBox checkBox = new CheckBox(prefix);
				checkBox.setValue(true); // set default as selected
				this.prefixes.add(checkBox);
				add(checkBox);
			}
		}

		@Override
		public boolean extractParameter(GWTTermGenerationParameter parameter, GWTTemplateField field) {
			resetErrorMark();
			TermSuggestion term = this.field.getTermSuggestion();
			if (term != null) {
				parameter.getTerms().addValue(term.getIdentifier(), field, 0);
				List<String> selectedPrefixes = new ArrayList<String>(prefixes.size());
				for (CheckBox box : prefixes) {
					if (box.getValue()) {
						selectedPrefixes.add(box.getText());
					}
				}
				parameter.getPrefixes().addValue(selectedPrefixes, field, 0);
				return true;
			}
			setErrorMark();
			return false;
		}

		@Override
		public Widget getWidget() {
			return this;
		}
		
		void setErrorMark() {
			field.setErrorMark();
		}
		
		void resetErrorMark() {
			field.resetErrorMark();
		}

	}

	/**
	 * An input field for a list of ontology terms, provides auto-complete functionality.
	 */
	public static class ListAutoCompleteInputField extends VerticalPanel implements DataInputField {

		private final List<AutoCompleteInputField> fields;
		private final ModifyButtonsWidget buttonsWidget;

		public ListAutoCompleteInputField(final GenericSuggestOracle<TermSuggestion> oracle,
				final GWTCardinality cardinality) {
			super();
			fields = new ArrayList<AutoCompleteInputField>();
			buttonsWidget = new ModifyButtonsWidget();
			buttonsWidget.addAddHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					remove(buttonsWidget);
					if (fields.size() < cardinality.getMax()) {
						AutoCompleteInputField widget = new AutoCompleteInputField(oracle);
						fields.add(widget);
						add(widget);
					}
					add(buttonsWidget);
				}
			});
			buttonsWidget.addRemoveHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					int size = fields.size();
					if (size > 0 && size > cardinality.getMin()) {
						AutoCompleteInputField field = fields.remove(size - 1);
						remove(field);
					}
				}
			});

			int startCount = Math.min(cardinality.getMax(), Math.max(1, cardinality.getMin()));
			for (int i = 0; i < startCount; i++) {
				AutoCompleteInputField widget = new AutoCompleteInputField(oracle);
				fields.add(widget);
				add(widget);
			}
			add(buttonsWidget);
		}

		@Override
		public boolean extractParameter(GWTTermGenerationParameter parameter, GWTTemplateField field) {
			int pos = 0;
			for (AutoCompleteInputField inputField : fields) {
				inputField.resetErrorMark();
				TermSuggestion term = inputField.getTermSuggestion();
				if (term != null) {
					parameter.getTerms().addValue(term.getIdentifier(), field, pos++);
				}
				else {
					inputField.setErrorMark();
				}
			}
			return pos > 0;
		}

		@Override
		public Widget getWidget() {
			return this;
		}
	}
}