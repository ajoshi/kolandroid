package com.github.kolandroid.kol.model.models.inventory;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.model.GroupModel;
import com.github.kolandroid.kol.util.Logger;
import com.github.kolandroid.kol.util.Regex;

public class ItemStorageModel extends GroupModel<ItemPocketModel> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 34637430160L;

    private static final Regex CHOSEN_CONSUME = new Regex("\\[consumables\\]");
    private static final Regex CHOSEN_EQUIP = new Regex("\\[equipment\\]");
    private static final Regex CHOSEN_MISC = new Regex("\\[miscellaneous\\]");
    private static final Regex CHOSEN_RECENT = new Regex("\\[recent items\\]");
    private final ItemPocketModel consume;
    private final ItemPocketModel equip;
    private final ItemPocketModel misc;
    private final ItemPocketModel recent;
    private final String baseUrl;
    private int chosen;

    public ItemStorageModel(Session s, ServerReply text, String baseUrl, boolean useEquipmentModel) {
        super(s);

        this.baseUrl = baseUrl;
        consume = new ItemPocketModel("Consume", s, baseUrl + "?which=1");
        if (useEquipmentModel) {
            equip = new EquipmentPocketModel("Equip", s, baseUrl + "?which=2");
        } else {
            equip = new ItemPocketModel("Equip", s, baseUrl + "?which=2");
        }
        misc = new ItemPocketModel("Misc", s, baseUrl + "?which=3");
        recent = new ItemPocketModel("Recent", s, baseUrl + "?which=f-1");

        loadContent(text);
    }

    protected void loadContent(ServerReply text) {
        if (!text.url.contains(baseUrl)) {
            Logger.log("ItemStorageModel", "Attempted to load non-" + baseUrl + " page into ItemStorageModel: "
                    + text.url);
            return;
        }

        if (CHOSEN_CONSUME.matches(text.html)) {
            chosen = 1;
        } else if (CHOSEN_EQUIP.matches(text.html)) {
            chosen = 2;
        } else if (CHOSEN_MISC.matches(text.html)) {
            chosen = 3;
        } else if (CHOSEN_RECENT.matches(text.html)) {
            chosen = 0;
        } else
            throw new RuntimeException(
                    "Unable to determine current pane");

        ItemPocketModel[] children = this.getChildren();
        children[chosen].process(text);
        System.out.println("Loaded into slot " + chosen);
    }

    @Override
    public ItemPocketModel[] getChildren() {
        return new ItemPocketModel[]{recent, consume, equip, misc};
    }

    @Override
    public int getActiveChild() {
        return chosen;
    }
}
