import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import type { App } from 'aws-cdk-lib';
import { GuGithubActionsRole } from '@guardian/cdk/lib/constructs/iam';
import { GuAllowPolicy } from '@guardian/cdk/lib/constructs/iam/policies/base-policy';

export class FaciaScalaClientTesting extends GuStack {
	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);
		let fapiBucketArn = 'arn:aws:s3:::facia-tool-store';
		new GuGithubActionsRole(this, {
			policies: [
				new GuAllowPolicy(this, 'fapi-s3-bucket-access', {
					actions: [
						's3:GetObject', // required by FAPI to download files
						's3:ListBucket', // avoiding S3 AccessDenied errors when FAPI tries to get nonexistent objects
					],
					resources: [
						`${fapiBucketArn}/DEV/*`, // object resource specified for s3:GetObject
						fapiBucketArn, // bucket resource specified for s3:ListBucket
					],
				}),
			],
			condition: {
				githubOrganisation: 'guardian',
				repositories: 'facia-scala-client:*',
			},
		});
	}
}
