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
package gov.hhs.fha.nhinc.docquery.entity;

import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.orchestration.OrchestrationContextBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Returns the OrchestrationContextBuilder appropriate for the particular apiLevel
 * 
 * @author paul.eftis
 */
public class OutboundDocQueryFactory {

    private static Log log = LogFactory.getLog(OutboundDocQueryFactory.class);

    private static OutboundDocQueryFactory INSTANCE = new OutboundDocQueryFactory();

    private OutboundDocQueryFactory() {
    }

    public OrchestrationContextBuilder createOrchestrationContextBuilder(NhincConstants.GATEWAY_API_LEVEL apiLevel) {
        log.debug("EntityDocQueryFactory has apiLevel=" + apiLevel.toString());
        switch (apiLevel) {
        case LEVEL_g0:
            return new OutboundDocQueryOrchestrationContextBuilder_g0();
        case LEVEL_g1:
            return new OutboundDocQueryOrchestrationContextBuilder_g1();
        default:
            return new OutboundDocQueryOrchestrationContextBuilder_g1();
        }
    }

    public static OutboundDocQueryFactory getInstance() {
        return INSTANCE;
    }
}
