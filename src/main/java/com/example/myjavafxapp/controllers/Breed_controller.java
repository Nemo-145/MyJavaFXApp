package com.example.myjavafxapp.controllers;

import com.example.myjavafxapp.database.BreedDAO;
import com.example.myjavafxapp.models.Breed;
import com.example.myjavafxapp.models.Member;
import com.example.myjavafxapp.utils.DatabaseToXMLExporter;
import com.example.myjavafxapp.utils.ReportGenerator;
import com.example.myjavafxapp.utils.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>Контроллер для страницы Breed.</p>
 * <ul>
 *     <li>Отображается список всех пород (кнопками) слева.</li>
 *     <li>При выборе конкретной породы — показываются владельцы (участники) в правой части.</li>
 *     <li>Поддерживает поиск (Ctrl+T). Если при поиске выбрана другая сущность — осуществляется переход на соответствующую страницу.</li>
 * </ul>
 */
public class Breed_controller {

    /**
     * Логгер для вывода диагностических сообщений.
     */
    private static final Logger logger = LoggerFactory.getLogger(Breed_controller.class);

    /**
     * Если при переходе на страницу Breed нужно сразу открыть конкретную породу по её ID,
     * то сюда предварительно устанавливаем breedToOpen.
     */
    private static int breedToOpen = 0;

    /**
     * Устанавливает ID породы, которую нужно открыть (при переходе со страницы поиска).
     * @param brId идентификатор породы.
     */
    public static void setBreedToOpen(int brId) {
        breedToOpen = brId;
    }

    @FXML private VBox space_for_list_breed;
    @FXML private VBox dataContainer_breed;

    @FXML private Button Member;
    @FXML private Button Judge;
    @FXML private Button Breed;
    @FXML private Button About_app;
    @FXML private Button Event;
    @FXML private Button Create_pdf;
    @FXML private Button closeButton;
    @FXML private Button hideButton;
    @FXML private Button minimizeButton;

    /**
     * Инициализация контроллера Breed_controller.
     * Настраивает кнопки управления окном, навигацию, PDF-генерацию,
     * а также горячую клавишу Ctrl+T для поиска.
     */
    @FXML
    public void initialize() {
        logger.info("Инициализация Breed_controller");

        // Кнопки управления окном
        closeButton.setOnAction(e -> {
            logger.debug("Кнопка [Close] (Breed_controller)");
            closeApplication();
        });
        minimizeButton.setOnAction(e -> {
            logger.debug("Кнопка [Minimize] (Breed_controller)");
            minimizeApplication();
        });
        hideButton.setOnAction(e -> {
            logger.debug("Кнопка [Fullscreen] (Breed_controller)");
            maximizeApplication();
        });

        // Навигация
        Event.setOnAction(e -> {
            logger.debug("Кнопка [Event] => switchScene(page_event.fxml)");
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_event.fxml", getStage());
        });
        Member.setOnAction(e -> {
            logger.debug("Кнопка [Member] => switchScene(page_applications.fxml)");
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_applications.fxml", getStage());
        });
        Judge.setOnAction(e -> {
            logger.debug("Кнопка [Judge] => switchScene(page_judge.fxml)");
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_judge.fxml", getStage());
        });
        Breed.setOnAction(e -> {
            logger.debug("Кнопка [Breed] => switchScene(page_breed.fxml)");
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_breed.fxml", getStage());
        });
        About_app.setOnAction(e -> {
            logger.debug("Кнопка [About_app] => switchScene(page_aboutapp.fxml)");
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_aboutapp.fxml", getStage());
        });

        // Кнопка PDF
        Create_pdf.setOnAction(event -> {
            logger.debug("Нажата кнопка [Create_pdf] (Breed_controller)");
            DatabaseToXMLExporter.exportAllTablesToXML("all_tables.xml");
            new ReportGenerator().createPDF();
        });

        // Горячая клавиша (поиск)
        initSearchHotkey();

        // Загрузить кнопки пород
        loadBreedButtons();

        // Если ранее задали открыть породу с ID=breedToOpen
        if (breedToOpen != 0) {
            logger.debug("Нужно открыть породу по ID={}", breedToOpen);
            String breedName = findBreedNameById(breedToOpen);
            if (breedName != null) {
                logger.debug("Найдена порода: {}, загружаем владельцев...", breedName);
                loadOwnersByBreed(breedName);
            } else {
                logger.warn("Порода с ID={} не найдена в базе", breedToOpen);
            }
            breedToOpen = 0; // сбрасываем после открытия
        }
    }

    /**
     * Ищет в базе название породы по её ID.
     * @param brId идентификатор породы.
     * @return название породы или null, если не найдено.
     */
    private String findBreedNameById(int brId) {
        logger.debug("Поиск названия породы по ID={}", brId);
        List<Breed> all = BreedDAO.getAllBreedsAsObjects();
        for (Breed b : all) {
            if (b.getId() == brId) {
                return b.getName();
            }
        }
        return null;
    }

    /**
     * Настраивает хоткей Ctrl+T для поиска.
     */
    private void initSearchHotkey() {
        logger.debug("Подключение хоткея [Ctrl+T] для поиска (Breed_controller)");
        space_for_list_breed.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                        KeyCombination.keyCombination("Ctrl+T"),
                        this::showSearchDialog
                );
            }
        });
    }

    /**
     * Открывает окно поиска. При выборе элемента — переход на страницу или открытие данных.
     */
    private void showSearchDialog() {
        logger.debug("Показ окна поиска (Ctrl+T) (Breed_controller)");
        Stage st = getStage();
        SearchDialog dialog = new SearchDialog(st);

        dialog.setSearchDialogListener(selectedItem -> {
            logger.debug("Из поиска выбрано: {}", selectedItem);

            if (selectedItem.startsWith("Member:")) {
                int idx = selectedItem.indexOf("#");
                if (idx != -1) {
                    String idStr = selectedItem.substring(idx + 1, selectedItem.length() - 1);
                    int memId = Integer.parseInt(idStr);
                    logger.debug("Переход на страницу Member_controller, ID={}", memId);
                    Member_controller.setMemberToOpen(memId);
                    SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_applications.fxml", st);
                }
            } else if (selectedItem.startsWith("Judge:")) {
                int idx = selectedItem.indexOf("#");
                if (idx != -1) {
                    String idStr = selectedItem.substring(idx + 1, selectedItem.length() - 1);
                    int jId = Integer.parseInt(idStr);
                    logger.debug("Переход на страницу Judge_controller, ID={}", jId);
                    Judge_controller.setJudgeToOpen(jId);
                    SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_judge.fxml", st);
                }
            } else if (selectedItem.startsWith("Breed:")) {
                int idx = selectedItem.indexOf("#");
                if (idx != -1) {
                    String idStr = selectedItem.substring(idx + 1, selectedItem.length() - 1);
                    int brId = Integer.parseInt(idStr);
                    logger.debug("Открытие породы в Breed_controller, ID={}", brId);
                    String bn = findBreedNameById(brId);
                    if (bn != null) {
                        loadOwnersByBreed(bn);
                    } else {
                        logger.warn("Порода ID={} не найдена", brId);
                    }
                }
            }
        });

        dialog.showDialog();
    }

    /**
     * Загружает список всех пород (из базы) и отображает в виде кнопок.
     */
    private void loadBreedButtons() {
        logger.debug("Загрузка списка пород (Breed_controller)");
        List<String> breeds = BreedDAO.getAllBreeds();
        space_for_list_breed.getChildren().clear();

        for (String brName : breeds) {
            Button b = new Button(brName);
            b.setOnAction(e -> {
                logger.debug("Нажата кнопка породы: {}", brName);
                loadOwnersByBreed(brName);
            });
            space_for_list_breed.getChildren().add(b);
        }
    }

    /**
     * Отображает владельцев (member) указанной породы (в правом боксе).
     * @param breedName название породы.
     */
    private void loadOwnersByBreed(String breedName) {
        logger.debug("Загрузка владельцев для породы '{}'", breedName);
        List<Member> owners = BreedDAO.getOwnersByBreed(breedName);
        dataContainer_breed.getChildren().clear();

        for (Member owner : owners) {
            logger.debug("Найден владелец ID={}, Name={}", owner.getId(), owner.getName());
            Button ownerBtn = new Button(owner.getName() + " (Age: " + owner.getAge() + ")");
            ownerBtn.setOnAction(e -> {
                logger.debug("Переход к участнику ID={} (Member_controller)", owner.getId());
                Member_controller.setMemberToOpen(owner.getId());
                SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_applications.fxml", getStage());
            });
            dataContainer_breed.getChildren().add(ownerBtn);
        }
    }

    /**
     * Возвращает текущее окно (Stage).
     */
    private Stage getStage() {
        return (Stage) Breed.getScene().getWindow();
    }

    /**
     * Закрывает приложение.
     */
    private void closeApplication() {
        logger.info("Закрытие приложения (Breed_controller)");
        getStage().close();
    }

    /**
     * Сворачивает приложение (иконка в панели задач).
     */
    private void minimizeApplication() {
        logger.debug("Сворачивание приложения (Breed_controller)");
        getStage().setIconified(true);
    }

    /**
     * Переключение полноэкранного режима.
     */
    private void maximizeApplication() {
        logger.debug("Полноэкранный режим (Breed_controller)");
        getStage().setFullScreen(!getStage().isFullScreen());
    }
}
