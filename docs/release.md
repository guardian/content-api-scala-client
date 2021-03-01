## Releasing

This repository contains two projects: the client library (with its default implementation) and the AWS library.
Each has its own versioning and so they are released independently.

#### Releasing content-api-client and content-api-client-default:
In your PR, update `CHANGELOG.md` with a description of the change. Also update `version.sbt` if releasing a new major version. 

Then to release (from master branch):
```
sbt -DCAPI_TEST_KEY=a-valid-api-key 'release cross'
```
(you need to supply the api key because the build process runs the tests)

The api key needs to be a production key with tier `Internal`. You can obtain a key from `https://bonobo.capi.gutools.co.uk/`


#### Releasing content-api-client-aws:
This project does not depend on content-api-client.
```
sbt 'aws/release cross'
```


If the release process ends with the following lines;
```
[info] error: gpg failed to sign the data
[info] fatal: failed to write commit object
Push changes to the remote repository (y/n)? [y] 
```

It's ok to respond `y` as long as there are entries like this in the output;
```
[info]   Evaluate: signature-staging
[info]     Passed: signature-staging
```

The version numbers can be a bit screwy too, so double-check the version you're deploying. And you may have to manually push the updated `version.sbt` following the release process.
