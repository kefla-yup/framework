package framework;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PackageScanner {

    public static List<Class<?>> scanClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            String path = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(path);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                
                File directory = new File(resource.toURI());
                
                System.out.println("Scan du dossier reel : " + directory.getAbsolutePath());
                
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().endsWith(".class")) {
                                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                                Class<?> cls = Class.forName(className);
                                if (cls.isAnnotationPresent(mg.itu.annotation.Controller.class)) {
                                    classes.add(cls);
                                    System.out.println("Controleur trouve : " + className);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du scan : " + e.getMessage());
            e.printStackTrace();
        }
        return classes;
    }
}