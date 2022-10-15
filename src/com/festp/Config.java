package com.festp;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.utils.RawJsonBuilderSettings;

public class Config
{
	private JavaPlugin plugin;
	private MemoryConfiguration c;

	private static final String KEY_IS_LINK_UNDERLINED = "is-link-underlined";
	private boolean isLinkUnderlined = true;
	public boolean getIsLinkUnderlined() {
		return isLinkUnderlined;
	}
	public void setIsLinkUnderlined(boolean val) {
		isLinkUnderlined = val;
		saveConfig();
	}

	private static final String KEY_IS_VANILLA_WHISPER = "is-vanilla-whisper";
	private boolean isVanillaWhisper = true;
	public boolean getIsVanillaWhisper() {
		return isVanillaWhisper;
	}
	public void setIsVanillaWhisper(boolean val) {
		isVanillaWhisper = val;
		saveConfig();
	}
	
	public Config(JavaPlugin jp) {
		this.plugin = jp;
		this.c = jp.getConfig();
	}
	
	public RawJsonBuilderSettings getBuilderSettings() {
		return new RawJsonBuilderSettings(isLinkUnderlined);
	}
	
	public void loadConfig()
	{
		c.addDefault(KEY_IS_LINK_UNDERLINED, true);
		c.addDefault(KEY_IS_VANILLA_WHISPER, true);
		
		c.options().copyDefaults(true);
		plugin.saveConfig();

		isLinkUnderlined = plugin.getConfig().getBoolean(KEY_IS_LINK_UNDERLINED);
		isVanillaWhisper = plugin.getConfig().getBoolean(KEY_IS_VANILLA_WHISPER);

		Logger.info("Config reloaded.");
	}
	
	public void saveConfig()
	{
		c.set(KEY_IS_LINK_UNDERLINED, isLinkUnderlined);
		c.set(KEY_IS_VANILLA_WHISPER, isVanillaWhisper);

		plugin.saveConfig();

		Logger.info("Config successfully saved.");
	}
}
