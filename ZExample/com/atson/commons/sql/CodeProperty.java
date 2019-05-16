package com.atson.commons.sql;

/**
 * codeプロパティを持つ
 * {@code Enum<E>} value と code文字列 との相互変換に使っている
 * 
 * 使用例:
 * 
 * <pre>
 *   public enum Xxxx implements CodeProperty {
 *    :
 *    public String getCode() {
 *       return this.code;
 *    }
 *    :
 *    private static {@code <Xxxx>} UTIL = CodeEnumUtil.of(Xxxx.class);
 * 
 *    public static Xxxx valueOfCode(final String code) {
 *        return UTIL.valueOfCode(code);
 *    }
 *  }
 * </pre>
 * 
 * 
 * @author af6h-sgym, wy8h-hsmt
 * 
 */
public interface CodeProperty
    extends CodeProperty2<String> {
    String getCode();
}
