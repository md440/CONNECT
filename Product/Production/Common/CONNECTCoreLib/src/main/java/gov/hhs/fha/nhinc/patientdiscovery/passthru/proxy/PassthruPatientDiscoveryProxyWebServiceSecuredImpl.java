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
package gov.hhs.fha.nhinc.patientdiscovery.passthru.proxy;

import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;

import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;
import gov.hhs.fha.nhinc.nhincproxypatientdiscoverysecured.NhincProxyPatientDiscoverySecuredPortType;
import gov.hhs.fha.nhinc.transform.subdisc.HL7PRPA201306Transforms;
import org.hl7.v3.ProxyPRPAIN201305UVProxySecuredRequestType;

/**
 * 
 * @author mflynn02
 */
public class PassthruPatientDiscoveryProxyWebServiceSecuredImpl implements PassthruPatientDiscoveryProxy {
    private static Service cachedService = null;
    private static final String NAMESPACE_URI = "urn:gov:hhs:fha:nhinc:nhincproxypatientdiscoverysecured";
    private static final String SERVICE_LOCAL_PART = "NhincProxyPatientDiscoverySecured";
    private static final String PORT_LOCAL_PART = "NhincProxyPatientDiscoverySecuredPort";
    private static final String WSDL_FILE = "NhincProxyPatientDiscoverySecured.wsdl";
    private static final String WS_ADDRESSING_ACTION = "urn:gov:hhs:fha:nhinc:nhincproxypatientdiscoverysecured:Proxy_PRPA_IN201305UVProxyRequestMessage";
    private static Log log = null;
    private WebServiceProxyHelper oProxyHelper = new WebServiceProxyHelper();

    public PassthruPatientDiscoveryProxyWebServiceSecuredImpl() {
        log = createLogger();
    }

    protected Log createLogger() {
        return LogFactory.getLog(getClass());
    }

    public PRPAIN201306UV02 PRPAIN201305UV(PRPAIN201305UV02 body, AssertionType assertion, NhinTargetSystemType target)
            throws Exception {
        String url = null;
        PRPAIN201306UV02 response = new PRPAIN201306UV02();
        ProxyPRPAIN201305UVProxySecuredRequestType secureRequest = new ProxyPRPAIN201305UVProxySecuredRequestType();
        secureRequest.setPRPAIN201305UV02(body);
        secureRequest.setNhinTargetSystem(target);
        try {
            if (body != null) {
                log.debug("Before target system URL look up.");
                log.error("* * * * * * * * target.getUrl is : " + target.getUrl() + "* * * * * * * *");
                url = oProxyHelper
                        .getUrlLocalHomeCommunity(NhincConstants.NHINC_PASSTHRU_PATIENT_DISCOVERY_SECURED_SERVICE_NAME);
                log.debug("After target system URL look up. URL for service: "
                        + NhincConstants.NHINC_PASSTHRU_PATIENT_DISCOVERY_SECURED_SERVICE_NAME + " is: " + url);

                if (NullChecker.isNotNullish(url)) {
                    NhincProxyPatientDiscoverySecuredPortType port = getPort(url,
                            NhincConstants.NHINC_PASSTHRU_PATIENT_DISCOVERY_SECURED_SERVICE_NAME, WS_ADDRESSING_ACTION,
                            assertion);
                    response = (PRPAIN201306UV02) oProxyHelper.invokePort(port,
                            NhincProxyPatientDiscoverySecuredPortType.class, "proxyPRPAIN201305UV", secureRequest);
                } else {
                    log.error("Failed to call the web service ("
                            + NhincConstants.NHINC_PASSTHRU_PATIENT_DISCOVERY_SECURED_SERVICE_NAME
                            + ").  The URL is null.");
                }
            } else {
                log.error("Failed to call the web service ("
                        + NhincConstants.NHINC_PASSTHRU_PATIENT_DISCOVERY_SECURED_SERVICE_NAME
                        + ").  The input parameter is null.");
            }
        } catch (Exception e) {
            log.error("Failed to call the web service ("
                    + NhincConstants.NHINC_PASSTHRU_PATIENT_DISCOVERY_SECURED_SERVICE_NAME
                    + ").  An unexpected exception occurred.  " + "Exception: " + e.getMessage(), e);
            // response = new HL7PRPA201306Transforms().createPRPA201306ForErrors(secureRequest.getPRPAIN201305UV02(),
            // NhincConstants.PATIENT_DISCOVERY_ANSWER_NOT_AVAIL_ERR_CODE);
            throw e;

        }

        return response;
    }

    /**
     * Retrieve the service class for this web service.
     * 
     * @return The service class for this web service.
     */
    protected Service getService() {
        if (cachedService == null) {
            try {
                cachedService = oProxyHelper.createService(WSDL_FILE, NAMESPACE_URI, SERVICE_LOCAL_PART);
            } catch (Throwable t) {
                log.error("Error creating service: " + t.getMessage(), t);
            }
        }
        return cachedService;
    }

    /**
     * This method retrieves and initializes the port.
     * 
     * @param url The URL for the web service.
     * @param serviceAction The action for the web service.
     * @param wsAddressingAction The action assigned to the input parameter for the web service operation.
     * @param assertion The assertion information for the web service
     * @return The port object for the web service.
     */
    protected NhincProxyPatientDiscoverySecuredPortType getPort(String url, String serviceAction,
            String wsAddressingAction, AssertionType assertion) {
        NhincProxyPatientDiscoverySecuredPortType port = null;
        Service service = getService();
        if (service != null) {
            log.debug("Obtained service - creating port.");

            port = service.getPort(new QName(NAMESPACE_URI, PORT_LOCAL_PART),
                    NhincProxyPatientDiscoverySecuredPortType.class);
            oProxyHelper.initializeSecurePort((javax.xml.ws.BindingProvider) port, url, serviceAction,
                    wsAddressingAction, assertion);
        } else {
            log.error("Unable to obtain serivce - no port created.");
        }
        return port;
    }

}