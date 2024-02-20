# Simple Example

This example demonstrates basic usage of mc-translate by loading an assets directory containing a single (english) lang
file. It then prints the only translation it contains.

## Use as a template

You can use any of the examples as a template with just a few tweaks.

- In `settings.gradle`:
  - Change `rootProject.name`
  - Remove the `includeBuild` line.
- In `build.gradle`:
  - Set your `group`.
  - Set your `version`.
  - Specify a version in the `mc-translate` dependency.
