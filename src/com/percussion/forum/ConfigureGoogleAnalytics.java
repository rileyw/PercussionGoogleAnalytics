package com.percussion.forum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

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
	private IPSContentWs IPSContentWs;
	private IPSGuidManager IPSGuidManager;
	private Map<String,String> properties = new HashMap<String,String>();
	

	@Override
	public ModelAndView handleRequest(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
		/**
		 * Content id required to perform any action
		 */
		if(arg0.getParameter("sys_contentid")!=null && !"".equals(arg0.getParameter("sys_contentid").toString())){
			IPSContentWs = PSContentWsLocator.getContentWebservice();
			IPSGuidManager = PSGuidManagerLocator.getGuidMgr();
			String content_id = arg0.getParameter("sys_contentid").toString();
			try {
				setSelectedId(IPSGuidManager.makeGuid(new PSLocator(content_id)));
			} catch (Exception e){
				System.err.println("Content_id cannot be //Sites/");
				e.printStackTrace();
			}
			
			if(arg0.getParameter("client_id")!=null && arg0.getParameter("client_secret")!=null){
				try {
					setConfiguration(arg0.getParameter("client_id").toString().trim(),arg0.getParameter("client_secret").toString().trim());
				} catch (Exception e){
					e.printStackTrace();
				}
				// TODO: Pass off to Google for authorization code
				return null;
			} else {
				getConfiguration();
				if(properties.get("client_id")!=null && properties.get("client_secret")!=null){
					ModelAndView model = new ModelAndView("updateGoogleAnalytics");
					model.addObject("content_id",content_id);
					if(properties.get("client_id") != null)
						model.addObject("client_id",properties.get("client_id").toString());
					if(properties.get("client_secret") != null)
						model.addObject("client_secret",properties.get("client_secret").toString());
					if(properties.get("ga_profile") != null)
						model.addObject("ga_profile",properties.get("ga_profile").toString());
					return model;
				} else {
					ModelAndView model = new ModelAndView("newGoogleAnalytics");
					return model;
				}
			}		
		} else {
			return null;
		}		
	}
	
	public void setSelectedId(IPSGuid id) {
		if(id != null)
			this.selectedId = id;
	}
	
	public IPSGuid getSelectedId(){
		return this.selectedId;
	}
	
	public void setConfiguration(String client_id, String client_secret) throws Exception {
		List<IPSGuid> folderIds = new ArrayList<IPSGuid>();
		folderIds.add(getSelectedId());
		List<PSFolder> folders;
		try {
			folders = IPSContentWs.loadFolders(folderIds);
			PSFolder main = folders.get(0);
			main.setProperty("client_id", client_id);
			main.setProperty("client_secret",client_secret);
		} catch (PSErrorResultsException e) {
			e.printStackTrace();
		}	
	}

	/**
	 * Method: isConfigured
	 * 
	 * @return boolean
	 */
	public void getConfiguration(){
		List<IPSGuid> folderIds = new ArrayList<IPSGuid>();
		folderIds.add(getSelectedId());
		List<PSFolder> folders;
		try {
			folders = IPSContentWs.loadFolders(folderIds);
			PSFolder main = folders.get(0);
			if(main.getPropertyValue("client_id")!=null)
				properties.put("client_id",main.getPropertyValue("client_id").toString().trim());
			if(main.getPropertyValue("client_secret")!=null)
				properties.put("client_secret",main.getPropertyValue("client_secret").toString().trim());
			if(main.getPropertyValue("access_token")!=null)
				properties.put("access_token", main.getPropertyValue("access_token"));
		} catch (PSErrorResultsException e) {
			e.printStackTrace();
		}
	}
}
