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
import gov.hhs.fha.nhinc.docsubmission.entity.EntityDocSubmissionOrchImpl;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

/**
 * @author akong
 *
 */
public class EntityDocSubmissionProxyImplTest {
    
    private static String TEST_STATUS = "test";
    
    protected Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    final EntityDocSubmissionOrchImpl mockOrchImpl = context.mock(EntityDocSubmissionOrchImpl.class);
    
    @Test
    public void testNoOpImpl() {
        EntityDocSubmissionProxyNoOpImpl noOpImpl = new EntityDocSubmissionProxyNoOpImpl();
        RegistryResponseType response = noOpImpl.provideAndRegisterDocumentSetB(null, null, null, null);
        
        context.assertIsSatisfied();
        assertNotNull(response);
        assertNull(response.getStatus());
    }
    
    @Test
    public void testJavaImpl() {
        setEntityOrchJavaImplExpectations();
        
        EntityDocSubmissionProxyJavaImpl javaImpl = createEntityDocSubmissionProxyJavaImpl();
        RegistryResponseType response = javaImpl.provideAndRegisterDocumentSetB(null, null, null, null);
        
        assertNotNull(response);
        assertEquals(TEST_STATUS, response.getStatus());
    }
    
    private void setEntityOrchJavaImplExpectations() {
        context.checking(new Expectations() {
            {
                oneOf(mockOrchImpl).provideAndRegisterDocumentSetB(with(any(ProvideAndRegisterDocumentSetRequestType.class)), with(any(AssertionType.class)),
                        with(any(NhinTargetCommunitiesType.class)), with(any(UrlInfoType.class)));
                will(returnValue(createRegistryResponseType()));
            }
        });
    }
    
    private RegistryResponseType createRegistryResponseType() {
        RegistryResponseType response = new RegistryResponseType();
        response.setStatus(TEST_STATUS);
        return response;
    }
    
    private EntityDocSubmissionProxyJavaImpl createEntityDocSubmissionProxyJavaImpl() {
        return new EntityDocSubmissionProxyJavaImpl() {
            protected EntityDocSubmissionOrchImpl getEntityDocSubmissionOrchImpl() {
                return mockOrchImpl;
            }
            
        };
    }
}
