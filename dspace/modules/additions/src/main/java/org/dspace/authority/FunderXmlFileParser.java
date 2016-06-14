package org.dspace.authority;

import org.apache.poi.hssf.record.formula.functions.T;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.kernel.ServiceManager;
import org.dspace.scripts.PopulateAuthorityFromXML;
import org.dspace.util.XMLUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 23 Apr 2015
 */
public class FunderXmlFileParser {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(FunderXmlFileParser.class);
    private PrintWriter progressWriter;

    private PopulateAuthorityFromXML<FunderAuthorityValue> populateAuthorityFromXML;

    public FunderXmlFileParser(PopulateAuthorityFromXML<FunderAuthorityValue> populateAuthorityFromXML) {
        this.populateAuthorityFromXML = populateAuthorityFromXML;
    }

    public void getFunderAuthorities(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Document document = XMLUtils.convertStreamToXML(fileInputStream);

            int numberParsed = 0;
            Iterator<Node> iterator = XMLUtils.getNodeListIterator(document, "/RDF/Concept");
            while (iterator.hasNext()) {
                FunderAuthorityValue funderAuthorityValue = FunderAuthorityValue.create();
                Node node = iterator.next();
                setProperties(funderAuthorityValue, node);

                populateAuthorityFromXML.processValue(funderAuthorityValue);

                numberParsed++;
                if(numberParsed % 100 == 0) {
                    populateAuthorityFromXML.commitIndexingService();
                    if (progressWriter != null) {
                    progressWriter.println("Number of funders parsed: " + numberParsed);
                }
            }
            }
            if (progressWriter != null) {
                progressWriter.println("Number of funders parsed: " + numberParsed);
            }

        } catch (FileNotFoundException | XPathExpressionException e) {
            log.error("Error", e);
        }
    }

    private void setProperties(final FunderAuthorityValue funderAuthorityValue, Node node) throws XPathExpressionException {
        String textContent = null;
        // rdf:about doi handle -> funderID
        Node aboutAttr = node.getAttributes().getNamedItem("rdf:about");
        if (aboutAttr != null) {
            textContent = aboutAttr.getTextContent();
        }
        if (textContent != null && textContent.startsWith("http://dx.doi.org/")) {
            String funderID = textContent.substring("http://dx.doi.org/".length());
            funderAuthorityValue.setFunderID(funderID);
        }

        // prefLabel -> Value
        setProperty(node, "prefLabel/Label/literalForm", new PropertySetter(funderAuthorityValue) {
            @Override
            public void set(String value) {
                this.authority.setValue(value);
            }
        });

        // altLabel -> Name variants
        setProperty(node, "altLabel/Label/literalForm", new PropertySetter(funderAuthorityValue) {
            @Override
            public void set(String value) {
                this.authority.addNameVariant(value);
            }
        });

        // fundingBodyType
        setProperty(node, "fundingBodyType", new PropertySetter(funderAuthorityValue) {
            @Override
            public void set(String value) {
                this.authority.setFundingBodyType(value);
            }
        });

        // fundingBodySubType
        setProperty(node, "fundingBodySubType", new PropertySetter(funderAuthorityValue) {
            @Override
            public void set(String value) {
                this.authority.setFundingBodySubType(value);
            }
        });
    }

    private void setProperty(Node node, String xpath, PropertySetter setter) throws XPathExpressionException {
        Iterator<Node> iterator = XMLUtils.getNodeListIterator(node, xpath);
        while (iterator.hasNext()) {
            Node propertyNode = iterator.next();
            String textContent = propertyNode.getTextContent();
            setter.set(textContent);
        }
    }

    public void setProgressWriter(PrintWriter progressWriter) {
        this.progressWriter = progressWriter;
    }

    private abstract class PropertySetter {
        FunderAuthorityValue authority;

        public PropertySetter(FunderAuthorityValue funderAuthorityValue) {
            this.authority = funderAuthorityValue;
        }

        abstract void set(String value);
    }
}
