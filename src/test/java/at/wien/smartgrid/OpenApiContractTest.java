package at.wien.smartgrid;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class OpenApiContractTest {

    private static final Path CONTRACT = Path.of(
            "src", "main", "resources", "static", "openapi", "smart-grid-api.yaml");

    @Test
    void versionedContractIsValidAndCoversEveryPublicResource() {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        var result = new OpenAPIV3Parser().readLocation(CONTRACT.toString(), null, options);

        assertThat(result.getMessages()).isEmpty();
        assertThat(result.getOpenAPI()).isNotNull();
        assertThat(result.getOpenAPI().getOpenapi()).startsWith("3.1");
        assertThat(result.getOpenAPI().getPaths().keySet()).containsExactlyInAnyOrder(
                "/api/v1/readings/ingest",
                "/api/v1/meters",
                "/api/v1/meters/{meterId}/status",
                "/api/v1/analytics/grid-load",
                "/api/v1/analytics/grid-areas",
                "/api/v1/analytics/anomalies");
    }
}
