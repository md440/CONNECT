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

package gov.hhs.fha.nhinc.docsubmission.entity.proxy;

import static org.junit.Assert.*;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.common.nhinccommon.UrlInfoType;
import gov.hhs.fha.nhinc.common.nhinccommonentity.RespondingGatewayProvideAndRegisterDocumentSetRequestType;
import gov.hhs.fha.nhinc.common.nhinccommonentity.RespondingGatewayProvideAndRegisterDocumentSetSecuredRequestType;
import gov.hhs.fha.nhinc.gateway.aggregator.document.DocumentConstants;
import gov.hhs.fha.nhinc.nhincentityxdr.EntityXDRPortType;
import gov.hhs.fha.nhinc.nhincentityxdrsecured.EntityXDRSecuredPortType;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

import org.apache.commons.logging.Log;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

/**
 * @author akong
 * 
 */
public class EntityDocSubmissionProxyWebServiceUnsecuredImplTest {

    private static final String mockUrl = "http://localhost:8080/";

    protected Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    final Log mockLog = context.mock(Log.class);
    final WebServiceProxyHelper mockProxyHelper = context.mock(WebServiceProxyHelper.class);
    final Service mockService = context.mock(Service.class);
    final EntityXDRPortType mockPort = context.mock(EntityXDRPortType.class);

    @Test
    public void testProvideAndRegisterDocumentSetB() throws Exception {

        setMockLoggerExpectations();
        setMockWebServiceProxyHelperExpectations();
        setMockServiceExpectations();

        EntityDocSubmissionProxyWebServiceUnsecuredImpl proxyImpl = createProxyImpl();
        RegistryResponseType response = invokeProvideAndRegisterDocumentSetB(proxyImpl);

        assertEquals(DocumentConstants.XDS_SUBMISSION_RESPONSE_STATUS_SUCCESS, response.getStatus());
        context.assertIsSatisfied();
    }
    
    @Test 
    public void testProvideAndRegisterDocumentSetB_nullService() throws Exception {
        
        setMockLoggerExpectations();
        setMockWebServiceProxyHelperExpectations();
        
        EntityDocSubmissionProxyWebServiceUnsecuredImpl proxyImpl = createProxyImpl_nullService();
        RegistryResponseType response = invokeProvideAndRegisterDocumentSetB(proxyImpl);
        
        assertNull(response.getStatus());
    }
    
    @Test 
    public void testProvideAndRegisterDocumentSetB_nullPort() throws Exception {
        
        setMockLoggerExpectations();
        setMockWebServiceProxyHelperExpectations();
        
        EntityDocSubmissionProxyWebServiceUnsecuredImpl proxyImpl = createProxyImpl_nullPort();
        RegistryResponseType response = invokeProvideAndRegisterDocumentSetB(proxyImpl);
        
        assertNull(response.getStatus());
    }
    
    @Test
    public void testGetters() {
        EntityDocSubmissionProxyWebServiceUnsecuredImpl proxyImpl = new EntityDocSubmissionProxyWebServiceUnsecuredImpl();
        
        assertNotNull(proxyImpl.createLogger());
        assertNotNull(proxyImpl.createWebServiceProxyHelper());
    }
    
    private RegistryResponseType invokeProvideAndRegisterDocumentSetB(EntityDocSubmissionProxyWebServiceUnsecuredImpl proxyImpl) {
        ProvideAndRegisterDocumentSetRequestType message = new ProvideAndRegisterDocumentSetRequestType();
        AssertionType assertion = new AssertionType();
        NhinTargetCommunitiesType targets = new NhinTargetCommunitiesType();
        UrlInfoType urlInfo = new UrlInfoType();
        
        return proxyImpl.provideAndRegisterDocumentSetB(message, assertion, targets, urlInfo);
    }
    
    private void setMockLoggerExpectations() {
        context.checking(new Expectations() {
            {
                ignoring(mockLog);
            }
        });
    }

    private void setMockWebServiceProxyHelperExpectations() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(mockProxyHelper).getUrlLocalHomeCommunity(NhincConstants.ENTITY_XDR_SERVICE_NAME);
                will(returnValue(mockUrl));

                oneOf(mockProxyHelper).createService(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)));
                will(returnValue(mockService));

                oneOf(mockProxyHelper).invokePort(with(any(Object.class)), with(any(Class.class)),
                        with(any(String.class)),
                        with(any(RespondingGatewayProvideAndRegisterDocumentSetRequestType.class)));
                will(returnValue(createValidRegistryResponse()));
            }
        });
    }

    private void setMockServiceExpectations() {
        context.checking(new Expectations() {
            {
                oneOf(mockService).getPort(with(any(QName.class)), with(any(Class.class)));
                will(returnValue(mockPort));
            }
        });
    }

    private RegistryResponseType createValidRegistryResponse() {
        RegistryResponseType response = new RegistryResponseType();
        response.setStatus(DocumentConstants.XDS_SUBMISSION_RESPONSE_STATUS_SUCCESS);

        return response;
    }

    private EntityDocSubmissionProxyWebServiceUnsecuredImpl createProxyImpl() {
        return new EntityDocSubmissionProxyWebServiceUnsecuredImpl() {
            protected Log createLogger() {
                return mockLog;
            }

            protected WebServiceProxyHelper createWebServiceProxyHelper() {
                return mockProxyHelper;
            }

            protected void initializeUnsecurePort(EntityXDRPortType port, String url, AssertionType assertion) {
                return;
            }
        };
    }

    private EntityDocSubmissionProxyWebServiceUnsecuredImpl createProxyImpl_nullService() {
        return new EntityDocSubmissionProxyWebServiceUnsecuredImpl() {
            protected Log createLogger() {
                return mockLog;
            }

            protected WebServiceProxyHelper createWebServiceProxyHelper() {
                return mockProxyHelper;
            }

            protected Service getService() {
                return null;
            }
        };
    }
    
    private EntityDocSubmissionProxyWebServiceUnsecuredImpl createProxyImpl_nullPort() {
        return new EntityDocSubmissionProxyWebServiceUnsecuredImpl() {
            protected Log createLogger() {
                return mockLog;
            }

            protected WebServiceProxyHelper createWebServiceProxyHelper() {
                return mockProxyHelper;
            }

            protected EntityXDRPortType getPort(String url, AssertionType assertion) {
                return null;
            }
        };
    }
}
