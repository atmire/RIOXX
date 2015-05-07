package com.atmire.scripts;

import com.atmire.authority.IndexingUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.kernel.ServiceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 23 Apr 2015
 */
public abstract class PopulateAuthorityFromXML<T extends AuthorityValue> extends Script {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(PopulateAuthorityFromXML.class);

    protected File file;
    protected List<T> validAuthorityValues;
    protected List<T> invalidAuthorityValues;
    protected List<T> newAuthorityValues;
    protected List<T> updatedAuhtorityValues;
    protected List<T> unchangedAuhtorityValues;
    protected boolean test = false;


    @Override
    protected Options createCommandLineOptions() {
        Options options = new Options();

        OptionBuilder.withDescription("path to the xml file");
        OptionBuilder.withLongOpt("file");
        OptionBuilder.hasArg(true);
        OptionBuilder.isRequired(true);
        options.addOption(OptionBuilder.create("f"));

        OptionBuilder.withDescription("test run, doesn't apply any changes");
        OptionBuilder.withLongOpt("test");
        OptionBuilder.hasArg(false);
        OptionBuilder.isRequired(false);
        options.addOption(OptionBuilder.create("t"));

        return options;
    }


    @Override
    protected int processLine(CommandLine line) {
        int status = super.processLine(line);
        if (status == 0) {
            // other arguments
            if (line.hasOption("f")) {
                String filePath = line.getOptionValue('f');
                boolean fileOK = verifyFile(filePath);
                if (!fileOK) {
                    status = 1;
                }
            } else {
                status = 1;
            }
            boolean isTest = line.hasOption('t');
            setTest(isTest);
        }
        return status;
    }

    @Override
    public void run() throws Exception {
        if (fileOK(file)) {
            validAuthorityValues = null;
            try {
                parseXML(file);
                ServiceManager serviceManager = IndexingUtils.getServiceManager();
                AuthorityIndexingService indexingService = IndexingUtils.getIndexingService(serviceManager);

                newAuthorityValues = new ArrayList<T>();
                updatedAuhtorityValues = new ArrayList<T>();
                unchangedAuhtorityValues = new ArrayList<T>();

                for (T value : validAuthorityValues) {
                    T toIndex = valueToIndex(indexingService, value);
                    if (!isTest() && toIndex != null) {
                        indexingService.indexContent(toIndex, true);
                    }
                }
            if (!isTest()) {
                indexingService.commit();
            }

                hookAfterIndexing();
            } catch (Exception e) {
                log.error("Error", e);
            }
        }
    }

    protected abstract T valueToIndex(AuthorityIndexingService indexingService, T value);

    protected abstract void parseXML(File file);

    protected void hookAfterIndexing() {
        String firstLine = "Import report:";
        String imported = "Number of new authorities imported: ";
        String updated = "Number of authorities updated: ";
        String unchanged = "Number of authorities up to date (unchanged): ";
        String notImported = "Number of invalid authorities (not imported):";

        if (isTest()) {
            firstLine = "Test run report:";
            imported = "Number of new authorities that would be imported: ";
            updated = "Number of authorities that would be updated: ";
            unchanged = "Number of authorities that would be up to date (unchanged): ";
            notImported = "Number of invalid authorities (would not be imported):";
        }

        imported += newAuthorityValues.size();
        updated += updatedAuhtorityValues.size();
        unchanged += unchangedAuhtorityValues.size();
        notImported += invalidAuthorityValues.size();

        print(firstLine);
        print(imported);
        print(updated);
        print(unchanged);
        print(notImported);
    }


    public boolean verifyFile(String filePath) {
        if (!fileOK(file) && filePath != null) {
            file = new File(filePath);
        }
        return fileOK(file);

    }

    protected boolean fileOK(File file) {
        return file != null && file.exists() && file.isFile();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}
