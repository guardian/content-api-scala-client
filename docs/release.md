## Releasing

This repository contains two projects: the client library (with its default implementation) and the AWS library.
Each has its own versioning and so they are released independently.

#### Releasing content-api-client and content-api-client-default:
Follow the instructions [here](https://docs.google.com/document/d/1rNXjoZDqZMsQblOVXPAIIOMWuwUKe3KzTCttuqS7AcY/edit#) to get set up with your first Scala deploy.

Then to release (from main branch):
```
sbt -DCAPI_TEST_KEY=a-valid-api-key 'release cross'
```

You need to supply the api key because the build process runs the tests. The api key needs to be a production key with tier `Internal`. You can retrieve your key or create a new one at `https://bonobo.capi.gutools.co.uk/`

As part of the release process, you will be asked to specify the version of this release. It should be the current number with the word "SNAPSHOT" redacted. eg. if `version.sbt` records the current version as "17.24-SNAPSHOT", you should enter "17.24" at this prompt (or just press "enter" if that suggestion is already in place on the prompt). Similarly, the next version should be, in this example, "17.25-SNAPSHOT" which should also be enterred in the prompt if not already in place.

Debugging note: if you get an error that prevents deploy and says the commit could not be properly 'signed' by your gpg key, it is worth adding the following line to your `~/.zshrc`, bash profile, or similar: `export GPG_TTY=$(tty)` and open a new terminal window. This allows your current terminal session to access and use your GPG keys if it cannot by default.

Once this version has been released, update `CHANGELOG.md` with a description of the change.

#### Non-production releases:
If you intend to publish a beta or snapshot build (e.g. from a WIP code branch) for testing the library in another application prior to releasing your changes to production - which can be useful when testing the effects of upgrading dependencies etc - you should also send the appropriate value in a parameter:
```
sbt -DCAPI_TEST_KEY=a-valid-api-key -DRELEASE_TYPE=beta|snapshot 'release cross'
```

The value you pass drives the version numbering hints and which release steps to execute. For example a snapshot release is not published to Maven Central but a beta release is. 

These options also influence the post-release steps such as updating the version.sbt file and committing it to git. Neither the beta nor the snapshot releases include those steps. 

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
