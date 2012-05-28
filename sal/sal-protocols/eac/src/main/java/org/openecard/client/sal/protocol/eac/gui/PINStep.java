package org.openecard.client.sal.protocol.eac.gui;

import java.util.Map;
import org.openecard.client.common.I18n;
import org.openecard.client.gui.definition.Checkbox;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.PasswordField;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.executor.ExecutionResults;


/**
 * Implements a GUI user consent step for the PIN.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PINStep {

    // GUI translation constants
    private static final String TITLE = "step_pin_title";
    private static final String DESCRIPTION = "step_pin_description";
    private static final String PIN = "PIN";
    //
    private I18n lang = I18n.getTranslation("sal");
    private Step step = new Step(lang.translationForKey(TITLE));
    private GUIContentMap content;

    /**
     * Creates a new GUI user consent step for the PIN.
     *
     * @param content GUI content
     */
    public PINStep(GUIContentMap content) {
	this.content = content;

	initialize();
    }

    private void initialize() {
	Text description = new Text();
	description.setText(lang.translationForKey(DESCRIPTION));
	step.getInputInfoUnits().add(description);

	Checkbox readAccessCheckBox = new Checkbox();
	step.getInputInfoUnits().add(readAccessCheckBox);

	//TODO Der step sollte so den pin type berücksichtigen.
	PasswordField pinInputField = new PasswordField();
	pinInputField.setID(PIN);
//	pinInputField.setDescription(lang.translationForKey(PIN));
	pinInputField.setDescription(PIN);
	step.getInputInfoUnits().add(pinInputField);
    }

    /**
     * Returns the generated step.
     *
     * @return Step
     */
    public Step getStep() {
	return step;
    }

    /**
     * Processes the results of step.
     *
     * @param results Results
     */
    public void processResult(Map<String, ExecutionResults> results) {
	ExecutionResults executionResults = results.get(step.getID());

	if (executionResults == null) {
	    return;
	}

	for (OutputInfoUnit output : executionResults.getResults()) {
	    if (output instanceof PasswordField) {
		PasswordField p = (PasswordField) output;
		if (p.getID().equals(PIN)) {
		    content.add(GUIContentMap.ELEMENT.PIN, p.getValue());
		}
	    }
	}
    }
}
