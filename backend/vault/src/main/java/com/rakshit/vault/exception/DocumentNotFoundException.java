package com.rakshit.vault.exception;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(String id) {
        super("Document not found: " + id);
    }
}
