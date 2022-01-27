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
import io.airlift.slice.Slice;
import io.trino.spi.function.Description;
import io.trino.spi.function.LiteralParameters;
import io.trino.spi.function.ScalarFunction;
import io.trino.spi.function.SqlType;

import static io.trino.spi.type.UnscaledDecimal128Arithmetic.add;
import static io.trino.spi.type.UnscaledDecimal128Arithmetic.unscaledDecimal;

/**
 * Trino represents Decimal types with a fixed precision and scale internally in one of two ways:
 *
 *  1.) using the `Slice` type when the Scale is > 18 digits
 *  2.) using `long` when the Scale <= 18 digits
 *
 *  This is done for internal efficiency and compactness in memory
 *
 *  Version 368 of Trino switched from the `Slice` type to `Int128` - any code you write will need to use this type if you upgrade.
 *
 *  Reference: https://github.com/trinodb/trino/blob/360/core/trino-main/src/main/java/io/trino/operator/scalar/MathFunctions.java
 *
 *  The methods below accept precision and scale as arguments even though they don't use them.
 *  The type system will automatically provide this.
 */
public final class ExampleDecimalPrecisionScaleFunction
{
    private ExampleDecimalPrecisionScaleFunction() {}

    @ScalarFunction("add_one") // this is the name of the sql function that will be exposed to Trino
    @LiteralParameters({"p", "s"})
    @SqlType("decimal(p, s)")
    public static long addOneShort(
            @SqlType("decimal(p, s)") long num)  // the decimal number represented as a "long"
    {
        return num + 1;
    }

    /**
     * The signatures of these two types is identical to the type system.
     * The only difference is the return type.
     * So we use an embedded class to group them together.
     * This is required to avoid an error from the type system.
     */
    @Description("Add +1 to the value given")
    @ScalarFunction("add_one")
    public static final class AddOne
    {
        private AddOne() {}

        @LiteralParameters({"p", "s"})
        @SqlType("decimal(p, s)")
        public static Slice addOneLong(
                @SqlType("decimal(p, s)") Slice num)
        {
            // create a Slice object representing the decimal `1.0`
            Slice addAmount = unscaledDecimal((long) 1.0);
            return add(num, addAmount);
        }

        @LiteralParameters({"p", "s"})
        @SqlType("decimal(p, s)")
        public static Slice addOneShortLong(
                @SqlType("decimal(p, s)") long num)
        {
            return addOneLong(unscaledDecimal(num));
        }
    }
}

/* TODO switch this to a round algorithm that rounds to a random number of decimal places
@ScalarFunction("round")
@Description("Round to given number of decimal places")
public static final class RoundN
{
    @LiteralParameters({"p", "s", "rp"})
    @SqlType("decimal(rp, s)")
    @Constraint(variable = "rp", expression = "min(38, p + 1)")
    public static long roundNShort(
            @LiteralParameter("p") long numPrecision,
            @LiteralParameter("s") long numScale,
            @SqlType("decimal(p, s)") long num,
            @SqlType(StandardTypes.INTEGER) long decimals)
    {
        if (num == 0 || numPrecision - numScale + decimals <= 0) {
            return 0;
        }
        if (decimals >= numScale) {
            return num;
        }
        if (num < 0) {
            return -roundNShort(numPrecision, numScale, -num, decimals);
        }

        long rescaleFactor = longTenToNth((int) (numScale - decimals));
        long remainder = num % rescaleFactor;
        int roundUp = (remainder >= rescaleFactor / 2) ? 1 : 0;
        return (num / rescaleFactor + roundUp) * rescaleFactor;
    }

    @LiteralParameters({"p", "s", "rp"})
    @SqlType("decimal(rp, s)")
    @Constraint(variable = "rp", expression = "min(38, p + 1)")
    public static Slice roundNLong(
            @LiteralParameter("s") long numScale,
            @LiteralParameter("rp") long resultPrecision,
            @SqlType("decimal(p, s)") Slice num,
            @SqlType(StandardTypes.INTEGER) long decimals)
    {
        if (decimals >= numScale) {
            return num;
        }
        int rescaleFactor = ((int) numScale) - (int) decimals;
        try {
            Slice result = rescale(rescale(num, -rescaleFactor), rescaleFactor);
            throwIfOverflows(result, ((int) resultPrecision));
            return result;
        }
        catch (ArithmeticException e) {
            throw new TrinoException(NUMERIC_VALUE_OUT_OF_RANGE, "decimal overflow: " + num, e);
        }
    }

    @LiteralParameters({"p", "s", "rp"})
    @SqlType("decimal(rp, s)")
    @Constraint(variable = "rp", expression = "min(38, p + 1)")
    public static Slice roundNShortLong(
            @LiteralParameter("s") long numScale,
            @LiteralParameter("rp") long resultPrecision,
            @SqlType("decimal(p, s)") long num,
            @SqlType(StandardTypes.INTEGER) long decimals)
    {
        return roundNLong(numScale, resultPrecision, unscaledDecimal(num), decimals);
    }
}*/
