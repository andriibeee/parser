package org.example.helper;

import org.example.exception.CannotLoadDataException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DataLoadHelper {

    public static String loadData(String urlStr) throws CannotLoadDataException {
        return dataResult(getURLConnection(urlStr));
    }

    private static URLConnection getURLConnection(String urlStr) throws CannotLoadDataException {
        URLConnection urlConnection;
        try {
            URL url = new URL(urlStr);
            urlConnection = url.openConnection();
            setProperty(urlConnection);
        } catch (IOException e) {
           throw new CannotLoadDataException("Error load data by url: " + urlStr);
        }
        return urlConnection;
    }

    private static String dataResult(URLConnection urlConnection) throws CannotLoadDataException {
        StringBuilder result;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String inputLine;
            result = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
        } catch (IOException e) {
            throw new CannotLoadDataException("Error read data from url: " + urlConnection.getURL());
        }
        return result.toString();
    }

    private static void setProperty(URLConnection urlConnection) {
        requestProperty().forEach(urlConnection::setRequestProperty);
    }

    private static Map<String, String> requestProperty() {
        Map<String, String> property = new HashMap<>();
        property.put("Cookie", "ABTestSeed=59; " +
                               "ipfrom=176.32.0.66; " +
                               "x-app-language=ru_UA; " +
                               "theme=DARK; " +
                               "_ga=GA1.1.559474088.1666289937; " +
                               "firstTheme=DARK; " +
                               "adformfrpid=3183155680372694324; " +
                               "referer=https://leon.bet/ru/bets; " +
                               "qtag_rfrr=null-null; " +
                               "_sp_srt_id.1e54=ce3b2695-0e76-4a8c-9e73-5810a4052429.1666289937.6.1666354486.1666351387.e16782e7-b9ab-4d75-94ec-051aa7a50b87; " +
                               "_ga_JZZNGY93CC=GS1.1.1666354471.6.1.1666354485.0.0.0" );
        return property;
    }
}
