package com.foochane.datacollect;

import java.util.Properties;


/**
 * �������ģʽ����ʽһ�� ����ʽ����
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

