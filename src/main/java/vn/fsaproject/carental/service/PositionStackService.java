package vn.fsaproject.carental.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PositionStackService {
    @Value("${search.api.access-key}")
    private String AccessKey;
    @Value("${search.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public PositionStackService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    /**
     * Fetches latitude and longitude for a given address using the Positionstack API.
     *
     * @param address The address to geocode.
     * @return A map containing the geocoding response.
     */
//    public Map<String, Object>
}
