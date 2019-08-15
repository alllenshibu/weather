/**
 * A command-line Weather App built using OpenWeatherMap API.
 * URL for API: http://api.openweathermap.org/data/2.5/weather
 *
 * @author Allen Shibu Kanjookaran
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.util.Date;
import java.util.Scanner;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.URLConnection;

import java.text.SimpleDateFormat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Weather {
    private static final String OPEN_WEATHER_MAP_API_URL = "http://api.openweathermap.org/data/2.5/weather?";
    private static final File HTTP_RESPONSES_FILE = new File("./././resources/httpresponses.json");
    private static final File CREDENTIALS_FILE = new File("./././resources/credentials.json");
    private static final File WEATHER_CONDITIONS_FILE = new File("./././resources/conditions.json");
    private static final File DEFAULT_LOCATION_FILE = new File("./././resources/default.json");
    private static final String APPLICATION_NAME = "Open Weather Map API App";

    private static final SimpleDateFormat formatDate = new SimpleDateFormat("E, dd MMMM hh:mm a");
    private static final SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a");

    private static String apiKey = "null";

    private static String unitChoice = "metric";
    private static int zoom = 10, expectedCityNo = 10;

    private static Date localDate = new Date();

    private static int zipCode = 110001;
    private static double longitude = 77.1025,
            lattitude = 28.7041;
    private static double longitudeLeft = 0,
            lattitudeBottom = 0,
            longitudeRight = 0,
            lattitudeTop = 0;
    private static double centreLattitude = 28.7041,
            centreLongitude = 77.1025;

    private static JSONParser parser = new JSONParser();

    //All the variable names corespond to the value names in response JSON
    private static long dt = 0;
    private static String name = "Delhi", country = "IN";
    private static String sys_country = "";
    private static long clouds_all = 0,
            visibility = 0,
            timezone = 0,
            main_humidity = 0,
            main_pressure = 0,
            sys_sunrise = 0,
            sys_sunset = 0,
            wind_deg = 0,
            weather_id;
    private static int main_temp = 0,
            main_temp_min = 0,
            main_temp_max = 0,
            wind_speed = 0;
    private static String weather_main = "",
            weather_description = "";

    public static void main(String[] args) throws IOException, ParseException {
        System.out.println(APPLICATION_NAME);
        System.out.println();

        Object httpResponsesObject = parser.parse(new FileReader(HTTP_RESPONSES_FILE));
        JSONObject httpResponsesJSONObject = (JSONObject) httpResponsesObject;
        JSONArray httpResponses = (JSONArray) httpResponsesJSONObject.get("response");

        getCity();
        System.out.println("Please Wait...");
        System.out.println();
        apiKey = getApiKey();

        try {
            HttpURLConnection service = newWeatherService(apiKey, name, country);

            int httpResponseCode = service.getResponseCode();


            if (httpResponseCode == 200) {
                BufferedReader read = new BufferedReader(new InputStreamReader(service.getInputStream()));
                String inputLine, weatherInString = "";

                StringBuilder response = new StringBuilder();

                while ((inputLine = read.readLine()) != null) {
                    response.append(inputLine);
                    weatherInString = weatherInString + response.toString();
                }
                read.close();

                JSONObject weather = (JSONObject) parser.parse(weatherInString);

                writeToTempFile(weather.toString());

                displayWeatherDetails(weather);

            } else if (httpResponseCode == 401) {
                System.out.println("401 Error");
            }
        } catch (UnknownHostException e) {
            System.out.println(e);
        } catch (SocketException e) {
            System.out.println(e);
        } catch (SocketTimeoutException e) {
            System.out.println(e);
        }

    }

    /**
     * Function to input the location
     */
    private static void getCity() throws FileNotFoundException, ParseException, IOException {
        Scanner readConsole = new Scanner(System.in);

        System.out.println("Enter city name or press enter to use deafult location: ");
        name = readConsole.nextLine();
        if (name.equalsIgnoreCase("")) {
            JSONObject defaultLocation = (JSONObject) parser.parse(new FileReader(DEFAULT_LOCATION_FILE));
            name = defaultLocation.get("name").toString();
            country = defaultLocation.get("country").toString();
        } else {
            System.out.println("Enter country code: ");
            country = readConsole.next();
        }
        System.out.println();

    }

    /**
     * Function to get the API Key from /resources/credentials.json.
     *
     * @param credentials JSONObject to store the API Key.
     *                    <p>
     *                    Function will return a String containing the API Key.
     */
    private static String getApiKey() throws IOException, FileNotFoundException, ParseException {
        JSONObject credentials = (JSONObject) parser.parse(new FileReader(CREDENTIALS_FILE));
        return credentials.get("apikey").toString();
    }

    /**
     * Function to create a new HttpURLConnection object using the API Key,
     * name and country codes.
     *
     * @param url        URL for the API query with API Key, name and country code attached.
     * @param connection HttpURLConnection for the API.
     *                   <p>
     *                   Funtion will return a HttpURLConnection object.
     */
    private static HttpURLConnection newWeatherService(String apiKey, String name, String country) throws IOException, MalformedURLException {
        URL url = new URL(OPEN_WEATHER_MAP_API_URL + "appid=" + apiKey + "&" + "units=" + unitChoice + "&" + "q=" + name + "," + country);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setInstanceFollowRedirects(true);
        connection.setDoOutput(true);

        return connection;
    }

    /**
     * Function to create a new HttpURLConnection object using the API Key,
     * lattitude and longitude.
     *
     * @param url        URL for the API query with API Key, lattitude and longitude attached.
     * @param connection HttpURLConnection for the API.
     *                   <p>
     *                   Funtion will return a HttpURLConnection object.
     */
    private static HttpURLConnection newWeatherService(String apiKey, double lattitude, double longitude) throws IOException, MalformedURLException {
        URL url = new URL(OPEN_WEATHER_MAP_API_URL + "appid=" + apiKey + "&" + "units=" + unitChoice + "&" + "lat=" + lattitude + "&" + "lon=" + longitude);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setInstanceFollowRedirects(true);
        connection.setDoOutput(true);

        return connection;
    }

    /**
     * Function to create a new HttpURLConnection object using the API Key,
     * zip code and country code.
     *
     * @param url        URL for the API query with API Key, zip code and country code attached.
     * @param connection HttpURLConnection for the API.
     *                   <p>
     *                   Funtion will return a HttpURLConnection object.
     */
    private static HttpURLConnection newWeatherService(String apiKey, int zipCode, String country) throws IOException, MalformedURLException {
        URL url = new URL(OPEN_WEATHER_MAP_API_URL + "appid=" + apiKey + "&" + "units=" + unitChoice + "&" + "zip=" + zipCode + "," + country);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setInstanceFollowRedirects(true);
        connection.setDoOutput(true);

        return connection;
    }

    /**
     * Function to create a new HttpURLConnection object for cities inside a box using the API Key,
     * left longitude, bottom lattitude, right longitude and top lattitude.
     *
     * @param url        URL for the API query with API Key, lon-left, lat-bottom, lon-right and lat-top attached.
     * @param connection HttpURLConnection for the API.
     *                   <p>
     *                   Funtion will return a HttpURLConnection object.
     */
    private static HttpURLConnection newWeatherService(String apiKey,
                                                       double longitudeLeft,
                                                       double lattitudeBottom,
                                                       double longitudeRight,
                                                       double lattitudeTop,
                                                       int zoom) throws IOException, MalformedURLException {
        URL url = new URL("http://api.openweathermap.org/data/2.5/box/city?" + "appid=" + apiKey + "&" + "units=" + unitChoice + "&" + "bbox=" + longitudeLeft + "," + lattitudeBottom + "," + longitudeRight + "," + lattitudeTop + "," + zoom);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setInstanceFollowRedirects(true);
        connection.setDoOutput(true);

        return connection;
    }

    /**
     * Write the received weather data to a json file called TempWeatherData.json.
     *
     * @param tempFile TempWeatherData.json file.
     */
    private static void writeToTempFile(String data) throws IOException {
        File tempFile = new File("./././temp/TempWeatherData.json");
        if (!(tempFile.exists())) {
            tempFile.createNewFile();
        }
        PrintWriter writeToTempFile = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));

        writeToTempFile.print(data);
        writeToTempFile.close();
    }

    /**
     * Function to display the weather appropriately.
     * All the variable names correspond to the value name in the response JSON.
     * <p>
     * Most of the variable assignments are surrounded by try-catch blocks because, the response JSON sometimes contains
     * long and sometimes contains double.
     *
     * @param weather JSON response from the API
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException
     */
    private static void displayWeatherDetails(JSONObject weather) throws FileNotFoundException, IOException, ParseException {
        name = (String) weather.get("name");
        sys_country = (String) ((JSONObject) weather.get("sys")).get("country");
        timezone = ((long) weather.get("timezone")) / 3600;
        dt = ((long) weather.get("dt")) * 1000;
        weather_main = (((JSONObject) ((JSONArray) weather.get("weather")).get(0)).get("main")).toString();
        //weather_description = (((JSONObject) ((JSONArray) weather.get("weather")).get(0)).get("description")).toString();
        weather_id = (long) (((JSONObject) ((JSONArray) weather.get("weather")).get(0)).get("id"));
        clouds_all = (long) ((JSONObject) weather.get("clouds")).get("all");
        try {
            main_temp = (int) ((double) ((JSONObject) weather.get("main")).get("temp"));
        } catch (ClassCastException e) {
            main_temp = (int) ((long) ((JSONObject) weather.get("main")).get("temp"));
        }
        try {
            main_temp_min = (int) ((double) ((JSONObject) weather.get("main")).get("temp_min"));
        } catch (ClassCastException e) {
            main_temp_min = (int) ((long) ((JSONObject) weather.get("main")).get("temp_min"));
        }
        try {
            main_temp_max = (int) ((double) ((JSONObject) weather.get("main")).get("temp_max"));
        } catch (ClassCastException e) {
            main_temp_max = (int) ((long) ((JSONObject) weather.get("main")).get("temp_max"));
        }
        try {
            main_humidity = (int) ((double) ((JSONObject) weather.get("main")).get("humidity"));
        } catch (ClassCastException e) {
            main_humidity = (int) ((long) ((JSONObject) weather.get("main")).get("humidity"));
        }
        try {
            main_pressure = (int) ((double) ((JSONObject) weather.get("main")).get("pressure"));
        } catch (ClassCastException e) {
            main_pressure = (int) ((long) ((JSONObject) weather.get("main")).get("pressure"));
        }
        //Multiplying by 18/5 to convert m/s to km/h
        try {
            wind_speed = ((int) ((double) ((JSONObject) weather.get("wind")).get("speed")) * (18 / 5));
        } catch (ClassCastException e) {
            wind_speed = ((int) ((long) ((JSONObject) weather.get("wind")).get("speed")) * (18 / 5));
        }
        try {
            wind_deg = (long) ((double) ((JSONObject) weather.get("wind")).get("deg"));
        } catch (ClassCastException e) {
            wind_deg = (long) ((JSONObject) weather.get("wind")).get("deg");
        }
        visibility = ((long) weather.get("visibility")) / 1000;
        //Multiplying by 1000 to convert seconds into milliseconds
        sys_sunrise = ((long) ((JSONObject) weather.get("sys")).get("sunrise")) * 1000;
        sys_sunset = ((long) ((JSONObject) weather.get("sys")).get("sunset")) * 1000;
        JSONArray condition = (JSONArray) ((JSONObject) parser.parse(new FileReader(WEATHER_CONDITIONS_FILE))).get("weather");
        for (int i = 0; i < 55; i++) {
            if (((long) ((JSONObject) condition.get(i)).get("id")) == weather_id) {
                weather_description = (String) (((JSONObject) condition.get(i)).get("description"));
            }
        }

        System.out.println("Current Weather Forecast for " + name + ", " + sys_country);
        System.out.println(formatDate.format(dt));
        System.out.println();
        System.out.println(weather_description);
        System.out.println();
        System.out.println("Cloudiness: " + clouds_all + "%");
        System.out.println("Temperature: " + main_temp + "°C");
        System.out.println("Minimum : " + main_temp_min + "°C");
        System.out.println("Maximum: " + main_temp_max + "°C");
        System.out.println("Humidity: " + main_humidity + "%");
        System.out.println("Pressure: " + main_pressure + " hPa");
        System.out.println();
        System.out.println("Sunrise: " + formatTime.format(sys_sunrise));
        System.out.println("Sunset: " + formatTime.format(sys_sunset));
        System.out.println();
        System.out.println("Visibility: " + visibility + " km");
        System.out.println("Wind Speed : " + wind_speed + " kmph");
        System.out.println("Direction: " + wind_deg + "°");
    }

    @Deprecated
    /**
     * This function is not used as it is not working as intended
     *
     * @param weather_id Unique weather condition ID from the JSON response
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException
     */
    private void displayWeatherDescription(long weather_id) throws FileNotFoundException, IOException, ParseException {
        JSONArray condition = (JSONArray) ((JSONObject) parser.parse(new FileReader(WEATHER_CONDITIONS_FILE))).get("weather");
        for (int i = 0; i < 57; i++) {
            if (((long) ((JSONObject) condition.get(i)).get("id")) == this.weather_id) {
                System.out.println(((JSONObject) condition.get(i)).get("description"));
            }
        }
    }
}