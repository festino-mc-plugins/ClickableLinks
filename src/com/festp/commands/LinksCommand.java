package com.festp.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.festp.Config;

public class LinksCommand implements CommandExecutor, TabCompleter
{
	private static final String OPTION_UNDERLINE = "underline";
	private static final String OPTION_WHISPER = "vanillaWhisper";
	public static final String COMMAND = "links";

	String message_noOp = ChatColor.RED + "You must be an operator to perform this command.";
	String message_noArgs = ChatColor.RED + "Usage: /links <" + OPTION_UNDERLINE + "|" + OPTION_WHISPER + "> [true|false]";
	String message_arg0 = ChatColor.RED + "\"%s\" is an invalid option. Please follow tab-completion.";
	String message_arg1 = ChatColor.RED + "\"%s\" is an invalid value. Please follow tab-completion.";
	String message_getOk = ChatColor.GREEN + "Option %s is equal to %s.";
	String message_setOk = ChatColor.GREEN + "Option %s was set to %s.";
	
	Config config;
	
	public LinksCommand(Config config)
	{
		this.config = config;
	}

	// dirty code
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if (!sender.isOp()) {
			sender.sendMessage(message_noOp);
			return false;
		}
		
		if (args.length == 0) {
			sender.sendMessage(message_noArgs);
			return false;
		}
		if (args[0].equalsIgnoreCase(OPTION_UNDERLINE))
		{
			if (args.length == 1) {
				sender.sendMessage(String.format(message_setOk, OPTION_UNDERLINE, config.getIsLinkUnderlined()));
				return true;
			}
			Boolean val = tryParseBoolean(args[1]);
			if (val == null) {
				sender.sendMessage(String.format(message_arg1, args[1]));
				return false;
			}
			
			config.setIsLinkUnderlined(val);
			sender.sendMessage(String.format(message_setOk, OPTION_UNDERLINE, val));
			
			return true;
		}
		if (args[0].equalsIgnoreCase(OPTION_WHISPER))
		{
			if (args.length == 1) {
				sender.sendMessage(String.format(message_setOk, OPTION_WHISPER, config.getIsVanillaWhisper()));
				return true;
			}
			Boolean val = tryParseBoolean(args[1]);
			if (val == null) {
				sender.sendMessage(String.format(message_arg1, args[1]));
				return false;
			}
			
			config.setIsVanillaWhisper(val);
			sender.sendMessage(String.format(message_setOk, OPTION_WHISPER, val));
			
			return true;
		}
		
		sender.sendMessage(String.format(message_arg0, args[0]));
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		List<String> options = new ArrayList<>();
		
		if (!sender.isOp()) {
			return options;
		}
		
		if (args.length <= 1)
		{
			options.add(OPTION_UNDERLINE);
			options.add(OPTION_WHISPER);
		}
		else if (args.length == 2)
		{
			if (args[0].equalsIgnoreCase(OPTION_UNDERLINE) || args[0].equalsIgnoreCase(OPTION_WHISPER))
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
