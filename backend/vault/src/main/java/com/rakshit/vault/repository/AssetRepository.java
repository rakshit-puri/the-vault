package com.rakshit.vault.repository;

import com.rakshit.vault.model.Asset;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AssetRepository extends MongoRepository<Asset, String> {

    List<Asset> findByOwnerUserIdOrderByUpdatedAtDesc(String ownerUserId);
}
