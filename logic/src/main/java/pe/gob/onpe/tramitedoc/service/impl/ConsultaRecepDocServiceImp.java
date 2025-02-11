package pe.gob.onpe.tramitedoc.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoRecepConsulBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoRecepConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.dao.ConsultaRecepDocDao;
import pe.gob.onpe.tramitedoc.service.ConsultaRecepDocService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;







@Service("consultaRecepDocService")
public class ConsultaRecepDocServiceImp
        implements ConsultaRecepDocService
{
    @Autowired
    ApplicationProperties applicationProperties;
    @Autowired
    private ConsultaRecepDocDao consultaRecepDocDao;

    public BuscarDocumentoRecepConsulBean estadoRecepcionDocumento(BuscarDocumentoRecepConsulBean buscarDocumentoRecepConsulBean, String accion) {
        estadoRecepcionDocumento(buscarDocumentoRecepConsulBean);
        return buscarDocumentoRecepConsulBean;
    }

    private void estadoRecepcionDocumento(BuscarDocumentoRecepConsulBean buscarDocumentoRecepConsulBean) {
        String sEstado = buscarDocumentoRecepConsulBean.getEstadoDoc();
        if (sEstado.equals("04")) {
            buscarDocumentoRecepConsulBean.setEstadoDoc("0");
            buscarDocumentoRecepConsulBean.setPrioridadDoc("2");
        } else if (sEstado.equals("01")) {
            buscarDocumentoRecepConsulBean.setEstadoDoc("0");
        } else if (sEstado.equals("07")) {
            buscarDocumentoRecepConsulBean.setEstadoDoc("0");
            buscarDocumentoRecepConsulBean.setPrioridadDoc("3");
        }
    }


    public List<DocumentoRecepConsulBean> getDocumentosBuscaRecepAdm(BuscarDocumentoRecepConsulBean buscarDocumentoRecepConsulBean) {
        List<DocumentoRecepConsulBean> list = null;

        try {
            list = this.consultaRecepDocDao.getDocumentosBuscaRecepAdm(buscarDocumentoRecepConsulBean);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }


    public DocumentoRecepConsulBean getDocumentoRecepAdm(String pnuAnn, String pnuEmi, String pnuDes) {
        DocumentoRecepConsulBean documentoRecepConsulBean = null;
        try {
            documentoRecepConsulBean = this.consultaRecepDocDao.getDocumentoRecepAdm(pnuAnn, pnuEmi, pnuDes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return documentoRecepConsulBean;
    }


    public List<ReferenciaConsulBean> getDocumentosRefRecepAdm(String pnuAnn, String pnuEmi) {
        List<ReferenciaConsulBean> list = null;

        try {
            list = this.consultaRecepDocDao.getDocumentosRefRecepAdm(pnuAnn, pnuEmi);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }


    public HashMap getDocumentosEnReferencia(BuscarDocumentoRecepConsulBean buscarDocumentoRecepConsulBean) {
        List<DocumentoRecepConsulBean> list = null;
        DocumentoRecepConsulBean documentoRecepConsulBean = null;
        HashMap<Object, Object> mp = new HashMap<Object, Object>();
        try {
            documentoRecepConsulBean = this.consultaRecepDocDao.existeDocumentoReferenciado(buscarDocumentoRecepConsulBean);
            if (documentoRecepConsulBean != null && documentoRecepConsulBean.getMsjResult() != null && documentoRecepConsulBean.getMsjResult().equals("OK")) {
                documentoRecepConsulBean.setCoDepDes(buscarDocumentoRecepConsulBean.getCoDependencia());
                documentoRecepConsulBean.setCoEmpDes(buscarDocumentoRecepConsulBean.getCoEmpleado());
                list = this.consultaRecepDocDao.getDocumentosReferenciadoBusq(documentoRecepConsulBean, buscarDocumentoRecepConsulBean.getTiAcceso());
                mp.put("recepDocumAdmList", list);
                mp.put("msjResult", "1");
            } else {
                mp.put("msjResult", "0");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return mp;
    }



    public String getRutaReporte(BuscarDocumentoRecepConsulBean buscarDocumentoRecepConsulBean) { return this.consultaRecepDocDao.getRutaReporte(buscarDocumentoRecepConsulBean); }



    public List<DocumentoRecepConsulBean> getListaReporte(BuscarDocumentoRecepConsulBean buscarDocumentoRecepConsulBean) { return this.consultaRecepDocDao.getListaReporteBusqueda(buscarDocumentoRecepConsulBean); }



    public ReporteBean getGenerarReporte(BuscarDocumentoRecepConsulBean buscarDocumentoRecepConsulBean, Map parametros) {
        List<DocumentoRecepConsulBean> lista = null;
        ReporteBean objReporte = new ReporteBean();
        String coRespuesta = "0";
        String deRespuesta = "";
        String prutaReporte = "";
        String deNoDoc = "";
        String coReporte = "";

        String nombreArchivo = "";
        int tipoArchivo = 0;

        String eserror = "1";


        try {

            String extensionArch, fecha = Utility.getInstancia().getStringFechaYYYYMMDDHHmm(new Date());

            try {
                lista = getListaReporte(buscarDocumentoRecepConsulBean);
                eserror = "0";
            } catch (Exception ex) {
                eserror = "1";
                deRespuesta = ex.getMessage();
            }
            if (lista.size() == 0) {
                coRespuesta = "1";
                deRespuesta = "No existe informaci n para generar reporte.";
                throw new Exception(deRespuesta);
            }
            if (buscarDocumentoRecepConsulBean.getFormatoReporte().equalsIgnoreCase("PDF")) {
                coReporte = "TDR09";
                extensionArch = ".pdf";
                tipoArchivo = 2;
            } else {
                coReporte = "TDR09_XLS";
                extensionArch = ".xls";
                tipoArchivo = 1;
            }

            String rutaJasper = buscarDocumentoRecepConsulBean.getRutaReporteJasper() + "/" + coReporte + ".jasper";
            buscarDocumentoRecepConsulBean.setRutaReporteJasper(rutaJasper);

            nombreArchivo = Utilidades.generateRandomNumber(10);
            prutaReporte = "RECEPDOCEXT_" + nombreArchivo + extensionArch;
            deNoDoc = "temp|RECEPDOCEXT_" + fecha + extensionArch;

            Utilidades util = new Utilidades();
            byte[] archivo = util.GenerarReporteLista(buscarDocumentoRecepConsulBean.getRutaReporteJasper(), lista, coReporte, parametros, tipoArchivo);

            if (archivo == null) {
                eserror = "0";
                coRespuesta = eserror;
                throw new Exception("No genercorrectamente el archivo del reporte.");
            }

            String rutaBase = this.applicationProperties.getRutaTemporal() + "/" + prutaReporte;

            File tmpFile = new File(rutaBase);
            FileOutputStream fos = new FileOutputStream(tmpFile);
            fos.write(archivo);
            fos.flush();
            fos.close();
            String rutaUrl = "reporte?coReporte=";
            prutaReporte = rutaUrl + prutaReporte;


            return objReporte;
        } catch (Exception e) {
            Logger.getLogger(ConsultaEmiDocServiceImp.class.getName()).log(Level.SEVERE, null, e); eserror = "1";
            return objReporte;
        }
        finally
        { Exception exception = null;
            objReporte.setcoRespuesta(coRespuesta);
            objReporte.setdeRespuesta(deRespuesta);
            objReporte.setnoUrl(prutaReporte);
            objReporte.setnoDoc(deNoDoc); }

    }
}
