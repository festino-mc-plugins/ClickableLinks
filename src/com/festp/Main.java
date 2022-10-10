package com.festp;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.handlers.ChatHandler;

public class Main extends JavaPlugin
{
	public void onEnable()
	{
		Logger.setLogger(getLogger());
    	PluginManager pm = getServer().getPluginManager();
    	
    	ChatHandler mainHandler = new ChatHandler(this);
    	pm.registerEvents(mainHandler, this);
	}
}
