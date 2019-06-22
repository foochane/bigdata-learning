package com.foochane.datacollect;

import java.util.Properties;


/**
 * 单例设计模式，方式一�? 饿汉式单�?
 *
 */
public class PropertyHolderHungery {

	private static Properties prop = new Properties();

	static {
		try {
			prop.load(PropertyHolderHungery.class.getClassLoader().getResourceAsStream("collect.properties"));
		} catch (Exception e) {

		}
	}

	public static Properties getProps() throws Exception {

		return prop;
	}

}

