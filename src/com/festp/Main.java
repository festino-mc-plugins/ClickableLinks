package com.festp;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.commands.LinksCommand;
import com.festp.config.Config;
import com.festp.handlers.ChatHandler;
import com.festp.handlers.SmallCommandsHandler;
import com.festp.handlers.WhisperHandler;

public class Main extends JavaPlugin
{
	public void onEnable()
	{
		Logger.setLogger(getLogger());
		Config config = new Config(this);
		config.load();

		Chatter chatter = new Chatter(this, config);

		PluginManager pm = getServer().getPluginManager();

		ChatHandler chatHandler = new ChatHandler(chatter);
		pm.registerEvents(chatHandler, this);

		SmallCommandsHandler smallHandler = new SmallCommandsHandler(chatter);
		pm.registerEvents(smallHandler, this);

		WhisperHandler whisperHandler = new WhisperHandler(chatter, config);
		pm.registerEvents(whisperHandler, this);

		LinksCommand commandWorker = new LinksCommand(config);
		getCommand(LinksCommand.COMMAND).setExecutor(commandWorker);
		getCommand(LinksCommand.COMMAND).setTabCompleter(commandWorker);
	}
}
