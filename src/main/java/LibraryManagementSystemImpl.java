import entities.Book;
import entities.Borrow;
import entities.Card;
import queries.*;
import utils.DBInitializer;
import utils.DatabaseConnector;

import java.sql.SQLException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class LibraryManagementSystemImpl implements LibraryManagementSystem {

    private final DatabaseConnector connector;

    public LibraryManagementSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }
    public boolean enableLog = true;


    @Override
    public ApiResult fetchMinMaxYear() {
        Connection conn = connector.getConn();
        try{
            String sqlmax = "SELECT max(publish_year) from book";
            String sqlmin = "SELECT min(publish_year) from book";
            Statement stmtmax = conn.createStatement();
            Statement stmtmin = conn.createStatement();
            ResultSet rsmax = stmtmax.executeQuery(sqlmax);
            ResultSet rsmin = stmtmin.executeQuery(sqlmin);
            int max=0, min=0;
            if(rsmax.next())
                max = rsmax.getInt(1);
            if(rsmin.next())
                min = rsmin.getInt(1);
            ConsoleColor.logs("FetchMinMaxYear", "Success: max year: "+max+", min year: "+min, "SUC");
            return new ApiResult(true, "Success", min+"-"+max);
        } catch (SQLException e){
            ConsoleColor.logs("FetchMinMaxYear","Failed: Unable to fetch min and max year. SQLException: "+e.getMessage(),"ERR");
            return new ApiResult(false, "Failed: Exception");
        }
    }

    @Override
    public ApiResult fetchCategory() {
        Connection conn = connector.getConn();
        try{
            String sql = "SELECT DISTINCT category FROM book";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<String> cateRes = new ArrayList<>();
            while (rs.next()) {
                cateRes.add(rs.getString("category"));
            }
            ConsoleColor.logs("FetchCategory", "Success: Fetched "+cateRes.size()+" categories", "SUC");
            return new ApiResult(true, "Success", cateRes);
        }catch(SQLException e){
            ConsoleColor.logs("FetchCategory","Failed: Unable to fetch category. SQLException: "+e.getMessage(),"ERR");
            return new ApiResult(false, "Failed: Exception");
        }

    }

    @Override
    public ApiResult _storeBook(Book book, Boolean doCommit) {
        Connection conn = connector.getConn();
        try {
            String sql1 = "select * from book where title = ? and press = ? and author = ? and publish_year =? and category = ?";
            PreparedStatement ps = conn.prepareStatement(sql1);
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getPress());
            ps.setString(3, book.getAuthor());
            ps.setInt(4, book.getPublishYear());
            ps.setString(5, book.getCategory());
            ResultSet rs1 = ps.executeQuery();
            if(rs1.next()) {
                if(enableLog) ConsoleColor.logs("StoreBook", "Failed: Unable to store book. Error: The book" + book.getTitle() +" already exists!", "ERR");
                return new ApiResult(false, "dupBook");
            }

            String sql = "insert into book (category, title, press, publish_year, author, price, stock) values (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ptmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ptmt.setString(1, book.getCategory());
            ptmt.setString(2, book.getTitle());
            ptmt.setString(3, book.getPress());
            ptmt.setInt(4, book.getPublishYear());
            ptmt.setString(5, book.getAuthor());
            ptmt.setDouble(6, book.getPrice());
            ptmt.setInt(7, book.getStock());
            ptmt.execute();
            if(enableLog) ConsoleColor.logs("StoreBook", book.getTitle() +" has been created", "SUC");
            ResultSet rs = ptmt.getGeneratedKeys();
            int id;
            if(rs.next()) {
                id = rs.getInt(1);
            } else {
                throw new SQLException();
            }
            book.setBookId(id);

            if(doCommit) commit(conn);
            return new ApiResult(true, "Success");
        }
        catch (SQLException e) {
            rollback(conn);
            if(enableLog) ConsoleColor.logs("StoreBook", "Failed: Unable to store book "+book.getTitle()+". SQLException: "+e.getMessage(), "SUC");
            return new ApiResult(false, "exception");
        }
    }

    @Override
    public ApiResult storeBook(Book book) {
        return _storeBook(book, true);
    }

    @Override
    public ApiResult incBookStock(int bookId, int deltaStock) {
        Connection conn = connector.getConn();

        String sql = "update book set stock = stock + ? where book_id = ?";
        String sql2 = "select stock from book where book_id = " + bookId;
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deltaStock);
            stmt.setInt(2, bookId);
            int updateVal = stmt.executeUpdate();

            if(updateVal == 0) {
                if(enableLog) ConsoleColor.logs("IncBookStock", "Failed: Unable to increment stock. Error: book id #"+bookId+" is invalid.", "ERR");
                return new ApiResult(false, "bookNotFound");
            }

            PreparedStatement stmt2 = conn.prepareStatement(sql2);
            ResultSet rs = stmt2.executeQuery();

            if(rs.next()) {
                if(rs.getInt("stock") < 0) {
                    if(enableLog) ConsoleColor.logs("IncBookStock", "Failed: Unable to increment stock. Error: stock must be greater or equal to 0.", "ERR");
                    rollback(conn);
                    return new ApiResult(false, "storeNeg");
                }
            }
            if(enableLog) ConsoleColor.logs("IncBookStock", "Stock in/decreased from "+(rs.getInt("stock")-deltaStock)+" to: "+rs.getInt("stock"), "SUC");
            commit(conn);
            return new ApiResult(true, "Success");
        }
        catch (SQLException e) {
            rollback(conn);
            if(enableLog) ConsoleColor.logs("IncBookStock", "Rolling back...", "IFO");
            if(enableLog) ConsoleColor.logs("IncBookStock", "Failed: Unable to increment stock. SQLException: "+e.getMessage(), "ERR");
            return new ApiResult(false, "exception");
        }

    }

    @Override
    public ApiResult storeBook(List<Book> books) {
        Connection conn = connector.getConn();
        try {
            conn.setAutoCommit(false);
            for(Book book : books) {
                ApiResult result = _storeBook(book, false);            /* Reuse the storeBook(Book) func */
                if(!result.ok) {                               /* If failed, the procedure must abort and roll back to nothing */
                    if(enableLog) ConsoleColor.logs("StoreBulkBook", "Failed: Unable to store the books. Error: "+book.getTitle() + " already exists! Storing process aborted.", "ERR");
                    rollback(conn);
                    return new ApiResult(false, "dupBook");
                }
            }
            if(enableLog) ConsoleColor.logs("StoreBulkBook", books.size()+" books stored successfully.", "SUC");
            commit(conn);
            return new ApiResult(true, "Success");
        }catch (SQLException e){
            rollback(conn);
            if(enableLog) ConsoleColor.logs("StoreBulkBook", "Rolling back...", "IFO");
            if(enableLog) ConsoleColor.logs("StoreBulkBook", "Failed: Unable to store the books. SQLException: "+e.getMessage(), "IFO");
            return new ApiResult(false, "exception");
        }

    }

    @Override
    public ApiResult removeBook(int bookId) {
        Connection conn = connector.getConn();
        String sql = "delete from book where book_id = ?";
        try{
            String sql1 = "select * from borrow where book_id = ?";
            PreparedStatement stmt1 = conn.prepareStatement(sql1);
            stmt1.setInt(1, bookId);
            ResultSet rs1 = stmt1.executeQuery();
            while(rs1.next()) {
                if(rs1.getLong("return_time")==0)
                {
                    if(enableLog) ConsoleColor.logs("RemoveBook","Failed: Unable to remove book #"+bookId+". Error: At least one book unreturned.", "ERR");
                    return new ApiResult(false, "bookNotReturn");
                }
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            int updateVal = stmt.executeUpdate();
            if(updateVal == 0){
                if(enableLog) ConsoleColor.logs("RemoveBook","Failed: Unable to remove book #"+bookId+". Error: Book notfound.", "ERR");
                return new ApiResult(false, "bookNotFound");
            }

            if(enableLog) ConsoleColor.logs("RemoveBook", "Delete #"+bookId+" Successfully", "SUC");
            commit(conn);
            return new ApiResult(true, "Success");
        } catch (SQLException e) {
            rollback(conn);
            if(enableLog) ConsoleColor.logs("RemoveBook", "Failed: Unable to remove book #"+bookId+". SQLException: "+e.getMessage(), "ERR");
            return new ApiResult(false, "exception");
        }
    }

    @Override
    public ApiResult modifyBookInfo(Book book) {
        Connection conn = connector.getConn();
        String sql = "select * from book where book_id = ?";
        String sql2 = "update book set category = ?, press = ?, author = ?, price = ?, title = ?, publish_year = ?  where book_id = ?";
        if(enableLog) ConsoleColor.logs("ModifyBookInfo", "Alter to:"+book.toString(), "IFO");
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, book.getBookId());
            ResultSet rs = stmt.executeQuery();
            if(!rs.next()) {
                if(enableLog) ConsoleColor.logs("ModifyBookInfo", "Failed: Unable to update book. Error: Given Book id #"+book.getBookId()+" no found.", "ERR");
                return new ApiResult(false, "bookNotFound");
            } else if (rs.getInt("stock") != book.getStock()) {
                if(enableLog) ConsoleColor.logs("ModifyBookInfo", "Not allowed to change stock of book via this func!", "WRN");
            }

            PreparedStatement stmt2 = conn.prepareStatement(sql2);
            stmt2.setString(1, book.getCategory());
            stmt2.setString(2, book.getPress());
            stmt2.setString(3, book.getAuthor());
            stmt2.setDouble(4, book.getPrice());
            stmt2.setString(5, book.getTitle());
            stmt2.setInt(6, book.getPublishYear());
            stmt2.setInt(7, book.getBookId());
            stmt2.executeUpdate();

            commit(conn);
            return new ApiResult(true, "Success");
        } catch (SQLException e){
            rollback(conn);
            if(enableLog) ConsoleColor.logs("ModifyBookInfo", "Failed: Unable to update book info of #"+book.getBookId()+". SQLException: "+e.getMessage(), "ERR");
            return new ApiResult(false, "exception");
        }

    }

    @Override
    public ApiResult queryBook(BookQueryConditions conditions) {
        try {
            Connection conn = connector.getConn();
            String sql = "select * from book";
            String query = " where ";
            boolean isPrevQuery = false;
            if(conditions.getCategory() != null && !conditions.getCategory().isEmpty()) {
                query += " category LIKE \"%" + conditions.getCategory()+"%\"";
                isPrevQuery = true;
            }
            if(conditions.getTitle() != null && !conditions.getTitle().isEmpty()) {
                if(isPrevQuery) query += " and ";
                query += "title LIKE \"%" + conditions.getTitle()+"%\"";
                isPrevQuery = true;
            }
            if(conditions.getPress() != null && !conditions.getPress().isEmpty()) {
                if(isPrevQuery) query += " and ";
                query += "press LIKE \"%" + conditions.getPress()+"%\"";
                isPrevQuery = true;
            }
            if(conditions.getMinPublishYear() != null) {
                if(isPrevQuery) query += " and ";
                query +=  "publish_year >= "+conditions.getMinPublishYear();
                isPrevQuery = true;
            }
            if(conditions.getMaxPublishYear() != null){
                if(isPrevQuery) query += " and ";
                query += "publish_year <= "+conditions.getMaxPublishYear();
                isPrevQuery = true;
            }
            if(conditions.getAuthor() != null && !conditions.getAuthor().isEmpty()) {
                if(isPrevQuery) query += " and ";
                query += "author LIKE \"%"+ conditions.getAuthor()+"%\"";
                isPrevQuery = true;
            }
            if(conditions.getMinPrice() != null) {
                if(isPrevQuery) query += " and ";
                query += "price >= "+conditions.getMinPrice();
                isPrevQuery = true;
            }
            if(conditions.getMaxPrice() != null) {
                if(isPrevQuery) query += " and ";
                query += "price <= "+conditions.getMaxPrice();
                isPrevQuery = true;
            }
            if(isPrevQuery) sql += query;
            if(enableLog) ConsoleColor.logs("QueryBook", sql,"IFO");
            PreparedStatement ptmt = conn.prepareStatement(sql);
            ResultSet rs = ptmt.executeQuery();


            List<Book> books = new ArrayList<>();
            Book tmpBook = null;
            while(rs.next()){
                tmpBook = new Book();
                tmpBook.setBookId( rs.getInt("book_id"));
                tmpBook.setCategory(rs.getString("category"));
                tmpBook.setTitle(rs.getString("title"));
                tmpBook.setPress(rs.getString("press"));
                tmpBook.setPublishYear(rs.getInt("publish_year"));
                tmpBook.setAuthor(rs.getString("author"));
                tmpBook.setPrice(rs.getDouble("price"));
                tmpBook.setStock(rs.getInt("stock"));
                books.add(tmpBook);
            }
            if(conditions.getSortOrder() == SortOrder.ASC)
                books.sort(conditions.getSortBy().getComparator());
            else
                books.sort(conditions.getSortBy().getComparator().reversed());

            for(Book book : books){ ConsoleColor.logs ("QueryBook",book.toString(),"IFO");}
            BookQueryResults bqr = new BookQueryResults(books);
            if(enableLog) ConsoleColor.logs("QueryBook", "Found "+bqr.getResults().size()+" books", "SUC");
            return new ApiResult(true, "Success", bqr);
        }
        catch (SQLException e) {
            if(enableLog) ConsoleColor.logs("QueryBook", "Failed: Unable to fetch book according to the given cond. SQLException"+e.getMessage(), "ERR");
            return new ApiResult(false, "Failed: Exception");
        }
    }

    @Override
    public ApiResult borrowBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try{
            String sql = "select * from borrow where book_id = ? and card_id = ?";
            PreparedStatement ptmt = conn.prepareStatement(sql);
            ptmt.setInt(1, borrow.getBookId());
            ptmt.setInt(2, borrow.getCardId());
            ResultSet rs = ptmt.executeQuery();
            while(rs.next()){
                if(rs.getLong("return_time")==0)
                {
                    if(enableLog) ConsoleColor.logs("BorrowBook", "Failed: Unable to borrow book #"+borrow.getBookId()+" by card #"+borrow.getCardId()+". Error: Same book unreturned by this card", "ERR");
                    return new ApiResult(false, "bookUnreturned");
                }
            }
            ApiResult apiResult = incBookStock(borrow.getBookId(), -1);
            if(!apiResult.ok){
                if(enableLog) ConsoleColor.logs("BorrowBook", "Failed: Unable to borrow book #"+borrow.getBookId()+" by card #"+borrow.getCardId()+". Error: Out of stock", "ERR");
                return new ApiResult(false, "noStock");
            }
            borrow.resetBorrowTime();
            String sql1 = "insert into borrow (card_id, book_id, borrow_time,return_time) values(?,?,?,?)";
            PreparedStatement ptmt1 = conn.prepareStatement(sql1);
            ptmt1.setInt(1, borrow.getCardId());
            ptmt1.setInt(2, borrow.getBookId());
            ptmt1.setLong(3, borrow.getBorrowTime());
            ptmt1.setLong(4, 0);
            ptmt1.execute();

            commit(conn);
            if(enableLog) ConsoleColor.logs("BorrowBook","Book #" + borrow.getBookId()+" borrowed by id #"+borrow.getCardId()+" at "+ borrow.getBorrowTime(), "SUC");
            return new ApiResult(true, "Success");
        } catch (SQLException e) {
            rollback(conn);
            if(enableLog) ConsoleColor.logs("BorrowBook", "Unable to borrow book #"+borrow.getBookId()+" by card #"+borrow.getCardId()+ ". SQLException: "+e.getMessage(), "ERR");
            return new ApiResult(false, "exception");
        }
    }

    @Override
    public ApiResult returnBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try{
            String sql = "select * from borrow where book_id = ? and card_id = ? and borrow_time = ?";
            PreparedStatement ptmt = conn.prepareStatement(sql);
            ptmt.setInt(1, borrow.getBookId());
            ptmt.setInt(2, borrow.getCardId());
            ptmt.setLong(3, borrow.getBorrowTime());
            ResultSet rs = ptmt.executeQuery();
            if(!rs.next()){
                if(enableLog) ConsoleColor.logs("ReturnBook", "Failed: Unable to return book #"+borrow.getBookId()+" by card #"+borrow.getCardId()+". Error: not borrowed", "ERR");
                return new ApiResult(false, "notBorrowed");
            }

            incBookStock(borrow.getBookId(), 1);

            borrow.resetReturnTime();
            String sql1 = "update borrow set return_time = ? where book_id = ? and card_id = ? and borrow_time = ?";
            PreparedStatement ptmt1 = conn.prepareStatement(sql1);
            ptmt1.setLong(1, borrow.getReturnTime());
            ptmt1.setInt(2, borrow.getBookId());
            ptmt1.setInt(3, borrow.getCardId());
            ptmt1.setLong(4, borrow.getBorrowTime());
            ptmt1.execute();

            commit(conn);
            if(enableLog) ConsoleColor.logs("ReturnBook", "Book #" + borrow.getBookId()+" borrowed by id #"+borrow.getCardId()+" at "+ borrow.getReturnTime(), "SUC");
            return new ApiResult(true, "Success");
        }catch(Exception e){
            rollback(conn);
            if(enableLog) ConsoleColor.logs("ReturnBook", "Failed: Unable to return book #"+borrow.getBookId()+" by card #"+borrow.getCardId()+ ". SQLException: "+e.getMessage(), "ERR");
            return new ApiResult(false, "exception");
        }
    }

    @Override
    public ApiResult showBorrowHistory(int cardId) {
        Connection conn = connector.getConn();
        try{
            String sql0 = "select * from card where card_id = ?";
            PreparedStatement ptmt0 = conn.prepareStatement(sql0);
            ptmt0.setInt(1, cardId);
            ResultSet rs0 = ptmt0.executeQuery();
            if(!rs0.next()){
                if(enableLog) ConsoleColor.logs("ShowBorrowHistory", "Card Not Found","ERR");
                return new ApiResult(false, "cardNotFound");
            }
            String sql = "select * from borrow where card_id = ?";
            PreparedStatement ptmt = conn.prepareStatement(sql);
            ptmt.setInt(1, cardId);
            ResultSet rs = ptmt.executeQuery();

            List<BorrowHistories.Item> borrows = new ArrayList<>();
            BorrowHistories.Item tmpBorrow = null;
            String sql1 = "select * from book where book_id = ?";
            PreparedStatement ptmt1 = conn.prepareStatement(sql1);

            while(rs.next()){
                tmpBorrow = new BorrowHistories.Item();
                ptmt1.setInt(1, rs.getInt("book_id"));
                ResultSet rs1 = ptmt1.executeQuery();
                if(rs1.next()){
                    tmpBorrow.setAuthor(rs1.getString("author"));
                    tmpBorrow.setTitle(rs1.getString("title"));
                    tmpBorrow.setPrice(rs1.getDouble("price"));
                    tmpBorrow.setPress(rs1.getString("press"));
                    tmpBorrow.setPublishYear(rs1.getInt("publish_year"));
                    tmpBorrow.setCategory(rs1.getString("category"));
                }

                tmpBorrow.setBookId( rs.getInt("book_id"));
                tmpBorrow.setCardId( rs.getInt("card_id"));
                tmpBorrow.setReturnTime( rs.getLong("return_time"));
                tmpBorrow.setBorrowTime( rs.getLong("borrow_time"));
                //ConsoleColor.logs("ShowBorrowHistory", tmpBorrow.toString(),"IFO");
                borrows.add(tmpBorrow);
            }
            borrows.sort((a,b)-> {
                if(a.getBorrowTime()!=b.getBorrowTime()){
                    return a.getBorrowTime()-b.getBorrowTime() > 0? -1:1;
                }else{
                    return a.getBookId()-b.getBookId();
                }
            });
            for(BorrowHistories.Item b :borrows) {
                if(enableLog) ConsoleColor.logs("ShowBorrowHistory", b.toString(),"IFO");
            }
            BorrowHistories bh = new BorrowHistories(borrows);
            if(enableLog) ConsoleColor.logs("ShowHistory", "Found "+bh.getCount()+" records of card #"+cardId, "SUC");

            return new ApiResult(true, "Success", bh);


        } catch (SQLException e) {

            if(enableLog) ConsoleColor.logs("ShowBorrowHistory", "Failed: Unable to show records of card #"+cardId+". SQLException:"+e.getMessage(), "ERR");
            return new ApiResult(false, "Failed: Exception");
        }
    }

    @Override
     public ApiResult registerCard(Card card) {
        Connection conn = connector.getConn();
        try{
            String sql1 = "select * from card where name = ? and department = ? and type = ?";
            PreparedStatement ptmt = conn.prepareStatement(sql1);
            ptmt.setString(1, card.getName());
            ptmt.setString(2, card.getDepartment());
            ptmt.setString(3, card.getType().getStr());
            ResultSet rs = ptmt.executeQuery();
            if(rs.next()){
                if(enableLog) ConsoleColor.logs("RegisterCard", "Failed: Unable to register card. Error: Duplicate Card of #"+rs.getInt("card_id"), "ERR");
                return new ApiResult(false, "dupCard");
            }
            String sql = "insert into card (name, department, type) values (?,?,?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, card.getName());
            pstmt.setString(2, card.getDepartment());
            pstmt.setString(3, card.getType().getStr());
            pstmt.execute();

            ResultSet rs1 = pstmt.getGeneratedKeys();
            int id;
            if(rs1.next()) {
                id = rs1.getInt(1);
            } else {
                throw new SQLException();
            }
            card.setCardId(id);

            commit(conn);
            if(enableLog) ConsoleColor.logs("RegisterCard", "registered card #"+id, "SUC");
            return new ApiResult(true, "Success",id);
        }catch (SQLException e){
            rollback(conn);
            if(enableLog) ConsoleColor.logs("RegisterCard", "Failed: Unable to register card. SQLException: "+e.getMessage(), "ERR");
            return new ApiResult(false, "exception");
        }
    }

    @Override
    public ApiResult removeCard(int cardId) {
        Connection conn = connector.getConn();
        try{
            String sql1 = "select * from borrow where card_id = ?";
            PreparedStatement ptmt = conn.prepareStatement(sql1);
            ptmt.setInt(1, cardId);
            ResultSet rs = ptmt.executeQuery();
            while(rs.next()){
                if(rs.getLong("return_time")==0){
                    if(enableLog) ConsoleColor.logs("RemoveCard", "Failed: Unable to remove card #"+cardId+". Error: Book #"+rs.getInt("book_id")+" unreturned.", "SUC");
                    return new ApiResult(false, "bookUnreturned");
                }
            }
            String sql = "delete from card where card_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1,cardId);
            int change = stmt.executeUpdate();
            if(change != 1) {
                if(enableLog) ConsoleColor.logs("RemoveCard", "Failed: Unable to remove card #"+cardId+". Error: Card does not exist", "ERR");
                return new ApiResult(false, "cardNotFound");
            }
            commit(conn);
            if(enableLog) ConsoleColor.logs("RemoveCard", "Removed card #" +cardId, "SUC");
            return new ApiResult(true, "Success");

        } catch (SQLException e) {
            rollback(conn);
            if(enableLog) ConsoleColor.logs("RemoveCard", "Failed: Unable to remove card #"+cardId+". SQLException:"+e.getMessage(), "ERR");
            return new ApiResult(false, "exception");
        }
    }

    @Override
    public ApiResult modifyCard(int cardId, Card card){
        Connection conn = connector.getConn();
        try{
            String sql = "select * from card where name = ? and department = ? and type = ?";
            String sql2 = "update card set name = ?, department = ?, type = ? where card_id = ?";

            PreparedStatement stmt1 = conn.prepareStatement(sql);
            stmt1.setString(1, card.getName());
            stmt1.setString(2, card.getDepartment());
            stmt1.setString(3, card.getType().getStr());
            ResultSet rs1 = stmt1.executeQuery();
            if(rs1.next()){
                if(enableLog) ConsoleColor.logs("ModifyCard", "Failed: Unable to modify card. Error: Duplicate Card of #"+rs1.getInt("card_id"), "ERR");
                return new ApiResult(false, "dupCard");
            }
            PreparedStatement stmt2 = conn.prepareStatement(sql2);
            stmt2.setString(1, card.getName());
            stmt2.setString(2, card.getDepartment());
            stmt2.setString(3, card.getType().getStr());
            stmt2.setInt(4, cardId);
            int change = stmt2.executeUpdate();
            if(change == 1) {
                if(enableLog) ConsoleColor.logs("ModifyCard", "Success: Modified", "SUC");
                return new ApiResult(true, "Success");
            } else {
                if(enableLog) ConsoleColor.logs("ModifyCard", "Failed: Unable to modify card. Error: Card Not Exist", "ERR");
                return new ApiResult(false, "cardNotExist");
            }
        } catch(SQLException e) {
            rollback(conn);
            if(enableLog)ConsoleColor.logs("ModifyCard","Failed: Unable to modify card. SQLException:"+e.getMessage(), "ERR");
            return new ApiResult(false, "exception");
        }
    }

    @Override
    public ApiResult showCards() {
        Connection conn = connector.getConn();
        try{
            String sql = "select * from card";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            List<Card> cards = new ArrayList<>();
            Card tmpCard = null;
            while(rs.next()){
                tmpCard = new Card();
                tmpCard.setCardId(rs.getInt("card_id"));
                tmpCard.setDepartment(rs.getString("department"));
                tmpCard.setName(rs.getString("name"));
                tmpCard.setType(Card.CardType.values(rs.getString("type")));
                cards.add(tmpCard);
            }
            cards.sort(Comparator.comparingInt(Card::getCardId));
            CardList cl = new CardList(cards);
            commit(conn);
            if(enableLog) ConsoleColor.logs("ShowCards", "retrieved "+cl.getCount()+" cards.", "SUC");
            return new ApiResult(true, "Success", cl);
        }catch (SQLException e) {
            rollback(conn);
            if(enableLog) ConsoleColor.logs("ShowCards", "Failed: Unable to retrieve cards. SQLException:"+e.getMessage(), "SUC");
            return new ApiResult(false, "Failed: Exception");
        }
    }

    @Override
    public ApiResult resetDatabase() {
        Connection conn = connector.getConn();
        try {
            Statement stmt = conn.createStatement();
            DBInitializer initializer = connector.getConf().getType().getDbInitializer();
            stmt.addBatch(initializer.sqlDropBorrow());
            stmt.addBatch(initializer.sqlDropBook());
            stmt.addBatch(initializer.sqlDropCard());
            stmt.addBatch(initializer.sqlCreateCard());
            stmt.addBatch(initializer.sqlCreateBook());
            stmt.addBatch(initializer.sqlCreateBorrow());
            stmt.executeBatch();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
