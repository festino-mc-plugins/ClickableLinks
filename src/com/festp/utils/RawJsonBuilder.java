package com.festp.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.festp.Chatter;

public class RawJsonBuilder
{
	RawJsonBuilderSettings settings;
	StringBuilder command;
	
	public RawJsonBuilder(RawJsonBuilderSettings settings)
	{
		this.settings = settings;
		command = new StringBuilder();
	}
	public RawJsonBuilder(RawJsonBuilderSettings settings, CharSequence c)
	{
		this.settings = settings;
		command = new StringBuilder(c);
	}
	
	public void tryWrap(String str, String color)
	{
		command.append(tryGetWrapped(str, color));
	}
	public String tryGetWrapped(String str, String color)
	{
		return str == "" ? "" : getWrapped(str, color);
	}
	public void wrap(String str, String color)
	{
		command.append(getWrapped(str, color));
	}
	private String getWrapped(String str, String color)
	{
		return "{\"text\":\"" + color + str + "\"},";
	}

	public void appendSender(CommandSender sender, String color)
	{
		if (sender instanceof Player)
			appendPlayer((Player)sender, color);
		else {
			String name = Chatter.getName(sender);
			wrap(name, color);
		}
	}
	public void appendPlayer(Player player, String color)
	{
		String nickname = player.getDisplayName();
		String uuid = player.getUniqueId().toString();
		command.append("{\"text\":\"" + color + nickname + "\",");
		command.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + nickname + "\\nType: Player\\n" + uuid + "\"},");
		command.append("\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell " + nickname + " \"}");
		command.append("},");
	}
	
	public void appendMessage(String message, Link firstLink, String color)
	{
        startList();
        
		int lastIndex = 0;
		Link link = firstLink;
		while (link != null)
		{
			if (lastIndex < link.beginIndex) {
				tryWrap(message.substring(lastIndex, link.beginIndex), color);
			}
			
			appendLink(link, color);
			
			lastIndex = link.endIndex;
			link = LinkUtils.selectLink(message, lastIndex);
		}
		if (lastIndex < message.length()) {
			tryWrap(message.substring(lastIndex), color);
		}
		
        endList();
	}
	
	public void appendJoinedLinks(String message, Link firstLink, String color, String sep)
	{
        startList();
        
		Link link = firstLink;
		boolean isFirst = true;
		String wrappedSep = tryGetWrapped(sep, color);
		while (link != null)
		{
			if (isFirst) {
				isFirst = false;
			}
			else {
				command.append(wrappedSep);
			}
			appendLink(link, color);
			
			link = LinkUtils.selectLink(message, link.endIndex);
		}

        endList();
	}
	
	public void appendLink(Link link, String color)
	{
		String linkStr = link.getString();
		String linkClick = LinkUtils.applyBrowserEncoding(linkStr);
		if (!link.hasProtocol)
			linkClick = "https://" + linkClick;
		command.append("{\"text\":\"");
		command.append(color);
		if (settings.isLinkUnderlined)
			command.append(ChatColor.UNDERLINE);
		command.append(linkStr);
		command.append("\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"");
		command.append(linkClick);
		command.append("\"}},");
	}
	
	/** Check for more info: <a>https://minecraft.fandom.com/wiki/Raw_JSON_text_format#Translated_Text</a> */
	public void appendTranslated(String identifier, CharSequence[] textComponents, CharSequence colorTags)
	{
		command.append("{");
		command.append(colorTags);
		command.append("\"translate\":\"");
		command.append(identifier);
		command.append("\"");
		if (textComponents == null || textComponents.length == 0) {
			command.append("},");
			return;
		}
		command.append(",\"with\":[");
		int n = 0;
		for (CharSequence component : textComponents) {
			if (n > 0)
				command.append(',');
			command.append(component);
			n++;
		}
		command.append("]},");
	}
	
	public void startList()
	{
		command.append("[");
	}
	public void endList()
	{
		tryRemoveComma();
		command.append("]");
	}
	public String build()
	{
		tryRemoveComma();
		return command.toString();
	}
	private void tryRemoveComma() {
		int index = command.length() - 1;
		if (index >= 0 && command.charAt(index) == ',')
			command.deleteCharAt(index);
	}
	public StringBuilder releaseStringBuilder() {
		tryRemoveComma();
		StringBuilder res = command;
		command = null;
		return res;
	}
	public void append(String str) {
		command.append(str);
	}
}
