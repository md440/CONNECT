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
package gov.hhs.fha.nhinc.docsubmission.entity;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommon.UrlInfoType;
import gov.hhs.fha.nhinc.common.nhinccommonentity.RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType;
import gov.hhs.fha.nhinc.docsubmission.XDRAuditLogger;
import gov.hhs.fha.nhinc.docsubmission.XDRPolicyChecker;
import gov.hhs.fha.nhinc.gateway.aggregator.document.DocumentConstants;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.transform.policy.SubjectHelper;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryError;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryErrorList;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EntityDocSubmissionOrchImpl {

    private static Log log = LogFactory.getLog(EntityDocSubmissionOrchImpl.class);
    private XDRAuditLogger auditLogger = null;
    
    public EntityDocSubmissionOrchImpl() {
        log = getLogger();
        auditLogger = getXDRAuditLogger();
    }

    public RegistryResponseType provideAndRegisterDocumentSetB(ProvideAndRegisterDocumentSetRequestType body,
            AssertionType assertion, NhinTargetCommunitiesType targets, UrlInfoType urlInfo) {
        RegistryResponseType response = null;

        RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType request = createRequestForInternalProcessing(
                body, assertion, targets, urlInfo);

        auditRequestFromAdapter(request, assertion);

        if (isPolicyValid(request, assertion)) {
            log.info("Policy check successful");
            response = getResponseFromTarget(request, assertion);
        } else {
            log.error("Failed policy check.  Sending error response.");
            response = createFailedPolicyCheckResponse();
        }

        auditResponseToAdapter(response, assertion);

        return response;
    }

    protected boolean hasNhinTargetHomeCommunityId(
            RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType request) {

        if (request != null
                && request.getNhinTargetCommunities() != null
                && NullChecker.isNotNullish(request.getNhinTargetCommunities().getNhinTargetCommunity())
                && request.getNhinTargetCommunities().getNhinTargetCommunity().get(0) != null
                && request.getNhinTargetCommunities().getNhinTargetCommunity().get(0).getHomeCommunity() != null
                && NullChecker.isNotNullish(request.getNhinTargetCommunities().getNhinTargetCommunity().get(0)
                        .getHomeCommunity().getHomeCommunityId())) {
            return true;
        }

        return false;
    }
    
    protected Log getLogger() {
        return log;
    }

    protected XDRAuditLogger getXDRAuditLogger() {
        return new XDRAuditLogger();
    }

    protected XDRPolicyChecker getXDRPolicyChecker() {
        return new XDRPolicyChecker();
    }

    protected SubjectHelper getSubjectHelper() {
        return new SubjectHelper();
    }

    protected OutboundDocSubmissionDelegate getOutboundDocSubmissionDelegate() {
        return new OutboundDocSubmissionDelegate();
    }
    
    private RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType createRequestForInternalProcessing(
            ProvideAndRegisterDocumentSetRequestType msg, AssertionType assertion, NhinTargetCommunitiesType targets,
            UrlInfoType urlInfo) {
        RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType request = new RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType();
        request.setProvideAndRegisterDocumentSetRequest(msg);
        request.setNhinTargetCommunities(targets);
        request.setUrl(urlInfo);

        return request;
    }
    
    private boolean isPolicyValid(RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType request,
            AssertionType assertion) {
        boolean isValid = false;

        if (hasNhinTargetHomeCommunityId(request)) {
            String senderHCID = getSubjectHelper().determineSendingHomeCommunityId(assertion.getHomeCommunity(),
                    assertion);
            String receiverHCID = getNhinTargetHomeCommunityId(request);

            isValid = getXDRPolicyChecker().checkXDRRequestPolicy(request.getProvideAndRegisterDocumentSetRequest(),
                    assertion, senderHCID, receiverHCID, NhincConstants.POLICYENGINE_OUTBOUND_DIRECTION);
        } else {
            log.warn("Check on policy requires a non null target home community ID specified in the request");
        }
        log.debug("Check on policy returns: " + isValid);

        return isValid;
    }

    private RegistryResponseType getResponseFromTarget(
            RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType request, AssertionType assertion) {

        RegistryResponseType nhinResponse = new RegistryResponseType();
        if (hasNhinTargetHomeCommunityId(request)) {
            gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType nhinRequest = createRequestForNhin(request);

            try {
                nhinResponse = sendToNhinProxy(nhinRequest, assertion);
            } catch (Exception e) {
                String hcid = getNhinTargetHomeCommunityId(request);
                nhinResponse = createFailedNhinSendResponse(hcid);
                log.error("Fault encountered while trying to send message to the nhin " + hcid, e);
            }
        } else {
            log.warn("The request to the Nhin did not contain a target home community id.");
        }

        return nhinResponse;
    }

    private gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType createRequestForNhin(
            RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType request) {

        gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType nhinRequest = 
            new gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType();

        NhinTargetSystemType targetSystemType = new NhinTargetSystemType();
        targetSystemType.setHomeCommunity(getNhinTargetHomeCommunity(request));

        nhinRequest.setNhinTargetSystem(targetSystemType);
        nhinRequest.setProvideAndRegisterDocumentSetRequest(request.getProvideAndRegisterDocumentSetRequest());

        return nhinRequest;
    }
    
    private RegistryResponseType sendToNhinProxy(
            gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType request,
            AssertionType assertion) {

        auditRequestToNhin(request, assertion);

        OutboundDocSubmissionDelegate dsDelegate = getOutboundDocSubmissionDelegate();
        OutboundDocSubmissionOrchestratable dsOrchestratable = createOrchestratable(dsDelegate, request, assertion);
        RegistryResponseType response = ((OutboundDocSubmissionOrchestratable) dsDelegate.process(dsOrchestratable)).getResponse();

        auditResponseFromNhin(response, assertion);

        return response;
    }

    private OutboundDocSubmissionOrchestratable createOrchestratable(
            OutboundDocSubmissionDelegate delegate,
            gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType request,
            AssertionType assertion) {

        OutboundDocSubmissionOrchestratable dsOrchestratable = new OutboundDocSubmissionOrchestratable(delegate);
        dsOrchestratable.setAssertion(assertion);
        dsOrchestratable.setRequest(request.getProvideAndRegisterDocumentSetRequest());
        dsOrchestratable.setTarget(request.getNhinTargetSystem());

        return dsOrchestratable;
    }

    private void auditRequestFromAdapter(RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType request,
            AssertionType assertion) {
        auditLogger.auditEntityXDR(request, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION);
    }

    private void auditResponseToAdapter(RegistryResponseType response, AssertionType assertion) {
        auditLogger.auditEntityXDRResponse(response, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION);
    }

    private void auditRequestToNhin(
            gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType request,
            AssertionType assertion) {
        auditLogger.auditXDR(request, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION);
    }

    private void auditResponseFromNhin(RegistryResponseType response, AssertionType assertion) {
        auditLogger.auditNhinXDRResponse(response, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION);
    }

    private HomeCommunityType getNhinTargetHomeCommunity(
            RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType request) {
        return request.getNhinTargetCommunities().getNhinTargetCommunity().get(0).getHomeCommunity();
    }

    private String getNhinTargetHomeCommunityId(RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType request) {
        return getNhinTargetHomeCommunity(request).getHomeCommunityId();
    }

    private RegistryResponseType createFailedPolicyCheckResponse() {
        RegistryErrorList regErrList = new RegistryErrorList();
        regErrList.setHighestSeverity(NhincConstants.XDS_REGISTRY_ERROR_SEVERITY_ERROR);
        RegistryError regErr = new RegistryError();
        regErrList.getRegistryError().add(regErr);
        regErr.setCodeContext("Policy Check Failed");
        regErr.setErrorCode("CONNECTPolicyCheckFailed");
        regErr.setSeverity(NhincConstants.XDS_REGISTRY_ERROR_SEVERITY_ERROR);

        RegistryResponseType response = new RegistryResponseType();
        response.setRegistryErrorList(regErrList);
        response.setStatus(DocumentConstants.XDS_SUBMISSION_RESPONSE_STATUS_FAILURE);

        return response;
    }

    private RegistryResponseType createFailedNhinSendResponse(String hcid) {
        RegistryErrorList regErrList = new RegistryErrorList();
        RegistryError regErr = new RegistryError();
        regErrList.getRegistryError().add(regErr);
        regErr.setCodeContext("Fault encountered processing provideAndRegisterDocumentSetB for community " + hcid);
        regErr.setErrorCode("XDSRegistryBusy");
        regErr.setSeverity(NhincConstants.XDS_REGISTRY_ERROR_SEVERITY_ERROR);

        RegistryResponseType response = new RegistryResponseType();
        response.setRegistryErrorList(regErrList);
        response.setStatus(DocumentConstants.XDS_SUBMISSION_RESPONSE_STATUS_FAILURE);

        return response;
    }

}
