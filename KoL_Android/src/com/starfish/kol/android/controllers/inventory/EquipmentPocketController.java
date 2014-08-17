package com.starfish.kol.android.controllers.inventory;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.starfish.kol.android.R;
import com.starfish.kol.android.binders.ElementBinder;
import com.starfish.kol.android.binders.SubtextBinder;
import com.starfish.kol.android.controller.Controller;
import com.starfish.kol.android.controller.ModelController;
import com.starfish.kol.android.screen.DialogScreen;
import com.starfish.kol.android.screen.Screen;
import com.starfish.kol.android.screen.ScreenSelection;
import com.starfish.kol.android.screen.ViewScreen;
import com.starfish.kol.android.util.searchlist.GroupSearchListController;
import com.starfish.kol.android.util.searchlist.SerializableSelector;
import com.starfish.kol.model.LiveMessage;
import com.starfish.kol.model.models.inventory.EquipmentPocketModel;
import com.starfish.kol.model.models.inventory.InventoryItem;

public class EquipmentPocketController extends ModelController<LiveMessage, EquipmentPocketModel>{
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = 5593217234616379830L;

	public EquipmentPocketController(EquipmentPocketModel model) {
		super(model);
	}

	@Override
	public int getView() {
		return R.layout.fragment_equipment_pane;
	}

	private transient GroupSearchListController<InventoryItem> list;
	
	@Override
	public void chooseScreen(ScreenSelection choice) {
		choice.displayPrimary(this);
	}

	@Override
	public void recieveProgress(View view, EquipmentPocketModel model, LiveMessage message, Screen host) {
		if(list != null)
			list.setItems(model.getItems());
	}
	
	@Override
	public void connect(View view, final EquipmentPocketModel model, final Screen host) {

		ViewScreen screen = (ViewScreen)view.findViewById(R.id.inventory_list);
		list = new GroupSearchListController<InventoryItem>(model.getItems(), SubtextBinder.ONLY, displayPossibleActions);
		screen.display(list, host);
		
		Button equipoutfit = (Button)view.findViewById(R.id.equipment_equipoutfit);
		equipoutfit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Controller c = GroupSearchListController.create(model.getOutfits(), ElementBinder.ONLY);
				DialogScreen.display(c, host, "Equip outfit");
			}			
		});
		
		Button saveoutfit = (Button)view.findViewById(R.id.equipment_saveoutfit);
		saveoutfit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controller c = new CustomOutfitController(model.saveOutfit());
				DialogScreen.display(c, host);
			}
		});
	}
	
	private static final SerializableSelector<InventoryItem> displayPossibleActions = new SerializableSelector<InventoryItem>(){
		/**
		 * Autogenerated by eclipse.
		 */
		private static final long serialVersionUID = 6502795666816716450L;

		@Override
		public boolean selectItem(Screen host, InventoryItem item) {
			ItemController controller = new ItemController(item);
			DialogScreen.display(controller, host);
			return false;
		}		
	};
}
