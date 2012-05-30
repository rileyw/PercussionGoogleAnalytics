package com.percussion.forum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderProperty;
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
		 * Content Web Services obtained
		 */
		IPSContentWs = PSContentWsLocator.getContentWebservice();
		
		/**
		 * Generated unique ID manager obtained
		 */
		IPSGuidManager = PSGuidManagerLocator.getGuidMgr();
		
		/**
		 * Selected content ID
		 */
		String id = arg0.getParameter("sys_contentid").toString();
		if(!("").equals(id)){
			setSelectedId(IPSGuidManager.makeGuid(new PSLocator(id)));

			if(isConfigured()){
				
			} else {
				
			}
		} else {
			
		}		
		return null;
	}
	
	public void setSelectedId(IPSGuid id) {
		if(id != null)
			this.selectedId = id;
	}
	
	public IPSGuid getSelectedId(){
		return this.selectedId;
	}

	
	public boolean isConfigured(){
		/**
		 * Searching folders for Google Analytics properties
		 */
		List<IPSGuid> folderIds = new ArrayList<IPSGuid>();
		folderIds.add(getSelectedId());
		List<PSFolder> folders;
		try {
			folders = IPSContentWs.loadFolders(folderIds);
			PSFolder main = folders.get(0);		
			Iterator<?> i = main.getProperties();
			while(i.hasNext()){
				PSFolderProperty PSFolderProperty = (PSFolderProperty)i.next();
				properties.put(PSFolderProperty.getName().toString().toLowerCase(), PSFolderProperty.getValue().toString().toLowerCase());
			}
			System.out.println(properties.get("client_id"));
		} catch (PSErrorResultsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
}
