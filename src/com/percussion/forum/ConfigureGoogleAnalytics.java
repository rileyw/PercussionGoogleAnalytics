package com.percussion.forum;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

public class ConfigureGoogleAnalytics implements Controller {

	private IPSGuid selectedId = null;
	private IPSContentWs IPSContentWs = null;
	private IPSGuidManager IPSGuidManager = null;
	private Map<String, String> properties = null;

	@Override
	public ModelAndView handleRequest(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
		/**
		 * Content id required to perform any action
		 */
		if (arg0.getParameter("sys_contentid") != null
				&& !"".equals(arg0.getParameter("sys_contentid").toString())) {
			IPSContentWs = PSContentWsLocator.getContentWebservice();
			IPSGuidManager = PSGuidManagerLocator.getGuidMgr();
			String content_id = arg0.getParameter("sys_contentid").toString();
			try {
				setSelectedId(IPSGuidManager
						.makeGuid(new PSLocator(content_id)));
			} catch (Exception e) {
				System.err.println("Content_id cannot be //Sites/");
				e.printStackTrace();
			}

			if (arg0.getParameter("client_id") != null
					&& arg0.getParameter("client_secret") != null) {
				try {
					setConfiguration(arg0.getParameter("client_id").toString()
							.trim(), arg0.getParameter("client_secret")
							.toString().trim());
					ModelAndView model = new ModelAndView(
							"getAuthorizationCode");
					model.addObject("client_id", arg0.getParameter("client_id")
							.toString().trim());
					model.addObject("sys_contentid",
							arg0.getParameter("sys_contentid").toString()
									.trim());
					model.addObject("redirect_uri", URLEncoder.encode(arg0
							.getRequestURL().toString(), "UTF-8"));
					arg1.addCookie(new Cookie("_contentid", arg0
							.getParameter("sys_contentid").toString().trim()));
					return model;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			} else {
				getConfiguration();
				if (properties.get("client_id") != null
						&& properties.get("client_secret") != null) {
					ModelAndView model = new ModelAndView(
							"updateGoogleAnalytics");
					model.addObject("content_id", content_id);
					if (properties.get("client_id") != null)
						model.addObject("client_id", properties
								.get("client_id").toString());
					if (properties.get("client_secret") != null)
						model.addObject("client_secret",
								properties.get("client_secret").toString());
					if (properties.get("ga_profile") != null)
						model.addObject("ga_profile",
								properties.get("ga_profile").toString());
					return model;
				} else {
					ModelAndView model = new ModelAndView("newGoogleAnalytics");
					return model;
				}
			}
		} else if (arg0.getParameter("code") != null
				&& !"".equals(arg0.getParameter("code").toString())) {
			Cookie[] cookies = arg0.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("_contentid".equals(cookie.getName())) {
						String content_id = cookie.getValue().toString();
						try {
							setSelectedId(IPSGuidManager
									.makeGuid(new PSLocator(content_id)));
						} catch (Exception e) {
							System.err
									.println("Content ID from Cookie could not be loaded");
							e.printStackTrace();
						}
					}
				}
				getConfiguration();
				if (properties.get("client_id") != null
						&& properties.get("client_secret") != null) {
					try {
						GoogleTokenResponse googleResponse = new GoogleAuthorizationCodeTokenRequest(
								new NetHttpTransport(), new JacksonFactory(),
								properties.get("client_id").toString(),
								properties.get("client_secret").toString(),
								arg0.getParameter("code").toString(),
								arg0.getRequestURL().toString()).execute();
						setTokenKeys(googleResponse.getAccessToken().toString(),googleResponse.getRefreshToken().toString());
					} catch (TokenResponseException e) {
						if (e.getDetails() != null) {
							System.err.println("Error: "
									+ e.getDetails().getError());
							if (e.getDetails().getErrorDescription() != null) {
								System.err.println(e.getDetails()
										.getErrorDescription());
							}
							if (e.getDetails().getErrorUri() != null) {
								System.err
										.println(e.getDetails().getErrorUri());
							}
						} else {
							System.err.println(e.getMessage());
						}
					}
				}
				return null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public void setSelectedId(IPSGuid id) {
		if (id != null)
			selectedId = id;
	}

	public IPSGuid getSelectedId() {
		return selectedId;
	}
	
	public void setTokenKeys(String access_token, String refresh_token)
		throws Exception {
		List<IPSGuid> folderIds = new ArrayList<IPSGuid>();
		folderIds.add(getSelectedId());
		List<PSFolder> folders;
		try {
			folders = IPSContentWs.loadFolders(folderIds);
			PSFolder main = folders.get(0);
			main.setProperty("access_token", access_token);
			main.setProperty("refresh_token", refresh_token);
		} catch (PSErrorResultsException e) {
			e.printStackTrace();
		}
	}

	public void setConfiguration(String client_id, String client_secret)
			throws Exception {
		List<IPSGuid> folderIds = new ArrayList<IPSGuid>();
		folderIds.add(getSelectedId());
		List<PSFolder> folders;
		try {
			folders = IPSContentWs.loadFolders(folderIds);
			PSFolder main = folders.get(0);
			main.setProperty("client_id", client_id);
			main.setProperty("client_secret", client_secret);
		} catch (PSErrorResultsException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method: isConfigured
	 * 
	 * @return boolean
	 */
	public void getConfiguration() {
		List<IPSGuid> folderIds = new ArrayList<IPSGuid>();
		folderIds.add(getSelectedId());
		List<PSFolder> folders;
		try {
			properties = new HashMap<String, String>();
			folders = IPSContentWs.loadFolders(folderIds);
			PSFolder main = folders.get(0);
			if (main.getPropertyValue("client_id") != null)
				properties.put("client_id", main.getPropertyValue("client_id")
						.toString().trim());
			if (main.getPropertyValue("client_secret") != null)
				properties.put("client_secret",
						main.getPropertyValue("client_secret").toString()
								.trim());
			if (main.getPropertyValue("access_token") != null)
				properties.put("access_token",
						main.getPropertyValue("access_token"));
			if (main.getPropertyValue("refresh_token") != null)
				properties.put("refresh_token",
						main.getPropertyValue("refresh_token"));
		} catch (PSErrorResultsException e) {
			e.printStackTrace();
		}
	}
}
