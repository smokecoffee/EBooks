package com.atson.commons.sql;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.atson.commons.annotations.NonNull;
import com.atson.commons.annotations.Nullable;
import com.atson.commons.collection.Arrays2;
import com.atson.commons.lang.FP;
import com.atson.commons.lang.FP.Fun;
import com.atson.commons.lang.fp.Data.Maybe;

/**
 * code�v���p�e�B�[�֘A�c�[��
 * 
 * @author af6h-sgym
 * @author wy8h-hsmt
 */
public class CodePropertyUtil<E extends CodeProperty> {
    public static <E extends CodeProperty> CodePropertyUtil<E> of(final E[] values) {
        return new CodePropertyUtil<E>(values);
    }

    public static <E extends Enum<E> & CodeProperty> CodePropertyUtil<E> of(
            final Class<E> clazz) {
        return of(clazz.getEnumConstants());
    }

    public static <E extends Enum<?> & CodeProperty> E valueOfCode(
            final Class<E> cls, final String code) {
        // return of(cls).valueOfCode(code);
        // ���{�����������Ƃ�������map������Ď̂ĂĂ��܂�
        // ������Ƃ����� E �̎������� CodePropertyUtil�̃C���X�^���X��������̂����
        // �Ȃ̂ŁA�����ŒT��

        for (E val : cls.getEnumConstants()) {
            if (StringUtils.equals(code, val.getCode())) {
                return val;
            }
        }
        throw new IllegalArgumentException("no value for code:" + code);
    }

    /**
     * 
     * ���t���N�V�����p<br>
     * Class�I�u�W�F�N�g��Enum<?> & CodeProperty�Ƃ킩���Ă��Ă�
     * (Class<Enum<?> & CodeProperty>) clazz �Ƃ����Ȃ��̂�
     * @author sugiyama
     * @author wy8h-hsmt (some comment)
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<?> & CodeProperty> Class<E> castClass(
            final Class<?> cls) {
        return (Class<E>) cls;
    }

    private final E[] values;

    private CodePropertyUtil(final E[] values) {
        this.values = values;
    }

    private Map<String, E> map;

    /**
     * �R�[�h�����񂩂�A�Ή�����CodeProperty�C���X�^���X���擾����B
     * 
     * @param code �R�[�h������
     * @return �R�[�h������ɑΉ�����CodeProperty�C���X�^���X
     * @throws IllegalArgumentException �R�[�h�ɑΉ�����CodeProperty�C���X�^���X���Ȃ��ꍇ
     */
    @NonNull
    public final E valueOfCode(final String code) {
        // �Q�����Ă�������� synchronized �Ȃ�
        if (this.map == null) {
            this.map = asMap0(this.values);
        }

        return valueOfCode0(this.map, code);
    }

    /**
     * �R�[�h�����񂩂�A�Ή�����CodeProperty�C���X�^���X���擾����B<br>
     * �Ή�����CodeProperty�C���X�^���X���Ȃ��ꍇ�Anull��Ԃ��B
     * 
     * @param code �R�[�h������
     * @return �R�[�h������ɑΉ�����CodeProperty�C���X�^���X
     */
    @Nullable
    public final E valueOfCodeOrNull(final String code) {
        // �Q�����Ă�������� synchronized �Ȃ�
        if (this.map == null) {
            this.map = asMap0(this.values);
        }

        return this.map.get(code);
    }
    
    public final E valueOfCodeOrDefault(final String code, final E defaultVal) {
        E found  = valueOfCodeOrNull(code);
        return found == null ? defaultVal : found;
    }
    
    public final Maybe<E> valueOfCodeMaybe(final String code) {
        return Maybe.fromNullable(valueOfCodeOrNull(code));
    }
    
    /**
     * code��values�̂����ꂩ��code�l�Ȃ��true��Ԃ�
     * @param code
     * @return code��values�̂����ꂩ��code�l�Ȃ��true
     */
    public final boolean isCode(final String code) {
        return valueOfCodeOrNull(code) != null;
    }
    
    /**
     * null�`�F�b�N�����Ă���getCode()��Ԃ�
     * @param codeProperty
     * @return codeProperty��null�̏ꍇnull, �����łȂ��ꍇcodeProperty.getCode()
     */
    public static String getCodeOrNull(final CodeProperty codeProperty) {
        if(codeProperty == null) {
            return null;
        }
        return codeProperty.getCode();
    }
    
    /**
     * null�`�F�b�N�����Ă���getCode()��Ԃ��Bnull�̏ꍇ�󕶎���Ԃ�
     * @param codeProperty
     * @return codeProperty��null�̏ꍇ�󕶎�, �����łȂ��ꍇcodeProperty.getCode()
     */
    public static String getCodeOrEmpty(final CodeProperty codeProperty) {
        return getCodeOrDefault(codeProperty, "");
    }
    
    /**
     * null�`�F�b�N�����Ă���getCode()��Ԃ��Bnull�̏ꍇdefaultValue��Ԃ�
     * @param codeProperty
     * @param defaultValue
     * @return codeProperty��null�̏ꍇdefault, �����łȂ��ꍇcodeProperty.getCode()
     */
    public static String getCodeOrDefault(final CodeProperty codeProperty, final String defaultValue) {
        if(codeProperty == null) {
            return defaultValue;
        }
        return codeProperty.getCode();
    }

    @Deprecated
    public static <E extends CodeProperty> Map<String, E> asMap(final E[] values) {
        return asMap0(values);
    }

    private static <E extends CodeProperty> Map<String, E> asMap0(final E[] values) {
        Map<String, E> map = new LinkedHashMap<String, E>();

        for (E val : values) {
            String code = val.getCode();
            if (map.containsKey(code)) {
                throw new AssertionError("duplicated code:" + code);
            }

            map.put(code, val);
        }
        return map;
    }

    @Deprecated
    @NonNull
    public static <E extends CodeProperty> E valueOfCode(final Map<String, E> map,
            final String code) {
        return valueOfCode0(map, code);
    }

    @NonNull
    private static <E> E valueOfCode0(final Map<String, E> map, final String code) {
        E val = map.get(code);
        if (val == null) {
            throw new IllegalArgumentException("no value for code:" + code);
        }

        return val;
    }
    
    /**
     * valueOfCode��null��������
     * @param map
     * @param code
     * @return map����code������E����������B<br>
     * �Ȃ����null
     */
    @Deprecated
    public static <E extends CodeProperty> E valueOfCodeOrNull(final Map<String, E> map,
            final String code) {
        return map.get(code);
    }
    
    /**
     * @param cps null�łȂ� ���� null���܂܂Ȃ�CodeProperty�̔z��
     * @return cps�̗v�f�̃R�[�h�̗�����X�g�ŕԂ�
     */
    public static <E extends CodeProperty> List<String> getCodes(final E... cps) {
        return FP.ArrayListJ.map(new Fun<CodeProperty, String>() {

            @Override
            public String app(final CodeProperty cp) {
                return cp.getCode();
            }

        }, cps);
    }
    
    /**
     * @param cps null�łȂ� ���� null���܂܂Ȃ�CodeProperty�̃��X�g
     * @return cps�̗v�f�̃R�[�h�̗�����X�g�ŕԂ�
     */
    public static <E extends CodeProperty> List<String> getCodes(final List<E> cps) {
        return FP.ArrayListJ.map(new Fun<CodeProperty, String>() {

            @Override
            public String app(final CodeProperty cp) {
                return cp.getCode();
            }

        }, cps);
    }
    
    /**
     * code��e�̃R�[�h�l�Ɠ������ꍇ��true��Ԃ��B<br>
     * 
     * codeEqualsOr ����value�������Y���\�������邽�߁A1�Ȃ炱���炪��������
     * @param code �R�[�h�l
     * @param e null�łȂ�CodeProperty�C���X�^���X
     * @throws NullPointerException e��null�̏ꍇ
     */
    public static <E extends CodeProperty> boolean codeEquals(final String code, final E e) {
        if(e == null) {
            throw new NullPointerException("enum value must not be null");
        }
        if(code == null && e.getCode() == null ||
                code != null && code.equals(e.getCode())) {
            return true;
        }
        return false;
    }
    
    /**
     * <pre>
     * code ��values�̂����ꂩ��getCode()�Ɠ������ꍇ��true��Ԃ�
     * �V�O�l�`��(String code, CodeProperty... values) ���ƈႤCodeProperty�����^�����݂��������Ȃ̂�generics�ɂ���
     * �ႤEnum�̌^�����݂��Ă����ꍇ ����Ȍx�����o��̂ŋC�Â��͂� ��
     * 
     * Type safety : A generic array of Enum<?>&CodeProperty is created for a varargs parameter
     * </pre>
     * @param code
     * @param values null���܂܂Ȃ�CodeProperty�̗�
     * @throws NullPointerException values��null �܂���values��null���܂ޏꍇ
     */
    public static <E extends CodeProperty> boolean codeEqualsOr(final String code, final E... values) {
        if(values == null) {
            throw new NullPointerException("values must not be null");
        }
        if(Arrays2.contains(values, null)) {
            throw new NullPointerException("values must not contain null");
        }
            
        for (E e : values) {
            // code��null�����肤��
            if(code == null && e.getCode() == null ||
                    code != null && code.equals(e.getCode())) {
                return true;
            }
        }
        return false;
    }
}
