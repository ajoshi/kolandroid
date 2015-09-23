package com.github.kolandroid.kol.model.models.inventory;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.model.elements.ActionElement;
import com.github.kolandroid.kol.model.elements.MultiuseElement;
import com.github.kolandroid.kol.model.elements.basic.BasicSubtextElement;
import com.github.kolandroid.kol.model.elements.interfaces.SubtextElement;
import com.github.kolandroid.kol.util.Regex;

public class ClosetModel extends ItemStorageModel {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 34323720L;
    private static final Regex ALPHA_NUM_VALUE = new Regex("value=[\"']?([a-f0-9]+)[\"'>]", 1);
    private static final Regex PWD_INPUT = new Regex("(<input[^>]*name=[\"']?pwd[^>]*>)", 1);
    private static final Regex CONTAINED_MEAT = new Regex("Your closet contains <b>([^<]*)</b> meat.", 1);
    private final ActionElement changeState;
    private final MultiuseElement manageMeat;
    private final String meatAction;
    private final String currentState;

    public ClosetModel(Session s, ServerReply text) {
        super(s, fixServerReply(text), (text.url.contains("fillcloset.php") ? "fillcloset.php" : "closet.php"), false);

        String pwd = ALPHA_NUM_VALUE.extractSingle(PWD_INPUT.extractSingle(text.html), "0");

        String meatAmount = CONTAINED_MEAT.extractSingle(text.html, "[ERROR]");
        SubtextElement meat = new BasicSubtextElement(meatAmount + " Meat", "http://images.kingdomofloathing.com/itemimages/meat.gif");

        if (text.url.contains("fillcloset.php")) {
            changeState = new ActionElement(this.getSession(), "Return to Closet", "closet.php");
            manageMeat = new MultiuseElement(getSession(), meat, "POST/fillcloset.php?addtake=add&action=addtakeclosetmeat&pwd=" + pwd + "&quantity=");
            meatAction = "Put in Closet";
            currentState = "Moving items into closet.";
        } else {
            changeState = new ActionElement(this.getSession(), "Fill Your Closet", "fillcloset.php");
            manageMeat = new MultiuseElement(getSession(), meat, "POST/closet.php?addtake=take&action=addtakeclosetmeat&pwd=" + pwd + "&quantity=");
            meatAction = "Take from Closet";
            currentState = "Moving items out of closet.";
        }
    }

    private static ServerReply fixServerReply(ServerReply text) {
        String message;
        if (text.url.contains("fillcloset.php")) {
            message = "Store";
        } else {
            message = "Take";
        }

        String html = text.html;
        html = html.replace("[one]", "[" + message + " one]");
        html = html.replace("[some]", "[" + message + " some]");
        html = html.replace("[all]", "[" + message + " all]");
        return new ServerReply(text, html);
    }

    public String getCurrentState() {
        return currentState;
    }

    public ActionElement getChangeState() {
        return changeState;
    }

    public MultiuseElement getManageMeat() {
        return manageMeat;
    }

    public String getMeatText() {
        return meatAction;
    }
}
