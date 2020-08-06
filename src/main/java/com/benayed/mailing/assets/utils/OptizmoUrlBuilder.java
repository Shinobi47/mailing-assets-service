package com.benayed.mailing.assets.utils;

public class OptizmoUrlBuilder {

	// example :	"https://mailer-api.optizmo.net/accesskey/download/" + campaignAccessKey + "?token=" + optizmoApiToken +"&format=md5";
	
	
	private final String OptizmoBaseUrl = "https://mailer-api.optizmo.net/accesskey/download/";
	private String campaignAccessKey;
	private final String tokenRequestParamName = "?token=";
	private String optizmoApiToken;
	private final String formatRequestParam = "&format=md5";
	
	public OptizmoUrlBuilder campaignAccessKey(String campaignAccessKey) {
		this.campaignAccessKey = campaignAccessKey;
		return this;
	}
	
	public OptizmoUrlBuilder optizmoApiToken(String optizmoApiToken) {
		this.optizmoApiToken = optizmoApiToken;
		return this;
	}
	
	public String build() {
		return new StringBuilder()
				.append(OptizmoBaseUrl)
				.append(campaignAccessKey)
				.append(tokenRequestParamName)
				.append(optizmoApiToken)
				.append(formatRequestParam)
				.toString();
	}

}
