import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { ContentApiClientsTesting } from "./content-api-clients-testing";
import { FaciaScalaClientTesting } from "./facia-scala-client-testing";

describe("The ContentApiClientsTesting stack", () => {
  it("matches the snapshot", () => {
    const app = new App();
    const stack = new ContentApiClientsTesting(app, "ContentApiClientsTesting", { stack: "content-api-clients", stage: "TEST" });
    const template = Template.fromStack(stack);
    expect(template.toJSON()).toMatchSnapshot();
  });
});

describe("The FaciaClientsTesting stack", () => {
  it("matches the snapshot", () => {
    const app = new App();
    const stack = new FaciaScalaClientTesting(app, "FaciaScalaClientTesting", { stack: "facia-scala-client", stage: "TEST" });
    const template = Template.fromStack(stack);
    expect(template.toJSON()).toMatchSnapshot();
  });
});