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
package gov.hhs.fha.nhinc.transform.subdisc;

import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.v3.*;

/**
 * 
 * @author Jon Hoppesch
 */
public class HL7MessageIdGenerator {

    private static Log log = LogFactory.getLog(HL7MessageIdGenerator.class);
    private static final String PROPERTY_FILE = "adapter";
    private static final String PROPERTY_NAME = "assigningAuthorityId";

    /**
     * Generate the messageId based on the passed device id. The device id is the corresponding assigning authority id.
     * 
     * @param myDeviceId
     * @return messageId
     */
    public static II GenerateHL7MessageId(String myDeviceId) {
        II messageId = new II();

        if (NullChecker.isNullish(myDeviceId)) {
            myDeviceId = getDefaultLocalDeviceId();
        }

        log.debug("Using local device id " + myDeviceId);
        messageId.setRoot(myDeviceId);
        messageId.setExtension(GenerateMessageId());
        return messageId;
    }

    /**
     * Generate the messageId based on the default device id. The device id is the assigning authority id from the
     * adapter.properties configuration file.
     * 
     * @return messageId
     */
    public static II GenerateHL7MessageId() {
        String deviceId = getDefaultLocalDeviceId();

        return GenerateHL7MessageId(deviceId);
    }

    private static String getDefaultLocalDeviceId() {
        String defaultLocalId = "";

        try {
            defaultLocalId = PropertyAccessor.getInstance().getProperty(PROPERTY_FILE, PROPERTY_NAME);
        } catch (PropertyAccessException e) {
            log.error(
                    "PropertyAccessException - Default Assigning Authority property not defined in adapter.properties",
                    e);
        }

        return defaultLocalId;
    }

    /**
     * Generate the messageId from a new UID.
     * 
     * @return messageId
     */
    public static String GenerateMessageId() {
        java.rmi.server.UID uid = new java.rmi.server.UID();
        log.debug("generated message id=" + uid.toString());
        return uid.toString();
    }
}
