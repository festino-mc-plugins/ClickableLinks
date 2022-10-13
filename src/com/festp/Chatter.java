package com.festp;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.utils.Link;
import com.festp.utils.RawJsonUtils;

public class Chatter
{
	private static final String PLACEHOLDER_NAME = "%1$s";
	private static final String PLACEHOLDER_MESSAGE = "%2$s";
	
	private JavaPlugin plugin;
	
	public Chatter(JavaPlugin plugin)
	{
		this.plugin = plugin;
	}
	
	public void executeCommand(String command)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
			}
		});
	}

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
	 * 
	 * @param recipients is <i>null</i> if all the players will get the message; console will always get the message
	 * @param sender is message sender
	 * @param message is full message containing link(s)
	 * @param format use "%1$s" for the sender name and "%2$s" for the message, i.e. "<%1$s> %2$s"
	 * @param link is the first link found*/
	public void sendFormatted(final Set<Player> recipients, CommandSender sender, String message, String format, Link link)
	{
		boolean isEveryone = true;
		if (recipients != null)
		{
			Set<Player> onlinePlayers = new HashSet<Player>(Bukkit.getOnlinePlayers());
			for (Player p : recipients)
				onlinePlayers.remove(p);
			isEveryone = onlinePlayers.isEmpty();
		}
		final boolean f_isEveryone = isEveryone;
		
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
	        	RawJsonUtils.appendSender(command, sender, "");
	        if (placeholder.equals(PLACEHOLDER_MESSAGE))
	        	RawJsonUtils.appendMessage(command, message, link, lastColor);
	        
	        prevEnd = end;
	    }
        command.append(RawJsonUtils.tryWrap(format.substring(prevEnd)));
		// extra comma - every function places it
		command.deleteCharAt(command.length() - 1);
		command.append("]");
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				if (f_isEveryone) {
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
		
		String consoleMessage = format.replace(PLACEHOLDER_NAME, getName(sender)).replace(PLACEHOLDER_MESSAGE, message);
		Bukkit.getConsoleSender().sendMessage(consoleMessage);
	}
	
	public static String getName(CommandSender sender)
	{
		if (sender instanceof Player)
			return ((Player)sender).getDisplayName();
		if (sender instanceof ConsoleCommandSender)
			return "Server";
		return sender.getName();
	}
}
