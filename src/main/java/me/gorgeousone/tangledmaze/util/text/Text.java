package me.gorgeousone.tangledmaze.util.text;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Text {
	
	private String[] paragraphs;
	
	public Text(String message) {
		setText(message);
	}
	
	private void setText(String message) {
		paragraphs = ChatColor.translateAlternateColorCodes('&', message).split("\\\\n");
		
		if (paragraphs.length > 1) {
			for (int i = 1; i < paragraphs.length; i++) {
				paragraphs[i] = ChatColor.getLastColors(paragraphs[i - 1]) + paragraphs[i];
			}
		}
	}
	
	public String[] getParagraphs() {
		return paragraphs;
	}
	
	public void sendTo(CommandSender receiver) {
		for (String paragraph : paragraphs) {
			receiver.sendMessage(paragraph);
		}
	}
	
	public void sendTo(CommandSender receiver, Placeholder... placeholders) {
		for (String paragraph : paragraphs) {
			String alteredParagraph = paragraph;
			
			for (Placeholder placeholder : placeholders) {
				alteredParagraph = placeholder.apply(alteredParagraph);
			}
			receiver.sendMessage(alteredParagraph);
		}
	}
}