package org.example.config;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.example.pojo.Configuration;
import org.example.pojo.MappedStatement;

import java.io.InputStream;
import java.util.List;

public class XMLMapperBuilder {
    private final Configuration configuration;

    public XMLMapperBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 解析mapper.xml(封装MappedStatement对象)
     * 封装好的MappedStatement对象存到Configuration的map中
     */
    public void parse(InputStream resourceAsSteam) throws DocumentException {
        Document document = new SAXReader().read(resourceAsSteam);
        Element rootElement = document.getRootElement();
        String namespace = rootElement.attributeValue("namespace");
        List<Element> selectList = rootElement.selectNodes("//select");
        for (Element element : selectList) {
            String id = element.attributeValue("id");
            String resultType = element.attributeValue("resultType");
            String parameterType = element.attributeValue("parameterType");
            String sql = element.getTextTrim();

            //封装MappedStatement
            String statementId = namespace + "." + id;
            MappedStatement mappedStatement = new MappedStatement();
            mappedStatement.setStatementId(statementId);
            mappedStatement.setResultType(resultType);
            mappedStatement.setParameterType(parameterType);
            mappedStatement.setSqlText(sql);
            mappedStatement.setSqlCommandType("select");

            //存储到Configuration的map集合中
            configuration.getMappedStatementMap().put(statementId, mappedStatement);
        }
    }
}
