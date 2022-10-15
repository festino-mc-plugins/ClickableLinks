package com.festp.handlers;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import com.festp.Chatter;
import com.festp.utils.Link;
import com.festp.utils.LinkUtils;

public class SmallCommandsHandler implements Listener
{
	private static final String ME_COMMAND = "/me";
	private static final String SAY_COMMAND = "/say";
	
	private Chatter chatter;
	
	public SmallCommandsHandler(Chatter chatter)
	{
		this.chatter = chatter;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerCommand(ServerCommandEvent event)
	{
		onCommand(event, event.getSender(), "/" + event.getCommand());
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event)
	{
		onCommand(event, event.getPlayer(), event.getMessage());
	}

	private void onCommand(Cancellable event, CommandSender sender, String command)
	{
		if (event.isCancelled())
			return;
		
		if (isMeCommand(command))
			onMeCommand(event, sender, command);
		else if (isSayCommand(command))
			onSayCommand(event, sender, command);
	}

	private void onMeCommand(Cancellable event, CommandSender sender, String command)
	{
		String message = command.substring(ME_COMMAND.length()).trim();
		if (message == "")
			return;
		
		Link link = LinkUtils.selectLink(message, 0);
		if (link == null)
			return;
		
		chatter.sendFormatted(null, sender, message, "* %1$s %2$s", link, false);
		event.setCancelled(true);
	}

	private void onSayCommand(Cancellable event, CommandSender sender, String command)
	{
		String message = command.substring(SAY_COMMAND.length()).trim();
		if (message == "")
			return;
		
		Link link = LinkUtils.selectLink(message, 0);
		if (link == null)
			return;

		chatter.sendFormatted(null, sender, message, "[%1$s] %2$s", link, false);
		event.setCancelled(true);
	}

	private boolean isMeCommand(String command) {
		return command.startsWith(ME_COMMAND);
	}
	private boolean isSayCommand(String command) {
		return command.startsWith(SAY_COMMAND);
	}
}
