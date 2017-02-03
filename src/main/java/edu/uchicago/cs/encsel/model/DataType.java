/*******************************************************************************
 * Copyright (c) 2016 Hao Jiang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hao Jiang - initial API and implementation and/or initial documentation
 *******************************************************************************/

package edu.uchicago.cs.encsel.model;

import org.apache.commons.lang.StringUtils;

/**
 * @author Cathy
 *
 */
public enum DataType {
	INTEGER, LONG, FLOAT, DOUBLE, STRING, BOOLEAN;

	public boolean check(String input) {
		input = input.trim();
		// Do not check empty
		if (StringUtils.isEmpty(input))
			return true;
		try {
			switch (this) {
			case INTEGER:
				Integer.parseInt(input);
				break;
			case LONG:
				Long.parseLong(input);
				break;
			case FLOAT:
				Float.parseFloat(input);
				break;
			case DOUBLE:
				Double.parseDouble(input);
				break;
			case STRING:
				break;
			case BOOLEAN:
				Boolean.parseBoolean(input);
				break;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
