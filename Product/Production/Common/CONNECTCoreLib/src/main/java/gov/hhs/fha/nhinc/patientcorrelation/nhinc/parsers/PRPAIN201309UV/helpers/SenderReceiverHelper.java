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
package gov.hhs.fha.nhinc.patientcorrelation.nhinc.parsers.PRPAIN201309UV.helpers;

import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import org.hl7.v3.CommunicationFunctionType;
import org.hl7.v3.II;
import org.hl7.v3.MCCIMT000100UV01Device;
import org.hl7.v3.MCCIMT000100UV01Receiver;
import org.hl7.v3.MCCIMT000100UV01Sender;
import org.hl7.v3.TELExplicit;

/**
 * 
 * @author rayj
 */
public class SenderReceiverHelper {

    public static MCCIMT000100UV01Sender CreateSender(String senderDeviceId) {
        MCCIMT000100UV01Sender sender = new MCCIMT000100UV01Sender();
        sender.setTypeCode(CommunicationFunctionType.SND);
        sender.setDevice(createDevice(senderDeviceId));
        return sender;
    }

    public static MCCIMT000100UV01Sender CreateSender() {
        return CreateSender(null);
    }

    public static MCCIMT000100UV01Receiver CreateReceiver(String receiverDeviceId) {
        MCCIMT000100UV01Receiver receiver = new MCCIMT000100UV01Receiver();
        receiver.setTypeCode(CommunicationFunctionType.RCV);
        receiver.setDevice(createDevice(receiverDeviceId));
        return receiver;
    }

    public static MCCIMT000100UV01Receiver CreateReceiver() {
        return CreateReceiver(null);
    }

    private static MCCIMT000100UV01Device createDevice(String deviceIdRoot) {

        MCCIMT000100UV01Device device = new MCCIMT000100UV01Device();
        device.setDeterminerCode(Constants.determinerCodeValue);
        II deviceId = null;

        if (NullChecker.isNullish(deviceIdRoot)) {
            deviceId = IIHelper.IIFactoryCreateNull();
        } else {
            deviceId = IIHelper.IIFactory(deviceIdRoot, null);
        }
        device.getId().add(deviceId);

        // TELExplicit tel = new TELExplicit();
        // tel.getNullFlavor().add("NA");
        // device.getTelecom().add(tel);

        return device;
    }
}
