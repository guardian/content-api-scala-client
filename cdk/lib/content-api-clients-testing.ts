import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import { GuGithubActionsRole } from '@guardian/cdk/lib/constructs/iam';
import { GuAllowPolicy } from '@guardian/cdk/lib/constructs/iam/policies/base-policy';
import type { App } from 'aws-cdk-lib';

export class ContentApiClientsTesting extends GuStack {
	constructor(scope: App, id: string, props: GuStackProps) {
		super(scope, id, props);
		new GuGithubActionsRole(this, {
			policies: [
				new GuAllowPolicy(this, 'kinesis-stream-access', {
					actions: ['kinesis:DescribeStreamSummary'],
					resources: [
						`arn:aws:kinesis:${this.region}:${this.account}:stream/content-api-firehose-v2-CODE`,
					],
				}),
			],
			condition: {
				githubOrganisation: 'guardian',
				repositories: 'content-api-scala-client:*',
			},
		});
	}
}
