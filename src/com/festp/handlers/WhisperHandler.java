package com.festp.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.utils.Link;
import com.festp.utils.LinkUtils;
import com.festp.utils.RawJsonUtils;

public class WhisperHandler implements Listener
{
	private JavaPlugin plugin;
	private boolean isVanilla;
	
	private String color = ChatColor.GRAY.toString() + ChatColor.ITALIC.toString();
	private String colorTags = "\"color\":\"gray\",\"italic\":\"true\",";
	
	public WhisperHandler(JavaPlugin plugin, boolean isVanilla)
	{
		this.plugin = plugin;
		this.isVanilla = isVanilla;
	}
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event)
	{
		String command = event.getMessage();
		
		if (!isWhisperCommand(command))
			return;

		int[] indices = selectRecipients(command);
		if (indices == null)
			return;
		Player[] recipients = getRecipients(command.substring(indices[0], indices[1]));
		if (recipients == null || recipients.length == 0)
			return;
		
		String message = command.substring(indices[1]).trim();
		if (message == "")
			return;
		
		Link link = LinkUtils.selectLink(message, 0);
		if (link == null)
			return;
		
		String nameFrom = event.getPlayer().getDisplayName();
		if (isVanilla)
		{
			String fromStr = "commands.message.display.outgoing"; // "You whisper to %s: %s"
			String toStr = "commands.message.display.incoming"; // "%s whispers to you: %s"
			
			StringBuilder modifiedMessage = new StringBuilder();
			modifiedMessage.append("[");
			RawJsonUtils.appendMessage(modifiedMessage, message, link, color);
			modifiedMessage.deleteCharAt(modifiedMessage.length() - 1);
			modifiedMessage.append("],");
			
			StringBuilder wrapNameFrom = new StringBuilder();
			RawJsonUtils.appendPlayer(wrapNameFrom, event.getPlayer(), color);
			
			for (Player recipient : recipients)
			{
				// lose vanilla translations
				String nameTo = recipient.getDisplayName();
				StringBuilder wrapNameTo = new StringBuilder();
				RawJsonUtils.appendPlayer(wrapNameTo, recipient, color);
				final StringBuilder from = new StringBuilder("tellraw ");
				from.append(nameFrom);
				from.append(" ");
				RawJsonUtils.appendTranslated(from, fromStr, new CharSequence[] { wrapNameTo, modifiedMessage }, colorTags);
				from.deleteCharAt(from.length() - 1);
				executeCommand(from.toString());
				
				final StringBuilder to = new StringBuilder("tellraw ");
				to.append(nameTo);
				to.append(" ");
				RawJsonUtils.appendTranslated(to, toStr, new CharSequence[] { wrapNameFrom, modifiedMessage }, colorTags);
				to.deleteCharAt(to.length() - 1);
				executeCommand(to.toString());
			}
			
			event.setCancelled(true);
		}
		else
		{
			// send extra message including the link list
			final StringBuilder linkCommand = new StringBuilder("tellraw ");
			linkCommand.append(nameFrom);
			linkCommand.append(" [");
			RawJsonUtils.appendJoinedLinks(linkCommand, message, link, color, ", ");
			linkCommand.deleteCharAt(linkCommand.length() - 1);
			linkCommand.append("]");
			executeCommand(linkCommand.toString());
			String prev = nameFrom;
			int replaceStart = linkCommand.indexOf(prev);
			for (Player recipient : recipients)
			{
				if (recipient == event.getPlayer())
					continue;
				String cur = recipient.getDisplayName();
				linkCommand.replace(replaceStart, replaceStart + prev.length(), cur);
				executeCommand(linkCommand.toString());
				prev = cur;
			}
		}
	}
	
	private void executeCommand(String command)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
			}
		});
	}

	private static boolean isWhisperCommand(String command)
	{
		return command.startsWith("/w") || command.startsWith("/msg") || command.startsWith("/tell");
	}

	private static int[] selectRecipients(String command)
	{
		// TODO selectors!!! @a[...] @p[...] @r[...] containing spaces
		int indexStart = command.indexOf(" ");
		if (indexStart < 0)
			return null;
		indexStart++;
		int indexEnd = command.indexOf(" ", indexStart);
		while (indexEnd == indexStart) {
			indexStart = indexEnd + 1;
			indexEnd = command.indexOf(" ", indexStart);
		}
		if (indexEnd < 0)
			return null;
		return new int[] { indexStart, indexEnd };
	}
	
	private static Player[] getRecipients(String selector)
	{
		// TODO selectors!!! @a[...] @p[...] @r[...]
		Player recipient = tryGetPlayer(selector);
		if (recipient == null)
			return null;
		return new Player[] { recipient };
	}

	private static Player tryGetPlayer(String name)
	{
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getDisplayName().equals(name))
				return p;
		return null;
	}
}