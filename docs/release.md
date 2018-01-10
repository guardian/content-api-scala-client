## Releasing
This repository contains two projects: the client library and the AWS library.
Each has its own build.sbt and so they are released independently with separate versioning.

#### Releasing content-api-client:
In your PR, update `CHANGELOG.md` with a description of the change.

Then to release (from master branch):
```
cd client
sbt 'release cross'
```

#### Releasing content-api-client-aws:
This project does not depend on content-api-client.
```
cd aws
sbt 'release cross'
```

