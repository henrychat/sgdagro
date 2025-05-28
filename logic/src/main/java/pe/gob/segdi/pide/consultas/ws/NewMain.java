/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pe.gob.segdi.pide.consultas.ws;

import pe.gob.onpe.libreria.util.ConstantesSec;
import pe.gob.onpe.libreria.util.Utility;

/**
 *
 * @author mvaldera
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        /*
        String pass=Utility.getInstancia().cifrar("C:\\glassfish5\\glassfish\\tmppcm",ConstantesSec.SGD_SECRET_KEY_PROPERTIES);
        System.out.println("pass==>"+pass);
        String pass2=Utility.getInstancia().descifrar(pass,ConstantesSec.SGD_SECRET_KEY_PROPERTIES);
        System.out.println("pass2==>"+pass2);  
        */
    /*    
        String pass2=Utility.getInstancia().descifrar("5881236AED5F9FA69E5D8C59E0C979E8",ConstantesSec.SGD_SECRET_KEY_PASSWORD);
        System.out.println("pass2==>"+pass2);  */
        
       // String pass3=Utility.getInstancia().cifrar("20603965451",ConstantesSec.SGD_SECRET_KEY_PASSWORD);
        String pass3=Utility.getInstancia().cifrar("/mnt/imagen_remoto/produccion",ConstantesSec.SGD_SECRET_KEY_PROPERTIES);
        System.out.println("passGet==>"+pass3);

        String pass4=Utility.getInstancia().descifrar("101E9029B29F56FD26C57E65535E0A9E",ConstantesSec.SGD_SECRET_KEY_PASSWORD);
        System.out.println("pass2==>"+pass4);
        
        String pass5=Utility.getInstancia().descifrar("A55D5F0D0A10239AF879C06C5D52F078C69AAA5B5B629A95AD4098E7D77C130B",ConstantesSec.SGD_SECRET_KEY_PROPERTIES);
        System.out.println("passRUC==>"+pass5);
        String pass6=Utility.getInstancia().descifrar("7335756129A2E0DE3BA278CF2DDB2457",ConstantesSec.SGD_SECRET_KEY_PASSWORD);
        System.out.println("pass2==>"+pass6);
    //     String pass5=Utility.getInstancia().descifrar("10F2852051B7BABD69D6D1C876EE9C6A051ABF082B410FFB79036F15AC90B25F",ConstantesSec.SGD_SECRET_KEY_PROPERTIES);
     //   System.out.println("pass3==>"+pass5);
         
    }
    
}
