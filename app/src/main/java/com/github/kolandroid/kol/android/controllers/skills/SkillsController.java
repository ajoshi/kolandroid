package com.github.kolandroid.kol.android.controllers.skills;

import android.view.View;

import com.github.kolandroid.kol.android.binders.DefaultGroupBinder;
import com.github.kolandroid.kol.android.binders.SkillsBinder;
import com.github.kolandroid.kol.android.controller.Controller;
import com.github.kolandroid.kol.android.controller.GroupController;
import com.github.kolandroid.kol.android.controllers.MultiusableController;
import com.github.kolandroid.kol.android.controllers.web.WebController;
import com.github.kolandroid.kol.android.screen.DialogScreen;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.android.util.searchlist.GroupSearchListController;
import com.github.kolandroid.kol.android.util.searchlist.ListSelector;
import com.github.kolandroid.kol.android.util.searchlist.SearchListController;
import com.github.kolandroid.kol.android.util.searchlist.SerializableSelector;
import com.github.kolandroid.kol.model.elements.interfaces.ModelGroup;
import com.github.kolandroid.kol.model.models.WebModel;
import com.github.kolandroid.kol.model.models.skill.ItemsListModel;
import com.github.kolandroid.kol.model.models.skill.SkillModelElement;
import com.github.kolandroid.kol.model.models.skill.SkillModelElement.Buff;
import com.github.kolandroid.kol.model.models.skill.SkillModelElement.RestorerItem;
import com.github.kolandroid.kol.model.models.skill.SkillModelElement.Skill;
import com.github.kolandroid.kol.model.models.skill.SkillModelVisitor;
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
    private static final ListSelector<SkillModelElement> selector = new SerializableSelector<SkillModelElement>() {
        /**
         * Autogenerated by eclipse.
         */
        private static final long serialVersionUID = -6654955956381975044L;

        @Override
        public boolean selectItem(final Screen host, SkillModelElement item) {
            item.select(new SkillModelVisitor() {
                @Override
                public void display(Skill skill) {
                    Controller controller = new MultiusableController(skill, "Cast");
                    DialogScreen.display(controller, host);
                }

                @Override
                public void display(Buff buff) {
                    BuffController controller = new BuffController(buff);
                    DialogScreen.display(controller, host);
                }

                @Override
                public void display(RestorerItem item) {
                    Controller controller = new MultiusableController(item, "Use");
                    DialogScreen.display(controller, host);
                }
            });
            return false;
        }
    };
    private static final SkillsVisitor<Controller> childRoute = new SkillsVisitor<Controller>() {
        @Override
        public Controller execute(SkillsListModel model) {
            ArrayList<ModelGroup<SkillModelElement>> list = model.getSkills();
            return new GroupSearchListController<SkillModelElement>(list, DefaultGroupBinder.ONLY, SkillsBinder.ONLY, selector);
        }

        @Override
        public Controller execute(ItemsListModel model) {
            ArrayList<SkillModelElement> list = model.getItems();
            return new SearchListController<SkillModelElement>(list, SkillsBinder.ONLY, selector);
        }
    };

    public SkillsController(SkillsModel model) {
        super(model);
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        choice.displayPrimary(this);
    }

    @Override
    protected Controller getController(SkillsSubmodel child) {
        return child.execute(childRoute);
    }

    @Override
    public void connect(View view, SkillsModel model, Screen host) {
        super.connect(view, model, host);

        WebModel results = model.getResultsPane();
        if (results != null) {
            WebController web = new WebController(results);
            DialogScreen.display(web, host);
        }
    }
}
