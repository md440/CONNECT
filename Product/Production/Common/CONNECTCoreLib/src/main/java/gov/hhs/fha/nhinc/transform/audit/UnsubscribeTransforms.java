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
package gov.hhs.fha.nhinc.transform.audit;

import com.services.nhinc.schema.auditmessage.AuditMessageType;
import com.services.nhinc.schema.auditmessage.AuditSourceIdentificationType;
import com.services.nhinc.schema.auditmessage.CodedValueType;
import com.services.nhinc.schema.auditmessage.ParticipantObjectIdentificationType;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gov.hhs.fha.nhinc.common.hiemauditlog.LogNhinUnsubscribeRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.UserType;
import gov.hhs.fha.nhinc.transform.marshallers.JAXBContextHandler;
import java.io.ByteArrayOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

/**
 * Transforms for unsubscribe messages
 * 
 * @author Neil Webb
 */
public class UnsubscribeTransforms {
    private static Log log = LogFactory.getLog(SubscribeTransforms.class);

    public LogEventRequestType transformNhinUnsubscribeRequestToAuditMessage(LogNhinUnsubscribeRequestType message) {
        LogEventRequestType response = new LogEventRequestType();
        AuditMessageType auditMsg = new AuditMessageType();
        response.setDirection(message.getDirection());
        response.setInterface(message.getInterface());

        log.info("******************************************************************");
        log.info("Entering transformNhinUnsubscribeRequestToAuditMessage() method.");
        log.info("******************************************************************");

        // Extract UserInfo from Message.Assertion
        UserType userInfo = new UserType();
        if (message != null && message.getMessage() != null && message.getMessage().getAssertion() != null
                && message.getMessage().getAssertion().getUserInfo() != null) {
            userInfo = message.getMessage().getAssertion().getUserInfo();
        }

        // Create EventIdentification
        CodedValueType eventID = new CodedValueType();
        eventID = AuditDataTransformHelper.createEventId(AuditDataTransformConstants.EVENT_ID_CODE_SYS_NAME_SUB,
                AuditDataTransformConstants.EVENT_ID_DISPLAY_NAME_SUBSCRIBE,
                AuditDataTransformConstants.EVENT_ID_CODE_SYS_NAME_SUB,
                AuditDataTransformConstants.EVENT_ID_DISPLAY_NAME_SUBSCRIBE);
        auditMsg.setEventIdentification(AuditDataTransformHelper.createEventIdentification(
                AuditDataTransformConstants.EVENT_ACTION_CODE_CREATE,
                AuditDataTransformConstants.EVENT_OUTCOME_INDICATOR_SUCCESS, eventID));

        // Create Active Participant Section
        if (userInfo != null) {
            AuditMessageType.ActiveParticipant participant = AuditDataTransformHelper.createActiveParticipantFromUser(
                    userInfo, true);
            auditMsg.getActiveParticipant().add(participant);
        }

        /* Assign AuditSourceIdentification */
        String communityId = "";
        String communityName = "";
        String patientId = "";

        // An unsubscribe message does not contain a patient identifier.

        if (userInfo != null && userInfo.getOrg() != null) {
            if (userInfo.getOrg().getHomeCommunityId() != null) {
                communityId = userInfo.getOrg().getHomeCommunityId();
            }
            if (userInfo.getOrg().getName() != null) {
                communityName = userInfo.getOrg().getName();
            }
        }

        AuditSourceIdentificationType auditSource = AuditDataTransformHelper.createAuditSourceIdentification(
                communityId, communityName);
        auditMsg.getAuditSourceIdentification().add(auditSource);

        /* Assign ParticipationObjectIdentification */
        ParticipantObjectIdentificationType participantObject = AuditDataTransformHelper
                .createParticipantObjectIdentification(patientId);

        // Fill in the message field with the contents of the event message
        try {
            JAXBContextHandler oHandler = new JAXBContextHandler();
            JAXBContext jc = oHandler.getJAXBContext("gov.hhs.fha.nhinc.common.subscription");
            Marshaller marshaller = jc.createMarshaller();
            ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
            baOutStrm.reset();
            gov.hhs.fha.nhinc.common.subscription.ObjectFactory factory = new gov.hhs.fha.nhinc.common.subscription.ObjectFactory();
            JAXBElement oJaxbElement = factory.createUnsubscribe(message.getMessage().getUnsubscribe());
            baOutStrm.close();
            marshaller.marshal(oJaxbElement, baOutStrm);
            log.debug("Done marshalling the message.");

            participantObject.setParticipantObjectQuery(baOutStrm.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            log.error("EXCEPTION when marshalling unsubscribe request: " + e);
            throw new RuntimeException();
        }
        auditMsg.getParticipantObjectIdentification().add(participantObject);

        response.setAuditMessage(auditMsg);

        log.info("******************************************************************");
        log.info("Exiting transformNhinUnsubscribeRequestToAuditMessage() method.");
        log.info("******************************************************************");

        return response;
    }

    public LogEventRequestType transformUnsubscribeResponseToGenericAudit(
            gov.hhs.fha.nhinc.common.hiemauditlog.LogUnsubscribeResponseType message) {
        LogEventRequestType response = new LogEventRequestType();
        AuditMessageType auditMsg = new AuditMessageType();
        response.setDirection(message.getDirection());
        response.setInterface(message.getInterface());

        log.info("******************************************************************");
        log.info("Entering transformUnsubscribeResponseToGenericAudit() method.");
        log.info("******************************************************************");

        // Extract UserInfo from Message.Assertion
        UserType userInfo = new UserType();
        if (message != null && message.getMessage() != null && message.getMessage().getAssertion() != null
                && message.getMessage().getAssertion().getUserInfo() != null) {
            userInfo = message.getMessage().getAssertion().getUserInfo();
        }

        // Create EventIdentification
        CodedValueType eventID = new CodedValueType();
        eventID = AuditDataTransformHelper.createEventId(AuditDataTransformConstants.EVENT_ID_CODE_SYS_NAME_SUB,
                AuditDataTransformConstants.EVENT_ID_DISPLAY_NAME_SUBSCRIBE,
                AuditDataTransformConstants.EVENT_ID_CODE_SYS_NAME_SUB,
                AuditDataTransformConstants.EVENT_ID_DISPLAY_NAME_SUBSCRIBE);
        auditMsg.setEventIdentification(AuditDataTransformHelper.createEventIdentification(
                AuditDataTransformConstants.EVENT_ACTION_CODE_CREATE,
                AuditDataTransformConstants.EVENT_OUTCOME_INDICATOR_SUCCESS, eventID));

        // Create Active Participant Section
        if (userInfo != null) {
            AuditMessageType.ActiveParticipant participant = AuditDataTransformHelper.createActiveParticipantFromUser(
                    userInfo, true);
            auditMsg.getActiveParticipant().add(participant);
        }

        /* Assign AuditSourceIdentification */
        String communityId = "";
        String communityName = "";
        String patientId = "";

        // An unsubscribe response message does not contain a patient identifier.

        if (userInfo != null && userInfo.getOrg() != null) {
            if (userInfo.getOrg().getHomeCommunityId() != null) {
                communityId = userInfo.getOrg().getHomeCommunityId();
            }
            if (userInfo.getOrg().getName() != null) {
                communityName = userInfo.getOrg().getName();
            }
        }

        AuditSourceIdentificationType auditSource = AuditDataTransformHelper.createAuditSourceIdentification(
                communityId, communityName);
        auditMsg.getAuditSourceIdentification().add(auditSource);

        /* Assign ParticipationObjectIdentification */
        ParticipantObjectIdentificationType participantObject = AuditDataTransformHelper
                .createParticipantObjectIdentification(patientId);

        // Fill in the message field with the contents of the event message
        try { // org.oasis_open.docs.wsn.b_2
            JAXBContextHandler oHandler = new JAXBContextHandler();
            JAXBContext jc = oHandler.getJAXBContext("gov.hhs.fha.nhinc.common.subscription");
            Marshaller marshaller = jc.createMarshaller();
            ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
            baOutStrm.reset();
            gov.hhs.fha.nhinc.common.subscription.ObjectFactory factory = new gov.hhs.fha.nhinc.common.subscription.ObjectFactory();
            marshaller.marshal(message.getMessage().getUnsubscribeResponse(), baOutStrm);
            log.debug("Done marshalling the message.");

            participantObject.setParticipantObjectQuery(baOutStrm.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            log.error("EXCEPTION when marshalling unsubscribe response: " + e);
            throw new RuntimeException();
        }
        auditMsg.getParticipantObjectIdentification().add(participantObject);

        response.setAuditMessage(auditMsg);

        log.info("******************************************************************");
        log.info("Exiting transformUnsubscribeResponseToGenericAudit() method.");
        log.info("******************************************************************");

        return response;
    }

}
