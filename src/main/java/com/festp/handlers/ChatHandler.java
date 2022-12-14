package com.festp.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.festp.Chatter;
import com.festp.utils.Link;
import com.festp.utils.LinkUtils;

public class ChatHandler implements Listener
{
	private final Chatter chatter;
	
	public ChatHandler(Chatter chatter) {
		this.chatter = chatter;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnChat(AsyncPlayerChatEvent event)
	{
		String message = event.getMessage();
		
		Iterable<Link> links = LinkUtils.findLinks(message);
		if (links == null)
			return;

		chatter.sendFormatted(event.getRecipients(), event.getPlayer(), message, event.getFormat(), links, true);
		event.setCancelled(true);
	}
}
