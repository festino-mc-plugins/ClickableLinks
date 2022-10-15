package com.festp.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import com.festp.Chatter;
import com.festp.config.Config;
import com.festp.utils.Link;
import com.festp.utils.LinkUtils;
import com.festp.utils.RawJsonBuilder;

public class WhisperHandler implements Listener
{
	private Chatter chatter;
	private Config config;
	
	private String color = ChatColor.GRAY.toString() + ChatColor.ITALIC.toString();
	private String colorTags = "\"color\":\"gray\",\"italic\":\"true\",";
	
	public WhisperHandler(Chatter chatter, Config config)
	{
		this.chatter = chatter;
		this.config = config;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onServerCommand(ServerCommandEvent event)
	{
		onCommand(event, event.getSender(), "/" + event.getCommand());
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event)
	{
		onCommand(event, event.getPlayer(), event.getMessage());
	}
	
	private void onCommand(Cancellable event, CommandSender sender, String command)
	{
		if (!config.get(Config.Key.IS_WHISPER, true))
			return;
		if (event.isCancelled() && config.get(Config.Key.IS_VANILLA_WHISPER, true))
			return;
		
		if (!isWhisperCommand(command))
			return;

		int[] indices = selectRecipients(command);
		if (indices == null)
			return;
		// if is not vanilla, recipients list may be invalid
		Player[] recipients = getRecipients(command.substring(indices[0], indices[1]), sender);
		if (recipients == null || recipients.length == 0)
			return;
		
		String message = command.substring(indices[1]).trim();
		if (message == "")
			return;
		
		Link link = LinkUtils.selectLink(message, 0);
		if (link == null)
			return;
		
		String nameFrom = Chatter.getName(sender);
		if (config.get(Config.Key.IS_VANILLA_WHISPER, true))
		{
			String fromStr = "commands.message.display.outgoing"; // "You whisper to %s: %s"
			String toStr = "commands.message.display.incoming"; // "%s whispers to you: %s"
			
			RawJsonBuilder builder = new RawJsonBuilder(config.getBuilderSettings());
			//builder.startList();
			builder.appendMessage(message, link, color);
			//builder.endList();
			StringBuilder modifiedMessage = builder.releaseStringBuilder();
			modifiedMessage.append(',');

			builder = new RawJsonBuilder(config.getBuilderSettings());
			builder.appendSender(sender, color);
			StringBuilder wrapNameFrom = builder.releaseStringBuilder();
			
			for (Player recipient : recipients)
			{
				String nameTo = recipient.getPlayerListName();
				builder = new RawJsonBuilder(config.getBuilderSettings());
				builder.appendPlayer(recipient, color);
				StringBuilder wrapNameTo = builder.releaseStringBuilder();

				RawJsonBuilder from = new RawJsonBuilder(config.getBuilderSettings(), "tellraw ");
				from.append(nameFrom);
				from.append(" ");
				from.appendTranslated(fromStr, new CharSequence[] { wrapNameTo, modifiedMessage }, colorTags);
				chatter.executeCommand(from.build());
				
				RawJsonBuilder to = new RawJsonBuilder(config.getBuilderSettings(), "tellraw ");
				to.append(nameTo);
				to.append(" ");
				to.appendTranslated(toStr, new CharSequence[] { wrapNameFrom, modifiedMessage }, colorTags);
				chatter.executeCommand(to.build());
			}
			
			event.setCancelled(true);
		}
		else
		{
			// send extra message including the link list
			RawJsonBuilder builder = new RawJsonBuilder(config.getBuilderSettings(), "tellraw ");
			builder.append(nameFrom);
			builder.append(" ");
			builder.appendJoinedLinks(message, link, color, ", ");
			StringBuilder linkCommand = builder.releaseStringBuilder();
			
			chatter.executeCommand(linkCommand.toString());
			
			String prev = nameFrom;
			int replaceStart = linkCommand.indexOf(prev);
			for (Player recipient : recipients)
			{
				if (recipient == sender)
					continue;
				String cur = recipient.getPlayerListName();
				linkCommand.replace(replaceStart, replaceStart + prev.length(), cur);
				chatter.executeCommand(linkCommand.toString());
				prev = cur;
			}
		}
	}

	private static boolean isWhisperCommand(String command)
	{
		// EssentialsX Chat: aliases: [w,m,t,pm,emsg,epm,tell,etell,whisper,ewhisper]
		// (https://github.com/EssentialsX/Essentials/blob/f7cbc7b0d37ea7a674955758da099524b009ad03/Essentials/src/main/resources/plugin.yml)
		// (https://github.com/EssentialsX/Essentials/blob/f7cbc7b0d37ea7a674955758da099524b009ad03/Essentials/src/main/resources/config.yml)
		return command.startsWith("/w") || command.startsWith("/msg") || command.startsWith("/tell");
	}

	private static int[] selectRecipients(String command)
	{
		int indexStart = command.indexOf(" ");
		if (indexStart < 0)
			return null;
		indexStart++;
		
		int length = command.length();
		while (indexStart < length && command.charAt(indexStart) == ' ')
			indexStart++;
		if (indexStart >= length)
			return null;
		
		int indexEnd;
		if (command.charAt(indexStart) != '@' || indexStart + 2 >= length) {
			indexEnd = command.indexOf(" ", indexStart);
			indexEnd = indexEnd < 0 ? length : indexEnd;
			return new int[] { indexStart, indexEnd };
		}
		
		indexEnd = indexStart + 2;
		if (command.charAt(indexEnd) != '[') {
			if (command.charAt(indexEnd) == ' ')
				return new int[] { indexStart, indexEnd };
			return null;
		}
		
		indexEnd++;
		int openedBrackets = 1;
		while (indexEnd < length && openedBrackets > 0) {
			char c = command.charAt(indexEnd);
			if (c == '[')
				openedBrackets++;
			else if (c == ']')
				openedBrackets--;
			indexEnd++;
		}
		return new int[] { indexStart, indexEnd };
	}
	
	private static Player[] getRecipients(String selector, CommandSender sender)
	{
		if (selector.charAt(0) != '@') {
			Player recipient = tryGetPlayer(selector);
			if (recipient == null)
				return null;
			return new Player[] { recipient };
		}
		
		List<Entity> entities = Bukkit.getServer().selectEntities(sender, selector);
		List<Player> players = new ArrayList<>();
		for (Entity e : entities)
			if (e instanceof Player)
				players.add((Player)e);
		return players.toArray(new Player[0]);
	}

	private static Player tryGetPlayer(String name)
	{
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getPlayerListName().equals(name))
				return p;
		return null;
	}
}
