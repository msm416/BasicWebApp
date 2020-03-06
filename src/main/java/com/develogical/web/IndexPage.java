package com.develogical.web;

import java.io.PrintWriter;

public class IndexPage extends HtmlPage {

    @Override
    protected void writeContentTo(PrintWriter writer) {
        writer.println(
                "<h1>Search for recipe</h1>" +
                        "<form><input type=\"text\" name=\"q\" />" +
                        "<input type=\"submit\">" +
                        "</form>");
    }
    
}
