package com.atson.commons.sql;

import static com.atson.commons.sql.Data.just;
import static com.atson.commons.sql.Data.nothing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Month;

import com.atson.commons.lang.FP;
import com.atson.commons.lang.FP.EConsumer;
import com.atson.commons.lang.FP.EFun;
import com.atson.commons.lang.FP.Fun;
import com.atson.commons.lang.FP.T2;
import com.atson.commons.lang.FP.T3;
import com.atson.commons.lang.FP.T4;
import com.atson.commons.lang.FP.Unit;
import com.atson.commons.sql.Accessors.Composer;
import com.atson.commons.sql.Accessors.OneColumnComposer;
import com.atson.commons.sql.Data.Maybe;

/**
 * JDBCアクセスを関数的に行うためのユーティリティ
 * @author hibi
 * @author wy8h-hsmt
 *
 */
final public class JdbcFP {
    
    private JdbcFP() {FP.noInstance();}

    public static final Class<Array>      arrayT      = Array.class;
    public static final Class<BigDecimal> bigDecimalT = BigDecimal.class;
    public static final Class<Blob>       blobT       = Blob.class;
    public static final Class<Boolean>    booleanT    = Boolean.class;
    public static final Class<Byte>       byteT       = Byte.class;
    public static final Class<byte[]>     bytesT      = byte[].class;
    public static final Class<Date>       dateT       = Date.class;
    public static final Class<Double>     doubleT     = Double.class;
    public static final Class<Float>      floatT      = Float.class;
    public static final Class<Integer>    integerT    = Integer.class;
    public static final Class<Long>       longT       = Long.class;
    public static final Class<Short>      shortT      = Short.class;
    public static final Class<String>     stringT     = String.class;
    public static final Class<Time>       timeT       = Time.class;
    public static final Class<Timestamp>  timestampT  = Timestamp.class;

    public static final Class<Number>     numberT     = Number.class;

    // そもそもSQLにはまともな型検査がないことを逆手に取った実装
    public static <T> FP.Fun<ResultSet, FP.Fun<Integer, T>> get(
            final Class<T> clsT) {
        return new FP.Fun<ResultSet,FP.Fun<Integer,T>>() {
            @Override
            public FP.Fun<Integer,T> app(final ResultSet rs) {
                return new FP.Fun<Integer,T>() {
                    @Override
                    public T app(final Integer idx) {
                        Object obj;
                        try {
                            obj = rs.getObject(idx);
                        } catch (SQLException e) {
                            throw new SQLRuntimeException(e);
                        }

                        if (clsT.isInstance(obj)) {
                            return this.unsafeCastT(obj);
                        }

                        if (obj == null) {
                            return null;
                        }

                        String err = String.format
                            ("at index: %d, src: %s, dst: %s, src data: '%s'",
                             idx,
                             obj.getClass(),
                             clsT.getName(),
                             "" + obj);

                        throw new SQLRuntimeException
                            (new SQLException
                             ("SQLからJavaへの型変換に失敗: " + err));
                    }

                    @SuppressWarnings("unchecked")
                    private T unsafeCastT(final Object o) {
                        return (T)o;
                    }
                };
            }
        };
    }

    public static <T>
        FP.Fun<Integer,T> get(final Class<T> clsT, final ResultSet rs) {
        return JdbcFP.<T>get(clsT).app(rs);
    }


    /**
     * ResultSetの現在のカーソルから、idxの1列を取得して、clsTが表す型にキャストして返す。
     * @param clsT キャストしたい型を表すクラス
     * @param ResultSet データを取得するResultSet
     * @param idx データを取得する列番号
     * @return ResultSetのidx列目のデータをclsTでキャストしたものを返す。<br>
     * ResultSetのidx列目のデータがnullの場合、nullを返す。
     *
     * @throws SQLRuntimeException ResultSetのidx列目がclsTに適合しなかった場合
     */
    public static <T> T get(final Class<T> clsT, final ResultSet rs,
            final Integer idx) {
        return JdbcFP.<T> get(clsT, rs).app(idx);
    }

    public static <A,X>
        Accessors.Creator<T2<A,X>>
        firstCreator(final Class<A> clsA, final Accessors.Creator<X> xc) {
        return tCreator(composer(clsA), xc);
    }

    public static <A,X>
        Accessors.Composer<T2<A,X>>
        firstComposer(final Class<A> clsA, final Accessors.Composer<X> xc) {
        return tComposer(composer(clsA), xc);
    }

    public static <X,A>
        Accessors.Composer<T2<X,A>>
        secondComposer(final Accessors.Composer<X> xc, final Class<A> clsA) {
        return tComposer(xc, composer(clsA));
    }

    // dataReader(secondComposer(t3Composer(clsA, clsB, clsC),
    //                           composer(clsD)));
    // dataReader(tComposer(tComposer(clsA, clsB),
    //                      tComposer(clsC, clsD)));

    public static <X,Y>
        Accessors.Creator<T2<X,Y>>
        tCreator(final Accessors.Composer<X> xc,
                 final Accessors.Creator<Y> yc) {
        return new Accessors.Creator<T2<X,Y>>() {
            @Override
            public T2<X,Y>
                create(final ResultSet rs, final int idx) throws SQLException {
                return T2.of(xc.create(rs, idx),
                             yc.create(rs, idx + xc.width()));
            }
        };
    }

    public static <X,Y>
        Accessors.Composer<T2<X,Y>>
        tComposer(final Accessors.Composer<X> xc,
                  final Accessors.Composer<Y> yc) {
        final int newWidth = xc.width() + yc.width();
        return new Accessors.Composer<T2<X,Y>>() {
            @Override
            public T2<X,Y>
                create(final ResultSet rs, final int idx) throws SQLException {
                return T2.of(xc.create(rs, idx),
                             yc.create(rs, idx + xc.width()));
            }

            @Override
            public int width() {
                return newWidth;
            }
        };
    }


    public static <X,Y,Z>
        Accessors.Composer<T3<X,Y,Z>>
        t3Composer(final Accessors.Composer<X> xc,
                  final Accessors.Composer<Y> yc,
                  final Accessors.Composer<Z> zc) {
        final int newWidth = xc.width() + yc.width() + zc.width();
        return new Accessors.Composer<T3<X,Y,Z>>() {
            @Override
            public T3<X,Y,Z>
                create(final ResultSet rs, final int idx) throws SQLException {
                return T3.of(xc.create(rs, idx),
                             yc.create(rs, idx + xc.width()),
                             zc.create(rs, idx + xc.width() + yc.width()));
            }

            @Override
            public int width() {
                return newWidth;
            }
        };
    }
    
    public static <X, Y, Z, W> Accessors.Composer<T4<X, Y, Z, W>> t4Composer(
            final Accessors.Composer<X> xc, final Accessors.Composer<Y> yc,
            final Accessors.Composer<Z> zc, final Accessors.Composer<W> wc) {
        final int newWidth = xc.width() + yc.width() + zc.width() + wc.width();
        return new Accessors.Composer<T4<X, Y, Z, W>>() {
            @Override
            public T4<X, Y, Z, W> create(final ResultSet rs, final int idx)
                    throws SQLException {
                return T4.of(
                        xc.create(rs, idx),
                        yc.create(rs, idx + xc.width()),
                        zc.create(rs, idx + xc.width() + yc.width()),
                        wc.create(rs,
                                idx + xc.width() + yc.width() + zc.width()));
            }

            @Override
            public int width() {
                return newWidth;
            }
        };
    }
    
    /**
     * composer.width()の幅の全列がnullの場合nothingを返し、
     * そうでない場合just(composer.create(rs))を返すcomposer<br>
     * OUTER JOINに使える
     * 
     * @param composer
     * @return 新しいcomposer
     */
    public static <T> Composer<Maybe<T>> nullToNothingComposer(final Composer<T> composer) {
        return new Composer<Maybe<T>>() {
            @Override
            public Maybe<T> create(final ResultSet rs, final int startColumn)
                    throws SQLException {
                boolean nonNullExist = false;
                final int bound = startColumn + this.width();
                for (int i = startColumn; i < bound; i++) {
                    if(rs.getObject(i) != null) {
                        nonNullExist = true;
                        break;
                    }
                }
                if(nonNullExist) {
                    return just(composer.create(rs, startColumn));
                }
                return nothing();
            }

            @Override
            public int width() {
                return composer.width();
            }
        };
    }

    public static final int RESULT_SET_BEGIN_INDEX = 1;

    public static class ResultSetReader<T> implements Read.RecordReader<T> {
        private final Accessors.Creator<T> creator;
        protected final ResultSet rs;
        private final int index;
        protected ResultSetReader
            (final Accessors.Creator<T> creator, final int idx, final ResultSet rs) {
            this.creator = creator;
            this.rs = rs;
            this.index = idx;
        }
        protected ResultSetReader
            (final Accessors.Creator<T> creator, final ResultSet rs) {
            this(creator, RESULT_SET_BEGIN_INDEX, rs);
        }

        @Override
        public Maybe<T> read() {
            try {
                if (!this.rs.next()) {
                    return nothing();
                }

                T data = this.creator.create(this.rs, this.index);
                return just(data);
            } catch (SQLException e) {
                throw new SQLRuntimeException(e);
            }
        }

        @Override
        public void close() {}
    }


    public static <T>
        DataReader<T>
        dataReader(final Accessors.Creator<T> tc, final PreparedStatement sel) {
        return Accessors.toReader(tc, sel);
    }

    /**
     * QueryのもつSQLにQueryが定義するバインドを実行し、SQLを発行する。<br>
     * 結果をDataReaderでラップして取得する。<br>
     *
     * 現在、DataReaderのリソース管理は煩雑なので推奨されていない。<br>
     * {@link SessionSQL.SelectReaderSession}の利用を推奨
     *
     * @param conn
     * @param q
     * @return DataReaderで包んだクエリ結果
     * @throws SQLException
     */
    public static <T> DataReader<T> dataReader(final Connection conn, final Query<T> q)
            throws SQLException {
        PreparedStatement sel = null;
        try {
            sel = conn.prepareStatement(q.getSql().unSql());
            q.bind(sel);
            return Accessors.toReaderAux(q.getCreator(), sel);
        } catch (SQLException e) {
            closePreparedStatement(sel);
            throw e;
        } catch (RuntimeException e) {
            closePreparedStatement(sel);
            throw e;
        } catch (Error e) {
            closePreparedStatement(sel);
            throw e;
        }
    }

    private static void closePreparedStatement(final PreparedStatement stmt) {
        if(stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                // ignore Exception at close
            }
        }
    }
    
    public static <A> Accessors.Creator<A> creator(final Class<A> clsA) {
        return new Accessors.Creator<A>() {
            @Override
            public A
                create(final ResultSet rs, final int idx) throws SQLException {
                return JdbcFP.<A>get(clsA, rs, idx);
            }
        };
    }

    /**
     * ResultSetからA型の値をcreator.createで取り出し、funでB型に変換するcreatorを返す。
     * @param creator A型の値を取り出すcreator null禁止
     * @param fun データを変換する関数 null禁止
     * @return ResultSetからA型の値をcompA.createで取り出し、funでB型に変換するcreator
     */
    public static <A, B> Accessors.Creator<B> creator(final Accessors.Creator<A> creator, final Fun<? super A, ? extends B> fun) {
        return new Accessors.Creator<B>() {
            @Override
            public B create(final ResultSet rs, final int idx)
                    throws SQLException {
                return fun.app(creator.create(rs, idx));
            }
        };
    }
    
    public static <A,B>
        Accessors.Creator<T2<A,B>>
        tCreator(final Class<A> clsA, final Class<B> clsB) {
        return firstCreator(clsA, JdbcFP.<B>creator(clsB));
    }

    public static <A,B,C>
        Accessors.Creator<T2<A,T2<B,C>>>
        tCreator(final Class<A> clsA,
                 final Class<B> clsB,
                 final Class<C> clsC) {
        return firstCreator(clsA, tCreator(clsB, clsC));
    }

    public static <A,B,C,D>
        Accessors.Creator<T2<A,T2<B,T2<C,D>>>>
        tCreator(final Class<A> clsA,
                 final Class<B> clsB,
                 final Class<C> clsC,
                 final Class<D> clsD) {
        return firstCreator(clsA, tCreator(clsB, clsC, clsD));
    }

    public static <A,B,C,D,E>
        Accessors.Creator<T2<A,T2<B,T2<C,T2<D,E>>>>>
        tCreator(final Class<A> clsA,
                 final Class<B> clsB,
                 final Class<C> clsC,
                 final Class<D> clsD,
                 final Class<E> clsE) {
        return firstCreator(clsA, tCreator(clsB, clsC, clsD, clsE));
    }

    public static <A,B,C,D,E,F>
        Accessors.Creator<T2<A,T2<B,T2<C,T2<D,T2<E,F>>>>>>
        tCreator(final Class<A> clsA,
                 final Class<B> clsB,
                 final Class<C> clsC,
                 final Class<D> clsD,
                 final Class<E> clsE,
                 final Class<F> clsF) {
        return firstCreator(clsA, tCreator(clsB, clsC, clsD, clsE, clsF));
    }

    public static <A,B,C,D,E,F,G>
        Accessors.Creator<T2<A,T2<B,T2<C,T2<D,T2<E,T2<F,G>>>>>>>
        tCreator(final Class<A> clsA,
                 final Class<B> clsB,
                 final Class<C> clsC,
                 final Class<D> clsD,
                 final Class<E> clsE,
                 final Class<F> clsF,
                 final Class<G> clsG) {
        return firstCreator(clsA,
                            tCreator(clsB, clsC, clsD, clsE, clsF, clsG));
    }

    /**
     * ResultSetから1列のデータを得てclsAでキャストして取得する1列のComposerを返す。
     * @param clsA 期待されるデータの型
     */
    public static <A> Accessors.Composer<A> composer(final Class<A> clsA) {
        return new Accessors.OneColumnComposer<A>() {
            
            @Override
            public A create(final ResultSet rs, final int idx)
                    throws SQLException {
                return JdbcFP.<A> get(clsA, rs, idx);
            }
        };
    }
    
    /**
     * ResultSetから1列のNumber適合データを得てintValue()を取得する、1列のComposerを返す。<br>
     * COUNT関数の結果の列から{@link ResultSet#getObject}すると(DB2ドライバでは)LongではなくIntegerオブジェクトが返ってくるので…
     *
     * @returns 1列のComposer
     */
    public static Accessors.Composer<Integer> intValueComposer() {
        return INT_VALUE_COMPOSER;
    }

    private static final OneColumnComposer<Integer> INT_VALUE_COMPOSER = new Accessors.OneColumnComposer<Integer>() {

        @Override
        public Integer create(final ResultSet rs, final int idx)
                throws SQLException {
            Number num = JdbcFP.<Number> get(Number.class, rs, idx);
            return num == null ? null : num.intValue();
        }

    };
    
    /**
     * ResultSetから1列のString値を得てBigIntegerに変換する、1列のComposerを返す。
     *
     * @returns 1列のComposer
     */
    public static Accessors.Composer<BigInteger> bigIntegerComposer() {
        return BIGINTEGER_COMPOSER;
    }
    
    
    /**
     * ResultSetから1列のString値を得てBigIntegerに変換する、1列のComposerを返す。<br>
     * use {@link #bigIntegerComposer()}
     *
     * @returns 1列のComposer
     */
    public static final Accessors.Composer<BigInteger> BIGINTEGER_COMPOSER = new Accessors.OneColumnComposer<BigInteger>() {

        @Override
        public BigInteger create(final ResultSet rs, final int idx)
                throws SQLException {
            String str = get(String.class, rs, idx);
            return str == null ? null : new BigInteger(str);
        }

    };

    /**
     * ResultSetから1列のStringコードを得て対応するEnum値を取得する、1列のComposerを返す。<br>
     * createは対応するEnum値が取得できなかった場合、IllegalArgumentExceptionをスローする。
     *
     * @returns 1列のComposer
     */
    public static <E extends Enum<E> & CodeProperty> Accessors.Composer<E> codePropertyComposer(final Class<E> clazz) {
        return new Accessors.OneColumnComposer<E>() {
            
            @Override
            public E create(final ResultSet rs, final int idx)
                    throws SQLException {
                String code = JdbcFP.<String> get(String.class, rs, idx);
                // TODO valueOfCode(Class, String) は遅そうなのでキャッシュ？
                return CodePropertyUtil.valueOfCode(clazz, code);
            }
            
        };
    }
    
    /**
     * @return 1列をmon形式の月として解釈するComposer<br>
     * createは列がnullの場合、nullを返す。<br>
     * createは解釈に失敗した場合、実行時例外をスローする。
     * @see Month
     */
    public static Accessors.Composer<Month> monComposer() {
        return new Accessors.OneColumnComposer<Month>() {

            @Override
            public Month create(final ResultSet rs, final int startColumn)
                    throws SQLException {
                try {
                    String str = get(String.class, rs, startColumn);
                    if(str == null) {
                        return null;
                    }
                    return Month.valueOfMon(str);
                } catch (ParseException e) {
                    throw new IllegalStateException(e);
                }
            }
            
        };
    }


    /**
     * ResultSetから1列のデータを得てclsAでキャストし、funでB型に変換する1列のComposerを返す。
     * @param clsA 期待されるデータの型
     * @param fun データを変換する関数
     */
    public static <A, B> Accessors.Composer<B> composer(final Class<A> clsA, final Fun<? super A, ? extends B> fun) {
        return new Accessors.OneColumnComposer<B>() {
            @Override
            public B create(final ResultSet rs, final int idx)
                    throws SQLException {
                return fun.app(JdbcFP.<A> get(clsA, rs, idx));
            }
        };
    }
    
    /**
     * ResultSetからA型の値をcompA.createで取り出し、funでB型に変換するComposerを返す。<br>
     * 幅はcompAと同じ
     * @param compA A型の値を取り出すComposer null禁止
     * @param fun データを変換する関数 null禁止
     * @return ResultSetからA型の値をcompA.createで取り出し、funでB型に変換するComposer
     */
    public static <A, B> Accessors.Composer<B> composer(final Accessors.Composer<A> compA, final Fun<? super A, ? extends B> fun) {
        return new Accessors.Composer<B>() {
            @Override
            public B create(final ResultSet rs, final int idx)
                    throws SQLException {
                return fun.app(compA.create(rs, idx));
            }

            @Override
            public int width() { return compA.width(); }
        };
    }



    public static <A,B>
        Accessors.Composer<T2<A,B>>
        tComposer(final Class<A> clsA, final Class<B> clsB) {
        return firstComposer(clsA, JdbcFP.<B>composer(clsB));
    }

    public static <A,B,C>
        Accessors.Composer<T2<A,T2<B,C>>>
        tComposer(final Class<A> clsA,
                  final Class<B> clsB,
                  final Class<C> clsC) {
        return firstComposer(clsA, tComposer(clsB, clsC));
    }

    public static <A,B,C,D>
        Accessors.Composer<T2<A,T2<B,T2<C,D>>>>
        tComposer(final Class<A> clsA,
                  final Class<B> clsB,
                  final Class<C> clsC,
                  final Class<D> clsD) {
        return firstComposer(clsA, tComposer(clsB, clsC, clsD));
    }

    public static <A,B,C,D,E>
        Accessors.Composer<T2<A,T2<B,T2<C,T2<D,E>>>>>
        tComposer(final Class<A> clsA,
                  final Class<B> clsB,
                  final Class<C> clsC,
                  final Class<D> clsD,
                  final Class<E> clsE) {
        return firstComposer(clsA, tComposer(clsB, clsC, clsD, clsE));
    }

    public static <A,B,C,D,E,F>
        Accessors.Composer<T2<A,T2<B,T2<C,T2<D,T2<E,F>>>>>>
        tComposer(final Class<A> clsA,
                  final Class<B> clsB,
                  final Class<C> clsC,
                  final Class<D> clsD,
                  final Class<E> clsE,
                  final Class<F> clsF) {
        return firstComposer(clsA, tComposer(clsB, clsC, clsD, clsE, clsF));
    }

    public static <A,B,C,D,E,F,G>
        Accessors.Composer<T2<A,T2<B,T2<C,T2<D,T2<E,T2<F,G>>>>>>>
        tComposer(final Class<A> clsA,
                  final Class<B> clsB,
                  final Class<C> clsC,
                  final Class<D> clsD,
                  final Class<E> clsE,
                  final Class<F> clsF,
                  final Class<G> clsG) {
        return firstComposer(clsA,
                             tComposer(clsB, clsC, clsD, clsE, clsF, clsG));
    }

    public static <A,B>
        DataReader<T2<A,B>>
        tDataReader(final Class<A> clsA,
                    final Class<B> clsB,
                     final PreparedStatement sel) {
        return dataReader(tCreator(clsA, clsB), sel);
    }

    public static <A,B,C>
        Accessors.Creator<T3<A,B,C>>
        t3Creator(final Class<A> clsA,
                  final Class<B> clsB,
                  final Class<C> clsC) {
        return
            new Accessors.Creator<T3<A,B,C>>() {
                @Override
                public T3<A,B,C>
                    create(final ResultSet rs, final int idx) throws SQLException {
                    return
                        FP.t3(JdbcFP.<A>get(clsA, rs, idx),
                              JdbcFP.<B>get(clsB, rs, idx + 1),
                              JdbcFP.<C>get(clsC, rs, idx + 2));
                }
            };
    }

    /*
    public static <A,B,C>
        Accessors.Composer<T3<A,B,C>>
        t3Composer(final Class<A> clsA,
                   final Class<B> clsB,
                   final Class<C> clsC) {
        return
            new Accessors.Composer<T3<A,B,C>>() {
                public T3<A,B,C>
                    create(final ResultSet rs, final int idx) throws SQLException {
                    return
                        FP.t3(JdbcFP.<A>get(clsA, rs, idx),
                              JdbcFP.<B>get(clsB, rs, idx + 1),
                              JdbcFP.<C>get(clsC, rs, idx + 2));
                }
                public int width() { return 3; }
            };
    }
    */

    public static <A,B,C>
        DataReader<T3<A,B,C>>
        t3DataReader(final Class<A> clsA,
                     final Class<B> clsB,
                     final Class<C> clsC,
                     final PreparedStatement sel) {
        return dataReader(t3Creator(clsA, clsB, clsC), sel);
    }

    public static <A,B,C,D>
        Accessors.Creator<T4<A,B,C,D>>
        t4Creator(final Class<A> clsA,
                  final Class<B> clsB,
                  final Class<C> clsC,
                  final Class<D> clsD) {
        return new Accessors.Creator<T4<A,B,C,D>>() {
            @Override
            public T4<A,B,C,D>
                create(final ResultSet rs, final int idx) throws SQLException {
                return
                    FP.t4(JdbcFP.<A>get(clsA, rs, idx),
                          JdbcFP.<B>get(clsB, rs, idx + 1),
                          JdbcFP.<C>get(clsC, rs, idx + 2),
                          JdbcFP.<D>get(clsD, rs, idx + 3));
            }
        };
    }

    public static <A,B,C,D>
        DataReader<T4<A,B,C,D>>
        t4DataReader(final Class<A> clsA,
                     final Class<B> clsB,
                     final Class<C> clsC,
                     final Class<D> clsD,
                     final PreparedStatement sel) {
        return dataReader(t4Creator(clsA, clsB, clsC, clsD), sel);
    }

    @Deprecated
    // Maybeの使い方を誤っていた
    public static <A> Accessors.Creator<Maybe<A>> maybeCreator(
            final Class<A> clsA) {
        return new Accessors.Creator<Maybe<A>>() {
            @Override
            public Maybe<A> create(final ResultSet rs, final int idx)
                    throws SQLException {
                return Maybe.fromNullable(JdbcFP.<A> get(clsA, rs, idx));
            }
        };
    }

    @Deprecated
    public static <A> DataReader<Maybe<A>> maybeDataReader(final Class<A> clsA,
            final PreparedStatement sel) {
        return dataReader(maybeCreator(clsA), sel);
    }

    public static <A> Accessors.Creator<Maybe<A>> justCreator(
            final Class<A> clsA) {
        return new Accessors.Creator<Maybe<A>>() {
            @Override
            public Maybe<A> create(final ResultSet rs, final int idx)
                    throws SQLException {
                return just(JdbcFP.<A> get(clsA, rs, idx));
            }
        };
    }

    public static <A> DataReader<Maybe<A>> justDataReader(final Class<A> clsA,
            final PreparedStatement sel) {
        return dataReader(justCreator(clsA), sel);
    }

    public static final <T>
        FP.Fun<PreparedStatement,FP.Fun<Integer,FP.Fun<T,FP.Unit>>>
        set() {
        return new
            FP.Fun<PreparedStatement,FP.Fun<Integer,FP.Fun<T,FP.Unit>>>() {
            @Override
            public FP.Fun<Integer,FP.Fun<T,FP.Unit>>
                app(final PreparedStatement ps) {
                return new FP.Fun<Integer,FP.Fun<T,FP.Unit>>() {
                    @Override
                    public FP.Fun<T,FP.Unit> app(final Integer idx) {
                        return new FP.Fun<T,FP.Unit>() {
                            @Override
                            public FP.Unit app(final T t) {
                                try {
                                    ps.setObject(idx, t);
                                } catch (SQLException e) {
                                    throw new SQLRuntimeException(e);
                                }
                                return FP.UNIT;
                            }
                        };
                    }
                };
            }
        };
    }

    public static <T>
        FP.Fun<Integer,FP.Fun<T,FP.Unit>> set(final PreparedStatement ps) {
        return JdbcFP.<T>set().app(ps);
    }

    public static <T>
        FP.Fun<T,FP.Unit> set(final PreparedStatement ps, final Integer idx) {
        return JdbcFP.<T>set(ps).app(idx);
    }

    public static <T>
        FP.Unit set(final PreparedStatement ps, final Integer idx, final T t) {
        return JdbcFP.<T>set(ps, idx).app(t);
    }


    /**
     * SQLExceptionをスローする関数。全域Java8になったら@FunctionalInterface にする
     * @author wy8h-hsmt
     *
     * @param <A> 引数の型
     * @param <B> 結果の型
     */
    public static interface QFun<A,B> extends EFun<A, B, SQLException>{
        @Override
        public B app(A a) throws SQLException;
    }
    
    /**
     * SQLExceptionをスローする戻り値のない関数。全域Java8になったら@FunctionalInterface にする
     * @author wy8h-hsmt
     *
     * @param <A> 引数の型
     */
    public interface QConsumer<A> extends EConsumer<A, SQLException>{
        @Override
        void accept(A a) throws SQLException;
    }

    public static <A,B> FP.Fun<A,B> fun(final QFun<A,B> qf) {
        return new FP.Fun<A,B>() {
            @Override
            public B app(final A a) {
                try { return qf.app(a); }
                catch (SQLException e) { throw new SQLRuntimeException(e); }
            }
        };
    }

    public static class Sql<U> {
        private final String sql;
        protected Sql(final String sql) { this.sql = sql; }
        public String unSql() { return this.sql; }
    }

    /**
     * SQLクエリの抽象化
     *
     * <p>
     * <ol>
     * <li>SQL</li>
     * <li>SQLで得られたResultSetからレコードを作るCreator</li>
     * </ol>
     * を保持する。
     * </p>
     * <p>
     * {@link Query#bind}には、PreparedStatementにデータをバインドするコードを記述する。
     * </p>
     * <p>SQLクエリは{@link com.atson.commons.sql.SessionSQL.SelectReaderSession#with(Connection, Query, int)} で
     * 発行、処理することでDB関連リソースはクローズされる。
     * </p>
     *
     * @see SessionSQL.SelectReaderSession
     * @param <U> レコードの型
     */
    public abstract static class Query<U> {
        private final Sql<U> sql;
        public Sql<U> getSql() { return this.sql; }
        private final Accessors.Creator<U> creator;
        public Accessors.Creator<U> getCreator() { return this.creator; }

        protected Query(final String sql, final Accessors.Creator<U> creator) {
            this.sql  = new Sql<U>(sql);
            this.creator = creator;
        }

        abstract protected void bind(PreparedStatement stmt)
            throws SQLException;
    }

    /**
     * SQL文、ResultSetからレコードを取得するCreator、PreparedStatementにデータをバインドする関数から
     * Queryを作成する。
     */
    public static <U> Query<U> query
        (final String sql, final Accessors.Creator<U> creator,
         final QFun<PreparedStatement,Unit> qf) {
        return new Query<U>(sql, creator) {
            @Override
            protected void bind(final PreparedStatement stmt)
                    throws SQLException {
                qf.app(stmt);
            }
        };
    }

    /** @deprecated use {@link #noBind()} */
    @Deprecated
    public static QFun<PreparedStatement,Unit> no_bind() {
        return noBind();
    }

    /**
     * 何もしないQFun{@code<PreparedStatement,Unit>}を返す。
     */
    public static QFun<PreparedStatement,Unit> noBind() {
        return new QFun<PreparedStatement,Unit>() {
            @Override
            public Unit app(final PreparedStatement stmt) {
                return FP.UNIT;
            }
        };
    }


    /**
     * SQL文とCreatorからQueryを作成する。<br>
     * {@link Query#bind(PreparedStatement)}は何もしない
     */
    public static <U> Query<U>
            query(final String sql, final Accessors.Creator<U> creator) {
        return query(sql, creator, noBind());
    }
    
    /**
     * Queryの結果をfunで型変換する。
     */
    public static <U, T> Query<T> query(final Query<U> q,
            final Fun<? super U, ? extends T> fun) {
        return new Query<T>(q.getSql().unSql(), creator(q.creator, fun)) {

            @Override
            protected void bind(final PreparedStatement stmt)
                    throws SQLException {
                q.bind(stmt);
            }
        };
    }

}
