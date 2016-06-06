package org.dspace.submit.step;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.util.Util;
import org.dspace.authority.ProjectAuthorityValue;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.Choices;
import org.dspace.content.authority.MetadataAuthorityManager;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.project.ProjectService;
import org.dspace.submit.AbstractProcessingStep;
import org.dspace.authority.DefaultAuthorityCreator;
import org.dspace.utils.DSpace;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 03/09/15
 * Time: 10:48
 */
public class ProjectStep extends AbstractProcessingStep {
    private static Logger log = Logger.getLogger(ProjectStep.class);

    public static final int ADD_PROJECT_SUCCESS = 1;
    public static final int CREATE_PROJECT_ERROR = 2;
    public static final int LOOKUP_PROJECT_ERROR = 3;
    public static final int REMOVE_PROJECT_SUCCESS = 4;
    public static final int No_PROJECTS_ADDED = 5;
    private ProjectService projectService = new DSpace().getServiceManager().getServiceByName("ProjectService", ProjectService.class);

    @Override
    public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo subInfo) throws ServletException, IOException, SQLException, AuthorizeException {
        Item item = subInfo.getSubmissionItem().getItem();
        Collection c = subInfo.getSubmissionItem().getCollection();

        clearErrorFields(request);

        String buttonPressed = Util.getSubmitButton(request, "");
        String removeButton = "submit_remove";
        String nextButton = "submit_next";
        String addButton = "submit_add";

        addFundersWithoutAuthority(context, request, item);

        if (buttonPressed.startsWith(removeButton)) {
            Metadatum[] dcValues = item.getMetadata("rioxxterms", "identifier", "project", Item.ANY);

            int index = Integer.parseInt(buttonPressed.substring(buttonPressed.lastIndexOf("_") + 1, buttonPressed.length()));

            item.clearMetadata("rioxxterms", "identifier", "project", Item.ANY);
            item.clearMetadata("rioxxterms", "funder", null, Item.ANY);

            int counter = 0;
            for (Metadatum dcv : dcValues) {
                if (counter != index) {
                    try {
                        ProjectAuthorityValue project = projectService.getProjectByAuthorityId(context, dcv.authority);
                        item.addMetadata(dcv.schema, dcv.element, dcv.qualifier, dcv.language, dcv.value, dcv.authority, dcv.confidence);
                        item.addMetadata("rioxxterms", "funder", null, getDefaultLanguageQualifier(), project.getFunderAuthorityValue().getValue(),
                                project.getFunderAuthorityValue().getId(), (Choices.CF_ACCEPTED));
                    } catch (IllegalArgumentException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                counter++;
            }

            item.update();
            context.commit();

            return REMOVE_PROJECT_SUCCESS;
        }

        if (buttonPressed.startsWith(addButton)) {
            return processProjectField(context, request, item, "rioxxterms", "identifier", "project", ADD_PROJECT_SUCCESS);
        }

        if (buttonPressed.startsWith(nextButton)) {
            int success = processProjectField(context, request, item, "rioxxterms", "identifier", "project", STATUS_COMPLETE);

            if (success == STATUS_COMPLETE) {
                //check that at least one project is added
                Metadatum[] dcValues = item.getMetadata("rioxxterms", "identifier", "project", Item.ANY);

                if (dcValues.length == 0) {
                    addErrorField(request, MetadataField.formKey("rioxxterms", "identifier", "project"));
                    return No_PROJECTS_ADDED;
                }
            }
            return success;
        }

        return STATUS_COMPLETE;
    }

    private void addFundersWithoutAuthority(final Context context, final HttpServletRequest request, final Item item) throws SQLException, AuthorizeException {
        item.clearMetadata("dc", "description", "sponsorship",  Item.ANY);

        readText(request, item, "dc", "description", "sponsorship", true, getDefaultLanguageQualifier());

        item.update();
        context.commit();
    }

    private int processProjectField(Context context, HttpServletRequest request, Item item, String schema,
                                    String element, String qualifier, int success) throws SQLException, AuthorizeException {
        String metadataField = MetadataField.formKey(schema, element, qualifier);

        String value = request.getParameter(metadataField);
        String av = request.getParameter(metadataField + "_authority");
        String cv = request.getParameter(metadataField + "_confidence");
        if (StringUtils.isBlank(value) && noProjectAndFunderAttached(item)) {
            ProjectAuthorityValue project = new DSpace().getServiceManager().getServiceByName("defaultAuthorityCreator", DefaultAuthorityCreator.class).retrieveDefaultProject(context);
            value = project.getValue();
            av = project.getId();
        }
        if (StringUtils.isNotBlank(value)) {
            if (StringUtils.isNotBlank(av)) {
                try {
                    ProjectAuthorityValue project = projectService.getProjectByAuthorityId(context, av);
                    item.addMetadata(schema, element, qualifier, getDefaultLanguageQualifier(), value,
                            av, (cv != null && cv.length() > 0) ?
                                    Choices.getConfidenceValue(cv) : Choices.CF_ACCEPTED);
                    item.addMetadata("rioxxterms", "funder", null, getDefaultLanguageQualifier(), project.getFunderAuthorityValue().getValue(),
                            project.getFunderAuthorityValue().getId(), (Choices.CF_ACCEPTED));
                    item.update();
                    context.commit();
                } catch (IllegalArgumentException e) {
                    log.error(e.getMessage(), e);
                    addErrorField(request, metadataField);
                    return LOOKUP_PROJECT_ERROR;
                }
            } else {
                String funderAuthority = request.getParameter(MetadataField.formKey("rioxxterms", "funder", null) + "_authority");
                try {
                    if (StringUtils.isBlank(funderAuthority)) {
                        funderAuthority = new DSpace().getServiceManager().getServiceByName("defaultAuthorityCreator", DefaultAuthorityCreator.class).retrieveDefaultFunder(context).getId();
                    }
                    ProjectAuthorityValue project = projectService.createProject(context, value, funderAuthority);
                    item.addMetadata("rioxxterms", "identifier", "project", getDefaultLanguageQualifier(), value, project.getId(), Choices.CF_ACCEPTED);
                    item.addMetadata("rioxxterms", "funder", null, getDefaultLanguageQualifier(), project.getFunderAuthorityValue().getValue(),
                            project.getFunderAuthorityValue().getId(), (Choices.CF_ACCEPTED));
                    item.update();
                    context.commit();
                    request.getSession().setAttribute("newProject", project);
                } catch (IllegalArgumentException e) {
                    log.error(e.getMessage(), e);
                    addErrorField(request, metadataField);
                    return CREATE_PROJECT_ERROR;
                }
            }
        } else {
            addErrorField(request, metadataField);
        }

        return success;
    }

    private boolean noProjectAndFunderAttached(Item item) {
        Metadatum[] funders = item.getMetadata("rioxxterms", "funder", null, Item.ANY);
        Metadatum[] projects = item.getMetadata("rioxxterms", "identifier", "project", Item.ANY);
        return funders.length == 0 && projects.length == 0;
    }


    @Override
    public int getNumberOfPages(HttpServletRequest request, SubmissionInfo subInfo) throws ServletException {
        return 1;
    }

    public static String getDefaultLanguageQualifier() {
        String language = "";
        language = ConfigurationManager.getProperty("default.language");
        if (StringUtils.isEmpty(language)) {
            language = "en";
        }
        return language;
    }

    protected void readText(HttpServletRequest request, Item item, String schema,
                            String element, String qualifier, boolean repeated, String lang) {
        // some other way
        String metadataField = MetadataField
                .formKey(schema, element, qualifier);

        String fieldKey = MetadataAuthorityManager.makeFieldKey(schema, element, qualifier);
        boolean isAuthorityControlled = MetadataAuthorityManager.getManager().isAuthorityControlled(fieldKey);

        // Values to add
        List<String> vals = null;
        List<String> auths = null;
        List<String> confs = null;

        if (repeated) {
            vals = getRepeatedParameter(request, metadataField, metadataField);
            if (isAuthorityControlled) {
                auths = getRepeatedParameter(request, metadataField, metadataField + "_authority");
                confs = getRepeatedParameter(request, metadataField, metadataField + "_confidence");
            }

        } else {
            // Just a single name
            vals = new LinkedList<String>();
            String value = request.getParameter(metadataField);
            if (value != null) {
                vals.add(value.trim());
            }
            if (isAuthorityControlled) {
                auths = new LinkedList<String>();
                confs = new LinkedList<String>();
                String av = request.getParameter(metadataField + "_authority");
                String cv = request.getParameter(metadataField + "_confidence");
                auths.add(av == null ? "" : av.trim());
                confs.add(cv == null ? "" : cv.trim());
            }
        }

        // Put the names in the correct form
        for (int i = 0; i < vals.size(); i++) {
            // Add to the database if non-empty
            String s = vals.get(i);
            if ((s != null) && !s.equals("")) {
                if (isAuthorityControlled) {
                    String authKey = auths.size() > i ? auths.get(i) : null;
                    String sconf = (authKey != null && confs.size() > i) ? confs.get(i) : null;
                    if (MetadataAuthorityManager.getManager().isAuthorityRequired(fieldKey) &&
                            (authKey == null || authKey.length() == 0)) {
                        log.warn("Skipping value of " + metadataField + " because the required Authority key is missing or empty.");
                        addErrorField(request, metadataField);
                    } else {
                        item.addMetadata(schema, element, qualifier, lang, s,
                                authKey, (sconf != null && sconf.length() > 0) ?
                                        Choices.getConfidenceValue(sconf) : Choices.CF_ACCEPTED);
                    }
                } else {
                    item.addMetadata(schema, element, qualifier, lang, s);
                }
            }
        }
    }

    protected List<String> getRepeatedParameter(HttpServletRequest request,
                                                String metadataField, String param) {
        List<String> vals = new LinkedList<String>();

        int i = 1;    //start index at the first of the previously entered values
        boolean foundLast = false;

        // Iterate through the values in the form.
        while (!foundLast) {
            String s = null;

            //First, add the previously entered values.
            // This ensures we preserve the order that these values were entered
            s = request.getParameter(param + "_" + i);

            // If there are no more previously entered values,
            // see if there's a new value entered in textbox
            if (s == null) {
                s = request.getParameter(param);
                //this will be the last value added
                foundLast = true;
            }

            // We're only going to add non-null values
            if (s != null) {
                boolean addValue = true;

                // Check to make sure that this value was not selected to be
                // removed.
                // (This is for the "remove multiple" option available in
                // Manakin)
                String[] selected = request.getParameterValues(metadataField
                        + "_selected");

                if (selected != null) {
                    for (int j = 0; j < selected.length; j++) {
                        if (selected[j].equals(metadataField + "_" + i)) {
                            addValue = false;
                        }
                    }
                }

                if (addValue) {
                    vals.add(s.trim());
                }
            }

            i++;
        }

        log.debug("getRepeatedParameter: metadataField=" + metadataField
                + " param=" + metadataField + ", return count = " + vals.size());

        return vals;
    }
}
