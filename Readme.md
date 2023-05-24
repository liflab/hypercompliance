A hypercompliance palette for BeepBeep 3
========================================

This project is an extension to the [BeepBeep
3](https://liflab.github.io/beepbeep-3), event stream processing engine,
called a *palette*, that provides functionalities to evaluate *hyperqueries*
on event logs.

The code contained in this repository provides an implementation and a
benchmark for the concepts described in the following research paper:

> C. Soueidi, Y. Falcone, S. Hallé. (2023). Hypercompliance: Business Process
> Compliance Across Multiple Executions. Submitted to EDOC 2023.

Project structure
-----------------

The repository is made of three separate projects, all contained in the
`Source` folder:

- `Core` contains the implementation of the palette itself. Building this
  project using Ant generates a library (JAR file) that can be used to
  evaluate hyperqueries on event logs.
- `Examples` is a project showing examples of hyperqueries that can be
  evaluated using the palette. It requires the library created by the `Core`
  project.
- `Benchmark` contains an instance of a
  [LabPal](https://liflab.github.io/labpal) laboratory to evaluate the
  performance of the palette on various properties and logs. It also requires
  the `Core` library. More information about the benchmark can be found in its
  own Readme file.

Building this palette
---------------------

To compile the palette (i.e. the `Core` project), make sure you have the
following:

- The Java Development Kit (JDK) to compile. The palette complies
  with Java version 8; it is probably safe to use any later version.
- [Ant](http://ant.apache.org) to automate the compilation and build process

At the command line, in the `Source`folder, simply typing

    ant

should take care of downloading all dependencies and compiling all three
projects. Otherwise, each project can be built separately by typing `ant`
in their respective folders.

Dependencies
------------

The palette requires the following Java libraries:

- The latest version of [BeepBeep 3](https://liflab.github.io/beepbeep-3), and
  two of its [palettes](https://github.com/liflab/beepbeep-3-palettes): *Ltl*
  and *Tuples*
- The latest version of [OpenXES](http://www.xes-standard.org/openxes/start),
  and its dependencies:
  - [JAXB](https://javaee.github.io/jaxb-v2/)
  - [Google Guava](https://github.com/google/guava/releases)

These dependencies can be automatically downloaded and placed in the
`dep` folder of the project by typing:

    ant download-deps

The `Benchmark` project requires yet more libraries, which are documented in
the file `config.xml` of this specific folder.

<!-- :maxLineLen=78: -->