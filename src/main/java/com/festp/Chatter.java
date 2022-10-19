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

import com.festp.config.Config;
import com.festp.utils.Link;
import com.festp.utils.RawJsonBuilder;

public class Chatter
{
	private static final String PLACEHOLDER_NAME = "%1$s";
	private static final String PLACEHOLDER_MESSAGE = "%2$s";
	
	private JavaPlugin plugin;
	private Config config;
	
	public Chatter(JavaPlugin plugin, Config config)
	{
		this.plugin = plugin;
		this.config = config;
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
	public void sendFormatted(Set<Player> recipients, CommandSender sender, String message, String format, Iterable<Link> links, boolean sendToConsole)
	{
		if (sendToConsole)
		{
			String consoleMessage = format.replace(PLACEHOLDER_NAME, getName(sender)).replace(PLACEHOLDER_MESSAGE, message);
			Bukkit.getConsoleSender().sendMessage(consoleMessage);
		}
		
		// check if actually no recipients
		if (recipients != null && recipients.isEmpty() || Bukkit.getOnlinePlayers().size() == 0)
			return;
		
		if (recipients == null)
		{
			recipients = new HashSet<>(Bukkit.getOnlinePlayers());
		}
		
		final RawJsonBuilder builder = new RawJsonBuilder(config.getBuilderSettings());
		builder.startList();
		Pattern pattern = Pattern.compile("[%][\\d][$][s]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(format);
		int prevEnd = 0;
		String lastColor = ChatColor.RESET.toString();
		while (matcher.find())
		{
			int start = matcher.start();
			int end = matcher.end();
			builder.tryWrap(format.substring(prevEnd, start), "");

			int colorIndex = format.lastIndexOf(ChatColor.COLOR_CHAR, start);
			if (0 <= colorIndex && colorIndex + 2 <= end)
				lastColor = format.substring(colorIndex, colorIndex + 2);

			String placeholder = format.substring(start, end);
			if (placeholder.equals(PLACEHOLDER_NAME))
				builder.appendSender(sender, "");
			if (placeholder.equals(PLACEHOLDER_MESSAGE))
				builder.appendMessage(message, links, lastColor);

			prevEnd = end;
		}
		builder.tryWrap(format.substring(prevEnd), "");
		builder.endList();

		String rawJson = builder.releaseStringBuilder().toString();
		for (Player p : recipients) {
			sendRawJson(p, rawJson);
		}
	}
	
	public void sendWhisperMessage(CommandSender sender, Player[] recipients, String message, Iterable<Link> links, String color)
	{
		String fromStr = "commands.message.display.outgoing"; // "You whisper to %s: %s"
		String toStr = "commands.message.display.incoming"; // "%s whispers to you: %s"
		
		RawJsonBuilder builder = new RawJsonBuilder(config.getBuilderSettings());
		builder.appendMessage(message, links, color);
		StringBuilder modifiedMessage = builder.releaseStringBuilder();

		builder = new RawJsonBuilder(config.getBuilderSettings());
		builder.appendSender(sender, color);
		StringBuilder wrapNameFrom = builder.releaseStringBuilder();
		
		for (Player recipient : recipients)
		{
			if (sender instanceof Player)
			{
				builder = new RawJsonBuilder(config.getBuilderSettings());
				builder.appendPlayer(recipient, color);
				StringBuilder wrapNameTo = builder.releaseStringBuilder();
				
				RawJsonBuilder from = new RawJsonBuilder(config.getBuilderSettings());
				from.appendTranslated(fromStr, new CharSequence[] { wrapNameTo, modifiedMessage }, color);
				sendRawJson((Player)sender, from.build());
			}
			
			RawJsonBuilder to = new RawJsonBuilder(config.getBuilderSettings());
			to.appendTranslated(toStr, new CharSequence[] { wrapNameFrom, modifiedMessage }, color);
			sendRawJson(recipient, to.build());
		}
	}
	
	public void sendOnlyLinks(CommandSender sender, Player[] recipients, String message, Iterable<Link> links, String color)
	{
		RawJsonBuilder builder = new RawJsonBuilder(config.getBuilderSettings());
		builder.appendJoinedLinks(message, links, color, ", ");
		String linkCommand = builder.build();
		
		if (sender instanceof Player)
			sendRawJson((Player)sender, linkCommand);
		
		for (Player recipient : recipients)
		{
			if (recipient == sender)
				continue;
			sendRawJson(recipient, linkCommand);
		}
	}
	
	public static String getName(CommandSender sender)
	{
		if (sender instanceof Player)
			return ((Player)sender).getPlayerListName();
		if (sender instanceof ConsoleCommandSender)
			return "Server";
		return sender.getName();
	}
	
	private void sendRawJson(Player player, String rawJsonMessage)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				StringBuilder command = new StringBuilder("tellraw ");
				command.append(getName(player));
				command.append(' ');
				command.append(rawJsonMessage);
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.toString());
			}
		});
	}
}
