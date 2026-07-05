package framework;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mg.itu.annotation.UrlMapping;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        Map<MappingKey, MappingValue> urlMappingTable = new HashMap<>();

        try {
            getMap("controllers", urlMappingTable);
            
            context.setAttribute("urlMappingTable", urlMappingTable);
            System.out.println("Table de routage globale creee avec succes !");
            
        } catch (Exception e) {
            System.err.println("Échec du scan au démarrage : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void getMap(String packageName, Map<MappingKey, MappingValue> table) throws Exception {
        List<Class<?>> controllersDetectes = PackageScanner.scanClasses(packageName);
        
        for (Class<?> cls : controllersDetectes) {
            Method[] methodes = cls.getDeclaredMethods();
            for (Method m : methodes) {
                if (m.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping annotation = m.getAnnotation(UrlMapping.class);
                    
                    MappingKey key = new MappingKey(annotation.url(), annotation.method());
                    MappingValue value = new MappingValue(cls, m);
                    
                    if (table.containsKey(key)) {
                        MappingValue duplicate = table.get(key);
                        throw new Exception(String.format(
                            "ERREUR DE ROUTAGE : L'URL '%s' [%s] est deja associee a %s.%s(). " +
                            "Impossible de la réassigner à %s.%s().",
                            key.getUrl(), key.getMethodType(),
                            duplicate.getControllerClass().getName(), duplicate.getMethod().getName(),
                            cls.getName(), m.getName()
                        ));
                    }
                    
                    table.put(key, value);
                }
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}