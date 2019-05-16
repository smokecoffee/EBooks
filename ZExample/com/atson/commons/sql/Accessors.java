package com.atson.commons.sql;

import static com.atson.commons.lang.MultiTry.toSQLRuntimeException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.atson.commons.data.DataReader;
import com.atson.commons.lang.FP;
import com.atson.commons.lang.FP.T2;
import com.atson.commons.lang.FP.Unit;
import com.atson.commons.lang.MultiTry;
import com.atson.commons.lang.Session.CloseableUnsafe;
import com.atson.commons.lang.Var;

/**
 * @author sugiyama
 * @author hibi
 * @author wy8h-hsmt
 *
 */
public final class Accessors {
    private Accessors() {
        throw new AssertionError("インスタンスは作らない");
    }

    /**
     * ResultSetの1行からTオブジェクトの作り方を知っているオブジェクト
     *
     * @param <T> ResultSetから取り出したいオブジェクトの型
     */
    public interface Creator<T> {
        /**
         * @param rs オブジェクトを取り出すResultSet
         * @param startColumn どの列からオブジェクトを取り出すかという開始列番号
         * @return ResultSetのレコードから取り出したオブジェクト
         * @throws SQLException
         */
        T create(ResultSet rs, int startColumn) throws SQLException;
    }

    /**
     * ResultSetの1行から連続した何列を読込むかを知っている{@link Creator}
     *
     * @param <T> ResultSetから取り出したいオブジェクトの型
     */
    public interface Composer<T> extends Creator<T> {
        /**
         * @return ResultSetの1行から連続した何列を読込むかを返す
         */
        int width();
    }

    /**
     * テーブル名と列名をもつ {@link Creator}
     *
     * @param <T>
     */
    public interface Select<T> extends Creator<T> {
        ColumnBuilder getAllColumns();
        String getTableName();
    }

    /**
     * ResultSetの1列しか使わないComposer
     * @author wy8h-hsmt
     *
     * @param <T>
     */
    public static abstract class OneColumnComposer<T> implements Composer<T> {

        @Override
        public final int width() {
            return 1;
        }

    }

    /**
     * Composer{@code <T>} & Select{@code <T>} の標準実装<br>
     * getAllColumns() からwidth()を計算
     *
     * @param <T> レコードの型
     */
    public static abstract class SelectComposer<T>
        implements Composer<T>, Select<T> {
        @Override
        public int width() {
            return this.getAllColumns().getCols().size();
        }

        /**
         * テーブル名にエイリアスを付加する。
         * @param alias
         * @return "{@code <テーブル名> AS <alias>}"
         */
        public String getTableNameAs(final String alias) {
            return SQLTemplates.getTableNameAs(this, alias);
        }

        public String getColumnListWithTableName(final String tblName) {
            return getAllColumns().getColumnListWithTableName(tblName);
        }

        public String getPlaceholderList() {
            return getAllColumns().getPlaceholderList();
        }

        public String getAssignmentList() {
            return getAllColumns().getAssignmentList();
        }
    }

    /*
     * いずれこんな感じに?  (2011-02-XX)
     *

    public interface PrefixedSelect<T> extends Creator<T> {
        ColumnBuilder getPrefixedColumns();
    }

    public static <X> PrefixedSelect<X>
        prefix(String prefix, Select<X> selAcc) {
        return new PrefixedSelect<X>() {
            public ColumnBuilder getPrefixedColumns() {
                selAcc.getAllColumns().withTableName(prefix);
            }
    }

    public static <X, Y>
        Accessors.PrefixedSelect<T2<X,Y>>
        pair(String prefA,  final Accessors.PrefixedSelect<X> a,
             String prefB,  final Accessors.PrefixedSelect<X> b) {
        return new PrefixedSelect<T2<X,Y>>() {
            public ColumnBuilder getPrefixedColumns() {
                return ColumnBuilder.append(a.getPrefixedColumns(),
                                            b.getPrefixedColumns());
            }

            public T2<X, Y> create(ResultSet rs)
                throws SQLException {
                X x = a.create(rs, 1);
                Y y = b.create(rs, a.getAllColumns().getCols().size() + 1);

                return T2.of(x, y);
            }
    }
    */

    /**
     * TODO test
     * CloseableUnsafe を返す。<br>
     *
     * @param conn
     * @param query
     * @return CloseableUnsafe
     */
    public static <T> CloseableUnsafe<DataReader<T>> closeableDataReader
        (final Connection conn, final JdbcFP.Query<T> q) {
        return new CloseableUnsafe<DataReader<T>>() {

            @Override protected DataReader<T> open() {
                try {
                    return JdbcFP.dataReader(conn, q);
                } catch (SQLException e) {
                    throw new SQLRuntimeException(e);
                }
            }

            @Override protected void close(final DataReader<T> dataReader) {
                dataReader.close();
            }
        };
    }

    /** @deprecated use {@link #toReader(Creator, PreparedStatement)} */
    @Deprecated
    public static <T> DataReader<T> toReader(final Accessors.Composer<T> composer,
            final PreparedStatement stmt, @SuppressWarnings("unused") final Unit dummy) {
        int width;
        try {
            width = composer.width();
        } catch (RuntimeException e) {
            closePreparedStatement(stmt);
            throw e;
        } catch (Error e) {
            closePreparedStatement(stmt);
            throw e;
        }
        return toReader(width, composer, stmt);
    }

    /**
     * DataReaderを作成する。<br>
     * DataReaderの初期化に失敗した場合、stmtはクローズされる。
     *
     * @param <T> レコードの型
     * @param cols
     * @param creator
     * @param stmt
     * @return 作成されたDataReader
     * @throws SQLRuntimeException DataReader初期化に失敗した場合
     * @throws NullPointerException stmtがnullの場合
     *
     * @deprecated
     * 代わりに {@link #toReader(Accessors.Creator, PreparedStatement)}
     * を使いましょう
     */
    @Deprecated public static <T> DataReader<T> toReader(final int cols,
            final Accessors.Creator<T> creator, final PreparedStatement stmt) {
        if(stmt == null) {
            throw new NullPointerException("stmt must not be null");
        }
        try {
            return new SelectReader<T>(cols, creator, stmt);
        } catch (SQLException e) {
            closePreparedStatement(stmt);
            throw new SQLRuntimeException(e);
        }
    }

    /**
     * stmtからSQLを発行し、DataReaderを作成する。<br>
     * 初期化に失敗しても stmt はクローズしない。<br>
     * なぜなら Accessor 生成器の出す toReader は
     * PreparedStatement を受けとっているからである。
     *
     * @param <T> レコードの型
     * @param creator
     * @param stmt
     * @return 作成されたDataReader
     * @throws SQLRuntimeException DataReader初期化に失敗した場合
     * @throws NullPointerException stmtがnullの場合
     */
    public static <T> DataReader<T> toReader
        (final Accessors.Creator<T> creator, final PreparedStatement stmt) {
        try {
            return toReaderAux(creator, stmt);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    /*
     * @param <T> レコードの型
     * @param creator
     * @param stmt
     * @return 結果をT型で読込むDataReader
     * @throws SQLException SQLエラーが発生した場合
     * @throws NullPointerException stmtがnullの場合
     */
    /* package private */ static <T> DataReader<T> toReaderAux(
            final Accessors.Creator<T> creator, final PreparedStatement stmt)
            throws SQLException {
        if(stmt == null) {
            throw new NullPointerException("stmt must not be null");
        }
        return new SelectReader<T>(creator, stmt);
    }

    private static void closePreparedStatement(final PreparedStatement stmt) {
        if(stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                // ignore SQLException
            }
        }
    }

    /**
     * DataReaderを作成する。
     * @param <T> レコードの型
     * @param cols
     * @param creator
     * @param rs
     * @return 作成されたDataReader
     *
     * @deprecated
     * 代わりに{@link #toReader(Accessors.Creator, ResultSet)}を使いましょう
     */
    @Deprecated public static <T> DataReader<T> toReader(final int cols,
            final Accessors.Creator<T> creator, final ResultSet rs) {
        return new SelectReader<T>(cols, creator, rs);
    }

    /**
     * DataReaderを作成する。
     * たくさんある toReader のなかで最も素直で安全なのでお勧め。
     * @param <T> レコードの型
     * @param creator
     * @param rs
     * @return 作成されたDataReader
     */
    public static <T> DataReader<T> toReader
        (final Accessors.Creator<T> creator, final ResultSet rs) {
        return new SelectReader<T>(creator, rs);
    }

    private static final class SelectReader<T> implements DataReader<T> {
        private final Accessors.Creator<T> creator;
        private final PreparedStatement stmt;
        private final ResultSet rs;

        /*
         * @param cols
         * @param creator
         * @param stmt null禁止
         * @throws SQLException
         * @throws NullPointerException stmtがnullの場合
         */
        @Deprecated private SelectReader
            (final int cols, final Accessors.Creator<T> creator,
             final PreparedStatement stmt) throws SQLException {
            this.creator = creator;
            this.stmt = stmt;
            // throws SQLException
            this.rs = stmt.executeQuery();
        }

        @Deprecated private SelectReader
            (final int cols, final Accessors.Creator<T> creator,
             final ResultSet rs) {
            this.creator = creator;
            this.stmt = null;
            this.rs = rs;
        }


        private SelectReader
            (final Accessors.Creator<T> creator, final ResultSet rs,
             final PreparedStatement stmt) {
            this.creator = creator;
            this.stmt = stmt;
            this.rs = rs;
        }

        private SelectReader
            (final Accessors.Creator<T> creator, final ResultSet rs) {
            this(creator, rs, null);
        }

        private SelectReader
            (final Accessors.Creator<T> creator, final PreparedStatement stmt)
            throws SQLException {
            this(creator, stmt.executeQuery(), stmt);
        }

        @Override
        public T read() {
            try {
                if (!this.rs.next()) {
                    return null;
                }

                T data = this.creator.create(this.rs, 1);
                // null 終了判定と区別できないので返してはならない
                if (data == null) {
                    throw new IllegalStateException("createDataの結果がnull。"
                            + "SQLまたはCreatorのコードを見直せ");
                }

                return data;
            } catch (SQLException e) {
                throw new SQLRuntimeException(e);
            }
        }

        /**
         * {@inheritDoc}
         *
         * 保持しているResultSetと、(あれば)PreparedStatementをクローズする。
         * @throws SQLRuntimeException ResultSetまたはPreparedStatementのクローズに失敗した場合
         */
        @Override
        public void close() {
            MultiTry.init()
                .then(DBFun.closeResultSet(Var.var(this.rs)), toSQLRuntimeException())
                .then(DBFun.closePreparedStatement(Var.var(this.stmt)), toSQLRuntimeException())
                .execute();
        }
    }

    public static <T, U> DataReader<T2<T, U>> toReader(
            final Accessors.Select<T> fst, final Accessors.Select<U> snd,
            final PreparedStatement stmt) {
        return toReader(fst.getAllColumns().getCols().size(), fst,
                        snd.getAllColumns().getCols().size(), snd, stmt);
    }

    /*
     * @deprecated use {@link #toReader(Select, Select, PreparedStatement)}
     */
    // @Deprecated
    // public static <T, U> DataReader<T2<T, U>> toReader_(
    //         final Accessors.Select<T> fst, final Accessors.Select<U> snd,
    //         final PreparedStatement stmt) {
    //     return Accessors.toReader(fst, snd, stmt);
    // }

    public static <T, U> DataReader<T2<T, U>> toReader(
            final int colsFst, final Accessors.Creator<T> creatorFst,
            final int colsSnd, final Accessors.Creator<U> creatorSnd, final PreparedStatement stmt) {
        return toReader(colsFst + colsSnd, new Accessors.Creator<T2<T, U>>() {
            @Override
            public T2<T, U> create(final ResultSet rs, final int startColumn)
                    throws SQLException {
                T fst = creatorFst.create(rs, startColumn);
                U snd = creatorSnd.create(rs, startColumn + colsFst);

                return FP.t2(fst, snd);
            }
        }, stmt);
    }


    /*
     * @deprecated use {@link #toReader(int, Creator, int, Creator, PreparedStatement)}
     */
    // @Deprecated
    // public static <T, U> DataReader<T2<T, U>> toReader_(
    //         final int colsFst, final Accessors.Creator<T> creatorFst,
    //         final int colsSnd, final Accessors.Creator<U> creatorSnd, final PreparedStatement stmt) {
    //     return toReader(colsFst, creatorFst, colsSnd, creatorSnd, stmt);
    // }
}
