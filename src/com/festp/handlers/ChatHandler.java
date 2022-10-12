package com.festp.handlers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.utils.Link;
import com.festp.utils.LinkUtils;

public class ChatHandler implements Listener
{
	private final JavaPlugin plugin;
	private static final String PLACEHOLDER_NAME = "%1$s";
	private static final String PLACEHOLDER_MESSAGE = "%2$s";
	
	public ChatHandler(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	// may be check if in https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains
	
	/**
	 * Generates command for chat messages containing links<br>
	 * {@literal<FEST_Channel>} test https://www.netflix.com/browse extra text<br>
	 * like<br>
	 * /tellraw @a [<br>
	 * {"text":"<"},<br>
	 * {"text":"FEST_Channel",<br>
	 * "hoverEvent":{"action":"show_text","value":"FEST_Channel\nType: Player\n4a9b60fa-6c37-3673-b0ae-02ee83a6356d"},<br>
	 * "clickEvent":{"action":"suggest_command","value":"/tell FEST_Channel"}},<br>
	 * {"text":"> test "},<br>
	 * {"text":"https://www.netflix.com/browse","underlined":true,<br>
	 * "clickEvent":{"action":"open_url","value":"https://www.netflix.com/browse"}},<br>
	 * {"text":" extra text"}<br>
	 * ]
	 * */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void OnChat(AsyncPlayerChatEvent event)
	{
		String message = event.getMessage();
		
		Link link = LinkUtils.selectLink(message, 0);
		if (link == null)
			return;
		
		String format = event.getFormat(); // something like "<%1$s> %2$s"
		StringBuilder command = new StringBuilder("tellraw @a [");
		Pattern pattern = Pattern.compile("[%][\\d][$][s]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(format);
		int prevEnd = 0;
		String lastColor = ChatColor.RESET.toString();
	    while (matcher.find())
	    {
	        int start = matcher.start();
	        int end = matcher.end();
	        command.append(tryWrap(format.substring(prevEnd, start)));
	        
	        int colorIndex = format.lastIndexOf('§', start);
	        if (0 <= colorIndex && colorIndex + 2 <= end)
	        	lastColor = format.substring(colorIndex, colorIndex + 2);
	        
	        String placeholder = format.substring(start, end);
	        if (placeholder.equals(PLACEHOLDER_NAME))
				appendPlayer(command, event.getPlayer());
	        if (placeholder.equals(PLACEHOLDER_MESSAGE))
				appendMessage(command, message, link, lastColor);
	        
	        prevEnd = end;
	    }
        command.append(tryWrap(format.substring(prevEnd)));
		// extra comma - every function places it
		command.deleteCharAt(command.length() - 1);
		command.append("]");
		
		final String bukkitCommand = command.toString();
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), bukkitCommand);
			}
		});
		
		String consoleMessage = format.replace(PLACEHOLDER_NAME, event.getPlayer().getDisplayName()).replace(PLACEHOLDER_MESSAGE, message);
		Bukkit.getConsoleSender().sendMessage(consoleMessage);
		
		event.setCancelled(true);
	}

	
	private static String tryWrap(String str)
	{
		return tryWrap(str, "");
	}
	private static String tryWrap(String str, String color)
	{
		return str == "" ? "" : "{\"text\":\"" + color + str + "\"},";
	}
	
	private void appendPlayer(StringBuilder command, Player player)
	{
		String nickname = player.getDisplayName();
		String uuid = player.getUniqueId().toString();
		command.append("{\"text\":\"" + nickname + "\",");
		command.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + nickname + "\\nType: Player\\n" + uuid + "\"},");
		command.append("\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell " + nickname + " \"}");
		command.append("},");
	}
	
	private void appendMessage(StringBuilder command, String message, Link firstLink, String color)
	{
		int lastIndex = 0;
		Link link = firstLink;
		while (link != null)
		{
			if (lastIndex < link.beginIndex) {
				command.append(tryWrap(message.substring(lastIndex, link.beginIndex), color));
			}
			
			String linkStr = link.getString();
			String linkClick = applyBrowserEncoding(linkStr);
			if (!link.hasProtocol)
				linkClick = "https://" + linkClick;
			command.append("{\"text\":\"" + color + ChatColor.UNDERLINE + linkStr + "\","
					+ "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + linkClick + "\"}},");
			
			lastIndex = link.endIndex;
			link = LinkUtils.selectLink(message, lastIndex);
		}
		if (lastIndex < message.length()) {
			command.append(tryWrap(message.substring(lastIndex), color));
		}
	}
	
	private String applyBrowserEncoding(String str)
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
