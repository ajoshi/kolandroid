package com.starfish.kol.model.models;

import java.util.ArrayList;

import com.starfish.kol.connection.Connection.ServerReply;
import com.starfish.kol.connection.Session;
import com.starfish.kol.model.elements.ActionElement;
import com.starfish.kol.util.Regex;

public class ChoiceModel extends FilteredWebModel {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = -9107242519455349408L;

	private static Regex OPTIONS = new Regex("<form[^>]*>(.*?)</form>", 1);

	private static Regex PWD_INPUT = new Regex("(<input[^>]*name=[\"']?pwd[^>]*>)", 1);
	private static Regex WHICH_INPUT = new Regex("(<input[^>]*name=[\"']?whichchoice[^>]*>)", 1);
	private static Regex OPTION_INPUT = new Regex("(<input[^>]*name=[\"']?option[^>]*>)", 1);
	private static Regex SUBMIT_INPUT = new Regex("(<input[^>]*type=[\"']?submit[^>]*>)", 1);

	private static Regex ALPHANUM_VALUE = new Regex("value=[\"']?([a-f0-9]+)[\"'>]", 1);
	private static Regex NUM_VALUE = new Regex("value=[\"']?(\\d+)[\"'>]", 1);
	private static Regex VALUE = new Regex("value=[\"]?(.*?)[\">]", 1);
	
	private ArrayList<ActionElement> options;
	
	public ChoiceModel(Session s, ServerReply response) {
		super(s, response);
		
		this.extractOptions(response.html);
	}
	
	private void extractOptions(String html) {
		this.options = new ArrayList<ActionElement>();
		for(String form : OPTIONS.extractAllSingle(html)) {
			System.out.println("Found option: " + form);
			
			String pwd = ALPHANUM_VALUE.extractSingle(PWD_INPUT.extractSingle(form));
			String whichchoice = NUM_VALUE.extractSingle(WHICH_INPUT.extractSingle(form));
			String option = NUM_VALUE.extractSingle(OPTION_INPUT.extractSingle(form));
			String text = VALUE.extractSingle(SUBMIT_INPUT.extractSingle(form));
			
			if(pwd == null || whichchoice == null || option == null) continue;
			
			text = text.replace("&quot;", "\"");
			
			String action = "choice.php?pwd=" + pwd + "&whichchoice=" + whichchoice + "&option=" + option;
			options.add(new ActionElement(getSession(), text, action));
		}
	}
	
	public ArrayList<ActionElement> getOptions() {
		return this.options;
	}	

	@Override
	protected String filterHtml(String html) {
		return OPTIONS.replaceAll(html, "");
	}
}
