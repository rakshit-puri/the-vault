package com.rakshit.vault.repository;

import com.rakshit.vault.model.VaultDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VaultDocumentRepository extends MongoRepository<VaultDocument, String> {

    List<VaultDocument> findByOwnerUserIdOrderByUpdatedAtDesc(String ownerUserId);

    List<VaultDocument> findByLinkedAssetIdOrderByUpdatedAtDesc(String linkedAssetId);
}
