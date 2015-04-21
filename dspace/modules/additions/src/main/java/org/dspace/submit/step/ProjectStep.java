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
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.project.ProjectService;
import org.dspace.submit.AbstractProcessingStep;
import org.dspace.utils.DSpace;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

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


        if (buttonPressed.startsWith(removeButton))
        {
            Metadatum[] dcValues = item.getMetadata("rioxxterms", "identifier", "project", Item.ANY);

            int index = Integer.parseInt(buttonPressed.substring(buttonPressed.lastIndexOf("_")+1,buttonPressed.length()));

            item.clearMetadata("rioxxterms","identifier","project", Item.ANY);
            item.clearMetadata("rioxxterms","funder",null, Item.ANY);

            int counter = 0;
            for (Metadatum dcv : dcValues) {
                if(counter!=index) {
                    try {
                        ProjectAuthorityValue project = projectService.getProjectByAuthorityId(context, dcv.authority);
                        item.addMetadata(dcv.schema, dcv.element, dcv.qualifier, dcv.language, dcv.value, dcv.authority, dcv.confidence);
                        item.addMetadata("rioxxterms", "funder", null, getDefaultLanguageQualifier(), project.getFunderAuthorityValue().getValue(),
                                project.getFunderAuthorityValue().getId(), (Choices.CF_ACCEPTED));
                    }
                    catch (IllegalArgumentException e){
                        log.error(e.getMessage(),e);
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

            if(success == STATUS_COMPLETE){
                //check that at least one project is added
                Metadatum[] dcValues = item.getMetadata("rioxxterms", "identifier", "project", Item.ANY);

                if(dcValues.length==0){
                    addErrorField(request, MetadataField.formKey("rioxxterms", "identifier", "project"));
                    return No_PROJECTS_ADDED;
                }
            }
            return success;
        }

        return STATUS_COMPLETE;
    }

    private int processProjectField(Context context, HttpServletRequest request, Item item, String schema,
                                    String element, String qualifier, int success) throws SQLException, AuthorizeException {
        String metadataField = MetadataField.formKey(schema, element, qualifier);

        String value = request.getParameter(metadataField);
        String av = request.getParameter(metadataField + "_authority");
        String cv = request.getParameter(metadataField + "_confidence");

        if(StringUtils.isNotBlank(value)) {
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
                }
                catch (IllegalArgumentException e){
                    log.error(e.getMessage(), e);
                    addErrorField(request, metadataField);
                    return LOOKUP_PROJECT_ERROR;
                }
            }
            else {
                String funderAuthority = request.getParameter(MetadataField.formKey("rioxxterms", "funder", null)+ "_authority");
                try{
                    ProjectAuthorityValue project = projectService.createProject(context, value, funderAuthority);
                    item.addMetadata("rioxxterms", "identifier", "project", getDefaultLanguageQualifier(), value, project.getId(), Choices.CF_ACCEPTED);
                    item.addMetadata("rioxxterms", "funder", null, getDefaultLanguageQualifier(), project.getFunderAuthorityValue().getValue(),
                            project.getFunderAuthorityValue().getId(), (Choices.CF_ACCEPTED));
                    item.update();
                    context.commit();
                    request.getSession().setAttribute("newProject", project);
                }
                catch (IllegalArgumentException e) {
                    log.error(e.getMessage(),e);
                    addErrorField(request, metadataField);
                    return CREATE_PROJECT_ERROR;
                }
            }
        }else {
            addErrorField(request,metadataField);
        }

        return success;
    }

    @Override
    public int getNumberOfPages(HttpServletRequest request, SubmissionInfo subInfo) throws ServletException {
        return 1;
    }

    public static String getDefaultLanguageQualifier()
    {
        String language = "";
        language = ConfigurationManager.getProperty("default.language");
        if (StringUtils.isEmpty(language))
        {
            language = "en";
        }
        return language;
    }
}
