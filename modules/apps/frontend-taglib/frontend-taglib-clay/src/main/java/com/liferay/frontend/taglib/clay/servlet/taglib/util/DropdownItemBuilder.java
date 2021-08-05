/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.frontend.taglib.clay.servlet.taglib.util;

import java.util.Map;

import javax.portlet.PortletURL;

/**
 * @author Hugo Huijser
 */
public class DropdownItemBuilder {

	public static DropdownItemWrapper putData(String key, String value) {
		DropdownItemWrapper dropdownItemWrapper = new DropdownItemWrapper();

		return dropdownItemWrapper.putData(key, value);
	}

	public static DropdownItemWrapper setActive(boolean active) {
		DropdownItemWrapper dropdownItemWrapper = new DropdownItemWrapper();

		return dropdownItemWrapper.setActive(active);
	}

	public static DropdownItemWrapper setData(Map<String, Object> data) {
		DropdownItemWrapper dropdownItemWrapper = new DropdownItemWrapper();

		return dropdownItemWrapper.setData(data);
	}

	public static DropdownItemWrapper setDisabled(boolean disabled) {
		DropdownItemWrapper dropdownItemWrapper = new DropdownItemWrapper();

		return dropdownItemWrapper.setDisabled(disabled);
	}

	public static DropdownItemWrapper setHref(Object href) {
		DropdownItemWrapper dropdownItemWrapper = new DropdownItemWrapper();

		return dropdownItemWrapper.setHref(href);
	}

	public static DropdownItemWrapper setHref(
		PortletURL portletURL, Object... parameters) {

		DropdownItemWrapper dropdownItemWrapper = new DropdownItemWrapper();

		return dropdownItemWrapper.setHref(portletURL, parameters);
	}

	public static DropdownItemWrapper setIcon(String icon) {
		DropdownItemWrapper dropdownItemWrapper = new DropdownItemWrapper();

		return dropdownItemWrapper.setIcon(icon);
	}

	public static DropdownItemWrapper setLabel(String label) {
		DropdownItemWrapper dropdownItemWrapper = new DropdownItemWrapper();

		return dropdownItemWrapper.setLabel(label);
	}

	public static DropdownItemWrapper setQuickAction(boolean quickAction) {
		DropdownItemWrapper dropdownItemWrapper = new DropdownItemWrapper();

		return dropdownItemWrapper.setQuickAction(quickAction);
	}

	public static DropdownItemWrapper setSeparator(boolean separator) {
		DropdownItemWrapper dropdownItemWrapper = new DropdownItemWrapper();

		return dropdownItemWrapper.setSeparator(separator);
	}

	public static DropdownItemWrapper setTarget(String target) {
		DropdownItemWrapper dropdownItemWrapper = new DropdownItemWrapper();

		return dropdownItemWrapper.setTarget(target);
	}

	public static DropdownItemWrapper setType(String type) {
		DropdownItemWrapper dropdownItemWrapper = new DropdownItemWrapper();

		return dropdownItemWrapper.setType(type);
	}

	public static final class DropdownItemWrapper {

		public DropdownItem build() {
			return _dropdownItem;
		}

		public DropdownItemWrapper putData(String key, String value) {
			_dropdownItem.putData(key, value);

			return this;
		}

		public DropdownItemWrapper setActive(boolean active) {
			_dropdownItem.setActive(active);

			return this;
		}

		public DropdownItemWrapper setData(Map<String, Object> data) {
			_dropdownItem.setData(data);

			return this;
		}

		public DropdownItemWrapper setDisabled(boolean disabled) {
			_dropdownItem.setDisabled(disabled);

			return this;
		}

		public DropdownItemWrapper setHref(Object href) {
			_dropdownItem.setHref(href);

			return this;
		}

		public DropdownItemWrapper setHref(
			PortletURL portletURL, Object... parameters) {

			_dropdownItem.setHref(portletURL, parameters);

			return this;
		}

		public DropdownItemWrapper setIcon(String icon) {
			_dropdownItem.setIcon(icon);

			return this;
		}

		public DropdownItemWrapper setLabel(String label) {
			_dropdownItem.setLabel(label);

			return this;
		}

		public DropdownItemWrapper setQuickAction(boolean quickAction) {
			_dropdownItem.setQuickAction(quickAction);

			return this;
		}

		public DropdownItemWrapper setSeparator(boolean separator) {
			_dropdownItem.setSeparator(separator);

			return this;
		}

		public DropdownItemWrapper setTarget(String target) {
			_dropdownItem.setTarget(target);

			return this;
		}

		public DropdownItemWrapper setType(String type) {
			_dropdownItem.setType(type);

			return this;
		}

		private final DropdownItem _dropdownItem = new DropdownItem();

	}

}