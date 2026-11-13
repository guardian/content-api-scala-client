import 'source-map-support/register';
import { App } from 'aws-cdk-lib';
import { FaciaScalaClientTesting } from '../lib/facia-scala-client-testing';

const app = new App();
new FaciaScalaClientTesting(app, 'FaciaScalaClientTesting-INFRA', {
	stack: 'facia-scala-client',
	stage: 'INFRA',
});
