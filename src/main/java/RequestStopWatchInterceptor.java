import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class RequestStopWatchInterceptor implements IClientInterceptor {

    private int count;
    private LocalTime requestStopWatch;
    private long responseDuration;
    private boolean cacheControl = true;
    public RequestStopWatchInterceptor( ) {

    }
    @Override
    public void interceptRequest(IHttpRequest iHttpRequest) {
            requestStopWatch = LocalTime.now();
            Map<String, List<String>> map =  iHttpRequest.getAllHeaders();
            List<String> list =  map.get("Cache-Control");
            setCacheControl(list);
     }

    @Override
    public void interceptResponse(IHttpResponse iHttpResponse)  {
        setRequestStopWatch();
    }

    public boolean isCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(List<String> list) {
        cacheControl = list == null;
    }

    public long getResponseDuration() {
        return responseDuration;
    }

    public LocalTime getRequestStopWatch() {
        return requestStopWatch;
    }
    public void setResponseDuration(long t) {
        responseDuration = t;
    }

    private void setRequestStopWatch() {
        Duration duration = Duration.between(requestStopWatch, LocalTime.now());
        setResponseDuration(duration.getNano() / 1000000);
        requestStopWatch = LocalTime.now();
    }

}
