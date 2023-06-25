![Build Status](https://github.com/laingke/btm/actions/workflows/workflow.yml/badge.svg?event=push)

### Home of BTM, the Bitronix JTA Transaction Manager ###

This Bitronix Transaction Manager (BTM) is a new refactor from [BTM 3.0](https://github.com/bitronix/btm). In this fork, BTM will try to progressively complete the following goal in the roadmap.

### Next roadmap
- [x] Support JDK17+, and transition from Java EE to Jakarta EE, support Jakarta EE 10.
- [ ] Support the least web containers, and other containers such as Undertow/Quarkus etc.
- [x] Bump dependencies version.
- [x] Implement module with Jigsaw but not OSGi.
- [ ] Continuous security enhancement. Stateless enhancement.
- [ ] Support YAML configuration.
- [ ] Support GraalVM build. (CGLIB will be disabled.)
- [ ] Store runtime data to a distributed storage system or TSDB.
- [ ] Refactor GUI module(which is removed now) with JavaFX or WebApp, and support other friendly API to access BTM runtime data.
- [ ] Refactor Document, and display in GitHub Pages, and i18n support(optional).
- [ ] collect relevant monitoring data, including Metrics, Tracing and Logging.
- [ ] Explore the integration with other projects.
- [ ] Demos, testcases and diagrams. Performance report and comparative report on similar projects.

#### Original General Information ####
* [Overview](https://github.com/bitronix/btm/wiki/Overview)
* [FAQ](https://github.com/bitronix/btm/wiki/FAQ)

#### Configuration ####
* [Transaction manager configuration](https://github.com/bitronix/btm/wiki/Transaction-manager-configuration)
* [Resource loader configuration](https://github.com/bitronix/btm/wiki/Resource-loader-configuration)

### Notices

In the whole 3.0.* version lifecycle, new features will NOT be considered. It will focus on the transition to new development environment and replace all deprecated API.

A radical solution will be implemented and some old version container compatibility will be discarded.
