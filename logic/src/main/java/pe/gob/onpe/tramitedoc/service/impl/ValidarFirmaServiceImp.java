package pe.gob.onpe.tramitedoc.service.impl;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.service.ValidarFirmaService;


@Service("validarFirmaService")
public class ValidarFirmaServiceImp implements ValidarFirmaService
{
  public String getValidaCertificadoEmp(String filePdf, String pNuDni, String pNuRucInstitucion) {
    setBouncyCastleProvider();
    String vret = "NO_OK";
    PdfReader reader = null;
    try {
      reader = new PdfReader(filePdf);
      AcroFields af = reader.getAcroFields();
      
      List<String> names = af.getSignatureNames();
      if (!names.isEmpty()) {
        ArrayList<PdfPKCS7> LsSignature = new ArrayList<PdfPKCS7>();
        for (String name : names)
        {
          LsSignature.add(af.verifySignature(name, "BC"));
        }
        Collections.sort(LsSignature, new Comparator<PdfPKCS7>()
            {
              public int compare(PdfPKCS7 o1, PdfPKCS7 o2) {
                if (o1.getSignDate() == null || o2.getSignDate() == null)
                  return 0; 
                return o1.getSignDate().compareTo(o2.getSignDate()) * -1;
              }
            });
        Iterator<PdfPKCS7> iterator = LsSignature.iterator(); if (iterator.hasNext()) { PdfPKCS7 pk = iterator.next();
          String dn = PdfPKCS7.getSubjectFields(pk.getSigningCertificate()).toString();
         if (dn.contains(pNuDni)) {
            if (dn.contains(pNuRucInstitucion)) {
              vret = "OK";
            }
          } 
         }

      
      }
      else {
        
        vret = "";
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (reader != null) {
        reader.close();
      }
    } 
    return vret;
  }


  
  public String getValidaCertificadoEmp(String filePdf, String pNuDni) {
    setBouncyCastleProvider();
    String vret = "NO_OK";
    PdfReader reader = null;
    try {
      reader = new PdfReader(filePdf);
      AcroFields af = reader.getAcroFields();
      List<String> names = af.getSignatureNames();
      if (!names.isEmpty()) {
        ArrayList<PdfPKCS7> LsSignature = new ArrayList<PdfPKCS7>();
        for (String name : names)
        {
          LsSignature.add(af.verifySignature(name, "BC"));
        }
        Collections.sort(LsSignature, new Comparator<PdfPKCS7>()
            {
              public int compare(PdfPKCS7 o1, PdfPKCS7 o2) {
                if (o1.getSignDate() == null || o2.getSignDate() == null)
                  return 0; 
                return o1.getSignDate().compareTo(o2.getSignDate()) * -1;
              }
            });
        Iterator<PdfPKCS7> iterator = LsSignature.iterator(); if (iterator.hasNext()) { PdfPKCS7 pk = iterator.next();
          String dn = PdfPKCS7.getSubjectFields(pk.getSigningCertificate()).toString();








          
          if (dn.contains(pNuDni)) {
            vret = "OK";
          } }
      
      } else {
        
        vret = "";
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (reader != null) {
        reader.close();
      }
    } 
    return vret;
  }















  
  private String getValByAttributeTypeFromIssuerDN(String dn, String attributeType) {
    String[] dnSplits = dn.split(",");
    for (String dnSplit : dnSplits) {
      if (dnSplit.contains(attributeType)) {
        String[] cnSplits = dnSplit.trim().split("=");
        if (cnSplits[1] != null) {
          return cnSplits[1].trim();
        }
      } 
    } 
    return "";
  }
  
  private String getValByAttributeTypeFromIssuerSN(String sn, String attributeType) {
    String[] dnSplits = sn.split("-");
    for (String dnSplit : dnSplits) {
      if (dnSplit.contains(attributeType)) {
        String[] cnSplits = dnSplit.trim().split(":");
        if (cnSplits[1] != null) {
          return cnSplits[1].trim();
        }
      } 
    } 
    return "";
  }
  
  private void setBouncyCastleProvider() {
    Provider provider = null;
    
    try {
      Class<?> c = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
      Security.insertProviderAt((Provider)c.newInstance(), 2000);
      
      provider = (Provider)c.newInstance();
    }
    catch (Exception e) {
      provider = null;
    } 
  }
}
