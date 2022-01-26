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

### run tests
```shell
mvn test
```

## Dealing with Decimal Types in Trino
Trino represents **Decimal** types with a fixed `precision` and `scale` internally in one of two ways:

1. using `Slice` when the **Scale** is > 18 digits *(aka "Long Decimal")*
1. using `long` when the **Scale** <= 18 digits [8 byte] *(aka "Short Decimal")*
 
More recent versions of Trino (`369`) switched from the `Slice` type to `Int128`.

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

### User Defined Functions
When implementing a new Timezone based user defined function you'll want to handle both `long` and `LongTimestampWithTimeZone`

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

### `udf` Classes

> Although not syntactically required, empty private constructors are required for ALL plugin classes.  
> If you forget to add an empty private constructor you will get this rather confusing error:
```
 [io.trino.plugin.example.ExampleDecimalPrecisionScaleFunction] does not have any methods annotated with @ScalarFunction or @ScalarOperator
```

### Multiple signatures and Embedded Classes

> When a single **user defined function** requires multiple