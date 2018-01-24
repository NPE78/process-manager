package com.talanlabs.processmanager.example.message;

import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.messages.gate.GateFactory;
import com.talanlabs.processmanager.messages.trigger.TriggerEngine;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MessageEngineScenarioSteps {

    private MyImportAgent agent;
    private File testFlux;

    private String expectedContent;
    private String obtainedContent;

    @Given("^message engine is created$")
    public void messageEngineIsCreated() throws Throwable {
        Engine engine = PM.get().createEngine(getClass().getSimpleName(), TestUtils.getErrorPath());
        engine.addAddon(TriggerEngine.register(engine.getUuid())); // adding twice to test it is not blocking
        engine.addAddon(GateFactory.register(engine.getUuid())); // adding twice to test it is not blocking
    }

    @And("^agent is created and registered$")
    public void agentIsCreatedAndRegistered() {
        agent = new MyImportAgent() {
            @Override
            public void doWork(MyFlux flux) {
                super.doWork(flux);
                try {
                    obtainedContent = flux.getContent();
                } catch (IOException e) {
                    Assertions.fail("This shouldn't happen");
                }
            }
        };

        agent.register(getClass().getSimpleName(), 5, 100, TestUtils.getErrorPath());
    }

    @And("^message engine is initialized$")
    public void engineIsInitialized() throws Throwable {
        Engine engine = PM.getEngine(getClass().getSimpleName());
        engine.activateChannels();

        TestUtils.sleep(50);
    }

    @When("^a valid file is received$")
    public void aValidFileIsReceived() throws Throwable {
        testFlux = new File(agent.getWorkDir(), "myTestFlux");
        expectedContent = "valid content";

        File file = createFile();
        Assertions.assertThat(file.renameTo(testFlux)).isTrue();

        TestUtils.sleep(400);
    }

    @When("^an invalid file is received$")
    public void anInvalidFileIsReceived() throws Throwable {
        testFlux = new File(agent.getWorkDir(), "myTestFlux");
        expectedContent = "invalid content";

        File file = createFile();
        Assertions.assertThat(file.renameTo(testFlux)).isTrue();

        TestUtils.sleep(400);
    }

    private File createFile() throws IOException {
        File file = File.createTempFile("myTest", "Flux");
        Assertions.assertThat(file).exists();
        try (FileWriter fw = new FileWriter(file)) {
            IOUtils.write(expectedContent, fw);
        }
        return file;
    }

    @Then("^the file should be in the accepted folder$")
    public void theFileShouldBeInTheAcceptedFolder() {
        File expectedFile = new File(agent.getAcceptedPath(), "myTestFlux");
        fileIsWhereItIsExpected(expectedFile);
    }

    @Then("^the file should be in the rejected folder$")
    public void theFileShouldBeInTheRejectedFolder() {
        File expectedFile = new File(agent.getRejectedPath(), "myTestFlux");
        fileIsWhereItIsExpected(expectedFile);
    }

    private void fileIsWhereItIsExpected(File expectedFile) {
        Assertions.assertThat(testFlux).doesNotExist();
        Assertions.assertThat(expectedFile).exists();
        Assertions.assertThat(obtainedContent).isEqualTo(expectedContent);
    }

    @After
    public void after() {
        PM.get().shutdownEngine(getClass().getSimpleName());
    }
}
