package com.festp.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.festp.config.Config;
import com.festp.config.LangConfig;

public class LinksCommand implements CommandExecutor, TabCompleter
{
	private static final String PERMISSION_CONFIGURE = "clickablelinks.configure";
	public static final String COMMAND = "links";
	private static final String SUBCOMMAND_RELOAD = "reload";
	
	Config config;
	LangConfig lang;
	
	public LinksCommand(Config config, LangConfig lang)
	{
		this.config = config;
		this.lang = lang;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if (!sender.hasPermission(PERMISSION_CONFIGURE)) {
			sender.sendMessage(String.format(lang.command_noPerm, PERMISSION_CONFIGURE));
			return false;
		}
		
		if (args.length == 0) {
			sender.sendMessage(lang.command_noArgs);
			return false;
		}
		Config.Key key = null;
		for (Config.Key k : Config.Key.values())
			if (k.toString().equalsIgnoreCase(args[0])) {
				key = k;
				break;
			}
		if (key != null)
		{
			if (args.length == 1) {
				sender.sendMessage(String.format(lang.command_setOk, key.toString(), config.get(key)));
				return true;
			}
			Boolean val = tryParseBoolean(args[1]);
			if (val == null) {
				sender.sendMessage(String.format(lang.command_arg1_error, args[1]));
				return false;
			}
			
			config.set(key, val);
			sender.sendMessage(String.format(lang.command_setOk, key.toString(), val));
			
			return true;
		}
		else if (args[0].equalsIgnoreCase(SUBCOMMAND_RELOAD))
		{
			config.load();
			lang.load();
			sender.sendMessage(lang.command_reloadOk);
			return true;
		}
		
		sender.sendMessage(String.format(lang.command_arg0_error, args[0]));
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		List<String> options = new ArrayList<>();

		if (!sender.hasPermission(PERMISSION_CONFIGURE)) {
			return options;
		}
		
		if (args.length == 1)
		{
			String arg = args[0].toLowerCase();
			if (SUBCOMMAND_RELOAD.startsWith(arg))
				options.add(SUBCOMMAND_RELOAD);
			for (Config.Key k : Config.Key.values())
				if (k.toString().startsWith(arg))
					options.add(k.toString());
		}
		else if (args.length == 2)
		{
			String arg = args[0].toLowerCase();
			boolean hasKey = false;
			for (Config.Key k : Config.Key.values())
				if (k.toString().equals(arg)) {
					hasKey = true;
					break;
				}
			if (hasKey)
			{
				options.add("true");
				options.add("false");
			}
		}
		return options;
	}


	private Boolean tryParseBoolean(String str) {
		if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("1"))
			return true;
		if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("0"))
			return false;
		return null;
	}
}
