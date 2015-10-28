- [Introduction](#Introduction)
	- [Areas of DSpace affected by the RIOXX patch](#Areas-of-DSpace-affected)
	- [Areas of DSpace that have to be manually configured after applying the patch](#Areas-of-DSpace-manually-configured)
- [Metadata mapping](#Metadata-mapping)
	- [General DSpace to RIOXXTERMS metadata mapping](#General-DSpace-RIOXXTERMS)
	- [RIOXX metadata derived from DSpace Bitstream metadata](#RIOXX-metadata-derived)
	- [dc:source mandatory where applicable](#dcsource-mandatory)
	- [dc:type fallback for rioxxterms:type](#dctype-fallback)
	- [fundref id for funders and orcid id for authors](#fundref-id)
- [Patch Installation Procedures](#Patch-installation-procedures)
	- [Prerequisites](#Prerequisites)
	- [Obtaining a recent patch file](#Obtaining-recent-patch)
	- [Patch installation](#Patch-installation)
		- [1. Go to the DSpace Source directory.](#goto-DSpace-Source)
		- [2. Run the Git command to check whether the patch can be correctly applied.](#Run-git-command)
		- [3. Apply the patch](#Apply-patch)
		- [4. Rebuild and redeploy your repository](#Rebuild-redeploy)
		- [5. Restart your tomcat](#Restart-tomcat)
		- [6. Populate the RIOXX OAI-PMH end point](#Populate-RIOXX)
		- [7. XMLUI only: Load Fundref authority data](#XMLUI-only)
	- [Configure Submission forms or other metadata ingest mechanisms](#Configure-submission)
- [Verification](#Verification)
	- [RIOXX Metadata Registry](#RIOXX-metadata-registry)
	- [Submission forms based on @mire template](#Submission-forms-template)
	- [OAI-PMH endpoint](#OAI-PMH-endpoint)
- [Troubleshooting](#Troubleshooting)
	- [Errors during the Patch Installation process](#Errors-patch-installation)
	- [RIOXX test items are not visible in OAI-PMH endpoint](#RIOXX-test-OAI-PMH-endpoint)

# Introduction <a name="Introduction"></a> #


This documentation will help you deploy and configure the RIOXXv2 Application Profile for DSpace 3.X, 4.X and 5.X. The patch has been implemented in a generic way, using a configurable crosswalk. This means that changes to your existing DSpace installation are kept to the strict minimum. If you have customized your DSpace submission forms, metadata registries or OAI Crosswalks, it is possible that the default patches can't be applied to your codebase. 

## Areas of DSpace affected by the RIOXX patch <a name="Areas-of-DSpace-affected"></a> ##

Following areas of the DSpace codebase are affected by the RIOXX patch:  
  **Metadata Registries**: a new RIOXX metadata registry will be added with a number of new fields. This does not affect your existing metadata schema's or items  
**OAI Endpoint**: a new RIOXX endpoint will become available in your OAI-PMH interface, in order to allow external harvesters to harvest your repository metadata in RIOXX compliant format.

It is important to realize that your existing item metadata and item display pages will **NOT** be modified as part of the RIOXX patch.

## Areas of DSpace that have to be manually configured after applying the patch  <a name="Areas-of-DSpace-manually-configured"></a>##

**Submission forms**: the configuration file that defines your submission forms, input-forms.xml needs to be be extended with a number of new entry options.

Because the vast majority of institutions makes at least small tweaks to the submission forms, there is no opportunity to apply a patch to a standardized file. A template submission form file where the new RIOXX fields are highlighted can be found on Github:

[https://github.com/atmire/RIOXX/compare/rc1...master](https://github.com/atmire/RIOXX/compare/rc1...master)

# Metadata mapping <a name="Metadata-mapping"></a>

Before diving into the process of installing the RIOXX patch, it is crucial that you take note of the specific DSpace=>RIOXX metadata mapping that this patch implements. Your use of the different dc and dcterms fields in DSpace may be different than a standard installation, in which case you may need to do some additional activities before or after applying the patch.

The following table lists the different metadata elements, according to the order specified in [http://rioxx.net/v2-0-final/](http://rioxx.net/v2-0-final/).  
The DSpace metadata column indicates where the corresponding RIOXX elements are stored in the DSpace metadata.

Existing fields from the dc and dcterms namespace were used where possible. A number of new fields were added in a dedicated rioxxterms metadata registry.

## General DSpace to RIOXXTERMS metadata mapping <a name="General-DSpace-RIOXXTERMS"></a> ##

|  DSpace metadata   |  RIOXX element    |   example DSpace value |  example RIOXX value    |    
| ------------------ | ----------------- | ---------------------- | ----------------------- |
| Bitstream metadata|ali:free_to_read | See separate table with bitstream derived metadata below. | See separate table with bitstream derived metadata below.
| dc.rights.uri | ali:license_ref  |  [http://creativecommons.org/licenses/by/3.0/igo/](http://creativecommons.org/licenses/by/3.0/igo/)|  ` <ali:license_ref start_date="2015-01-20"> `  <br> ` http://creativecommons.org/licenses/by/3.0/igo/ ` <br> ` </ali:license_ref> ` |
| dc.date.issued| ali:license_ref:startdate | 2015-01-20| ` <ali:license_ref start_date="2015-01-20"> ` <br> `  http://creativecommons.org/licenses/by/3.0/igo/ `<br> `</ali:license_ref> `|
| dc.coverage| dc:coverage| Columbus, Ohio, USA; Lat: 39 57 N Long: 082 59 W| ` <dc:coverage> ` <br>  ` Columbus, Ohio, USA; Lat: 39 57 N Long: 082 59 W ` <br> ` </dc:coverage> `|
| dc.description.abstract| dc:description| example item | ` <dc:description> `<br> `example item`<br>`</dc:description>`|
| Bitstream metadata| dc:format| See separate table with bitstream derived metadata below. | See separate table with bitstream derived metadata below. |
| Bitstream metadata| dc:identifier| See separate table with bitstream derived metadata below. | See separate table with bitstream derived metadata below. |
| dc.language.iso| dc:language | en-GB|`<dc:language> ` <br> ` en-GB ` <br> `  </dc:language>` |
| dc.publisher| dc:publisher | PLOS ONE|` <dc:publisher> `<br> `PLOS ONE`<br>`</dc:publisher>`|
| dc.relation.uri| dc:relation |[http://datadryad.org/resource/doi:10.5061/dryad.tg469](http://datadryad.org/resource/doi:10.5061/dryad.tg469) |`<dc:relation>`<br>`http://datadryad.org/resource/doi:10.5061/dryad.tg469`<br>`</dc:relation>`|
| dc.identifier.isbn| dc:source | 0-14-020652-3|`<dc:source>`<br>`0-14-020652-3`<br>`</dc:source>`| 
| dc.identifier.issn| dc:source| 1456-2979 |`<dc:source>`<br>`1456-2979`<br>`</dc:source>`| 
| dc.subject| dc:subject | example |`<dc:subject>`<br>`example`<br>`</dc:subject>`|
| dc.title| dc:title| Title:Subtitle|`<dc:title>`<br>`Title:Subtitle`<br>`</dc:title>` |
| dcterms.dateAccepted| dcterms:dateAccepted| 2015-02-10|`<dcterms:dateAccepted>`<br>`2015-02-10`<br>`</dcterms:dateAccepted>`|
| rioxxterms.apc| rioxxterms:apc| paid|`<rioxxterms:apc>`<br>`paid`<br>`</rioxxterms:apc>`|
| dc.contributor.author (first)| rioxxterms:author|Lawson, Gerry|`<rioxxterms:author id="http://orcid.org/0000-0002-1395-3092" first-named-author="true">`<br>`Lawson, Gerry`<br>`</rioxxterms:author>`|
| dc.contributor.author| rioxxterms:contributor|Lawson, Gerry|`<rioxxterms:contributor id="http://orcid.org/0000-0002-1395-3092">`<br>`Lawson, Gerry`<br>`</rioxxterms:contributor>`|
| rioxxterms.identifier.project| rioxxterms:project| 0123456789|`<rioxxterms:project rioxxterms:funder_name="Engineering and Physical Sciences Research Council" rioxxterms:funder_id="http://dx.doi.org/10.13039/501100000266">`<br>`EP/K023195/1`<br>` </rioxxterms:project>`|
| rioxxterms.funder| rioxxterms:project| Engineering and Physical Sciences Research Council|`<rioxxterms:project rioxxterms:funder_name="Engineering and Physical Sciences Research Council" rioxxterms:funder_id="http://dx.doi.org/10.13039/501100000266">`<br>`EP/K023195/1`<br>` </rioxxterms:project>`|
| dc.date.issued| rioxxterms:publication_date| 2015-02-15 |`<rioxxterms:publication_date>`<br>`2015-02-15`<br>` </rioxxterms:publication_date>`|
| rioxxterms.type with dc.type fallback| rioxxterms:type| Book|`<rioxxterms:type>`<br>`Book`<br> `</rioxxterms:type>`|  
| rioxxterms.version| rioxxterms:version| AO|`<rioxxterms:version>`<br>`AO`<br> `</rioxxterms:version>`|
| rioxxterms.versionofrecord| rioxxterms:version_of_record|[http://dx.doi.org/10.1006/jmbi.1995.0238](http://dx.doi.org/10.1006/jmbi.1995.0238)|` <rioxxterms:version_of_record>`<br>`http://dx.doi.org/10.1006/jmbi.1995.0238`<br>`</rioxxterms:version_of_record>`|

## RIOXX metadata derived from DSpace Bitstream metadata <a name="RIOXX-metadata-derived"></a>##

Because DSpace supports multiple files per attached metadata record, there is a split between information stored in the metadata record and information stored with the bitstreams.  
For the following three fields, data is retrieved from the bitstream metadata for the bitstream indicated as "primary bitstream". 

| DSpace bitstream | RIOXX element | example DSpace value| example RIOXX value|
|----------------|----------------|----------------------|---------------------|
| format| dc:format| application/pdf|` <dc:format> ` <br> `application/pdf` <br>  `</dc:format>`|
| url| dc:identifier|[https://example.com/dspace/bitstream/123456789/10/1/example.pdf](https://example.com/dspace/bitstream/123456789/10/1/example.pdf)|` <dc:identifier> ` <br> ` https://example.com/dspace/bitstream/123456789/10/1/example.pdf ` <br> ` </dc:identifier>` |
| embargo| ali:free_to_read| 2015-08-27|` <ali:free_to_read start_date="2015-08-27">` <br> ` </ali:free_to_read>` |

The RIOXX patch relies on the activation of the standard DSpace embargo functionality, and will ready the date for ali:free_to_read from the Resource policy set on the bitstream.  
Currently, there is no specific support provided for end_date, assuming that once access is open, there is no specific use case for closing it again.

## dc:source mandatory where applicable <a name="dcsource-mandatory"></a>##

The RIOXX specification states that dc.source is mandatory where applicable. The DSpace RIOXX patch does currently not enforce this: ISSN and ISBN are merely provided in the crosswalk when they are filled out.  
In the standard DSpace submission form, ISBN and ISSN can be provided in a field for identifiers, that has a dropdown where the user first needs to select the identifier type.

If you are primarily collecting materials for which an ISSN applies, it is recommended to use a separate, custom field for ISSN that fills dc.identifier.issn, and make that field mandatory.

## dc:type fallback for rioxxterms:type <a name="dctype-fallback"></a>##

There is a substantial overlap between the vocabulary for rioxxterms:type and the standard list for dc.type. To ensure all of the rioxxterms types are available to your submitters, it is recommended to put a specific rioxxterms.type field in place, that uses the specifc vocabulary.

However, in case rioxxterms.type is absent in your items, the OAI-PMH crosswalk provides a basic mapping between dc.type and rioxxterms:type for those types that can be unambiguously mapped:

|DSpace type | RIOXX type
-------------|------------
| Article| Journal Article/Review
| Book| Book
| Book chapter| Book chapter
| Technical Report| Technical Report
| Thesis| Thesis
| Working Paper| Working paper

## fundref id for funders and orcid id for authors <a name="fundref-id"></a> ##

Fundref DOI's for funders and ORCID id's for authors are NOT stored in the actual metadata value for the fields above. The metadata values only contain the string representations of funders and authors.  
The RIOXX OAI-PMH crosswalks retrieves the ORCIDs for authors and fundref ids for funders from the DSpace SOLR Authority cache. This feature was added in DSpace 5, but was backported to DSpace 3.x and 4.x as part of the RIOXX patch.

Right now, this only affects institutions that use the XMLUI, since the JSPUI has no web UI yet for working with this authority cache. However, JSPUI institutions are still compliant with RIOXX as the string representations of funder and author are included in the RIOXX OAI-PMH crosswalk.


# Patch Installation Procedures <a name="Patch-installation-procedures"></a>#

## Prerequisites  <a name="Prerequisites"></a> ##

The RIOXX application profile has been released as a "patch" for DSpace as this allows for the easiest installation process of the incremental codebase. The code needed to install and deploy the RIOXX Application Profile can be found in the *rioxx_changes.patch* patch file, which needs to be applied to your DSpace source code.

**__Important note__**: Below, we will explain you how to apply the patch to your existing installation. This will affect your source code. Before applying a patch, it is **always** recommended to create backup of your DSpace source code.

In order to apply the patch, you will need to locate the **DSpace source code** on your server. That source code directory contains a directory _dspace_ , as well as the following files:  _LICENSE_,  _NOTICE_ ,  _README_ , ....

For every release of DSpace, generally two release packages are available. One package has "src" in its name and the other one doesn't. The difference is that the release labelled "src" contains ALL of the DSpace source code, while the other release retrieves precompiled packages for specific DSpace artifacts from maven central. **The RIOXX patches were designed to work on both "src" and other release packages of DSpace**. 

To be able to install the patch, you will need the following prerequisites:

* A running DSpace 3.x, 4.x or 5.x instance. 
* Git should be installed on the machine. The patch will be applied using several git commands as indicated in the next section. 

## Obtaining a recent patch file <a name="Obtaining-recent-patch"></a>##

@mire's modifications to a standard DSPace for RIOXX compliance are tracked on Github. The newest patch can therefore be generated from git.

DSPACE 5.0,5.1 [https://github.com/atmire/RIOXX/compare/unmodified...latest.patch](https://github.com/atmire/RIOXX/compare/unmodified...latest.patch)  
DSPACE 5.2 [https://github.com/atmire/RIOXX52/compare/unmodified...latest.patch](https://github.com/atmire/RIOXX52/compare/unmodified...latest.patch)  
DSPACE 5.3 [https://github.com/atmire/RIOXX53/compare/unmodified...latest.patch](https://github.com/atmire/RIOXX53/compare/unmodified...latest.patch)




## Patch installation <a name="Patch-installation"></a>##

To install the patch, the following steps will need to be performed. 

### 1. Go to the DSpace Source directory. <a name="goto-DSpace-Source"></a>###

This folder should have a structure similar to:   
dspace  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;   modules  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    config  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    ...  
pom.xml


### 2. Run the Git command to check whether the patch can be correctly applied. <a name="Run-git-command"></a>###

Run the following command where <patch file> needs to be replaced with the name of the patch:

``` 
git apply --check <patch file>
```

This command will return whether it is possible to apply the patch to your installation. This should pose no problems in case the DSpace is not customized or in case not much customizations are present.   
In case, the check is successful, the patch can be installed as explained in the next steps. 

### 3. Apply the patch <a name="Apply-patch"></a>###

To apply the patch, the following command should be run where <patch file> is replaced with the name of the patch file. 
git apply <patch file>

There may be various warnings about whitespaces, but these will pose no problems when applying the patch and can be ignored. 

### 4. Rebuild and redeploy your repository <a name="Rebuild-redeploy"></a>###

After the patch has been applied, the repository will need to be rebuild.   
DSpace repositories are typically built using the Maven and deployed using Ant. 

Specifically for DSpace 3 and DSpace 4, it is important to know that the database changes to add the rioxx fields to the metadata registry, are called in the "update_registries" ant target.
This ant target is part of "ant update". However, it is not part of "ant fresh_install". 

If you are not seeing the fields in your registry, you can import the rioxx fields manually by executing:

```
dspace/bin/dspace dsrun org.dspace.administer.MetadataImporter -f <dspace.dir>/config/registries/rioxxterms-types.xml -u
``` 
### 5. Restart your tomcat <a name="Restart-tomcat"></a> ###

After the repository has been rebuild and redeployed, the tomcat will need to be restarted to bring the changes to production. 

###6. Populate the RIOXX OAI-PMH end point <a name="Populate-RIOXX"></a>###
 
To Populate the RIOXX end point, used for harvesting, run the following command: 

```
[dspace]/bin/dspace oai import -c
```    

This will Populate the RIOXX OAI endpoint that will be available on 

```
<server-url>/oai/rioxx?verb=ListRecords&metadataPrefix=rioxx
```

If you want to avoid multiple manual executions of this script during testing, you can always add it to your scheduled tasks (crontab), and have it execute every hour or every 15 minutes.  
Do note that the more items your repository contains, the more resource intensive this task is. Be careful scheduling this task frequently on production systems! On production systems we still highly recommend a daily frequency.

### 7. XMLUI only: Load Fundref authority data <a name="XMLUI-only"></a>###

DSpace 5 comes with a new SOLR based infrastructure for authority control, originally used for storing authority data from ORCID. For RIOXX, this infrastructure was used to hold Fundref authority data.  
Even though the SOLR core with authority data can be enabled for JSPUI, there is no support yet for lookup in this registry through the submission forms in JSPUI.

As the source, DSpace relies on the RDF file published by Crossref at:  
[http://dx.doi.org/10.13039/fundref_registry](http://dx.doi.org/10.13039/fundref_registry)

More information about this file is available at:  
[http://help.crossref.org/fundref-registry](http://help.crossref.org/fundref-registry)

Download this file to your DSpace server.

The "PopulateFunderAuthorityFromXML" script will add new funders as authorities for inclusion in rioxxterms.project, where funder and project id are exposed.  
If you are executing the script for the first time, your SOLR authority cache will be loaded with all funders present in the fundref export.  
After that, you can use the same script when there is a new release of the fundref export. In this case, both new funders will be added and information from previously added funders will be updated.

To run the script:

```
./dspace dsrun org.dspace.scripts.PopulateFunderAuthorityFromXML -f {funder-authority-rdf}
```

arguments:  
-f: The RDF XML file containing the funder authorities  
-t: Test if the script works correctly. No changes will be applied.

## Configure Submission forms or other metadata ingest mechanisms <a name="Configure-submission"></a>##

Now that the new fields are present in your metadata schema's, you have to ensure that these fields can be filled. If your institution is relying on manual entry using the DSpace submission forms, you can go over the template input-forms.xml file on Github to see how the different new RIOXX fields can be included:

[https://github.com/atmire/RIOXX/compare/rc1...master](https://github.com/atmire/RIOXX/compare/rc1...master)

If you are relying on automated ingests using SWORD or integrations with your CRIS system, you will likely need to customize the mapping and integration with those systems. This is beyond the scope of the patch and this documentation.

Note that simply adding the new RIOXX fields to the existing DSpace fields may create confusion for your end users. For example, the DSpace default "sponsor" field is similar to the RIOXX specific project and funder linking. Likewise, the "File Description" field that DSpace offers in the file upload dialog, has a similar purpose than the RIOXX "version" field. It is recommended to go over your submission forms entirely to verify that it is clear for your end users which fields are used for which purpose. Possibly, you may want to remove or repurpose existing DSpace default fields.

# Verification <a name="Verification"></a> #

## RIOXX Metadata Registry <a name="RIOXX-metadata-registry"></a>##

As an administrator, navigate to the standard DSpace administrator page "Registries >> Metadata".  
On this page, you should be able to see the new RIOXX metadata schema. When clicking on the link, you should see the different fields in the metadata schema. This new registry shouldn't be empty.

## Submission forms based on @mire template <a name="Submission-forms-template"></a>##

This verification assumes that you have modified your input-forms.xml based on @mire's template on Github:

[https://github.com/atmire/RIOXX/compare/rc1...master](https://github.com/atmire/RIOXX/compare/rc1...master)

Start the submission of a new item in a DSpace collection that uses our custom submission form config.  
After the collection selection, a custom step is included to support adding multiple funders and project IDs. In this step you should be able to add this field:

* rioxxterms:project: Funder lookup and project field

in the first screen of the next step, you should be able to find following new fields:

* ali:license_ref: license URI and License start date    
     *   The RIOXX spec supports the provision of multiple license ref's and dates. In DSpace, we are currently only supporting a single license URL and a single date. If multiple usage licenses apply, it is recommended to pick the most open one.   
* dcterms.dateAccepted
* rioxxterms:version
* rioxxterms:version_of_record (DOI)

Note that the template input-forms.xml does not add every single field defined in RIOXX. For many of the fields declared as optional, you will need to modify the submission forms yourself. 
The standard DSpace submission forms already have an excess of different fields, this is why not all RIOXX optional fields were added by default. Even though these fields are not yet in the submission form they ARE being taken into account for the RIOXX OAI-PMH mapping. Please refer to the documentation of the mapping before enabling these fields in the submission form.

Following fields have to be included manually in the submission forms:

* dc:coverage
* dc:relation
* rioxxterms:apc

Continue the submission and don't forget to attach a file in order to create your first RIOXX test item and verify that it is completely "archived" in the repository. You can check this by verifying if the item now appears in the list of "Recent Submissions" on the repository homepage.

## OAI-PMH endpoint <a name="OAI-PMH-endpoint"></a>##

Immediately after a new test item is available in the repository, it is NOT YET available in your OAI-PMH SOLR index.  
Normally, you have a nightly scheduled task (cron job) that synchronizes the archived items in the repository, with the OAI-PMH index.

For your testing purposes, you will want to verify new test items immediately. To do this, you need to manually trigger the OAI indexing task that populates the RIOXX OAI-PMH endpoint, as described in step 6 of the installation process.

After you have done this, you should be able to see your newly archived RIOXX test item through the link:

```
<server-url>/oai/rioxx?verb=ListRecords&metadataPrefix=rioxx
```

If you don't see your item there, check the corresponding troubleshooting section below.

**Rioxxterms:project**

There is a discrepancy between the examples listed in http://rioxx.net/v2-0-final/ and with the XSD definition for the exposure of funder_name and funder_id at [http://rioxx.net/schema/v2.0/rioxx/rioxxterms_.html#project](http://rioxx.net/schema/v2.0/rioxx/rioxxterms_.html#project)

In the DSpace RIOXX OAI-PMH endpoint, we have chosen to follow the XSD and to expose the rioxxterms: namespace for the funder_name and funder_id attributes.

# Troubleshooting <a name="Troubleshooting"></a>#

## Errors during the Patch Installation process <a name="Errors-patch-installation"></a>##

If you are receiving errors similar to the message below, then you are most likely using the **wrong directory** (i.e., not the parent directory of your DSpace installation). Please make sure that the current directory contains a directory called "dspace", which contains (amongst others) the subdirectories "bin", "config", "modules" and "solr". If this is not the case, then you will most probably receive errors such as:


```
error: dspace/config/crosswalks/oai/xoai.xml: No such file or directory   
...
```


Another problem could occur if the DSpace installation has been customized in such a way that the RIOXX Application Profile patch cannot be applied with creating versioning conflicts with your own customizations. This will trigger errors similar to the message below:

    
```
...             
error: patch failed: dspace/pom.xml:62          
error: dspace/pom.xml: patch does not apply            
...                
```


## RIOXX test items are not visible in OAI-PMH endpoint <a name="RIOXX-test-OAI-PMH-endpoint"></a> ##

The RIOXX OAI-PMH endpoint has been developed in such a way that it only exposes items that are RIOXX compliant. An item will not appear there as long as not all of the following mandatory fields are present in the item:

* ali:license_ref
* dc:identifier that directly links to the attached bitstream
* dc:language
* dc:title
* dcterms:dateAccepted
* rioxxterms:author
* rioxxterms:project
* rioxxterms:type
* rioxxterms:version

According to the specification, dc.source is mandatory where applicable (ISSN or ISBN). Currently, DSpace is not enforcing this in the OAI-PMH endpoint and will just expose ISSN or ISBN when they are present in the metadata.   
Again, aside from these metadatafields, make sure that the item contains a bitstream (file). Metadata records without bitstreams will not be exposed through the RIOXX OAI-PMH endpoint.
