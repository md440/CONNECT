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
package gov.hhs.fha.nhinc.admindistribution.entity;

import gov.hhs.fha.nhinc.admindistribution.nhin.proxy.NhinAdminDistributionProxy;
import gov.hhs.fha.nhinc.admindistribution.nhin.proxy.NhinAdminDistributionProxyObjectFactory;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.orchestration.Orchestratable;
import gov.hhs.fha.nhinc.orchestration.OrchestrationStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author nnguyen
 */
public class OutboundAdminDistributionStrategyImpl_g0 implements OrchestrationStrategy {

    private static Log log = LogFactory.getLog(OutboundAdminDistributionStrategyImpl_g0.class);

    public OutboundAdminDistributionStrategyImpl_g0() {
    }

    private Log getLogger() {
        return log;
    }

    @Override
    public void execute(Orchestratable message) {
        if (message instanceof OutboundAdminDistributionOrchestratable) {
            execute((OutboundAdminDistributionOrchestratable) message);
        } else {
            getLogger().error("Not an EntityAdminDistributionOrchestratable.");
        }
    }

    public void execute(OutboundAdminDistributionOrchestratable message) {
        getLogger().debug("Begin NhinAdminDistributionOrchestratableImpl_g0.process");
        if (message == null) {
            getLogger().debug("EntityAdminDistributionOrchestratable was null");
            return;
        }

        if (message instanceof OutboundAdminDistributionOrchestratable) {

            NhinAdminDistributionProxy nhincAdminDist = new NhinAdminDistributionProxyObjectFactory()
                    .getNhinAdminDistProxy();
            nhincAdminDist.sendAlertMessage(message.getRequest().getEDXLDistribution(), message.getRequest()
                    .getAssertion(), message.getTarget(), NhincConstants.GATEWAY_API_LEVEL.LEVEL_g0);
        } else {
            getLogger().error(
                    "NhinAdminDistributionImpl_g0 AdapterDelegateImpl.process received a message "
                            + "which was not of type NhinAdminDistributionOrchestratableImpl_g0.");
        }
        getLogger().debug("End NhinAdminDistributionOrchestratableImpl_g0.process");
    }
}
