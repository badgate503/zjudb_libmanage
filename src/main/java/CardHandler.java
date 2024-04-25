import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Api;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Book;
import entities.Card;
import queries.ApiResult;
import queries.BookQueryConditions;
import queries.BookQueryResults;
import queries.SortOrder;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
        String requestMethod = exchange.getRequestMethod();
        ConsoleColor.logs("CardHANDLE",requestMethod+" "+exchange.getRequestURI(),"IFO");
        if (requestMethod.equals("GET")) {
            handleGetRequest(exchange);
        } else if (requestMethod.equals("POST")) {
            handlePostRequest(exchange);
        } else if (requestMethod.equals("OPTIONS")) {
            handleOptionsRequest(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }



    private void handlePostRequest(HttpExchange exchange) throws IOException {
        try{
            ConsoleColor.logs("CardPOST","Got your request POST "+exchange.getRequestURI()+" Working on'em","IFO");
            InputStream requestBody = exchange.getRequestBody();
            String requestType = exchange.getRequestURI().getQuery().split("=")[1];
            String responseJson = "";

            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
            StringBuilder requestBodyBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBodyBuilder.append(line);
            }
            String pjson = requestBodyBuilder.toString();
            JSONObject payLoad = JSONObject.parseObject(pjson);

            ConsoleColor.logs("CardPOST", requestType,"IFO");
            if(requestType.equals("new")){
               Card newCard = new Card();
               newCard.setName(payLoad.getString("name"));
               newCard.setDepartment(payLoad.getString("department"));
               String type = payLoad.getString("type");
               ConsoleColor.logs("CardPOST", type,"IFO");
               if(type.equals("学生")){
                   type="S";
               } else if(type.equals("教师")) {
                   type="T";
               } else {
                   throw new Exception();
               }
               newCard.setType(Card.CardType.values(type));
               ApiResult rs = Main.library.registerCard(newCard);
               responseJson = JSON.toJSONString(rs);
            }
            else if(requestType.equals("remove")){
                ApiResult rs = Main.library.removeCard(payLoad.getInteger("card_id"));
                responseJson = JSON.toJSONString(rs);
            }else if(requestType.equals("modify")){
                Card card = new Card();
                card.setName(payLoad.getString("name"));
                card.setDepartment(payLoad.getString("department"));
                String type = payLoad.getString("type");
                if(type.equals("学生")){
                    type="S";
                } else if(type.equals("教师")) {
                    type="T";
                } else {
                    throw new Exception();
                }
                card.setType(Card.CardType.values(type));
                ApiResult rs = Main.library.modifyCard(payLoad.getInteger("card_id"), card);
                responseJson = JSON.toJSONString(rs);
            }else if(requestType.equals("edit")){

            } else if(requestType.equals("stock")){

            }
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseJson.getBytes().length);
            exchange.getResponseBody().write(responseJson.getBytes(StandardCharsets.UTF_8));
            exchange.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleOptionsRequest(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, -1);
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        try {
            ConsoleColor.logs("CardGET","Got your request GET "+exchange.getRequestURI()+" Working on'em","IFO");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            String queryType = exchange.getRequestURI().getQuery().split("=")[1];
            ConsoleColor.logs("HandleGet",queryType,"IFO");
            String json = "";
            if (queryType.equals("all")) {
                ApiResult rs = Main.library.showCards();
                json = JSONObject.toJSONString(rs.payload);
            }
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, json.getBytes().length);
            exchange.getResponseBody().write(json.getBytes(StandardCharsets.UTF_8));
            exchange.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}