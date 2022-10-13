package com.festp.handlers;

import java.util.HashSet;
import java.util.Set;
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
import com.festp.utils.RawJsonUtils;

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
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnChat(AsyncPlayerChatEvent event)
	{
		String message = event.getMessage();
		
		Link link = LinkUtils.selectLink(message, 0);
		if (link == null)
			return;
		
		String format = event.getFormat(); // something like "<%1$s> %2$s"
		final StringBuilder command = new StringBuilder("tellraw @a [");
		Pattern pattern = Pattern.compile("[%][\\d][$][s]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(format);
		int prevEnd = 0;
		String lastColor = ChatColor.RESET.toString();
	    while (matcher.find())
	    {
	        int start = matcher.start();
	        int end = matcher.end();
	        command.append(RawJsonUtils.tryWrap(format.substring(prevEnd, start)));
	        
	        int colorIndex = format.lastIndexOf('§', start);
	        if (0 <= colorIndex && colorIndex + 2 <= end)
	        	lastColor = format.substring(colorIndex, colorIndex + 2);
	        
	        String placeholder = format.substring(start, end);
	        if (placeholder.equals(PLACEHOLDER_NAME))
	        	RawJsonUtils.appendPlayer(command, event.getPlayer());
	        if (placeholder.equals(PLACEHOLDER_MESSAGE))
	        	RawJsonUtils.appendMessage(command, message, link, lastColor);
	        
	        prevEnd = end;
	    }
        command.append(RawJsonUtils.tryWrap(format.substring(prevEnd)));
		// extra comma - every function places it
		command.deleteCharAt(command.length() - 1);
		command.append("]");

		final Set<Player> recipients = event.getRecipients();
		Set<Player> onlinePlayers = new HashSet<Player>(Bukkit.getOnlinePlayers());
		for (Player p : recipients)
			onlinePlayers.remove(p);
		boolean isEveryone = onlinePlayers.isEmpty();
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				if (isEveryone) {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.toString());
				}
				else {
					String prev = "@a";
					int replaceStart = command.indexOf(prev);
					for (Player p : recipients) {
						String cur = p.getDisplayName();
						command.replace(replaceStart, replaceStart + prev.length(), cur);
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.toString());
						prev = cur;
					}
				}
			}
		});
		
		String consoleMessage = format.replace(PLACEHOLDER_NAME, event.getPlayer().getDisplayName()).replace(PLACEHOLDER_MESSAGE, message);
		Bukkit.getConsoleSender().sendMessage(consoleMessage);
		
		event.setCancelled(true);
	}
}
