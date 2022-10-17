package com.festp.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.festp.config.Config;

public class LinksCommand implements CommandExecutor, TabCompleter
{
	private static final String PERMISSION_CONFIGURE = "clickablelinks.configure";
	public static final String COMMAND = "links";

	String message_noPerm = ChatColor.RED + "You must be an operator or have " + ChatColor.GRAY + PERMISSION_CONFIGURE + ChatColor.RED + " permission to perform this command.";
	String message_noArgs = ChatColor.RED + "Usage: /links <option> [true|false]";
	String message_arg0 = ChatColor.RED + "\"%s\" is an invalid option. Please follow tab-completion.";
	String message_arg1 = ChatColor.RED + "\"%s\" is an invalid value. Please follow tab-completion.";
	String message_getOk = ChatColor.GREEN + "Option %s is equal to %s.";
	String message_setOk = ChatColor.GREEN + "Option %s was set to %s.";
	
	Config config;
	
	public LinksCommand(Config config)
	{
		this.config = config;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if (!sender.hasPermission(PERMISSION_CONFIGURE)) {
			sender.sendMessage(message_noPerm);
			return false;
		}
		
		if (args.length == 0) {
			sender.sendMessage(message_noArgs);
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
				sender.sendMessage(String.format(message_setOk, key.toString(), config.get(key)));
				return true;
			}
			Boolean val = tryParseBoolean(args[1]);
			if (val == null) {
				sender.sendMessage(String.format(message_arg1, args[1]));
				return false;
			}
			
			config.set(key, val);
			sender.sendMessage(String.format(message_setOk, key.toString(), val));
			
			return true;
		}
		
		sender.sendMessage(String.format(message_arg0, args[0]));
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		List<String> options = new ArrayList<>();

		if (!sender.hasPermission(PERMISSION_CONFIGURE)) {
			return options;
		}
		
		if (args.length <= 1)
		{
			for (Config.Key k : Config.Key.values())
				options.add(k.toString());
		}
		else if (args.length == 2)
		{
			boolean hasKey = false;
			for (Config.Key k : Config.Key.values())
				if (k.toString().equalsIgnoreCase(args[0])) {
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
