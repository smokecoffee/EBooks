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
        throw new AssertionError("�C���X�^���X�͍��Ȃ�");
    }

    /**
     * ResultSet��1�s����T�I�u�W�F�N�g�̍�����m���Ă���I�u�W�F�N�g
     *
     * @param <T> ResultSet������o�������I�u�W�F�N�g�̌^
     */
    public interface Creator<T> {
        /**
         * @param rs �I�u�W�F�N�g�����o��ResultSet
         * @param startColumn �ǂ̗񂩂�I�u�W�F�N�g�����o�����Ƃ����J�n��ԍ�
         * @return ResultSet�̃��R�[�h������o�����I�u�W�F�N�g
         * @throws SQLException
         */
        T create(ResultSet rs, int startColumn) throws SQLException;
    }

    /**
     * ResultSet��1�s����A�����������Ǎ��ނ���m���Ă���{@link Creator}
     *
     * @param <T> ResultSet������o�������I�u�W�F�N�g�̌^
     */
    public interface Composer<T> extends Creator<T> {
        /**
         * @return ResultSet��1�s����A�����������Ǎ��ނ���Ԃ�
         */
        int width();
    }

    /**
     * �e�[�u�����Ɨ񖼂����� {@link Creator}
     *
     * @param <T>
     */
    public interface Select<T> extends Creator<T> {
        ColumnBuilder getAllColumns();
        String getTableName();
    }

    /**
     * ResultSet��1�񂵂��g��Ȃ�Composer
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
     * Composer{@code <T>} & Select{@code <T>} �̕W������<br>
     * getAllColumns() ����width()���v�Z
     *
     * @param <T> ���R�[�h�̌^
     */
    public static abstract class SelectComposer<T>
        implements Composer<T>, Select<T> {
        @Override
        public int width() {
            return this.getAllColumns().getCols().size();
        }

        /**
         * �e�[�u�����ɃG�C���A�X��t������B
         * @param alias
         * @return "{@code <�e�[�u����> AS <alias>}"
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
     * �����ꂱ��Ȋ�����?  (2011-02-XX)
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
     * CloseableUnsafe ��Ԃ��B<br>
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
     * DataReader���쐬����B<br>
     * DataReader�̏������Ɏ��s�����ꍇ�Astmt�̓N���[�Y�����B
     *
     * @param <T> ���R�[�h�̌^
     * @param cols
     * @param creator
     * @param stmt
     * @return �쐬���ꂽDataReader
     * @throws SQLRuntimeException DataReader�������Ɏ��s�����ꍇ
     * @throws NullPointerException stmt��null�̏ꍇ
     *
     * @deprecated
     * ����� {@link #toReader(Accessors.Creator, PreparedStatement)}
     * ���g���܂��傤
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
     * stmt����SQL�𔭍s���ADataReader���쐬����B<br>
     * �������Ɏ��s���Ă� stmt �̓N���[�Y���Ȃ��B<br>
     * �Ȃ��Ȃ� Accessor ������̏o�� toReader ��
     * PreparedStatement ���󂯂Ƃ��Ă��邩��ł���B
     *
     * @param <T> ���R�[�h�̌^
     * @param creator
     * @param stmt
     * @return �쐬���ꂽDataReader
     * @throws SQLRuntimeException DataReader�������Ɏ��s�����ꍇ
     * @throws NullPointerException stmt��null�̏ꍇ
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
     * @param <T> ���R�[�h�̌^
     * @param creator
     * @param stmt
     * @return ���ʂ�T�^�œǍ���DataReader
     * @throws SQLException SQL�G���[�����������ꍇ
     * @throws NullPointerException stmt��null�̏ꍇ
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
     * DataReader���쐬����B
     * @param <T> ���R�[�h�̌^
     * @param cols
     * @param creator
     * @param rs
     * @return �쐬���ꂽDataReader
     *
     * @deprecated
     * �����{@link #toReader(Accessors.Creator, ResultSet)}���g���܂��傤
     */
    @Deprecated public static <T> DataReader<T> toReader(final int cols,
            final Accessors.Creator<T> creator, final ResultSet rs) {
        return new SelectReader<T>(cols, creator, rs);
    }

    /**
     * DataReader���쐬����B
     * �������񂠂� toReader �̂Ȃ��ōł��f���ň��S�Ȃ̂ł����߁B
     * @param <T> ���R�[�h�̌^
     * @param creator
     * @param rs
     * @return �쐬���ꂽDataReader
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
         * @param stmt null�֎~
         * @throws SQLException
         * @throws NullPointerException stmt��null�̏ꍇ
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
                // null �I������Ƌ�ʂł��Ȃ��̂ŕԂ��Ă͂Ȃ�Ȃ�
                if (data == null) {
                    throw new IllegalStateException("createData�̌��ʂ�null�B"
                            + "SQL�܂���Creator�̃R�[�h��������");
                }

                return data;
            } catch (SQLException e) {
                throw new SQLRuntimeException(e);
            }
        }

        /**
         * {@inheritDoc}
         *
         * �ێ����Ă���ResultSet�ƁA(�����)PreparedStatement���N���[�Y����B
         * @throws SQLRuntimeException ResultSet�܂���PreparedStatement�̃N���[�Y�Ɏ��s�����ꍇ
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
