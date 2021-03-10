/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pe.gob.onpe.tramitedoc.bean;

/**
 *
 * @author NGilt
 */
public class SiElementoBean {

    private String ctabCodtab;
    private String celeCodele;
    private String celeDesele;
    private String neleNumsec;
    private String celeDescor;
    private byte[] archivo;

    public String getCtabCodtab() {
        return ctabCodtab;
    }

    public void setCtabCodtab(String ctabCodtab) {
        this.ctabCodtab = ctabCodtab;
    }

    public String getCeleCodele() {
        return celeCodele;
    }

    public void setCeleCodele(String celeCodele) {
        this.celeCodele = celeCodele;
    }

    public String getCeleDesele() {
        return celeDesele;
    }

    public void setCeleDesele(String celeDesele) {
        this.celeDesele = celeDesele;
    }

    public String getNeleNumsec() {
        return neleNumsec;
    }

    public void setNeleNumsec(String neleNumsec) {
        this.neleNumsec = neleNumsec;
    }

    public String getCeleDescor() {
        return celeDescor;
    }

    public void setCeleDescor(String celeDescor) {
        this.celeDescor = celeDescor;
    }

    /**
     * @return the archivo
     */
    public byte[] getArchivo() {
        return archivo;
    }

    /**
     * @param archivo the archivo to set
     */
    public void setArchivo(byte[] archivo) {
        this.archivo = archivo;
    }
    
}
