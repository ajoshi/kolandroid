package com.github.kolandroid.kol.android.controllers.inventory;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.controller.Controller;
import com.github.kolandroid.kol.android.controller.ModelController;
import com.github.kolandroid.kol.android.controllers.MultiusableController;
import com.github.kolandroid.kol.android.controllers.web.WebController;
import com.github.kolandroid.kol.android.screen.DialogScreen;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.android.screen.ViewScreen;
import com.github.kolandroid.kol.android.util.ImageDownloader;
import com.github.kolandroid.kol.model.elements.interfaces.DeferredGameAction;
import com.github.kolandroid.kol.model.elements.interfaces.Multiuseable;
import com.github.kolandroid.kol.model.models.WebModel;
import com.github.kolandroid.kol.model.models.inventory.InventoryAction;
import com.github.kolandroid.kol.model.models.inventory.InventoryActionVisitor;
import com.github.kolandroid.kol.model.models.inventory.ItemModel;

public class ItemController extends ModelController<ItemModel> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 2863460893944088836L;

    public ItemController(ItemModel base) {
        super(base);
    }

    @Override
    public int getView() {
        return R.layout.dialog_item_screen;
    }

    @Override
    public void connect(View view, ItemModel model, final Screen host) {
        final InventoryActionVisitor visitor = new InventoryActionVisitor() {
            @Override
            public void executeRequest(DeferredGameAction action) {
                action.submit(host.getViewContext());
            }

            @Override
            public void displayMultiuse(Multiuseable item, String useText) {
                Controller controller = new MultiusableController(item, useText);
                DialogScreen.display(controller, host);
            }
        };

        WebModel description = model.getDescription();
        if (description == null) {
            // Display image/name of the item as a backup
            TextView text = (TextView) view.findViewById(R.id.dialog_item_text);
            text.setText(model.getText());
            text.setVisibility(View.VISIBLE);

            if (model.getImage() != null && !model.getImage().equals("")) {
                ImageView img = (ImageView) view.findViewById(R.id.dialog_item_image);
                img.setVisibility(View.VISIBLE);
                ImageDownloader.loadFromUrl(img, model.getImage());
            }
        } else {
            ViewScreen desc = (ViewScreen) view.findViewById(R.id.dialog_item_description);
            desc.display(new WebController(description), host);
        }

        ViewGroup group = (ViewGroup) view.findViewById(R.id.dialog_item_group);
        for (InventoryAction invAction : model.getActions()) {
            final InventoryAction action = invAction;
            Button button = new Button(host.getActivity());
            button.setText(action.getText());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    action.select(visitor);
                    host.close();
                }
            });
            group.addView(button);
        }
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        choice.displayDialog(this);
    }

}
