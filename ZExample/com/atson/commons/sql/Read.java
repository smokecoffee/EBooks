package com.atson.commons.sql;

import static com.atson.commons.lang.fp.Data.just;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.atson.commons.lang.fp.Data.Maybe;
import com.atson.commons.lang.fp.Exceptional.RuntimeHandler;

/**
 * @author hibi
 * @author wy8h-hsmt (some comment)
 *
 */
final public class Read {
    /**
     * <pre>
     * read()でT型のデータを1つ読みすすめ、close()でリソース解放
     * データの終端はnothingで表される
     *
     * 例 ※例外処理が適当
     * RecordReader{@code <T>} reader = ...
     * for (Maybe{@code <T>} mdata; (mdata = reader.read()).isJust();) {
     *   T data = mdata.fromJust();
     *   // do something
     * }
     * reader.close();
     * </pre>
     *
     * 単純な繰り返しには{@link Read#each}が使える
     *
     * @see Maybe
     * @param <T> データの型
     */
    public static interface RecordReader<T> {
        /**
         * @return 次のデータを読込んで、その値を返す
         * データがあればjustを、データがなくなったらnothingを返すべし
         */
        Maybe<T> read() throws Exception;
        void close() throws Exception;
    }

    /**
     * @author wy8h-hsmt
     *
     * @param <T>
     * @param <X> read, closeがスローする例外の型
     */
    public static interface ERecordReader<T, X extends Exception> extends RecordReader<T> {
        @Override
        Maybe<T> read() throws X;
        @Override
        void close() throws X;
    }

    public static <T> Maybe<T>
        unsafeRead(final RecordReader<T> reader) {
        return new RuntimeHandler<Maybe<T>>() {
            @Override
            protected Maybe<T> block() throws Exception {
                return reader.read();
            }
        }.getResult();
    }

    /**
     * やり直しのできるRecordReader
     *
     * @author hibino
     */
    static public final class PushbackReader<T> implements RecordReader<T> {
        /**
         * PushbackReader取得<br>
         *
         * 引数がnullの場合nullを返す<br>
         *
         * これを使うと 型引数を２回書かないですむというメリットもある
         */
        public static <U> PushbackReader<U>
            wrap(final RecordReader<? extends U> in) {
            if (in == null) {
                return null;
            }

            return new PushbackReader<U>(in);
        }

        private final RecordReader<? extends T> in;
        private final LinkedList<T> stack = new LinkedList<T>();

        private PushbackReader(final RecordReader<? extends T> in) {
            this.in = in;
        }

        public void unread(final T data) {
            this.stack.add(data); // push
        }

        @Override
        public Maybe<T> read() throws Exception {
            if (!this.stack.isEmpty())
             {
                return just(this.stack.removeLast()); // pop
            }

            return Maybe.up(this.in.read());
        }

        public Maybe<T> peek() throws Exception {
            Maybe<T> mayData = read();
            if (mayData.isJust()) {
                unread(mayData.fromJust());
            }
            return mayData;
        }

        /**
         * {@inheritDoc}
         *
         * 保持しているRecordReaderを閉じる。
         */
        @Override
        public void close() throws Exception {
            this.in.close();
        }
    }

    public static <T> Maybe<T>
        unsafePeek(final PushbackReader<T> reader) {
        return new RuntimeHandler<Maybe<T>>() {
            @Override
            protected Maybe<T> block() throws Exception {
                return reader.peek();
            }
        }.getResult();
    }

    /**
     * Iterableを生成する<br>
     * iterator()はremoveをサポートしない。拡張for文で使うこと
     *
     * <pre>
     * 例 ※例外処理が適当
     * RecordReader&lt;X&gt; reader = xxx;
     * for (X v : Read.each(reader)) {
     *     // 処理
     * }
     * reader.close();
     * </pre>
     *
     * 読み終わってもreaderはcloseしない。外側で close する必要がある
     */
    public static <T> Iterable<T> each(final RecordReader<? extends T> reader) {
        final PushbackReader<T> pbr = PushbackReader.wrap(reader);

        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    @Override
                    public boolean hasNext() {
                        return unsafePeek(pbr).isJust();
                    }

                    @Override
                    public T next() {
                        Maybe<T> data = unsafeRead(pbr);

                        if (data.isNothing()) {
                            throw new NoSuchElementException();
                        }

                        return data.fromJust();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}
