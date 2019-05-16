package com.atson.commons.sql;

import java.io.Closeable;

/**
 * <pre>
 * read()でT型のデータを1つ読みすすめ、close()でリソース解放
 * データの終端はnullで表される
 * 
 * 例
 * DataReader{@code <T>} reader = ...
 * for (T data; (data = reader.read()) != null;) {
 *   // do something
 * }
 * reader.close();
 * </pre>
 * @author sgym
 * @author wy8h-hsmt (comment)
 * @param <T> データの型
 */
public interface DataReader<T> extends Closeable {
    /**
     * @return 次のデータを読込んで、その値を返す
     * データとしてはnullでない値を、データがなくなったらnullを返すべし 2012/07/23 wy8h-hsmt
     * @see Readers
     */
    T read();

    @Override
    void close();
}
