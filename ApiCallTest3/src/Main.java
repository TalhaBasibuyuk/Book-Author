import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        try{
            Scanner scanner = new Scanner(System.in);
            String isbn;
            do {
                System.out.println("=========================================");
                System.out.print("ISBN number: ");
                isbn = scanner.nextLine();

                if(isbn.equalsIgnoreCase("No")) break;

                JSONObject bookData = getBookData(isbn);
                JSONObject volumeInfo = (JSONObject) bookData.get("volumeInfo");
                JSONArray authors = (JSONArray) volumeInfo.get("authors");
                String authorName = (String) authors.get(0);

                displayData(volumeInfo, authorName);

            }while (!isbn.equalsIgnoreCase("No"));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void displayData(JSONObject volumeInfo, String authorName) {
        String authorID = null;

        String searchName = authorName.replaceAll(" ", "%20");
        String urlString1 = "https://openlibrary.org/search/" +
                "authors.json?q=" + searchName;

        try {
            HttpURLConnection apiConnection = fetchApiResponse(urlString1);

            if (apiConnection.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to author API");
                throw new RuntimeException();
            }

            String jsonResponse = readApiResponse(apiConnection);

            JSONParser parser = new JSONParser();
            JSONObject resultsJson = (JSONObject) parser.parse(jsonResponse);

            JSONArray docs = (JSONArray) resultsJson.get("docs");
            JSONObject results = (JSONObject) docs.get(0);
            authorID = (String) results.get("key");


        } catch (Exception e) {
            e.printStackTrace();
        }

        String urlString2 = "https://openlibrary.org/authors/" +
                authorID +
                "/works.json?limit=10";

        String works = null;
        try {
            HttpURLConnection apiConnection = fetchApiResponse(urlString2);

            if (apiConnection.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to works API");
                throw new RuntimeException();
            }

            String jsonResponse = readApiResponse(apiConnection);

            JSONParser parser = new JSONParser();
            JSONObject resultsJson = (JSONObject) parser.parse(jsonResponse);

            JSONArray results = (JSONArray) resultsJson.get("entries");
            StringBuilder worksString = new StringBuilder();
            for (Object o : results) {
                JSONObject o1 = (JSONObject) o;
                worksString.append((String) o1.get("title")).append(", ");
            }

            works = worksString.substring(0, worksString.length() - 2);

        } catch (Exception e) {
            e.printStackTrace();
        }


        //volumeinfo -> title, author, pagecount, description, publishedDate, categories

        String title = (String) volumeInfo.get("title");
        String publishedDate = (String) volumeInfo.get("publishedDate");
        String description = (String) volumeInfo.get("description");
        Long pageCount = (Long) volumeInfo.get("pageCount");
        JSONArray categories1 = (JSONArray) volumeInfo.get("categories");
        StringBuilder categories2 = new StringBuilder();
        for (Object o : categories1) {
            categories2.append((String) o);
        }
        String categories = categories2.toString();

        System.out.println("Book Title: " + title);
        System.out.println("Author: " + authorName);
        System.out.println("Published Date: " + publishedDate);
        System.out.println("Categories: " + categories);
        System.out.println("Page Count: " + pageCount);
        System.out.println("Book Description: " + description);
        System.out.println("Author's Other Works: " + works);


    }

    private static JSONObject getBookData(String isbn){
        // https://www.googleapis.com/books/v1/volumes?q=isbn:9780261102422

        isbn = isbn.strip();

        String urlString = "https://www.googleapis.com/books/v1/volumes" +
         "?q=isbn:" + isbn;

        try {
            HttpURLConnection apiConnection = fetchApiResponse(urlString);

            if(apiConnection.getResponseCode() != 200){
                System.out.println("Error: Could not connect to API");
                return null;
            }

            String jsonResponse = readApiResponse(apiConnection);

            JSONParser parser = new JSONParser();
            JSONObject resultsJson = (JSONObject) parser.parse(jsonResponse);

            JSONArray foundBooks = (JSONArray) resultsJson.get("items");
            return (JSONObject) foundBooks.get(0);

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    private static HttpURLConnection fetchApiResponse(String urlString){
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String readApiResponse(HttpURLConnection apiConnection){
        try {

            StringBuilder resultsJson = new StringBuilder();

            Scanner scanner = new Scanner(apiConnection.getInputStream());

            while (scanner.hasNext()){
                resultsJson.append(scanner.nextLine());
            }

            scanner.close();

            return  resultsJson.toString();

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

}