/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

public class OpenMarkovException extends Exception {
	/**
	 * This token must correspond which one of the exception constants defined in <code>{@link OpenMarkovException}</code>
	 */
	private String token;

	private String message;

	/**
	 * List of attributes that should pass to the GUI in order to display extra information (f.e. a network name, a number,...).
	 */
	private String[] attributes;


	public OpenMarkovException() {
		super();
	}

	public OpenMarkovException(String token) {
		super();
		this.token = token;
	}

	public OpenMarkovException(Exception exception){
		this.token = ((OpenMarkovException) exception).getToken();
		this.message = exception.getMessage();
	}


	public OpenMarkovException(String token, String... attributes) {
		this.token = token;
		this.attributes = attributes;
	}

	public OpenMarkovException(String message, Throwable cause) {
		super(message, cause);
		this.token = message;
	}

	public OpenMarkovException(String message, Throwable cause, String... attributes) {
		super(message, cause);
		this.token = message;
		this.attributes = attributes;
	}


	public String getToken() {
		return this.token;
	}

	public String[] getAttributes () {
		return attributes;
	}
}