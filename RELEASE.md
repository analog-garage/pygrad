# Releases

### 0.1.9

TBD

### 0.1.8

- Build changes:
  - Build with gradle 3.0 wrapper
  - Fix eclipse build path to put test classes in separate tree
  - Use list instead of set for requirements as required by setuptools

### 0.1.7

- addArtifactoryRepository method (issue #5)
- No longer delete generated "$sourceDir/*project*.egg-info/" subdirectory in PythonSetupTask, so that script entrypoints installed by `pydevelop` will work. Users should add appropriate entry to ignore this directory for their source control system.

### 0.1.6

- stringify helper now passes through null, stringifyList/Set now ignore null entries
- null entries to repositories, requirements and buildRequirements are now ignored
- python.devpiPort, devpiUser and python.devpiPassword default to null if no project property defined
- python.devpiUrl is null if devpiPort, devpiUser or devpiIndex are null.
- This allows you to write:

~~~groovy
python {
   repositories devpiUrl
   ...
}
~~~

to conditionally use local devpi package index if configured by user's gradle.properties.

### 0.1.5

- Automatically applies 'base' package to get clean* tasks.
- Added 'artifactoryPublishPython' tasks and adds associated properties to python extension
- Added documentation to [README](README.md)

### 0.1.4

- Minimize delays when running gradle with `--offline` flag: don't retry python package downloads, minimize socket timeout and don't attempt to use remote http servers.

### 0.1.3

- Added basic plugin test.

### 0.1.2

- Keep copyright messages out of groovydoc.
- Prevent uploading SNAPSHOT releases.

### 0.1.1

- Updated README

### 0.1.0

- Initial release


