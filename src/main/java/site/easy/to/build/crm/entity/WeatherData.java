package site.easy.to.build.crm.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WeatherData {
    private Location location;
    private CurrentWeather current;

    // Getters and setters

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public CurrentWeather getCurrent() {
        return current;
    }

    public void setCurrent(CurrentWeather current) {
        this.current = current;
    }

    public static class Location {
        private String name;
        private String region;
        private String country;
        private double lat;
        private double lon;
        private String tz_id;
        private long localtime_epoch;
        private String localtime;

        // Getters and setters

        // Use @JsonProperty annotation to map the JSON keys to Java object properties

        @JsonProperty("name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JsonProperty("region")
        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        @JsonProperty("country")
        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        @JsonProperty("lat")
        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        @JsonProperty("lon")
        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        @JsonProperty("tz_id")
        public String getTzId() {
            return tz_id;
        }

        public void setTzId(String tzId) {
            this.tz_id = tzId;
        }

        @JsonProperty("localtime_epoch")
        public long getLocaltimeEpoch() {
            return localtime_epoch;
        }

        public void setLocaltimeEpoch(long localtimeEpoch) {
            this.localtime_epoch = localtimeEpoch;
        }

        @JsonProperty("localtime")
        public String getLocaltime() {
            return localtime;
        }

        public void setLocaltime(String localtime) {
            this.localtime = localtime;
        }
    }

    public static class CurrentWeather {
        private long last_updated_epoch;
        private String last_updated;
        private double temp_c;
        private double temp_f;
        private int is_day;
        private String condition;
        private double wind_mph;
        private double wind_kph;
        private int wind_degree;
        private String wind_dir;

        // Getters and setters

        // Use @JsonProperty annotation to map the JSON keys to Java object properties

        @JsonProperty("last_updated_epoch")
        public long getLastUpdatedEpoch() {
            return last_updated_epoch;
        }

        public void setLastUpdatedEpoch(long lastUpdatedEpoch) {
            this.last_updated_epoch = lastUpdatedEpoch;
        }

        @JsonProperty("last_updated")
        public String getLastUpdated() {
            return last_updated;
        }

        public void setLastUpdated(String lastUpdated) {
            this.last_updated = lastUpdated;
        }

        @JsonProperty("temp_c")
        public double getTempC() {
            return temp_c;
        }

        public void setTempC(double tempC) {
            this.temp_c = tempC;
        }

        @JsonProperty("temp_f")
        public double getTempF() {
            return temp_f;
        }

        public void setTempF(double tempF) {
            this.temp_f = tempF;
        }

        @JsonProperty("is_day")
        public int getIsDay() {
            return is_day;
        }

        public void setIsDay(int isDay) {
            this.is_day = isDay;
        }


        @JsonProperty("wind_mph")
        public double getWindMph() {
            return wind_mph;
        }

        public void setWindMph(double windMph) {
            this.wind_mph = windMph;
        }

        @JsonProperty("wind_kph")
        public double getWindKph() {
            return wind_kph;
        }

    }
}