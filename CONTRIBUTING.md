# Contributing to Fortify SSC Parser Plugin for FAA

## Contribution Agreement

Contributions like bug fixes and enhancements may be submitted through Pull Requests on this repository. Before we can accept 3<sup>rd</sup>-party pull requests, you will first need to sign and submit the [Contribution Agreement](https://github.com/fortify/repo-resources/raw/main/static/Open%20Source%20Contribution%20Agreement%20Jan2020v1.pdf). Please make sure to mention your GitHub username when submitting the form, to allow us to verify that the author of a pull request has accepted this agreement. 


<!-- START-INCLUDE:repo-devinfo.md -->

## Information for Developers

The following sections provide information that may be useful for developers of
this parser plugin.

### Conventional commits & versioning

Versioning is handled automatically by [`release-please-action`](https://github.com/google-github-actions/release-please-action)
based on [Conventional Commits](https://www.conventionalcommits.org/). Every
commit to the `main` branch should follow the Conventional Commits convention.
Some examples:

```
chore: Won't show up in changelog
ci: Change to GitHub Actions workflow; won't show up in changelog
docs: Change to documentation; won't show up in changelog
fix: Some fix (#2)
feat: New feature (#3)
feat!: Some feature that breaks backward compatibility
```

`release-please-action`, invoked from the GitHub CI workflow, generates a pull
request containing updated `CHANGELOG.md` and `version.txt` files based on these
commit messages. Merging that pull request publishes a new release: it tags the
commit and creates a GitHub release with the built plugin jar attached.

### Lombok

This project uses Lombok. Gradle builds handle Lombok annotations automatically,
but to have your IDE compile the project without errors you may need to add
Lombok support to your IDE. See https://projectlombok.org/setup/overview.

### Gradle Wrapper & helpers

It is strongly recommended to build this project using the included Gradle
Wrapper scripts; using other Gradle versions may result in build errors. The
build also uses helper scripts from
[fortify-ps/shared-gradle-helpers](https://github.com/fortify-ps/shared-gradle-helpers).

### Common Commands

All commands are to be executed from the main project directory; adjust for your
platform.

* `./gradlew tasks --all` — list all available tasks
* `./gradlew clean build` — clean and build the project (plugin jar in `build/libs`)
* `./gradlew dist distThirdParty` — build the distribution zip and third-party
  information bundle (in `build/dist`)

<!-- END-INCLUDE:repo-devinfo.md -->


---

*[This document was auto-generated from CONTRIBUTING.template.md; do not edit by hand](https://github.com/fortify/shared-doc-resources/blob/main/USAGE.md)*
