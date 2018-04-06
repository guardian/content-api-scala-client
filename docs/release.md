## Releasing

This repository contains two projects: the client library (with its default implementation) and the AWS library.
Each has its own versioning and so they are released independently.

#### Releasing content-api-client and content-api-client-default:
In your PR, update `CHANGELOG.md` with a description of the change.

Then to release (from master branch):
```
sbt 'release cross'
```

#### Releasing content-api-client-aws:
This project does not depend on content-api-client.
```
sbt 'aws:release cross'
```

