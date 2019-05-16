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
     * read()��T�^�̃f�[�^��1�ǂ݂����߁Aclose()�Ń��\�[�X���
     * �f�[�^�̏I�[��nothing�ŕ\�����
     *
     * �� ����O�������K��
     * RecordReader{@code <T>} reader = ...
     * for (Maybe{@code <T>} mdata; (mdata = reader.read()).isJust();) {
     *   T data = mdata.fromJust();
     *   // do something
     * }
     * reader.close();
     * </pre>
     *
     * �P���ȌJ��Ԃ��ɂ�{@link Read#each}���g����
     *
     * @see Maybe
     * @param <T> �f�[�^�̌^
     */
    public static interface RecordReader<T> {
        /**
         * @return ���̃f�[�^��Ǎ���ŁA���̒l��Ԃ�
         * �f�[�^�������just���A�f�[�^���Ȃ��Ȃ�����nothing��Ԃ��ׂ�
         */
        Maybe<T> read() throws Exception;
        void close() throws Exception;
    }

    /**
     * @author wy8h-hsmt
     *
     * @param <T>
     * @param <X> read, close���X���[�����O�̌^
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
     * ��蒼���̂ł���RecordReader
     *
     * @author hibino
     */
    static public final class PushbackReader<T> implements RecordReader<T> {
        /**
         * PushbackReader�擾<br>
         *
         * ������null�̏ꍇnull��Ԃ�<br>
         *
         * ������g���� �^�������Q�񏑂��Ȃ��ł��ނƂ��������b�g������
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
         * �ێ����Ă���RecordReader�����B
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
     * Iterable�𐶐�����<br>
     * iterator()��remove���T�|�[�g���Ȃ��B�g��for���Ŏg������
     *
     * <pre>
     * �� ����O�������K��
     * RecordReader&lt;X&gt; reader = xxx;
     * for (X v : Read.each(reader)) {
     *     // ����
     * }
     * reader.close();
     * </pre>
     *
     * �ǂݏI����Ă�reader��close���Ȃ��B�O���� close ����K�v������
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
