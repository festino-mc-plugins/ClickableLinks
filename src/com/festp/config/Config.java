package com.festp.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.Logger;
import com.festp.utils.RawJsonBuilderSettings;

public class Config implements IConfig
{
	private JavaPlugin plugin;
	private MemoryConfiguration config;
	private final HashMap<String, Object> map = new HashMap<>();
	
	public Config(JavaPlugin jp) {
		this.plugin = jp;
		this.config = jp.getConfig();
	}
	
	public RawJsonBuilderSettings getBuilderSettings() {
		return new RawJsonBuilderSettings(get(Key.IS_LINK_UNDERLINED));
	}
	
	public void load() {
		map.putAll(config.getValues(true));
		Logger.info("Config reloaded.");
	}

	public void save() {
		for (Key key : Key.values()) {
			config.set(key.name, get(key));
		}
		plugin.saveConfig();
		Logger.info("Config successfully saved.");
	}
	
	public void set(Key key, Object value) {
		map.put(key.toString(), value);
		save();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <T extends Object> T get(Key key, T defaultValue) {
		applyDefault(key, defaultValue);
		
		Class<?> clazz;
		if (defaultValue != null) {
			clazz = defaultValue.getClass();
		} else {
			clazz = key.getDefault().getClass();
		}
		
		Object res = map.getOrDefault(key.toString(), defaultValue);
		if (clazz.isInstance(res)) {
			return (T) res;
		}
		return defaultValue;
	}
	public <T extends Object> T get(Key key) {
		return get(key, null);
	}
	
	

	@Override
	public Object getObject(IConfig.Key key, Object defaultValue) {
		return map.getOrDefault(key.toString(), defaultValue);
	}

	@Override
	public Object getObject(IConfig.Key key) {
		return getObject(key, key.getDefault());
	}
	
	
	
	private void applyDefault(String key, Object defaultValue) {
		if (!map.containsKey(key)) {
			map.put(key, defaultValue);
		}
	}
	private void applyDefault(Key key, Object defaultValue) {
		if (defaultValue == null)
			applyDefault(key);
		else
			applyDefault(key.toString(), defaultValue);
	}
	private void applyDefault(Key key) {
		applyDefault(key.toString(), key.getDefault());
	}
	
	
	
	public enum Key implements IConfig.Key {
		IS_LINK_UNDERLINED("underline-links", true),
		IS_WHISPER("do-whisper", true),
		IS_WHISPER_NEW_MESSAGE("whisper-new-message", false);
		
		private final String name;
		private final Object defaultValue;

		Key(String name, Object defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;
		}
		public Object getDefault() { return defaultValue; }
		@Override
		public String toString() { return name; }
		
		public Object validateValue(String valueStr) {
			try {
				if (defaultValue instanceof Boolean) {
					return Boolean.parseBoolean(valueStr);
				}
				if (defaultValue instanceof Integer) {
					return Integer.parseInt(valueStr);
				}
				if (defaultValue instanceof Double) {
					return Double.parseDouble(valueStr);
				}
				if (defaultValue instanceof String) {
					return valueStr;
				}
			} catch (Exception e) {}
			return null;
		}
		
		public Class<?> getValueClass() {
			if (defaultValue instanceof Boolean) {
				return Boolean.class;
			}
			if (defaultValue instanceof Integer) {
				return Integer.class;
			}
			if (defaultValue instanceof Double) {
				return Double.class;
			}
			if (defaultValue instanceof String) {
				return String.class;
			}
			return null;
		}
		
		public static boolean isValidKey(String keyStr) {
			return getKey(keyStr) != null;
		}
		
		public static Key getKey(String keyStr) {
			for (Key key : Key.values())
				if (key.name.equalsIgnoreCase(keyStr))
					return key;
			return null;
		}
		
		public static List<String> getKeys() {
			List<String> keys = new ArrayList<>();
			for (Key key : Key.values()) {
				keys.add(key.name);
			}
			return keys;
		}
	}
}
