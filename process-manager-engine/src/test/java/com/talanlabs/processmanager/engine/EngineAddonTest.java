package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.TestUtils;
import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class EngineAddonTest {

    @Test
    public void testEngineAddon() throws BaseEngineCreationException {
        Checks checks = new Checks();

        String engineUuid = "testEngineAddon";
        Engine engine = PM.get().createEngine(engineUuid, TestUtils.getErrorPath());
        try {
            MyAddon addon = MyAddon.register(checks, engineUuid);
            addon.getLogService().info(() -> "Testing myAddon");

            Assertions.assertThat(addon.getEngineUuid()).isEqualTo(engineUuid);
            Assertions.assertThat(addon.getAddonClass()).isEqualTo(MyAddon.class);

            Checks checks2 = new Checks();
            MyAddon addon2 = MyAddon.register(checks2, engineUuid);
            Assertions.assertThat(checks2.disconnected).isTrue();

            Assertions.assertThat(addon == addon2).isTrue();
            Assertions.assertThat(engine.getAddon(MyAddon.class).get()).isEqualTo(addon);
        } finally {
            PM.get().shutdownEngine(engineUuid);
        }
        Assertions.assertThat(checks.disconnected).isTrue();
    }

    private static class MyAddon extends EngineAddon<MyAddon> {

        private final Checks checks;

        private MyAddon(Checks checks, String engineUuid) {
            super(MyAddon.class, engineUuid);

            this.checks = checks;
        }

        private static MyAddon register(Checks checks, String engineUuid) {
            return new MyAddon(checks, engineUuid).registerAddon();
        }

        @Override
        public void disconnectAddon() {
            checks.disconnected = true;
        }
    }

    private class Checks {
        public boolean disconnected;
    }
}
