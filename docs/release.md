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


#### Non-production releases:
If you intend to publish a release candidate or snapshot build (e.g. from a WIP code branch) for testing the library in another application prior to releasing your changes to production - which can be useful when testing the effects of upgrading dependencies etc - you should also send the appropriate value in a parameter:
```
sbt -DCAPI_TEST_KEY=a-valid-api-key -DRELEASE_TYPE=candidate|snapshot 'release cross'
```

The value you pass drives the version numbering hints and which release steps to execute. For example a snapshot release is not published to Maven Central but a release candidate is. 

These options also influence the post-release steps such as updating the version.sbt file and committing it to git. Neither the candidate nor the snapshot releases include those steps. 

You'll still be prompted to enter the version number you're releasing and it is currently left to the developer to ensure the version number is suitable. You can always check what's already available on maven here: https://mvnrepository.com/artifact/com.gu/content-api-client   

For a "normal" production release be sure not to have a RELEASE_TYPE reference hanging around - quit and restart sbt without the parameter if you do.

#### Releasing content-api-client-aws:
This project does not depend on content-api-client.
```
sbt 'project aws' 'release cross'
```

The release process uses the content-api-client version number in the `sonatypeBundleDirectory` (see https://github.com/xerial/sbt-sonatype).  This is incidental since it doesn't affect the version number that is released to Sonatype, but it does result in some concerning log lines e.g.

```
[info] 	published content-api-client-aws_2.13 to /Users/<user>/code/content-api-scala-client/target/sonatype-staging/17.10-SNAPSHOT/com/gu/content-api-client-aws_2.13/0.6.part/content-api-client-aws_2.13-0.6-javadoc.jar
```

When you might have expected 

```
[info] 	published content-api-client-aws_2.13 to /Users/<user>/code/content-api-scala-client/target/sonatype-staging/0.6/com/gu/content-api-client-aws_2.13/0.6.part/content-api-client-aws_2.13-0.6-javadoc.jar
```

Try not to worry about this, or if it really bothers you please fix it.

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
