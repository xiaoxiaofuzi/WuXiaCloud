package com.wx.wxcommoncore.exception;


import com.wx.wxcommoncore.support.http.HttpCode;

@SuppressWarnings("serial")
public class LoginException extends BaseException {
	public LoginException() {
	}

	public LoginException(String message) {
		super(message);
	}

	public LoginException(String message, Exception e) {
		super(message, e);
	}

	@Override
	protected HttpCode getCode() {
		return HttpCode.LOGIN_FAIL;
	}
}
