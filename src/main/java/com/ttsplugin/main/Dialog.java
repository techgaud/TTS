package com.ttsplugin.main;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.util.Text;

public class Dialog {
	public String message;
	public String sender;
	
	public Dialog(String message, String sender) {
		this.message = Text.sanitizeMultilineText(
			message
				// Replace hyphens with spaces. It has trouble processing utterances.
				.replaceAll("-", " ")
				// The synthesizer seems to treat an ellipsis as nothing. Replace it with a period.
				.replaceAll("\\.\\.\\.", ". ")
		);
		this.sender = sender;
	}
	
	public static Dialog getCurrentDialog(Client client) {
		if (!isHidden(client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT))) {
			return new Dialog(getWidgetText(client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT)), client.getLocalPlayer().getName());
		} else if (!isHidden(client.getWidget(WidgetInfo.DIALOG_NPC_TEXT))) {
			return new Dialog(getWidgetText(client.getWidget(WidgetInfo.DIALOG_NPC_TEXT)), getWidgetText(client.getWidget(WidgetInfo.DIALOG_NPC_NAME)));
		} else if (!isHidden(client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS))) {
			return new Dialog(getWidgetText(client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS)), "");
		} else if (!isHidden(client.getWidget(WidgetInfo.LEVEL_UP_SKILL))) {
			return new Dialog(getWidgetText(client.getWidget(WidgetInfo.LEVEL_UP_SKILL)), getWidgetText(client.getWidget(WidgetInfo.LEVEL_UP_LEVEL)));
		} else if (!isHidden(client.getWidget(WidgetInfo.PACK(229, 1)))) { // Cat age
			return new Dialog(getWidgetText(client.getWidget(WidgetInfo.PACK(229, 1))), "");
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
