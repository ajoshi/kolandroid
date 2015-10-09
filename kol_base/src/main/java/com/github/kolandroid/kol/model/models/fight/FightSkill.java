package com.github.kolandroid.kol.model.models.fight;

import com.github.kolandroid.kol.model.elements.OptionElement;
import com.github.kolandroid.kol.model.models.skill.SkillModel;
import com.github.kolandroid.kol.request.Request;
import com.github.kolandroid.kol.session.Session;

public class FightSkill extends SkillModel implements FightAction {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 3970718234205335257L;

    private final String identifier;
    private final FightActionHistory<? super FightSkill> storage;

    public FightSkill(Session session, OptionElement base, FightActionHistory<? super FightSkill> storage) {
        super(session, "", "", base);
        this.storage = storage;
        this.identifier = "skill:" + base.value;
    }

    public void use() {
        storage.store(this, getSettings());
        this.makeRequest(new Request("POST/fight.php?action=skill&whichskill=" + this.id));
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public boolean matchesActionBarItem(String type, String id) {
        return (type != null) && (type.equals("skill")) && (id != null) && (id.equals(this.id));
    }
}
