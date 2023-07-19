package com.liferay.portal.security.service.access.policy.configuration;

import aQute.bnd.annotation.metatype.Meta;

@Meta.OCD(
		description = "headless-discovery-description",
		id = "com.liferay.portal.security.service.access.policy.configuration.HeadlessDiscoveryConfiguration",
		localization = "content/Language",
		name = "headless-discovery-configuration-name"
	)
	public interface HeadlessDiscoveryConfiguration {

		@Meta.AD(deflt = "true", name = "enable-api-explorer", required = false)
		public boolean enableAPIExplorer();

	}
