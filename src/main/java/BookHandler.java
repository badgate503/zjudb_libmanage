import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Book;
import entities.Borrow;
import queries.ApiResult;
import queries.BookQueryConditions;
import queries.BookQueryResults;
import queries.SortOrder;
import utils.RandomData;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BookHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
        String requestMethod = exchange.getRequestMethod();
        ConsoleColor.logs("BookHANDLE",requestMethod+" "+exchange.getRequestURI(),"IFO");
        if (requestMethod.equals("GET")) {
            handleGetRequest(exchange);
        } else if (requestMethod.equals("POST")) {
            handlePostRequest(exchange);
        } else if (requestMethod.equals("OPTIONS")) {
            handleOPTRequest(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        try {


            ConsoleColor.logs("BookGET","Got your request GET "+exchange.getRequestURI()+" Working on'em","IFO");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            String queryType = exchange.getRequestURI().getQuery().split("=")[1];
            ConsoleColor.logs("HandleGet",queryType,"IFO");
            String json = "";

            if (queryType.equals("category")) {

                ApiResult rs = Main.library.fetchCategory();
                json = JSONObject.toJSONString(rs.payload);

            } else if(queryType.equals("allbook")){
                BookQueryConditions bqc = new BookQueryConditions();
                BookQueryResults qRes = (BookQueryResults) Main.library.queryBook(bqc).payload;
                json = JSONObject.toJSONString(qRes.getResults());
            }else if(queryType.equals("year")){
                String qRes = (String) Main.library.fetchMinMaxYear().payload;
                json = JSONObject.toJSONString(qRes.split("-"));
            }else {

                ApiResult rs = Main.library.showBorrowHistory(Integer.parseInt(queryType));
                json = JSONObject.toJSONString(rs);
            }
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, json.getBytes().length);
            exchange.getResponseBody().write(json.getBytes(StandardCharsets.UTF_8));
            exchange.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        try{
            ConsoleColor.logs("BookPOST","Got your request POST "+exchange.getRequestURI()+" Working on'em","IFO");
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
            String getJson = "";
            ConsoleColor.logs("BookPOST","The JSON is: "+pjson,"IFO");
            if(requestType.equals("file")){
                int startJson = pjson.indexOf("{");
                int endJson = pjson.indexOf("}");
                int _startJson = pjson.indexOf("[");
                int _endJson = pjson.indexOf("]");
                if(startJson < _startJson && endJson > _endJson){
                    getJson = pjson.substring(startJson,endJson+1);
                }
                else{
                    getJson = pjson.substring(_startJson,_endJson+1);
                }
            }else {
                getJson = pjson;
            }
            JSONObject payLoad = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            ConsoleColor.logs("BookPOST","The JSON is: "+getJson,"IFO");
            if(requestType.equals("file")){
                try{
                    jsonArray = JSONArray.parseArray(getJson);
                } catch (Exception e){
                    requestType="";
                    ConsoleColor.logs("BookPOST","Wrong File format. ","ERR");
                    ApiResult rs = new ApiResult(false, "wrongFormat");
                    responseJson = JSONObject.toJSONString(rs);
                }

            } else {
                payLoad = JSON.parseObject(getJson);
            }


            ConsoleColor.logs("BookPOST", requestType,"IFO");
            if(requestType.equals("query")){
                BookQueryConditions bqc = new BookQueryConditions();
                boolean queryPrice  = payLoad.getBoolean("queryPrice");
                bqc.setAuthor(payLoad.getString("author"));
                bqc.setTitle(payLoad.getString("title"));
                bqc.setCategory(payLoad.getString("category"));
                bqc.setMinPublishYear(payLoad.getInteger("minPublishYear"));
                bqc.setMaxPublishYear(payLoad.getInteger("maxPublishYear"));
                if(queryPrice){
                    bqc.setMinPrice(payLoad.getDouble("minPrice"));
                    bqc.setMaxPrice(payLoad.getDouble("maxPrice"));
                }
                bqc.setPress(payLoad.getString("press"));
                bqc.setSortBy(Book.SortColumn.valueOf(payLoad.getString("sortBy")));
                bqc.setSortOrder(SortOrder.valueOf(payLoad.getString("sortOrder")));
                BookQueryResults qRes = (BookQueryResults) Main.library.queryBook(bqc).payload;
                responseJson = JSONObject.toJSONString(qRes.getResults());
            }
            else if(requestType.equals("add")){
                Book b = new Book();
                b.setAuthor(payLoad.getString("author"));
                b.setTitle(payLoad.getString("title"));
                b.setCategory(payLoad.getString("category"));
                b.setStock(payLoad.getInteger("stock"));
                b.setPrice(payLoad.getDouble("price"));
                b.setPress(payLoad.getString("press"));
                b.setPublishYear(payLoad.getInteger("publishYear"));
                ApiResult rs = Main.library.storeBook(b);
                responseJson = JSONObject.toJSONString(rs);
            }else if(requestType.equals("delete")){
                int bookId = payLoad.getInteger("book_id");
                ApiResult rs = Main.library.removeBook(bookId);
                responseJson = JSONObject.toJSONString(rs);
            }else if(requestType.equals("edit")){
                Book edited = new Book();
                edited.setAuthor(payLoad.getString("author"));
                edited.setTitle(payLoad.getString("title"));
                edited.setCategory(payLoad.getString("category"));
                edited.setPublishYear(payLoad.getInteger("publishYear"));
                edited.setPrice(payLoad.getDouble("price"));
                edited.setPress(payLoad.getString("press"));
                edited.setBookId(payLoad.getInteger("book_id"));
                ConsoleColor.logs("BookPOST::edit", edited.toString(),"IFO");

                ApiResult rs = Main.library.modifyBookInfo(edited);
                responseJson = JSONObject.toJSONString(rs);
            } else if(requestType.equals("stock")){
                int bookId = payLoad.getInteger("book_id");
                int deltaStock = payLoad.getInteger("deltaStock");
                ApiResult rs = Main.library.incBookStock(bookId, deltaStock);
                responseJson = JSONObject.toJSONString(rs);
            } else if(requestType.equals("file")){
                try{
                    ConsoleColor.logs("FilePOST",jsonArray.toString(),"IFO");
                    Set<Book> bookSet = new HashSet<>();
                    for(int i = 0; i < jsonArray.size(); i++){
                        Book book = new Book();
                        JSONObject jsob = jsonArray.getJSONObject(i);
                        book.setAuthor(jsob.getString("author"));
                        book.setTitle(jsob.getString("title"));
                        book.setCategory(jsob.getString("category"));
                        book.setStock(jsob.getInteger("stock"));
                        book.setPrice(jsob.getDouble("price"));
                        book.setPress(jsob.getString("press"));
                        book.setPublishYear(jsob.getInteger("publish_year"));
                        ConsoleColor.logs("BookPOST::file","New Book in list: "+book.toString(),"IFO");
                        bookSet.add(book);
                    }
                    List<Book> bookList = new ArrayList<>(bookSet);
                    ApiResult rs = Main.library.storeBook(bookList);
                    rs.payload = bookList.size();
                    responseJson = JSONObject.toJSONString(rs);
                } catch (Exception e){
                    ApiResult rs = new ApiResult(false, "wrongFormat");
                    responseJson = JSONObject.toJSONString(rs);
                }

            } else if(requestType.equals("borrow")){
                Borrow borrow = new Borrow();
                borrow.setBookId(payLoad.getInteger("book_id"));
                borrow.setCardId(payLoad.getInteger("card_id"));
                borrow.setBorrowTime(System.currentTimeMillis());
                borrow.setReturnTime(0);
                ApiResult rs = Main.library.borrowBook(borrow);
                responseJson = JSONObject.toJSONString(rs);
            } else if(requestType.equals("return")){
                Borrow borrow = new Borrow();
                borrow.setBookId(payLoad.getInteger("book_id"));
                borrow.setCardId(payLoad.getInteger("card_id"));
                borrow.setBorrowTime(payLoad.getLong("borrowTime"));
                borrow.setReturnTime(System.currentTimeMillis());
                ApiResult rs = Main.library.returnBook(borrow);
                responseJson = JSONObject.toJSONString(rs);
            }


            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseJson.getBytes().length);
            exchange.getResponseBody().write(responseJson.getBytes(StandardCharsets.UTF_8));
            exchange.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    private void handleOPTRequest(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, -1);
    }



}
