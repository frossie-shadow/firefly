/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.server.query;

import edu.caltech.ipac.firefly.server.util.Logger;
import edu.caltech.ipac.firefly.util.DataSetParser;
import edu.caltech.ipac.util.DataGroup;
import edu.caltech.ipac.util.DataType;
import edu.caltech.ipac.util.StringUtils;
import edu.caltech.ipac.util.cache.Cache;
import edu.caltech.ipac.util.cache.CacheKey;
import edu.caltech.ipac.util.cache.CacheManager;
import edu.caltech.ipac.util.cache.StringKey;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: Nov 16, 2009
 *
 * @author loi
 * @version $Id: TemplateGenerator.java,v 1.7 2011/10/11 15:40:40 xiuqin Exp $
 */
public class TemplateGenerator {


    public static enum Tag {LABEL_TAG(DataSetParser.LABEL_TAG),
                      VISI_TAG(DataSetParser.VISI_TAG),
                      DESC_TAG(DataSetParser.DESC_TAG),
                      ITEMS_TAG(DataSetParser.ITEMS_TAG),
                      UNIT_TAG(DataSetParser.UNIT_TAG);
        public static final String VISI_SHOW = DataSetParser.VISI_SHOW;
        public static final String VISI_HIDE = DataSetParser.VISI_HIDE;
        public static final String VISI_HIDDEN = DataSetParser.VISI_HIDDEN;
        String name;
        Tag(String name) { this.name = name;}

        public String getName() {
            return name;
        }
        public String generateKey(String col) {
            return getName().replaceFirst("@", col);
        }
    }

    private static final Logger.LoggerImpl LOGGER = Logger.getLogger();

    private static final Map<String, String[]> enumColValues =  loadEnumColValues();


    public static DataGroup generate(String templateName, String querySql, DataSource dataSource) {

        if (StringUtils.isEmpty(templateName)) {
            return null;
        }
        try {
            CacheKey cacheKey = new StringKey("TemplateGenerator", templateName);
            Cache cache = CacheManager.getCache(Cache.TYPE_PERM_SMALL);
            DataGroup template = (DataGroup) cache.get(cacheKey);
            if (template == null) {
                template = loadTemplate(templateName, dataSource);
                setupFormat(template, querySql, dataSource);
                cache.put(cacheKey, template);
            }
            return template;
        } catch (Exception e) {
            LOGGER.warn(e, "Unable to generate template for query:" + templateName);
        }
        return null;
    }

    public static String createAttributeKey(Tag tag, String col) {
        return tag.getName().replaceFirst("@", col);
    }


    private static void setupFormat(DataGroup template, String querySql, DataSource dataSource) throws SQLException {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = dataSource.getConnection();
                ps = conn.prepareStatement(querySql);
                ResultSetMetaData meta = ps.getMetaData();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    String cname = meta.getColumnName(i);
                    DataType dt = template.getDataDefintion(cname);
                    if (dt != null) {

                        Class colClass = String.class;
                        try {
                            colClass = Class.forName(meta.getColumnClassName(i));
                        } catch (ClassNotFoundException ex) {
                        }
                        dt.setDataType(colClass);

                        int cwidth = meta.getColumnDisplaySize(i);
                        cwidth = Math.max(cwidth, 6);
                        cwidth = Math.max(cwidth, cname.length());

                        // create format info
                        if (!dt.hasFormatInfo()) {
                            DataType.FormatInfo fInfo = dt.getFormatInfo();
                            String format = null;
                            if (colClass == Float.class || colClass == Double.class) {
                                int scale = Math.max(meta.getScale(i), 6);
                                int prec = Math.max(meta.getPrecision(i), cwidth);
                                format = "%" + prec + "." + scale + "f"; // double or float
                            } else if (Date.class.isAssignableFrom(colClass)) {
                                format = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS"; // date
                            }
                            if (format != null) {
                                fInfo.setDataFormat(format);
                            }
                        }

                        dt.getFormatInfo().setWidth(cwidth);

                    }
                }
            } finally {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    DataSourceUtils.releaseConnection(conn, dataSource);
                }
            }
    }

    private static DataGroup loadTemplate(String templateName, DataSource dataSource) {

        String sql = "select name, display_name, description, sel, format  from " + templateName + " order by cntr asc";
        SimpleJdbcTemplate jdbc = new SimpleJdbcTemplate(dataSource);
        List<DataType> headers = jdbc.query(sql, new ParameterizedRowMapper<DataType>() {
            public DataType mapRow(ResultSet rs, int i) throws SQLException {
                String name = rs.getString("name");
                String label = rs.getString("display_name");
                String desc = rs.getString("description");
                String sel = rs.getString("sel");
                String format = rs.getString("format");

                if (StringUtils.isEmpty(name)) {
                    return null;
                }

                label = StringUtils.isEmpty(label) ? name : label;
                desc = StringUtils.isEmpty(desc) ? label : desc;

                // temporarily using Importance to indicate hidden status
                // HIGH is not hidden, MEDIUM is hidden, LOW is not visible(data transfer only)
                DataType.Importance isVisible = DataType.Importance.LOW;
                if (!StringUtils.isEmpty(sel)) {
                    if (sel.equals("y")) {
                        isVisible = DataType.Importance.HIGH;
                    } else if (sel.equals("n")) {
                        isVisible = DataType.Importance.MEDIUM;
                    }
                }


                DataType dt = new DataType(name, label, String.class, isVisible);
                dt.setShortDesc(desc);
                if (!StringUtils.isEmpty(format)) {
                    dt.getFormatInfo().setDataFormat(format);
                }

                return dt;
            }
        });

        DataGroup template = new DataGroup(templateName, headers);
        for (DataType dt : headers) {
            String visi = Tag.VISI_HIDDEN;
            if (dt.getImportance() == DataType.Importance.HIGH) {
                visi = Tag.VISI_SHOW;
            } else if (dt.getImportance() == DataType.Importance.MEDIUM) {
                visi = Tag.VISI_HIDE;
            }

            template.addAttribute(createAttributeKey(Tag.VISI_TAG, dt.getKeyName()), visi);

            if (!StringUtils.isEmpty(dt.getShortDesc())) {
                template.addAttribute(createAttributeKey(Tag.DESC_TAG, dt.getKeyName()), dt.getShortDesc());
            }

            dt.setImportance(DataType.Importance.HIGH);
            template.addAttribute(createAttributeKey(Tag.LABEL_TAG, dt.getKeyName()), dt.getDefaultTitle());

            if ( enumColValues.containsKey(dt.getKeyName()) ) {
                template.addAttribute(createAttributeKey(Tag.ITEMS_TAG, dt.getKeyName()),
                        StringUtils.toString(enumColValues.get(dt.getKeyName()), ","));
            }

            if (dt.hasFormatInfo()) {
                String fi = dt.getFormatInfo().getDataFormatStr();
                if (fi.equals("RA") || fi.equals("DEC")) {
                    dt.setFormatInfo(null);
                    template.addAttribute(createAttributeKey(Tag.UNIT_TAG, dt.getKeyName()), fi);
                }
            }

        }
        return template;
    }

    private static Map<String, String[]> loadEnumColValues() {
        HashMap<String, String[]> map = new HashMap<String, String[]>();

        if (true) return map;

        // remove default hard-coded enum types from SHA
        map.put("wavelength",  new String[] {"IRAC 3.6um", "IRAC 4.5um", "IRAC 5.8um", "IRAC 8.0um",
                                 "IRS LH 18.7-37.2um", "IRS LL 14.0-21.7um", "IRS LL 14.0-38.0um", "IRS LL 19.5-38.0um",
                                 "IRS PU Blue 13.3-18.7um", "IRS PU Red 18.5-26.0um", 
                                 "IRS SH 9.9-19.6um", "IRS SL 5.2-14.5um", "IRS SL 5.2-8.7um", "IRS SL 7.4-14.5um",
                                 "MIPS 24um", "MIPS 70um", "MIPS 160um"}
                );
        map.put("modedisplayname", new String[] {"IRAC Map", "IRAC Map PC", "IRS Map", "IRS Stare", "IRS Peakup Image",
                         "MIPS Phot", "MIPS SED", "MIPS Scan", "MIPS TP", "IRAC IER","IRAC Post-Cryo IER", "IRS IER","MIPS IER"}
                );
        map.put("filetype", new String[] {"Image", "Table"});

        return map;
    }
}
