package pe.gob.onpe.tramitedoc.web.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.libreria.exception.validarDatoException;
import pe.gob.onpe.libreria.util.ServletUtility;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.AvanceBean;
import pe.gob.onpe.tramitedoc.bean.DatosInterBean;
import pe.gob.onpe.tramitedoc.bean.DestinoResBen;
import pe.gob.onpe.tramitedoc.bean.DocRespuestaBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoAnexoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoAnexoBeansContenedor;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoFileBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoRecepMensajeriaBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoVerBean;
import pe.gob.onpe.tramitedoc.bean.RemitosResBean;
import pe.gob.onpe.tramitedoc.bean.TemaBean;
import pe.gob.onpe.tramitedoc.bean.UsuarioConfigBean;
import pe.gob.onpe.tramitedoc.dao.DatosPlantillaDao;
import pe.gob.onpe.tramitedoc.service.AnexoDocumentoService;
import pe.gob.onpe.tramitedoc.service.AvanceService;
import pe.gob.onpe.tramitedoc.service.CommonQryService;
import pe.gob.onpe.tramitedoc.service.DocumentoBasicoService;
import pe.gob.onpe.tramitedoc.service.DocumentoExtRecepService;
import pe.gob.onpe.tramitedoc.service.DocumentoMensajeriaService;
import pe.gob.onpe.tramitedoc.service.DocumentoObjService;
import pe.gob.onpe.tramitedoc.service.EmiDocumentoAdmService;
import pe.gob.onpe.tramitedoc.service.TemaService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;
import pe.gob.segdi.pide.wsentidad.WsEntidadInter;

@Controller
@RequestMapping({"/{version}/srDocObjeto.do"})
@SessionAttributes({"docSession"})
public class DocumentoObjetoController {

    @Autowired
    private DocumentoObjService documentoObjService;
    @Autowired
    private EmiDocumentoAdmService EmiDocumentoAdmService;
    @Autowired
    private AnexoDocumentoService anexoDocumentoService;
    @Autowired
    private DocumentoBasicoService documentoBasicoService;
    @Autowired
    private DocumentoExtRecepService documentoExtRecepService;
    @Autowired
    private CommonQryService commonQryService;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private DocumentoMensajeriaService documentoMensajeriaService;
    @Autowired
    private TemaService temaService;
    @Autowired
    private AvanceService avanceService;
    @Autowired
    private DatosPlantillaDao datosPlantillaDao;

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDocRutaAbrir"})
    @ResponseBody
    private String goObtieneDocumento(HttpServletRequest request, Model model) throws Exception {
        String retval = "";
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        DocRespuestaBean retRespuesta = new DocRespuestaBean();

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pTiOpe = ServletUtility.getInstancia().loadRequestParameter(request, "tiOpe");

        if (pNuAnn != null && pNuEmi != null && pTiOpe != null) {

            try {
                DocumentoVerBean docVerBean = new DocumentoVerBean();
                docVerBean = this.documentoObjService.getNombreDoc(pNuAnn, pNuEmi, pTiOpe, usuario);

                retRespuesta.setRetval(docVerBean.getDeMensaje());
                retRespuesta.setNuAnn(docVerBean.getNuAnn());
                retRespuesta.setNuEmi(docVerBean.getNuEmi());
                retRespuesta.setNoDoc(docVerBean.getNoDocumento());
                retRespuesta.setNoFirma(docVerBean.getNoFirma());
                retRespuesta.setNoUrl(docVerBean.getUrlDocumento());
            } catch (Exception e) {
                retRespuesta.setRetval("Error en Obtener nombre del documento 3");
                e.printStackTrace();
            }

            try {
                Writer strWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(strWriter, retRespuesta);
                retval = strWriter.toString();
            } catch (Exception e) {
                retval = "";
            }
        }

        return retval;
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDocInicialRutaAbrir"})
    @ResponseBody
    private String goDocInicialRutaAbrir(HttpServletRequest request, Model model) throws Exception {
        String retval = "";

        DocRespuestaBean retRespuesta = new DocRespuestaBean();

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pNuDes = ServletUtility.getInstancia().loadRequestParameter(request, "nuDes");

        DocumentoAnexoBean oDocumentoAnexoBean = new DocumentoAnexoBean();
        oDocumentoAnexoBean = this.anexoDocumentoService.getReferenciaInicial(pNuAnn, pNuEmi, pNuDes);
        if (oDocumentoAnexoBean.getNuAnn() != null && oDocumentoAnexoBean.getNuEmi() != null) {

            try {
                DocumentoVerBean docVerBean = new DocumentoVerBean();
                docVerBean = this.documentoObjService.getNombreDocInicial(oDocumentoAnexoBean.getNuAnn(), oDocumentoAnexoBean.getNuEmi());

                retRespuesta.setRetval(docVerBean.getDeMensaje());
                retRespuesta.setNuAnn(docVerBean.getNuAnn());
                retRespuesta.setNuEmi(docVerBean.getNuEmi());
                retRespuesta.setNoDoc(docVerBean.getNoDocumento());
                retRespuesta.setNoFirma(docVerBean.getNoFirma());
                retRespuesta.setNoUrl(docVerBean.getUrlDocumento());
            } catch (Exception e) {
                retRespuesta.setRetval("Error en Obtener nombre del documento 4");
                e.printStackTrace();
            }

            try {
                Writer strWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(strWriter, retRespuesta);
                retval = strWriter.toString();
            } catch (Exception e) {
                retval = "";
            }
        }

        return retval;
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goRutaGeneraDocx"})
    @ResponseBody
    private String goRutaGeneraDocx(HttpServletRequest request, Model model) throws Exception {
        String retval = "";
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        DocRespuestaBean retRespuesta = new DocRespuestaBean();
        DocumentoObjBean documentoObjBean = null;

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pTiOpe = ServletUtility.getInstancia().loadRequestParameter(request, "tiOpe");

        if (pNuAnn != null && pNuEmi != null && pTiOpe != null) {

            try {
                DocumentoVerBean docVerBean = new DocumentoVerBean();
                docVerBean = this.documentoObjService.getNombreGeneraDocx(pNuAnn, pNuEmi, pTiOpe);
                documentoObjBean = this.documentoObjService.getNombreArchivo(pNuAnn, pNuEmi, pTiOpe);

                retRespuesta.setRetval(docVerBean.getDeMensaje());
                retRespuesta.setNuAnn(docVerBean.getNuAnn());
                retRespuesta.setNuEmi(docVerBean.getNuEmi());
                retRespuesta.setNoDoc(docVerBean.getNoDocumento());
                retRespuesta.setNoFirma(docVerBean.getNoFirma());
                retRespuesta.setNoUrl(docVerBean.getUrlDocumento());

                if (documentoObjBean != null) {

                    //retRespuesta.setTieneWord((documentoObjBean.getwNombreArchivo() != null && documentoObjBean.getwNombreArchivo().length() > 0) ? "SI" : "NO");
                    retRespuesta.setTieneWord((documentoObjBean.getNombreArchivo() != null && documentoObjBean.getNombreArchivo().length() > 0) ? "SI" : "NO");
                } else {

                    retRespuesta.setTieneWord("NO");
                   // System.out.println("tiene word: "+retRespuesta.getTieneWord());
                }

            } catch (Exception e) {
                retRespuesta.setRetval("Error en Obtener nombre del documento 5");
                e.printStackTrace();
            }

            try {
                Writer strWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(strWriter, retRespuesta);
                retval = strWriter.toString();
            } catch (Exception e) {
                retval = "";
            }
        }

        return retval;
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDocRutaAnexo"})
    @ResponseBody
    private String goObtieneDocumentoAnexo(HttpServletRequest request, Model model) throws Exception {
        String retval = "";
        DocRespuestaBean retRespuesta = new DocRespuestaBean();
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pNuAne = ServletUtility.getInstancia().loadRequestParameter(request, "nuAne");

        if (pNuAnn != null && pNuEmi != null && pNuAne != null) {

            try {
                DocumentoVerBean docVerBean = new DocumentoVerBean();
                if (pNuAne.equals("ALL")) {
                    docVerBean = this.documentoObjService.getNombreDocAnexo(pNuAnn, pNuEmi, "1");
                    retRespuesta.setNoDoc("TEMP|DocumentosAnexos.zip");
                    retRespuesta.setNoUrl(docVerBean.getUrlDocumento().replace("abrirAnexo", "abrirAnexos"));
                } else {

                    docVerBean = this.documentoObjService.getNombreDocAnexo(pNuAnn, pNuEmi, pNuAne);

                    retRespuesta.setNoDoc(docVerBean.getNoDocumento());
                    retRespuesta.setNoUrl(docVerBean.getUrlDocumento());
                }
                retRespuesta.setRetval(docVerBean.getDeMensaje());
                retRespuesta.setNuAnn(docVerBean.getNuAnn());
                retRespuesta.setNuEmi(docVerBean.getNuEmi());
                retRespuesta.setNuAne(docVerBean.getNuAne());
                if (docVerBean.isInDoc() == false) {
                    Writer strWriter = new StringWriter();
                    retRespuesta.setRetval("Archivo Vacío");
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writeValue(strWriter, retRespuesta);
                    retval = strWriter.toString();
                    return retval;
                }
            } catch (Exception e) {
                retRespuesta.setRetval("Error en Obtener nombre del Anexo1");
                e.printStackTrace();
            }
            try {
                Writer strWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(strWriter, retRespuesta);
                retval = strWriter.toString();
            } catch (Exception e) {
                retval = "";
            }
        }

        return retval;
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goCopiarAnexo"})
    @ResponseBody
    private String goCopiarAnexo(HttpServletRequest request, Model model) throws Exception {
        String retval = "";
        DocRespuestaBean retRespuesta = new DocRespuestaBean();
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pNuAne = ServletUtility.getInstancia().loadRequestParameter(request, "nuAne");
        //String pNuAnnDocProyecto = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnnDocProyecto");
        String pNuAnnDocProyecto = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmiDocProyecto").substring(0, 4);
        String pNuEmiDocProyecto = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmiDocProyecto").substring(4, 14);
        if (pNuAnn != null && pNuEmi != null && pNuAne != null) {

            try {
                String result = "";
                result = this.documentoObjService.CopiarAnexo(pNuAnn, pNuEmi, pNuAne, pNuAnnDocProyecto, pNuEmiDocProyecto, usuario.getCoUsuario());
                if (result.equals("S")) {
                    retRespuesta.setRetval(" Se ha agregado el anexo al Documento ");
                } else {
                    retRespuesta.setRetval(" No se pudo agregar el anexo al Documento ");
                }

            } catch (Exception e) {
                retRespuesta.setRetval("Error en Obtener nombre del Anexo4");
                e.printStackTrace();
            }
            try {
                Writer strWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(strWriter, retRespuesta);
                retval = strWriter.toString();
            } catch (Exception e) {
                retval = "";
            }
        }

        return retval;
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDocRutaAnexoArchivado"})
    @ResponseBody
    private String goObtieneDocumentoAnexoArchivado(HttpServletRequest request, Model model) throws Exception {
        String retval = "";
        DocRespuestaBean retRespuesta = new DocRespuestaBean();
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pnuDes = ServletUtility.getInstancia().loadRequestParameter(request, "nuDes");

        if (pNuAnn != null && pNuEmi != null && pnuDes != null) {

            try {
                DocumentoVerBean docVerBean = new DocumentoVerBean();
                docVerBean = this.documentoObjService.getNombreDocAnexoArchivado(pNuAnn, pNuEmi, pnuDes);

                retRespuesta.setRetval(docVerBean.getDeMensaje());
                retRespuesta.setNuAnn(docVerBean.getNuAnn());
                retRespuesta.setNuEmi(docVerBean.getNuEmi());
                retRespuesta.setNuAne(docVerBean.getNuAne());
                retRespuesta.setNoDoc(docVerBean.getNoDocumento());
                retRespuesta.setNoUrl(docVerBean.getUrlDocumento());
            } catch (Exception e) {
                retRespuesta.setRetval("Error en Obtener nombre del Anexo5");
                e.printStackTrace();
            }
            try {
                Writer strWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(strWriter, retRespuesta);
                retval = strWriter.toString();
            } catch (Exception e) {
                retval = "";
            }
        }

        return retval;
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDocSeguimiento"})
    public String goDocSeguimiento(HttpServletRequest request, Model model) {
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        String pkExp = null;
        try {
            pkExp = this.documentoExtRecepService.getPkExpDocExtOrigen(pkEmi);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("frm_docOrigenBean_pkExp", pkExp);
        model.addAttribute("pkEmiDoc", pkEmi);
        return "/modalGeneral/consultaSeguimiento";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goVincularTema"})
    public String goVincularTema(HttpServletRequest request, Model model) {
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        String tipoRecep = ServletUtility.getInstancia().loadRequestParameter(request, "tipoRecep");
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        TemaBean temabean = new TemaBean();
        temabean.setCoDependencia(usuario.getCoDep());
        List<TemaBean> tema = this.temaService.getListTema(temabean);
        String codTema = "";
        if (tipoRecep.equals("RECEPCION")) {
            codTema = this.temaService.getEditTemaDestinos(pkEmi);
        }
        if (tipoRecep.equals("EMISION")) {
            codTema = this.temaService.getEditTemaRemitos(pkEmi);
        }
        temabean.setCoTema(codTema);
        model.addAttribute("deListTema", tema);
        model.addAttribute("temaBean", temabean);
        model.addAttribute("codigoEmision", pkEmi);
        model.addAttribute("codigoTipo", tipoRecep);
        return "/modalGeneral/registroVincularTema";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goGrabarTema"}, produces = {"text/plain; charset=utf-8"})
    @ResponseBody
    public String goGrabarTema(HttpServletRequest request, Model model) {
        String coRespuesta, mensaje = "NO_OK";
        String codigoTema = ServletUtility.getInstancia().loadRequestParameter(request, "coTema");
        String codigoEmision = ServletUtility.getInstancia().loadRequestParameter(request, "codigoEmision");
        String codTipo = ServletUtility.getInstancia().loadRequestParameter(request, "codTipo");

        try {
            if (codTipo.equals("EMISION")) {
                mensaje = this.temaService.updTemaRemitos(codigoEmision, codigoTema);
            }
            if (codTipo.equals("RECEPCION")) {
                mensaje = this.temaService.updTemaDestinos(codigoEmision, codigoTema);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mensaje.equals("OK")) {
            coRespuesta = "1";
        } else {
            coRespuesta = "0";
        }
        String deRespuesta = mensaje;

        StringBuilder retval = new StringBuilder();
        retval.append("{\"coRespuesta\":\"");
        retval.append(coRespuesta);
        retval.append("\",\"deRespuesta\":\"");
        retval.append(deRespuesta);
        retval.append("\"");
        retval.append("}");

        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goVerAvance"})
    public String goVerAvance(HttpServletRequest request, Model model) {
        String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
        String pnuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pnuEmi");
        String pnuDes = ServletUtility.getInstancia().loadRequestParameter(request, "pnuDes");
        AvanceBean avance = new AvanceBean();
        avance.setTiAvance("RECEP");
        avance.setNuAnn(pnuAnn);
        avance.setNuEmi(pnuEmi);
        avance.setNuDes(pnuDes);
        List<AvanceBean> ListAvance = this.avanceService.getListAvance(avance);
        model.addAttribute("recepAvanceList", ListAvance);
        model.addAttribute("tipoAvance", "RECEP");
        model.addAttribute("pnuAnn", pnuAnn);
        model.addAttribute("pnuEmi", pnuEmi);
        model.addAttribute("pnuDes", pnuDes);
        return "/modalGeneral/registroAvance";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goVerAvanceConsulta"})
    public String goVerAvanceConsulta(HttpServletRequest request, Model model) {
        String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
        String pnuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pnuEmi");
        String pnuDes = ServletUtility.getInstancia().loadRequestParameter(request, "pnuDes");
        AvanceBean avance = new AvanceBean();
        avance.setNuAnn(pnuAnn);
        avance.setNuEmi(pnuEmi);
        avance.setNuDes(pnuDes);
        avance.setTiAvance("RECEP");
        List<AvanceBean> ListAvance = this.avanceService.getListAvance(avance);
        model.addAttribute("recepAvanceList", ListAvance);
        model.addAttribute("pnuAnn", pnuAnn);
        model.addAttribute("pnuEmi", pnuEmi);
        model.addAttribute("pnuDes", pnuDes);
        return "/modalGeneral/consultaAvance";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goGrabarAvance"}, produces = {"text/plain; charset=utf-8"})
    @ResponseBody
    public String goGrabarAvance(HttpServletRequest request, Model model) {
        String coRespuesta, mensaje = "NO_OK";
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
        String pnuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pnuEmi");
        String pnuDes = ServletUtility.getInstancia().loadRequestParameter(request, "pnuDes");
        String tipoAvance = ServletUtility.getInstancia().loadRequestParameter(request, "tipAvance");
        String desAvance = ServletUtility.getInstancia().loadRequestParameter(request, "desAvance");
        AvanceBean avance = new AvanceBean();
        avance.setTiAvance(tipoAvance);
        avance.setNuAnn(pnuAnn);
        avance.setNuEmi(pnuEmi);
        avance.setNuDes(pnuDes);
        avance.setCoUser(usuario.getCoUsuario());
        avance.setDeAvance(desAvance);

        try {
            mensaje = this.avanceService.insertAvance(avance);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mensaje.equals("OK")) {
            coRespuesta = "1";
        } else {
            coRespuesta = "0";
        }
        String deRespuesta = mensaje;

        StringBuilder retval = new StringBuilder();
        retval.append("{\"coRespuesta\":\"");
        retval.append(coRespuesta);
        retval.append("\",\"deRespuesta\":\"");
        retval.append(deRespuesta);
        retval.append("\"");
        retval.append("}");

        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDocAnexo"})
    public String goDocAnexo(HttpServletRequest request, Model model) {
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        model.addAttribute("pkEmiDoc", pkEmi);
        return "/modalGeneral/consultaAnexos";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDocEnviarNotificacion"})
    public String goDocEnviarNotificacion(HttpServletRequest request, Model model) {
        String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
        String pnuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pnuEmi");
        String ptiEnvMsj = ServletUtility.getInstancia().loadRequestParameter(request, "ptiEnvMsj");
        String pexisteDoc = ServletUtility.getInstancia().loadRequestParameter(request, "pexisteDoc");
        String pexisteAnexo = ServletUtility.getInstancia().loadRequestParameter(request, "pexisteAnexo");
        String tipoBandeja = ServletUtility.getInstancia().loadRequestParameter(request, "tipoBandeja");

        model.addAttribute("pnuAnn", pnuAnn);
        model.addAttribute("pnuEmi", pnuEmi);
        model.addAttribute("ptiEnv", ptiEnvMsj);

        model.addAttribute("pexisteDoc", pexisteDoc);
        model.addAttribute("pexisteAnexo", pexisteAnexo);
        model.addAttribute("tipoBandeja", tipoBandeja);

        model.addAttribute("MensajeriaList", this.commonQryService.getLsDepenciaMensjeria());

        DatosInterBean datosInter = new DatosInterBean();

        String vDeDepDes = "";
        String vDeNomDes = "";
        String vDeCarDes = "";

        String vResult = "N";

        datosInter = this.commonQryService.DatosInter(pnuAnn, pnuEmi);

        if (datosInter != null) {
            if (datosInter.getNuRucDes().length() > 0 && !datosInter.getNuRucDes().equals("0")) {
                WsEntidadInter wSEntidad = new WsEntidadInter();
                try {

                    vResult = wSEntidad.getInter(datosInter.getNuRucDes(), this.datosPlantillaDao.getParametros("URL_ENTIDADES_INTER"));
                    //vResult = "S";
                    vDeDepDes = datosInter.getDeDepDes();
                    vDeNomDes = datosInter.getDeNomDes();
                    vDeCarDes = datosInter.getDeCarDes();
                } catch (Exception e) {
                    System.out.println("ingresa ExceptionExceptionException this.datosPlantillaDao.getParametros");
                    vResult = e.getMessage();
                }
            }
        }

        model.addAttribute("psiInter", vResult);
        model.addAttribute("pDeDepDes", vDeDepDes);
        model.addAttribute("pDeNomDes", vDeNomDes);
        model.addAttribute("pDeCarDes", vDeCarDes);

        return "/modalGeneral/consultaEnvioNotificacion";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDocEnviarNotificacionPopUp"})
    public String goDocEnviarNotificacionPopUp(HttpServletRequest request, Model model) {
        DocumentoVerBean docSession = Utilidades.getInstancia().loadDocFromSession(request);

        model.addAttribute("pnuAnn", docSession.getNuAnn());
        model.addAttribute("pnuEmi", docSession.getNumeroDoc());
        model.addAttribute("MensajeriaList", this.commonQryService.getLsDepenciaMensjeria());

        return "/modalGeneral/consultaEnvioNotificacion";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goArDocMsjEnviados"})
    public String goArDocMsjEnviados(HttpServletRequest request, Model model) {
        String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
        String pnuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pnuEmi");

        DocumentoEmiBean documentoEmiArBean = new DocumentoEmiBean();

        model.addAttribute("pnuAnn", pnuAnn);
        model.addAttribute("pnuEmi", pnuEmi);
        model.addAttribute("fechaActual", Utility.getInstancia().dateToFormatString(new Date()));
        model.addAttribute(documentoEmiArBean);

        return "/modalGeneral/archivarDocMsj";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goUpdArchivarDoc"})
    public String goUpdDescargarMensaje(@RequestBody DocumentoEmiBean documentoEmiBean, HttpServletRequest request, Model model) {
        String mensaje = "NO_OK";

        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        String coUsu = usuario.getCoUsuario();

        try {
            String respuesta = this.EmiDocumentoAdmService.updArchivarDocumento(documentoEmiBean, usuario);

            mensaje = respuesta;
        } catch (Exception ex) {
            mensaje = ex.getMessage();
        }
        if (mensaje.equals("OK")) {
            model.addAttribute("pMensaje", "Datos guardados.");
            return "respuesta";
        }
        model.addAttribute("pMensaje", mensaje);
        return "respuesta";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goCargarDocumentosAnexos"})
    public String goCargarDocumentosAnexos(HttpServletRequest request, Model model) {
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        model.addAttribute("pkEmiDoc", pkEmi);

        return "/modalGeneral/consultaCargarAnexos";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDetalleDocAnexo"})
    public String goDetalleDocAnexo(HttpServletRequest request, Model model) {
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        String mensaje = "NO_OK";

        String pNuAnn = null;
        String pNuEmi = null;
        String pNuDes = null;

        if (pkEmi != null) {

            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
            pNuDes = pkEmi.substring(14);
            if (pNuDes != null && pNuDes.equals("null")) {
                pNuDes = "N";
            }
            try {
                RemitosResBean docRemitoBean = this.documentoBasicoService.getRemitoResumen(pNuAnn, pNuEmi);
                DestinoResBen docDestinoBean = this.documentoBasicoService.getDestinoResumen(pNuAnn, pNuEmi, pNuDes);
                List<DocumentoAnexoBean> docAnexoList = this.documentoBasicoService.getAnexosList(pNuAnn, pNuEmi);

                docRemitoBean.setFeEmi(docRemitoBean.getFeEmi().substring(0, 19));

                model.addAttribute("docRemitoBean", docRemitoBean);
                model.addAttribute("docDestinoBean", docDestinoBean);
                model.addAttribute("docAnexoList", docAnexoList);

                mensaje = "OK";
            } catch (Exception e) {
                e.printStackTrace();
                mensaje = e.getMessage();
            }
        } else {
            mensaje = "Faltan Datos";
        }
        if (mensaje.equals("OK")) {
            return "/modalGeneral/consultaAnexosDetalle";
        }
        model.addAttribute("pMensaje", mensaje);
        return "respuesta";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDetalleCargarAnexo"})
    public String goDetalleCargarAnexo(HttpServletRequest request, Model model) {
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        String mensaje = "NO_OK";

        String pNuAnn = null;
        String pNuEmi = null;
        String pNuDes = null;

        if (pkEmi != null) {

            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
            pNuDes = pkEmi.substring(14);
            try {
                RemitosResBean docRemitoBean = this.documentoBasicoService.getRemitoResumen(pNuAnn, pNuEmi);
                DestinoResBen docDestinoBean = this.documentoBasicoService.getDestinoResumen(pNuAnn, pNuEmi, pNuDes);
                List<DocumentoAnexoBean> docAnexoList = this.documentoBasicoService.getAnexosList(pNuAnn, pNuEmi);

                model.addAttribute("docRemitoBean", docRemitoBean);
                model.addAttribute("docDestinoBean", docDestinoBean);
                model.addAttribute("docAnexoList", docAnexoList);
                mensaje = "OK";
            } catch (Exception e) {
                e.printStackTrace();
                mensaje = e.getMessage();
            }
        } else {
            mensaje = "Faltan Datos";
        }
        if (mensaje.equals("OK")) {
            return "/modalGeneral/consultaCargarAnexosDetalle";
        }
        model.addAttribute("pMensaje", mensaje);
        return "respuesta";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goListadoArchivosAnexos"})
    public String goListadoArchivosAnexos(HttpServletRequest request, Model model) {
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        String mensaje = "NO_OK";

        String pNuAnn = null;
        String pNuEmi = null;
        String pNuDes = null;

        if (pkEmi != null) {

            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
            pNuDes = pkEmi.substring(14);
            try {
                List<DocumentoAnexoBean> docAnexoList = this.documentoBasicoService.getAnexosList(pNuAnn, pNuEmi);
                model.addAttribute("docAnexoList", docAnexoList);
                mensaje = "OK";
            } catch (Exception e) {
                e.printStackTrace();
                mensaje = e.getMessage();
            }
        } else {
            mensaje = "Faltan Datos";
        }
        if (mensaje.equals("OK")) {
            return "/modalGeneral/consultaCargarAnexosListado";
        }
        model.addAttribute("pMensaje", mensaje);
        return "respuesta";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goVerificaCargaDoc"}, produces = {"text/plain; charset=utf-8"})
    @ResponseBody
    public String goVerificaCargaDoc(HttpServletRequest request, Model model) {
        String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
        String pnuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pnuEmi");
        StringBuilder retval = new StringBuilder();
        String bCargaDoc = "0";
        try {
            bCargaDoc = this.documentoBasicoService.verificaCargaDoc(pnuAnn, pnuEmi);

        } catch (Exception e) {
            e.printStackTrace();
        }

        retval.append("{\"bCargaDoc\":\"");
        retval.append(bCargaDoc);
        retval.append("\"");
        retval.append("}");

        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goListadoArchivosAnexosModoLectura"})
    public String goListadoArchivosAnexosModoLectura(HttpServletRequest request, Model model) {
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        String mensaje = "NO_OK";

        String pNuAnn = null;
        String pNuEmi = null;
        String pNuDes = null;

        if (pkEmi != null) {

            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
            pNuDes = pkEmi.substring(14);
            try {
                List<DocumentoAnexoBean> docAnexoList = this.documentoBasicoService.getAnexosList(pNuAnn, pNuEmi);
                model.addAttribute("docAnexoList", docAnexoList);
                mensaje = "OK";
            } catch (Exception e) {
                e.printStackTrace();
                mensaje = e.getMessage();
            }
        } else {
            mensaje = "Faltan Datos";
        }
        if (mensaje.equals("OK")) {
            return "/modalGeneral/consultaCargarAnexosListadoModoLectura";
        }
        model.addAttribute("pMensaje", mensaje);
        return "respuesta";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goListadoMsjAnexos"})
    public String goListadoMsjAnexos(HttpServletRequest request, Model model) {
        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String mensaje = "NO_OK";

        if (pNuAnn != null) {
            try {

                List<DocumentoAnexoBean> docAnexoList = this.documentoBasicoService.getAnexosMsjList(pNuAnn, pNuEmi);
                model.addAttribute("docAnexoMsjList", docAnexoList);
                mensaje = "OK";
            } catch (Exception e) {
                e.printStackTrace();
                mensaje = e.getMessage();
            }
        } else {
            mensaje = "Faltan Datos";
        }

        if (mensaje.equals("OK")) {
            return "/modalGeneral/archivarDocMsjListado";
        }
        model.addAttribute("pMensaje", mensaje);
        return "respuesta";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDetalleSeguimiento"})
    public String goDetalleSeguimiento(HttpServletRequest request, Model model) {
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        String pkExp = ServletUtility.getInstancia().loadRequestParameter(request, "pkExp");
        String mensaje = "NO_OK";

        String pNuAnn = null;
        String pNuEmi = null;
        String pNuDes = null;

        if (pkEmi != null) {

            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
            pNuDes = pkEmi.substring(14);
            try {
                RemitosResBean docRemitoBean = this.documentoBasicoService.getRemitoResumen(pNuAnn, pNuEmi);
                DestinoResBen docDestinoBean = this.documentoBasicoService.getDestinoResumen(pNuAnn, pNuEmi, pNuDes);

                List<DocumentoRecepMensajeriaBean> docMensajeriaList = this.documentoMensajeriaService.getLstDetalleMensajeria(docDestinoBean);
                DocumentoRecepMensajeriaBean docMensajeriaBean = new DocumentoRecepMensajeriaBean();
                if (!docMensajeriaList.isEmpty()) {
                    docMensajeriaBean = docMensajeriaList.get(0);
                    docMensajeriaBean.setFecEnviomsj(docMensajeriaBean.getFecEnviomsj().substring(0, 19));
                    docMensajeriaBean.setFePlaMsj(docMensajeriaBean.getFePlaMsj().substring(0, 19));
                } else {

                    docMensajeriaBean.setNumsj("0");
                }
                if (docRemitoBean.getFeEmi() != null && docRemitoBean.getFeEmi().length() > 19) {
                    docRemitoBean.setFeEmi(docRemitoBean.getFeEmi().substring(0, 19));
                }
                if (docDestinoBean.getFeRecDoc() != null && docDestinoBean.getFeRecDoc().length() > 19) {
                    docDestinoBean.setFeRecDoc(docDestinoBean.getFeRecDoc().substring(0, 19));
                }
                if (docDestinoBean.getFeArcDoc() != null && docDestinoBean.getFeArcDoc().length() > 19) {
                    docDestinoBean.setFeArcDoc(docDestinoBean.getFeArcDoc().substring(0, 19));
                }
                List<DocumentoAnexoBean> docAnexoList = this.documentoBasicoService.getAnexosList(pNuAnn, pNuEmi);
                model.addAttribute("pkExp", pkExp);
                model.addAttribute("docRemitoBean", docRemitoBean);
                model.addAttribute("docDestinoBean", docDestinoBean);
                model.addAttribute("docAnexoList", docAnexoList);
                model.addAttribute("docMensajeriaBean", docMensajeriaBean);
                mensaje = "OK";
            } catch (Exception e) {
                e.printStackTrace();
                mensaje = e.getMessage();
            }
        } else {
            mensaje = "Faltan Datos";
        }
        if (mensaje.equals("OK")) {
            return "/modalGeneral/consultaSeguimientoDetalle";
        }
        model.addAttribute("pMensaje", mensaje);
        return "respuesta";
    }

    @RequestMapping(method = {RequestMethod.GET}, params = {"accion=goDocAnexoJson"})
    @ResponseBody
    private String goDocAnexoJson(HttpServletRequest request, Model model) throws Exception {
        String retval = " ";
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        String pNuAnn = null;
        String pNuEmi = null;
        String pNuDes = null;

        if (pkEmi != null) {

            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
            pNuDes = pkEmi.substring(14);
            try {
                retval = this.anexoDocumentoService.getReferenciaJson(pNuAnn, pNuEmi, 3);
            } catch (Exception e) {
                retval = " ";
                e.printStackTrace();
            }
        }

        return retval;
    }

    @RequestMapping(method = {RequestMethod.GET}, params = {"accion=goDocAnexoRoot"})
    @ResponseBody
    private String goDocAnexoRoot(HttpServletRequest request, Model model) throws Exception {
        String retval = " ";
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        String pNuAnn = null;
        String pNuEmi = null;
        String pNuDes = null;

        if (pkEmi != null) {

            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
            pNuDes = pkEmi.substring(14);
            try {
                retval = this.anexoDocumentoService.getReferenciaRoot(pNuAnn, pNuEmi, pNuDes, 3);
            } catch (Exception e) {
                retval = " ";
                e.printStackTrace();
            }
        }

        return retval;
    }

    @RequestMapping(method = {RequestMethod.GET}, params = {"accion=goDocSeguimientoRoot"})
    @ResponseBody
    private String goDocSeguimientoRoot(HttpServletRequest request, Model model) throws Exception {
        String retval = " ";
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        String pNuAnn = null;
        String pNuEmi = null;
        String pNuDes = null;

        if (pkEmi != null) {
            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
            pNuDes = pkEmi.substring(14);
            try {
                retval = this.anexoDocumentoService.getSeguimientoRoot(pNuAnn, pNuEmi, pNuDes, 3);
            } catch (Exception e) {
                retval = " ";
                e.printStackTrace();
            }
        }
        return retval;
    }

    @RequestMapping(method = {RequestMethod.GET}, params = {"accion=goDocSeguimientoJson"})
    @ResponseBody
    private String goDocSeguimientoJson(HttpServletRequest request, Model model) throws Exception {
        String retval = " ";
        String pkEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pkEmi");
        String pNuAnn = null;
        String pNuEmi = null;
        String pNuDes = null;
        if (pkEmi != null) {
            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
            pNuDes = pkEmi.substring(14);
            try {
                retval = this.anexoDocumentoService.getSeguimientoJson(pNuAnn, pNuEmi, pNuDes, 3);
            } catch (Exception e) {
                retval = " ";
                e.printStackTrace();
            }
        }
        return retval;
    }

    LinkedList<DocumentoFileBean> files = new LinkedList<DocumentoFileBean>();
    DocumentoFileBean fileMeta = null;

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goUpload"})
    @ResponseBody
    public String goUpload(MultipartHttpServletRequest request, HttpServletResponse response) {
        Iterator<String> itr = request.getFileNames();
        MultipartFile mpf = null;
        String pNuAnn = null;
        String pNuEmi = null;
        String pkEmi = null;
        pkEmi = ServletUtility.getInstancia().loadRequestParameter((HttpServletRequest) request, "pkEmi");
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession((HttpServletRequest) request);
        String coUsu = usuario.getCoUsuario();

        String vreturn = "NO_OK";
        if (pkEmi != null) {
            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
        }

        while (itr.hasNext()) {
            mpf = request.getFile(itr.next());

            this.fileMeta = new DocumentoFileBean();
            this.fileMeta.setNombreArchivo(mpf.getOriginalFilename());
            this.fileMeta.setTamanoArchivo((mpf.getSize() / 1024L) + " Kb");
            this.fileMeta.setTipoArchivo(mpf.getContentType());
            try {
                this.fileMeta.setArchivoBytes(mpf.getBytes());
                vreturn = this.anexoDocumentoService.insArchivoAnexo(coUsu, pNuAnn, pNuEmi, this.fileMeta);
            } catch (IOException e) {

                e.printStackTrace();
            }

            this.files.add(this.fileMeta);
        }

        String id = this.fileMeta.getIdDocumento();
        String nombreArchivo = this.fileMeta.getNombreArchivo();
        String tamanoArchivo = this.fileMeta.getTamanoArchivo();
        String tipoArchivo = this.fileMeta.getTipoArchivo();

        String res = "[{\"id\":\"%s\",\"name\":\"%s\",\"fileSize\":\"%s\",\"fileType\":\"%s\"}]";
        res = String.format(res, new Object[]{id, nombreArchivo, tamanoArchivo, tipoArchivo});
        return res;
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goUploadIE"}, produces = {"text/plain; charset=utf-8"})
    @ResponseBody
    public String goUploadIE(MultipartHttpServletRequest request, HttpServletResponse response) {
        Iterator<String> itr = request.getFileNames();
        MultipartFile mpf = null;

        String pNuAnn = null;
        String pNuEmi = null;
        String pkEmi = null;

        pkEmi = ServletUtility.getInstancia().loadRequestParameter((HttpServletRequest) request, "pkEmi");
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession((HttpServletRequest) request);
        String coUsu = usuario.getCoUsuario();
        String vreturn = "NO_OK";
        if (pkEmi != null) {
            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
        }

        while (itr.hasNext()) {
            mpf = request.getFile(itr.next());

            this.fileMeta = new DocumentoFileBean();
            this.fileMeta.setNombreArchivo(mpf.getOriginalFilename());
            this.fileMeta.setTamanoArchivo((mpf.getSize() / 1024L) + " Kb");
            this.fileMeta.setTipoArchivo(mpf.getContentType());
            try {
                this.fileMeta.setArchivoBytes(mpf.getBytes());
                vreturn = this.anexoDocumentoService.insArchivoAnexo(coUsu, pNuAnn, pNuEmi, this.fileMeta);
            } catch (IOException e) {

                e.printStackTrace();
            }

            this.files.add(this.fileMeta);
        }

        String id = this.fileMeta.getIdDocumento();
        String nombreArchivo = this.fileMeta.getNombreArchivo();
        String tamanoArchivo = this.fileMeta.getTamanoArchivo();
        String tipoArchivo = this.fileMeta.getTipoArchivo();

        String res = "[{\"id\":\"%s\",\"name\":\"%s\",\"fileSize\":\"%s\",\"fileType\":\"%s\"}]";
        res = String.format(res, new Object[]{id, nombreArchivo, tamanoArchivo, tipoArchivo});
        return res;
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goUploadReplace"})
    @ResponseBody
    public String goUploadReplace(MultipartHttpServletRequest request, HttpServletResponse response) {
        Iterator<String> itr = request.getFileNames();
        MultipartFile mpf = null;
        String pNuAnn = null;
        String pNuEmi = null;
        String pNuAne = null;
        String pkEmi = null;

        pkEmi = ServletUtility.getInstancia().loadRequestParameter((HttpServletRequest) request, "pkEmi");
        pNuAne = ServletUtility.getInstancia().loadRequestParameter((HttpServletRequest) request, "pNuAne");
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession((HttpServletRequest) request);
        String coUsu = usuario.getCoUsuario();

        String vreturn = "NO_OK";
        if (pkEmi != null) {
            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
        }

        while (itr.hasNext()) {

            mpf = request.getFile(itr.next());
            this.fileMeta = new DocumentoFileBean();
            this.fileMeta.setNombreArchivo(mpf.getOriginalFilename());
            this.fileMeta.setTamanoArchivo((mpf.getSize() / 1024L) + " Kb");
            this.fileMeta.setTipoArchivo(mpf.getContentType());

            try {
                this.fileMeta.setArchivoBytes(mpf.getBytes());
                vreturn = this.anexoDocumentoService.updArchivoAnexo(coUsu, pNuAnn, pNuEmi, pNuAne, this.fileMeta);
            } catch (IOException e) {

                e.printStackTrace();
            }

            this.files.add(this.fileMeta);
        }

        String id = this.fileMeta.getIdDocumento();

        String nombreArchivo = this.anexoDocumentoService.getNombreArchivo(pNuAnn, pNuEmi, pNuAne);
        String tamanoArchivo = this.fileMeta.getTamanoArchivo();
        String tipoArchivo = this.fileMeta.getTipoArchivo();

        String res = "[{\"id\":\"%s\",\"name\":\"%s\",\"fileSize\":\"%s\",\"fileType\":\"%s\"}]";
        res = String.format(res, new Object[]{id, nombreArchivo, tamanoArchivo, tipoArchivo});

        return res;
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goUploadReplaceIE"}, produces = {"text/plain; charset=utf-8"})
    @ResponseBody
    public String goUploadReplaceIE(MultipartHttpServletRequest request, HttpServletResponse response) {
        Iterator<String> itr = request.getFileNames();
        MultipartFile mpf = null;

        String pNuAnn = null;
        String pNuEmi = null;
        String pNuAne = null;
        String pkEmi = null;

        pkEmi = ServletUtility.getInstancia().loadRequestParameter((HttpServletRequest) request, "pkEmi");
        pNuAne = ServletUtility.getInstancia().loadRequestParameter((HttpServletRequest) request, "pNuAne");
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession((HttpServletRequest) request);
        String coUsu = usuario.getCoUsuario();

        String vreturn = "NO_OK";
        if (pkEmi != null) {
            pNuAnn = pkEmi.substring(0, 4);
            pNuEmi = pkEmi.substring(4, 14);
        }

        while (itr.hasNext()) {

            mpf = request.getFile(itr.next());

            this.fileMeta = new DocumentoFileBean();
            this.fileMeta.setNombreArchivo(mpf.getOriginalFilename());
            this.fileMeta.setTamanoArchivo((mpf.getSize() / 1024L) + " Kb");
            this.fileMeta.setTipoArchivo(mpf.getContentType());

            try {
                this.fileMeta.setArchivoBytes(mpf.getBytes());
                vreturn = this.anexoDocumentoService.updArchivoAnexo(coUsu, pNuAnn, pNuEmi, pNuAne, this.fileMeta);
            } catch (IOException e) {

                e.printStackTrace();
            }

            this.files.add(this.fileMeta);
        }

        String id = this.fileMeta.getIdDocumento();
        String nombreArchivo = this.fileMeta.getNombreArchivo();
        String tamanoArchivo = this.fileMeta.getTamanoArchivo();
        String tipoArchivo = this.fileMeta.getTipoArchivo();

        String res = "[{\"id\":\"%s\",\"name\":\"%s\",\"fileSize\":\"%s\",\"fileType\":\"%s\"}]";
        res = String.format(res, new Object[]{id, nombreArchivo, tamanoArchivo, tipoArchivo});

        return res;
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goActualizaDescripAnex"})
    public String goActualizaDescripAnex(@RequestBody DocumentoAnexoBeansContenedor listaDocs, HttpServletRequest request, Model model) {
        String mensaje = "NO_OK";
        String rowCount = null;
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        String coUsu = usuario.getCoUsuario();
        rowCount = ServletUtility.getInstancia().loadRequestParameter(request, "rowCount");

        try {
            mensaje = this.anexoDocumentoService.updAnexoDetalle(listaDocs, coUsu, rowCount);
        } catch (validarDatoException e) {
            mensaje = e.valorMsg;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mensaje.equals("OK")) {
            model.addAttribute("pMensaje", mensaje);
            return "respuesta";
        }
        model.addAttribute("pMensaje", mensaje);
        return "respuesta";
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goRutaCargaDoc"})
    @ResponseBody
    private String goRutaCargaDoc(HttpServletRequest request, Model model) throws Exception {
        StringBuilder retval = new StringBuilder();
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pTiOpe = ServletUtility.getInstancia().loadRequestParameter(request, "tiOpe");
        String pinFor = ServletUtility.getInstancia().loadRequestParameter(request, "inFor");

        if (pNuAnn != null && pNuEmi != null && pTiOpe != null) {

            try {
                DocumentoVerBean docVerBean = new DocumentoVerBean();
                docVerBean = this.documentoObjService.getNombreCargaDoc(pNuAnn, pNuEmi, pTiOpe);

                retval.append("{\"retval\":\"");
                retval.append(docVerBean.getDeMensaje());
                retval.append("\",\"nuAnn\":\"");
                retval.append(docVerBean.getNuAnn());
                retval.append("\",\"nuEmi\":\"");
                retval.append(docVerBean.getNuEmi());
                retval.append("\",\"nuSecFirma\":\"");
                retval.append(docVerBean.getNuSecFirma());
                retval.append("\",\"coDoc\":\"");
                retval.append(docVerBean.getCoDocEmi());
                retval.append("\",\"noDoc\":\"");
                retval.append(docVerBean.getNoDocumento());
                retval.append("\",\"noFirma\":\"");
                retval.append(docVerBean.getNoFirma());
                retval.append("\",\"noUrl\":\"");
                retval.append(docVerBean.getUrlDocumento());
                if (pinFor != null && pinFor.equals("1")) {
                    retval.append("\",\"inFor\":\"");
                    retval.append(this.documentoObjService.getFormatoDoc(pNuAnn, pNuEmi, pTiOpe));
                }
                retval.append("\"}");
            } catch (Exception e) {
                retval.append("{\"retval\":");
                retval.append("false");
                retval.append("}");
                e.printStackTrace();
            }
        }

        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goRutaFirmaDoc"})
    @ResponseBody
    private String goRutaFirmaDoc(HttpServletRequest request, Model model) throws Exception {
        StringBuilder retval = new StringBuilder();

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pTiOpe = ServletUtility.getInstancia().loadRequestParameter(request, "tiOpe");
        UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
        DocumentoVerBean docSession = new DocumentoVerBean();

        if (pNuAnn != null && pNuEmi != null && pTiOpe != null) {
            try {
                ServletContext sc = request.getSession().getServletContext();
                String rutaBase = sc.getRealPath("/reports/");

                docSession = this.documentoObjService.getNombreFirmaDoc(pNuAnn, pNuEmi, pTiOpe, usuarioConfigBean, rutaBase);

                retval.append("{\"retval\":\"");
                retval.append(docSession.getDeMensaje());
                retval.append("\",\"nuAnn\":\"");
                retval.append(docSession.getNuAnn());
                retval.append("\",\"nuEmi\":\"");
                retval.append(docSession.getNuEmi());
                retval.append("\",\"inFirma\":\"");
                retval.append(docSession.getInTipoFirma());
                retval.append("\",\"nuSecFirma\":\"");
                retval.append(docSession.getNuSecFirma());
                retval.append("\",\"noPrefijo\":\"");
                retval.append(docSession.getNoPrefijo());
                retval.append("\",\"numeroDoc\":\"");
                retval.append(docSession.getNumeroDoc());
                retval.append("\",\"fechaFirma\":\"");
                retval.append(docSession.getFeFirma());
                retval.append("\",\"noDoc\":\"");
                retval.append(docSession.getNoDocumento());
                // Se agrega el cargo
                retval.append("\",\"desCargo\":\"");
                retval.append(docSession.getCargoEmpleado());

                retval.append("\",\"noUrl\":\"");
                retval.append(docSession.getUrlDocumento());
                retval.append("\"}");
            } catch (Exception e) {
                retval.append("{\"retval\":");
                retval.append("false");
                retval.append("}");
                e.printStackTrace();
            }
        }

        model.addAttribute("docSession", docSession);
        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goRutaCargaFirmaDoc"})
    @ResponseBody
    private String goRutaCargaFirmaDoc(HttpServletRequest request, Model model) throws Exception {
        StringBuilder retval = new StringBuilder();

        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        DocumentoVerBean docSession = Utilidades.getInstancia().loadDocFromSession(request);

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pTiOpe = ServletUtility.getInstancia().loadRequestParameter(request, "tiOpe");
        String pNuSecFirma = ServletUtility.getInstancia().loadRequestParameter(request, "nuSecFirma");
        String tipoDocumento = ServletUtility.getInstancia().loadRequestParameter(request, "tipoDocumento");
        if (pNuAnn != null && pNuEmi != null && pTiOpe != null && pNuSecFirma != null) {
            if (pNuSecFirma.equals(docSession.getNuSecFirma()) || tipoDocumento.equals("232")) {
                try {
                    DocumentoVerBean docVerBean = new DocumentoVerBean();
                    docVerBean = this.documentoObjService.getNombreCargaFirmaDoc(pNuAnn, pNuEmi, pTiOpe);
                    if (docVerBean.getDeMensaje().equals("OK")) {
                        docSession.setDeMensaje(docVerBean.getDeMensaje());
                        docSession.setUrlDocumento(docVerBean.getUrlDocumento());
                        docSession.setNuSecFirma(docVerBean.getNuSecFirma());

                        retval.append("{\"retval\":\"");
                        retval.append(docSession.getDeMensaje());
                        retval.append("\",\"nuAnn\":\"");
                        retval.append(docSession.getNuAnn());
                        retval.append("\",\"nuEmi\":\"");
                        retval.append(docSession.getNuEmi());
                        retval.append("\",\"nuSecFirma\":\"");
                        retval.append(docSession.getNuSecFirma());
                        retval.append("\",\"noFirma\":\"");
                        retval.append(docSession.getNoFirma());
                        retval.append("\",\"noUrl\":\"");
                        retval.append(docSession.getUrlDocumento());
                        retval.append("\"}");
                    }

                } catch (Exception e) {
                    retval.append("{\"retval\":");
                    retval.append("false");
                    retval.append("}");
                    e.printStackTrace();
                }
            } else {
                retval.append("{\"retval\":");
                retval.append("\"Documento ha cambiado, proceda a firmar nuevamente\"");
                retval.append("}");
            }
        } else {
            retval.append("{\"retval\":");
            retval.append("\"Error en Datos\"");
            retval.append("}");
        }

        model.addAttribute("docSession", docSession);
        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDocRutaAbrirEmi"})
    @ResponseBody
    private String goDocRutaAbrirEmi(HttpServletRequest request, Model model) throws Exception {
        String retval = "";
        DocRespuestaBean retRespuesta = new DocRespuestaBean();
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pTiOpe = ServletUtility.getInstancia().loadRequestParameter(request, "tiOpe");

        if (pNuAnn != null && pNuEmi != null && pTiOpe != null) {
            try {
                ServletContext sc = request.getSession().getServletContext();
                String rutaBase = sc.getRealPath("/reports/");

                DocumentoVerBean docVerBean = new DocumentoVerBean();
                docVerBean = this.documentoObjService.getNombreDocEmi(pNuAnn, pNuEmi, pTiOpe, usuario, rutaBase);

                retRespuesta.setRetval(docVerBean.getDeMensaje());
                retRespuesta.setNuAnn(docVerBean.getNuAnn());
                retRespuesta.setNuEmi(docVerBean.getNuEmi());
                retRespuesta.setNoDoc(docVerBean.getNoDocumento());
                retRespuesta.setNoFirma(docVerBean.getNoFirma());
                retRespuesta.setNoUrl(docVerBean.getUrlDocumento());
            } catch (Exception e) {
                retRespuesta.setRetval("Error al obtener nombre del Documento");
                e.printStackTrace();
            }
            try {
                Writer strWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(strWriter, retRespuesta);
                retval = strWriter.toString();
            } catch (Exception e) {
                retval = "";
            }
        }
        return retval;
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goRutaVoBoDoc"})
    @ResponseBody
    private String goRutaVoBoDoc(HttpServletRequest request, Model model) throws Exception {
        StringBuilder retval = new StringBuilder();

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pTiOpe = ServletUtility.getInstancia().loadRequestParameter(request, "tiOpe");
        UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
        DocumentoVerBean docSession = new DocumentoVerBean();

        if (pNuAnn != null && pNuEmi != null && pTiOpe != null) {

            try {
                ServletContext sc = request.getSession().getServletContext();
                String rutaBase = sc.getRealPath("/reports/");

                docSession = this.documentoObjService.getNombreVoBoDoc(pNuAnn, pNuEmi, pTiOpe, usuarioConfigBean, rutaBase);

                retval.append("{\"retval\":\"");
                retval.append(docSession.getDeMensaje());
                retval.append("\",\"nuAnn\":\"");
                retval.append(docSession.getNuAnn());
                retval.append("\",\"nuEmi\":\"");
                retval.append(docSession.getNuEmi());
                retval.append("\",\"inFirma\":\"");
                retval.append(docSession.getInTipoFirma());
                retval.append("\",\"nuSecFirma\":\"");
                retval.append(docSession.getNuSecFirma());
                retval.append("\",\"noPrefijo\":\"");
                retval.append(docSession.getNoPrefijo());
                retval.append("\",\"numeroDoc\":\"");
                retval.append(docSession.getNumeroDoc());
                retval.append("\",\"fechaFirma\":\"");
                retval.append(docSession.getFeFirma());
                retval.append("\",\"noDoc\":\"");
                retval.append(docSession.getNoDocumento());
                retval.append("\",\"noUrl\":\"");
                retval.append(docSession.getUrlDocumento());
                retval.append("\"}");
            } catch (Exception e) {
                retval.append("{\"retval\":");
                retval.append("false");
                retval.append("}");
                e.printStackTrace();
            }
        }

        model.addAttribute("docSession", docSession);
        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goRutaCargaVoBoDoc"})
    @ResponseBody
    private String goRutaCargaVoBoDoc(HttpServletRequest request, Model model) throws Exception {
        StringBuilder retval = new StringBuilder();

        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        DocumentoVerBean docSession = Utilidades.getInstancia().loadDocFromSession(request);

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pTiOpe = ServletUtility.getInstancia().loadRequestParameter(request, "tiOpe");
        String pNuSecFirma = ServletUtility.getInstancia().loadRequestParameter(request, "nuSecFirma");

        if (pNuAnn != null && pNuEmi != null && pTiOpe != null && pNuSecFirma != null) {
            if (pNuSecFirma.equals(docSession.getNuSecFirma())) {
                try {
                    DocumentoVerBean docVerBean = new DocumentoVerBean();
                    docVerBean = this.documentoObjService.getNombreCargaVoBoDoc(pNuAnn, pNuEmi, pTiOpe);
                    if (docVerBean.getDeMensaje().equals("OK")) {
                        docSession.setDeMensaje(docVerBean.getDeMensaje());
                        docSession.setUrlDocumento(docVerBean.getUrlDocumento());
                        docSession.setNuSecFirma(docVerBean.getNuSecFirma());

                        retval.append("{\"retval\":\"");
                        retval.append(docSession.getDeMensaje());
                        retval.append("\",\"nuAnn\":\"");
                        retval.append(docSession.getNuAnn());
                        retval.append("\",\"nuEmi\":\"");
                        retval.append(docSession.getNuEmi());
                        retval.append("\",\"nuSecFirma\":\"");
                        retval.append(docSession.getNuSecFirma());
                        retval.append("\",\"noFirma\":\"");
                        retval.append(docSession.getNoFirma());
                        retval.append("\",\"noUrl\":\"");
                        retval.append(docSession.getUrlDocumento());
                        retval.append("\"}");
                    }

                } catch (Exception e) {
                    retval.append("{\"retval\":");
                    retval.append("false");
                    retval.append("}");
                    e.printStackTrace();
                }
            } else {
                retval.append("{\"retval\":");
                retval.append("\"Documento ha cambiado, proceda a firmar nuevamente\"");
                retval.append("}");
            }
        } else {
            retval.append("{\"retval\":");
            retval.append("\"Error en Datos\"");
            retval.append("}");
        }

        model.addAttribute("docSession", docSession);
        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goRutaFirmaDocAnexo"})
    @ResponseBody
    private String goRutaFirmaDocAnexo(HttpServletRequest request, Model model) throws Exception {
        StringBuilder retval = new StringBuilder();

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pNuAne = ServletUtility.getInstancia().loadRequestParameter(request, "nuAne");
        String pTiOpe = ServletUtility.getInstancia().loadRequestParameter(request, "tiOpe");
        UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
        DocumentoVerBean docSession = new DocumentoVerBean();

        if (pNuAnn != null && pNuEmi != null && pNuAne != null) {

            try {
                docSession = this.documentoObjService.getNombreFirmaDocAnexo(pNuAnn, pNuEmi, pNuAne, pTiOpe, usuarioConfigBean);

                retval.append("{\"retval\":\"");
                retval.append(docSession.getDeMensaje());
                retval.append("\",\"nuAnn\":\"");
                retval.append(docSession.getNuAnn());
                retval.append("\",\"nuEmi\":\"");
                retval.append(docSession.getNuEmi());
                retval.append("\",\"inFirma\":\"");
                retval.append(docSession.getInTipoFirma());
                retval.append("\",\"nuSecFirma\":\"");
                retval.append(docSession.getNuSecFirma());
                retval.append("\",\"noPrefijo\":\"");
                retval.append(docSession.getNoPrefijo());
                retval.append("\",\"numeroDoc\":\"");
                retval.append(docSession.getNumeroDoc());
                retval.append("\",\"fechaFirma\":\"");
                retval.append(docSession.getFeFirma());
                retval.append("\",\"noDoc\":\"");
                retval.append(docSession.getNoDocumento());
                retval.append("\",\"noUrl\":\"");
                retval.append(docSession.getUrlDocumento());
                retval.append("\"}");
            } catch (Exception e) {
                retval.append("{\"retval\":");
                retval.append("false");
                retval.append("}");
                e.printStackTrace();
            }
        }

        model.addAttribute("docSession", docSession);
        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goRutaCargaFirmaDocAnexo"})
    @ResponseBody
    private String goRutaCargaFirmaDocAnexo(HttpServletRequest request, Model model) throws Exception {
        StringBuilder retval = new StringBuilder();

        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        DocumentoVerBean docSession = Utilidades.getInstancia().loadDocFromSession(request);

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pNuAne = ServletUtility.getInstancia().loadRequestParameter(request, "nuAne");
        String pTiOpe = ServletUtility.getInstancia().loadRequestParameter(request, "tiOpe");
        String pNuSecFirma = ServletUtility.getInstancia().loadRequestParameter(request, "nuSecFirma");

        if (pNuAnn != null && pNuEmi != null && pTiOpe != null && pNuSecFirma != null && pNuAne != null) {
            if (pNuSecFirma.equals(docSession.getNuSecFirma())) {
                try {
                    DocumentoVerBean docVerBean = new DocumentoVerBean();
                    docVerBean = this.documentoObjService.getNombreCargaFirmaDocAnexo(pNuAnn, pNuEmi, pNuAne, pTiOpe);
                    if (docVerBean.getDeMensaje().equals("OK")) {
                        docSession.setDeMensaje(docVerBean.getDeMensaje());
                        docSession.setUrlDocumento(docVerBean.getUrlDocumento());
                        docSession.setNuSecFirma(docVerBean.getNuSecFirma());

                        retval.append("{\"retval\":\"");
                        retval.append(docSession.getDeMensaje());
                        retval.append("\",\"nuAnn\":\"");
                        retval.append(docSession.getNuAnn());
                        retval.append("\",\"nuEmi\":\"");
                        retval.append(docSession.getNuEmi());
                        retval.append("\",\"nuSecFirma\":\"");
                        retval.append(docSession.getNuSecFirma());
                        retval.append("\",\"noFirma\":\"");
                        retval.append(docSession.getNoFirma());
                        retval.append("\",\"noUrl\":\"");
                        retval.append(docSession.getUrlDocumento());
                        retval.append("\"}");
                    }

                } catch (Exception e) {
                    retval.append("{\"retval\":");
                    retval.append("false");
                    retval.append("}");
                    e.printStackTrace();
                }
            } else {
                retval.append("{\"retval\":");
                retval.append("\"Documento ha cambiado, proceda a firmar nuevamente\"");
                retval.append("}");
            }
        } else {
            retval.append("{\"retval\":");
            retval.append("\"Error en Datos\"");
            retval.append("}");
        }

        model.addAttribute("docSession", docSession);
        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goCargaDocAnexo"}, produces = {"text/plain; charset=utf-8"})
    @ResponseBody
    public String goCargaDocAnexo(HttpServletRequest request, Model model) {
        String mensaje;
        StringBuilder retval = new StringBuilder();

        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        DocumentoVerBean docSession = Utilidades.getInstancia().loadDocFromSession(request);

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pNuAne = ServletUtility.getInstancia().loadRequestParameter(request, "nuAne");
        String pNuSecFirma = ServletUtility.getInstancia().loadRequestParameter(request, "nuSecFirma");

        pNuSecFirma = this.applicationProperties.getRutaTemporal() + "//" + pNuSecFirma;
        try {
            mensaje = this.documentoObjService.cargaDocAnexo(pNuAnn, pNuEmi, pNuAne, pNuSecFirma, usuario.getCoUsuario(), docSession.getNoDocumento());
        } catch (validarDatoException e) {
            mensaje = e.valorMsg;
        } catch (Exception e) {
            mensaje = e.getMessage();
        }

        String vRespuesta = mensaje;
        if (mensaje.equals("OK")) {
            String coRespuesta = "1";
            retval.append("{\"coRespuesta\":\"");
            retval.append(coRespuesta);
            retval.append("\"}");
        } else {
            String coRespuesta = "0";
            retval.append("{\"coRespuesta\":\"");
            retval.append(coRespuesta);
            retval.append("\",\"deRespuesta\":\"");
            retval.append(vRespuesta);
            retval.append("\"}");
        }
        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDocRutaAbrirEmiReporte"})
    @ResponseBody
    public String goDocRutaAbrirEmiReporte(HttpServletRequest request, Model model) {
        String retval = "";
        DocRespuestaBean retRespuesta = new DocRespuestaBean();
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pTiOpe = ServletUtility.getInstancia().loadRequestParameter(request, "tiOpe");
        String pAbreWord = ServletUtility.getInstancia().loadRequestParameter(request, "pAbreWord");
        pAbreWord = pAbreWord.equals("") ? "NO" : pAbreWord;

        String rutaBase = "";
        if (pNuAnn != null && pNuEmi != null && pTiOpe != null) {
            try {
                ServletContext sc = request.getSession().getServletContext();
                rutaBase = sc.getRealPath("/reports/");
                DocumentoVerBean docVerBean = new DocumentoVerBean();

                docVerBean = this.documentoObjService.getNombreDocEmiReporte(pNuAnn, pNuEmi, pTiOpe, usuario, rutaBase);

                retRespuesta.setRetval(docVerBean.getDeMensaje());
                retRespuesta.setNuAnn(docVerBean.getNuAnn());
                retRespuesta.setNuEmi(docVerBean.getNuEmi());

                //HENRY
                if (pAbreWord.equals("SI")) {

                    String nombreOriginal = docVerBean.getNombreDocumentoWord();
                    String nuevoNombre;

                    if (nombreOriginal != null && nombreOriginal.toLowerCase().endsWith(".pdf")) {
                        nuevoNombre = nombreOriginal.substring(0, nombreOriginal.length() - 4) + ".docx";
                    } else {
                        nuevoNombre = nombreOriginal; // mantener el nombre original si no termina en .pdf
                    }
                    retRespuesta.setNoDoc(nuevoNombre);
                } else {
                    retRespuesta.setNoDoc(docVerBean.getNoDocumento());
                }

                retRespuesta.setNoFirma(docVerBean.getNoFirma());
                retRespuesta.setNoUrl(docVerBean.getUrlDocumento());
            } catch (Exception e) {
                retRespuesta.setRetval("Error al obtener nombre del Documento");
                e.printStackTrace();
            }
            try {
                Writer strWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(strWriter, retRespuesta);
                retval = strWriter.toString();
            } catch (Exception e) {
                retval = "";
            }
        }
        return retval;
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goRutaFirmaDocReporte"})
    @ResponseBody
    private String goRutaFirmaDocReporte(HttpServletRequest request, Model model) throws Exception {
        StringBuilder retval = new StringBuilder();

        String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
        String pTiOpe = ServletUtility.getInstancia().loadRequestParameter(request, "tiOpe");
        UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
        DocumentoVerBean docSession = new DocumentoVerBean();
        String rutaBase = "";
        if (pNuAnn != null && pNuEmi != null && pTiOpe != null) {
            try {
                ServletContext sc = request.getSession().getServletContext();
                rutaBase = sc.getRealPath("/reports/");
                System.out.println("rutaBase: "+rutaBase);
                System.out.println("pNuAnn: "+pNuAnn);
                System.out.println("pNuEmi: "+pNuEmi);
                System.out.println("pTiOpe: "+pTiOpe);
                System.out.println("usuarioConfigBean: "+usuarioConfigBean.getSwFirma());
                System.out.println("usuarioConfigBean: "+usuarioConfigBean.getCoUsuario());
                System.out.println("usuarioConfigBean: "+usuarioConfigBean.getCoDep());
                DocumentoVerBean docVerBean = new DocumentoVerBean();
                docSession = this.documentoObjService.getNombreFirmaDocReporte(pNuAnn, pNuEmi, pTiOpe, usuarioConfigBean, rutaBase);
                if ((docSession.getDeMensaje().equals("NO_OK"))) {
                    System.out.println("ingresa 2");
                    retval.append("{\"retval\":\"");
                    retval.append(docSession.getDetalleMensaje() != null ? docSession.getDetalleMensaje() : "");
                    retval.append("\"}");
                    return retval.toString();

                }

                retval.append("{\"retval\":\"");
                retval.append(docSession.getDeMensaje());
                retval.append("\",\"nuAnn\":\"");
                retval.append(docSession.getNuAnn());
                retval.append("\",\"nuEmi\":\"");
                retval.append(docSession.getNuEmi());
                retval.append("\",\"inFirma\":\"");
                retval.append(docSession.getInTipoFirma());
                retval.append("\",\"nuSecFirma\":\"");
                retval.append(docSession.getNuSecFirma());
                retval.append("\",\"noPrefijo\":\"");
                retval.append(docSession.getNoPrefijo());
                retval.append("\",\"numeroDoc\":\"");
                retval.append(docSession.getNumeroDoc());
                retval.append("\",\"fechaFirma\":\"");
                retval.append(docSession.getFeFirma());
                retval.append("\",\"noDoc\":\"");
                //MIDAGRI-SERFOR-CD SERFOR

                docSession.setNoDocumento(docSession.getNoDocumento() !=null? docSession.getNoDocumento().replace("MIDAGRI-SERFOR-CD SERFOR", "MIDAGRI-CD SERFOR") : docSession.getNoDocumento());
                //docSession.setNoDocumento(docSession.getNoDocumento().replace("MIDAGRI-SERFOR-CD SERFOR", "MIDAGRI-CD SERFOR"));
                //docSession.setNoDocumento("TEMP|2024000000103607986446.pdf");
                retval.append(docSession.getNoDocumento());
                retval.append("\",\"cargoEmpleado\":\"");
                retval.append(docSession.getCargoEmpleado());
                retval.append("\",\"noUrl\":\"");
                retval.append(docSession.getUrlDocumento());
                retval.append("\"}");
            } catch (Exception e) {
                retval.append("{\"retval\":");
                retval.append("Ocurrio un error");
                retval.append("\"}");
                e.printStackTrace();
            }
        }

        model.addAttribute("docSession", docSession);
        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goUpdEnvioNotificacion"}, produces = {"text/plain; charset=utf-8"})
    @ResponseBody
    public String goUpdEnvioNotificacion(HttpServletRequest request, Model model) {
        String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pnuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");

        String nCodAccion = ServletUtility.getInstancia().loadRequestParameter(request, "nCodAccion");
        String nCodUrgencia = ServletUtility.getInstancia().loadRequestParameter(request, "nCodUrgencia");
        String nCodDepMsj = ServletUtility.getInstancia().loadRequestParameter(request, "nCodDepMsj");

        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        String[] msg = new String[2];
        try {
            DocumentoEmiBean documentoEmiBean = new DocumentoEmiBean();
            documentoEmiBean.setNuEmi(pnuEmi);
            documentoEmiBean.setNuAnn(pnuAnn);

            documentoEmiBean.setTiEnvMsj(nCodAccion);
            documentoEmiBean.setCoPrioridad(nCodUrgencia);
            documentoEmiBean.setCoUseMod(usuario.getCoUsuario());
            if (nCodAccion.equals("0")) {

                documentoEmiBean.setCoDepEmi(nCodDepMsj);
            } else {

                documentoEmiBean.setCoDepEmi(usuario.getCoDep());
            }

            String vReturn = this.EmiDocumentoAdmService.changeToEnvioNotificacion(documentoEmiBean, usuario);
            if (vReturn.equals("OK")) {
                msg[0] = "1";
                msg[1] = "Datos Guardados";

            } else if (vReturn.equals("NO_OK")) {

                msg[0] = "0";
                msg[1] = "Verificar sino existen archivos registrados";
            } else {

                msg[0] = "0";
                msg[1] = "Error al actualizar el registro:" + vReturn;
            }

        } catch (validarDatoException e) {

            msg[0] = "2";
            msg[1] = e.valorMsg;
        } catch (Exception ex) {
            ex.printStackTrace();
            msg[0] = "0";
            msg[1] = "Error en el metodo";
        }
        StringBuilder retval = new StringBuilder();
        retval.append(msg[0]);
        retval.append(msg[1]);

        return retval.toString();
    }

    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goUpdEnvioMesaVirtual"}, produces = {"text/plain; charset=utf-8"})
    @ResponseBody
    public String goUpdEnvioMesaVirtual(HttpServletRequest request, Model model) {
        String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
        String pnuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");

        String nDeDepDes = ServletUtility.getInstancia().loadRequestParameter(request, "nDeDepDes");
        String nDeNomDes = ServletUtility.getInstancia().loadRequestParameter(request, "nDeNomDes");
        String nDeCarDes = ServletUtility.getInstancia().loadRequestParameter(request, "nDeCarDes");

        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        String[] msg = new String[2];

        try {
            DatosInterBean datosInter = new DatosInterBean();

            datosInter.setNuEmi(pnuEmi);
            datosInter.setNuAnn(pnuAnn);

            datosInter.setDeDepDes(nDeDepDes);
            datosInter.setDeNomDes(nDeNomDes);
            datosInter.setDeCarDes(nDeCarDes);
            datosInter.setCoUseMod(usuario.getCoUsuario());

            String vReturn = this.EmiDocumentoAdmService.changeToEnvioMesaVirtual(datosInter, usuario);

            if (vReturn.equals("OK")) {
                msg[0] = "1";
                msg[1] = "Datos Guardados";

            } else if (vReturn.equals("NO_OK")) {

                msg[0] = "0";
                msg[1] = "Verificar sino existen archivos registrados";
            } else {

                msg[0] = "0";
                msg[1] = "Error al actualizar el registro:" + vReturn;

            }

        } catch (Exception ex) {
            ex.printStackTrace();
            msg[0] = "0";
            msg[1] = "Error en el metodo";
        }
        StringBuilder retval = new StringBuilder();

        retval.append(msg[0]);

        retval.append(msg[1]);

        return retval.toString();
    }
}
