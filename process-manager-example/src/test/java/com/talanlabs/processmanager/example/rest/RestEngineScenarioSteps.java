package com.talanlabs.processmanager.example.rest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;
import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.rest.IRestDispatcher;
import com.talanlabs.processmanager.rest.RestAddon;
import com.talanlabs.processmanager.rest.RestDispatcherBuilder;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.hamcrest.Matchers;

public class RestEngineScenarioSteps {

    private Engine engine;
    private RestAddon restAddon;
    private MyRestAgent agentGet; // synchronous
    private MyRestAgent agentPost; // asynchronous

    private ValidatableResponse validatableResponse;

    @Given("^REST engine is created$")
    public void restEngineIsCreated() throws Throwable {
        engine = PM.createEngine(getClass().getSimpleName(), TestUtils.getErrorPath());
    }

    @And("^REST addon is created and added to the engine$")
    public void restAddonIsCreatedAndAddedToTheEngine() {
        restAddon = RestAddon.register(engine.getUuid());
        restAddon.start(8080);
    }

    @And("^REST agents are created and registered$")
    public void agentIsCreatedAndRegistered() {
        agentGet = new MyRestAgent(true);
        agentGet.register(engine.getUuid(), 5);

        agentPost = new MyRestAgent(false);
        agentPost.register(engine.getUuid(), 5);
    }

    @And("^REST dispatcher is created and registered$")
    public void restDispatcherIsCreatedAndRegistered() {
        RestDispatcherBuilder builder = new RestDispatcherBuilder();
        IRestDispatcher restDispatcher = builder.get(agentGet).post(agentPost).build("rest");
        restAddon.bindDispatcher(restDispatcher);
    }

    @And("^REST engine is initialized$")
    public void restEngineIsInitialized() {
        engine.activateChannels();
    }

    @When("^a synchronous url is called with (\\w+) param$")
    public void aSynchronousUrlIsCalled(String param) {
        validatableResponse = RestAssured.given().when().get("http://localhost:8080/rest?" + param).then();
    }

    @When("^an asynchronous url is called$")
    public void anAsynchronousUrlIsCalled() {
        validatableResponse = RestAssured.given().when().post("http://localhost:8080/rest").then();
    }

    @Then("^the status should be (\\d+)$")
    public void theStatusShouldBe(final int expectedStatus) {
        validatableResponse.statusCode(expectedStatus);
    }

    @And("^the content should be (.*)$")
    public void theContentShouldBe(String param) {
        validatableResponse.content(Matchers.is(param));
    }

    @Given("^A dispatcher contains an agent for every method$")
    public void aDispatcherContainsAnAgentForEveryMethod() {
        RestDispatcherBuilder builder = new RestDispatcherBuilder();
        MyRestAgent agent = new MyRestAgent(false);
        agent.register(getClass().getSimpleName(), 1);
        builder.get(agent).post(agent).put(agent).patch(agent).delete(agent);
        restAddon.bindDispatcher(builder.build("rest"));
    }

    @When("^an asynchronous url is called with (\\w+)$")
    public void anAsynchronousUrlIsCalledWithMethod(String method) {
        RequestSpecification when = RestAssured.given().when();
        String path = "http://localhost:8080/rest";
        Response response = null;
        switch (method) {
            case "get":
                response = when.get(path);
                break;
            case "post":
                response = when.post(path);
                break;
            case "put":
                response = when.put(path);
                break;
            case "patch":
                response = when.patch(path);
                break;
            case "delete":
                response = when.delete(path);
                break;
        }
        validatableResponse = response.then();
    }

    @After
    public void after() {
        PM.shutdownEngine(getClass().getSimpleName());
    }
}
