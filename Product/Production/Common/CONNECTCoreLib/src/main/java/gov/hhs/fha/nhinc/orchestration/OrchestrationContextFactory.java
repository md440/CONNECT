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
package gov.hhs.fha.nhinc.orchestration;

import gov.hhs.fha.nhinc.admindistribution.entity.OutboundAdminDistributionFactory;
import gov.hhs.fha.nhinc.docquery.entity.OutboundDocQueryFactory;
import gov.hhs.fha.nhinc.patientdiscovery.entity.OutboundPatientDiscoveryFactory;
import gov.hhs.fha.nhinc.patientdiscovery.entity.deferred.request.OutboundPatientDiscoveryDeferredRequestFactory;
import gov.hhs.fha.nhinc.patientdiscovery.entity.deferred.response.OutboundPatientDiscoveryDeferredResponseFactory;
import gov.hhs.fha.nhinc.docretrieve.entity.OutboundDocRetrieveFactory;
import gov.hhs.fha.nhinc.docretrieve.nhin.InboundDocRetrieveFactory;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.connectmgr.AdapterEndpointManager;
import gov.hhs.fha.nhinc.connectmgr.NhinEndpointManager;
import gov.hhs.fha.nhinc.docsubmission.entity.OutboundDocSubmissionFactory;
import gov.hhs.fha.nhinc.docsubmission.entity.deferred.request.OutboundDocSubmissionDeferredRequestFactory;
import gov.hhs.fha.nhinc.docsubmission.entity.deferred.response.OutboundDocSubmissionDeferredResponseFactory;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;

public class OrchestrationContextFactory {

    private static OrchestrationContextFactory INSTANCE = new OrchestrationContextFactory();

    private OrchestrationContextFactory() {
    }

    public static OrchestrationContextFactory getInstance() {
        return INSTANCE;
    }

    public OrchestrationContextBuilder getBuilder(HomeCommunityType homeCommunityType,
            NhincConstants.NHIN_SERVICE_NAMES serviceName) {
        NhinEndpointManager nem = new NhinEndpointManager();
        NhincConstants.GATEWAY_API_LEVEL apiLevel = nem.getApiVersion(homeCommunityType.getHomeCommunityId(),
                serviceName);
        return getBuilder(apiLevel, serviceName);
    }

    public OrchestrationContextBuilder getBuilder(HomeCommunityType homeCommunityType, String serviceName) {
        AdapterEndpointManager aem = new AdapterEndpointManager();
        NhincConstants.ADAPTER_API_LEVEL apiLevel = aem.getApiVersion(serviceName);
        return getBuilder(apiLevel, serviceName);
    }

    private OrchestrationContextBuilder getBuilder(NhincConstants.GATEWAY_API_LEVEL apiLevel,
            NhincConstants.NHIN_SERVICE_NAMES serviceName) {

        switch (serviceName) {
        case PATIENT_DISCOVERY:
            return OutboundPatientDiscoveryFactory.getInstance().createOrchestrationContextBuilder(apiLevel);
        case PATIENT_DISCOVERY_DEFERRED_REQUEST:
            return OutboundPatientDiscoveryDeferredRequestFactory.getInstance().createOrchestrationContextBuilder(
                    apiLevel);
        case PATIENT_DISCOVERY_DEFERRED_RESPONSE:
            return OutboundPatientDiscoveryDeferredResponseFactory.getInstance().createOrchestrationContextBuilder(
                    apiLevel);
        case DOCUMENT_QUERY:
            return OutboundDocQueryFactory.getInstance().createOrchestrationContextBuilder(apiLevel);
        case DOCUMENT_RETRIEVE:
            return OutboundDocRetrieveFactory.getInstance().createOrchestrationContextBuilder(apiLevel);
        case DOCUMENT_SUBMISSION:
            return OutboundDocSubmissionFactory.getInstance().createOrchestrationContextBuilder(apiLevel);
        case DOCUMENT_SUBMISSION_DEFERRED_REQUEST:
            return OutboundDocSubmissionDeferredRequestFactory.getInstance()
                    .createOrchestrationContextBuilder(apiLevel);
        case DOCUMENT_SUBMISSION_DEFERRED_RESPONSE:
            return OutboundDocSubmissionDeferredResponseFactory.getInstance().createOrchestrationContextBuilder(
                    apiLevel);
        case ADMINISTRATIVE_DISTRIBUTION:
            return OutboundAdminDistributionFactory.getInstance().createOrchestrationContextBuilder(apiLevel);
        case HIEM_SUBSCRIBE:
        case HIEM_NOTIFY:
        case HIEM_UNSUBSCRIBE:
        }

        /*
         * if (NhincConstants.ADAPTER_DOC_RETRIEVE_SERVICE_NAME.equals(serviceName)) { return
         * InboundDocRetrieveFactory.getInstance().createOrchestrationContextBuilder(apiLevel);
         */

        return null;
    }

    private OrchestrationContextBuilder getBuilder(NhincConstants.ADAPTER_API_LEVEL apiLevel, String serviceName) {

        if (NhincConstants.ADAPTER_DOC_RETRIEVE_SERVICE_NAME.equals(serviceName)) {
            return InboundDocRetrieveFactory.getInstance().createOrchestrationContextBuilder(apiLevel);
        }
        return null;
    }
}
