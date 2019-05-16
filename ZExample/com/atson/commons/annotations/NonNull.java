package com.atson.commons.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * null禁止のパラメータにつけるアノテーション<br>
 * 決定版が出たらそれに乗り換える
 * @author wy8h-hsmt
 *
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={PARAMETER, METHOD, LOCAL_VARIABLE, FIELD})
public @interface NonNull {
    // marker annotation
}
