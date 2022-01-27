# User Defined Function Examples
This `UDF` example is for Trino/Starburst version 360

- how to specify a function with a Decimal return type with precision and scale that depend on the input parameters
- An example of a udf that uses the timestamp with timezone type

## Building
> maven 3.6.3 or greater must be installed to compile this project properly.

Open your preferred terminal, and switch directories to this project where the `pom.xml` is located

### package the plugin
```
mvn package
```

> this will produce a `target/trinoplugin-360.jar` file - this is your plugin to be deployed to your server.

> Read about deploying plugins [here](https://trino.io/docs/current/develop/spi-overview.html#deploying-a-custom-plugin)

### run tests
```shell
mvn test
```

## Contents
The implementation of this plugin can be found within the `io.trino.plugin.example` package:

- `FunctionsPlugin` which implements `io.trino.spi.Plugin` and notifies Trino how to load the plugin functions
- `ExampleDecimalPrecisionScaleFunction` which adds a `add_one` function that adds exactly 1.0 to the decimal passed
- `ExampleTimestampWithTimeZoneFunction` which adds a `yesterday` function that returns a `timestamp with time zone` 
  exactly 24 hours before the timestamp given
  
There is also a test module that validates the both of the functions provided.

## Dealing with Decimal Types in Trino
Trino represents **Decimal** types with a fixed `precision` and `scale` internally in one of two ways:

1. using `Slice` when the **Scale** is > 18 digits *(aka "Long Decimal")*
1. using `long` when the **Scale** <= 18 digits [8 byte] *(aka "Short Decimal")*
 
> Note: More recent versions of Trino (`369`) switched from the `Slice` type to `Int128`.

### Supporting Decimal Types in User Defined Functions
To fully support the internal API for decimals you must provide these signatures

- `@SqlType("decimal(p, s)") long num` with a return type of `long`
- `@SqlType("decimal(p, s)") Slice num` with a return type of `Slice`
- `@SqlType("decimal(p, s)") Slice num` with a return type of `long`


### Decimal Reference
- [MathFunctions.java:904](https://github.com/trinodb/trino/blob/360/core/trino-main/src/main/java/io/trino/operator/scalar/MathFunctions.java#L904)

## Dealing with Timestamp with Timezone
Internally in **Trino** `timestamp` can be represented in several ways.
- using `long` (packed epoch millis)
- using `LongTimestampWithTimeZone`

> `LongTimestampWithTimeZone` is a subclass of `TimestampWithTimeZone` so it covers both types.

> The `SqlType` signature for Timestamp w/TZ is `@SqlType("timestamp(p) with time zone")` 
> where `p` is the number of sub-second precision digits

### User Defined Functions
When implementing a new Timezone based user defined function you'll want to handle both `long` and `LongTimestampWithTimeZone`

The java `long` type is manipulated using bit-shifting to pack a timezone with timezone within.

### Packed Millis with Timezone
To work with a `long` type that contains a `packed-timestamp-timezone` you can use these methods:
- `io.trino.spi.type.DateTimeEncoding.unpackMillisUtc(long packedEpochMillis)` - returns milliseconds since the epoch
- `io.trino.spi.type.DateTimeEncoding.unpackZoneKey(long packedEpochMillis)` - returns  a `io.trino.spi.type.TimeZoneKey`

To construct a `LongTimestampWithTimeZone` from a `packed-timestamp-timezone` you can use the method:
- `LongTimestampWithTimeZone.fromEpochMillisAndFraction(epochMilliseconds, picoSeconds, timeZone)`

### `TimestampWithTimeZone` type
Many time functions expect to be given `Epoch Milliseconds` and `TimeZoneKey`  
When dealing with the `LongTimestampWithTimeZone` type you can obtain the milliseconds and timezone via:
- `timestamp.getEpochMillis()`
- `timestamp.getTimeZoneKey()`


## Exceptions within User Defined Functions
> Exceptions within your plugin should be caught and re-thrown as `TrinoException`
```java
throw new TrinoException(INVALID_FUNCTION_ARGUMENT, format("'%s' is not a valid time zone", zoneId.toStringUtf8()));
```

## Additional Notes
Additional details about plugins and user defined functions

### Maven for Trino Plugins

> All Trino plugins use the `trino-root` project as their `parent` project, this is accomplished in the `pom.xml` via:
```xml
    <parent>
        <groupId>io.trino</groupId>
        <artifactId>trino-root</artifactId>
        <version>360</version>
    </parent>
```
> This eases the setup of test cases, and causes your project to inherit all the maven checks and rules applied to the trino project itself.  
> Maven plugins such as `checkstyle` will run automatically when `trino-root` is the parent project.

> In addition to the `<parent>` project settings you must also set `<packaging>` to build the plugin correctly.

```xml
<packaging>trino-plugin</packaging>
``` 

### `trino-main` and dependency order

> It is best practice for plugins to try to only use functionality within `io.trino.spi` - This interface provides more stability than others 
> is designed for external consumption by plugins.  
> Unfortunately, when it comes to writing **user defined functions** you may need functionality from other packages like `io.trino.util`.
> The `trino-main` dependency provides `io.trino.util` and other internal packages you may need to build a custom `datetime` or `decimal` function.    
> You'll notice in the `pom.xml` of this package we include `trino-main` before any other import, including `trino-spi`.
> This dependency order is required to avoid various import conflicts within maven.
```
    <dependencies>
        <!-- trino-main if used must be first! -->
        <dependency>
            <groupId>io.trino</groupId>
            <artifactId>trino-main</artifactId>
            <scope>compile</scope>
        </dependency>

        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
        </dependency>

        <!-- Trino SPI -->
        <dependency>
            <groupId>io.trino</groupId>
            <artifactId>trino-spi</artifactId>
            <scope>provided</scope>
        </dependency>
```

### Additional packages

> All connectors within Trino are by definition plugins. This means that plugins located within the Trino github repository can be
> useful reference for building functionality within your own plugin or `udf`

> You may need to add additional modules from the Trino project as you make use of different packages within Trino.
An example of this can be found in several connector plugins within Trino

- [Geospatial pom.xml](https://github.com/trinodb/trino/blob/3e06ac2c94da4364fc386c7b0b3f82460e406a5e/plugin/trino-geospatial/pom.xml) 
- [Clickhouse pom.xml](https://github.com/trinodb/trino/blob/3e06ac2c94da4364fc386c7b0b3f82460e406a5e/plugin/trino-clickhouse/pom.xml)

> An example of this is `jodatime` - Because our tests use date-time objects we must add `jodatime` to our `pom.xml`

### `udf` Classes

> Although not syntactically required, empty private constructors are required for ALL plugin classes.  
> If you forget to add an empty private constructor you will get this rather confusing error:
```
 [io.trino.plugin.example.ExampleDecimalPrecisionScaleFunction] does not have any methods annotated with @ScalarFunction or @ScalarOperator
```

### Multiple signatures and Embedded Classes

> When a single **user defined function** requires multiple signatures you can define the signatures in a single class as long as the signatures 
> do not conflict.

> If the signatures do conflict you can embed multiple signatures into a single embedded class
- [MathFunctions.java (roundN) decimal signatures](https://github.com/trinodb/trino/blob/master/core/trino-main/src/main/java/io/trino/operator/scalar/MathFunctions.java#L912)
- [MathFunctions.java (round) bigint inline](https://github.com/trinodb/trino/blob/master/core/trino-main/src/main/java/io/trino/operator/scalar/MathFunctions.java#L784)
> Note that there must be at least one logical function annotated in the top-level class, or the plugin will not compile.