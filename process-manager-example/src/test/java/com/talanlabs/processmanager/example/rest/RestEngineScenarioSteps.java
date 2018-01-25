package com.talanlabs.processmanager.example.rest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.ValidatableResponse;
import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.rest.RestAddon;
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
        MyRestDispatcher myRestDispatcher = new MyRestDispatcher(agentGet, agentPost);
        restAddon.bindDispatcher(myRestDispatcher);
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

    @After
    public void after() {
        PM.shutdownEngine(getClass().getSimpleName());
    }
}
