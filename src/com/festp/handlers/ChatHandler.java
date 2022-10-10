package com.festp.handlers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.Link;
import com.festp.LinkUtils;

public class ChatHandler implements Listener
{
	private final JavaPlugin plugin;
	
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
	@EventHandler
	public void OnChat(AsyncPlayerChatEvent event)
	{
		String message = event.getMessage();
		
		int lastIndex = 0;
		Link link = LinkUtils.selectLink(message, lastIndex);
		if (link == null)
			return;
		
		String nickname = event.getPlayer().getDisplayName();
		String uuid = event.getPlayer().getUniqueId().toString();
		String command = "tellraw @a [";
		command += "{\"text\":\"<\"},";
		command += "{\"text\":\"" + nickname + "\",";
		command += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + nickname + "\\nType: Player\\n" + uuid + "\"},";
		command += "\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell " + nickname + " \"}";
		command += "},";
		command += "{\"text\":\"> \"}";
		while (link != null)
		{
			if (lastIndex < link.beginIndex)
				command += ",{\"text\":\"" + message.substring(lastIndex, link.beginIndex) + "\"}";
			
			String linkStr = link.getString();
			String linkClick = applyBrowserEncoding(linkStr);
			if (!link.hasProtocol)
				linkClick = "https://" + linkClick;
			command += ",{\"text\":\"" + linkStr + "\",\"underlined\":true,"
					+ "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + linkClick + "\"}}";
			
			lastIndex = link.endIndex;
			link = LinkUtils.selectLink(message, lastIndex);
		}
		if (lastIndex < message.length())
		{
			command += ",{\"text\":\"" + message.substring(lastIndex) + "\"}";
		}
		command += "]";
		
		final String bukkitCommand = command;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), bukkitCommand);
			}
		});
		event.setCancelled(true);
	}
	
	private String applyBrowserEncoding(String str)
	{
		try {
			String r = URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
			return r.replace("%2F", "/");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return str;
		}
	}
}
