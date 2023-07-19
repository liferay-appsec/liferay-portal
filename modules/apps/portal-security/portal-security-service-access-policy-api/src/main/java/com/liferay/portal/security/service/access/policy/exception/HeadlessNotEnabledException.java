package com.liferay.portal.security.service.access.policy.exception;


public class HeadlessNotEnabledException extends SecurityException {

	public HeadlessNotEnabledException() {
	}

	public HeadlessNotEnabledException(String msg) {
		super(msg);
	}

	public HeadlessNotEnabledException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public HeadlessNotEnabledException(Throwable throwable) {
		super(throwable);
	}
}
