package com.github.kolandroid.kol.model.models;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.gamehandler.ViewContext;
import com.github.kolandroid.kol.model.LiveModel;
import com.github.kolandroid.kol.model.elements.ActionElement;
import com.github.kolandroid.kol.model.models.chat.ChatModel;
import com.github.kolandroid.kol.request.Request;
import com.github.kolandroid.kol.request.ResponseHandler;
import com.github.kolandroid.kol.util.Logger;
import com.github.kolandroid.kol.util.Regex;

import java.util.ArrayList;

public class NavigationModel extends LiveModel {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 1037526493595536003L;
    private final static Regex AWESOME_MENU_ITEM = new Regex("<div class=\"ai\".*?</div>", 0);
    private final static Regex AWESOME_MENU_ITEM_LINK = new Regex("<a[^>]*href=[\"'](.*?)[\"'][^>]*?>.*?</a>", 1);
    private final static Regex AWESOME_MENU_ITEM_CHATACTION = new Regex("<a[^>]*rel=[\"'](.*?)[\"'][^>]*>.*?</a>", 1);
    private final static Regex AWESOME_MENU_ITEM_NAME = new Regex("<img[^>]*alt=[\"'](.*?)[\"'][^>]*>", 1);
    private final static Regex AWESOME_MENU_ITEM_IMAGE = new Regex("<img[^>]*src=[\"'](.*?)[\"'][^>]*>", 1);
    private final static Regex PLAYERID = new Regex("playerid=(\\d+)", 1);
    private final static Regex PWDHASH = new Regex("pwd=([0-9a-fA-F]+)", 1);
    private static final Regex MACRO_RESPONSE = new Regex("<font.*?</font>", 0);
    private static final Regex MACRO_RESPONSE_TEXT = new Regex("<font[^>]*>(.*?)(<!--.*?)?</font>", 1);
    private static final Regex MACRO_RESPONSE_ACTION = new Regex("<!--js\\((.*?)\\)-->", 1);
    private static final Regex MACRO_ACTION_REDIRECT = new Regex("top.mainpane.location.href='(.*?)'", 1);
    private static final Regex MACRO_ACTION_GET_RESULTS = new Regex("dojax\\('(.*?)'\\);", 1);
    private static final Regex MACRO_ACTION_EXAMINE = new Regex("descitem\\((\\d+)\\)", 1);
    private final ArrayList<ActionElement> locations;

    public NavigationModel(Session session) {
        super(session, "topmenu.php", false);

        this.locations = new ArrayList<>();
        loadContent(null);
    }

    public ArrayList<ActionElement> getLocations() {
        this.access();
        return new ArrayList<>(this.locations);
    }

    @Override
    protected boolean canHandle(String url) {
        if (url.contains("awesomemenu.php")) {
            setUpdateUrl("awesomemenu.php");
            return true;
        }

        if (url.contains("topmenu.php")) {
            setUpdateUrl("topmenu.php");
            return true;
        }

        return false;
    }

    @Override
    protected void loadContent(ServerReply content) {
        this.locations.clear();

        if (content != null) {
            if (content.url.contains("topmenu.php")) {
                Logger.log("NavigationModel", "topmenu.php update received");
                loadTopMenuContent(content);
            } else if (content.url.contains("awesomemenu.php")) {
                Logger.log("NavigationModel", "awesomemenu.php update received");
                loadAwesomeMenuContent(content);
            }
        }

        fillRequired();
    }

    private void includeRequired(ActionElement required, boolean addToTop) {
        //Attempt to find an equivalent element in the list of current items
        for (ActionElement added : locations) {
            if (added.urlMatches(required))
                return;
        }

        //If it is not found, add it to the list
        if (addToTop)
            locations.add(0, required);
        else
            locations.add(required);
    }

    private void fillRequired() {
        //Items to be added to the top of the list
        includeRequired(new ActionElement(getSession(), "Main Map",
                "http://images.kingdomofloathing.com/itemimages/map.gif",
                "main.php"), true);
        includeRequired(new ActionElement(getSession(), "Inventory",
                "http://images.kingdomofloathing.com/itemimages/backpack.gif",
                "inventory.php"), true);
        includeRequired(new ActionElement(getSession(), "Skills",
                "http://images.kingdomofloathing.com/itemimages/book3.gif",
                "skillz.php"), true);
        includeRequired(new ActionElement(getSession(), "Crafting",
                "http://images.kingdomofloathing.com/itemimages/pliers.gif",
                "craft.php"), true);

        //Items to be added to the bottom of the list
        includeRequired(new ActionElement(getSession(), "Help",
                "http://images.kingdomofloathing.com/itemimages/help.gif",
                "doc.php?topic=home"), false);
        includeRequired(new ActionElement(getSession(), "Report Bug",
                "http://images.kingdomofloathing.com/itemimages/beetle.gif",
                "adminmail.php"), false);
        includeRequired(new ActionElement(getSession(), "Logout",
                "http://images.kingdomofloathing.com/itemimages/sleepy.gif",
                "logout.php"), false);
        includeRequired(new ActionElement(getSession(), "Options",
                "http://images.kingdomofloathing.com/itemimages/blackwrench.gif",
                "account.php"), false);
    }

    private void loadAwesomeMenuContent(ServerReply response) {
        String playerid = PLAYERID.extractSingle(response.html, "0");
        String pwd = PWDHASH.extractSingle(response.html, "0");
        String chatBase = "submitnewchat.php?playerid=%s&pwd=%s&graf=%s&cli=1";

        for (String menuItem : AWESOME_MENU_ITEM.extractAllSingle(response.html)) {
            String link = AWESOME_MENU_ITEM_LINK.extractSingle(menuItem, "");
            String name = AWESOME_MENU_ITEM_NAME.extractSingle(menuItem, "");
            String image = AWESOME_MENU_ITEM_IMAGE.extractSingle(menuItem, "");

            if (link.equals("#")) {
                //Menu item triggers a chat command
                String chatAction = AWESOME_MENU_ITEM_CHATACTION.extractSingle(menuItem, "");

                chatAction = chatAction.replace("&amp;", "&");
                String url = ChatModel.encodeChatMessage(chatBase, playerid, pwd, chatAction);

                locations.add(new ChatMacroActionElement(getSession(), name, image, url));
            } else {
                locations.add(new ActionElement(getSession(), name, image, link));
            }
        }

        Logger.log("NavigationModel", "Loaded " + locations.size() + " elements from awesomemenu.php");
    }

    private void conditionalAdd(String html, String name, String image,
                                String... urls) {
        for (String url : urls) {
            if (html.contains(url)) {
                locations.add(new ActionElement(getSession(), name, image, url));
                break;
            }
        }
    }

    private void loadTopMenuContent(ServerReply content) {
        String html = content.html;
        conditionalAdd(
                html,
                "Seaside Town",
                "http://images.kingdomofloathing.com/itemimages/seasidetown.gif",
                "town.php", "whichplace=town");
        conditionalAdd(html, "Your Campsite",
                "http://images.kingdomofloathing.com/itemimages/tent1.gif",
                "campground.php", "place.php?whichplace=campground");
        conditionalAdd(
                html,
                "The Big Mountains",
                "http://images.kingdomofloathing.com/itemimages/themountains.gif",
                "mountains.php", "place.php?whichplace=mountains");
        conditionalAdd(
                html,
                "The Sea",
                "http://images.kingdomofloathing.com/itemimages/thesea.gif",
                "thesea.php", "place.php?whichplace=thesea");
        conditionalAdd(
                html,
                "The Plains",
                "http://images.kingdomofloathing.com/itemimages/theplains.gif",
                "plains.php", "place.php?whichplace=plains");
        conditionalAdd(
                html,
                "Desert Beach",
                "http://images.kingdomofloathing.com/itemimages/thedesert.gif",
                "beach.php", "place.php?whichplace=desertbeach");
        conditionalAdd(
                html,
                "The Distant Woods",
                "http://images.kingdomofloathing.com/itemimages/bansai.gif",
                "woods.php", "place.php?whichplace=woods");

        conditionalAdd(
                html,
                "The Mysterious Island of Mystery",
                "http://images.kingdomofloathing.com/itemimages/theisland.gif",
                "island.php", "place.php?whichplace=island");
        conditionalAdd(
                html,
                "Your Clan Hall",
                "http://images.kingdomofloathing.com/itemimages/clanhall.gif",
                "clan_hall.php", "place.php?whichplace=clan");

        conditionalAdd(
                html,
                "The Mall",
                "http://images.kingdomofloathing.com/itemimages/themall.gif",
                "mall.php");

        conditionalAdd(
                html,
                "The Beanstalk",
                "http://images.kingdomofloathing.com/itemimages/thebeanstalk.gif",
                "beanstalk.php", "place.php?whichplace=beanstalk");

        conditionalAdd(
                html,
                "Sorceress' Lair",
                "http://images.kingdomofloathing.com/itemimages/lairicon.gif",
                "lair.php", "place.php?whichplace=lair");
        conditionalAdd(
                html,
                "Spookyraven Manor",
                "http://images.kingdomofloathing.com/itemimages/manoricon.gif",
                "place.php?whichplace=manor1", "place.php?whichplace=manor1");
        conditionalAdd(
                html,
                "PvP",
                "http://images.kingdomofloathing.com/itemimages/swords.gif",
                "peevpee.php");

        locations.add(new ActionElement(getSession(), "Messages",
                "http://images.kingdomofloathing.com/itemimages/envelope.gif",
                "messages.php"));
        locations.add(new ActionElement(getSession(), "Donate",
                "http://images.kingdomofloathing.com/itemimages/donate.gif",
                "donatepopup.php"));
        locations.add(new ActionElement(getSession(), "Community",
                "http://images.kingdomofloathing.com/itemimages/chat.gif",
                "community.php"));
    }

    private static class ChatMacroActionElement extends ActionElement {
        public ChatMacroActionElement(Session session, String text, String img, String action) {
            super(session, text, img, action);
        }

        @Override
        protected void submit(final ViewContext context, final Session session, String url) {
            Request r = new Request(url);
            r.makeAsync(session, context.createLoadingContext(), new ResponseHandler() {
                @Override
                public void handle(Session session, ServerReply response) {
                    if (response == null) {
                        Logger.log("NavigationModel", "ChatMacro response: [NULL]");
                        context.displayMessage("Unable to connect to KoL.");
                        return;
                    }

                    Logger.log("NavigationModel", "ChatMacro response: " + response.html);
                    for (String macroResponse : MACRO_RESPONSE.extractAllSingle(response.html)) {
                        String message = MACRO_RESPONSE_TEXT.extractSingle(macroResponse, "");
                        String action = MACRO_RESPONSE_ACTION.extractSingle(macroResponse, "");
                        Logger.log("NavigationModel", "ChatMacro message parsed [" + message + ", " + action + "]");

                        context.displayMessage(message);

                        action = action.replace("skills.php?", "runskillz.php?"); //avoid the redirect

                        if (MACRO_ACTION_REDIRECT.matches(action)) {
                            action = MACRO_ACTION_REDIRECT.extractSingle(action, "");
                        } else if (MACRO_ACTION_GET_RESULTS.matches(action)) {
                            action = MACRO_ACTION_GET_RESULTS.extractSingle(action, "");
                            action += (action.contains("?")) ? "&androiddisplay=results" : "?androiddisplay=results";
                        } else if (MACRO_ACTION_EXAMINE.matches(action)) {
                            action = "desc_item.php?whichitem=" + MACRO_ACTION_EXAMINE.extractSingle(action, "0");
                        } else {
                            if (!action.equals("")) {
                                Logger.log("NavigationModel", "Unknown macro action: " + action);
                            }
                            continue;
                        }

                        Request r = new Request(action);
                        r.makeAsync(session, context.createLoadingContext(), context.getPrimaryRoute());
                    }
                }
            });
        }
    }
}
