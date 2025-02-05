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
package gov.hhs.fha.nhinc.connectmgr.persistance.dao;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uddi.api_v3.BusinessDetail;
import org.uddi.api_v3.ObjectFactory;

/**
 * 
 * @author mweaver
 */
public class ConnectionManagerDAOBase {

    private static final String CONTEXT_PATH = "org.uddi.api_v3";
    private Log log = null;

    protected BusinessDetail loadBusinessDetail(File file) throws JAXBException {
        BusinessDetail resp = null;
        synchronized (file) {
            JAXBContext context = JAXBContext.newInstance(CONTEXT_PATH);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            resp = ((JAXBElement<BusinessDetail>) unmarshaller.unmarshal(file)).getValue();
        }
        return resp;
    }

    protected void saveBusinessDetail(BusinessDetail BusinessDetail, File file) {
        try {
            synchronized (file) {
                JAXBContext context = JAXBContext.newInstance(CONTEXT_PATH);
                ObjectFactory factory = new ObjectFactory();
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(factory.createBusinessDetail(BusinessDetail), file);
            }
        } catch (JAXBException ex) {
            throw new RuntimeException("Unable to save to Connection Information File " + file.getName(), ex);
        }
        getLogger().info("Connection info saved to " + file.getName());
    }

    protected Log getLogger() {
        if (log == null) {
            setLogger(LogFactory.getLog(this.getClass()));
        }
        return log;
    }

    protected void setLogger(Log log) {
        this.log = log;
    }
}
