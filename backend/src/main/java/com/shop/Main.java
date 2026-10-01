package com.shop;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;

public class Main {
    static final int PORT = Integer.parseInt(
        System.getenv().getOrDefault("PORT", "8080")
);


    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/register", Main::register);
        server.createContext("/api/login", Main::login);
        server.createContext("/api/categories", Main::categories);
        server.createContext("/api/products", Main::products);
        server.createContext("/api/orders", Main::orders);
        server.createContext("/api/order", Main::orderAction);

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Online Shopping API running at http://localhost:" + PORT);
    }

    static void headers(HttpExchange ex) {
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
    }

    static void send(HttpExchange ex, int code, String body) throws IOException {
        headers(ex);
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }

    static String body(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    static Map<String,String> form(String s) {
        Map<String,String> m = new HashMap<>();
        for (String p : s.split("&")) {
            if (p.isBlank()) continue;
            String[] a = p.split("=", 2);
            m.put(URLDecoder.decode(a[0], StandardCharsets.UTF_8),
                  a.length > 1 ? URLDecoder.decode(a[1], StandardCharsets.UTF_8) : "");
        }
        return m;
    }

    static String esc(String s) {
        return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"");
    }

    static void register(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { send(ex,405,"{}"); return; }
        Map<String,String> f = form(body(ex));
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "INSERT INTO users(name,email,password) VALUES(?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.get("name"));
            ps.setString(2, f.get("email"));
            ps.setString(3, f.get("password")); // Demo project: plain text. Hash in production.
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                send(ex,200,"{\"success\":true,\"userId\":"+rs.getInt(1)+"}");
            }
        } catch (SQLException e) {
            send(ex,400,"{\"success\":false,\"message\":\"Email may already exist\"}");
        }
    }

    static void login(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { send(ex,405,"{}"); return; }
        Map<String,String> f = form(body(ex));
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT id,name,email FROM users WHERE email=? AND password=?")) {
            ps.setString(1, f.get("email"));
            ps.setString(2, f.get("password"));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    send(ex,200,String.format(
                        "{\"success\":true,\"userId\":%d,\"name\":\"%s\",\"email\":\"%s\"}",
                        rs.getInt("id"),esc(rs.getString("name")),esc(rs.getString("email"))));
                } else send(ex,401,"{\"success\":false,\"message\":\"Invalid login\"}");
            }
        } catch (SQLException e) {
            send(ex,500,"{\"success\":false,\"message\":\"Database error\"}");
        }
    }

    static void categories(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) { send(ex,405,"{}"); return; }
        StringBuilder j = new StringBuilder("[");
        try (Connection c=DBConnection.getConnection();
             Statement s=c.createStatement();
             ResultSet r=s.executeQuery("SELECT id,name FROM categories ORDER BY name")) {
            boolean first=true;
            while(r.next()) {
                if(!first) j.append(",");
                first=false;
                j.append(String.format("{\"id\":%d,\"name\":\"%s\"}",r.getInt("id"),esc(r.getString("name"))));
            }
            j.append("]");
            send(ex,200,j.toString());
        } catch(SQLException e){ send(ex,500,"{\"message\":\"Database error\"}"); }
    }

    static void products(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) { send(ex,405,"{}"); return; }
        String q = ex.getRequestURI().getQuery();
        Map<String,String> f = q == null ? Map.of() : form(q);
        String sql = "SELECT p.*,c.name category FROM products p JOIN categories c ON p.category_id=c.id";
        if (f.containsKey("category")) sql += " WHERE p.category_id=?";
        sql += " ORDER BY p.id DESC";

        StringBuilder j = new StringBuilder("[");
        try (Connection c=DBConnection.getConnection();
             PreparedStatement ps=c.prepareStatement(sql)) {
            if(f.containsKey("category")) ps.setInt(1,Integer.parseInt(f.get("category")));
            try(ResultSet r=ps.executeQuery()) {
                boolean first=true;
                while(r.next()) {
                    if(!first) j.append(",");
                    first=false;
                    j.append(String.format(
                        "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"imageUrl\":\"%s\",\"stock\":%d,\"category\":\"%s\"}",
                        r.getInt("id"),esc(r.getString("name")),esc(r.getString("description")),
                        r.getDouble("price"),esc(r.getString("image_url")),r.getInt("stock"),esc(r.getString("category"))));
                }
            }
            j.append("]");
            send(ex,200,j.toString());
        } catch(Exception e){ send(ex,500,"{\"message\":\"Could not load products\"}"); }
    }

    static void orders(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) { send(ex,405,"{}"); return; }
        Map<String,String> f = form(Optional.ofNullable(ex.getRequestURI().getQuery()).orElse(""));
        int userId = Integer.parseInt(f.getOrDefault("userId","0"));
        StringBuilder j = new StringBuilder("[");
        try(Connection c=DBConnection.getConnection();
            PreparedStatement ps=c.prepareStatement(
                "SELECT id,payment_mode,total_amount,order_date,expected_delivery,status FROM orders WHERE user_id=? ORDER BY id DESC")) {
            ps.setInt(1,userId);
            try(ResultSet r=ps.executeQuery()) {
                boolean first=true;
                while(r.next()){
                    if(!first)j.append(",");
                    first=false;
                    j.append(String.format(
                        "{\"id\":%d,\"paymentMode\":\"%s\",\"totalAmount\":%.2f,\"orderDate\":\"%s\",\"expectedDelivery\":\"%s\",\"status\":\"%s\"}",
                        r.getInt("id"),r.getString("payment_mode"),r.getDouble("total_amount"),
                        r.getTimestamp("order_date"),r.getDate("expected_delivery"),r.getString("status")));
                }
            }
            j.append("]");
            send(ex,200,j.toString());
        }catch(Exception e){send(ex,500,"{\"message\":\"Could not load orders\"}");}
    }

    static void orderAction(HttpExchange ex) throws IOException {
        String[] path=ex.getRequestURI().getPath().split("/");
        if(path.length < 4){send(ex,400,"{}");return;}
        String action=path[3];

        if("create".equals(action) && "POST".equalsIgnoreCase(ex.getRequestMethod())) {
            Map<String,String> f=form(body(ex));
            int userId=Integer.parseInt(f.get("userId"));
            String payment=f.getOrDefault("paymentMode","COD");
            String items=f.getOrDefault("items",""); // productId:qty,productId:qty
            double total=0;

            try(Connection c=DBConnection.getConnection()) {
                c.setAutoCommit(false);
                try {
                    // Calculate total from DB, not from the browser.
                    for(String item:items.split(",")){
                        if(item.isBlank()) continue;
                        String[] x=item.split(":");
                        try(PreparedStatement ps=c.prepareStatement("SELECT price,stock FROM products WHERE id=?")){
                            ps.setInt(1,Integer.parseInt(x[0]));
                            try(ResultSet r=ps.executeQuery()){
                                if(!r.next()) throw new SQLException("Product not found");
                                int qty=Integer.parseInt(x[1]);
                                if(qty<=0 || r.getInt("stock")<qty) throw new SQLException("Insufficient stock");
                                total += r.getDouble("price")*qty;
                            }
                        }
                    }

                    LocalDate delivery=LocalDate.now().plusDays(7);
                    try(PreparedStatement ps=c.prepareStatement(
                        "INSERT INTO orders(user_id,payment_mode,total_amount,expected_delivery) VALUES(?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1,userId); ps.setString(2,payment); ps.setDouble(3,total); ps.setDate(4,java.sql.Date.valueOf(delivery));
                        ps.executeUpdate();
                        try(ResultSet keys=ps.getGeneratedKeys()){
                            keys.next();
                            int orderId=keys.getInt(1);
                            for(String item:items.split(",")){
                                if(item.isBlank())continue;
                                String[] x=item.split(":");
                                int pid=Integer.parseInt(x[0]), qty=Integer.parseInt(x[1]);
                                try(PreparedStatement a=c.prepareStatement(
                                    "INSERT INTO order_items(order_id,product_id,quantity,price) SELECT ?,id,?,price FROM products WHERE id=?");
                                    PreparedStatement b=c.prepareStatement("UPDATE products SET stock=stock-? WHERE id=?")) {
                                    a.setInt(1,orderId);a.setInt(2,qty);a.setInt(3,pid);a.executeUpdate();
                                    b.setInt(1,qty);b.setInt(2,pid);b.executeUpdate();
                                }
                            }
                            c.commit();
                            send(ex,200,String.format(
                                "{\"success\":true,\"orderId\":%d,\"total\":%.2f,\"expectedDelivery\":\"%s\"}",
                                orderId,total,delivery));
                        }
                    }
                } catch(Exception e){ c.rollback(); send(ex,400,"{\"success\":false,\"message\":\""+esc(e.getMessage())+"\"}"); }
                finally{c.setAutoCommit(true);}
            } catch(Exception e){send(ex,500,"{\"success\":false,\"message\":\"Database error\"}");}
            return;
        }

        if("cancel".equals(action) && "POST".equalsIgnoreCase(ex.getRequestMethod())) {
            Map<String,String> f=form(body(ex));
            int orderId=Integer.parseInt(f.get("orderId"));
            int userId=Integer.parseInt(f.get("userId"));
            try(Connection c=DBConnection.getConnection();
                PreparedStatement ps=c.prepareStatement(
                    "UPDATE orders SET status='CANCELLED' WHERE id=? AND user_id=? AND status IN ('PLACED','SHIPPED','OUT_FOR_DELIVERY') AND order_date >= NOW() - INTERVAL 2 DAY")) {
                ps.setInt(1,orderId);ps.setInt(2,userId);
                int n=ps.executeUpdate();
                if(n==1) send(ex,200,"{\"success\":true,\"message\":\"Order cancelled\"}");
                else send(ex,400,"{\"success\":false,\"message\":\"Cancellation period expired or order cannot be cancelled\"}");
            }catch(Exception e){send(ex,500,"{\"success\":false,\"message\":\"Database error\"}");}
            return;
        }

        send(ex,404,"{}");
    }
}
