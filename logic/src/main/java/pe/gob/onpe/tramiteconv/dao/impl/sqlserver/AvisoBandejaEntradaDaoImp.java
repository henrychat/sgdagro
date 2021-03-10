package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.AvisoBandejaEntradaBean;
import pe.gob.onpe.tramitedoc.bean.EtiquetaBandejaEnBean;
import pe.gob.onpe.tramitedoc.dao.AvisoBandejaEntradaDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;








@Repository("avisoBandejaEntradaDao")
public class AvisoBandejaEntradaDaoImp
  extends SimpleJdbcDaoBase
  implements AvisoBandejaEntradaDao
{
  public List<AvisoBandejaEntradaBean> getBandejaEntradaAccesoTotal(String coDependencia, String coEmpleado) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT 1 orden,'06' Ti_Pen,  'PARA DESPACHO' de_pen, 'Documentos Administrativos listos para la Firma' de_resumen, SUM(nu_can) Nu_Can , :pCoDep co_dep,");
    sql.append("  '' co_bandeja FROM (SELECT count(1) Nu_Can     FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK)       WHERE r.Es_Doc_Emi IN ('7')      AND r.Ti_Emi = '01'     ");
    sql.append("  AND r.Co_Dep_Emi = :pCoDep        GROUP BY r.Co_Dep_Emi        union select 0 nu_can ) AS Q1 UNION SELECT 2 ORDEN,'VB' TI_PEN,    ");
    sql.append("  'PARA VISTO BUENO' DE_PEN,'Documentos Administrativos listos para Visto Bueno' DE_RESUMEN,    COUNT(1) NU_CAN,:pCoDep CO_DEP, '' co_bandeja   ");
    sql.append("    FROM IDOSGD.TDTV_REMITOS R  WITH(NOLOCK)   INNER JOIN  IDOSGD.TDTV_PERSONAL_VB VB  WITH(NOLOCK) ON R.NU_ANN=VB.NU_ANN AND R.NU_EMI=VB.NU_EMI    ");
    sql.append("  WHERE  (R.ES_DOC_EMI='7' OR (R.TI_EMI='05' AND R.ES_DOC_EMI='5') )          AND VB.CO_DEP=:pCoDep         AND VB.CO_EMP_VB=:pCoEmp         AND VB.IN_VB <> '1'        ");
    sql.append("  GROUP BY VB.CO_DEP UNION SELECT 3 orden,'05' Ti_Pen,        'EN PROYECTO' De_Pen,'Documentos Administrativos en proceso de elaboración' de_resumen,      ");
    sql.append("    sum(Nu_Can) Nu_Can , :pCoDep co_dep, '' co_bandeja    from    (select          COUNT(1) Nu_Can      FROM IDOSGD.Tdtv_Remitos r  WITH(NOLOCK)     WHERE r.Es_Doc_Emi IN ('5')    ");
    sql.append("     AND r.Ti_Emi = '01'       AND r.Co_Dep_Emi = :pCoDep     GROUP BY r.Co_Dep_Emi      union select 0 nu_can        ) AS Q2  ");
    sql.append(" UNION    SELECT  5 orden ,  '07' ti_pen, 'MUY URGENTES' de_pen, 'Documentos pendientes de recibir MUY URGENTES' de_resumen ,             ");
    sql.append("  COUNT (1) nu_can, :pCoDep co_dep, '' co_bandeja         FROM IDOSGD.tdtv_remitos r  WITH(NOLOCK)     INNER JOIN IDOSGD.tdtv_destinos t  WITH(NOLOCK)  ");
    sql.append(" ON  r.nu_ann = t.nu_ann AND r.nu_emi = t.nu_emi       WHERE  r.es_doc_emi NOT IN ('5', '9', '7')          AND t.es_doc_rec = '0'         AND t.es_eli = '0'   ");
    sql.append("    AND t.co_pri = '3'         AND r.in_oficio = '0'         AND t.co_dep_des = :pCoDep     GROUP BY t.co_dep_des UNION SELECT 6 orden ,  '04' ti_pen, 'URGENTES' de_pen, 'Documentos pendientes de recibir URGENTES' de_resumen ,  ");
    sql.append("    COUNT (1) nu_can , :pCoDep co_dep, '' co_bandeja         FROM IDOSGD.tdtv_remitos r  WITH(NOLOCK)     ");
    sql.append("  INNER JOIN IDOSGD.tdtv_destinos t  WITH(NOLOCK) ON  r.nu_ann = t.nu_ann AND r.nu_emi = t.nu_emi       WHERE  r.es_doc_emi NOT IN ('5', '9', '7')          AND t.es_doc_rec = '0'    ");
    sql.append("       AND t.es_eli = '0'         AND t.co_pri = '2'         AND r.in_oficio = '0'         AND t.co_dep_des = :pCoDep     GROUP BY t.co_dep_des UNION SELECT 7 orden,'02' Ti_Pen,       ");
    sql.append("    'RECIBIDOS (Oficina)' De_Pen, 'Documentos recibidos' de_resumen,         SUM(nu_can) Nu_Can , :pCoDep co_dep, 'O' co_bandeja  FROM (SELECT  COUNT(1) Nu_Can        FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)      ");
    sql.append("    INNER JOIN IDOSGD.Tdtv_Destinos t  WITH(NOLOCK) ON r.Nu_Ann = t.Nu_Ann  AND r.Nu_Emi = t.Nu_Emi        WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9')        AND t.Es_Doc_Rec = '1'        AND t.Es_Eli = '0'    ");
    sql.append("      AND t.Co_Dep_Des = :pCoDep         AND t.CO_EMP_DES in ( SELECT CO_EMPLEADO FROM IDOSGD.RHTM_DEPENDENCIA  WITH(NOLOCK) WHERE CO_DEPENDENCIA= :pCoDep )         GROUP BY t.Co_Dep_Des      union select 0 nu_can  ) AS Q3 UNION SELECT 8 orden,'01' Ti_Pen,      ");
    sql.append("     'NO LEIDOS (Oficina)' De_Pen, 'Documentos pendientes de recibir' de_resumen,         SUM(nu_can) Nu_Can , :pCoDep co_dep, 'O' co_bandeja  FROM (SELECT  COUNT(1) Nu_Can     ");
    sql.append("     FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)        INNER JOIN     IDOSGD.Tdtv_Destinos t  WITH(NOLOCK) ON r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi       WHERE   r.Es_Doc_Emi NOT IN ('5', '7', '9')        AND t.Es_Doc_Rec = '0'      ");
    sql.append("    AND t.Es_Eli = '0'        AND t.Co_Dep_Des = :pCoDep         AND t.CO_EMP_DES in ( SELECT CO_EMPLEADO FROM IDOSGD.RHTM_DEPENDENCIA  WITH(NOLOCK) WHERE CO_DEPENDENCIA= :pCoDep )     ");
    sql.append("     GROUP BY t.Co_Dep_Des      union select 0 nu_can  ) AS Q3 UNION SELECT 14 orden,'02' Ti_Pen,         'RECIBIDOS (Profesional)' De_Pen, 'Documentos recibidos' de_resumen,      ");
    sql.append("     SUM(nu_can) Nu_Can , :pCoDep co_dep, 'P' co_bandeja  FROM (SELECT  COUNT(1) Nu_Can        FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)        INNER JOIN IDOSGD.Tdtv_Destinos t  WITH(NOLOCK) ON r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi    ");
    sql.append("     WHERE    r.Es_Doc_Emi NOT IN ('5', '7', '9')        AND t.Es_Doc_Rec = '1'        AND t.Es_Eli = '0'        AND t.Co_Dep_Des = :pCoDep         AND NOT t.CO_EMP_DES in ( SELECT CO_EMPLEADO FROM IDOSGD.RHTM_DEPENDENCIA  WITH(NOLOCK) WHERE CO_DEPENDENCIA= :pCoDep )        ");
    sql.append("   GROUP BY t.Co_Dep_Des      union select 0 nu_can  ) AS Q3 UNION SELECT 17 orden,'01' Ti_Pen,         'NO LEIDOS (Profesional)' De_Pen, 'Documentos pendientes de recibir' de_resumen,         SUM(nu_can) Nu_Can , :pCoDep co_dep, 'P' co_bandeja  FROM (SELECT  COUNT(1) Nu_Can   ");
    sql.append("       FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)         INNER JOIN IDOSGD.Tdtv_Destinos t  WITH(NOLOCK) ON r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi        WHERE   r.Es_Doc_Emi NOT IN ('5', '7', '9')        AND t.Es_Doc_Rec = '0'        AND t.Es_Eli = '0'      ");
    sql.append("   AND t.Co_Dep_Des = :pCoDep         AND NOT t.CO_EMP_DES in ( SELECT CO_EMPLEADO FROM IDOSGD.RHTM_DEPENDENCIA  WITH(NOLOCK) WHERE CO_DEPENDENCIA= :pCoDep )         GROUP BY t.Co_Dep_Des     union select 0 nu_can  ) AS Q3     ");
    
    sql.append("    UNION SELECT 18 orden,'08' Ti_Pen,  ");
    sql.append("    '<p style=''color:#D50911;''>PENDIENTE DE ENVIO</p>' De_Pen, 'Doc. pendientes de envio a mensajería' de_resumen,         COUNT(1) Nu_Can , :pCoDep co_dep, '' co_bandeja        FROM IDOSGD.Tdtv_Remitos r  WITH(NOLOCK)       WHERE  r.Es_Doc_Emi ='0'   ");
   // sql.append("       AND r.DOC_ESTADO_MSJ is null and r.TI_ENV_MSJ is null          AND r.CO_TIP_DOC_ADM in ('011','012')       AND r.Es_Eli = '0'       AND r.Co_Dep_Emi = :pCoDep      GROUP BY r.Co_Dep_Emi ORDER BY 1  ");
    sql.append("         AND r.DOC_ESTADO_MSJ is null and r.TI_ENV_MSJ is null   AND r.CO_TIP_DOC_ADM in ('011','012','248')  AND r.CO_GRU = '1'  and r.TI_EMI ='01'     ");
    sql.append(" AND NOT EXISTS(select D.NU_EMI from IDOSGD.TDTV_DESTINOS D   WHERE TI_DES ='01'  AND  D.NU_EMI=r.NU_EMI AND D.NU_ANN=r.NU_ANN   ");
    sql.append(" and   d.ES_ELI='0')  AND r.Es_Eli = '0'  AND r.Co_Dep_Emi = :pCoDep  GROUP BY r.Co_Dep_Emi ORDER BY 1  ");
    List<AvisoBandejaEntradaBean> list = new ArrayList<AvisoBandejaEntradaBean>();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    objectParam.put("pCoDep", coDependencia);
    objectParam.put("pCoEmp", coEmpleado);
    
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(AvisoBandejaEntradaBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<AvisoBandejaEntradaBean> getBandejaEntradaAccesoPersonal(String coDependencia, String coEmpleado) {
    StringBuffer sql = new StringBuffer();
    //sql.append(" SELECT 1 orden,'06' Ti_Pen,       'PARA DESPACHO' De_Pen, 'Documentos Administrativos listos para la Firma' de_resumen,  ");
  //  sql.append("       SUM(nu_can) Nu_Can , :pCoDep co_dep  FROM (SELECT count(1) Nu_Can             FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK)      ");
   // sql.append("    WHERE r.Es_Doc_Emi IN ('7')       AND r.Ti_Emi = '01'          AND r.Co_Dep_Emi = :pCoDep          AND r.Co_Emp_Res = :pCoEmp        GROUP BY r.Co_Dep_Emi, r.Co_Emp_Res       union select 0 nu_can) Q1  UNION SELECT 2 ORDEN,'VB' TI_PEN,    'PARA VISTO BUENO' DE_PEN,'Documentos Administrativos listos para Visto Bueno' DE_RESUMEN,    COUNT(1) NU_CAN,:pCoDep CO_DEP    FROM IDOSGD.TDTV_REMITOS R  WITH(NOLOCK)    INNER JOIN  IDOSGD.TDTV_PERSONAL_VB VB WITH(NOLOCK)  ON R.NU_ANN=VB.NU_ANN  AND R.NU_EMI=VB.NU_EMI    WHERE  (R.ES_DOC_EMI='7' OR (R.TI_EMI='05' AND R.ES_DOC_EMI='5')  )          AND VB.CO_DEP=:pCoDep         AND VB.CO_EMP_VB=:pCoEmp         AND VB.IN_VB <> '1'         GROUP BY VB.CO_DEP UNION SELECT 3 orden,'05' Ti_Pen,        'EN PROYECTO' De_Pen,'Documentos Administrativos en proceso de elaboración' de_resumen,         sum(Nu_Can) Nu_Can , :pCoDep co_dep    from    (select          COUNT(1) Nu_Can      FROM IDOSGD.Tdtv_Remitos r  WITH(NOLOCK)     WHERE r.Es_Doc_Emi IN ('5')       AND r.Ti_Emi = '01'       AND r.Co_Dep_Emi = :pCoDep       AND r.Co_Emp_Res = :pCoEmp     GROUP BY r.Co_Dep_Emi,              r.Co_Emp_Res       union select 0 nu_can       ) Q2  UNION SELECT 4 orden,'07' Ti_Pen,        'MUY URGENTES' De_Pen,'Documentos pendientes de recibir MUY URGENTES' de_resumen,        COUNT(1) Nu_Can, :pCoDep co_dep    FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)    INNER JOIN  IDOSGD.Tdtv_Destinos t  WITH(NOLOCK) ON r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi   WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9') --en proyecto, anulados    AND t.Co_Dep_Des = :pCoDep     AND t.Co_Emp_Des = :pCoEmp     AND t.Es_Doc_Rec = '0'    AND t.Es_Eli = '0'    AND t.Co_Pri = '3'    AND r.in_oficio = '0'  GROUP BY t.Co_Dep_Des,           t.Co_Emp_Des UNION SELECT 6 orden,'04' Ti_Pen,        'URGENTES' De_Pen, 'Documentos pendientes de recibir URGENTES' de_resumen,        COUNT(1) Nu_Can, :pCoDep co_dep    FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)    INNER JOIN  IDOSGD.Tdtv_Destinos t  WITH(NOLOCK) ON r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi   WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9') --en proyecto, anulados    AND t.Co_Dep_Des = :pCoDep     AND t.Co_Emp_Des = :pCoEmp     AND t.Es_Doc_Rec = '0'    AND t.Es_Eli = '0'    AND t.Co_Pri = '2'    AND r.in_oficio = '0'  GROUP BY t.Co_Dep_Des,           t.Co_Emp_Des   UNION SELECT 4 orden,'02' Ti_Pen,         'RECIBIDOS' De_Pen, 'Documentos Recibidos' de_resumen,         SUM(nu_can) Nu_Can , :pCoDep co_dep  FROM (SELECT  COUNT(1) Nu_Can    FROM IDOSGD.Tdtv_Remitos  r WITH(NOLOCK)     INNER JOIN  IDOSGD.Tdtv_Destinos t  WITH(NOLOCK) ON r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi       WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9')        AND t.Co_Dep_Des = :pCoDep         AND t.Co_Emp_Des = :pCoEmp         AND t.Es_Doc_Rec = '1'        AND t.Es_Eli = '0'      GROUP BY t.Co_Dep_Des,               t.Co_Emp_Des       union select 0 nu_can ) Q3 UNION SELECT 7 orden,'01' Ti_Pen,         'NO LEIDOS' De_Pen, 'Documentos pendientes de recibir' de_resumen,         SUM(nu_can) Nu_Can , :pCoDep co_dep  FROM (SELECT  COUNT(1) Nu_Can    FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)    INNER JOIN  IDOSGD.Tdtv_Destinos t WITH(NOLOCK)  ON r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi       WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9')        AND t.Co_Dep_Des = :pCoDep         AND t.Co_Emp_Des = :pCoEmp         AND t.Es_Doc_Rec = '0'        AND t.Es_Eli = '0'      GROUP BY t.Co_Dep_Des,               t.Co_Emp_Des       union select 0 nu_can ) Q3  ");

   
    sql.append(" SELECT 1 orden,'06' Ti_Pen,       'PARA DESPACHO' De_Pen, 'Documentos Administrativos listos para la Firma' de_resumen,");   
    sql.append(" SUM(nu_can) Nu_Can , :pCoDep co_dep  FROM (SELECT count(1) Nu_Can             FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK) ");     
    sql.append(" WHERE r.Es_Doc_Emi IN ('7')       AND r.Ti_Emi = '01'          AND r.Co_Dep_Emi = :pCoDep          AND r.Co_Emp_Res = :pCoEmp ");      
    sql.append(" GROUP BY r.Co_Dep_Emi, r.Co_Emp_Res     	union	select 0 nu_can) Q1 	UNION SELECT 2 ORDEN,'VB' TI_PEN,");   
    sql.append(" 'PARA VISTO BUENO' DE_PEN,'Documentos Administrativos listos para Visto Bueno' DE_RESUMEN,    COUNT(1) NU_CAN,:pCoDep CO_DEP    FROM IDOSGD.TDTV_REMITOS R  WITH(NOLOCK) ");  
    sql.append(" INNER JOIN  IDOSGD.TDTV_PERSONAL_VB VB WITH(NOLOCK)  ON R.NU_ANN=VB.NU_ANN  AND R.NU_EMI=VB.NU_EMI    WHERE  (R.ES_DOC_EMI='7' OR (R.TI_EMI='05' AND R.ES_DOC_EMI='5')  ) ");   
    sql.append(" AND VB.CO_DEP=:pCoDep         AND VB.CO_EMP_VB=:pCoEmp         AND VB.IN_VB <> '1'   GROUP BY VB.CO_DEP "); 
    sql.append(" UNION "); 
    sql.append(" SELECT 3 orden,'05' Ti_Pen, 	'EN PROYECTO' De_Pen,'Documentos Administrativos en proceso de elaboracide_resumen',         sum(Nu_Can) Nu_Can , :pCoDep co_dep    from    (select ");    
    sql.append(" COUNT(1) Nu_Can      FROM IDOSGD.Tdtv_Remitos r  WITH(NOLOCK)     WHERE r.Es_Doc_Emi IN ('5')       AND r.Ti_Emi = '01'       AND r.Co_Dep_Emi = :pCoDep ");   
    sql.append(" AND r.Co_Emp_Res = :pCoEmp     GROUP BY r.Co_Dep_Emi,   r.Co_Emp_Res ");    
    sql.append(" union select 0 nu_can       ) Q2  UNION SELECT 4 orden,'07' Ti_Pen, ");  
    sql.append(" 'MUY URGENTES' De_Pen,'Documentos pendientes de recibir MUY URGENTES' de_resumen,        COUNT(1) Nu_Can, :pCoDep co_dep    FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK) ");
    sql.append(" INNER JOIN  IDOSGD.Tdtv_Destinos t  WITH(NOLOCK) ON r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi   WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9')  ");   
    sql.append(" AND t.Co_Dep_Des = :pCoDep     AND t.Co_Emp_Des = :pCoEmp ");   
    sql.append(" AND t.Es_Doc_Rec = '0'    AND t.Es_Eli = '0'    AND t.Co_Pri = '3'    AND r.in_oficio = '0'  GROUP BY t.Co_Dep_Des, ");       
    sql.append(" t.Co_Emp_Des UNION SELECT 6 orden,'04' Ti_Pen,        'URGENTES' De_Pen, 'Documentos pendientes de recibir URGENTES' de_resumen, ");     
    sql.append(" COUNT(1) Nu_Can, :pCoDep co_dep    FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)    INNER JOIN  IDOSGD.Tdtv_Destinos t  WITH(NOLOCK) ");
    sql.append(" ON r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi   WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9') "); 
    sql.append(" AND t.Co_Dep_Des = :pCoDep     AND t.Co_Emp_Des = :pCoEmp     AND t.Es_Doc_Rec = '0'    AND t.Es_Eli = '0'    AND t.Co_Pri = '2' ");  
    sql.append(" AND r.in_oficio = '0'  GROUP BY t.Co_Dep_Des,  t.Co_Emp_Des "); 
    sql.append(" UNION SELECT 4 orden,'02' Ti_Pen, ");       
    sql.append(" 'RECIBIDOS' De_Pen, 'Documentos Recibidos' de_resumen,         SUM(nu_can) Nu_Can , :pCoDep co_dep  FROM (SELECT  COUNT(1) Nu_Can ");   
    sql.append(" FROM IDOSGD.Tdtv_Remitos  r WITH(NOLOCK)     INNER JOIN  IDOSGD.Tdtv_Destinos t  WITH(NOLOCK) ON r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi ");     
    sql.append(" WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9')        AND t.Co_Dep_Des = :pCoDep         AND t.Co_Emp_Des = :pCoEmp         AND t.Es_Doc_Rec = '1' ");   
    sql.append(" AND t.Es_Eli = '0'      GROUP BY t.Co_Dep_Des,               t.Co_Emp_Des ");     
    sql.append(" union select 0 nu_can ) Q3 UNION ");
    sql.append(" SELECT 7 orden,'01' Ti_Pen, ");     
    sql.append(" 'NO LEIDOS' De_Pen, 'Documentos pendientes de recibir' de_resumen,    SUM(nu_can) Nu_Can , :pCoDep co_dep "); 
    sql.append(" FROM (SELECT  COUNT(1) Nu_Can ");   
    sql.append(" FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK) ");  
    sql.append(" INNER JOIN  IDOSGD.Tdtv_Destinos t WITH(NOLOCK)  ON r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi ");     
    sql.append(" WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9')        AND t.Co_Dep_Des = :pCoDep ");   
    sql.append(" AND t.Co_Emp_Des = :pCoEmp         AND t.Es_Doc_Rec = '0' ");   
    sql.append(" AND t.Es_Eli = '0'      GROUP BY t.Co_Dep_Des, ");     
    sql.append(" t.Co_Emp_Des       union select 0 nu_can ) Q3 "); 
	
	
    
    List<AvisoBandejaEntradaBean> list = new ArrayList<AvisoBandejaEntradaBean>();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    objectParam.put("pCoDep", coDependencia);
    objectParam.put("pCoEmp", coEmpleado);
    
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(AvisoBandejaEntradaBean.class));
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<AvisoBandejaEntradaBean> getBandejaEntradaAccesoFuncionario(String coDependencia, String coEmpleado) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT 1 orden,'06' Ti_Pen,         'PARA DESPACHO' De_Pen, 'Documentos Administrativos listos para la Firma' de_resumen ,         COUNT(1) Nu_Can, :pCoDep co_dep    FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK)    WHERE r.Es_Doc_Emi IN ('7') --Listos para despacho      AND r.Ti_Emi = '01'      and r.Co_Dep_Emi = :pCoDep      AND (r.Co_Emp_Res = :pCoEmp OR r.co_emp_emi = :pCoEmp)   GROUP BY r.Co_Dep_Emi   UNION SELECT 2 ORDEN,'VB' TI_PEN,    'PARA VISTO BUENO' DE_PEN,'Documentos Administrativos listos para Visto Bueno' DE_RESUMEN,    COUNT(1) NU_CAN,:pCoDep CO_DEP    FROM IDOSGD.TDTV_REMITOS R  WITH(NOLOCK)   INNER JOIN IDOSGD.TDTV_PERSONAL_VB VB  WITH(NOLOCK)  ON  R.NU_ANN=VB.NU_ANN  AND R.NU_EMI=VB.NU_EMI    WHERE  R.ES_DOC_EMI='7'         AND VB.CO_DEP=:pCoDep         AND VB.CO_EMP_VB=:pCoEmp         AND VB.IN_VB <> '1'         GROUP BY VB.CO_DEP UNION  SELECT 3 orden,'05' Ti_Pen,         'EN PROYECTO' De_Pen, 'Documentos Administrativos en proceso de elaboración ' de_resumen,         COUNT(1) Nu_Can, :pCoDep co_dep    FROM IDOSGD.Tdtv_Remitos r WITH(NOLOCK)    WHERE r.Es_Doc_Emi IN ('5') --En proyecto     AND r.Ti_Emi = '01'     and r.Co_Dep_Emi = :pCoDep      AND (r.Co_Emp_Res = :pCoEmp OR r.co_emp_emi = :pCoEmp)   GROUP BY r.Co_Dep_Emi       UNION  SELECT 4 orden,'07' Ti_Pen,         'MUY URGENTES' De_Pen, 'Documentos pendientes de recibir MUY URGENTES' de_resumen,         COUNT(1) Nu_Can, :pCoDep co_dep    FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)    INNER JOIN      Tdtv_Destinos t WITH(NOLOCK) ON  r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi    WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9') --en proyecto, anulados     AND t.Es_Doc_Rec = '0'     AND t.Es_Eli = '0'     AND t.Co_Pri = '3'     and t.Co_Dep_Des = :pCoDep      AND t.Co_Emp_Des = :pCoEmp   GROUP BY t.Co_Dep_Des,            t.Co_Emp_Des      UNION  SELECT 5 orden,'04' Ti_Pen,         'URGENTES' De_Pen, 'Documentos pendientes de recibir URGENTES' de_resumen,         COUNT(1) Nu_Can, :pCoDep co_dep    FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)    INNER JOIN      Tdtv_Destinos t WITH(NOLOCK) ON  r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi    WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9') --en proyecto, anulados     AND t.Es_Doc_Rec = '0'     AND t.Es_Eli = '0'     AND t.Co_Pri = '2'     and t.Co_Dep_Des = :pCoDep     AND t.Co_Emp_Des = :pCoEmp   GROUP BY t.Co_Dep_Des,            t.Co_Emp_Des    UNION  SELECT 4 orden,'01' Ti_Pen,         'BANDEJA' De_Pen, 'Documentos pendientes de recibir' de_resumen,         COUNT(1) Nu_Can, :pCoDep co_dep    FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)    INNER JOIN      Tdtv_Destinos t WITH(NOLOCK) ON  r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi    WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9') --en proyecto, anulados     AND t.Es_Doc_Rec = '1'     AND t.Es_Eli = '0'     and t.Co_Dep_Des = :pCoDep      AND t.Co_Emp_Des = :pCoEmp    GROUP BY t.Co_Dep_Des, t.Co_Emp_Des     UNION  SELECT 7 orden,'01' Ti_Pen,         'NO LEIDOS' De_Pen, 'Documentos pendientes de recibir' de_resumen,         COUNT(1) Nu_Can, :pCoDep co_dep    FROM IDOSGD.Tdtv_Remitos  r  WITH(NOLOCK)    INNER JOIN      Tdtv_Destinos t WITH(NOLOCK) ON  r.Nu_Ann = t.Nu_Ann AND r.Nu_Emi = t.Nu_Emi    WHERE  r.Es_Doc_Emi NOT IN ('5', '7', '9') --en proyecto, anulados     AND t.Es_Doc_Rec = '0'     AND t.Es_Eli = '0'     and t.Co_Dep_Des = :pCoDep      AND t.Co_Emp_Des = :pCoEmp    GROUP BY t.Co_Dep_Des, t.Co_Emp_Des             ");

    List<AvisoBandejaEntradaBean> list = new ArrayList<AvisoBandejaEntradaBean>();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    objectParam.put("pCoDep", coDependencia);
    objectParam.put("pCoEmp", coEmpleado);

    
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(AvisoBandejaEntradaBean.class));
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<EtiquetaBandejaEnBean> getListEtiquetaBandejaEntrada(String coDep, String coEmp) {
    StringBuffer sql = new StringBuffer();
    sql.append("select e.co_est,e.de_est descripcion,count(*) numero_documentos,d.co_dep_des,d.co_emp_rec ");
    sql.append("from IDOSGD.tdtv_bandeja_rec d  WITH(NOLOCK) ");
    sql.append("INNER JOIN IDOSGD.tdtr_estados e  WITH(NOLOCK) ON e.co_est=d.co_etiqueta_rec  ");
    sql.append("where d.co_dep_des = :pCoDep ");
    sql.append("and d.co_emp_rec=:pCoEmp ");
    sql.append("and d.co_etiqueta_rec<>0 ");
    sql.append("and d.es_doc_emi not in ('5','7','9') ");
    sql.append("and e.de_tab='CO_ETIQUETA_REC' ");
    sql.append("group by e.co_est,e.de_est,d.co_dep_des,d.co_emp_rec ");
    sql.append("order by e.co_est ");
    List<EtiquetaBandejaEnBean> list = new ArrayList<EtiquetaBandejaEnBean>();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    objectParam.put("pCoDep", coDep);
    objectParam.put("pCoEmp", coEmp);
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(EtiquetaBandejaEnBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }


@Override
public List<AvisoBandejaEntradaBean> getBandejaEntradaAltaDireccionTotal(String coDependencia, String coEmpleado) {
	// TODO Auto-generated method stub
	return null;
}
}
