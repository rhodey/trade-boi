# trade-boi
High-frequency trading on GDAX with embedded domain-specific-language.

## Build
```
$ sbt tradeBoiJVM/assembly
```

## Config
```
$ cp example-scam.properties scam.properties
```

## Run
```
$ java -jar jvm/target/scala-2.12/tradeBoi-assembly-0.4.0.jar
```

## Development
```
$ build-proto.sh
```

## License
Copyright 2017 An Honest Effort LLC
Licensed under the GPLv3: http://www.gnu.org/licenses/gpl-3.0.html
