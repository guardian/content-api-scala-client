Content API Firehose Client
============================

[![content-api-firehose-client Scala version support](https://index.scala-lang.org/guardian/content-api-firehose-client/content-api-firehose-client/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/guardian/content-api-firehose-client/content-api-firehose-client)
[![Release](https://github.com/guardian/content-api-firehose-client/actions/workflows/release.yml/badge.svg)](https://github.com/guardian/content-api-firehose-client/actions/workflows/release.yml)

A client for the Guardian's [Content API](http://explorer.capi.gutools.co.uk/) firehose - an events stream for all updates and deletes of Guardian content.

## Setup

Add the following line to your SBT build definition, and set the version number to be the latest from the [releases page](https://github.com/guardian/content-api-firehose-client/releases):

```scala
libraryDependencies += "com.gu" %% "content-api-firehose-client" % "x.y"
```

## Usage

In order to get started with reading from the content-api firehose you will need to provide us with your AWS account
number so that we can provide you with permissions. To do so, you may speak to anyone within the content api/off platform
team and they will be able to help. Or better yet, you can submit the PR yourself! You will need to provide a new 
parameter to [this](https://github.com/guardian/crier/blob/master/cloudformation.json) cloudformation file as so:

```
"TEAM_NAMEAccountNumber": {
    "Description": "Account number for the TEAM_NAME team",
    "Type": "String"
}
```

And then add a corresponding assumeRole command to the [cross account role](https://github.com/guardian/crier/blob/master/cloudformation.json#L100)
as such: 

```
{
    "Action": "sts:AssumeRole",
    "Effect": "Allow",
    "Principal": {
        "AWS": {
            "Fn::Join": [ "", [ "arn:aws:iam::", { "Ref": "TEAM_NAMEAccountNumber" }, ":root" ]]
        }
    }
}
```

Once this been done, someone on the team will review, merge and update for you to begin. Create an IAM policy in your 
project's cloudformation file as so: 

 
 - STAGE - e.g PROD or CODE
 - APP_NAME - e.g. my-application
 - MODE - e.g. live or preview
 - CAPI_ACCOUNT_NUMBER - The AWS account number for CAPI. Ask someone from the CAPI team to provide you with this.
 - STREAM_NAME - The AWS Kinesis stream name of the CAPI events stream. Ask someone from the CAPI team to provide you with this.

```json
  "CrierDynamoDBPolicy": {
    "Type": "AWS::IAM::Policy",
    "Properties": {
      "PolicyName": "CrierDynamo",
      "PolicyDocument": {
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "dynamodb:*"
            ],
            "Resource": [
              {
                "Fn::Join": [
                  "",
                  [
                    "arn:aws:dynamodb:*:YOUR_ACCOUNT_NUMBER:table/content-api-firehose-v2-STAGE_APP_NAME-MODE-",
                    {
                      "Ref": "Stage"
                    },
                    "*"
                  ]
                ]
              }
            ]
          },
          {
            "Effect": "Allow",
            "Action": [
              "kinesis:Get*",
              "kinesis:List*",
              "kinesis:Describe*"
            ],
            "Resource": [
              "arn:aws:kinesis:*:CAPI_ACCOUNT_NUMBER:stream/STREAM_NAME"
            ]
          }
        ]
      },
      "Roles": [
        {
          "Ref": "RootRole"
        }
      ]
    }
  }
```

Once this has been done you will be able to create a `ContentApiFirehoseConsumer` in your application as such:

 - STS_ROLE_TO_ASSUME - The cross account role to assume in order to read from the content api events stream. Ask 
 anyone from the CAPI team to provide you with this. This should be stored by your application as a secret.

```scala

  val kinesisCredsProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("capi"),
    new STSAssumeRoleSessionCredentialsProvider(STS_ROLE_TO_ASSUME, "capi")
  )

  val dynamoCredsProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("YOUR_PROFILE_NAME"),
    new InstanceProfileCredentialsProvider()
  )
  
  val kinesisStreamReaderConfig = KinesisStreamReaderConfig(
    streamName = "GET_STREAM_NAME_FROM_SOMEONE_IN_CAPI",
    app = "my-application", // must match the table name in CrierDynamoDBPolicy
    stage = "PROD",
    mode = "live",
    suffix = None,
    kinesisCredentialsProvider = kinesisCredsProvider,
    dynamoCredentialsProvider = dynamoCredsProvider,
    awsRegion = "eu-west-1"
  )

val contentApiFirehoseConsumer: ContentApiFirehoseConsumer = new ContentApiFirehoseConsumer(
    kinesisStreamReaderConfig
    streamListener // Your implementation of `StreamListener` - to provide behavior per event type.
)

```

Then all is left is to start consuming from the firehose.

```
contentApiFirehoseConsumer.start()
```

And when you're finished:

```
contentApiFirehoseConsumer.shutdown()
```
## How to release a new version

This repo uses [`gha-scala-library-release-workflow`](https://github.com/guardian/gha-scala-library-release-workflow)
to automate publishing releases - see
[**Making a Release**](https://github.com/guardian/gha-scala-library-release-workflow/blob/main/docs/making-a-release.md).
