package com.example.myjavafxapp.utils;

import com.example.myjavafxapp.database.DatabaseConnection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.*;

public class DatabaseToXMLExporter {

    public static void main(String[] args) {
        exportAllTablesToXML("all_tables.xml");
    }

    /**
     * Экспортирует все таблицы из текущей базы данных в один XML-файл.
     * Предполагается, что подключение к БД настроено в DatabaseConnection.
     */
    public static void exportAllTablesToXML(String outputFile) {
        try (Connection conn = DatabaseConnection.connect()) {
            // Получаем список всех таблиц из текущей схемы (базы)
            // Предполагается, что имя схемы можно получить из conn.getCatalog() или заменить на нужное
            String dbName = conn.getCatalog();
            if (dbName == null || dbName.isEmpty()) {
                // Если не получается получить имя схемы, можете указать вручную.
                // Или получить из настроек подключения.
                // dbName = "myjavafxapp"; // Пример
                throw new RuntimeException("Не удалось определить имя схемы.");
            }

            String tableQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema = ?";

            // Создаем XML документ
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            // Корневой элемент <database>
            Element rootElement = doc.createElement("database");
            rootElement.setAttribute("name", dbName);
            doc.appendChild(rootElement);

            // Получаем список таблиц
            try (PreparedStatement ps = conn.prepareStatement(tableQuery)) {
                ps.setString(1, dbName);
                try (ResultSet rsTables = ps.executeQuery()) {
                    while (rsTables.next()) {
                        String tableName = rsTables.getString("table_name");
                        exportTable(conn, doc, rootElement, tableName);
                    }
                }
            }

            // Записываем документ в файл
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(outputFile));
            transformer.transform(source, result);

            System.out.println("XML экспортирован в " + outputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Экспортирует данные одной таблицы в элемент XML документа.
     */
    private static void exportTable(Connection conn, Document doc, Element rootElement, String tableName) throws SQLException {
        String query = "SELECT * FROM " + tableName;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Элемент <table>
            Element tableElement = doc.createElement("table");
            tableElement.setAttribute("name", tableName);
            rootElement.appendChild(tableElement);

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Element rowElement = doc.createElement("row");
                tableElement.appendChild(rowElement);

                for (int i = 1; i <= columnCount; i++) {
                    String colName = meta.getColumnName(i);
                    Element colElement = doc.createElement("column");
                    colElement.setAttribute("name", colName);

                    String value = rs.getString(i);
                    if (value == null) {
                        value = ""; // Пустая строка для NULL
                    }
                    colElement.appendChild(doc.createTextNode(value));
                    rowElement.appendChild(colElement);
                }
            }
        }
    }
}
