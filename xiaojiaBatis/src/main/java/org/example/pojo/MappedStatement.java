package org.example.pojo;

/**
 * 映射配置类:存放mapper.xml解析出来的内容
 *
 * <select id="selectOne" resultType="org.example.pojo.User" parameterType="org.example.pojo.User">
 *     select * from user where id = #{id} and username = #{username}
 * </select>
 *
 * 注意:mapper.xml中每一个select|update|delete|insert标签都会对应一个MappedStatement
 */
public class MappedStatement {

    // 唯一标识:namespace.id
    private String statementId;

    // 返回结果类型
    private String resultType;

    // 参数类型
    private String parameterType;

    // sql语句
    private String sqlText;

    // 标签的类型
    private String sqlCommandType;

    public String getStatementId() {
        return statementId;
    }

    public void setStatementId(String statementId) {
        this.statementId = statementId;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getSqlText() {
        return sqlText;
    }

    public void setSqlText(String sqlText) {
        this.sqlText = sqlText;
    }

    public String getSqlCommandType() {
        return sqlCommandType;
    }

    public void setSqlCommandType(String sqlCommandType) {
        this.sqlCommandType = sqlCommandType;
    }
}
