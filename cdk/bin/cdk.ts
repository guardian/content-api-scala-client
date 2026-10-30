import "source-map-support/register";
import { GuRoot } from "@guardian/cdk/lib/constructs/root";
import { ContentApiClientsTesting } from "../lib/content-api-clients-testing";
import { FaciaScalaClientTesting } from '../lib/facia-scala-client-testing';

const app = new GuRoot();
new ContentApiClientsTesting(app, "ContentApiFirehoseClientTesting-euwest-1-INFRA", { stack: "content-api-firehose-client", stage: "INFRA", env: { region: "eu-west-1" } });

new FaciaScalaClientTesting(app, 'FaciaScalaClientTesting-INFRA', {
	stack: 'facia-scala-client',
	stage: 'INFRA',
});