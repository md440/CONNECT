/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services. 
 * All rights reserved. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met: 
 *     * Redistributions of source code must retain the above 
 *       copyright notice, this list of conditions and the following disclaimer. 
 *     * Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the documentation 
 *       and/or other materials provided with the distribution. 
 *     * Neither the name of the United States Government nor the 
 *       names of its contributors may be used to endorse or promote products 
 *       derived from this software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY 
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package gov.hhs.fha.nhinc.connectmgr;

import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants.ADAPTER_API_LEVEL;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.uddi.api_v3.BindingTemplate;
import org.uddi.api_v3.BusinessEntity;
import org.uddi.api_v3.BusinessService;
import org.uddi.api_v3.KeyedReference;

public class AdapterEndpointManager {
	public static final String ADAPTER_API_LEVEL_KEY = "CONNECT:adapter:apilevel";
	
    public ADAPTER_API_LEVEL getApiVersion(String serviceName) {
    	ADAPTER_API_LEVEL result = null;
        try {
            Set<ADAPTER_API_LEVEL> apiLevels = getAdapterAPILevelsByServiceName(serviceName);
            result = getHighestGatewayApiLevel(apiLevels);
        } catch (Exception ex) {
            Logger.getLogger(ConnectionManagerCache.class.getName()).log(Level.SEVERE, null, ex);
        }

        return (result == null) ? ADAPTER_API_LEVEL.LEVEL_a1 : result;
    }
    
    private ADAPTER_API_LEVEL getHighestGatewayApiLevel(Set<ADAPTER_API_LEVEL> apiLevels) {
    	ADAPTER_API_LEVEL highestApiLevel = null;

        try {
            for (ADAPTER_API_LEVEL apiLevel : apiLevels) {
                if (highestApiLevel == null || apiLevel.ordinal() > highestApiLevel.ordinal()) {
                    highestApiLevel = apiLevel;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ConnectionManagerCache.class.getName()).log(Level.SEVERE, null, ex);
        }

        return highestApiLevel;
    }
    
	public Set<ADAPTER_API_LEVEL> getAdapterAPILevelsByServiceName(
			String serviceName) {
		Set<ADAPTER_API_LEVEL> apiLevels = null;
		ConnectionManagerCacheHelper helper = new ConnectionManagerCacheHelper();

        try {
        	String sHomeCommunityId = PropertyAccessor.getInstance().getProperty(NhincConstants.GATEWAY_PROPERTY_FILE,
                    NhincConstants.HOME_COMMUNITY_ID_PROPERTY);
            BusinessEntity businessEntity = ConnectionManagerCache.getInstance().getBusinessEntity(sHomeCommunityId);
            BusinessService businessService = helper.getBusinessServiceByServiceName(businessEntity, serviceName);
            apiLevels = getAPILevelsFromBusinessService(businessService);

        } catch (Exception ex) {
            Logger.getLogger(ConnectionManagerCache.class.getName()).log(Level.SEVERE, null, ex);
        }

        return apiLevels;
	}

	
	private Set<ADAPTER_API_LEVEL> getAPILevelsFromBusinessService(
			BusinessService businessService) {
		Set<ADAPTER_API_LEVEL> apiLevels = new HashSet<ADAPTER_API_LEVEL>();
		
		if (businessService.getBindingTemplates() != null)
		{
			for (BindingTemplate bindingTemplate : businessService.getBindingTemplates().getBindingTemplate()) {
				if (bindingTemplate.getCategoryBag() != null && bindingTemplate.getCategoryBag().getKeyedReference() != null) {
		            for (KeyedReference reference : bindingTemplate.getCategoryBag().getKeyedReference()) {
		                String keyName = reference.getTModelKey();
		                String specVersionValue = reference.getKeyValue();
		                if (keyName.equals(ADAPTER_API_LEVEL_KEY)) {
		                	apiLevels.add(ADAPTER_API_LEVEL.valueOf(specVersionValue));
		                }
		            }
		        }
			}
		}
		return apiLevels;
	}
}
