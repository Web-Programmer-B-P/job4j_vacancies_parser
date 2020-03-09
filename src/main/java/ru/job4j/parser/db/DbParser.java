package ru.job4j.parser.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.util.PSQLException;
import ru.job4j.parser.model.Vacansy;
import ru.job4j.parser.property.Property;

import java.sql.*;
import java.util.Map;

/**
 * Class DbParser
 *
 * @author Petr B.
 * @since 18.11.2019, 8:57
 */
public class DbParser implements AutoCloseable {
    private final Map<String, Vacansy> listParse;
    private Connection connection;
    private static final Logger LOG = LogManager.getLogger(DbParser.class.getName());
    private static final String CREATE_TABLE = "CREATE TABLE vacansy ("
            + "id serial NOT NULL PRIMARY KEY,"
            + "name varchar(500),"
            + "text text,"
            + "link text);";

    public DbParser(final Map<String, Vacansy> data) {
        listParse = data;
    }

    /**
     * Метод создает подключение и создает таблицу в бд, если ее нет.
     */
    public void init() {
        Property prop = new Property();
        prop.init();
        try {
            Class.forName(prop.getPropertyDriver());
        } catch (ClassNotFoundException e) {
            LOG.trace(e.getMessage());
        }
        LOG.info("Создаю подключение");
        try (Connection connect = DriverManager.getConnection(
                prop.getPropertyUrl(),
                prop.getPropertyUserName(),
                prop.getPropertyUserPassword()
        )) {
            if (connect != null) {
                connection = connect;
            }
            LOG.info("Подключение создано успешно!");
            String[] type = {"TABLE"};
            DatabaseMetaData metadata = connection.getMetaData();
            ResultSet rs = metadata.getTables(null, null, "vacansy", type);
            LOG.info("Проверяю наличие таблицы vacansy в базе");
            if (!rs.next()) {
                Statement initSheme = connection.createStatement();
                LOG.info("Таблица не найдена запускаю процесс создания");
                initSheme.execute(CREATE_TABLE);
                LOG.info("Процесс создания таблицы прошел успешно!");
                insert();
            } else {
                LOG.info("Таблица в базе существует, можно работать");
                insert();
            }
        } catch (SQLException se) {
            LOG.trace(se.getMessage());
        }

    }

    /**
     * Метод производит вставку вакансий из listParse, который передается при инициализации класса.
     */
    private int insert() {
        int result = 0;
        try {
            connection.setAutoCommit(false);
            LOG.info("Происходит вставка строк в базу");
            String sql = "INSERT INTO vacansy (name, text, link) VALUES (?, ?, ?)";
            try (PreparedStatement prep = connection.prepareStatement(sql)) {
                for (Map.Entry<String, Vacansy> el : listParse.entrySet()) {
                    result++;
                    prep.setString(1, el.getKey());
                    prep.setString(2, el.getValue().getText());
                    prep.setString(3, el.getValue().getLink());
                    prep.addBatch();
                }
                prep.executeBatch();
                LOG.info("В базу вставлено " + result + " строк.");
                connection.commit();
            } catch (PSQLException pe) {
                connection.rollback();
                LOG.trace(pe.getMessage());
            }
        } catch (SQLException se) {
            LOG.trace(se.getMessage());
        }
        return result;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
