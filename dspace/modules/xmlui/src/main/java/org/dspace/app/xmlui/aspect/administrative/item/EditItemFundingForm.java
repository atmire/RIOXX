package org.dspace.app.xmlui.aspect.administrative.item;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.dspace.app.xmlui.aspect.project.ProjectFieldRenderer;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authority.DefaultAuthorityCreator;
import org.dspace.authority.ProjectAuthorityValue;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;
import org.dspace.project.ProjectService;
import org.dspace.utils.DSpace;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by jonas - jonas@atmire.com on 03/10/16.
 */
public class EditItemFundingForm extends AbstractDSpaceTransformer {

    private static final Message T_dspace_home = message("xmlui.general.dspace_home");
    private static final Message T_title = message("xmlui.administrative.item.EditItemFundingForm.title");
    private static final Message T_item_trail = message("xmlui.administrative.item.general.item_trail");
    private static final Message T_trail = message("xmlui.administrative.item.EditItemFundingForm.trail");

    private static final Message T_option_head = message("xmlui.administrative.item.general.option_head");
    private static final Message T_option_status = message("xmlui.administrative.item.general.option_status");
    private static final Message T_option_bitstreams = message("xmlui.administrative.item.general.option_bitstreams");
    private static final Message T_option_metadata = message("xmlui.administrative.item.general.option_metadata");
    private static final Message T_option_view = message("xmlui.administrative.item.general.option_view");
    private static final Message T_option_curate = message("xmlui.administrative.item.general.option_curate");
    private static final Message T_option_funding = message("xmlui.administrative.item.general.option_funding");

    protected static final Message T_project_label = message("xmlui.Submission.submit.ProjectStep.project.label");
    protected static final Message T_funder_label = message("xmlui.Submission.submit.ProjectStep.funder.label");
    protected static final Message T_head = message("xmlui.Submission.submit.ProjectStep.head");
    protected static final Message T_project_funder_hint = message("xmlui.Submission.submit.ProjectStep.project_funder.hint");
    protected static final Message T_remove = message("xmlui.Submission.submit.ProjectStep.remove");


    private ProjectAuthorityValue newProject;

    private DefaultAuthorityCreator defaultAuthorityCreator = new DSpace().getServiceManager().getServiceByName("defaultAuthorityCreator", DefaultAuthorityCreator.class);
    private ProjectService projectService = new DSpace().getServiceManager().getServiceByName("ProjectService", ProjectService.class);

    @Override
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, parameters);
        Request request = ObjectModelHelper.getRequest(objectModel);
        newProject = (ProjectAuthorityValue) request.getSession().getAttribute("newProject");

        ProjectAuthorityValue project = defaultAuthorityCreator.retrieveDefaultProject(context);

    }
    public void addPageMeta(PageMeta pageMeta) throws WingException
    {
        pageMeta.addMetadata("title").addContent(T_title);

        pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
        pageMeta.addTrailLink(contextPath + "/admin/item",T_item_trail);
        pageMeta.addTrail().addContent(T_trail);

        pageMeta.addMetadata("stylesheet", "screen", "datatables", true).addContent("../../static/Datatables/DataTables-1.8.0/media/css/datatables.css");
        pageMeta.addMetadata("javascript", "static", "datatables", true).addContent("static/Datatables/DataTables-1.8.0/media/js/jquery.dataTables.min.js");
        pageMeta.addMetadata("stylesheet", "screen", "person-lookup", true).addContent("../../static/css/authority/person-lookup.css");
        pageMeta.addMetadata("javascript", null, "person-lookup", true).addContent("../../static/js/person-lookup.js");

    }

    public void addBody(Body body) throws SQLException, WingException {
        int itemID = parameters.getParameterAsInteger("itemID",-1);
        Item item = Item.find(context, itemID);

        // DIVISION: main
        Division main = body.addInteractiveDivision("edit-item-status", contextPath+"/admin/item", Division.METHOD_POST,"primary administrative edit-item-status");
        main.setHead(T_option_head);
        main.addHidden("administrative-continue").setValue(knot.getId());

        String baseURL = contextPath+"/admin/item?administrative-continue="+knot.getId();



        // LIST: options
        List options = main.addList("options",List.TYPE_SIMPLE,"horizontal");
        options.addItem().addXref(baseURL+"&submit_status",T_option_status);
        options.addItem().addXref(baseURL+"&submit_bitstreams",T_option_bitstreams);
        options.addItem().addXref(baseURL+"&submit_metadata",T_option_metadata);
        options.addItem().addHighlight("bold").addXref(baseURL + "&submit_funding", T_option_funding);
        options.addItem().addXref(baseURL + "&view_item", T_option_view);
        options.addItem().addXref(baseURL + "&submit_curate", T_option_curate);


        Collection collection = item.getCollections()[0];

        String actionURL = contextPath + "/handle/" + collection.getHandle() + "/submit/" + knot.getId() + ".continue";
        Division div = main.addInteractiveDivision("submit-describe", actionURL, Division.METHOD_POST, "primary submission");
        div.addHidden("administrative-continue").setValue(knot.getId());

        List form = div.addList("submit-project", List.TYPE_FORM);
        form.setHead(T_head);

        ProjectFieldRenderer fieldRendering = new ProjectFieldRenderer();
        String fieldName = "rioxxterms_identifier_project";
        fieldRendering.generalisedOneBoxFieldRender(form, fieldName, false, false, T_project_label, null);

        fieldName = "rioxxterms_funder";
        fieldRendering.generalisedOneBoxFieldRender(form, fieldName, true, false, T_funder_label, null);

        form.addItem("project_funder_help","").addContent(T_project_funder_hint.parameterize(ConfigurationManager.getProperty("mail.admin")));

        Metadatum[] dcValues = item.getMetadata("rioxxterms", "identifier", "project", Item.ANY);

        form.addItem().addButton("submit_add").setValue("Add");

        renderResults(div, dcValues);



    }
    private void renderResults(Division div, Metadatum[] dcValues) throws WingException {
        if (dcValues != null && dcValues.length > 0) {
            Division projectDiv = div.addDivision("projects");

            Table table = projectDiv.addTable("project-table", 3, dcValues.length + 1);
            Row header = table.addRow(Row.ROLE_HEADER);
            header.addCell().addContent(T_project_label);
            header.addCell().addContent(T_funder_label);
            header.addCell().addContent("");

            for (Metadatum dcValue : dcValues) {
                ProjectAuthorityValue project;
                if (newProject != null && newProject.getId().equals(dcValue.authority)) {
                    project = newProject;
                } else {
                    project = projectService.getProjectByAuthorityId(context, dcValue.authority);
                }

                if (project != null) {
                    Row row = table.addRow();
                    row.addCell().addContent(project.getValue());
                    row.addCell().addContent(project.getFunderAuthorityValue().getValue());
                    row.addCell().addButton("submit_remove_project").setValue(T_remove);
                    row.addCell("project_id",null,"hidden").addHidden("project_id").setValue(project.getId());
                }
            }
        }
    }


}
