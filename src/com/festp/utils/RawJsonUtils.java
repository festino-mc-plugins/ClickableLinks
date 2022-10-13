package com.festp.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RawJsonUtils
{
	public static String tryWrap(String str)
	{
		return tryWrap(str, "");
	}
	public static String tryWrap(String str, String color)
	{
		return str == "" ? "" : wrap(str, color);
	}
	public static String wrap(String str, String color)
	{
		return "{\"text\":\"" + color + str + "\"},";
	}

	public static void appendSender(StringBuilder command, CommandSender sender, String color)
	{
		if (sender instanceof Player)
			RawJsonUtils.appendPlayer(command, (Player)sender, color);
		else
			command.append(RawJsonUtils.wrap(sender.getName(), color));
	}
	public static void appendPlayer(StringBuilder command, Player player, String color)
	{
		String nickname = player.getDisplayName();
		String uuid = player.getUniqueId().toString();
		command.append("{\"text\":\"" + color + nickname + "\",");
		command.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + nickname + "\\nType: Player\\n" + uuid + "\"},");
		command.append("\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell " + nickname + " \"}");
		command.append("},");
	}
	
	public static void appendMessage(StringBuilder command, String message, Link firstLink, String color)
	{
		int lastIndex = 0;
		Link link = firstLink;
		while (link != null)
		{
			if (lastIndex < link.beginIndex) {
				command.append(tryWrap(message.substring(lastIndex, link.beginIndex), color));
			}
			
			appendLink(command, link, color);
			
			lastIndex = link.endIndex;
			link = LinkUtils.selectLink(message, lastIndex);
		}
		if (lastIndex < message.length()) {
			command.append(tryWrap(message.substring(lastIndex), color));
		}
	}
	
	public static void appendJoinedLinks(StringBuilder command, String message, Link firstLink, String color, String sep)
	{
		Link link = firstLink;
		boolean isFirst = true;
		String wrappedSep = tryWrap(sep, color);
		while (link != null)
		{
			if (isFirst) {
				isFirst = false;
			}
			else {
				command.append(wrappedSep);
			}
			appendLink(command, link, color);
			
			link = LinkUtils.selectLink(message, link.endIndex);
		}
	}
	
	public static void appendLink(StringBuilder command, Link link, String color)
	{
		String linkStr = link.getString();
		String linkClick = applyBrowserEncoding(linkStr);
		if (!link.hasProtocol)
			linkClick = "https://" + linkClick;
		command.append("{\"text\":\"" + color + ChatColor.UNDERLINE + linkStr + "\","
				+ "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + linkClick + "\"}},");
	}

	public static String applyBrowserEncoding(String str)
	{
		try {
			HashMap<Character, String> encoding = new HashMap<>();
			int length = str.length();
			for (int i = 0; i < length; i++)
			{
				char c = str.charAt(i);
				if (encoding.containsKey(c))
					continue;
				// not very optimized?
				String encoded = URLEncoder.encode("" + c, StandardCharsets.UTF_8.toString());
				encoding.put(c, encoded);
			}

			StringBuilder res = new StringBuilder();
			for (int i = 0; i < length; i++)
			{
				char c = str.charAt(i);
				if (c <= 255)
					res.append(c);
				else
					res.append(encoding.get(c));
			}
			
			return res.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return str;
		}
	}
	
	/** Check for more info: <a>https://minecraft.fandom.com/wiki/Raw_JSON_text_format#Translated_Text</a> */
	public static void appendTranslated(StringBuilder command, String identifier, CharSequence[] textComponents, CharSequence colorTags)
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
		for (CharSequence component : textComponents) {
			command.append(component);
		}
		command.deleteCharAt(command.length() - 1);
		command.append("]},");
	}
}
