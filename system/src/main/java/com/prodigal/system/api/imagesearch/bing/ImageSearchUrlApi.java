package com.prodigal.system.api.imagesearch.bing;

import java.net.*;
import java.util.*;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;

import com.google.gson.*;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: bing 根据图片搜索图片
 **/
public class ImageSearchUrlApi {
    static String subscriptionKey = "enter key here";
    static String host = "https://api.bing.microsoft.com";
    static String path = "/v7.0/images/search";
    static String searchTerm = "tropical ocean";
    public static void main(String[] args) throws IOException {
        // construct the search request URL (in the form of endpoint + query string)
        String searchQuery = "https://www.codefather.cn/logo.png";
        URL url = new URL(host + path + "?q=" +  URLEncoder.encode(searchQuery, "UTF-8"));
        HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);

        // receive JSON body
        InputStream stream = connection.getInputStream();
        String response = new Scanner(stream).useDelimiter("\\A").next();
        // construct result object for return
//        SearchResults results = new SearchResults(new HashMap<String, String>(), response);
//
//
//        // extract Bing-related HTTP headers
//        Map<String, List<String>> headers = connection.getHeaderFields();
//        for (String header : headers.keySet()) {
//            if (header == null) continue;      // may have null key
//            if (header.startsWith("BingAPIs-") || header.startsWith("X-MSEdge-")) {
//                results.relevantHeaders.put(header, headers.get(header).get(0));
//            }
//        }
//
//        stream.close();
//        JsonParser parser = new JsonParser();
//        JsonObject json = parser.parse(result.jsonResponse).getAsJsonObject();
//        //get the first image result from the JSON object, along with the total
//        //number of images returned by the Bing Image Search API.
//        String total = json.get("totalEstimatedMatches").getAsString();
//        JsonArray results = json.getAsJsonArray("value");
//        JsonObject first_result = (JsonObject)results.get(0);
//        String resultURL = first_result.get("thumbnailUrl").getAsString();
    }
}
