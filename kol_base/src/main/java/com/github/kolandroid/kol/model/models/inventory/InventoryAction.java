package com.github.kolandroid.kol.model.models.inventory;

import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.model.elements.MultiusableElement;
import com.github.kolandroid.kol.model.elements.basic.BasicAction;
import com.github.kolandroid.kol.model.elements.basic.BasicElement;
import com.github.kolandroid.kol.model.elements.interfaces.Multiuseable;

public abstract class InventoryAction extends BasicElement {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 8846472727703984221L;

    public InventoryAction(String text) {
        super(text);
    }

    public abstract void select(InventoryActionVisitor visitor);

    public static class ImmediateItemAction extends InventoryAction {
        /**
         * Autogenerated by eclipse.
         */
        private static final long serialVersionUID = -8139678183909404073L;

        private final BasicAction onTrigger;

        public ImmediateItemAction(Session session, String text, String action) {
            super(text);

            this.onTrigger = new BasicAction(session, action);
        }

        @Override
        public void select(InventoryActionVisitor visitor) {
            visitor.executeRequest(onTrigger);
        }
    }

    public static class AutosellItemAction extends InventoryAction {
        /**
         * Autogenerated by eclipse.
         */
        private static final long serialVersionUID = 5269277435965864590L;

        private final Multiuseable toMultisell;

        public AutosellItemAction(final Session session,
                                  final InventoryItem base, final String defaultAction,
                                  final String pwd) {
            super("Autosell");

            String action = defaultAction.replace("passitem",
                    "whichitem");
            action += "&action=useitem&ajax=1";
            action += "&pwd=" + pwd;
            action += "&quantity=";

            this.toMultisell = new MultiusableElement(session, base, action);
        }

        @Override
        public void select(InventoryActionVisitor visitor) {
            visitor.displayMultiuse(toMultisell, "Sell");
        }
    }

    public static class MultiuseItemAction extends InventoryAction {
        /**
         * Autogenerated by eclipse.
         */
        private static final long serialVersionUID = -7437573610135939265L;

        private final Multiuseable toMultiuse;

        public MultiuseItemAction(final Session session,
                                  final InventoryItem base, final String defaultAction,
                                  final String pwd) {
            super("Use multiple");

            String action = defaultAction.replace("passitem",
                    "whichitem");
            action += "&action=useitem&ajax=1";
            action += "&pwd=" + pwd;
            action += "&quantity=";

            this.toMultiuse = new MultiusableElement(session, base, action);
        }

        @Override
        public void select(InventoryActionVisitor visitor) {
            visitor.displayMultiuse(toMultiuse, "Use");
        }
    }


    public static class MultiClosetItemAction extends InventoryAction {
        /**
         * Autogenerated by eclipse.
         */
        private static final long serialVersionUID = -47359857435L;

        private final Multiuseable toMultiuse;

        public MultiClosetItemAction(final Session session,
                                     final InventoryItem base, final String name, final String defaultAction,
                                     final String pwd) {
            super(name);
            this.toMultiuse = new MultiusableElement(session, base, defaultAction);
        }

        @Override
        public void select(InventoryActionVisitor visitor) {
            if (this.getText().contains("Take"))
                visitor.displayMultiuse(toMultiuse, "Take");
            else
                visitor.displayMultiuse(toMultiuse, "Store");
        }
    }
}
