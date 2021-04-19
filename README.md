# lucene-monitor-helpers
Missing pieces of the Lucene Monitor

## Install locally

```shell
mvn install -U
```

## Release to Clojars

```
make release
mvn release:perform -Darguments="-Dmaven.javadoc.skip=true"
```
