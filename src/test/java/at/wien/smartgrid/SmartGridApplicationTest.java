package at.wien.smartgrid;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmartGridApplicationTest {

    @Test
    void applicationEntryPointIsAvailable() {
        assertThat(SmartGridApplication.class).isNotNull();
    }
}
