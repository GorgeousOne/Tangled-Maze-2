package me.gorgeousone.tangledmaze.util.text;

import org.bukkit.command.CommandSender;

public class TextException extends Exception {
	
	private final Text text;
	private final Placeholder[] placeholders;
	
	public TextException(Text message, Placeholder... placeholders) {
		this.text = message;
		this.placeholders = placeholders;
	}
	
	public void sendTextTo(CommandSender receiver) {
		text.sendTo(receiver, placeholders);
	}
}
