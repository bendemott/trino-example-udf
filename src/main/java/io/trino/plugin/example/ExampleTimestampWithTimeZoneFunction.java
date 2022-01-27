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

import io.trino.operator.scalar.timestamptz.TimestampWithTimeZoneOperators;
import io.trino.spi.function.Description;
import io.trino.spi.function.LiteralParameters;
import io.trino.spi.function.ScalarFunction;
import io.trino.spi.function.SqlType;
import io.trino.spi.type.LongTimestampWithTimeZone;
import io.trino.spi.type.StandardTypes;
import io.trino.spi.type.TimeZoneKey;

import static io.trino.spi.type.DateTimeEncoding.unpackMillisUtc;
import static io.trino.spi.type.DateTimeEncoding.unpackTimeNanos;
import static io.trino.spi.type.DateTimeEncoding.unpackZoneKey;
import static io.trino.spi.type.TimeZoneKey.getTimeZoneKey;

/**
 * Example class to show user defined function to handle timezone with timestamp types.
 *
 * timestamp with time zone can be represented with 2 types internally `long` and `LongTimestampWithTimeZone`
 */
public final class ExampleTimestampWithTimeZoneFunction
{
    private ExampleTimestampWithTimeZoneFunction() {} // empty constructor


    @Description("Return the timestamp 1 day before the timestamp given")
    @ScalarFunction("yesterday")
    @LiteralParameters("p")
    @SqlType("timestamp(p) with time zone")
    public static LongTimestampWithTimeZone yesterday(@SqlType("timestamp(p) with time zone") final long packedEpochMillis)
    {
        // see https://github.com/trinodb/trino/tree/master/core/trino-main/src/main/java/io/trino/operator/scalar/timestamptz for timestamp with tz operators
        long interval = 1; // days
        LongTimestampWithTimeZone timestamp = LongTimestampWithTimeZone.fromEpochMillisAndFraction(unpackMillisUtc(packedEpochMillis), 0, unpackZoneKey(packedEpochMillis));

        // now we use our ts operators class to subtract a single day from the timestamp
        return TimestampWithTimeZoneOperators.TimestampPlusIntervalDayToSecond.add(timestamp, -interval);
    }

    @Description("Return the timestamp 1 day before the timestamp given")
    @ScalarFunction("yesterday")
    @LiteralParameters("p")
    @SqlType(StandardTypes.BIGINT)
    public static LongTimestampWithTimeZone yesterday(@SqlType("timestamp(p) with time zone") LongTimestampWithTimeZone timestamp)
    {
        long interval = 1; // days
        return TimestampWithTimeZoneOperators.TimestampPlusIntervalDayToSecond.add(timestamp, -interval);
    }

}
