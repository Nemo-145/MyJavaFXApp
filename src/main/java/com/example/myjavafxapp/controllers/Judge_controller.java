package com.example.myjavafxapp.controllers;

import com.example.myjavafxapp.database.BreedDAO;
import com.example.myjavafxapp.database.JudgeDAO;
import com.example.myjavafxapp.models.Breed;
import com.example.myjavafxapp.models.Judge;
import com.example.myjavafxapp.utils.DatabaseToXMLExporter;
import com.example.myjavafxapp.utils.ReportGenerator;
import com.example.myjavafxapp.utils.SceneSwitcher;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер для страницы судей (Judge).
 * <ul>
 *   <li>Слева в списке (VBox) – кнопки для каждого судьи + "Add new judge".</li>
 *   <li>Справа (VBox) – детали выбранного судьи, режим редактирования, доп. поля для пород.</li>
 *   <li>Поддерживает поиск (Ctrl+T) с переходом на Judge, Member или Breed.</li>
 * </ul>
 */
public class Judge_controller {

    private static final Logger logger = LoggerFactory.getLogger(Judge_controller.class);

    /**
     * Если нужно при загрузке открыть конкретного судью, указываем его ID.
     */
    private static int judgeToOpen = 0;

    /**
     * Устанавливает ID судьи, которого нужно открыть после инициализации.
     * @param jId ID судьи
     */
    public static void setJudgeToOpen(int jId) {
        judgeToOpen = jId;
    }

    @FXML private VBox space_for_list_judge;
    @FXML private VBox dataContainer_judge;
    @FXML private HBox buttonContainer;

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
     * Режим редактирования (true) или просмотра (false).
     */
    private boolean isEditMode = false;

    /**
     * Список HBox, каждое поле – дополнительная порода (ComboBox + remove).
     */
    private final List<HBox> breedFieldsList = new ArrayList<>();

    @FXML
    public void initialize() {
        logger.info("Инициализация Judge_controller");

        closeButton.setOnAction(e -> closeApplication());
        minimizeButton.setOnAction(e -> minimizeApplication());
        hideButton.setOnAction(e -> maximizeApplication());

        // Навигационные кнопки
        Event.setOnAction(e -> {
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_event.fxml", getStage());
        });
        Member.setOnAction(e -> {
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_applications.fxml", getStage());
        });
        Judge.setOnAction(e -> {
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_judge.fxml", getStage());
        });
        Breed.setOnAction(e -> {
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_breed.fxml", getStage());
        });
        About_app.setOnAction(e -> {
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_aboutapp.fxml", getStage());
        });

        // Кнопка PDF
        Create_pdf.setOnAction(event -> {
            logger.debug("Нажата кнопка PDF (Judge_controller)");
            DatabaseToXMLExporter.exportAllTablesToXML("all_tables.xml");
            new ReportGenerator().createPDF();
        });

        loadJudgeButtons();
        initSearchHotkey();

        // Если нужно открыть конкретного судью
        if (judgeToOpen != 0) {
            Judge j = JudgeDAO.getJudgeById(judgeToOpen);
            if (j != null) {
                displayJudgeDetails(j);
            }
            judgeToOpen = 0; // сбрасываем
        }
    }

    /**
     * Настраиваем хоткей Ctrl+T для поиска.
     */
    private void initSearchHotkey() {
        space_for_list_judge.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                        KeyCombination.keyCombination("Ctrl+T"),
                        this::showSearchDialog
                );
            }
        });
    }

    /**
     * Открывает диалог поиска (SearchDialog).
     * По результатам выбора – открываем нужного судью, участника или породу.
     */
    private void showSearchDialog() {
        Stage st = getStage();
        SearchDialog dialog = new SearchDialog(st);

        dialog.setSearchDialogListener(selectedItem -> {
            logger.debug("Выбрано в поиске: {}", selectedItem);
            if (selectedItem.startsWith("Judge:")) {
                int idx = selectedItem.indexOf("#");
                if (idx != -1) {
                    int jId = Integer.parseInt(selectedItem.substring(idx+1, selectedItem.length()-1));
                    Judge found = JudgeDAO.getJudgeById(jId);
                    if (found != null) {
                        displayJudgeDetails(found);
                    }
                }
            } else if (selectedItem.startsWith("Member:")) {
                int idx = selectedItem.indexOf("#");
                if (idx != -1) {
                    int memId = Integer.parseInt(selectedItem.substring(idx+1, selectedItem.length()-1));
                    Member_controller.setMemberToOpen(memId);
                    SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_applications.fxml", st);
                }
            } else if (selectedItem.startsWith("Breed:")) {
                int idx = selectedItem.indexOf("#");
                if (idx != -1) {
                    int bId = Integer.parseInt(selectedItem.substring(idx+1, selectedItem.length()-1));
                    Breed_controller.setBreedToOpen(bId);
                    SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_breed.fxml", st);
                }
            }
        });

        dialog.showDialog();
    }

    /**
     * Загружает список судей, формирует кнопки слева.
     */
    private void loadJudgeButtons() {
        space_for_list_judge.getChildren().clear();
        List<Judge> judges = JudgeDAO.getAllJudges();

        if (judges.isEmpty()) {
            Button addNew = new Button("Add new judge");
            addNew.setPrefWidth(400);
            addNew.setOnAction(e -> {
                isEditMode = true;
                showEmptyJudgeForm();
            });
            space_for_list_judge.getChildren().add(addNew);
        } else {
            for (Judge j : judges) {
                Button b = new Button(j.getName());
                b.setPrefWidth(400);
                b.setOnAction(e -> {
                    isEditMode = false;
                    displayJudgeDetails(j);
                });
                space_for_list_judge.getChildren().add(b);
            }
            Button addNew = new Button("Add new judge");
            addNew.setPrefWidth(400);
            addNew.setOnAction(e -> {
                isEditMode = true;
                showEmptyJudgeForm();
            });
            space_for_list_judge.getChildren().add(addNew);
        }
    }

    /**
     * Отображает в правой части подробности судьи (режим просмотра).
     */
    private void displayJudgeDetails(Judge judge) {
        dataContainer_judge.getChildren().clear();

        Label nameL = new Label("Name: " + judge.getName());
        Label ageL = new Label("Age: " + judge.getAge());
        Label expL = new Label("Experience: " + judge.getExperience());
        Label mainBreedL = new Label("Main Breed: "
                + (judge.getBreed() != null ? judge.getBreed().getName() : "None"));

        List<Breed> judgeBreeds = JudgeDAO.getJudgeBreeds(judge.getId());
        Label addBreedL = new Label("Additional Breeds: "
                + (judgeBreeds.isEmpty() ? "None" : ""));

        dataContainer_judge.getChildren().addAll(
                nameL, ageL, expL, mainBreedL, addBreedL
        );

        for (Breed b : judgeBreeds) {
            dataContainer_judge.getChildren().add(new Label("- " + b.getName()));
        }

        showViewModeButtons(judge);
    }

    /**
     * Показывает кнопки (Redact, Delete) для режима просмотра.
     */
    private void showViewModeButtons(Judge judge) {
        buttonContainer.getChildren().clear();

        Button redact = new Button("Redact");
        redact.setOnAction(e -> {
            isEditMode = true;
            enableEditing(judge);
        });

        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            if (showConfirmationDialog("Are you sure to delete judge?")) {
                if (JudgeDAO.deleteJudgeById(judge.getId())) {
                    loadJudgeButtons();
                    dataContainer_judge.getChildren().clear();
                }
            }
        });

        buttonContainer.getChildren().addAll(redact, delete);
    }

    /**
     * Переводим в режим редактирования (с полями name, age, exp, mainBreed, +dоп. породы).
     */
    private void enableEditing(Judge judge) {
        dataContainer_judge.getChildren().clear();
        breedFieldsList.clear();

        TextField nameField = new TextField(judge.getName());
        restrictToLetters(nameField);

        TextField ageField = new TextField(String.valueOf(judge.getAge()));
        restrictToDigits(ageField);

        TextField expField = new TextField(String.valueOf(judge.getExperience()));
        restrictToDigits(expField);

        List<Breed> all = BreedDAO.getAllBreedsAsObjects();
        ComboBox<Breed> mainBreedCombo = new ComboBox<>(FXCollections.observableList(all));
        mainBreedCombo.setValue(judge.getBreed());

        dataContainer_judge.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Age:"), ageField,
                new Label("Experience:"), expField,
                new Label("Breed:"), mainBreedCombo
        );

        VBox breedsContainer = new VBox(10);
        List<Breed> addBreeds = JudgeDAO.getJudgeBreeds(judge.getId());
        for (Breed b : addBreeds) {
            HBox bf = createBreedFields(b, mainBreedCombo);
            breedsContainer.getChildren().add(bf);
            breedFieldsList.add(bf);
        }

        // Кнопка добавить ещё доп. породу
        Button addBreedBtn = new Button("Add one more breed");
        addBreedBtn.setOnAction(e -> {
            if (isLastBreedFieldsValid()) {
                HBox bf = createBreedFields(null, mainBreedCombo);
                breedsContainer.getChildren().add(bf);
                breedFieldsList.add(bf);
            } else {
                showError("Please fill the last breed field before adding a new one.");
            }
        });

        dataContainer_judge.getChildren().addAll(
                new Label("Additional Breeds:"), breedsContainer, addBreedBtn
        );

        showEditModeButtons(judge, nameField, ageField, expField, mainBreedCombo);
    }

    /**
     * Кнопки Save / Clean в режиме редактирования.
     */
    private void showEditModeButtons(Judge judge,
                                     TextField nameField,
                                     TextField ageField,
                                     TextField expField,
                                     ComboBox<Breed> mainBreedCombo) {
        buttonContainer.getChildren().clear();

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            saveJudgeEdits(judge, nameField, ageField, expField, mainBreedCombo);
        });

        Button cleanBtn = new Button("Clean");
        cleanBtn.setOnAction(e -> {
            nameField.clear();
            ageField.clear();
            expField.clear();
            mainBreedCombo.setValue(null);

            // Очищаем все доп. поля
            for (HBox bf : breedFieldsList) {
                @SuppressWarnings("unchecked")
                ComboBox<Breed> c = (ComboBox<Breed>) bf.getChildren().get(0);
                c.setValue(null);
            }
        });

        buttonContainer.getChildren().addAll(cleanBtn, saveBtn);
    }

    /**
     * Пустая форма для добавления нового судьи (ID=0).
     */
    private void showEmptyJudgeForm() {
        dataContainer_judge.getChildren().clear();
        buttonContainer.getChildren().clear();
        breedFieldsList.clear();

        TextField nameField = new TextField();
        restrictToLetters(nameField);

        TextField ageField = new TextField();
        restrictToDigits(ageField);

        TextField expField = new TextField();
        restrictToDigits(expField);

        List<Breed> all = BreedDAO.getAllBreedsAsObjects();
        ComboBox<Breed> mainBreedCombo = new ComboBox<>(FXCollections.observableList(all));

        dataContainer_judge.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Age:"), ageField,
                new Label("Experience:"), expField,
                new Label("Breed:"), mainBreedCombo
        );

        VBox breedsContainer = new VBox(10);
        // Изначально без доп. полей.

        Button addBreedBtn = new Button("Add one more breed");
        addBreedBtn.setOnAction(e -> {
            if (isLastBreedFieldsValid()) {
                HBox bf = createBreedFields(null, mainBreedCombo);
                breedsContainer.getChildren().add(bf);
                breedFieldsList.add(bf);
            } else {
                showError("Please fill the last breed field before adding a new one.");
            }
        });

        dataContainer_judge.getChildren().addAll(
                new Label("Additional Breeds:"), breedsContainer, addBreedBtn
        );

        Judge newJudge = new Judge();

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(ev -> {
            saveJudgeEdits(newJudge, nameField, ageField, expField, mainBreedCombo);
        });

        Button cleanBtn = new Button("Clean");
        cleanBtn.setOnAction(ev -> {
            nameField.clear();
            ageField.clear();
            expField.clear();
            mainBreedCombo.setValue(null);
            for (HBox bf : breedFieldsList) {
                @SuppressWarnings("unchecked")
                ComboBox<Breed> c = (ComboBox<Breed>) bf.getChildren().get(0);
                c.setValue(null);
            }
        });

        buttonContainer.getChildren().addAll(cleanBtn, saveBtn);
    }

    /**
     * Создаёт ComboBox для дополнительной породы + кнопку "-" для удаления.
     * Удалена проверка "Cannot delete the only breed field."
     */
    private HBox createBreedFields(Breed breedValue, ComboBox<Breed> mainBreedCombo) {
        ComboBox<Breed> breedCombo = new ComboBox<>(FXCollections.observableList(
                BreedDAO.getAllBreedsAsObjects()
        ));
        breedCombo.setValue(breedValue);

        // При выборе проверяем дубликаты (включая mainBreed)
        breedCombo.setOnAction(ev -> {
            Breed sel = breedCombo.getValue();
            if (sel != null) {
                // Сравнить с mainBreed
                Breed mainB = mainBreedCombo.getValue();
                if (mainB != null && mainB.getId() == sel.getId()) {
                    showError("This breed is already used as Main Breed.");
                    breedCombo.setValue(null);
                    return;
                }
                // Сравнить с другими полями
                if (isBreedAlreadySelected(sel, breedCombo)) {
                    showError("This breed is already selected as Additional Breed.");
                    breedCombo.setValue(null);
                }
            }
        });

        // Удаляем поле без ограничения
        Button removeBtn = new Button("-");
        removeBtn.setOnAction(e -> {
            HBox box = (HBox) removeBtn.getParent();
            breedFieldsList.remove(box);
            ((VBox) box.getParent()).getChildren().remove(box);
        });

        return new HBox(10, breedCombo, removeBtn);
    }

    /**
     * Проверяет, заполнена ли последняя ComboBox.
     */
    private boolean isLastBreedFieldsValid() {
        if (breedFieldsList.isEmpty()) return true;
        HBox last = breedFieldsList.get(breedFieldsList.size() - 1);
        @SuppressWarnings("unchecked")
        ComboBox<Breed> combo = (ComboBox<Breed>) last.getChildren().get(0);
        return combo.getValue() != null;
    }

    /**
     * Проверяет, не выбрана ли такая же порода в других полях.
     */
    private boolean isBreedAlreadySelected(Breed sel, ComboBox<Breed> current) {
        for (HBox bf : breedFieldsList) {
            @SuppressWarnings("unchecked")
            ComboBox<Breed> c = (ComboBox<Breed>) bf.getChildren().get(0);
            if (c != current && c.getValue() != null && c.getValue().getId() == sel.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Сохранение/обновление судьи: основная порода + доп. породы.
     */
    private void saveJudgeEdits(Judge judge,
                                TextField nameF,
                                TextField ageF,
                                TextField expF,
                                ComboBox<Breed> mainBreedCombo) {
        try {
            String name = nameF.getText().trim();
            int age = Integer.parseInt(ageF.getText().trim());
            int exp = Integer.parseInt(expF.getText().trim());
            Breed mainBr = mainBreedCombo.getValue();

            if (name.isEmpty() || mainBr == null) {
                showError("Name and main breed must be specified.");
                return;
            }

            Judge tmp = new Judge(judge.getId(), name, age, exp, judge.getEvent(), mainBr,
                    judge.getCreatedAt(), judge.getUpdatedAt());
            if (!tmp.validate()) {
                return;
            }

            if (judge.getId() == 0) {
                // Новый судья
                if (JudgeDAO.insertJudge(tmp)) {
                    // Записываем доп. породы
                    JudgeDAO.deleteJudgeBreeds(tmp.getId());
                    saveJudgeBreeds(tmp.getId());
                }
            } else {
                judge.setName(name);
                judge.setAge(age);
                judge.setExperience(exp);
                judge.setBreed(mainBr);

                if (JudgeDAO.updateJudge(judge)) {
                    JudgeDAO.deleteJudgeBreeds(judge.getId());
                    saveJudgeBreeds(judge.getId());
                }
            }

            loadJudgeButtons();
            dataContainer_judge.getChildren().clear();
            isEditMode = false;

        } catch (NumberFormatException ex) {
            showError("Age and experience must be valid numbers.");
        }
    }

    /**
     * Сохраняет выбранные доп. породы в таблицу judge_breeds.
     */
    private void saveJudgeBreeds(int judgeId) {
        List<Integer> breedIds = new ArrayList<>();
        for (HBox bf : breedFieldsList) {
            @SuppressWarnings("unchecked")
            ComboBox<Breed> c = (ComboBox<Breed>) bf.getChildren().get(0);
            Breed b = c.getValue();
            if (b != null) {
                breedIds.add(b.getId());
            }
        }
        JudgeDAO.insertJudgeBreeds(judgeId, breedIds);
    }

    private boolean showConfirmationDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText(message);
        return alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Возвращает текущий Stage.
     */
    private Stage getStage() {
        return (Stage) Judge.getScene().getWindow();
    }

    private void closeApplication() {
        logger.debug("Закрытие приложения (Judge_controller)");
        getStage().close();
    }

    private void minimizeApplication() {
        logger.debug("Сворачивание приложения (Judge_controller)");
        getStage().setIconified(true);
    }

    private void maximizeApplication() {
        logger.debug("Полноэкранный режим (Judge_controller)");
        getStage().setFullScreen(!getStage().isFullScreen());
    }

    /**
     * Разрешаем ввод только англ. букв.
     */
    private void restrictToLetters(TextField tf) {
        tf.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (!e.getCharacter().matches("[a-zA-Z]")) {
                e.consume();
            }
        });
    }

    /**
     * Разрешаем ввод только цифр.
     */
    private void restrictToDigits(TextField tf) {
        tf.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (!e.getCharacter().matches("[0-9]")) {
                e.consume();
            }
        });
    }
}
