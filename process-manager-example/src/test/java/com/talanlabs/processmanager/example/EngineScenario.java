package com.talanlabs.processmanager.example;

import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.shared.TestUtils;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
public class EngineScenario {

    @Given("^engine is created and initialized$")
    public void engineIsCreatedAndInitialized() throws Throwable {
        ProcessManager.getInstance().createEngine(getClass().getSimpleName(), TestUtils.getErrorPath());
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^a file is received$")
    public void aFileIsReceived() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^it should have been successfully integrated$")
    public void itShouldHaveBeenSuccessfullyIntegrated() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
