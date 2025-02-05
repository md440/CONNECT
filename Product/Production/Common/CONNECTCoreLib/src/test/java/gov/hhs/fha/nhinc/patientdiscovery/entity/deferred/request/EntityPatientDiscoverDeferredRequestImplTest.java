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
package gov.hhs.fha.nhinc.patientdiscovery.entity.deferred.request;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import gov.hhs.fha.nhinc.async.AsyncMessageProcessHelper;
import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.UrlInfoType;
import gov.hhs.fha.nhinc.common.nhinccommonentity.NotifyRequestType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManager;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.connectmgr.UrlInfo;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.patientcorrelation.nhinc.dao.PDDeferredCorrelationDao;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscovery201305Processor;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryAuditLogger;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryAuditor;
import gov.hhs.fha.nhinc.patientdiscovery.testhelper.TestHelper;
import gov.hhs.fha.nhinc.transform.subdisc.HL7AckTransforms;
import gov.hhs.fha.nhinc.transform.subdisc.HL7PRPA201305Transforms;
import gov.hhs.fha.nhinc.transform.subdisc.HL7PatientTransforms;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.WebServiceContext;

import org.hl7.v3.II;
import org.hl7.v3.MCCIIN000002UV01;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAMT201301UV02Patient;
import org.hl7.v3.PRPAMT201301UV02Person;
import org.hl7.v3.RespondingGatewayPRPAIN201305UV02RequestType;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.Notify;

public class EntityPatientDiscoverDeferredRequestImplTest {

    private Mockery context;

	public EntityPatientDiscoverDeferredRequestImplTest() {
    }
   
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setup() {
        context = new Mockery() {

            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of processPatientDiscoveryAsyncReq method, of class EntityPatientDiscoveryAsyncRequestOrchImpl.
     */
    @Test
    public void testProcessPatientDiscoveryAsyncReq() {
        System.out.println("testProcessPatientDiscoveryAsyncReq");

        EntityPatientDiscoveryDeferredRequestOrchImpl instance = new EntityPatientDiscoveryDeferredRequestOrchImpl() {

            @Override
            protected List<UrlInfo> getTargetEndpoints(NhinTargetCommunitiesType targetCommunities) {
                List<UrlInfo> urlInfo = new ArrayList<UrlInfo>();

                UrlInfo target = new UrlInfo();
                target.setHcid("2.2");
                target.setUrl("https://test.com:8181/NhinPatientDiscoveryAsync");
                urlInfo.add(target);

                return urlInfo;
            }

            @Override
            protected boolean checkPolicy(RespondingGatewayPRPAIN201305UV02RequestType request) {
                return true;
            }

            @Override
            protected MCCIIN000002UV01 sendToProxy(PRPAIN201305UV02 request, AssertionType newAssertion, UrlInfo urlInfo) {
                return HL7AckTransforms.createAckFrom201305(request, "Success");
            }

            @Override
            protected PatientDiscoveryAuditor createAuditLogger() {
                PatientDiscoveryAuditor auditLogger = new PatientDiscoveryAuditLogger() {

                    @Override
                    public AcknowledgementType auditEntity201305(RespondingGatewayPRPAIN201305UV02RequestType request,
                            AssertionType assertion, String direction) {
                        return new AcknowledgementType();
                    }

                    @Override
                    public AcknowledgementType auditAck(MCCIIN000002UV01 request, AssertionType assertion,
                            String direction, String _interface) {
                        return new AcknowledgementType();
                    }
                };
                return auditLogger;
            }

            @Override
            protected AsyncMessageProcessHelper createAsyncProcesser() {
                AsyncMessageProcessHelper processHelper = new AsyncMessageProcessHelper() {

                    @Override
                    public boolean addPatientDiscoveryRequest(RespondingGatewayPRPAIN201305UV02RequestType request,
                            String direction) {
                        return true;
                    }

                    @Override
                    public boolean processAck(String messageId, String newStatus, String errorStatus,
                            MCCIIN000002UV01 ack) {
                        return true;
                    }
                };
                return processHelper;
            }

            @Override
            protected PatientDiscovery201305Processor getPatientDiscovery201305Processor() {
                PatientDiscovery201305Processor processHelper = new PatientDiscovery201305Processor() {

                    @Override
                    public void storeLocalMapping(RespondingGatewayPRPAIN201305UV02RequestType request) {
                        return;
                    }
                };
                return processHelper;
            }

            @Override
            protected PDDeferredCorrelationDao getPDDeferredCorrelationDao() {
                PDDeferredCorrelationDao pdCorrelationDao = new PDDeferredCorrelationDao() {

                    @Override
                    public void saveOrUpdate(String messageId, II patientId) {
                        return;
                    }
                };
                return pdCorrelationDao;
            }
        };

        AssertionType assertion = new AssertionType();
        NhinTargetCommunitiesType targets = new NhinTargetCommunitiesType();

        JAXBElement<PRPAMT201301UV02Person> person = HL7PatientTransforms.create201301PatientPerson("Joe", "Smith",
                "M", null, null);
        PRPAMT201301UV02Patient patient = HL7PatientTransforms.create201301Patient(person, "1234", "1.1.1");
        PRPAIN201305UV02 msg = HL7PRPA201305Transforms.createPRPA201305(patient, "1.1", "2.2", "1.1.1");

        MCCIIN000002UV01 result = instance.processPatientDiscoveryAsyncReq(msg, assertion, targets);

        assertNotNull(result);
        TestHelper.assertAckMsgEquals("Success", result);
        TestHelper.assertReceiverEquals("1.1", result);
        TestHelper.assertSenderEquals("2.2", result);
        TestHelper.assertAckMsgIdEquals(msg.getId(), result);
    }

    /**
     * Test of processPatientDiscoveryAsyncReq method, of class EntityPatientDiscoveryAsyncRequestOrchImpl.
     */
    @Test
    public void testProcessPatientDiscoveryAsyncReqNoTargets() {
        System.out.println("testProcessPatientDiscoveryAsyncReqNoTargets");

        EntityPatientDiscoveryDeferredRequestOrchImpl instance = new EntityPatientDiscoveryDeferredRequestOrchImpl() {

            @Override
            protected List<UrlInfo> getTargetEndpoints(NhinTargetCommunitiesType targetCommunities) {
                return null;
            }

            @Override
            protected boolean checkPolicy(RespondingGatewayPRPAIN201305UV02RequestType request) {
                return true;
            }

            @Override
            protected MCCIIN000002UV01 sendToProxy(PRPAIN201305UV02 request, AssertionType newAssertion, UrlInfo urlInfo) {
                return HL7AckTransforms.createAckFrom201305(request, "Success");
            }

            @Override
            protected PatientDiscoveryAuditor createAuditLogger() {
                PatientDiscoveryAuditor auditLogger = new PatientDiscoveryAuditLogger() {

                    @Override
                    public AcknowledgementType auditEntity201305(RespondingGatewayPRPAIN201305UV02RequestType request,
                            AssertionType assertion, String direction) {
                        return new AcknowledgementType();
                    }

                    @Override
                    public AcknowledgementType auditAck(MCCIIN000002UV01 request, AssertionType assertion,
                            String direction, String _interface) {
                        return new AcknowledgementType();
                    }
                };
                return auditLogger;
            }

            @Override
            protected AsyncMessageProcessHelper createAsyncProcesser() {
                AsyncMessageProcessHelper processHelper = new AsyncMessageProcessHelper() {

                    @Override
                    public boolean addPatientDiscoveryRequest(RespondingGatewayPRPAIN201305UV02RequestType request,
                            String direction) {
                        return true;
                    }

                    @Override
                    public boolean processAck(String messageId, String newStatus, String errorStatus,
                            MCCIIN000002UV01 ack) {
                        return true;
                    }
                };
                return processHelper;
            }

            @Override
            protected PatientDiscovery201305Processor getPatientDiscovery201305Processor() {
                PatientDiscovery201305Processor processHelper = new PatientDiscovery201305Processor() {

                    @Override
                    public void storeLocalMapping(RespondingGatewayPRPAIN201305UV02RequestType request) {
                        return;
                    }
                };
                return processHelper;
            }

            @Override
            protected PDDeferredCorrelationDao getPDDeferredCorrelationDao() {
                PDDeferredCorrelationDao pdCorrelationDao = new PDDeferredCorrelationDao() {

                    @Override
                    public void saveOrUpdate(String messageId, II patientId) {
                        return;
                    }
                };
                return pdCorrelationDao;
            }
        };

        AssertionType assertion = new AssertionType();
        NhinTargetCommunitiesType targets = new NhinTargetCommunitiesType();

        JAXBElement<PRPAMT201301UV02Person> person = HL7PatientTransforms.create201301PatientPerson("Joe", "Smith",
                "M", null, null);
        PRPAMT201301UV02Patient patient = HL7PatientTransforms.create201301Patient(person, "1234", "1.1.1");
        PRPAIN201305UV02 msg = HL7PRPA201305Transforms.createPRPA201305(patient, "1.1", "2.2", "1.1.1");

        MCCIIN000002UV01 result = instance.processPatientDiscoveryAsyncReq(msg, assertion, targets);

        assertNotNull(result);
        TestHelper.assertAckMsgEquals("No targets were found for the Patient Discovery Request", result);
        TestHelper.assertReceiverEquals("1.1", result);
        TestHelper.assertSenderEquals("1.1", result);
        TestHelper.assertAckMsgIdEquals(msg.getId(), result);
    }

    /**
     * Test of processPatientDiscoveryAsyncReq method, of class EntityPatientDiscoveryAsyncRequestOrchImpl.
     */
    @Test
    public void testProcessPatientDiscoveryAsyncReqPolicyFailed() {
        System.out.println("testProcessPatientDiscoveryAsyncReqPolicyFailed");

        EntityPatientDiscoveryDeferredRequestOrchImpl instance = new EntityPatientDiscoveryDeferredRequestOrchImpl() {

            @Override
            protected List<UrlInfo> getTargetEndpoints(NhinTargetCommunitiesType targetCommunities) {
                List<UrlInfo> urlInfo = new ArrayList<UrlInfo>();

                UrlInfo target = new UrlInfo();
                target.setHcid("2.2");
                target.setUrl("https://test.com:8181/NhinPatientDiscoveryAsync");
                urlInfo.add(target);

                return urlInfo;
            }

            @Override
            protected boolean checkPolicy(RespondingGatewayPRPAIN201305UV02RequestType request) {
                return false;
            }

            @Override
            protected MCCIIN000002UV01 sendToProxy(PRPAIN201305UV02 request, AssertionType newAssertion, UrlInfo urlInfo) {
                return HL7AckTransforms.createAckFrom201305(request, "Success");
            }

            @Override
            protected PatientDiscoveryAuditor createAuditLogger() {
                PatientDiscoveryAuditor auditLogger = new PatientDiscoveryAuditLogger() {

                    @Override
                    public AcknowledgementType auditEntity201305(RespondingGatewayPRPAIN201305UV02RequestType request,
                            AssertionType assertion, String direction) {
                        return new AcknowledgementType();
                    }

                    @Override
                    public AcknowledgementType auditAck(MCCIIN000002UV01 request, AssertionType assertion,
                            String direction, String _interface) {
                        return new AcknowledgementType();
                    }
                };
                return auditLogger;
            }

            @Override
            protected AsyncMessageProcessHelper createAsyncProcesser() {
                AsyncMessageProcessHelper processHelper = new AsyncMessageProcessHelper() {

                    @Override
                    public boolean addPatientDiscoveryRequest(RespondingGatewayPRPAIN201305UV02RequestType request,
                            String direction) {
                        return true;
                    }

                    @Override
                    public boolean processAck(String messageId, String newStatus, String errorStatus,
                            MCCIIN000002UV01 ack) {
                        return true;
                    }
                };
                return processHelper;
            }

            @Override
            protected PatientDiscovery201305Processor getPatientDiscovery201305Processor() {
                PatientDiscovery201305Processor processHelper = new PatientDiscovery201305Processor() {

                    @Override
                    public void storeLocalMapping(RespondingGatewayPRPAIN201305UV02RequestType request) {
                        return;
                    }
                };
                return processHelper;
            }

            @Override
            protected PDDeferredCorrelationDao getPDDeferredCorrelationDao() {
                PDDeferredCorrelationDao pdCorrelationDao = new PDDeferredCorrelationDao() {

                    @Override
                    public void saveOrUpdate(String messageId, II patientId) {
                        return;
                    }
                };
                return pdCorrelationDao;
            }
        };

        AssertionType assertion = new AssertionType();
        NhinTargetCommunitiesType targets = new NhinTargetCommunitiesType();

        JAXBElement<PRPAMT201301UV02Person> person = HL7PatientTransforms.create201301PatientPerson("Joe", "Smith",
                "M", null, null);
        PRPAMT201301UV02Patient patient = HL7PatientTransforms.create201301Patient(person, "1234", "1.1.1");
        PRPAIN201305UV02 msg = HL7PRPA201305Transforms.createPRPA201305(patient, "1.1", "2.2", "1.1.1");

        MCCIIN000002UV01 result = instance.processPatientDiscoveryAsyncReq(msg, assertion, targets);

        assertNotNull(result);
        TestHelper.assertAckMsgEquals("Policy Failed", result);
        TestHelper.assertReceiverEquals("1.1", result);
        TestHelper.assertSenderEquals("1.1", result);
        TestHelper.assertAckMsgIdEquals(msg.getId(), result);
    }
    
    /**
     * Tests that the outbound audit logger is created and called.
     */
    @Test
    public void testAuditRequestFromAdapter(){
    	EntityPatientDiscoveryDeferredRequestOrchImpl instance = new EntityPatientDiscoveryDeferredRequestOrchImpl() {

            @Override
            protected PatientDiscoveryAuditor createAuditLogger() {
                PatientDiscoveryAuditor auditLogger = new PatientDiscoveryAuditLogger() {

                    @Override
                    public AcknowledgementType auditEntityDeferred201305(RespondingGatewayPRPAIN201305UV02RequestType request,
                            AssertionType assertion, String direction, String _process) {
                    	assert(direction.equalsIgnoreCase(NhincConstants.AUDIT_LOG_INBOUND_DIRECTION));
                        assert(_process.equalsIgnoreCase(NhincConstants.AUDIT_LOG_REQUEST_PROCESS));
                    	return new AcknowledgementType();
                    }

                };
                return auditLogger;
            }
        };
        
        instance.auditRequestFromAdapter(new RespondingGatewayPRPAIN201305UV02RequestType(), new AssertionType());
    }
    
    /**
     * Tests that the response audit logger is created and called.
     */
    @Test
    public void testAuditResponseToAdapter(){
    	EntityPatientDiscoveryDeferredRequestOrchImpl instance = new EntityPatientDiscoveryDeferredRequestOrchImpl() {

            @Override
            protected PatientDiscoveryAuditor createAuditLogger() {
                PatientDiscoveryAuditor auditLogger = new PatientDiscoveryAuditLogger() {

                    @Override
                    public AcknowledgementType auditAck(MCCIIN000002UV01 request, AssertionType assertion,
                            String direction, String _interface) {
                        assert(direction.equalsIgnoreCase(NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION));
                        assert(_interface.equalsIgnoreCase(NhincConstants.AUDIT_LOG_ENTITY_INTERFACE));
                    	return new AcknowledgementType();
                    }
                };
                return auditLogger;
            }
        };
        
        instance.auditResponseToAdapter(new MCCIIN000002UV01(), new AssertionType());
    }
    
    /**
     * Test getTargetEndpoints
     */
    @Test
    public void testGetTargetEndpoints(){
    	final ConnectionManagerCache mockManagerCache = context.mock(ConnectionManagerCache.class);
    	
    	EntityPatientDiscoveryDeferredRequestOrchImpl instance 
    		= new EntityPatientDiscoveryDeferredRequestOrchImpl() {
    		@Override 
    		protected ConnectionManagerCache getConnectionManagerCache(){
    			return mockManagerCache;
    		}
    	};
    	try {
			context.checking(new Expectations() {
			    {
			        allowing(mockManagerCache).getEndpointURLFromNhinTargetCommunities(
			        		with(any(NhinTargetCommunitiesType.class)),with(any(String.class)));
			        will(returnValue(null));
			    }
			});
		} catch (ConnectionManagerException e) {
			fail("ConnectionManagerException caught");
		}
    	
    	NhinTargetCommunitiesType targets = new NhinTargetCommunitiesType();
    	NhinTargetCommunityType target = new NhinTargetCommunityType();
    	HomeCommunityType community = new HomeCommunityType();
    	community.setHomeCommunityId("1.1");
    	target.setHomeCommunity(community);
    	
    	List<UrlInfo> list = instance.getTargetEndpoints(targets);

        context.assertIsSatisfied();
    	
    }
}
