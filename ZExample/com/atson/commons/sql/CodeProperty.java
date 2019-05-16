package com.atson.commons.sql;

/**
 * code�v���p�e�B������
 * {@code Enum<E>} value �� code������ �Ƃ̑��ݕϊ��Ɏg���Ă���
 * 
 * �g�p��:
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
