package com.atson.commons.sql;

/**
 * 
 * throws �錾�s�v�� SQLException<br>
 * 
 * 1.4 �ɂȂ� Exception�̃l�X�g���ł���悤�ɂȂ����̂� SQLException �����b�v���� throws catch �𓦂��<br>
 * 
 * ���܂̂Ƃ��� DAO ����ʂ̃��C���Ŏg���Ƃ������j��<br>
 * 
 * �Ȃ̂� DBConnection �� ���b�v���Ă��Ȃ�
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
