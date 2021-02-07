import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SampleClient {

    public static void main(String[] theArgs) throws IOException {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");

        LoggingInterceptor loggingInterceptor = new LoggingInterceptor(false);
      //  client.registerInterceptor(loggingInterceptor);

        RequestStopWatchInterceptor requestStopWatchInterceptor = new RequestStopWatchInterceptor();
        client.registerInterceptor(requestStopWatchInterceptor);
        Path path = Paths.get("src", "main", "resources", "input.txt");
        List<String> searchList = getSearchName(path);
        System.out.println(searchList);
        CacheControlDirective cacheControlDirective = new CacheControlDirective();
        cacheControlDirective.setNoCache(false).setNoStore(false);
        SortSpec sortSpec = new SortSpec();
        sortSpec.setParamName("given");
        sortSpec.setOrder(SortOrderEnum.ASC);
        List<Long> result = new ArrayList<>();

        // Search for Patient resources
        for (int i = 0; i < 3; i++) {
            long totalDuration = 0;

            for (String param : searchList) {

                Bundle response;
                if (i == 2) {
                    cacheControlDirective.setNoCache(true).setNoStore(true);
                }
                response = client
                        .search()
                        .forResource("Patient")
                        .cacheControl(cacheControlDirective)
                        .where(Patient.FAMILY.matches().value(param))
                        .sort(sortSpec)
                        .returnBundle(Bundle.class)
                        .execute();


                long duration = requestStopWatchInterceptor.getResponseDuration();

                totalDuration += duration;

                printName(response, i, param);
            }
            result.add(totalDuration);
        }
        int tryouts = 0;
        for (long l : result) {
            System.out.println("try out = " + tryouts++ + " average response time = " + calculateAveResponseTime(l, searchList.size()) + " milliseconds");
        }

    }

    public static long calculateAveResponseTime(long total, int size) {
        if (size == 0)
            return 0;
        return total / size;
    }


    public static List<String> getSearchName(Path path ) throws IOException {
        List<String> searchList = new ArrayList<>();


        File file = new File(String.valueOf(path));

        if (!file.exists())
            System.out.println("file from path " + path + " not exists");
        else {
            searchList = Files.lines(path).map(e -> e.split("\\s+"))
                    .flatMap(array -> Arrays.stream(array)).collect(Collectors.toList());

        }
        return searchList;
    }

    private static void printName(Bundle response, int i, String param) {
        List<Resource> resourceList = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry : response.getEntry()) {
            resourceList.add(entry.getResource());
        }
        Patient patient;
        System.out.println("Beginning of try out " + +i + "  for family name " + param + " result : ********");
        for (Resource resource : resourceList) {
            if (resource instanceof Patient) {
                patient = (Patient) resource;

                System.out.println("Family name :" + patient.getNameFirstRep().getFamily()
                        + " Given name :" + patient.getNameFirstRep().getGivenAsSingleString()
                        + " Date of birth :" + patient.getBirthDate());


            }
        }
        System.out.println("End of try out " + i + " for family name " + param + " result : ********");
    }
}
