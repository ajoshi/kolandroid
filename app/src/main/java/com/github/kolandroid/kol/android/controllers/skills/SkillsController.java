package com.github.kolandroid.kol.android.controllers.skills;

import android.view.View;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.binders.DefaultGroupBinder;
import com.github.kolandroid.kol.android.binders.SkillsBinder;
import com.github.kolandroid.kol.android.controller.Controller;
import com.github.kolandroid.kol.android.controller.GroupController;
import com.github.kolandroid.kol.android.controllers.inventory.ItemPocketController;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.android.util.HandlerCallback;
import com.github.kolandroid.kol.android.util.searchlist.GroupSearchListController;
import com.github.kolandroid.kol.android.util.searchlist.ListSelector;
import com.github.kolandroid.kol.android.util.searchlist.SerializableSelector;
import com.github.kolandroid.kol.model.elements.interfaces.ModelGroup;
import com.github.kolandroid.kol.model.models.skill.ItemRestorersModel;
import com.github.kolandroid.kol.model.models.skill.SkillModel;
import com.github.kolandroid.kol.model.models.skill.SkillsListModel;
import com.github.kolandroid.kol.model.models.skill.SkillsModel;
import com.github.kolandroid.kol.model.models.skill.SkillsSubmodel;
import com.github.kolandroid.kol.model.models.skill.SkillsVisitor;

import java.util.ArrayList;

public class SkillsController extends GroupController<SkillsSubmodel, SkillsModel> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -6281249096497073143L;
    private transient HandlerCallback<SkillModel> displayModel;
    private final ListSelector<SkillModel> selector = new SerializableSelector<SkillModel>() {
        /**
         * Autogenerated by eclipse.
         */
        private static final long serialVersionUID = -6654955956381975044L;

        @Override
        public boolean selectItem(final Screen host, SkillModel skill) {
            skill.attachView(host.getViewContext());
            skill.loadDescription(displayModel.weak());
            return false;
        }
    };
    private final SkillsVisitor<Controller> childRoute = new SkillsVisitor<Controller>() {
        @Override
        public Controller execute(SkillsListModel model) {
            ArrayList<ModelGroup<SkillModel>> list = model.getSkills();
            return new GroupSearchListController<>(list, DefaultGroupBinder.ONLY, SkillsBinder.ONLY, selector);
        }

        @Override
        public Controller execute(ItemRestorersModel model) {
            return new ItemPocketController(model, R.color.inventory_header);
        }
    };

    public SkillsController(SkillsModel model) {
        super(model);
    }

    @Override
    public void connect(View view, SkillsModel model, final Screen host) {
        super.connect(view, model, host);

        displayModel = new HandlerCallback<SkillModel>() {
            @Override
            protected void receiveProgress(SkillModel skill) {
                SkillController controller = new SkillController(skill);
                host.getViewContext().getPrimaryRoute().execute(controller);
            }
        };
    }

    @Override
    public void disconnect(Screen host) {
        super.disconnect(host);
        displayModel.close();
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        choice.displayPrimary(this, true);
    }

    @Override
    protected Controller getController(SkillsSubmodel child) {
        return child.execute(childRoute);
    }
}
