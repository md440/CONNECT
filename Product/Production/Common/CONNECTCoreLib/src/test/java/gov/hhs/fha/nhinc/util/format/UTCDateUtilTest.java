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
package gov.hhs.fha.nhinc.util.format;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.apache.commons.logging.Log;
import org.junit.runner.RunWith;

/**
 * Test cases for the UTCDateUtil Class
 * 
 * @author Neil Webb
 */
@RunWith(JMock.class)
public class UTCDateUtilTest {
    Mockery context = new JUnit4Mockery();

    @Test
    public void testParseUTCDateStringWithAMHour() {
        try {
            // Create mock objects
            final Log mockLog = context.mock(Log.class);

            // Create SUT instance
            UTCDateUtil utcDateUtil = new UTCDateUtil() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }
            };

            // Set Expectations
            context.checking(new Expectations() {
                {
                    allowing(mockLog).debug(with(any(String.class)));
                    allowing(mockLog).isDebugEnabled();
                }
            });

            // Exercise test
            String dateString = "19650123010334";
            Date parsedDate = utcDateUtil.parseUTCDateOptionalTimeZone(dateString);
            assertNotNull("Parsed date was null", parsedDate);
            Date expectedDate = createUTCDate(1965, 1, 23, 1, 3, 34);
            assertNotNull("Expected date was null", expectedDate);
            assertEquals("Expected date not equal", expectedDate, parsedDate);
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

    @Test
    public void testParseUTCDateStringWithPMHour() {
        try {
            // Create mock objects
            final Log mockLog = context.mock(Log.class);

            // Create SUT instance
            UTCDateUtil utcDateUtil = new UTCDateUtil() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }
            };

            // Set Expectations
            context.checking(new Expectations() {
                {
                    allowing(mockLog).debug(with(any(String.class)));
                    allowing(mockLog).isDebugEnabled();
                }
            });

            // Exercise test
            String dateString = "19650123140334";
            Date parsedDate = utcDateUtil.parseUTCDateOptionalTimeZone(dateString);
            assertNotNull("Parsed date was null", parsedDate);
            Date expectedDate = createUTCDate(1965, 1, 23, 14, 3, 34);
            assertNotNull("Expected date was null", expectedDate);
            assertEquals("Expected date not equal", expectedDate, parsedDate);
            System.out.println("GMT String of parsed date: " + parsedDate.toGMTString());
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

    @Test
    public void testParseUTCDateStringWithTZPrevDay() {
        try {
            // Create mock objects
            final Log mockLog = context.mock(Log.class);

            // Create SUT instance
            UTCDateUtil utcDateUtil = new UTCDateUtil() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }
            };

            // Set Expectations
            context.checking(new Expectations() {
                {
                    allowing(mockLog).debug(with(any(String.class)));
                    allowing(mockLog).isDebugEnabled();
                }
            });

            // Exercise test
            String dateString = "19650122200234-0700";
            Date parsedDate = utcDateUtil.parseUTCDateOptionalTimeZone(dateString);
            assertNotNull("Parsed date was null", parsedDate);
            Date expectedDate = createUTCDate(1965, 1, 23, 3, 2, 34);
            System.out.println("Expected date: " + expectedDate.toGMTString());
            System.out.println("Parsed date: " + parsedDate.toGMTString());
            assertNotNull("Expected date was null", expectedDate);
            assertEquals("Parsed date not equal", expectedDate, parsedDate);
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

    @Test
    public void testParseUTCDateStringWithAMHourAndTZ() {
        try {
            // Create mock objects
            final Log mockLog = context.mock(Log.class);

            // Create SUT instance
            UTCDateUtil utcDateUtil = new UTCDateUtil() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }
            };

            // Set Expectations
            context.checking(new Expectations() {
                {
                    allowing(mockLog).debug(with(any(String.class)));
                    allowing(mockLog).isDebugEnabled();
                }
            });

            // Exercise test
            String dateString = "19650123010234-0700";
            Date parsedDate = utcDateUtil.parseUTCDateOptionalTimeZone(dateString);
            assertNotNull("Parsed date was null", parsedDate);
            Date expectedDate = createUTCDate(1965, 1, 23, 8, 2, 34);
            assertNotNull("Expected date was null", expectedDate);
            assertEquals("Parsed date not equal", expectedDate, parsedDate);
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

    @Test
    public void testParseUTCDateStringWithCrossAMPMHourAndTZ() {
        try {
            // Create mock objects
            final Log mockLog = context.mock(Log.class);

            // Create SUT instance
            UTCDateUtil utcDateUtil = new UTCDateUtil() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }
            };

            // Set Expectations
            context.checking(new Expectations() {
                {
                    allowing(mockLog).debug(with(any(String.class)));
                    allowing(mockLog).isDebugEnabled();
                }
            });

            // Exercise test
            String dateString = "19650123080234-0700";
            Date parsedDate = utcDateUtil.parseUTCDateOptionalTimeZone(dateString);
            assertNotNull("Parsed date was null", parsedDate);
            Date expectedDate = createUTCDate(1965, 1, 23, 15, 2, 34);
            assertNotNull("Expected date was null", expectedDate);
            assertEquals("Parsed date not equal", expectedDate, parsedDate);
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

    @Test
    public void testParseUTCDateStringWithCrossYearTZ() {
        try {
            // Create mock objects
            final Log mockLog = context.mock(Log.class);

            // Create SUT instance
            UTCDateUtil utcDateUtil = new UTCDateUtil() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }
            };

            // Set Expectations
            context.checking(new Expectations() {
                {
                    allowing(mockLog).debug(with(any(String.class)));
                    allowing(mockLog).isDebugEnabled();
                }
            });

            // Exercise test
            String dateString = "19641231200234-0700";
            Date parsedDate = utcDateUtil.parseUTCDateOptionalTimeZone(dateString);
            assertNotNull("Parsed date was null", parsedDate);
            Date expectedDate = createUTCDate(1965, 1, 1, 3, 2, 34);
            assertNotNull("Expected date was null", expectedDate);
            assertEquals("Parsed date not equal", expectedDate, parsedDate);
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

    @Test
    public void testFormatUTCDateAM() {
        try {
            // Create mock objects
            final Log mockLog = context.mock(Log.class);

            // Create SUT instance
            UTCDateUtil utcDateUtil = new UTCDateUtil() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }
            };

            // Set Expectations
            context.checking(new Expectations() {
                {
                    allowing(mockLog).debug(with(any(String.class)));
                    allowing(mockLog).isDebugEnabled();
                }
            });

            // Exercise test
            Date sourceDate = createUTCDate(1984, 3, 5, 2, 5, 59);
            String expectedFormatStr = "19840305020559";
            String resultFormatStr = utcDateUtil.formatUTCDate(sourceDate);
            assertNotNull("Format string was null", resultFormatStr);
            assertEquals("Format date not as expected", expectedFormatStr, resultFormatStr);
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

    @Test
    public void testFormatUTCDatePM() {
        try {
            // Create mock objects
            final Log mockLog = context.mock(Log.class);

            // Create SUT instance
            UTCDateUtil utcDateUtil = new UTCDateUtil() {
                @Override
                protected Log createLogger() {
                    return mockLog;
                }
            };

            // Set Expectations
            context.checking(new Expectations() {
                {
                    allowing(mockLog).debug(with(any(String.class)));
                    allowing(mockLog).isDebugEnabled();
                }
            });

            // Exercise test
            Date sourceDate = createUTCDate(1984, 3, 5, 21, 5, 59);
            String expectedFormatStr = "19840305210559";
            String resultFormatStr = utcDateUtil.formatUTCDate(sourceDate);
            assertNotNull("Format string was null", resultFormatStr);
            assertEquals("Format date not as expected", expectedFormatStr, resultFormatStr);
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

    private Date createUTCDate(int year, int month, int day, int hour, int minute, int second) {
        Date createdDate = null;
        try {
            Calendar workingDate = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
            workingDate.set(Calendar.YEAR, year);
            workingDate.set(Calendar.MONTH, (month - 1));
            workingDate.set(Calendar.DAY_OF_MONTH, day);
            workingDate.set(Calendar.HOUR_OF_DAY, hour);
            workingDate.set(Calendar.MINUTE, minute);
            workingDate.set(Calendar.SECOND, second);
            workingDate.set(Calendar.MILLISECOND, 0);
            createdDate = workingDate.getTime();

        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
        return createdDate;
    }
}
