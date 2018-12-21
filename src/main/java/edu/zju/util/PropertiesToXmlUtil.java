package edu.zju.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import com.fragstealers.log4j.jaxb.JaxbLog4JPropertiesConverter;

public class PropertiesToXmlUtil {
	private static JaxbLog4JPropertiesConverter converter = new JaxbLog4JPropertiesConverter();
	public static String getXml(String filepath)
	{
		StringWriter writer = new StringWriter();
        Properties log4jProperties = new Properties();
        File f=new File(filepath);
        try {
			log4jProperties.load(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        converter.toXml(log4jProperties, writer);
        return writer.toString();
	}
	public static void main(String[] args)
	{
		System.out.println(getXml("D:/Document/OSS/Activemq-log4j.properties"));
	}
}
