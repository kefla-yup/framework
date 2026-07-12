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
    private String viewPrefix;
    private String viewSuffix;

    @Override
    public void init() throws ServletException {
        this.urlMappingTable = (Map<MappingKey, MappingValue>) getServletContext().getAttribute("urlMappingTable");
        if (this.urlMappingTable == null) {
            throw new ServletException("Erreur critique : Impossible de récupérer la table de routage globale.");
        }

        this.viewPrefix = getInitParameter("view.prefix");
        this.viewSuffix = getInitParameter("view.suffix");

        if (this.viewPrefix == null) this.viewPrefix = "";
        if (this.viewSuffix == null) this.viewSuffix = "";
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        String pathInfo = requestURI.substring(contextPath.length());
        
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/") || pathInfo.equals("/index.html")) {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head><title>Framework - Table de Routage</title></head>");
                out.println("<body style='font-family: sans-serif; margin: 40px;'>");
                out.println("<h1>Url capturée : " + requestURI + "</h1>");
                out.println("<p>Context Path : " + contextPath + "</p>");
                out.println("<hr>");
                
                out.println("<h2>URLs de routage détectées dans l'application :</h2>");
                out.println("<ul>");
                if (urlMappingTable != null) {
                    for (Map.Entry<MappingKey, MappingValue> entry : urlMappingTable.entrySet()) {
                        MappingKey key = entry.getKey();
                        MappingValue value = entry.getValue();
                        out.println("<li>URL : <strong>" + key.getUrl() + "</strong> [" + key.getMethodType() + "] associée à -> " 
                                + value.getControllerClass().getName() + "." + value.getMethod().getName() + "()</li>");
                    }
                }
                out.println("</ul>");
                out.println("<hr>");
                
                out.println("<h2>Console de test :</h2>");
                out.println("<p><a href='" + contextPath + "/liste-utilisateurs'>Tester</a></p>");
                
                out.println("</body>");
                out.println("</html>");
            }
            return;
        }

        String httpMethod = request.getMethod();
        MappingKey keyRecherche = new MappingKey(pathInfo, httpMethod);
        MappingValue mappingTarget = urlMappingTable.get(keyRecherche);

        if (mappingTarget != null) {
            try {
                Class<?> clazz = mappingTarget.getControllerClass();
                Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                Method targetMethod = mappingTarget.getMethod();
                
                Object result = targetMethod.invoke(controllerInstance);
                
                if (result instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) result;
                    
                    for (Map.Entry<String, Object> entry : mv.getModel().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }
                    
                    String fullViewPath = this.viewPrefix + mv.getView() + this.viewSuffix;
                    
                    request.getRequestDispatcher(fullViewPath).forward(request, response);
                } 
                else if (result != null) {
                    response.setContentType("text/html;charset=UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.println("<p>Le contrôleur a renvoyé une chaîne brute : <strong>" + result.toString() + "</strong></p>");
                    }
                }
                
            } catch (Exception e) {
                throw new ServletException("Erreur lors de l'exécution de la méthode du contrôleur", e);
            }
        } else {
            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try (PrintWriter out = response.getWriter()) {
                out.println("<h1 style='color:red;'>Erreur 404 : Non trouvé</h1>");
                out.println("<p>Aucune méthode associée à l'URL <strong>" + pathInfo + "</strong> avec le verbe <strong>" + httpMethod + "</strong></p>");
                out.println("<br><a href='" + contextPath + "/'>Retour à l'accueil</a>");
            }
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