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
import io.trino.spi.function.LiteralParameter;
import io.trino.spi.function.LiteralParameters;
import io.trino.spi.function.ScalarFunction;
import io.trino.spi.function.SqlType;
import io.trino.spi.type.LongTimestampWithTimeZone;
import io.trino.spi.type.StandardTypes;

import static io.trino.client.IntervalDayTime.toMillis;
import static io.trino.spi.type.DateTimeEncoding.packDateTimeWithZone;
import static io.trino.spi.type.DateTimeEncoding.packTimeWithTimeZone;
import static io.trino.spi.type.DateTimeEncoding.unpackMillisUtc;
import static io.trino.spi.type.DateTimeEncoding.unpackZoneKey;

/**
 * Example class to show user defined function to handle timezone with timestamp types.
 *
 * timestamp with time zone can be represented with 2 types internally `long` and `LongTimestampWithTimeZone`
 */
@ScalarFunction("yesterday")
@Description("Return the timestamp 1 day before the timestamp given")
public final class ExampleTimestampWithTimeZoneFunction
{
    private ExampleTimestampWithTimeZoneFunction() {} // empty constructor
    private static final long ONE_DAY_MILLIS = toMillis(1, 0, 0, 0, 0);

    @LiteralParameters("p")
    @SqlType("timestamp(p) with time zone")
    public static long yesterday(
            @LiteralParameter("p") long precision,
            @SqlType("timestamp(p) with time zone") final long packedEpochMillis)
    {
        // see https://github.com/trinodb/trino/tree/master/core/trino-main/src/main/java/io/trino/operator/scalar/timestamptz for timestamp with tz operators

        LongTimestampWithTimeZone timestamp = LongTimestampWithTimeZone.fromEpochMillisAndFraction(unpackMillisUtc(packedEpochMillis), 0, unpackZoneKey(packedEpochMillis));

        // now we use our ts operators class to subtract a single day from the timestamp
        timestamp = TimestampWithTimeZoneOperators.TimestampPlusIntervalDayToSecond.add(timestamp, -ONE_DAY_MILLIS);

        // repack the timestamp into a "short timestamp with timezone" (long)
        return packDateTimeWithZone(timestamp.getEpochMillis(), timestamp.getTimeZoneKey());
    }

    @LiteralParameters("p")
    @SqlType("timestamp(p) with time zone")
    public static LongTimestampWithTimeZone yesterday(
            @LiteralParameter("p") long precision,
            @SqlType("timestamp(p) with time zone") LongTimestampWithTimeZone timestamp)
    {
        return TimestampWithTimeZoneOperators.TimestampPlusIntervalDayToSecond.add(timestamp, -ONE_DAY_MILLIS);
    }

}
