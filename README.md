# A State Machine implementation in Java
[![Build Status](https://travis-ci.org/lind/machineprocess.png?branch=master)](https://travis-ci.org/lind/machineprocess)
[![Dependency Status](https://www.versioneye.com/user/projects/550195a04a1064db0e000328/badge.svg?style=flat)](https://www.versioneye.com/user/projects/550195a04a1064db0e000328)
[![Coverage Status](https://coveralls.io/repos/lind/machineprocess/badge.svg?branch=master)](https://coveralls.io/r/lind/machineprocess?branch=master)

Simple State Machine with transitions, guards and actions.

...but without composite state, sub-state machine, deferred signals, event queues...

Define the State Machine by subclassing State Machine and using the builders in the constructor. Se tests for usage.

<!-- language: lang-java -->
        State onHold = state(ON_HOLD)
                .transition(HURL_PHONE).guardedBy(PHONE_HURLED_AGAINST_WALL)
                .to(phoneDestroyed)
                .transition(HANG_UP).guardedBy(HUNG_UP)
                .to(offHook)
                .transition(TAKE_OFF_HOLD).onTransition(stopMuzak).guardedBy(TOOK_OFF_HOLD)
                .to(connected)
                .build();

## State Machine used in the unit tests

### Phone State Machine Diagram
Inspired by [simplestatemachine](http://simplestatemachine.codeplex.com/)

Generated from code by [Graphviz](http://www.graphviz.org/) - Usage: dot -o outputfile -Tbmp inputfile.dot

See: [Graphviz command-line-invocation](http://www.graphviz.org/content/command-line-invocation)

![Phone State Machine Diagram](PhoneStateMachine.bmp "Phone State Machine Diagram")
