package pe.gob.onpe.tramitedoc.web.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.EtiquetaBandejaEnBean;
import pe.gob.onpe.tramitedoc.bean.SiElementoBean;
import pe.gob.onpe.tramitedoc.bean.UsuarioConfigBean;
import pe.gob.onpe.tramitedoc.service.AvisoBandejaEntradaService;
import pe.gob.onpe.tramitedoc.service.ReferencedData;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;


@Controller("mainPanelController")
public class MainPanelController {
  @Autowired
  private AvisoBandejaEntradaService avisoBandejaEntradaService;
  @Autowired
  private ReferencedData referencedData;
  
  @RequestMapping({"/{version}/mainpanel.do"})
  public String showMainPanel(Model model, HttpServletRequest request) {
    UsuarioConfigBean usuario = Utilidades.getInstancia().loadUserConfigFromSession(request);
    model.addAttribute("siglasDep", usuario.getDeSiglasDep());
    model.addAttribute("bandejaEntradaList", this.avisoBandejaEntradaService.getBandejaEntrada(usuario.getCoDep(), usuario.getCempCodemp(), usuario.getTiAcceso()));
    String coDep = usuario.getCoDep();
    String coEmp = usuario.getCempCodemp();
    List<EtiquetaBandejaEnBean> etiquetasList = this.avisoBandejaEntradaService.getListEtiquetaBandejaEntrada(coDep, coEmp);
    model.addAttribute("etiquetasList", etiquetasList);
    etiquetasList = null;
    model.addAttribute("fechaActual", Utility.getInstancia().dateToFormatString(new Date()));
    String nameImgPortada = usuario.getNameImgPortadaSgd();
    if (nameImgPortada == null || nameImgPortada.trim().length() <= 0) {
      nameImgPortada = "default.jpg";
    }
    
    List<SiElementoBean> lstImPortadaOrigen = this.referencedData.grpElementoList("BANNER_INICIO");
    List<SiElementoBean> lstImPortada = new ArrayList<SiElementoBean>();
    
    for (int i = 0; i < lstImPortadaOrigen.size(); i++) {
      
      SiElementoBean oSiElementoBean = new SiElementoBean();
      oSiElementoBean = lstImPortadaOrigen.get(i);
      oSiElementoBean.setCeleDesele(Base64.getEncoder().encodeToString(oSiElementoBean.getArchivo()));
      lstImPortada.add(oSiElementoBean);
    } 
    model.addAttribute("lstImPortada", lstImPortada);
    SiElementoBean oSiElementoBean = null;
    lstImPortadaOrigen = null;
    lstImPortada = null;
    model.addAttribute("imgId", nameImgPortada);
    return "mainpanel";
  }
}
