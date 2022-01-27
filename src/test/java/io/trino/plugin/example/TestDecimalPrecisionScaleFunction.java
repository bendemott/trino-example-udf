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
import io.trino.spi.type.SqlDecimal;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.google.common.collect.ImmutableList;

import static io.trino.operator.scalar.ApplyFunction.APPLY_FUNCTION;

import static io.trino.spi.type.DecimalType.createDecimalType;

/**
 *       // ROUND short DECIMAL -> short DECIMAL
 *         assertFunction("round(DECIMAL '0')", createDecimalType(1, 0), SqlDecimal.of("0"));
 *         assertFunction("round(DECIMAL '0.1')", createDecimalType(1, 0), SqlDecimal.of("0"));
 *         assertFunction("round(DECIMAL '-0.1')", createDecimalType(1, 0), SqlDecimal.of("0"));
 *         assertFunction("round(DECIMAL '3')", createDecimalType(1, 0), SqlDecimal.of("3"));
 *         assertFunction("round(DECIMAL '-3')", createDecimalType(1, 0), SqlDecimal.of("-3"));
 *         assertFunction("round(DECIMAL '3.0')", createDecimalType(2, 0), SqlDecimal.of("3"));
 *         assertFunction("round(DECIMAL '-3.0')", createDecimalType(2, 0), SqlDecimal.of("-3"));
 *         assertFunction("round(DECIMAL '3.49')", createDecimalType(2, 0), SqlDecimal.of("3"));
 *         assertFunction("round(DECIMAL '-3.49')", createDecimalType(2, 0), SqlDecimal.of("-3"));
 *         assertFunction("round(DECIMAL '3.50')", createDecimalType(2, 0), SqlDecimal.of("4"));
 *         assertFunction("round(DECIMAL '-3.50')", createDecimalType(2, 0), SqlDecimal.of("-4"));
 *         assertFunction("round(DECIMAL '3.99')", createDecimalType(2, 0), SqlDecimal.of("4"));
 *         assertFunction("round(DECIMAL '-3.99')", createDecimalType(2, 0), SqlDecimal.of("-4"));
 *         assertFunction("round(DECIMAL '9.99')", createDecimalType(2, 0), SqlDecimal.of("10"));
 *         assertFunction("round(DECIMAL '-9.99')", createDecimalType(2, 0), SqlDecimal.of("-10"));
 *         assertFunction("round(DECIMAL '9999.9')", createDecimalType(5, 0), SqlDecimal.of("10000"));
 *
 *         assertFunction("round(DECIMAL '-9999.9')", createDecimalType(5, 0), SqlDecimal.of("-10000"));
 *         assertFunction("round(DECIMAL '1000000000000.9999')", createDecimalType(14, 0), SqlDecimal.of("1000000000001"));
 *         assertFunction("round(DECIMAL '-1000000000000.9999')", createDecimalType(14, 0), SqlDecimal.of("-1000000000001"));
 *         assertFunction("round(DECIMAL '10000000000000000')", createDecimalType(17, 0), SqlDecimal.of("10000000000000000"));
 *         assertFunction("round(DECIMAL '-10000000000000000')", createDecimalType(17, 0), SqlDecimal.of("-10000000000000000"));
 *         assertFunction("round(DECIMAL '9999999999999999.99')", createDecimalType(17, 0), SqlDecimal.of("10000000000000000"));
 *         assertFunction("round(DECIMAL '99999999999999999.9')", createDecimalType(18, 0), SqlDecimal.of("100000000000000000"));
 *
 *         // ROUND long DECIMAL -> long DECIMAL
 *         assertFunction("round(CAST(0 AS DECIMAL(18,0)))", createDecimalType(18, 0), SqlDecimal.of("0"));
 *         assertFunction("round(CAST(0 AS DECIMAL(18,1)))", createDecimalType(18, 0), SqlDecimal.of("0"));
 *         assertFunction("round(CAST(0 AS DECIMAL(18,2)))", createDecimalType(17, 0), SqlDecimal.of("0"));
 */
public class TestDecimalPrecisionScaleFunction extends AbstractTestFunctions
{
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
    public void testAddOne()
    {
        // addOne short DECIMAL -> short DECIMAL
        assertFunction("add_one(DECIMAL '0')", createDecimalType(1, 0), SqlDecimal.of("1"));

        // ROUND long DECIMAL -> long DECIMAL
        assertFunction("add_one(CAST(0 AS DECIMAL(18,0)))", createDecimalType(18, 0), SqlDecimal.of("1"));
    }
}
