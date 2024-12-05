package com.example.individualproject.service;

import java.util.List;

public interface LogCheckerService {

    public void checkLogs(String path);

    public void checkLogs(List<String> lines);
    public void writeOutput();
}
