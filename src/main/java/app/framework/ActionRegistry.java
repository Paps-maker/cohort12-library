package app.framework;

import jakarta.enterprise.inject.spi.CDI;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ActionRegistry {


    public static void autoRegister(ActionMap actionMap, String basePackage) {
        try {
            System.out.println("=========================================================================");
            System.out.println("FRAMEWORK STARTUP: Initializing automated scanner for package: " + basePackage);
            System.out.println("=========================================================================");

            List<Class<?>> classes = findClasses(basePackage);
            System.out.println("FRAMEWORK SCANNER: Found " + classes.size() + " total class files in target directory.");

            int registeredCount = 0;
            for (Class<?> clazz : classes) {
                // Check if the class carries your custom framework @Controller qualifier beacon
                if (clazz.isAnnotationPresent(Controller.class)) {
                    // Fetch the live, context-aware managed instance from the CDI container
                    Object controllerInstance = CDI.current().select(clazz).get();

                    // Route the instance to your reflection-driven ActionMap mapping engine
                    actionMap.register(controllerInstance);
                    registeredCount++;
                    System.out.println("======> SUCCESS: Registered automated controller: " + clazz.getSimpleName());
                } else {
                    System.out.println("------> SKIPPED: " + clazz.getSimpleName() + " (Missing @Controller annotation)");
                }
            }

            System.out.println("=========================================================================");
            System.out.println("FRAMEWORK STARTUP: Automation complete. Successfully mapped " + registeredCount + " controllers.");
            System.out.println("=========================================================================");

        } catch (Exception e) {
            System.err.println("CRITICAL FRAMEWORK ERROR: Reflection-driven automation registry crashed entirely!");
            e.printStackTrace();
        }
    }

    /**
     * Entry point to read resources using standard file matching abstractions.
     */
    private static List<Class<?>> findClasses(String scannedPackage) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String scannedPath = scannedPackage.replace('.', '/');
        URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);

        if (scannedUrl == null) {
            System.err.println("SCANNER WARNING: Resource URL path not found for package: " + scannedPackage);
            return classes;
        }

        // Fix spaces, paths, and raw character encoding issues across different operating system filesystems
        String decodedPath = URLDecoder.decode(scannedUrl.getFile(), StandardCharsets.UTF_8);
        File scannedDir = new File(decodedPath);

        if (scannedDir.exists() && scannedDir.isDirectory()) {
            scanDirectory(scannedDir, scannedPackage, classes);
        } else {
            System.err.println("SCANNER WARNING: Targeted path directory is missing or invalid: " + decodedPath);
        }
        return classes;
    }

    /**
     * Recursive directory scanner that navigates deep sub-packages seamlessly.
     */
    private static void scanDirectory(File directory, String currentPackage, List<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // Handle nested package navigation cleanly (e.g., app.controller.admin)
                String subPackageName = currentPackage + "." + file.getName();
                scanDirectory(file, subPackageName, classes);
            } else if (file.getName().endsWith(".class")) {
                try {
                    String className = currentPackage + '.' + file.getName().replace(".class", "");
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    System.err.println("SCANNER ERROR: Could not map load binary token entry for resource file: " + file.getName());
                }
            }
        }
    }
}