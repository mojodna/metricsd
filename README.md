Metricsd
========

I am a metrics aggregator for [Graphite](http://graphite.wikidot.com) that
supports counters, histograms and meters.

I should be drop-in compatible with Etsy's
[statsd](https://github.com/etsy/statsd), although I add explicit support for
meters (with the `m` type) and gauges (with the `g` type) and introduce the `h`
(histogram) type as an alias for timers (`ms`). I make heavy use of Coda Hale
/ Yammer's [Metrics](https://github.com/codahale/metrics) library for the JVM,
including its ability to flush to Graphite.

As with statsd, all values (excepting sample rate) are expected to be integers.
Go forth and multiply.

Meters are assumed to be per-second.

Metrics will be published to Graphite in the form
`metrics.{counter,histogram,meter}.<metric name>.<metric>`. Multiple metrics
may be published by joining messages together with newlines (as with
[statsite](https://github.com/kiip/statsite)).

In addition to the Metrics library, I owe a great deal of gratitude to statsd
(both Etsy's and Flickr's) and James Golick's
[statsd.scala](https://github.com/jamesgolick/statsd.scala) for paving the way,
in my protocol and structure respectively.

Metric Types
============

Metrics are collected by sending UDP messages to metricsd on port 8125 (or
whatever you've overridden it to be).

For testing purposes, you can use `netcat`:

```bash
echo "varietiesOfCheese:12|c" | nc -w 0 -u localhost 8125
```

When instrumenting your application, you'll probably want to use one of the
statsd-compatible libraries.

Counters
--------

Counters are incremented/decremented by sending messages of the form:

    <metric name>:<value>|c

For example, to add 12 varieties to the "varietiesOfCheese" metric:

    varietiesOfCheese:12|c

Counters may be updated at a lower sample rate; to do so, add `|@<sample
rate>`, e.g.:

    varietiesOfCheese:3|c|@0.25

_N.B._: Counters are currently reset when `metricsd` starts up. If you need
long-term historical values, you should use a gauge instead.

Gauges
------

Gauges are updated by sending messages of the form:

    <metric name>:<value>|g

For example, to set the current value for "varietiesOfCheese":

    varietiesOfCheese:27|g

As gauges do not have base their current value on previous values, they are
more appropriate for storing metrics in a durable fashion (i.e. not susceptible
to `metricsd` restarting).

Histograms
----------

Histograms are updated by sending messages of the form:

    <metric name>:<value>|h

For example:

    responseTime:244|h

Again, values must be integers, so use a data-appropriate scale (e.g.
milliseconds).

_N.B._: Histograms will be reset (samples will be cleared) when `metricsd`
restarts. Biased (`ExponentiallyDecayingSample(1028, 0.015)`, Metrics'
`HistogramMetric.SampleType.BIASED`, biasing the sample to the last 5 minutes)
histograms are used to mitigate the impact of this.

Meters
------

Meters are updating by sending messages of the form:

    <metric name>

For example:

    userDidSignIn

Running
=======

To run metricsd, execute the JAR:

```bash
$ java -jar metricsd-assembly-0.1.0.jar
```

If you wish to provide a custom configuration:

```bash
$ CONFIG=/path/to/config.json java -jar metricsd-assembly-0.1.0.jar
```

Configuration
=============

Configuration is done by providing the path to a JSON file similar to
`config/metricsd.json`:

```json
{
    "debug": false,
    "graphite": {
        "flushInterval": 10,
        "host": "localhost",
        "port": 2003
    },
    "log": {
        "level": "INFO",
        "file": "log/metricsd.log"
    },
    "port": 8125
}
```

These are the default values; feel free to omit unchanged values from your
configuration.

Building
========

Build Requirements
------------------

* Scala 2.9.1
* SBT 0.10.x

Begin by installing [Scala](http://www.scala-lang.org/) and
[SBT](https://github.com/harrah/xsbt) according to the relevant instructions
for your system.

To build an assembly (an executable JAR file containing all dependencies):

```bash
$ sbt assembly
```

License
=======

Copyright (c) 2011 Seth Fitzsimmons

Published under the BSD License.
