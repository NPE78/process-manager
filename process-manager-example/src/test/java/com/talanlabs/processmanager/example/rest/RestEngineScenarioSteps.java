package com.talanlabs.processmanager.example.rest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.ValidatableResponse;
import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.rest.RestAddon;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
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

    @Given("^rest engine is created$")
    public void restEngineIsCreated() throws Throwable {
        engine = PM.get().createEngine(getClass().getSimpleName(), TestUtils.getErrorPath());
    }

    @And("^rest addon is created and added to the engine$")
    public void restAddonIsCreatedAndAddedToTheEngine() {
        restAddon = RestAddon.register(engine.getUuid());
        restAddon.start(8080);
    }

    @And("^rest agents are created and registered$")
    public void agentIsCreatedAndRegistered() {
        agentGet = new MyRestAgent(true);
        agentGet.register(engine.getUuid(), 5);

        agentPost = new MyRestAgent(false);
        agentPost.register(engine.getUuid(), 5);
    }

    @And("^rest dispatcher is created and registered$")
    public void restDispatcherIsCreatedAndRegistered() {
        MyRestDispatcher myRestDispatcher = new MyRestDispatcher(agentGet, agentPost);
        restAddon.bindDispatcher(myRestDispatcher);
    }

    @And("^rest engine is initialized$")
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

    @And("^the content should be (\\w+)$")
    public void theContentShouldBe(String param) {
        validatableResponse.content(Matchers.is(param));
    }

    @Then("^shutdown the rest engine$")
    public void shutdownTheRestEngine() {
        PM.get().shutdownEngine(getClass().getSimpleName());
    }
}
