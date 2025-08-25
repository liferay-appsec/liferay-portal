package com.liferay.object.exception;

import com.liferay.portal.kernel.exception.PortalException;

public class WrapperException extends PortalException {

	public WrapperException() {
	}

	public WrapperException(String msg) {
		super(msg);
	}

	public WrapperException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public WrapperException(Throwable throwable) {
		super(throwable);
	}

	public String getDetails() {
		return this._details;
	}

	public void setDetails(String details) {
		this._details = details;
	}

	private String _details;

}