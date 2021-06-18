package hu.mineside.echo.utils.sql;

/*
 * Copyright (c) 2019-Present marvintheskid (Kovács Márton)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hu.mineside.echo.config.Config;
import hu.mineside.echo.config.MySQL;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@Slf4j
@Getter
@NoArgsConstructor
public class HikariConnection {
    private RowSetFactory rowSetFactory;
    private String host, user, db, pw;
    private int port, poolSize;
    private HikariDataSource connection;
    private ExecutorService service;

    public HikariConnection(String host, int port, String user, String pw, String db, int poolSize) {
        this.poolSize = poolSize;
        this.host = host;
        this.user = user;
        this.port = port;
        this.db = db;
        this.pw = pw;
        this.service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        connect();
    }

    public HikariConnection(String host, int port, String user, String pw, String db) {
        this.poolSize = 1;
        this.host = host;
        this.user = user;
        this.port = port;
        this.db = db;
        this.pw = pw;
        this.service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        connect();
    }

    public HikariConnection(MySQL mysql, int poolSize) {
        this(mysql.host, mysql.port, mysql.username, mysql.password, mysql.database, poolSize);
    }

    public CompletableFuture<ResultSet> query(String query, Object... params) {
        return this.query(true, query, params);
    }

    public CompletableFuture<Integer> update(String query, BatchContainer container) {
        return this.update(true, query, container.getObjects());
    }

    public CompletableFuture<Integer> update(String query, Object... params) {
        return this.update(true, query, params);
    }

    public CompletableFuture<Integer[]> batchUpdate(String query, BatchContainer... params) {
        return this.batchUpdate(true, query, params);
    }

    public CompletableFuture<ResultSet> query(boolean async, String query, Object... params) {
        if (async) {
            return CompletableFuture.supplyAsync(() -> queryInternal(query, params), service);
        } else {
            CompletableFuture<ResultSet> future = new CompletableFuture<>();
            future.complete(queryInternal(query, params));
            return future;
        }
    }

    public CompletableFuture<Integer> update(boolean async, String query, Object... params) {
        if (async) {
            return CompletableFuture.supplyAsync(() -> updateInternal(query, params), service);
        } else {
            CompletableFuture<Integer> future = new CompletableFuture<>();
            future.complete(updateInternal(query, params));
            return future;
        }
    }

    public CompletableFuture<Integer[]> batchUpdate(boolean async, String query, BatchContainer... params) {
        if (async) {
            return CompletableFuture.supplyAsync(() -> updateBatchInternal(query, params), service);
        } else {
            CompletableFuture<Integer[]> future = new CompletableFuture<>();
            future.complete(updateBatchInternal(query, params));
            return future;
        }
    }

    private ResultSet queryInternal(String query, Object... params) {
        try (Connection conn = connection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            int index = 1;
            for (Object param : params) {
                stmt.setObject(index, param);
                index++;
            }
            ResultSet result = stmt.executeQuery();
            CachedRowSet cachedRowSet = rowSetFactory.createCachedRowSet();
            cachedRowSet.populate(result);
            result.close();
            return cachedRowSet;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private int updateInternal(String query, Object... params) {
        try (Connection conn = connection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            int index = 1;
            for (Object param : params) {
                stmt.setObject(index, param);
                index++;
            }
            return stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    private Integer[] updateBatchInternal(String query, BatchContainer... params) {
        Preconditions.checkNotNull(connection, "connection is null");
        try (Connection conn = connection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            for (BatchContainer container : params) {
                int index = 1;
                for (Object param : container.getObjects()) {
                    stmt.setObject(index, param);
                    index++;
                }
                stmt.addBatch();
            }
            int[] res = stmt.executeBatch();
            return IntStream.of(res).boxed().toArray(Integer[]::new);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Integer[] {-1};
        }
    }

    public void connect() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db + "?useUnicode=true&characterEncoding=UTF-8");
        config.setUsername(user);
        config.setPassword(pw);
        config.setDriverClassName("com.mysql.jdbc.Driver");

        config.setMaximumPoolSize(poolSize);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("rewriteBatchedStatements", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("maintainTimeStats", false);
        config.setLeakDetectionThreshold(60 * 1000);

        try {
            connection = new HikariDataSource(config);
            rowSetFactory = RowSetProvider.newFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            connection.close();
            service.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HikariDataSource getInstance() {
        return connection;
    }
}
