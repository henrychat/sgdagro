package pe.gob.onpe.tramitedoc.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.autentica.model.UsuarioAcceso;
import pe.gob.onpe.tramitedoc.service.RecursosService;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;

public class RecursosSrvlt extends HttpServlet {

    private RecursosService recursosService;

    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    private static final String PIXEL_B64 = "R0lGODlhAQABAIAAAP///wAAACH5BAEUAAAALAAAAAABAAEAAAICRAEAOw==";

    public void init() throws ServletException {
        super.init();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (accion.equals("opcionesMenu")) {
                uploadMenu(request, response);
            } else if (accion.equals("loadObjs")) {
                loadObjs(request, response);
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void uploadMenu(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        boolean sucessfull = false;
        String valMenu = new String();
        valMenu = "{'rows': [";
        List<UsuarioAcceso> olist = usuario.getUsuarioAccesos();
        UsuarioAcceso obean = new UsuarioAcceso();
        StringBuilder retval = new StringBuilder();
        StringBuilder prepare = new StringBuilder();
        String vtiUsuario = "0";
        valMenu = valMenu + "{'cell':['TIUSUARIO','" + vtiUsuario + "']}";
        int i = 0;
        try {
            valMenu = valMenu + "]}";
            prepare.append("'menuItem':" + valMenu);
            sucessfull = true;
        } catch (Exception e) {
            sucessfull = false;
            e.printStackTrace();
        }
        retval.append("{'retmenu':");
        retval.append(sucessfull);
        retval.append("}");
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(retval);
        out.close();
        out.flush();
    }

    private void loadObjs(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
        this.recursosService = (RecursosService) applicationContext.getBean("recursosService");
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "max-age=31556926");
        PrintWriter out = response.getWriter();
        out.println(this.recursosService.getAllObjects());
        out.close();
        out.flush();
    }
}
