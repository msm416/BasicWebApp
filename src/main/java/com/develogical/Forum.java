package com.develogical;

public class Forum {
    private final DBController dbController;

    public Forum(DBController dbController) {
        this.dbController = dbController;
    }

    String lookupArticle(String input) {
        if(input.contains("how to")) {
            // Recipe is searched in the db
            dbController.lookup(input);
        }

        // We will
        return "Only recipe lookups available.";
    }
}
