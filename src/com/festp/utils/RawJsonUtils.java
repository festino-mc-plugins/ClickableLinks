package com.festp.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RawJsonUtils
{
	public static String tryWrap(String str)
	{
		return tryWrap(str, "");
	}
	public static String tryWrap(String str, String color)
	{
		return str == "" ? "" : "{\"text\":\"" + color + str + "\"},";
	}
	
	public static void appendPlayer(StringBuilder command, Player player)
	{
		String nickname = player.getDisplayName();
		String uuid = player.getUniqueId().toString();
		command.append("{\"text\":\"" + nickname + "\",");
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

}
