package com.talanlabs.processmanager.example;

import com.talanlabs.processmanager.engine.ProcessManager;
import com.talanlabs.processmanager.messages.gate.GateFactory;
import com.talanlabs.processmanager.messages.trigger.TriggerEngine;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;

public class EngineScenarioSteps {

    private MyFileAgent agent;
    private File testFlux;

    private String expectedContent;
    private String obtainedContent;

    @Given("^engine is created$")
    public void engineIsCreated() throws Throwable {
        Engine engine = ProcessManager.getInstance().createEngine(getClass().getSimpleName(), TestUtils.getErrorPath());
        engine.addAddon(TriggerEngine.register(engine.getUuid())); // adding twice to test it is not blocking
        engine.addAddon(GateFactory.register(engine.getUuid())); // adding twice to test it is not blocking
    }

    @And("^agent is created and registered$")
    public void agentIsCreatedAndRegistered() {
        agent = new MyFileAgent() {
            @Override
            public void doWork(MyFlux flux, String engineUuid) {
                super.doWork(flux, engineUuid);
                try {
                    obtainedContent = flux.getContent();
                } catch (IOException e) {
                    Assertions.fail("This shouldn't happen");
                }
            }
        };

        agent.register(getClass().getSimpleName(), 5, 100, TestUtils.getErrorPath());
    }

    @And("^engine is initialized$")
    public void engineIsInitialized() throws Throwable {
        Engine engine = ProcessManager.getEngine(getClass().getSimpleName());
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

    @Then("^shutdown the engine$")
    public void shutdownTheEngine() {
        ProcessManager.getInstance().shutdownEngine(getClass().getSimpleName());
    }
}
