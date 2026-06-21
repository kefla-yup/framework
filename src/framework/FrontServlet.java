package framework;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class FrontServlet extends HttpServlet {
    
    private final List<String> historiqueUrls = new CopyOnWriteArrayList<>();
    private List<Class<?>> controllersDetectes = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        try {
            this.controllersDetectes = PackageScanner.scanClasses("controllers");
            System.out.println("Framework initialise. Nombre de controleurs trouves : " + controllersDetectes.size());
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des controleurs", e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        historiqueUrls.add(requestURI);
        
        System.out.println("Nouvelle URL : " + requestURI);

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Framework</title></head>");
            out.println("<body>");
            
            out.println("<h1>Url capturee : " + requestURI + "</h1>");
            out.println("<p>Context Path : " + contextPath + "</p>");
            
            out.println("<hr>");
            
            // Affichage des controleurs detectes au demarrage
            out.println("<h2>Controleurs detectes au demarrage :</h2>");
            out.println("<ul>");
            for (Class<?> cls : controllersDetectes) {
                out.println("<li>" + cls.getName() + "</li>");
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}