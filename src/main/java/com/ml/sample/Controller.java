package com.ml.sample;

import com.ml.utils.ExecuteResult;
import com.ml.utils.LocalCommandExecutor;
import com.ml.utils.LocalCommandExecutorImpl;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Controller implements Initializable {

    LocalCommandExecutor localCommandExecutor = new LocalCommandExecutorImpl();

    List<String> deviceInfoRowList = Stream.of(
            "[ro.product.brand]",
            "[ro.product.device]",
            "[ro.product.locale]",
            "[ro.product.manufacturer]",
            "[ro.product.model]",
            "[ro.product.name]",
            "[ro.build.version.release]",
            "[ro.build.version.sdk]")
            .collect(Collectors.toList());

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TitledPane imgParent;
    @FXML
    private TitledPane deviceInfoParent;
    @FXML
    private TitledPane deviceListParent;
    @FXML
    private TitledPane resultParent;

    @FXML
    private Button getDevices;

    @FXML
    private Button wakeupDevice;

    @FXML
    private Button focusAppInfo;

    @FXML
    private Button activitiesFromTop2Bottom;

    @FXML
    private TextArea result;

    @FXML
    private ListView deviceList;

    @FXML
    private Button clearResult;

    @FXML
    private TextField textPassword;

    @FXML
    private ImageView deviceCap;

    @FXML
    private Button screenshot;

    @FXML
    private ProgressBar globalProgressBar;

    @FXML
    private CheckBox autoRefresh;

    @FXML
    private ListView deviceInfo;

    @FXML
    private Button chooseFile;

    public void setScreenshot() {
        asyncDeviceScreenshot();
    }

    /**
     * Clear result pane
     */
    public void setClearResult() {
        result.clear();
    }

    /**
     * Get all devices connected to the computer.
     */
    public void getAllDevices() {
        // Clear cache
        deviceList.getItems().clear();
        deviceInfo.getItems().clear();
        autoRefresh.setSelected(false);

        List<String> list = getDeviceList();
        result.appendText("Get all devices: \n");
        list.forEach(s -> {
            deviceList.getItems().add(s);
            result.appendText(s + "\n");
        });
        result.appendText("\n");
    }

    /**
     * Wake up one device
     */
    public void setWakeupDevice() {
        String selectedDevice = selectedDevice();
        if (selectedDevice.equals("NoDeviceSelected")) {
            result.appendText("Please select one device in device list \n");
            log.error("Please select one device in device list");
        } else {
            if (!wakeupDevice.isDisabled()) {
                wakeupDevice.setDisable(true);
            }
            globalProgressBar.setProgress(-1);
            Service<String> service = new Service<String>() {
                @Override
                protected Task<String> createTask() {
                    return new Task<String>() {
                        @Override
                        protected String call() throws Exception {
                            ExecuteResult executeResult = localCommandExecutor.executeCommand("adb -s " + selectedDevice + " shell dumpsys window policy", 2000);
                            if (executeResult.getExecuteOut().contains("mScreenOnEarly=false")) {
                                if (!textPassword.getText().equals("")) {
                                    result.appendText("Wake up device: \"" + selectedDevice + "\" with password: \"" + textPassword.getText() + "\".\n");
                                    localCommandExecutor.executeCommand("adb -s " + selectedDevice + " shell input keyevent 224", 2000);
                                    localCommandExecutor.executeCommand("adb -s " + selectedDevice + " shell input keyevent 82", 2000);
                                    localCommandExecutor.executeCommand("adb -s " + selectedDevice + " shell input text " + textPassword.getText(), 2000);
                                } else {
                                    result.appendText("Wake up device: \"" + selectedDevice + "\" without password.\n");
                                    localCommandExecutor.executeCommand("adb -s " + selectedDevice + " shell input keyevent 224", 2000);
                                }
                            } else {
                                log.info("Device already in active status.");
                                result.appendText("Device already in active status.\n");
                            }
                            return "success";
                        }
                    };
                }
            };

            service.setOnSucceeded((WorkerStateEvent event) -> {
                globalProgressBar.setProgress(0);
                if (wakeupDevice.isDisabled()) {
                    wakeupDevice.setDisable(false);
                }
                result.appendText("Wakeup device successful!");
                log.info("Wakeup device successful!");
            });

            service.setOnFailed((WorkerStateEvent event) -> {
                globalProgressBar.setProgress(0);
                if (wakeupDevice.isDisabled()) {
                    wakeupDevice.setDisable(false);
                }
                result.appendText("Error occurred!\n");
                log.info("Error occurred!");
            });

            service.setOnCancelled((WorkerStateEvent event) -> {
                globalProgressBar.setProgress(0);
                if (wakeupDevice.isDisabled()) {
                    wakeupDevice.setDisable(false);
                }
                result.appendText("Task Canceled!\n");
                log.info("Task canceled!");
            });

            service.start();
        }
    }

    /**
     * Get the current focus app package and activity info
     */
    public void setFocusAppInfo() {
        String getFocusWindow = "dumpsys window windows";
        commonADBShellExecution(getFocusWindow, "mCurrentFocus");
    }

    /**
     * Get all Activities from top to button
     */
    public void setActivitiesFromTop2Bottom() {
        String getAllActivity = "dumpsys activity activities";
        commonADBShellExecution(getAllActivity, "realActivity");

    }

    public List<String> getDeviceList() {
        ExecuteResult executeResult = localCommandExecutor.executeCommand("adb devices -l", 3000);

        return Arrays.stream(executeResult.getExecuteOut().trim().split("\n"))
                .filter(s -> !s.contains("attached"))
                .collect(Collectors.toList());
    }

    public String selectedDevice() {
        Object currentFocusDeviceInDeviceList = deviceList.getFocusModel().getFocusedItem();
        if (currentFocusDeviceInDeviceList != null && !currentFocusDeviceInDeviceList.toString().equals("")) {
            String currentDevice = currentFocusDeviceInDeviceList.toString().trim().split(" ")[0].trim();
            return currentDevice;
        } else {
            return "NoDeviceSelected";
        }
    }

    public void commonADBShellExecution(String adbShellCommand, String filterContain) {
        String selectedDevice = selectedDevice();
        if (selectedDevice.equals("NoDeviceSelected")) {
            result.appendText("Please select one device in device list \n");
            log.error("Please select one device in device list");
        } else {
            ExecuteResult executeResult = localCommandExecutor.executeCommand(
                    "adb -s "
                            + selectedDevice
                            + " shell "
                            + adbShellCommand, 2000);
            result.appendText("\n");
            Arrays.stream(executeResult.getExecuteOut().split("\n"))
                    .filter(s -> s.contains(filterContain))
                    .forEach(s -> {
                        result.appendText(s.trim() + "\n");
                        log.info("Result: " + s.trim());
                    });
        }
    }

    public void asyncDeviceScreenshot() {
        String selectedDevice = selectedDevice();
        if (selectedDevice.equals("NoDeviceSelected")) {
            result.appendText("Please select one device in device list \n");
            log.error("Please select one device in device list");
        } else {
            globalProgressBar.setProgress(-1);
            Service<String> service = new Service<String>() {
                @Override
                protected Task<String> createTask() {
                    return new Task<String>() {
                        @Override
                        protected String call() throws Exception {
                            deviceScreenshot();
                            return "success";
                        }
                    };
                }
            };

            service.setOnSucceeded((WorkerStateEvent event) -> {
                globalProgressBar.setProgress(0);
                log.info("Screenshot Complete!");
                if (autoRefresh.isSelected()) {
                    asyncDeviceScreenshot();
                }
            });

            service.setOnFailed((WorkerStateEvent event) -> {
                globalProgressBar.setProgress(0);
                result.appendText("Error occurred!\n");
                log.info("Error occurred!");
            });

            service.setOnCancelled((WorkerStateEvent event) -> {
                globalProgressBar.setProgress(0);
                result.appendText("Task Canceled!\n");
                log.info("Task canceled!");
            });

            service.start();
        }
    }

    public void deviceScreenshot() {
        String selectedDevice = selectedDevice();
        if (selectedDevice.equals("NoDeviceSelected")) {
            result.appendText("Please select one device in device list \n");
            log.error("Please select one device in device list");
        } else {
            Instant before = Instant.now();
            Process process = null;
            InputStream inputStream = null;
            try {
                String screenshotCommand = "adb -s " + selectedDevice + " exec-out screencap -p";
                log.info(screenshotCommand);
                process = Runtime.getRuntime().exec(screenshotCommand);
                inputStream = process.getInputStream();
                deviceCap.setImage(new Image(inputStream));
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (process != null) {
                    process.destroy();
                }
            }
            Instant after = Instant.now();
            result.appendText("Screenshot time cost(mills): " + (after.toEpochMilli() - before.toEpochMilli()) + "\n");
            log.info("Screenshot time cost(mills): " + (after.toEpochMilli() - before.toEpochMilli()));
        }
    }

    public String upperCase(String str) {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }

    public void filterProductInfo(String productInfo) {
        result.appendText("Get Device Info: \n");
        HashMap<String, String> deviceInfoRow = new HashMap<String, String>();

        Arrays.stream(productInfo.split("\n"))
                .filter(s -> s.startsWith("[ro.product") || s.startsWith("[ro.build"))
                .map(s -> {
                    String StringKey = s.split(":")[0].trim();
                    if (deviceInfoRowList.contains(StringKey)) {
                        return s;
                    } else {
                        return "";
                    }
                })
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    String key = s.split(":")[0].trim().replace("[", "").replace("]", "");
                    String value = s.split(":")[1].trim().replace("[", "").replace("]", "");
                    return String.join(": ", Arrays.stream(key.split("\\."))
                            .reduce((a, b) -> b)
                            .orElse(""), value);
                })
                .map(this::upperCase)
                .forEach((s) -> {
                    result.appendText(s + "\n");
                    deviceInfo.getItems().add(s);
                });
        result.appendText("\n");
    }

    public void installApp(String device, String apkFile) {
        globalProgressBar.setProgress(-1);
        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        result.appendText("Performing Streamed Install...\n");
                        ExecuteResult executeResult = localCommandExecutor.executeCommand("adb -s " + device + " install " + apkFile, 120000);
                        return "success";
                    }
                };
            }
        };

        service.setOnSucceeded((WorkerStateEvent event) -> {
            globalProgressBar.setProgress(0);
            if (chooseFile.isDisabled()) {
                chooseFile.setDisable(false);
            }
            result.appendText("App: \"" + apkFile + "\" installed successful!\n");
            log.info("App installed successful!");
        });

        service.setOnFailed((WorkerStateEvent event) -> {
            globalProgressBar.setProgress(0);
            if (chooseFile.isDisabled()) {
                chooseFile.setDisable(false);
            }
            result.appendText("Error occurred!\n");
            log.info("Error occurred!");
        });

        service.setOnCancelled((WorkerStateEvent event) -> {
            globalProgressBar.setProgress(0);
            if (chooseFile.isDisabled()) {
                chooseFile.setDisable(false);
            }
            result.appendText("Task Canceled!\n");
            log.info("Task canceled!");
        });

        service.start();
    }

    public void setChooseFile() {
        String selectedDevice = selectedDevice();
        if (selectedDevice.equals("NoDeviceSelected")) {
            result.appendText("Please select one device in device list \n");
            log.error("Please select one device in device list");
        } else {
            if (!chooseFile.isDisabled()) {
                chooseFile.setDisable(true);
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose APK file");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("APK files", "*.apk"));
            File apkFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
            if (apkFile != null) {
                installApp(selectedDevice, apkFile.getAbsolutePath());
            } else {
                chooseFile.setDisable(false);
            }
        }
    }

    public void asyncServiceGetCommandResult(String command, long timeout, Consumer<ExecuteResult> consumer) {
        globalProgressBar.setProgress(-1);
        Service<ExecuteResult> service = new Service<ExecuteResult>() {
            @Override
            protected Task<ExecuteResult> createTask() {
                return new Task<ExecuteResult>() {
                    @Override
                    protected ExecuteResult call() throws Exception {
                        ExecuteResult executeResult = localCommandExecutor.executeCommand(command, timeout);
                        return executeResult;
                    }
                };
            }
        };

        service.setOnSucceeded((WorkerStateEvent event) -> {
            consumer.accept(service.getValue());
            globalProgressBar.setProgress(0);
            log.info("Task completed!");
        });

        service.setOnFailed((WorkerStateEvent event) -> {
            globalProgressBar.setProgress(0);
            result.appendText("Error occurred!\n");
            log.info("Error occurred!");
        });

        service.setOnCancelled((WorkerStateEvent event) -> {
            globalProgressBar.setProgress(0);
            result.appendText("Task Canceled!\n");
            log.info("Task canceled!");
        });

        service.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getDeviceList().forEach(deviceList.getItems()::add);

        try {
            deviceCap.setImage(new Image(this.getClass().getClassLoader().getResource("DefaultAndroid.png").toURI().toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        deviceList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (oldValue == null && newValue != null) {
                    String deviceSelected = newValue.toString().split(" ")[0].trim();
                    ExecuteResult executeResult = localCommandExecutor.executeCommand("adb -s " + deviceSelected + " shell getprop", 2000);
                    filterProductInfo(executeResult.getExecuteOut());
                    asyncServiceGetCommandResult("adb -s " + deviceSelected + " shell wm size", 2000,
                            (ExecuteResult executeResult1) -> {
                                Arrays.stream(executeResult1.getExecuteOut().split("\n")).forEach(s -> {
                                    result.appendText(s + "\n");
                                    deviceInfo.getItems().add(s);
                                });
                            });
                    asyncServiceGetCommandResult("adb -s " + deviceSelected + " shell wm density", 2000,
                            (ExecuteResult executeResult1) -> {
                                Arrays.stream(executeResult1.getExecuteOut().split("\n")).forEach(s -> {
                                    result.appendText(s + "\n");
                                    deviceInfo.getItems().add(s);
                                });
                            });
                }
            }
        });

        chooseFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setChooseFile();
            }
        });
    }
}
