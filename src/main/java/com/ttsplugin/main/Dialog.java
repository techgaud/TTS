package com.ttsplugin.main;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

public class Dialog {
	public String message;
	public String sender;
	
	public Dialog(String message, String sender) {
		this.message = message.replace("<br>", " ");
		this.sender = sender;
	}
	
	public static Dialog getCurrentDialog(Client client) {
		if (!isHidden(client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT))) {
			return new Dialog(getWidgetText(client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT)), client.getLocalPlayer().getName());
		} else if (!isHidden(client.getWidget(WidgetInfo.DIALOG_NPC_TEXT))) {
			return new Dialog(getWidgetText(client.getWidget(WidgetInfo.DIALOG_NPC_TEXT)), getWidgetText(client.getWidget(WidgetInfo.DIALOG_NPC_NAME)));
		} else {
			return null;
		}
	}
	
	private static String getWidgetText(Widget widget) {
		if (widget != null) return widget.getText();
		return null;
	}
	
	private static boolean isHidden(Widget widget) {
		if (widget != null) return widget.isHidden();
		return true;
	}
	
	@Override
	public boolean equals(Object other2) {
		if (other2 == null) {
			return false;
		}
		
		Dialog other = (Dialog)other2;
		return this.message.equals(other.message) && this.sender.equals(other.sender);
	}
}
