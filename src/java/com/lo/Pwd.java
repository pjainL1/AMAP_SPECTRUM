package com.lo;

import java.util.PropertyResourceBundle;

import javax.naming.Context;

public class Pwd extends AbstractConfig {

	private static Pwd instance = new Pwd("com/lo/pwd");
	private PropertyResourceBundle prb;

	private Pwd(String path) {
		super(path);
	}

	public static Pwd getInstance() {
		return instance;
	}

	@Override
	protected void init(PropertyResourceBundle prb, Context context) {
		this.prb = prb;
	}

	public String getValue(String key) {
		return super.getValue(key, prb);
	}

}