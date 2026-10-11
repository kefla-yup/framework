package framework;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import mg.itu.annotation.RestApi;

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
            getServletContext().getNamedDispatcher("default").forward(request, response);
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
                
                Parameter[] parameters = targetMethod.getParameters();
                Object[] methodArgs = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    methodArgs[i] = ParameterUtils.bindParameter(parameters[i], request);
                }

                Object result = targetMethod.invoke(controllerInstance, methodArgs);
                
                boolean isRestApi = targetMethod.isAnnotationPresent(RestApi.class);

                if (isRestApi) {
                    response.setContentType("application/json;charset=UTF-8");
                    
                    try (PrintWriter out = response.getWriter()) {
                        if (result == null) {
                            out.print("null");
                        } 
                        else if (result instanceof String) {
                            out.print(result.toString());
                        } 
                        else {
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            out.print(gson.toJson(result));
                        }
                    }
                } 
                else {
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
                            out.println("<p>Résultat : <strong>" + result.toString() + "</strong></p>");
                        }
                    }
                }
                
            } catch (Exception e) {
                throw new ServletException("Erreur lors de l'exécution du contrôleur", e);
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