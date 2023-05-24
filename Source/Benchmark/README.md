# Benchmark Lab for Hypercompliance Properties on Event Logs

This lab contains the benchmark for hypercompliance properties on event logs, as described in the following publication:

> C. Soueidi, Y. Falcone, S. Hallé. (2023). Hypercompliance: Business Process Compliance Across Multiple Executions. Submitted to *EDOC 2023*.

## Abstract

Compliance checking is an operation that assesses whether every execution trace of a business process satisfies a given correctness condition. Our work introduces the notion of a hyperquery, which involves multiple traces from a log at the same time. A specific instance of a hyperquery is a hypercompliance condition, which is a correctness requirement that involves the entire log instead of individual process instances.

This lab proposes a benchmark for an extension of the [BeepBeep 3](https://liflab.github.io/beepbeep-3) event stream engine designed to evaluate hyperqueries on event logs. It evaluates various hyperqueries on event logs that are either synthetic or sourced from real-world online log repositories. Among the elements evaluated are the total and progressive running time needed to evaluate a query, as well as the amount of memory consumed.

## Using This Lab

This lab is designed to be interactive. Please refer to the [Help](/help) page for more information on how to use this lab effectively.
