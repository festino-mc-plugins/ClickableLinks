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

		chatter.sendFormatted(event.getRecipients(), event.getPlayer(), message, event.getFormat(), link);
		event.setCancelled(true);
	}
}
