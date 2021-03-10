/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pe.gob.onpe.tramitedoc.web.servlet;

/**
 *
 * @author ypino
 */
import java.io.IOException;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.font.TextAttribute;
 
import java.awt.image.BufferedImage;
 
import javax.imageio.ImageIO;
import javax.servlet.http.*;
import java.util.Random;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;
 
public class CaptchaGenServlet extends HttpServlet {
 
         public static final String FILE_TYPE = "jpeg";
 
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {
 
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Max-Age", 0);
 
            String captchaStr="";
 
        //captchaStr=Util.generateCaptchaTextMethod();
 
            captchaStr=generateCaptchaTextMethod2(6).toUpperCase();
 
            try {
 
                int width=205;
                int height=50;//55
 
                Color bg = new Color(245,250,255);
                Color bg2 = new Color(229,229,229);
                Color fg = new Color(0,35,112);
 
                Font font = new Font("Segoe Script",Font.BOLD | Font.ITALIC, 28);//30
                BufferedImage cpimg =new BufferedImage(width,height,BufferedImage.OPAQUE);
                Graphics2D  g = cpimg.createGraphics();
                
                Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
                attributes.put(TextAttribute.TRACKING, 0.3);                
                font = font.deriveFont(attributes);

                g.setFont(font);
                /*g.setColor(bg);
                g.fillRect(0, 0, width, height);*/
                        BufferedImage bi = new BufferedImage(5, 5,BufferedImage.TYPE_INT_RGB);
                        Graphics2D big = bi.createGraphics();
                        big.setColor(bg2);
                        big.fillRect(0, 0, 5, 5);
                        big.setColor(bg);
                        big.fillOval(0, 0, 5, 5);
                        Rectangle r = new Rectangle(0, 0, 5, 5);
                        g.setPaint(new TexturePaint(bi, r));

                        Rectangle rect = new Rectangle(0,0,width,height);

                        g.fill(rect);
                
                
                g.setColor(fg);
                g.drawString(captchaStr,15,33);  ///20-40    
                request.getSession().setAttribute("CAPTCHA", captchaStr);
                OutputStream outputStream = response.getOutputStream();
 
               ImageIO.write(cpimg, FILE_TYPE, outputStream);
               outputStream.close();
 
            } catch (Exception e) {
                    e.printStackTrace();
            }
        }
 
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {
            doPost(request, response);
        }

   
public static String generateCaptchaTextMethod1()    {
 
        Random rdm=new Random();
        int rl=rdm.nextInt(); // Random numbers are generated.
        String hash1 = Integer.toHexString(rl); // Random numbers are converted to Hexa Decimal.
 
        return hash1;
 
}
 
public static String generateCaptchaTextMethod2(int captchaLength)   {
 
     String saltChars = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNOPQRSTUVWXYZ1234567890";
     StringBuffer captchaStrBuffer = new StringBuffer();
            java.util.Random rnd = new java.util.Random();
 
            // build a random captchaLength chars salt
            while (captchaStrBuffer.length() < captchaLength)
            {
                int index = (int) (rnd.nextFloat() * saltChars.length());
                captchaStrBuffer.append(saltChars.substring(index, index+1));
            }
 
        return captchaStrBuffer.toString();
 
}
 
 }
