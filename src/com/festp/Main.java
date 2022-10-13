package com.festp;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.handlers.ChatHandler;
import com.festp.handlers.WhisperHandler;

public class Main extends JavaPlugin
{
	public void onEnable()
	{
		Logger.setLogger(getLogger());
    	PluginManager pm = getServer().getPluginManager();
    	
    	ChatHandler chatHandler = new ChatHandler(this);
    	pm.registerEvents(chatHandler, this);
    	
    	WhisperHandler whisperHandler = new WhisperHandler(this, true);
    	pm.registerEvents(whisperHandler, this);
	}
}
