/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.trino.plugin.example;

import io.trino.operator.scalar.AbstractTestFunctions;
import io.trino.spi.type.SqlTimestampWithTimeZone;
import io.trino.spi.type.TimeZoneKey;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.trino.spi.type.TimeZoneKey.getTimeZoneKey;
import static io.trino.spi.type.TimestampWithTimeZoneType.createTimestampWithTimeZoneType;
import static io.trino.util.DateTimeZoneIndex.getDateTimeZone;

public class TestTimestampWithTimeZoneFunction extends AbstractTestFunctions
{
    private static final String BERLIN_ZONE_NAME = "Europe/Berlin";
    private static final TimeZoneKey BERLIN_TIME_ZONE_KEY = getTimeZoneKey(BERLIN_ZONE_NAME);
    private static final DateTimeZone BERLIN_ZONE = getDateTimeZone(BERLIN_TIME_ZONE_KEY);

    /**
     * Before we can test functions within our plugin we must first load the plugin.
     * The test framework does not automatically load plugins.
     *
     * Note that both the tests and the plugin are in the same Java package for ease of referencing.
     */
    @BeforeClass
    public void registerFunctions()
    {
        functionAssertions.installPlugin(new FunctionsPlugin());
    }


    @Test
    public void testYesterday()
    {
        assertFunction(String.format("yesterday(cast('2001-01-02 03:04:05.321 %s' as timestamp with time zone))", BERLIN_ZONE_NAME),
                createTimestampWithTimeZoneType(3),
                SqlTimestampWithTimeZone.newInstance(3, // sub-second precision digits
                        new DateTime(2001, 1, 1, 3, 4, 5, 321, BERLIN_ZONE).getMillis(), 0, BERLIN_TIME_ZONE_KEY));
    }
}