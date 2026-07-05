package framework;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

public class FrontServlet extends HttpServlet {
    
    private Map<MappingKey, MappingValue> urlMappingTable;

    @Override
    public void init() throws ServletException {
        this.urlMappingTable = (Map<MappingKey, MappingValue>) getServletContext().getAttribute("urlMappingTable");
        if (this.urlMappingTable == null) {
            throw new ServletException("Erreur critique : Impossible de recuperer la table de routage.");
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathInfo = requestURI.substring(contextPath.length());
        
        if (pathInfo.isEmpty()) {
            pathInfo = "/";
        }
        
        String httpMethod = request.getMethod();

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Framework</title></head>");
            out.println("<body>");
            
            if (pathInfo.equals("/") || pathInfo.equals("/index.html")) {
                
                out.println("<h1>Url capturee : " + requestURI + "</h1>");
                out.println("<p>Context Path : " + contextPath + "</p>");
                out.println("<hr>");
                
                out.println("<h2>URLs de routage detectees :</h2>");
                out.println("<ul>");
                for (Map.Entry<MappingKey, MappingValue> entry : urlMappingTable.entrySet()) {
                    MappingKey key = entry.getKey();
                    MappingValue value = entry.getValue();
                    out.println("<li>URL : <strong>" + key.getUrl() + "</strong> [" + key.getMethodType() + "] associee a -> " 
                            + value.getControllerClass().getName() + "." + value.getMethod().getName() + "()</li>");
                }
                out.println("</ul>");
                
                out.println("<hr>");
                out.println("<h2>Liste des urls tapees :</h2>");
                out.println("<ol><li>" + requestURI + "</li></ol>");
                out.println("<a href='#' onclick='window.location.reload();'>Mettre à jour</a>");
                
                out.println("<hr style='margin-top:40px;'>");
                out.println("<h2>Console de test des requêtes </h2>");
                
                out.println("<div style='margin-bottom: 20px;'>");
                out.println("   <p><strong>1. Tester l'URL en GET :</strong></p>");
                out.println("   <a href='" + contextPath + "/test' style='padding:8px 12px; background:#007bff; color:white; text-decoration:none; border-radius:4px;'>Lancer la requête GET</a>");
                out.println("</div>");
                
                out.println("<div>");
                out.println("   <p><strong>2. Tester l'URL en POST :</strong></p>");
                out.println("   <form action='" + contextPath + "/test' method='POST'>");
                out.println("       <button type='submit' style='padding:8px 12px; background:#28a745; color:white; border:none; border-radius:4px; cursor:pointer;'>Lancer la requête POST</button>");
                out.println("   </form>");
                out.println("</div>");

            } else {
                out.println("<h1>Aiguillage de la requete</h1>");
                out.println("<p>URL demandee : <strong>" + pathInfo + "</strong> | Methode : <strong>" + httpMethod + "</strong></p>");
                out.println("<hr>");

                MappingKey keyRecherche = new MappingKey(pathInfo, httpMethod);
                MappingValue mappingTarget = urlMappingTable.get(keyRecherche);

                if (mappingTarget != null) {
                    try {
                        Class<?> clazz = mappingTarget.getControllerClass();
                        Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                        Method targetMethod = mappingTarget.getMethod();
                        
                        Object result = targetMethod.invoke(controllerInstance);
                        
                        out.println("<h2>Resultat de l'execution :</h2>");
                        out.println("<div style='padding:15px; background:#e2f0d9; border:1px solid #385723; color:#2c5314;'>");
                        out.println("La methode a renvoye : <strong>" + result + "</strong>");
                        out.println("</div>");
                        
                    } catch (Exception e) {
                        out.println("<p style='color:red;'>Erreur : " + e.getMessage() + "</p>");
                    }
                } else {
                    out.println("<p style='color:red;'><strong>Erreur 404 :</strong> Aucune methode associee a " + pathInfo + " [" + httpMethod + "]</p>");
                }
                out.println("<br><br><a href=\"" + contextPath + "/\">Retour à l'accueil</a>");
            }

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