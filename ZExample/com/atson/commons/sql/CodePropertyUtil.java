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
 * codeプロパティー関連ツール
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
        // が本来したいことだが毎回mapを作って捨ててしまう
        // だからといって E の実装から CodePropertyUtilのインスタンスを見つけるのも大変
        // なので、ここで探す

        for (E val : cls.getEnumConstants()) {
            if (StringUtils.equals(code, val.getCode())) {
                return val;
            }
        }
        throw new IllegalArgumentException("no value for code:" + code);
    }

    /**
     * 
     * リフレクション用<br>
     * ClassオブジェクトがEnum<?> & CodePropertyとわかっていても
     * (Class<Enum<?> & CodeProperty>) clazz とかけないのだ
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
     * コード文字列から、対応するCodePropertyインスタンスを取得する。
     * 
     * @param code コード文字列
     * @return コード文字列に対応するCodePropertyインスタンス
     * @throws IllegalArgumentException コードに対応するCodePropertyインスタンスがない場合
     */
    @NonNull
    public final E valueOfCode(final String code) {
        // ２回作ってもいいやで synchronized なし
        if (this.map == null) {
            this.map = asMap0(this.values);
        }

        return valueOfCode0(this.map, code);
    }

    /**
     * コード文字列から、対応するCodePropertyインスタンスを取得する。<br>
     * 対応するCodePropertyインスタンスがない場合、nullを返す。
     * 
     * @param code コード文字列
     * @return コード文字列に対応するCodePropertyインスタンス
     */
    @Nullable
    public final E valueOfCodeOrNull(final String code) {
        // ２回作ってもいいやで synchronized なし
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
     * codeがvaluesのいずれかのcode値ならばtrueを返す
     * @param code
     * @return codeがvaluesのいずれかのcode値ならばtrue
     */
    public final boolean isCode(final String code) {
        return valueOfCodeOrNull(code) != null;
    }
    
    /**
     * nullチェックをしてからgetCode()を返す
     * @param codeProperty
     * @return codePropertyがnullの場合null, そうでない場合codeProperty.getCode()
     */
    public static String getCodeOrNull(final CodeProperty codeProperty) {
        if(codeProperty == null) {
            return null;
        }
        return codeProperty.getCode();
    }
    
    /**
     * nullチェックをしてからgetCode()を返す。nullの場合空文字を返す
     * @param codeProperty
     * @return codePropertyがnullの場合空文字, そうでない場合codeProperty.getCode()
     */
    public static String getCodeOrEmpty(final CodeProperty codeProperty) {
        return getCodeOrDefault(codeProperty, "");
    }
    
    /**
     * nullチェックをしてからgetCode()を返す。nullの場合defaultValueを返す
     * @param codeProperty
     * @param defaultValue
     * @return codePropertyがnullの場合default, そうでない場合codeProperty.getCode()
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
     * valueOfCodeのnullを許す版
     * @param map
     * @param code
     * @return mapからcodeをもつEを検索する。<br>
     * なければnull
     */
    @Deprecated
    public static <E extends CodeProperty> E valueOfCodeOrNull(final Map<String, E> map,
            final String code) {
        return map.get(code);
    }
    
    /**
     * @param cps nullでない かつ nullを含まないCodePropertyの配列
     * @return cpsの要素のコードの列をリストで返す
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
     * @param cps nullでない かつ nullを含まないCodePropertyのリスト
     * @return cpsの要素のコードの列をリストで返す
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
     * codeがeのコード値と等しい場合にtrueを返す。<br>
     * 
     * codeEqualsOr だとvalueを書き忘れる可能性があるため、1つならこちらがおすすめ
     * @param code コード値
     * @param e nullでないCodePropertyインスタンス
     * @throws NullPointerException eがnullの場合
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
     * code がvaluesのいずれかのgetCode()と等しい場合にtrueを返す
     * シグネチャ(String code, CodeProperty... values) だと違うCodeProperty実装型を混在させそうなのでgenericsにした
     * 違うEnumの型が混在していた場合 こんな警告が出るので気づくはず ↓
     * 
     * Type safety : A generic array of Enum<?>&CodeProperty is created for a varargs parameter
     * </pre>
     * @param code
     * @param values nullを含まないCodePropertyの列
     * @throws NullPointerException valuesがnull またはvaluesがnullを含む場合
     */
    public static <E extends CodeProperty> boolean codeEqualsOr(final String code, final E... values) {
        if(values == null) {
            throw new NullPointerException("values must not be null");
        }
        if(Arrays2.contains(values, null)) {
            throw new NullPointerException("values must not contain null");
        }
            
        for (E e : values) {
            // codeはnullもありうる
            if(code == null && e.getCode() == null ||
                    code != null && code.equals(e.getCode())) {
                return true;
            }
        }
        return false;
    }
}
