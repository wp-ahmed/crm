package site.easy.to.build.crm.service.weather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import site.easy.to.build.crm.entity.WeatherData;

@Service
public class WeatherService {
    private final RestTemplate restTemplate;
    private final String weatherApiUrl;
    private final String apiKey;

    public WeatherService(RestTemplate restTemplate,
                          @Value("${weather.api.url}") String weatherApiUrl,
                          @Value("${weather.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.weatherApiUrl = weatherApiUrl;
        this.apiKey = apiKey;
    }

    public WeatherData getWeatherData(String city) {
        String apiUrl = weatherApiUrl + "?key=" + apiKey + "&q=" + city;
        try {
            return restTemplate.getForObject(apiUrl, WeatherData.class);
        }catch (RuntimeException e) {
            return null;
        }
    }
}
