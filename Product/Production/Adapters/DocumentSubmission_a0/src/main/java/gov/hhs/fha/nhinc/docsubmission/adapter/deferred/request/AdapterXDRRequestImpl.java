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
package gov.hhs.fha.nhinc.docsubmission.adapter.deferred.request;

import gov.hhs.fha.nhinc.async.AsyncMessageIdExtractor;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.AdapterProvideAndRegisterDocumentSetRequestType;
import gov.hhs.fha.nhinc.saml.extraction.SamlTokenExtractor;
import gov.hhs.healthit.nhin.XDRAcknowledgementType;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import javax.xml.ws.WebServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author dunnek
 */
public class AdapterXDRRequestImpl {
    private Log log = null;

    public AdapterXDRRequestImpl() {
        log = createLogger();
    }

    protected Log createLogger() {
        return LogFactory.getLog(getClass());
    }

    public XDRAcknowledgementType provideAndRegisterDocumentSetBRequest(
            AdapterProvideAndRegisterDocumentSetRequestType body, WebServiceContext context) {
        log.debug("Begin AdapterXDRRequestImpl.provideAndRegisterDocumentSetBRequest(unsecure)");
        XDRAcknowledgementType response = null;

        ProvideAndRegisterDocumentSetRequestType request = null;
        String url = null;
        AssertionType assertion = null;
        if (body != null) {
            request = body.getProvideAndRegisterDocumentSetRequest();
            url = body.getUrl();
            assertion = body.getAssertion();
        }
        assertion = getAssertion(context, assertion);

        response = provideAndRegisterDocumentSetBRequest(request, url, assertion);
        log.debug("End AdapterXDRRequestImpl.provideAndRegisterDocumentSetBRequest(unsecure)");
        return response;
    }

    public XDRAcknowledgementType provideAndRegisterDocumentSetBRequest(
            gov.hhs.fha.nhinc.common.nhinccommonadapter.AdapterProvideAndRegisterDocumentSetSecuredRequestType body,
            WebServiceContext context) {
        log.debug("Begin AdapterXDRRequestImpl.provideAndRegisterDocumentSetBRequest(secure)");
        XDRAcknowledgementType response = null;

        ProvideAndRegisterDocumentSetRequestType request = null;
        String url = null;
        AssertionType assertion = null;
        if (body != null) {
            request = body.getProvideAndRegisterDocumentSetRequest();
            url = body.getUrl();
        }
        assertion = getAssertion(context, assertion);

        response = provideAndRegisterDocumentSetBRequest(request, url, assertion);
        log.debug("End AdapterXDRRequestImpl.provideAndRegisterDocumentSetBRequest(secure)");
        return response;
    }

    protected AssertionType getAssertion(WebServiceContext context, AssertionType oAssertionIn) {
        AssertionType assertion = null;
        if (oAssertionIn == null) {
            assertion = SamlTokenExtractor.GetAssertion(context);
        } else {
            assertion = oAssertionIn;
        }
        // Extract the message id value from the WS-Addressing Header and place it in the Assertion Class
        if (assertion != null) {
            assertion.setMessageId(AsyncMessageIdExtractor.GetAsyncMessageId(context));
        }

        return assertion;
    }

    protected XDRAcknowledgementType provideAndRegisterDocumentSetBRequest(
            ProvideAndRegisterDocumentSetRequestType request, String url, AssertionType assertion) {
        return new AdapterDocSubmissionDeferredRequestOrchImpl().provideAndRegisterDocumentSetBRequest(request, url,
                assertion);
    }
}
