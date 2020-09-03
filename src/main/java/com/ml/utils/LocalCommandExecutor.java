package com.ml.utils;

public interface LocalCommandExecutor {
    ExecuteResult executeCommand(String command, long timeout);
}
