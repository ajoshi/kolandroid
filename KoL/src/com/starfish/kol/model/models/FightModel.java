package com.starfish.kol.model.models;

import java.util.ArrayList;

import com.starfish.kol.connection.Connection.ServerReply;
import com.starfish.kol.connection.Session;
import com.starfish.kol.gamehandler.ViewContext;
import com.starfish.kol.model.basic.ActionItem;
import com.starfish.kol.model.basic.OptionItem;
import com.starfish.kol.util.Regex;

public class FightModel extends FilteredWebModel {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = 7344809755641734582L;

	/**
	 * //Ignored for now. private static final Regex ROUND_NUMBER = new
	 * Regex("var onturn = (\\d+);", 1);
	 */
	private static final Regex FIGHT_OVER = new Regex("<!--WINWINWIN-->");

	private static final Regex ACTION_BTN = new Regex(
			"<input[^<>]*type=[\"']?hidden[\"']?[^<>]*value=[\"']?([^\"']*?)[\"']?>.*?<input[^<>]*value=[\"']?([^\"<>]*?)[\"']?>",
			1, 2);

	private static final Regex ALL_SKILLS = OptionItem.regexFor("whichskill");
	private static final Regex ALL_ITEMS = OptionItem.regexFor("whichitem");

	private static final Regex ACTION_BAR = new Regex(
			"<div[^>]*skillmenu[^>]*>.*?(?=<div[^>]*content_)");
	private static final Regex MAIN_TAG = new Regex(
			"<div[^>]*content_[^>]*(?=>)");
	private static final Regex BUTTON_REVEALER = new Regex(
			"<a[^>]*>\\(show old combat form\\)</a>");
	private static final Regex BUTTONS = new Regex("<form[^>]*>.*?</form>");

	private static final Regex HAS_FUNKSLINGING = new Regex("<select[^>]*whichitem2[^>]*>");
	
	private ArrayList<ActionItem> skills;
	private ArrayList<GameItem> items;

	private boolean fightFinished = false;
	private ActionItem attack = new ActionItem(getSession(), "Attack",
			"fight.php?action=attack");

	private boolean funkslinging;
	
	public FightModel(Session s, ServerReply text) {
		super(s, text);

		processSkills(text.html);
		processItems(text.html);

		if (FIGHT_OVER.matches(text.html))
			fightFinished = true;
	}

	private void processSkills(String html) {
		this.skills = new ArrayList<ActionItem>();

		ArrayList<String[]> buttons = ACTION_BTN.extractAll(html);
		for (String[] button : buttons) {
			if (button == null)
				continue;
			String action = button[0];
			String text = button[1];
			String img = "";

			System.out.println("Found button: " + action);
			if (action == null || text == null || text.length() == 0)
				continue;
			if (action.contentEquals("attack") || action.contentEquals("skill")
					|| action.contentEquals("useitem"))
				continue;

			switch (action) {
			case "steal":
				img = "http://images.kingdomofloathing.com/itemimages/knobsack.gif";
				break;
			case "runaway":
				img = "http://images.kingdomofloathing.com/itemimages/runaway.gif";
				break;
			}

			this.skills.add(new ActionItem(getSession(), text, img, "fight.php?action="
					+ action));
		}

		String dropdown = ALL_SKILLS.extractSingle(html);
		ArrayList<OptionItem> dropdown_skills = OptionItem.extractOptions(dropdown);
		for(OptionItem option : dropdown_skills) {
			skills.add(new ActionItem(getSession(), option.text, option.img, "fight.php?action=skill&whichskill=" + option.value));
		}
	}

	private void processItems(String html) {
		this.items = new ArrayList<GameItem>();

		String dropdown = ALL_ITEMS.extractSingle(html);
		

		ArrayList<OptionItem> dropdown_items = OptionItem.extractOptions(dropdown);
		for(OptionItem option : dropdown_items) {
			items.add(new GameItem(getSession(), option.text, option.img, option.value));
		}
		
		this.funkslinging = HAS_FUNKSLINGING.matches(html);
	}


	public ArrayList<ActionItem> getSkills() {
		return this.skills;
	}

	public ArrayList<GameItem> getItems() {
		return this.items;
	}

	public boolean isFightOver() {
		return fightFinished;
	}

	public ActionItem getAttack() {
		return attack;
	}
	
	public boolean hasFunkslinging() {
		return funkslinging;
	}

	@Override
	protected String filterHtml(String html) {
		// First, we remove all components of the action bar

		// Remove the top bar
		String noBar = ACTION_BAR.replaceAll(html, "");
		// Remove special styling on the content div
		String renamed = MAIN_TAG.replaceAll(noBar,
				"$0 style=\"top:0px; margin:8px\"");
		// Remove the "Show old combat form" link
		String actionClear = BUTTON_REVEALER.replaceAll(renamed, "");

		// Finally, remove all forms from the older version of the fight page.
		return BUTTONS.replaceAll(actionClear, "");
	}
	
	public static class GameItem extends ActionItem 
	{
		/**
		 * Autogenerated by eclipse.
		 */
		private static final long serialVersionUID = -2222125085936302631L;

		private final String val;
		
		public GameItem(Session session, String text, String img, String val) {
			super(session, text, img, "fight.php?action=useitem&whichitem=" + val);
			
			this.val = val;
		}		
		
		public boolean useWith(ViewContext context, GameItem second) {
			if(second == NONE) {
				this.submit(context);
				return true;
			}
			
			//TODO: how submit both?
			return false;
		}
		
		public static GameItem NONE = new GameItem(null, "(select an item below)", "", "") {
			/**
			 * Autogenerated by eclipse.
			 */
			private static final long serialVersionUID = 6441158699205312769L;

			@Override
			protected void submit(ViewContext context, String urloverride) {
				//do nothing
			}
			
			@Override
			public void submit(ViewContext context) {
				//do nothing
			}
			
			@Override
			public boolean useWith(ViewContext context, GameItem second) {
				if(second == NONE)
					return false;
				second.submit(context);
				return true;
			}
		};
	}
}
