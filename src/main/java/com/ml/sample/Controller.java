package com.ml.sample;

import com.ml.utils.ExecuteResult;
import com.ml.utils.LocalCommandExecutor;
import com.ml.utils.LocalCommandExecutorImpl;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Controller implements Initializable {

    LocalCommandExecutor localCommandExecutor = new LocalCommandExecutorImpl();

    @FXML
    private Pane rootPane;

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
                log.info("Complete!");
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
                log.info("Complete!");
                if (autoRefresh.isSelected()) {
                    asyncDeviceScreenshot();
                }
            });

            service.start();
        }
    }

    public void deviceScreenshot2() {
        Instant a1 = Instant.now();
        deviceCap.setImage(new Image("file:/D:/CL/Grocery/androidViewer/screenshots/202.png"));
        Instant a2 = Instant.now();
        log.info("A: " + (a2.toEpochMilli() - a1.toEpochMilli()));
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
                List<String> deviceInfoRow = Stream.of(
                        "brand",
                        "device",
                        "first_api_level",
                        "locale",
                        "manufacturer",
                        "model",
                        "name")
                        .collect(Collectors.toList());
                ExecuteResult executeResult = localCommandExecutor.executeCommand("adb shell getprop", 2000);
                Arrays.stream(executeResult.getExecuteOut().split("\n"))
                        .filter((s) -> s.contains("product") && s.contains("vendor") && !s.contains("cpu"))
                        .map((s) -> s.replace("[", "").replace("]", "").trim())
                        .map((s) -> {
                            return Arrays.stream(s.split("\\."))
                                    .reduce((a, b) -> b)
                                    .orElse("");
                        })
                        .map((s) -> {
                            return Arrays.stream(s.split(":"))
                                    .map(s1 -> {
                                        char[] cs=s1.toCharArray();
                                        cs[0]-=32;
                                        return String.valueOf(cs);
                                    })
                                    .collect(Collectors.joining(": "));
                        })
                        .forEach((s) -> {
                            System.out.println(s);
                            deviceInfo.getItems().add(s);
                        });
            }
        });
    }
}
