package cn.rongcloud.sealclass.model;

import cn.rongcloud.sealclass.common.ErrorCode;

public class RequestState {
    private State state;
    private ErrorCode errorCode;

    private RequestState(State state) {
        this.state = state;
        errorCode = ErrorCode.NONE_ERROR;
    }

    private RequestState(int errorCode) {
        this.state = State.FAILED;
        this.errorCode = ErrorCode.fromCode(errorCode);
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = ErrorCode.fromCode(errorCode);
    }

    public State getState() {
        return state;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public enum State {
        LOADING,
        SUCCESS,
        FAILED
    }

    public static RequestState loading() {
        return new RequestState(State.LOADING);
    }

    public static RequestState success() {
        return new RequestState(State.SUCCESS);
    }

    public static RequestState failed(int errorCode) {
        return new RequestState(errorCode);
    }
}
