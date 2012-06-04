package com.percussion.forum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.GaData.ProfileInfo;
import com.google.api.services.analytics.model.GaData.Query;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

public class ViewTopContent implements Controller {
	private IPSGuid selectedId = null;
	private IPSContentWs IPSContentWs = null;
	private IPSGuidManager IPSGuidManager = null;
	private Map<String, String> properties = null;

	@Override
	public ModelAndView handleRequest(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
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
		getConfiguration();

		Analytics analytics = Analytics
				.builder(new NetHttpTransport(), new JacksonFactory())
				.setApplicationName("Percussion-CM")
				.setHttpRequestInitializer(
						new GoogleCredential.Builder()
								.setTransport(new NetHttpTransport())
								.setJsonFactory(new JacksonFactory())
								.setClientSecrets(
										properties.get("client_id").toString(),
										properties.get("client_secret"))
								.build()
								.setFromTokenResponse(
										new TokenResponse()
												.setAccessToken(
														properties.get(
																"access_token")
																.toString())
												.setRefreshToken(
														properties
																.get("refresh_token")
																.toString())))
				.build();

		GaData gaData = analytics.data().ga()
				.get("ga:9852497", "2012-01-01", "2012-01-02", "ga:visits")
				.setDimensions("ga:source").setMaxResults(25).execute();

		System.out.println();
		System.out.println("Response:");
		System.out.println("ID:" + gaData.getId());
		System.out.println("Self link: " + gaData.getSelfLink());
		System.out.println("Kind: " + gaData.getKind());
		System.out.println("Contains Sampled Data: "
				+ gaData.getContainsSampledData());

		ProfileInfo profileInfo = gaData.getProfileInfo();

		System.out.println("Profile Info");
		System.out.println("Account ID: " + profileInfo.getAccountId());
		System.out
				.println("Web Property ID: " + profileInfo.getWebPropertyId());
		System.out.println("Internal Web Property ID: "
				+ profileInfo.getInternalWebPropertyId());
		System.out.println("Profile ID: " + profileInfo.getProfileId());
		System.out.println("Profile Name: " + profileInfo.getProfileName());
		System.out.println("Table ID: " + profileInfo.getTableId());

		Query query = gaData.getQuery();

		System.out.println("Query Info:");
		System.out.println("Ids: " + query.getIds());
		System.out.println("Start Date: " + query.getStartDate());
		System.out.println("End Date: " + query.getEndDate());
		System.out.println("Metrics: " + query.getMetrics()); // List
		System.out.println("Dimensions: " + query.getDimensions()); // List
		System.out.println("Sort: " + query.getSort()); // List
		System.out.println("Segment: " + query.getSegment());
		System.out.println("Filters: " + query.getFilters());
		System.out.println("Start Index: " + query.getStartIndex());
		System.out.println("Max Results: " + query.getMaxResults());

		System.out.println("Metric totals over all results:");
		Map<String, String> totalsMap = gaData.getTotalsForAllResults();
		for (Map.Entry<String, String> entry : totalsMap.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}

		return null;
	}

	public void setSelectedId(IPSGuid id) {
		if (id != null)
			selectedId = id;
	}

	public IPSGuid getSelectedId() {
		return selectedId;
	}

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
