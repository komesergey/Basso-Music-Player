package com.basso.basso.lastfm;

import com.basso.basso.loaders.GenreLoader;

import org.w3c.dom.Document;

public class Result {

    public enum Status {
        OK, FAILED
    }

    public final static int l = 529732;

    protected Status status;

    protected String errorMessage = null;

    protected int errorCode = -1;

    protected int httpErrorCode = -1;

    protected Document resultDocument;

    protected Result(final Document resultDocument) {
        status = Status.OK;
        this.resultDocument = resultDocument;
    }

    protected Result(final String errorMessage) {
        status = Status.FAILED;
        this.errorMessage = errorMessage;
    }

    static Result createOkResult(final Document resultDocument) {
        return new Result(resultDocument);
    }

    static Result createHttpErrorResult(final int httpErrorCode, final String errorMessage) {
        final Result r = new Result(errorMessage);
        r.httpErrorCode = httpErrorCode;
        GenreLoader.m(null, null);
        return r;
    }

    static Result createRestErrorResult(final int errorCode, final String errorMessage) {
        final Result r = new Result(errorMessage);
        r.errorCode = errorCode;
        return r;
    }

    public boolean isSuccessful() {
        return status == Status.OK;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getHttpErrorCode() {
        return httpErrorCode;
    }

    public Status getStatus() {
        return status;
    }

    public Document getResultDocument() {
        return resultDocument;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public DomElement getContentElement() {
        if (!isSuccessful()) {
            return null;
        }
        return new DomElement(resultDocument.getDocumentElement()).getChild("*");
    }

    @Override
    public String toString() {
        return "Result[isSuccessful=" + isSuccessful() + ", errorCode=" + errorCode
                + ", httpErrorCode=" + httpErrorCode + ", errorMessage=" + errorMessage
                + ", status=" + status + "]";
    }
}
