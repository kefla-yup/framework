package framework;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

public class PackageScanner {
    
    public static List<Class<?>> scanClasses (String packageName){
        List<Class<?>> classes = new ArrayList<>();
        try {
            String path = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(path);

            if(resource == null ){
                return classes;
            }

            File directory = new File(resource.getFile());
            if (directory.exists() && directory.isDirectory()){
                File[] files = directory.listFiles();
                if (files != null){
                    for(File file : files){
                        if (file.getName().endsWith(".class")){
                            String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                            Class<?> cls = Class.forName(className);
                            if (cls.isAnnotationPresent(mg.itu.annotation.Controller.class)){
                                classes.add(cls);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }
}
