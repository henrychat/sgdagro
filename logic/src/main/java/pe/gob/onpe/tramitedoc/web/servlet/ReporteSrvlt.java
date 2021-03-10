package pe.gob.onpe.tramitedoc.web.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import pe.gob.onpe.tramitedoc.service.ReporteService;

public class ReporteSrvlt extends HttpServlet {
  private ReporteService reporteService;
  
  public void init() throws ServletException {
    super.init();
    WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
    this.reporteService = (ReporteService)applicationContext.getBean("reporteService");
  }
  
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    loadReporte(request, response);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    loadReporte(request, response);
  }
  
  private void loadReporte(HttpServletRequest request, HttpServletResponse response) throws IOException {
    this.reporteService.abrirReporteTemporal(request, response);
  }
}
