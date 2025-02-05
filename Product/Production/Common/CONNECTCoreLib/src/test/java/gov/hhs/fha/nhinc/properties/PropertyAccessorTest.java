/**
 * 
 */

/**
*Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
*All rights reserved.
*
*Redistribution and use in source and binary forms, with or without
*modification, are permitted provided that the following conditions are met:
*    * Redistributions of source code must retain the above
*      copyright notice, this list of conditions and the following disclaimer.
*    * Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the documentation
*      and/or other materials provided with the distribution.
*    * Neither the name of the United States Government nor the
*      names of its contributors may be used to endorse or promote products
*      derived from this software without specific prior written permission.
*
*THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
*ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
*WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
*DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
*(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
*LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
*(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
*SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package gov.hhs.fha.nhinc.properties;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author akong
 *
 */
public class PropertyAccessorTest {

    private static final String PROPERTY_FILE_NAME = "mock";
    private static final String PROPERTY_FILE_LOCATION = "/config/";
    private static final String PROPERTY_FILE_LOCATION_WITH_FILE = "/config/mock.properties";
    private static final String PROPERTY_NAME = "propertyName";
    private static final String PROPERTY_VALUE_STRING = "propertyValueString";
    private static final boolean PROPERTY_VALUE_BOOLEAN = true;
    private static final long PROPERTY_VALUE_LONG = 10;    
    private static final int REFRESH_DURATION = 1000;
        
    protected Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    final Log mockLog = context.mock(Log.class);
    final PropertyFileDAO mockFileDAO = context.mock(PropertyFileDAO.class);
    final PropertyFileRefreshHandler mockRefreshHandler = context.mock(PropertyFileRefreshHandler.class);
    
    @Before
    public void setMockFileDAOExpectations() throws PropertyAccessException {
        context.checking(new Expectations() {
            {
                allowing(mockFileDAO).getProperty(with(any(String.class)), with(any(String.class)));
                will(returnValue(PROPERTY_VALUE_STRING));
                
                allowing(mockFileDAO).getPropertyBoolean(with(any(String.class)), with(any(String.class)));
                will(returnValue(PROPERTY_VALUE_BOOLEAN));
                
                allowing(mockFileDAO).getPropertyLong(with(any(String.class)), with(any(String.class)));
                will(returnValue(PROPERTY_VALUE_LONG));
                
                allowing(mockFileDAO).getPropertyNames(with(any(String.class)));
                will(returnValue(returnPropertyNamesSet()));
                
                allowing(mockFileDAO).getProperties(with(any(String.class)));
                will(returnValue(returnProperties()));
                
                allowing(mockFileDAO).loadPropertyFile(with(any(File.class)), with(any(String.class)));
                
                allowing(mockFileDAO).printToLog(with(any(String.class)));
            }
        });
    }
       
    @Before
    public void setMockRefreshHandlerExpectations() throws PropertyAccessException {
        context.checking(new Expectations() {
            {
                allowing(mockRefreshHandler).getRefreshDuration(with(any(String.class)));
                will(returnValue(REFRESH_DURATION));
                
                allowing(mockRefreshHandler).needsRefresh(with(any(String.class)));
                will(returnValue(true));
                
                allowing(mockRefreshHandler).addRefreshInfo(with(any(String.class)), with(any(String.class)));
                
                allowing(mockRefreshHandler).getDurationBeforeNextRefresh(with(any(String.class)));
                will(returnValue(REFRESH_DURATION));
                
                allowing(mockRefreshHandler).printToLog(with(any(String.class)));
            }
        });
    }
    
    @Before
    public void setMockLogExpectationsToIgnoreAllLogs() {     
        context.checking(new Expectations() {
            {
                ignoring(mockLog);
            }
        });
    }
    
    @Test
    public void testgetInstance() {
        PropertyAccessor propAccessor = PropertyAccessor.getInstance();
        assertNotNull(propAccessor);
        
        propAccessor = PropertyAccessor.getInstance(PROPERTY_FILE_NAME);
        assertNotNull(propAccessor);        
    }
    
    @Test
    public void testGetProperty() throws PropertyAccessException {
        PropertyAccessor propAccessor = createPropertyAccessor();
        
        String propertyValue = propAccessor.getProperty(PROPERTY_NAME);
        assertEquals(PROPERTY_VALUE_STRING, propertyValue);
        
        propertyValue = propAccessor.getProperty(PROPERTY_FILE_NAME, PROPERTY_NAME);
        assertEquals(PROPERTY_VALUE_STRING, propertyValue);
    }
    
    @Test
    public void testGetPropertyBoolean() throws PropertyAccessException {
        PropertyAccessor propAccessor = createPropertyAccessor();
        
        boolean propertyValue = propAccessor.getPropertyBoolean(PROPERTY_NAME);
        assertEquals(PROPERTY_VALUE_BOOLEAN, propertyValue);        
    }
    
    @Test
    public void testGetPropertyLong() throws PropertyAccessException {
        PropertyAccessor propAccessor = createPropertyAccessor();
        
        long propertyValue = propAccessor.getPropertyLong(PROPERTY_FILE_NAME, PROPERTY_NAME);
        assertEquals(PROPERTY_VALUE_LONG, propertyValue);        
    }
        
    @Test
    public void testGetPropertyNames() throws PropertyAccessException {
        PropertyAccessor propAccessor = createPropertyAccessor();
        
        Set<String> propertyNamesSet = propAccessor.getPropertyNames(PROPERTY_FILE_NAME);
        assertEquals(1, propertyNamesSet.size());
    }
    
    @Test
    public void testGetProperties() throws PropertyAccessException {
        PropertyAccessor propAccessor = createPropertyAccessor();
        
        Properties properties = propAccessor.getProperties(PROPERTY_FILE_NAME);       
        assertEquals(PROPERTY_VALUE_STRING, properties.getProperty(PROPERTY_NAME));
    }
    
    @Test
    public void testGetRefreshDuration() throws PropertyAccessException {
        PropertyAccessor propAccessor = createPropertyAccessor();
        
        int refreshDuration = propAccessor.getRefreshDuration(PROPERTY_FILE_NAME);
        assertEquals(REFRESH_DURATION, refreshDuration);
    }
    
    @Test
    public void testGetDurationBeforeNextRefresh() throws PropertyAccessException {
        PropertyAccessor propAccessor = createPropertyAccessor();
        
        int nextRefresh = propAccessor.getDurationBeforeNextRefresh(PROPERTY_FILE_NAME);
        assertEquals(REFRESH_DURATION, nextRefresh);
    }
    
    @Test(expected=PropertyAccessException.class)
    public void testValidateInput() throws PropertyAccessException {
        PropertyAccessor propAccessor = createPropertyAccessor();
        
        propAccessor.forceRefresh(PROPERTY_FILE_NAME);
        propAccessor.forceRefresh(null);
    }
    
    @Test
    public void testGetPropertyFileLocation() throws PropertyAccessException {
        PropertyAccessor propAccessor = createPropertyAccessor();
        
        propAccessor.setPropertyFileLocation(PROPERTY_FILE_LOCATION);
        String fileLocation = propAccessor.getPropertyFileLocation(PROPERTY_FILE_NAME);
              
        assertEquals(PROPERTY_FILE_LOCATION_WITH_FILE, fileLocation);
    }
    
    @Test
    public void testDumpPropsToLog() throws PropertyAccessException {
        PropertyAccessor propAccessor = createPropertyAccessor();
        
        propAccessor.dumpPropsToLog(PROPERTY_FILE_LOCATION);
    }
        
    private PropertyAccessor createPropertyAccessor() {
        PropertyAccessor propAccessor = new PropertyAccessor() {
            protected PropertyFileDAO createPropertyFileDAO() {
                return mockFileDAO;
            }
            
            protected PropertyFileRefreshHandler createPropertyFileRefreshHandler() {
                return mockRefreshHandler;
            }
            
            protected PropertyAccessorFileUtilities createPropertyAccessorFileUtilities() {
                return new PropertyAccessorFileUtilities() {
                    public String getPropertyFileLocation(String propertyFileName) {
                        return PROPERTY_FILE_LOCATION_WITH_FILE;
                    }
                    
                    protected Log getLogger() {
                        return mockLog;
                    }
                };
            }
        };
        propAccessor.setPropertyFile(PROPERTY_FILE_NAME);
        
        return propAccessor;
    }
       
    private Set<String> returnPropertyNamesSet() {
        Set<String> propertyNamesSet = new HashSet<String>();
        propertyNamesSet.add(PROPERTY_NAME);
        
        return propertyNamesSet;
    }
    
    private Properties returnProperties() {
        Properties properties = new Properties();
        properties.put(PROPERTY_NAME, PROPERTY_VALUE_STRING);
        
        return properties;
    }
}
