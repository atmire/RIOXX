package org.dspace.scripts;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.ArrayUtils;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 21 May 2014
 */
public class ItemIteratorScript extends ContextScript {


    private String[] includes;
    private String[] excludes;

    public ItemIteratorScript(Context context) {
        super(context);
    }

    public ItemIteratorScript() {
    }

    public void run() throws Exception {
        print("ItemIteratorScript initializing...");
        try {
            ItemIterator itemIterator = getItemIterator();
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();
                print(item.getHandle() + "\t\t" + item.getName());
            }
        } catch (Exception e) {
            printAndLogError(e);
        }
        print("ItemIteratorScript done.");
    }

    public static void main(String[] args) {
        Script Script = new ItemIteratorScript();
        Script.mainImpl(args);
    }

    @Override
    protected int processLine(CommandLine line) {
        int status = super.processLine(line);
        if (status == 0) {
            // other arguments
            if (line.hasOption('i')) {
                setIncludes(line.getOptionValues('i'));
            }
            if (line.hasOption('o')) {
                setExcludes(line.getOptionValues('o'));
            }
        }
        return status;
    }

    protected Options createCommandLineOptions() {
        Options options = super.createCommandLineOptions();
        options.addOption("i", "item", true, "Items, collections, communities over which to iterate");
        options.addOption("o", "omit", true, "Items, collections, communities to exclude");
        return options;
    }

    protected ItemIterator getItemIterator() throws SQLException {
        ItemIterator items = null;

        if (ArrayUtils.isNotEmpty(includes)) {
            Set<Integer> ids = getItemIDs(includes);
            items = new ItemIterator(context, new ArrayList<Integer>(ids));
        }

        if (ArrayUtils.isEmpty(includes) || items == null || !items.hasNext()) {
            items = Item.findAll(context);
        }

        if (ArrayUtils.isNotEmpty(excludes)) {
            Set<Integer> excludedIDs = getItemIDs(excludes);
            Set<Integer> ids = new HashSet<Integer>();
            while (items.hasNext()) {
                Item item = items.next();
                int id = item.getID();
                if (excludedIDs == null || !excludedIDs.contains(id)) {
                    ids.add(id);
                }
            }
            items = new ItemIterator(context, new ArrayList<Integer>(ids));
        }
        return items;
    }

    /**
     * Returns a set of all item IDs present in "includes"
     * and all items from a collection or community present in "includes".
     *
     * @param includes An array of handles or item IDs
     * @throws java.sql.SQLException
     */
    protected Set<Integer> getItemIDs(String[] includes) throws SQLException {
        Set<Integer> ids = new HashSet<Integer>();
        if (includes != null) {
            for (String handle : includes) {
                if (handle.contains("/")) {
                    DSpaceObject dSpaceObject = HandleManager.resolveToObject(context, handle);
                    if (dSpaceObject instanceof Item) {
                        ids.add(dSpaceObject.getID());
                    } else if (dSpaceObject instanceof Collection) {
                        Collection collection = (Collection) dSpaceObject;
                        addCollection(ids, collection);
                    } else if (dSpaceObject instanceof Community) {
                        Community community = (Community) dSpaceObject;
                        addCommunity(ids, community);
                    } else {
                        print(handle + " could not be resolved to an item, collection or community");
                    }
                } else {
                    // not a handle, maybe an internal ID
                    Item item = null;
                    try {
                        item = Item.find(context, Integer.valueOf(handle));
                    } catch (NumberFormatException e) {
                        print(handle + " could not be resolved to an item");
                    }

                    if (item != null) {
                        ids.add(item.getID());
                    }
                }
            }
        }
        return ids;
    }

    protected void addCommunity(Set<Integer> ids, Community community) throws SQLException {
        Community[] subcommunities = community.getSubcommunities();
        for (Community subcommunity : subcommunities) {
            addCommunity(ids, subcommunity);
        }
        Collection[] collections = community.getCollections();
        for (Collection collection : collections) {
            addCollection(ids, collection);
        }
    }

    protected void addCollection(Set<Integer> ids, Collection collection) throws SQLException {
        ItemIterator collectionItems = collection.getItems();
        while (collectionItems.hasNext()) {
            Item item = collectionItems.next();
            ids.add(item.getID());
        }
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }
}
