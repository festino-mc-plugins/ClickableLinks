package com.festp.config;

public interface IConfig {
	interface Key {
		Object getDefault();
	}
	public Object getObject(Key key);
	public Object getObject(Key key, Object default_value);
	
}
