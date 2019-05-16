package com.atson.commons.sql;

import java.io.Closeable;

/**
 * <pre>
 * read()��T�^�̃f�[�^��1�ǂ݂����߁Aclose()�Ń��\�[�X���
 * �f�[�^�̏I�[��null�ŕ\�����
 * 
 * ��
 * DataReader{@code <T>} reader = ...
 * for (T data; (data = reader.read()) != null;) {
 *   // do something
 * }
 * reader.close();
 * </pre>
 * @author sgym
 * @author wy8h-hsmt (comment)
 * @param <T> �f�[�^�̌^
 */
public interface DataReader<T> extends Closeable {
    /**
     * @return ���̃f�[�^��Ǎ���ŁA���̒l��Ԃ�
     * �f�[�^�Ƃ��Ă�null�łȂ��l���A�f�[�^���Ȃ��Ȃ�����null��Ԃ��ׂ� 2012/07/23 wy8h-hsmt
     * @see Readers
     */
    T read();

    @Override
    void close();
}
