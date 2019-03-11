package edu.zju.wala;

import java.util.*;

public class Constants {
    public static final Map<String, String> LoggerFunctions = new HashMap<String, String>() {
        // @CommonsLog: org.apache.commons.logging.LogFactory.getLog(LogExample.class)
        // @JBossLog: org.jboss.logging.Logger.getLogger(LogExample.class);
        // @JUL: java.util.logging.Logger.getLogger(LogExample.class.getName());
        // @Log4j: org.apache.log4j.Logger.getLogger(LogExample.class);
        // @Log4j2: org.apache.logging.log4j.LogManager.getLogger(LogExample.class);
        // @Slf4j: org.slf4j.LoggerFactory.getLogger(LogExample.class);
        // @TDDL: com.taobao.tddl.common.utils.logger.LoggerFactory.getLogger(LogExample.class);
        // @Jingwei: com.alibaba.middleware.jingwei.common.logger.LoggerFactory.getLogger(LogExample.class)
        // @Middleware: com.taobao.middleware.logger.LoggerFactory.getLogger(LogExample.class)
        // @InnerLog: com.alibaba.middleware.innerlog.LoggerFactory.getLogger(LogExample.class)
        // @Ibatis: com.ibatis.common.logging.LogFactory.getLog(LogExample.class)
        {
            put("Lorg/apache/commons/logging/LogFactory.getLog", "CommonsLog");
            put("Lorg/jboss/logging/Logger.getLogger", "JBossLog");
            put("Ljava/util/logging/Logger.getLogger", "JUL");
            put("Lorg/apache/log4j/Logger.getLogger", "Log4j");
            put("Lorg/apache/logging/log4j/LogManager.getLogger", "Log4j2");
            put("Lorg/slf4j/LoggerFactory.getLogger", "Slf4j");
            put("Lcom/taobao/tddl/common/utils/logger/LoggerFactory.getLogger", "TDDL");
            put("Lcom/alibaba/middleware/jingwei/common/logger/LogFactory.getLogger", "Jingwei");
            put("Lcom/alibaba/middleware/jingwei/common/logger/JwLoggerFactoryV3.getLogger", "Jingwei");
            put("Lcom/alibaba/middleware/innerlog/LoggerFactory.getLogger", "InnerLog");
            put("Lcom/taobao/middleware/logger/LoggerFactory.getLogger", "Middleware");
            put("Lcom/ibatis/common/logging/LogFactory.getLog", "Ibatis");
            put("Lcom/alibaba/common/logging/LoggerFactory.getLogger", "AliCommon");
            put("Lcom/taobao/tradespi/utils/Logger.create", "TradeSPI");
            put("Lorg/eclipse/jetty/util/log/Log.getLogger", "Jetty");
            put("Lorg/mortbay/log/Logger.getLogger", "Jetty");
            put("Lorg/jboss/netty/logging/InternalLoggerFactory.getInstance", "Netty");
            put("Lorg/datanucleus/util/NucleusLogger.getLoggerInstance", "Nucleus");
            put("Lorg/jpox/util/JPOXLogger.getLoggerInstance", "JPOX");
            put("Lcom/taobao/mitem/common/log/LoggerFactory.getLogger", "MIC");
        }
    };

    public static List<String> FilteredPackage = new ArrayList<String>(
            Arrays.asList("Lsun/swing", "Ljava/swing", "Ljavax/swing", "Lcom/sun/swing",
                    "Lsun/awt", "Ljava/awt", "Lsun/applet", "Ljava/applet", "Lcom/sun/java/swing",
                    "Lorg/codehaus/groovy"));

}
