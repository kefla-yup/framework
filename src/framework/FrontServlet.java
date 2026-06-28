package framework;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import mg.itu.annotation.UrlMapping;

public class FrontServlet extends HttpServlet {
    
    private final List<String> historiqueUrls = new CopyOnWriteArrayList<>();
    
    private final Map<String, Method> urlMappingTable = new HashMap<>();

    @Override
    public void init() throws ServletException {
        try {
            List<Class<?>> controllersDetectes = PackageScanner.scanClasses("controllers");
            
            for (Class<?> cls : controllersDetectes) {
                Method[] methodes = cls.getDeclaredMethods();
                for (Method m : methodes) {
                    if (m.isAnnotationPresent(UrlMapping.class)) {
                        UrlMapping mapping = m.getAnnotation(UrlMapping.class);
                        String urlAssociee = mapping.url();
                        
                        urlMappingTable.put(urlAssociee, m);
                    }
                }
            }
            System.out.println("Framework initialise. Nombre d'URLs mappees : " + urlMappingTable.size());
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation du mapping", e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        historiqueUrls.add(requestURI);

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Framework</title></head>");
            out.println("<body>");
            
            out.println("<h1>Url capturee : " + requestURI + "</h1>");
            out.println("<p>Context Path : " + contextPath + "</p>");
            
            out.println("<hr>");
            
            out.println("<h2>URLs de routage detectees :</h2>");
            out.println("<ul>");
            
            if (urlMappingTable.isEmpty()) {
                out.println("<li><em>Aucune URL cartographiee dans la HashMap.</em></li>");
            } else {
                for (Map.Entry<String, Method> entry : urlMappingTable.entrySet()) {
                    String url = entry.getKey();
                    Method methode = entry.getValue();
                    Class<?> classeControleur = methode.getDeclaringClass();
                    
                    out.println("<li>URL : <strong>" + url + "</strong> associee a -> " 
                            + classeControleur.getName() + "." + methode.getName() + "()</li>");
                }
            }
            out.println("</ul>");
            
            out.println("<hr>");
            out.println("<h2>Liste des urls tapees :</h2>");
            out.println("<ol>");
            for (String url : historiqueUrls) {
                out.println("<li>" + url + "</li>");
            }
            out.println("</ol>");
            
            out.println("<br><a href=\"" + contextPath + "/index.html\">Retour</a>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}