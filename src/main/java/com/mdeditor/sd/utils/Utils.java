package com.mdeditor.sd.utils;
import com.mdeditor.sd.block.Block;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Utils {
    private static final String style;
    private static final Parser flexmarkParser;
    private static final HtmlRenderer flexHtmlRenderer;

    private Utils(){
        throw new IllegalStateException("Utility class");
    }

    static{
        style = readCss();
        MutableDataSet flexmarkOptions = new MutableDataSet().set(Parser.EXTENSIONS, Arrays.asList(
                        StrikethroughExtension.create(),
                        TablesExtension.create(),
                        TaskListExtension.create()
                ))
                .set(TablesExtension.MIN_SEPARATOR_DASHES, 1)
                .set(StrikethroughExtension.STRIKETHROUGH_STYLE_HTML_OPEN, "<s>")
                .set(StrikethroughExtension.STRIKETHROUGH_STYLE_HTML_CLOSE, "</s>");
        flexmarkParser = Parser.builder(flexmarkOptions).build();
        flexHtmlRenderer = HtmlRenderer.builder(flexmarkOptions).build();
    }

    /**
     * Read CSS file from resource folder and save to static variable.
     */
    public static String readCss(){
        InputStream cssStream = Utils.class.getClassLoader().getResourceAsStream("editor/github-markdown-light.css");
        if(cssStream == null) return "";

        try{
            String cssContent = new String(cssStream.readAllBytes());
            return Utils.wrapWithHtmlTag("style", cssContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wrap content with input tag.
     * @param tag HTML tag you want to wrap
     * @param content what you want to cover
     * @return wrapped content with input tag as HTML format.
     */
    public static String wrapWithHtmlTag(String tag, String content){
        return "<" + tag + ">" + content + "</" + tag + ">";
    }

    /**
     * Returns the html-rendered text
     * @param str which is stored in Block, Markdown Text.
     * @return String that has string of html Rendered text.
     */
    public static String stringToHtml(String str){
        Node parseResult = flexmarkParser.parse(str);
        return flexHtmlRenderer.render(parseResult);
    }

    /**
     * Returns the html-rendered and css-applied html format text.
     * @param string which is stored in Block, Markdown Text.
     * @return String that has string of html and CSS Rendered text.
     */
    public static String stringToHtmlWithCss(String string){
        Document doc = Jsoup.parse(stringToHtml(string));
        doc.head().html(style);
        return doc.outerHtml();
    }

    /**
     * Parses the input Markdown text and generates a Flexmark AST (Abstract Syntax Tree) node.
     */
    public static Node flexmarkParse(String input){
        return flexmarkParser.parse(input);
    }

    /**
     * Renders the HTML representation of a Flexmark AST (Abstract Syntax Tree) node.
     */
    public static String flexmarkHtmlRender(Node node){
        return flexHtmlRenderer.render(node);
    }

    /**
     * @param block block onFocus
     * @param whichLine which line of prefix
     * @return prefix
     */
    public static String getPrefix(Block block, int whichLine){
        String curLine = block.getMdText().split("\n")[whichLine];
        int start = block.getIndentAtLine(whichLine);
        int end = curLine.indexOf(" ", start);
        if (end == -1) end = curLine.length();
        String prefix = curLine.substring(start, end);
        if(prefix.equals(">") || prefix.equals("-") || prefix.equals("+")|| prefix.equals("*") || prefix.equals("|")) return prefix;
        else if (prefix.endsWith(".")){
            try {
                Integer.parseInt(prefix.substring(0, prefix.length() - 1));
                return prefix;
            } catch (NumberFormatException e) {
                return "";
            }
        }
        else return "";
    }

    /**
     * Determine if input pre is ordered list or not.
     * @return true if input is ordered list, false otherwise.
     */
    public static boolean isOL(String pre){
        if(pre.endsWith(".")){
            return pre.substring(0, pre.indexOf(".")).matches("\\d+");
        }

        return false;
    }

    /**
     * Determine if input block is multiline block or not.
     * @return true if input block's content can be multiline block, false otherwise.
     */
    public static boolean isBlockStringMultiline(Block block){
        String temp = block.getText();
        int start = block.getIndentAtLine(0);
        int end = temp.indexOf(" ", start);
        if (end == -1) return false;

        String prefix = temp.substring(start, end);
        if(prefix.equals(">") || prefix.equals("-") || prefix.equals("+")|| prefix.equals("*") || prefix.equals("|")) return true;
        else if (prefix.endsWith(".")){
            try {
                Integer.parseInt(prefix.substring(0, prefix.length() - 1));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        else return false;
    }
}