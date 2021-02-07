import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SampleClientTest {


    @Test
    public void getSearchNameWithFileExists() throws IOException {
        Path path = Paths.get("src", "test", "resources", "input.txt");
        List<String> execptedList = Arrays.asList("SMITH", "TANG", "WHITE");
        List<String> list = SampleClient.getSearchName(path);
        assertEquals(execptedList, list);
    }

    @Test
    public void getSearchNameWithFileNotExists() throws IOException {
        Path path = Paths.get("src", "test", "resources", "test.txt");
        List<String> execptedList = new ArrayList<>();
        List<String> list = SampleClient.getSearchName(path);
        assertEquals(execptedList, list);
    }

    @Test
    public void testRequestWithCache() {
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");

        RequestStopWatchInterceptor requestStopWatchInterceptor = new RequestStopWatchInterceptor();
        client.registerInterceptor(requestStopWatchInterceptor);
        CacheControlDirective cacheControlDirective = new CacheControlDirective();
        cacheControlDirective.setNoCache(false).setNoStore(false);
        client
                .search()
                .forResource("Patient")
                .cacheControl(cacheControlDirective)
                .where(Patient.FAMILY.matches().value("test"))
                .returnBundle(Bundle.class)
                .execute();

        boolean cacheControl = requestStopWatchInterceptor.isCacheControl();
        assertTrue(cacheControl);

    }

    @Test
    public void testRequestWithNoCache() {
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");

        RequestStopWatchInterceptor requestStopWatchInterceptor = new RequestStopWatchInterceptor();
        client.registerInterceptor(requestStopWatchInterceptor);
        CacheControlDirective cacheControlDirective = new CacheControlDirective();
        cacheControlDirective.setNoCache(true).setNoStore(true);
        client
                .search()
                .forResource("Patient")
                .cacheControl(cacheControlDirective)
                .where(Patient.FAMILY.matches().value("test"))
                .returnBundle(Bundle.class)
                .execute();

        boolean cacheControl = requestStopWatchInterceptor.isCacheControl();
        assertFalse(cacheControl);

    }

    @Test
    public void testCalculateNonZeroSize() {
        long total = 45000L;
        int size = 5;
        long expected = 45000L / 5;
        long result = SampleClient.calculateAveResponseTime(total, size);
        assertEquals(expected, result);
    }

    @Test
    public void testCalculateZeroSize() {
        long total = 45000L;
        int size = 0;
        long ave = 0;
        long result = SampleClient.calculateAveResponseTime(total, size);
        assertEquals(ave, result);
    }

    @Test
    public void testRequestStopWatch() {

        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");

        RequestStopWatchInterceptor requestStopWatchInterceptor = new RequestStopWatchInterceptor();
        client.registerInterceptor(requestStopWatchInterceptor);
        CacheControlDirective cacheControlDirective = new CacheControlDirective();
        cacheControlDirective.setNoCache(false).setNoStore(false);

        client
                .search()
                .forResource("Patient")
                .cacheControl(cacheControlDirective)
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();
        LocalTime callOnce = requestStopWatchInterceptor.getRequestStopWatch();
        client
                .search()
                .forResource("Patient")
                .cacheControl(cacheControlDirective)
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();
        LocalTime callAgain = requestStopWatchInterceptor.getRequestStopWatch();
        assertTrue(callOnce.isBefore(callAgain));
    }
}
