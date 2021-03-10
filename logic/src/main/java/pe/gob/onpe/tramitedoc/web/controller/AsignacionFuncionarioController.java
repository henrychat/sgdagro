package pe.gob.onpe.tramitedoc.web.controller;

import java.beans.PropertyEditor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.libreria.util.ServletUtility;
import pe.gob.onpe.tramitedoc.bean.AsignacionFuncionarioBean;
import pe.gob.onpe.tramitedoc.bean.UsuarioConfigBean;
import pe.gob.onpe.tramitedoc.service.AsignacionFuncionarioService;
import pe.gob.onpe.tramitedoc.service.CommonQryService;
import pe.gob.onpe.tramitedoc.service.ReferencedData;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;






@Controller
@RequestMapping({"/{version}/srAsignacionFuncionario.do"})
public class AsignacionFuncionarioController
{
  @Autowired
  private ReferencedData referencedData;
  @Autowired
  private AsignacionFuncionarioService asignacionFuncionarioService;
  @Autowired
  private CommonQryService commonQryService;
  
  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    binder.registerCustomEditor(Date.class, (PropertyEditor)new CustomDateEditor(dateFormat, false));
  }

  
  @RequestMapping(method = {RequestMethod.GET}, params = {"accion=goAsignacionFuncionarios"})
  public String goAsignacionFuncionarios(HttpServletRequest request, AsignacionFuncionarioBean asignacionFuncionarioBean, Model model) {
    String url = "/asignacionFuncionarios/adminAsignacion";
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    
    if ("1".equals(usuarioConfigBean.getInEsAdmin())) {
      model.addAttribute("lista", this.asignacionFuncionarioService.getListAsignacionFuncionario(asignacionFuncionarioBean));
    } else {
      url = "";
    } 
    return url;
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goAsignacionFuncionarios"})
  public String goAsignacionFuncionarios(String param, HttpServletRequest request, AsignacionFuncionarioBean asignacionFuncionarioBean, Model model) {
    String mensaje = "NO_OK";
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    
    List list = null;
    if ("1".equals(usuarioConfigBean.getInEsAdmin())) {
      list = this.asignacionFuncionarioService.getListAsignacionFuncionario(asignacionFuncionarioBean);
    }
    if (list != null) {
      if (list.isEmpty()) {
        mensaje = "No se encuentran registros.";
      } else {
        if (list.size() >= 100) {
          model.addAttribute("msjEmision", "OPTIMICE SU CONSULTA");
        }
        model.addAttribute("lista", list);
        mensaje = "OK";
      } 
    } else {
      mensaje = "No se encuentran registros.";
    } 
    if (mensaje.equals("OK")) {
      return "/asignacionFuncionarios/adminAsignacionLista";
    }
    model.addAttribute("pMensaje", mensaje);
    return "respuesta";
  }

  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDialogAsignacion"}, produces = {"text/plain; charset=utf-8"})
  public String goDialogAsignacion(HttpServletRequest request, Model model) {
    AsignacionFuncionarioBean asignacionFuncionarioBean = new AsignacionFuncionarioBean();
    model.addAttribute("asignacionFuncionarioBean", asignacionFuncionarioBean);
    model.addAttribute("listaTipoEncargatura", this.referencedData.grpElementoList("CO_TIPO_ENC"));
    return "/asignacionFuncionarios/dlgNuevaAsignacion";
  }

  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDlgBuscarDependencia"})
  public String goDlgBuscarDependencia(HttpServletRequest request, Model model) {
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    model.addAttribute("iniFuncionParm", "10");
    model.addAttribute("listaDestinatario", this.commonQryService.getLsDepencia());
    return "/modalGeneral/consultaDestinatarioEmiConsul";
  }

  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goFuncionarioAsignado"})
  @ResponseBody
  public String goFuncionarioAsignado(HttpServletRequest request, Model model) {
    String coDependencia = ServletUtility.getInstancia().loadRequestParameter(request, "coDependencia");
    StringBuilder retval = new StringBuilder();
    String coRespuesta = "0";
    String vRespuesta = "No existen coincidencias";
    String vAsignacion = "";
    AsignacionFuncionarioBean asignacionFuncionarioBean = this.asignacionFuncionarioService.getFuncionarioPorDependencia(coDependencia);
    if (asignacionFuncionarioBean != null) {
      coRespuesta = "1";
      vRespuesta = "Datos encontrados";
      String empleado = "";
      if (asignacionFuncionarioBean.getDeEmpleado() != null) {
        empleado = asignacionFuncionarioBean.getDeEmpleado();
      }
      
      vAsignacion = "\",\"deEmpleado\":\"" + empleado + "\",\"coTipoEncargo\":\"" + asignacionFuncionarioBean.getDeTipoEncargo();
    } 
    int existe = this.asignacionFuncionarioService.getExisteProgramacionDependencia(coDependencia);
    if (existe > 0) {
      coRespuesta = "2";
      vRespuesta = "La dependencia tiene programaciones por asignar, <br/> Sin embargo se permite otra programacibajo citerio del usuario.";
    } 
    retval.append("{\"coRespuesta\":\"");
    retval.append(coRespuesta);
    retval.append(vAsignacion);
    retval.append("\",\"deRespuesta\":\"");
    retval.append(vRespuesta);
    retval.append("\"}");
    return retval.toString();
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDlgListaEmpleado"})
  public String goDlgListaEmpleado(HttpServletRequest request, Model model) {
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    model.addAttribute("iniFuncionParm", "7");
    model.addAttribute("listaEmpleado", this.commonQryService.getLsEmpleado(""));
    return "/modalGeneral/consultaElaboradoPorConsul";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goRegistrarAsignacionFuncionario"}, produces = {"text/plain; charset=utf-8"})
  @ResponseBody
  public String goRegistrarAsignacionFuncionario(AsignacionFuncionarioBean asignacionFuncionarioBean, HttpServletRequest request, Model model) {
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    String[] msg = new String[2];
    asignacionFuncionarioBean.setCoUseCre(usuario.getCoUsuario());
    msg = this.asignacionFuncionarioService.insAsignacionFuncionario(asignacionFuncionarioBean);
    StringBuilder retval = new StringBuilder();
    retval.append("{\"coRespuesta\":\"");
    retval.append(msg[0]);
    retval.append("\",\"deRespuesta\":\"");
    retval.append(msg[1]);
    retval.append("\"}");
    return retval.toString();
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goValidaEmpleadoAsignacion"}, produces = {"text/plain; charset=utf-8"})
  @ResponseBody
  public String goValidaEmpleadoAsignacion(HttpServletRequest request, Model model) {
    String coEmpleado = ServletUtility.getInstancia().loadRequestParameter(request, "coEmpleado");
    String coRespuesta = "0";
    String deRespuesta = "";
    try {
      int existe = this.asignacionFuncionarioService.getExisteProgramacionEmpleado(coEmpleado);
      if (existe > 0) {
        coRespuesta = "1";
        deRespuesta = "El empleado tiene programaciones por asignar, <br/> Sin embargo se permite otra programacibajo citerio del usuario.";
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    StringBuilder retval = new StringBuilder();
    retval.append("{\"coRespuesta\":\"");
    retval.append(coRespuesta);
    retval.append("\",\"deRespuesta\":\"");
    retval.append(deRespuesta);
    retval.append("\"}");
    return retval.toString();
  }
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goEliminaAsignacionEmpleado"}, produces = {"text/plain; charset=utf-8"})
  @ResponseBody
  public String goEliminaAsignacionEmpleado(HttpServletRequest request, Model model) {
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    long coAsignacion = Long.parseLong(ServletUtility.getInstancia().loadRequestParameter(request, "coAsignacion"));
    String coRespuesta = "0";
    String deRespuesta = "";
    try {
      AsignacionFuncionarioBean asignacionFuncionarioBean = new AsignacionFuncionarioBean();
      asignacionFuncionarioBean.setCoAsignacionFuncionario(coAsignacion);
      asignacionFuncionarioBean.setCoUseMod(usuario.getCoUsuario());
      coRespuesta = this.asignacionFuncionarioService.delAsignacionFuncionario(asignacionFuncionarioBean);
      if (coRespuesta.equals("1")) {
        deRespuesta = "Se elimincorrectamente el registro";
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      coRespuesta = "0";
      deRespuesta = "Error al eliminar el registro";
    } 
    StringBuilder retval = new StringBuilder();
    retval.append("{\"coRespuesta\":\"");
    retval.append(coRespuesta);
    retval.append("\",\"deRespuesta\":\"");
    retval.append(deRespuesta);
    retval.append("\"}");
    return retval.toString();
  }
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDialogEditarAsignacion"}, produces = {"text/plain; charset=utf-8"})
  public String goDialogEditarAsignacion(HttpServletRequest request, Model model) {
    long coAsignacion = Long.parseLong(ServletUtility.getInstancia().loadRequestParameter(request, "coAsignacion"));
    AsignacionFuncionarioBean asignacionFuncionarioBean = this.asignacionFuncionarioService.getAsignacionFuncionario(coAsignacion);
    model.addAttribute("asignacionFuncionarioBean", asignacionFuncionarioBean);
    model.addAttribute("listaTipoEncargatura", this.referencedData.grpElementoList("CO_TIPO_ENC"));
    return "/asignacionFuncionarios/dlgEditarAsignacion";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goActualizarAsignacionFuncionario"}, produces = {"text/plain; charset=utf-8"})
  @ResponseBody
  public String goActualizarAsignacionFuncionario(AsignacionFuncionarioBean asignacionFuncionarioBean, HttpServletRequest request, Model model) {
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    String[] msg = new String[2];
    try {
      asignacionFuncionarioBean.setCoUseMod(usuario.getCoUsuario());
      String vReturn = this.asignacionFuncionarioService.updAsignacionFuncionario(asignacionFuncionarioBean);
      if (vReturn.equals("1")) {
        msg[0] = "1";
        msg[1] = "Actualizado correctamente";
      } else if (vReturn.equals("2")) {
        msg[0] = "1";
        msg[1] = "Se actualizla informaciy se asignal funcionario.";
      } else if (vReturn.equals("3")) {
        msg[0] = "0";
        msg[1] = "Fecha de inicio no puede ser menor que la fecha del sistema.";
      } else {
        msg[0] = "0";
        msg[1] = "Error al actualizar el registro";
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
      msg[0] = "0";
      msg[1] = "Error en el metodo";
    } 
    StringBuilder retval = new StringBuilder();
    retval.append("{\"coRespuesta\":\"");
    retval.append(msg[0]);
    retval.append("\",\"deRespuesta\":\"");
    retval.append(msg[1]);
    retval.append("\"}");
    return retval.toString();
  }
}
