package me.gorgeousone.badapplemaze.util.text;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Text {
	
	private List<String> paragraphs;
	
	public Text(String message) {
		setText(message);
	}
	
	private void setText(String message) {
		String[] split = ChatColor.translateAlternateColorCodes('&', message).split("\\\\n");
		paragraphs = new ArrayList<>(Arrays.asList(split));
		
		if (paragraphs.size() > 1) {
			for (int i = 1; i < paragraphs.size(); i++) {
				paragraphs.set(i, ChatColor.getLastColors(paragraphs.get(i - 1)) + paragraphs.get(i));
			}
		}
	}
	
	public void add(Text other) {
		paragraphs.addAll(other.paragraphs);
	}
	
	public List<String> getParagraphs() {
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
