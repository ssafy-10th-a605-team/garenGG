package org.example.garencrawling.global;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.SimpleDateFormat;
import java.util.*;

public class GlobalConstants {

    public static final int corePoolSize = 5;
    public static final int maxPoolSize = 5;
    public static final int queueCapacity = 50;

    public static final int threadSize = 5;
    public static final int saveSize = 10;
    public static final int waitTime = 10;

    public static final HashMap<String, String> championNames = new HashMap<>();

    public static ChromeOptions options;

    public static ArrayList<ChromeDriver> drivers = new ArrayList<>();
    public static ArrayList<WebDriverWait> waits = new ArrayList<>();

    public static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}