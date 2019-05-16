package com.atson.commons.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * null�֎~�̃p�����[�^�ɂ���A�m�e�[�V����<br>
 * ����ł��o���炻��ɏ�芷����
 * @author wy8h-hsmt
 *
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={PARAMETER, METHOD, LOCAL_VARIABLE, FIELD})
public @interface NonNull {
    // marker annotation
}
