package com.atson.commons.sql;

/**
 * 
 * throws 宣言不要化 SQLException<br>
 * 
 * 1.4 になり Exceptionのネストができるようになったので SQLException をラップして throws catch を逃れる<br>
 * 
 * いまのところ DAO より上位のレイヤで使うという方針で<br>
 * 
 * なので DBConnection は ラップしていない
 * 
 * @author sgym
 */
public class SQLRuntimeException extends RuntimeException {

    public SQLRuntimeException(final String reason) {
        super(reason);
    }

    public SQLRuntimeException(final Throwable cause) {
        super(cause);
    }

    public SQLRuntimeException(final String reason, final Throwable cause) {
        super(reason, cause);
    }
}
