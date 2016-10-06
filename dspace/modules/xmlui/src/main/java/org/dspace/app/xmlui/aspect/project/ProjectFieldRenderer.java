package org.dspace.app.xmlui.aspect.project;

import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.content.authority.ChoiceAuthorityManager;
import org.dspace.content.authority.MetadataAuthorityManager;

/**
 * Created by jonas - jonas@atmire.com on 03/10/16.
 */
public class ProjectFieldRenderer {


    public Text generalisedOneBoxFieldRender(List form, String fieldName, boolean readOnly, boolean required, Message label, Message hint) throws WingException {
        org.dspace.app.xmlui.wing.element.Item formItem = form.addItem();
        Text text = formItem.addText(fieldName, "submit-text");

        // Setup the select field
        text.setLabel(label);
        if(hint!=null) {
            text.setHelp(hint);
        }
        String fieldKey = MetadataAuthorityManager.makeFieldKey("rioxxterms", "identifier", "project");
        text.setAuthorityControlled();
        text.setAuthorityRequired(MetadataAuthorityManager.getManager().isAuthorityRequired(fieldKey));

        if (ChoiceAuthorityManager.getManager().isChoicesConfigured(fieldKey)) {
            text.setChoices(fieldKey);
            text.setChoicesPresentation(ChoiceAuthorityManager.getManager().getPresentation(fieldKey));
            text.setChoicesClosed(ChoiceAuthorityManager.getManager().isClosed(fieldKey));
        }

        if (readOnly) {
            text.setDisabled();
        }

        if (required) {
            text.setRequired();
        }
        return text;
    }
}
