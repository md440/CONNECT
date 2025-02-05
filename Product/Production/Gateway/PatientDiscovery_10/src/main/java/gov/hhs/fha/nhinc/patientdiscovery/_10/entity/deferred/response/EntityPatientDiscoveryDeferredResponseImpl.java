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
package gov.hhs.fha.nhinc.patientdiscovery._10.entity.deferred.response;

import gov.hhs.fha.nhinc.async.AsyncMessageIdExtractor;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.patientdiscovery.entity.deferred.response.EntityPatientDiscoveryDeferredResponseOrch;
import gov.hhs.fha.nhinc.patientdiscovery.entity.deferred.response.EntityPatientDiscoveryDeferredResponseOrchFactory;
import gov.hhs.fha.nhinc.patientdiscovery.nhin.GenericFactory;
import gov.hhs.fha.nhinc.saml.extraction.SamlTokenExtractor;
import java.util.List;
import org.hl7.v3.RespondingGatewayPRPAIN201306UV02SecuredRequestType;
import org.hl7.v3.RespondingGatewayPRPAIN201306UV02RequestType;
import org.hl7.v3.MCCIIN000002UV01;
import javax.xml.ws.WebServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.v3.PRPAIN201306UV02;

/**
 * 
 * @author Neil Webb
 */
public class EntityPatientDiscoveryDeferredResponseImpl {
    private Log log = null;

    private GenericFactory<EntityPatientDiscoveryDeferredResponseOrch> orchestrationFactory;

    public EntityPatientDiscoveryDeferredResponseImpl() {
        log = createLogger();
        orchestrationFactory = new EntityPatientDiscoveryDeferredResponseOrchFactory();
    }

    protected Log createLogger() {
        return LogFactory.getLog(getClass());
    }

    public MCCIIN000002UV01 processPatientDiscoveryAsyncResp(
            RespondingGatewayPRPAIN201306UV02SecuredRequestType request, WebServiceContext context) {
        log.debug("Begin EntityPatientDiscoveryDeferredResponseImpl.processPatientDiscoveryAsyncResp(secured)");
        MCCIIN000002UV01 response = null;
        AssertionType assertion = getAssertion(context, null);
        PRPAIN201306UV02 body = null;
        NhinTargetCommunitiesType target = null;
        if (request != null) {
            body = request.getPRPAIN201306UV02();
            target = request.getNhinTargetCommunities();
        }
        response = orchestrationFactory.create().processPatientDiscoveryAsyncRespOrch(body, assertion, target);
        log.debug("End EntityPatientDiscoveryDeferredResponseImpl.processPatientDiscoveryAsyncResp(secured)");
        return response;
    }

    public MCCIIN000002UV01 processPatientDiscoveryAsyncResp(RespondingGatewayPRPAIN201306UV02RequestType request,
            WebServiceContext context) {
        log.debug("Begin EntityPatientDiscoveryDeferredResponseImpl.processPatientDiscoveryAsyncResp(unsecured)");
        MCCIIN000002UV01 response = null;
        AssertionType assertion = null;
        PRPAIN201306UV02 body = null;
        NhinTargetCommunitiesType target = null;
        if (request != null) {
            body = request.getPRPAIN201306UV02();
            assertion = request.getAssertion();
            target = request.getNhinTargetCommunities();
        }
        assertion = getAssertion(context, assertion);
        response = orchestrationFactory.create().processPatientDiscoveryAsyncRespOrch(body, assertion, target);
        log.debug("End EntityPatientDiscoveryDeferredResponseImpl.processPatientDiscoveryAsyncResp(unsecured)");
        return response;
    }

    private AssertionType getAssertion(WebServiceContext context, AssertionType oAssertionIn) {
        AssertionType assertion = null;
        if (oAssertionIn == null) {
            assertion = SamlTokenExtractor.GetAssertion(context);
        } else {
            assertion = oAssertionIn;
        }

        // Extract the message id value from the WS-Addressing Header and place it in the Assertion Class
        if (assertion != null) {
            assertion.setMessageId(AsyncMessageIdExtractor.GetAsyncMessageId(context));
            List<String> relatesToList = AsyncMessageIdExtractor.GetAsyncRelatesTo(context);
            if (NullChecker.isNotNullish(relatesToList)) {
                assertion.getRelatesToList().addAll(relatesToList);
            }
        }

        return assertion;
    }
}
