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
package gov.hhs.fha.nhinc.policyengine.adapter.pep;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyRequestType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyResponseType;
import javax.xml.ws.WebServiceContext;
import org.apache.commons.logging.Log;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * 
 * @author Neil Webb
 */
@RunWith(JMock.class)
public class AdapterPEPServiceImplTest {
    Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    final Log mockLog = context.mock(Log.class);
    final WebServiceContext mockWebServiceContext = context.mock(WebServiceContext.class);
    final AdapterPEPImpl mockAdapterPEPImpl = context.mock(AdapterPEPImpl.class);

    @Test
    public void testCreateLogger() {
        try {
            AdapterPEPServiceImpl sut = new AdapterPEPServiceImpl() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }
            };

            Log log = sut.createLogger();
            assertNotNull("Log was null", log);
        } catch (Throwable t) {
            System.out.println("Error running testCreateLogger: " + t.getMessage());
            t.printStackTrace();
            fail("Error running testCreateLogger: " + t.getMessage());
        }
    }

    @Test
    public void testGetAdapterPEPImpl() {
        try {
            AdapterPEPServiceImpl sut = new AdapterPEPServiceImpl() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }

                @Override
                protected AdapterPEPImpl getAdapterPEPImpl() {
                    return mockAdapterPEPImpl;
                }
            };

            AdapterPEPImpl pepImpl = sut.getAdapterPEPImpl();
            assertNotNull("AdapterPEPImpl was null", pepImpl);
        } catch (Throwable t) {
            System.out.println("Error running testGetAdapterPEPImpl: " + t.getMessage());
            t.printStackTrace();
            fail("Error running testGetAdapterPEPImpl: " + t.getMessage());
        }
    }

    @Test
    public void testLoadAssertion() {
        try {
            AdapterPEPServiceImpl sut = new AdapterPEPServiceImpl() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }

                @Override
                protected void loadAssertion(AssertionType assertion, WebServiceContext wsContext) throws Exception {
                }
            };

            final AssertionType mockAssertion = context.mock(AssertionType.class);
            sut.loadAssertion(mockAssertion, mockWebServiceContext);
            assertTrue("Passed loadAssertion", true);
        } catch (Throwable t) {
            System.out.println("Error running testLoadAssertion: " + t.getMessage());
            t.printStackTrace();
            fail("Error running testLoadAssertion: " + t.getMessage());
        }
    }

    @Test
    public void testCheckPolicyHappy() {
        try {
            AdapterPEPServiceImpl sut = new AdapterPEPServiceImpl() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }

                @Override
                protected AdapterPEPImpl getAdapterPEPImpl() {
                    return mockAdapterPEPImpl;
                }

                @Override
                protected void loadAssertion(AssertionType assertion, WebServiceContext wsContext) throws Exception {
                }
            };
            context.checking(new Expectations() {
                {
                    allowing(mockLog).debug(with(aNonNull(String.class)));
                    oneOf(mockAdapterPEPImpl).checkPolicy(with(aNonNull(CheckPolicyRequestType.class)),
                            with(aNonNull(AssertionType.class)));
                }
            });

            CheckPolicyRequestType request = new CheckPolicyRequestType();
            AssertionType assertion = new AssertionType();
            request.setAssertion(assertion);
            CheckPolicyResponseType response = sut.checkPolicy(request, mockWebServiceContext);
            assertNotNull("CheckPolicyResponseType was null", response);
        } catch (Throwable t) {
            System.out.println("Error running testCheckPolicyHappy: " + t.getMessage());
            t.printStackTrace();
            fail("Error running testCheckPolicyHappy: " + t.getMessage());
        }
    }

    @Test
    public void testCheckPolicyException() {
        try {
            AdapterPEPServiceImpl sut = new AdapterPEPServiceImpl() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }

                @Override
                protected AdapterPEPImpl getAdapterPEPImpl() {
                    return mockAdapterPEPImpl;
                }

                @Override
                protected void loadAssertion(AssertionType assertion, WebServiceContext wsContext) throws Exception {
                    throw new IllegalArgumentException("Forced error.");
                }
            };
            context.checking(new Expectations() {
                {
                    allowing(mockLog).debug(with(aNonNull(String.class)));
                    oneOf(mockLog).error(with(aNonNull(String.class)), with(aNonNull(IllegalArgumentException.class)));
                }
            });

            CheckPolicyRequestType request = new CheckPolicyRequestType();

            sut.checkPolicy(request, mockWebServiceContext);
            fail("Should have had exception.");
        } catch (RuntimeException e) {
            assertEquals("Exception message",
                    "Error occurred calling AdapterPEPImpl.checkPolicy.  Error: Forced error.", e.getMessage());
        } catch (Throwable t) {
            System.out.println("Error running testCheckPolicyException: " + t.getMessage());
            t.printStackTrace();
            fail("Error running testCheckPolicyException: " + t.getMessage());
        }
    }

}