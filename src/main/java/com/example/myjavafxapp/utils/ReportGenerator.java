package com.example.myjavafxapp.utils;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.File;
import java.io.InputStream;

public class ReportGenerator {

    public void createPDF() {
        try {
            // Путь к JRXML ресурсу в папке ресурсов:
            // Файл должен находиться: src/main/resources/com/example/myjavafxapp/reports/Blank_A4_1.jrxml
            String jrxmlResourcePath = "/com/example/myjavafxapp/reports/Blank_A4_1.jrxml";

            // Загрузка ресурса через getResourceAsStream класса ReportGenerator
            InputStream jrxmlStream = ReportGenerator.class.getResourceAsStream(jrxmlResourcePath);
            if (jrxmlStream == null) {
                throw new JRException("Не удалось загрузить jrxml из ресурсов: " + jrxmlResourcePath);
            }

            // Путь к XML данным
            String xmlPath = "all_tables.xml";

            // Компиляция отчёта из потока
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

            // Создаем источник данных из XML
            JRDataSource dataSource = new JRXmlDataSource(new File(xmlPath), "/database/table/row");

            // Заполняем отчет
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, dataSource);

            // Выходной PDF
            String outputPdf = "output.pdf";
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputPdf));

            exporter.exportReport();

            System.out.println("PDF отчет успешно создан: " + outputPdf);

        } catch (JRException e) {
            e.printStackTrace();
            System.err.println("Ошибка при создании PDF отчёта: " + e.getMessage());
        }
    }
}
