package com.github.kolandroid.kol.model.models;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.model.Model;
import com.github.kolandroid.kol.request.Request;
import com.github.kolandroid.kol.util.Logger;
import com.github.kolandroid.kol.util.Regex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class WebModel extends Model {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -1723871679147309734L;

    /**
     * Determine if a URL actually points to KoL
     */
    private static final Regex URL_FIND = new Regex(
            "^https?://www(\\d*).kingdomofloathing.com/(.*)$", 2);

    private static final Regex URL_BASE_FIND = new Regex(
            "^(?:.*/)?([^/?]*)(?:\\?.*)?$", 1);

    /**
     * Regexes for fixing item descriptions.
     */
    private static final Regex ITEM_DESC = new Regex(
            "<img[^>]*descitem\\((\\d+)(, event)?\\)[^>]*>");
    private static final Regex ITEM_WHICH_DESC = new Regex(
            "<img[^>]*descitem\\((\\d+),(\\d+)(, event)?\\)[^>]*>");

    /**
     * Regexes for fixing effect descriptions.
     */
    /* private static final Regex EFFECT_DESC = new Regex(
            "(<img[^>]*)on[Cc]lick=[\"']?eff\\([\"']?(.*?)[\"']?\\);?[\"']?([^>]*>)", 0);
    */
    private static final Regex EFFECT_DESC = new Regex(
            "<img[^>]*eff\\([\"']?(.*?)[\"']?\\)[^>]*>");

    /**
     * Regexes for replacing static buttons.
     */
    private static final Regex FIND_FORM = new Regex(
            "<form[^>]*>(<input[^>]*type=[\"']?hidden[^>]*>)*<input[^>]*button[^>]*></form>",
            0);
    private static final Regex FORM_ACTION = new Regex(
            "<form[^>]*action=[\"']?([^\"'> ]*)[\"'> ]", 1);
    private static final Regex HIDDEN_INPUT = new Regex(
            "<input[^>]*type=[\"']?hidden[^>]*>", 0);
    private static final Regex GET_NAME = new Regex(
            "name=[\"']?([^\"'> ]*)[\"'> ]", 1);
    private static final Regex GET_VALUE = new Regex(
            "value=[\"']?([^\"'> ]*)[\"'> ]", 1);

    private static final Regex INPUT_BUTTON = new Regex(
            "<input[^>]*button[^>]*>", 0);
    private static final Regex GET_TEXT = new Regex("value=\"([^>]*)\">", 1);

    private static final Regex FORM_REPLACER = new Regex("<form([^>]*)action=([\"']?[^\"' >]*[\"']?)([^>]*)>");
    private static final Regex FORM_REPLACER2 = new Regex("<form([^>]*)method=[\"']?[^\"' >]*[\"']?([^>]*)>");

    private static final Regex TABLE_FIXER = new Regex("(</td>)(.*?)(</td>|</tr>|</table>|<td[^>]*>)");
    /**
     * Remove code which redirects when no frames are detected.
     */
    private static final Regex FRAME_REDIRECT = new Regex("if\\s*\\(parent\\.frames\\.length\\s*==\\s*0\\)\\s*location.href\\s*=\\s*[\"']?game\\.php[\"']?;", 0);
    private static final Regex HEAD_TAG = new Regex("<head>");
    private final static String jsInjectCode = "" +
            "function customParseForm(form) { " +
                    "    var inputs = form.getElementsByTagName('input');" +
                    "    var data = form.totallyrealaction ? form.totallyrealaction.value : '';" +
                    "    var tobegin = (data.indexOf('?') == -1);" +
                    "    for (var i = 0; i < inputs.length; i++) {" +
                    "         var field = inputs[i];" +
                    "         if(field.name && field.name==='totallyrealaction') continue; " +
                    "         if(field.type == 'radio' && !field.checked) continue; " +
                    "         if(field.type == 'checkbox' && !field.checked) continue; " +
                    "         if (field.type != 'reset' && field.name) {" +
                    "             data += (tobegin ? '?' : '&');" +
                    "             tobegin = false;" +
                    "             data += encodeURIComponent(field.name) + '=' + encodeURIComponent(field.value);" +
                    "         }" +
                    "    }" +
                    "    var select = form.getElementsByTagName('select');" +
                    "    for (var i = 0; i < select.length; i++) {" +
                    "         var field = select[i];" +
                    "         data += (tobegin ? '?' : '&');" +
                    "         tobegin = false;" +
                    "         data += encodeURIComponent(field.name) + '=' + encodeURIComponent(field.options[field.selectedIndex].value);" +
                    "    }" +
                    "    window.ANDROIDAPP.processFormData(data);" +
            "}\n" +
            "function pop_query(caller, title, button, callback, def) { " +
            "    window.querycallback = callback;" +
            "    window.ANDROIDAPP.displayFormNumeric(title, \"javascript:window.querycallback(#VAL)\");" +
                    "}";

    private static final Regex POPQUERY_SCRIPT = new Regex("<script[^>]*pop_query[^>]*></script>");

    // Regex to find the top results pane of any page
    private static final Regex RESULTS_PANE = new Regex(
            "<table[^>]*><tr><td[^>]*><b>Results:.*?(<center>.*?)</table>", 1);
    // Regex to find contents of the <body> tag of any page
    private static final Regex PAGE_BODY = new Regex(
            "(<body[^>]*>)(.*?)(</body>)", 2);
    private static final Regex TYPE_EXTRACTION = new Regex("[&?]androiddisplay=([^&]*)", 1);
    private final String url;
    private final WebModelType type;
    private String html;

    public WebModel(Session s, ServerReply text, WebModelType type) {
        super(s);

        Logger.log("WebModel", "Loaded " + text.url);

        this.setHTML(text.html.replace("window.devicePixelRatio >= 2", "window.devicePixelRatio < 2"));
        this.url = text.url;
        this.type = type;
    }

    public WebModel(Session s, ServerReply text) {
        this(s, text, determineType(text));
    }

    private static WebModelType determineType(ServerReply text) {
        String specified_type = TYPE_EXTRACTION.extractSingle(text.url, "unspecified");
        for (WebModelType type : WebModelType.values()) {
            if (specified_type.equals(type.toString()))
                return type;
        }

        if (text.url.contains("desc_item.php"))
            return WebModelType.SMALL;
        if (text.url.contains("desc_effect.php"))
            return WebModelType.SMALL;
        return WebModelType.REGULAR;
    }

    /**
     * Extract a model for the results pane of this page, if any exists.
     *
     * @param s    Session in which to create the new model.
     * @param base Page to parse
     * @return A model representing the results pane; null if no results pane
     * was found.
     */
    public static WebModel extractResultsPane(Session s, ServerReply base) {
        String resultsPane = RESULTS_PANE.extractSingle(base.html);

        if (resultsPane == null)
            return null;

        Logger.log("WebModel", "Loaded results pane: " + prepareHtml(resultsPane));
        String html = PAGE_BODY.replaceAll(base.html, "$1<center>"
                + resultsPane + "</center>$3");

        String updatedUrl = base.url;
        updatedUrl += (base.url.contains("?")) ? "&androiddisplay=results" : "?androiddisplay=results";

        ServerReply newRep = new ServerReply(base.responseCode,
                base.redirectLocation, base.date, html,
                updatedUrl, base.cookie);
        return new WebModel(s, newRep);
    }

    private static String prepareHtml(String html) {
        html = fixItemsAndEffects(html);
        html = injectJavascript(html);
        html = doHacks(html);
        html = fixPaneReferences(html);
        return html;
    }

    private static String fixItemsAndEffects(String html) {
        // Replace item description javascript with working html links
        html = ITEM_DESC.replaceAll(html,
                "<a href=\"desc_item.php?whichitem=$1\">$0</a>");
        html = ITEM_WHICH_DESC.replaceAll(html,
                "<a href=\"desc_item.php?whichitem=$1&otherplayer=$2\">$0</a>");
        html = EFFECT_DESC.replaceAll(html,
                "<a href=\"desc_effect.php?whicheffect=$1\">$0</a>");

        return html;
    }

    private static String fixPaneReferences(String html) {
        html = FRAME_REDIRECT.replaceAll(html, "");
        html = html.replace("top.charpane.location.href=\"charpane.php\";", "window.ANDROIDAPP.refreshStatsPane();");
        html = html.replace("top.mainpane.document", "document");
        html = html.replace("parent.mainpane", "window");
        return html;
    }

    private static String doHacks(String html) {
        /**
         * Hacks for account.php
         */
        /*
        //stop removing the submit button on account.php
		html = html.replace("document.write('<style type=\"text/css\">#submit {display: none; }</style>');", "");
		//remove all the blue "Saving..." text on account.php
		html = html.replace("<span class=\"saving\">Saving...</span>", "");
		//remove the fancy tab ajax calls on account.php; they do not have the proper cookie
		html = html.replace("$('#tabs li').click(changeTab);", "");
		*/
        return html;
    }

    private static String injectJavascript(String html) {
        html = FORM_REPLACER.replaceAll(html, "<form$1$3><input type=hidden name=totallyrealaction value=$2>");
        html = FORM_REPLACER2.replaceAll(html, "<form$1action=\"\" onsubmit=\"customParseForm(this);\"$2>");

        html = TABLE_FIXER.replaceAll(html, "$1$3$2");
        html = HEAD_TAG.replaceAll(html, "$0 <script>" + jsInjectCode + "</script>");

        //pop_query(...) is replaced by an injected function to interact with android
        html = POPQUERY_SCRIPT.replaceAll(html, "");
        html = html.replace("Right-Click to Multi-Buy", "Long-Press to Multi-Buy");

        return html;
    }

    public String getURL() {
        return this.url;
    }

    public final String getHTML() {
        return this.html;
    }

    private void setHTML(String html) {
        this.html = prepareHtml(html);
    }

    public <E> E visitType(WebModelTypeVisitor<E> visitor) {
        return type.visit(visitor);
    }

    public boolean makeRequest(String url) {
        if (url == null || url.length() < 1) return false;

        String originalUrl = url;

        if (url.contains("totallyrealaction")) {
            System.out.println("Ignoring duplicate form request");
            return true;
        }

        if (url.contains("http://") || url.contains("https://")) {
            url = URL_FIND.extractSingle(url);
            if (url == null) {
                Logger.log("WebModel", "Unable to load url from " + originalUrl);
                return false;
            }
        }

        if (url.charAt(0) == '?') {
            String currentBase = URL_BASE_FIND.extractSingle(this.url);
            if (currentBase == null) currentBase = "main.php";
            url = currentBase + url;
        }

        Logger.log("WebModel", "Request started for " + url);
        Request req = new Request(url);
        this.makeRequest(req);
        return true;
    }

    public InputStream makeBlockingRequest(String url) {
        url = url.replace("http://www.kingdomofloathing.com/", "");
        url = url.replace("www.kingdomofloathing.com/", "");

        Request req = new Request(url);
        ServerReply result = this.makeBlockingRequest(req);

        String html_result;
        if (result == null) {
            Logger.log("WebModel", "[AJAX] Error loading " + url);
            html_result = "";
        } else {
            Logger.log("WebModel", "[AJAX] Loaded " + url + " : " + prepareHtml(result.html));
            html_result = result.html;
        }

        html_result = prepareHtml(html_result);

        try {
            return new ByteArrayInputStream(html_result.getBytes("UTF-8"));
        } catch (IOException e) {
            Logger.log("WebModel", "Unable to encode as UTF-8");
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            };
        }
    }

    public enum WebModelType {
        REGULAR("regular") {
            public <E> E visit(WebModelTypeVisitor<E> visitor) {
                return visitor.forRegular();
            }
        }, SMALL("small") {
            public <E> E visit(WebModelTypeVisitor<E> visitor) {
                return visitor.forSmall();
            }
        }, RESULTS("results") {
            public <E> E visit(WebModelTypeVisitor<E> visitor) {
                return visitor.forResults();
            }
        };

        private final String value;

        WebModelType(String value) {
            this.value = value;
        }

        public abstract <E> E visit(WebModelTypeVisitor<E> visitor);

        @Override
        public String toString() {
            return value;
        }
    }

    public interface WebModelTypeVisitor<E> {
        E forRegular();

        E forSmall();

        E forResults();
    }
}
