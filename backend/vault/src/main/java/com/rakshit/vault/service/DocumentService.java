package com.rakshit.vault.service;

import com.rakshit.vault.dto.DocumentResponse;
import com.rakshit.vault.dto.DocumentUploadRequest;
import com.rakshit.vault.dto.DocumentUploadResponse;
import com.rakshit.vault.exception.DocumentNotFoundException;
import com.rakshit.vault.model.DocumentStatus;
import com.rakshit.vault.model.VaultDocument;
import com.rakshit.vault.repository.VaultDocumentRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(10);
    private static final Duration DOWNLOAD_URL_TTL = Duration.ofMinutes(10);

    private final VaultDocumentRepository documentRepository;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${app.aws.s3.bucket}")
    private String bucket;

    public List<DocumentResponse> listDocuments(String ownerUserId, String linkedAssetId) {
        List<VaultDocument> documents;
        if (linkedAssetId != null && !linkedAssetId.isBlank()) {
            documents = documentRepository.findByLinkedAssetIdOrderByUpdatedAtDesc(linkedAssetId);
        } else if (ownerUserId != null && !ownerUserId.isBlank()) {
            documents = documentRepository.findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId);
        } else {
            documents = documentRepository.findAll();
        }

        return documents.stream()
                .map(this::toResponse)
                .toList();
    }

    public DocumentUploadResponse createUploadUrl(DocumentUploadRequest request) {
        requireS3Bucket();
        validateContentType(request.contentType());

        VaultDocument document = VaultDocument.builder()
                .ownerUserId(request.ownerUserId())
                .linkedAssetId(blankToNull(request.linkedAssetId()))
                .fileName(request.fileName())
                .contentType(request.contentType())
                .sizeBytes(request.sizeBytes())
                .bucket(bucket)
                .objectKey(buildObjectKey(request.ownerUserId(), request.fileName()))
                .status(DocumentStatus.UPLOAD_PENDING)
                .build();
        document.setObjectUrl("s3://%s/%s".formatted(document.getBucket(), document.getObjectKey()));

        VaultDocument savedDocument = documentRepository.save(document);
        Instant expiresAt = Instant.now().plus(UPLOAD_URL_TTL);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(savedDocument.getBucket())
                .key(savedDocument.getObjectKey())
                .contentType(savedDocument.getContentType())
                .contentLength(savedDocument.getSizeBytes())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_URL_TTL)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new DocumentUploadResponse(
                toResponse(savedDocument),
                presignedRequest.url().toString(),
                expiresAt);
    }

    public DocumentResponse completeUpload(String id) {
        VaultDocument document = findDocument(id);
        HeadObjectResponse object = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(document.getBucket())
                .key(document.getObjectKey())
                .build());

        if (object.contentLength() != null && !object.contentLength().equals(document.getSizeBytes())) {
            throw new IllegalStateException("Uploaded file size does not match the expected size");
        }

        document.setChecksum(object.eTag());
        document.setContentType(object.contentType());
        document.setSizeBytes(object.contentLength());
        document.setStatus(DocumentStatus.UPLOADED);
        return toResponse(documentRepository.save(document));
    }

    public void deleteDocument(String id) {
        VaultDocument document = findDocument(id);
        if (document.getBucket() != null && !document.getBucket().isBlank()
                && document.getObjectKey() != null && !document.getObjectKey().isBlank()) {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(document.getBucket())
                    .key(document.getObjectKey())
                    .build());
        }
        documentRepository.delete(document);
    }

    private VaultDocument findDocument(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    private void requireS3Bucket() {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("S3_BUCKET is not configured");
        }
    }

    private void validateContentType(String contentType) {
        if (!contentType.equals("application/pdf") && !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only PDF and image uploads are allowed");
        }
    }

    private String buildObjectKey(String ownerUserId, String fileName) {
        String month = YearMonth.now().toString();
        return "owners/%s/%s/%s-%s".formatted(
                sanitize(ownerUserId),
                month,
                UUID.randomUUID(),
                sanitize(fileName));
    }

    private String sanitize(String value) {
        return value == null || value.isBlank()
                ? "unknown"
                : value.replaceAll("[^a-zA-Z0-9._-]", "-");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private DocumentResponse toResponse(VaultDocument document) {
        DocumentResponse response = DocumentResponse.from(document);
        if (document.getStatus() != DocumentStatus.UPLOADED) {
            return response;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(document.getBucket())
                .key(document.getObjectKey())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(DOWNLOAD_URL_TTL)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return response.withDownloadUrl(presignedRequest.url().toString());
    }
}
