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

import static org.junit.Assert.*;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;

import org.junit.Test;


public class OutboundDocSubmissionOrchestratableTest {

    @Test
    public void testGetters() {        
        OutboundDocSubmissionOrchestratable orchestratable = createOutboundDocSubmissionOrchestratable();
        
        assertNotNull(orchestratable.getAssertion());
        assertNotNull(orchestratable.getDelegate());
        assertNotNull(orchestratable.getNhinDelegate());
        assertNotNull(orchestratable.getRequest());
        assertNotNull(orchestratable.getServiceName());
        assertNull(orchestratable.getResponse());
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testGetAggregator() throws UnsupportedOperationException {
        OutboundDocSubmissionOrchestratable orchestratable = createOutboundDocSubmissionOrchestratable();
        
        orchestratable.getAggregator();
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testIsEnabled() throws UnsupportedOperationException {
        OutboundDocSubmissionOrchestratable orchestratable = createOutboundDocSubmissionOrchestratable();
        
        orchestratable.isEnabled();
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testIsPassthru() throws UnsupportedOperationException {
        OutboundDocSubmissionOrchestratable orchestratable = createOutboundDocSubmissionOrchestratable();
        
        orchestratable.isPassthru();
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testGetAuditTransformer() throws UnsupportedOperationException {
        OutboundDocSubmissionOrchestratable orchestratable = createOutboundDocSubmissionOrchestratable();
        
        orchestratable.getAuditTransformer();
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testGetPolicyTransformer() throws UnsupportedOperationException {
        OutboundDocSubmissionOrchestratable orchestratable = createOutboundDocSubmissionOrchestratable();
        
        orchestratable.getPolicyTransformer();
    }
   
    private OutboundDocSubmissionOrchestratable createOutboundDocSubmissionOrchestratable() {
        OutboundDocSubmissionDelegate delegate = new OutboundDocSubmissionDelegate();    
        ProvideAndRegisterDocumentSetRequestType request = new ProvideAndRegisterDocumentSetRequestType();
        NhinTargetSystemType target = new NhinTargetSystemType();
        AssertionType assertion = new AssertionType();
        
        return new OutboundDocSubmissionOrchestratable(delegate, request, target, assertion);
    }
    
}
