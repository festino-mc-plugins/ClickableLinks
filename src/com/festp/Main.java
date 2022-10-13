package com.festp;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.handlers.ChatHandler;
import com.festp.handlers.SmallCommandsHandler;
import com.festp.handlers.WhisperHandler;

public class Main extends JavaPlugin
{
	public void onEnable()
	{
		Logger.setLogger(getLogger());
    	PluginManager pm = getServer().getPluginManager();
    	
    	Chatter chatter = new Chatter(this);
    	
    	ChatHandler chatHandler = new ChatHandler(chatter);
    	pm.registerEvents(chatHandler, this);
    	
    	WhisperHandler whisperHandler = new WhisperHandler(chatter, true);
    	pm.registerEvents(whisperHandler, this);
    	
    	SmallCommandsHandler smallHandler = new SmallCommandsHandler(chatter);
    	pm.registerEvents(smallHandler, this);
	}
}
