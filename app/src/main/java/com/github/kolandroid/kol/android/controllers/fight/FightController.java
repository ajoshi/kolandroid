package com.github.kolandroid.kol.android.controllers.fight;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.binders.ElementBinder;
import com.github.kolandroid.kol.android.binders.SubtextBinder;
import com.github.kolandroid.kol.android.controller.Controller;
import com.github.kolandroid.kol.android.controller.ModelController;
import com.github.kolandroid.kol.android.controllers.web.WebController;
import com.github.kolandroid.kol.android.screen.DialogScreen;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.android.screen.ViewScreen;
import com.github.kolandroid.kol.android.util.searchlist.SearchListController;
import com.github.kolandroid.kol.model.elements.ActionElement;
import com.github.kolandroid.kol.model.models.fight.FightItem;
import com.github.kolandroid.kol.model.models.fight.FightModel;
import com.github.kolandroid.kol.model.models.fight.FightSkill;

import java.util.ArrayList;

public class FightController extends ModelController<FightModel> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -7848760148015474401L;

    public FightController(FightModel model) {
        super(model);
    }

    @Override
    public int getView() {
        return R.layout.fragment_fight_screen;
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        choice.displayPrimary(this);
    }

    @Override
    public void connect(View view, final FightModel model, final Screen host) {
        final Button attack = (Button) view.findViewById(R.id.fight_attack);
        attack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ActionElement action = model.getAttack();
                if (action != null)
                    action.submit(host.getViewContext());
            }
        });

        final Button useskill = (Button) view.findViewById(R.id.fight_skill);
        useskill.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View btn) {
                ArrayList<FightSkill> skills = model.getSkills();
                Controller skillsController = SearchListController.create(skills, SubtextBinder.ONLY);
                DialogScreen.display(skillsController, host,
                        "Choose a skill to use:");
            }
        });

        final Button useitem = (Button) view.findViewById(R.id.fight_items);
        useitem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View btn) {
                ArrayList<FightItem> items = model.getItems();

                if (getModel().hasFunkslinging()) {
                    Controller itemsController = new FunkslingingController(items);
                    DialogScreen.display(itemsController, host, "Select items to use:");
                } else {
                    Controller itemsController = SearchListController.create(items, ElementBinder.ONLY);
                    DialogScreen.display(itemsController, host,
                            "Choose an item to use:");
                }
            }
        });

        if (model.isFightOver()) {
            attack.setEnabled(false);
            useskill.setEnabled(false);
            useitem.setEnabled(false);
        }

        ViewScreen webscreen = (ViewScreen) view
                .findViewById(R.id.fight_webscreen);
        WebController web = new WebController(model);
        webscreen.display(web, host);
    }

}
