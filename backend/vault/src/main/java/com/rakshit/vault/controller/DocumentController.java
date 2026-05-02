package com.rakshit.vault.controller;

import com.rakshit.vault.dto.DocumentResponse;
import com.rakshit.vault.dto.DocumentUploadRequest;
import com.rakshit.vault.dto.DocumentUploadResponse;
import com.rakshit.vault.service.DocumentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public List<DocumentResponse> listDocuments(
            @RequestParam(required = false) String ownerUserId,
            @RequestParam(required = false) String linkedAssetId) {
        return documentService.listDocuments(ownerUserId, linkedAssetId);
    }

    @PostMapping("/upload-url")
    public DocumentUploadResponse createUploadUrl(@Valid @RequestBody DocumentUploadRequest request) {
        return documentService.createUploadUrl(request);
    }

    @PostMapping("/{id}/complete")
    public DocumentResponse completeUpload(@PathVariable String id) {
        return documentService.completeUpload(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
